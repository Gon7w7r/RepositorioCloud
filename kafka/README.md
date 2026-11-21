# Kafka para Tienda Perritos

Este módulo reúne la infraestructura y los flujos Kafka adaptados al dominio de la tienda. RabbitMQ conserva sus colas de notificaciones y tickets; Kafka recibe eventos de producto, y Kafka Streams publica alertas cuando el stock cae bajo el umbral configurado.

## Infraestructura

Desde la raíz del repositorio, inicia ZooKeeper, dos brokers y Kafka UI:

    docker compose -f .\kafka\compose.yaml up -d
    docker compose -f .\kafka\compose.yaml ps

Kafka UI queda en http://localhost:8081. El backend se conecta a los listeners locales localhost:9092 y localhost:9094. Los brokers usan tres particiones y factor de replicación dos para los topics administrados por la aplicación.

## Topics y procesamiento

- tienda.productos.eventos: eventos tipados de producto, con tres particiones, dos réplicas y retención de un día.
- tienda.productos.alertas-stock: alertas que crea Kafka Streams para productos con menos de cinco unidades.
- tienda.productos.eventos.DLT: mensajes de producto que no pudieron procesarse después de tres intentos.

Los eventos se publican al crear, actualizar o eliminar productos. El consumidor de auditoría y el flujo Kafka Streams tienen identificadores de aplicación distintos y registran los eventos que procesan.

## Administración protegida

Las rutas requieren un JWT con el rol Admin:

- GET /api/kafka/admin/topics
- POST /api/kafka/admin/topics
- PATCH /api/kafka/admin/topics/{name}
- DELETE /api/kafka/admin/topics/{name}
- GET /api/kafka/admin/consumer-groups
- GET /api/kafka/admin/consumer-groups/{groupId}

El PATCH permite aumentar las particiones y cambiar configuraciones de topic. Kafka no permite reducir las particiones una vez creado el topic.

## Prueba de carga

Con el backend activo y una cuenta Admin, envía una petición POST a /api/kafka/load-test/start?count=100000. La respuesta incluye confirmaciones del broker, fallos, mensajes pendientes, tiempo y throughput. La configuración del productor agrupa mensajes en lotes de 32 KB, espera hasta 20 ms y usa Snappy.

Para observar lag, inicia el backend con el perfil kafka-lab. Ese perfil habilita un consumidor adicional que espera 10 ms por evento. Consulta sus offsets desde Kafka UI o el endpoint de consumer groups.

## Configuración externa

La URL y las credenciales de MySQL se pueden suministrar con DB_URL, DB_USERNAME y DB_PASSWORD. Para el entorno local se conservan los valores de application.properties. No incluir credenciales reales en archivos versionados.
