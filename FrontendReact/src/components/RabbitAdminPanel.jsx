import { useState } from 'react';
import { useMsal } from '@azure/msal-react';

const API = import.meta.env.VITE_API_BASE_URL;
import { protectedResources } from '../authConfig.js';

export default function RabbitAdminPanel() {
  const { instance, accounts } = useMsal();
  const [respuesta, setRespuesta] = useState(null);

  const [queue, setQueue] = useState({ name: '', durable: true, deadLetterExchange: '', deadLetterRoutingKey: '' });
  const [colaEliminar, setColaEliminar] = useState('');
  const [exchange, setExchange] = useState({ name: '', type: 'direct', durable: true });
  const [exchangeEliminar, setExchangeEliminar] = useState('');
  const [binding, setBinding] = useState({ queue: '', exchange: '', routingKey: '' });

  const llamar = async (metodo, ruta, body) => {
    try {
      const token = await instance.acquireTokenSilent({
        scopes: protectedResources.api.scopes,
        account: accounts[0],
      });
      const res = await fetch(`${API}${ruta}`, {
        method: metodo,
        headers: {
          Authorization: `Bearer ${token.accessToken}`,
          ...(body ? { 'Content-Type': 'application/json' } : {}),
        },
        body: body ? JSON.stringify(body) : undefined,
      });
      const texto = await res.text();
      setRespuesta({ ok: res.ok, status: res.status, texto });
    } catch (e) {
      setRespuesta({ ok: false, status: 0, texto: e.message });
    }
  };

  const campo = (estado, setEstado, nombre, etiqueta) => (
    <input
      placeholder={etiqueta}
      value={estado[nombre]}
      onChange={(e) => setEstado({ ...estado, [nombre]: e.target.value })}
    />
  );

  return (
    <section>
      <h2>Administración de RabbitMQ</h2>

      <h3>Colas</h3>
      {campo(queue, setQueue, 'name', 'Nombre de la cola')}
      {campo(queue, setQueue, 'deadLetterExchange', 'Dead letter exchange (opcional)')}
      {campo(queue, setQueue, 'deadLetterRoutingKey', 'Dead letter routing key (opcional)')}
      <label>
        <input type="checkbox" checked={queue.durable}
          onChange={(e) => setQueue({ ...queue, durable: e.target.checked })} />
        Durable
      </label>
      <button onClick={() => llamar('POST', '/api/rabbit/queues', queue)}>Crear cola</button>
      <div>
        <input placeholder="Cola a eliminar" value={colaEliminar}
          onChange={(e) => setColaEliminar(e.target.value)} />
        <button onClick={() => llamar('DELETE', `/api/rabbit/queues/${encodeURIComponent(colaEliminar)}`)}>
          Eliminar cola
        </button>
      </div>

      <h3>Exchanges</h3>
      {campo(exchange, setExchange, 'name', 'Nombre del exchange')}
      <select value={exchange.type} onChange={(e) => setExchange({ ...exchange, type: e.target.value })}>
        <option value="direct">direct</option>
        <option value="topic">topic</option>
        <option value="fanout">fanout</option>
      </select>
      <label>
        <input type="checkbox" checked={exchange.durable}
          onChange={(e) => setExchange({ ...exchange, durable: e.target.checked })} />
        Durable
      </label>
      <button onClick={() => llamar('POST', '/api/rabbit/exchanges', exchange)}>Crear exchange</button>
      <div>
        <input placeholder="Exchange a eliminar" value={exchangeEliminar}
          onChange={(e) => setExchangeEliminar(e.target.value)} />
        <button onClick={() => llamar('DELETE', `/api/rabbit/exchanges/${encodeURIComponent(exchangeEliminar)}`)}>
          Eliminar exchange
        </button>
      </div>

      <h3>Bindings</h3>
      {campo(binding, setBinding, 'queue', 'Cola')}
      {campo(binding, setBinding, 'exchange', 'Exchange')}
      {campo(binding, setBinding, 'routingKey', 'Routing key')}
      <button onClick={() => llamar('POST', '/api/rabbit/bindings', binding)}>Crear binding</button>
      <button onClick={() => llamar('DELETE', '/api/rabbit/bindings', binding)}>Eliminar binding</button>

      {respuesta && (
        <pre style={{ color: respuesta.ok ? 'green' : 'crimson' }}>
          {respuesta.status} {respuesta.texto}
        </pre>
      )}
    </section>
  );
}