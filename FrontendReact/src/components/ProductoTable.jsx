
function ProductoTable({
  productos,
  onEditar,
  onEliminar,
}) {
  return (
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Descripción</th>
          <th>Precio</th>
          <th>Stock</th>
          <th>Acciones</th>
        </tr>
      </thead>

      <tbody>
        {productos.map((producto) => (
          <tr key={producto.id}>
            <td>{producto.id}</td>

            <td>{producto.nombre}</td>

            <td>
              {producto.descripcion || ""}
            </td>

            <td>
              ${Number(producto.precio).toFixed(2)}
            </td>

            <td>{producto.stock}</td>

            <td>
              <button
                onClick={() =>
                  onEditar(producto.id)
                }
              >
                Editar
              </button>

              <button
                className="danger"
                onClick={() =>
                  onEliminar(producto.id)
                }
              >
                Eliminar
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default ProductoTable;

