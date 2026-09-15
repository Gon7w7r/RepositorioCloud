
function ProductoForm({
  editandoId,
  nombre,
  descripcion,
  precio,
  stock,
  setNombre,
  setDescripcion,
  setPrecio,
  setStock,
  onGuardar,
  onCancelar,
}) {
  return (
    <section>
      <h2>
        {editandoId !== null
          ? `Editar producto #${editandoId}`
          : "Nuevo producto"}
      </h2>

      <div className="form-grid">
        <div>
          <label htmlFor="nombre">
            Nombre
          </label>

          <input
            type="text"
            id="nombre"
            value={nombre}
            onChange={(e) =>
              setNombre(e.target.value)
            }
            placeholder="Ej: Alimento Premium Cachorro"
          />
        </div>

        <div>
          <label htmlFor="descripcion">
            Descripción
          </label>

          <textarea
            id="descripcion"
            rows="2"
            value={descripcion}
            onChange={(e) =>
              setDescripcion(e.target.value)
            }
            placeholder="Ej: Sabor pollo, razas pequeñas"
          />
        </div>

        <div>
          <label htmlFor="precio">
            Precio
          </label>

          <input
            type="number"
            id="precio"
            step="0.01"
            min="0"
            value={precio}
            onChange={(e) =>
              setPrecio(e.target.value)
            }
            placeholder="Ej: 19990"
          />
        </div>

        <div>
          <label htmlFor="stock">
            Stock
          </label>

          <input
            type="number"
            id="stock"
            min="0"
            value={stock}
            onChange={(e) =>
              setStock(e.target.value)
            }
            placeholder="Ej: 10"
          />
        </div>
      </div>

      <div className="actions form-actions">
        <button onClick={onGuardar}>
          {editandoId !== null
            ? "Actualizar"
            : "Guardar"}
        </button>

        <button
          className="secondary"
          onClick={onCancelar}
        >
          Cancelar
        </button>
      </div>
    </section>
  );
}

export default ProductoForm;
