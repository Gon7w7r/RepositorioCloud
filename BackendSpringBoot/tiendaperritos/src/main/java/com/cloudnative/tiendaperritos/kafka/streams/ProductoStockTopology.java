package com.cloudnative.tiendaperritos.kafka.streams;

import com.cloudnative.tiendaperritos.kafka.eventos.AlertaStock;
import com.cloudnative.tiendaperritos.kafka.eventos.ProductoEvento;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

@Configuration
@EnableKafkaStreams
public class ProductoStockTopology {

    @Bean
    KStream<String, ProductoEvento> productoStockStream(
            StreamsBuilder builder,
            @Value("${app.kafka.topics.product-events}") String eventosTopic,
            @Value("${app.kafka.topics.stock-alerts}") String alertasTopic,
            @Value("${app.kafka.minimum-stock-alert:5}") int stockMinimo) {

        JacksonJsonSerde<ProductoEvento> productoSerde = new JacksonJsonSerde<>(ProductoEvento.class);
        JacksonJsonSerde<AlertaStock> alertaSerde = new JacksonJsonSerde<>(AlertaStock.class);

        KStream<String, ProductoEvento> eventos = builder.stream(
                eventosTopic, Consumed.with(Serdes.String(), productoSerde));

        eventos.filter((key, evento) -> evento != null
                        && evento.stock() != null
                        && evento.stock() < stockMinimo)
                .selectKey((key, evento) -> String.valueOf(evento.productoId()))
                .mapValues(evento -> new AlertaStock(
                        evento.eventoId(),
                        evento.productoId(),
                        evento.nombre(),
                        evento.stock(),
                        evento.stock() == 0 ? "CRITICA" : "BAJA",
                        evento.fechaEvento()))
                .to(alertasTopic, Produced.with(Serdes.String(), alertaSerde));

        return eventos;
    }
}
