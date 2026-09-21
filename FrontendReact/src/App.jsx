import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";
import { protectedResources } from "./authConfig.js";
import "./App.css";
import { Routes, Route, Link } from "react-router-dom";
import ProtectedPage from "./components/ProtectedPage";
import ProductoTable from "./components/ProductoTable";
import ProductoForm from "./components/ProductoForm";
import AuthButtons from "./components/AuthButtons";
import {
  AuthenticatedTemplate,
  UnauthenticatedTemplate,
} from "@azure/msal-react";

const API_BASE = `${import.meta.env.VITE_API_BASE_URL}/api/data`;


function App() {
  const [productos, setProductos] = useState([]);

  const [editandoId, setEditandoId] = useState(null);
  const { instance, accounts } = useMsal();
  const [nombre, setNombre] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [precio, setPrecio] = useState("");
  const [stock, setStock] = useState("");

  const [status, setStatus] = useState("");
  const [statusTipo, setStatusTipo] = useState("ok");

  async function fetchAutenticado(url, options = {}) {
  const token = await obtenerToken();

  return fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${token}`,
    },
  });
}

  function setStatusMessage(mensaje, tipo = "ok") {
    setStatus(mensaje);
    setStatusTipo(tipo);
  }

    async function obtenerToken() {
    const response = await instance.acquireTokenSilent({
      scopes: protectedResources.api.scopes,
      account: accounts[0],
    });

    return response.accessToken;
  }


    async function cargarProductos() {
  try {
      if (accounts.length === 0) {
    setStatusMessage(
      "Debes iniciar sesión para cargar los productos.",
      "error"
    );
    return;
  }

    const token = await obtenerToken();

    const res = await fetch(API_BASE, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!res.ok) throw new Error("Error al cargar productos");

    const data = await res.json();
    setProductos(data);
    setStatusMessage("Productos cargados correctamente.", "ok");
  } catch (error) {
    console.error(error);
    setStatusMessage(
      "No se pudieron cargar los productos. ¿Está el backend levantado?",
      "error"
    );
  }
}

  function limpiarFormulario() {
    setEditandoId(null);
    setNombre("");
    setDescripcion("");
    setPrecio("");
    setStock("");
  }

  function validarProducto() {
    if (!nombre.trim()) {
      return "El nombre es obligatorio.";
    }

    if (
      precio === "" ||
      Number(precio) < 0 ||
      isNaN(Number(precio))
    ) {
      return "El precio debe ser un número mayor o igual a 0.";
    }

    if (
      stock === "" ||
      Number(stock) < 0 ||
      isNaN(Number(stock))
    ) {
      return "El stock debe ser un número mayor o igual a 0.";
    }

    return null;
  }

  async function guardarProducto() {
    const error = validarProducto();

    if (error) {
      setStatusMessage(error, "error");
      return;
    }

    const producto = {
      nombre: nombre.trim(),
      descripcion: descripcion.trim(),
      precio: Number(precio),
      stock: Number(stock),
    };

    try {
      let res;

      if (editandoId !== null) {
      res = await fetchAutenticado(`${API_BASE}/${editandoId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(producto),
      });
    } else {
      res = await fetchAutenticado(API_BASE, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(producto),
      });
    }

      if (!res.ok) {
        const data = await res
          .json()
          .catch(() => ({}));

        throw new Error(
          data.message ||
            "Error al guardar el producto"
        );
      }

      if (editandoId !== null) {
        setStatusMessage(
          "Producto actualizado correctamente.",
          "ok"
        );
      } else {
        setStatusMessage(
          "Producto creado correctamente.",
          "ok"
        );
      }

      limpiarFormulario();

      await cargarProductos();
    } catch (error) {
      console.error(error);

      setStatusMessage(
        "Ocurrió un error al guardar el producto.",
        "error"
      );
    }
  }

  async function editarProducto(id) {
    try {
      const res = await fetchAutenticado(`${API_BASE}/${id}`);

      if (!res.ok) {
        throw new Error(
          "No se pudo obtener el producto"
        );
      }

      const producto = await res.json();

      setEditandoId(producto.id);
      setNombre(producto.nombre);
      setDescripcion(
        producto.descripcion || ""
      );
      setPrecio(producto.precio);
      setStock(producto.stock);

      setStatusMessage(
        "Editando producto.",
        "ok"
      );
    } catch (error) {
      console.error(error);

      setStatusMessage(
        "No se pudo cargar el producto para editarlo.",
        "error"
      );
    }
  }

  async function eliminarProducto(id) {
  try {
    const res = await fetchAutenticado(`${API_BASE}/${id}`, {
      method: "DELETE",
    });

    if (!res.ok) {
      throw new Error("Error al eliminar producto");
    }

    await cargarProductos();
    setStatusMessage("Producto eliminado correctamente.", "ok");
  } catch (error) {
    console.error(error);
    setStatusMessage("No se pudo eliminar el producto.", "error");
  }
}

  function confirmarEliminar(id) {
    if (
      window.confirm(
        "¿Seguro que deseas eliminar este producto?"
      )
    ) {
      eliminarProducto(id);
    }
  }

  function cancelarEdicion() {
    limpiarFormulario();

    setStatusMessage(
      "Edición cancelada.",
      "ok"
    );
  }

  useEffect(() => {
    cargarProductos();
  }, []);

  return (
  <Routes>
    <Route
      path="/"
      element={
        <>
          <header>
            <h1>Tienda de Alimentos para Perritos 🐶</h1>

            <p>
              Gestión CRUD de productos en contenedores Docker (Frontend + Backend + MySQL)
            </p>

            <AuthButtons />

            <AuthenticatedTemplate>
              <p>Usuario autenticado.</p>
            </AuthenticatedTemplate>

            <UnauthenticatedTemplate>
              <p>Debes iniciar sesión para acceder a las funciones protegidas.</p>
            </UnauthenticatedTemplate>

            <Link to="/protegida">
            <button>Ir a página protegida</button>
            </Link>


          </header>

          <main>
            <section>
              <h2>Productos</h2>

              <div className="actions">
                <button onClick={cargarProductos}>
                  Cargar Productos
                </button>
              </div>

              <ProductoTable
                productos={productos}
                onEditar={editarProducto}
                onEliminar={confirmarEliminar}
              />
            </section>

            <ProductoForm
              editandoId={editandoId}
              nombre={nombre}
              descripcion={descripcion}
              precio={precio}
              stock={stock}
              setNombre={setNombre}
              setDescripcion={setDescripcion}
              setPrecio={setPrecio}
              setStock={setStock}
              onGuardar={guardarProducto}
              onCancelar={cancelarEdicion}
            />

            {status && (
              <div className={`status ${statusTipo}`}>
                {status}
              </div>
            )}
          </main>
        </>
      }
    />

    <Route
      path="/protegida"
      element={<ProtectedPage />}
    />
  </Routes>
);
}

export default App;


