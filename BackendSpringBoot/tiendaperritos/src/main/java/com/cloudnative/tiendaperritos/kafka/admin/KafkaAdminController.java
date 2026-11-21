package com.cloudnative.tiendaperritos.kafka.admin;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka/admin")
public class KafkaAdminController {

    private final KafkaAdminService service;

    public KafkaAdminController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping("/topics")
    public List<KafkaTopicSummary> listarTopics() {
        return service.listarTopics();
    }

    @PostMapping("/topics")
    public ResponseEntity<String> crearTopic(@Valid @RequestBody CreateKafkaTopicRequest request) {
        service.crearTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Topic creado: " + request.name());
    }

    @PatchMapping("/topics/{name}")
    public ResponseEntity<String> actualizarTopic(
            @PathVariable String name,
            @Valid @RequestBody UpdateKafkaTopicRequest request) {
        service.actualizarTopic(name, request);
        return ResponseEntity.ok("Topic actualizado: " + name);
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<String> eliminarTopic(@PathVariable String name) {
        service.eliminarTopic(name);
        return ResponseEntity.ok("Topic eliminado: " + name);
    }

    @GetMapping("/consumer-groups")
    public List<KafkaConsumerGroupSummary> listarGrupos() {
        return service.listarGrupos();
    }

    @GetMapping("/consumer-groups/{groupId}")
    public KafkaConsumerGroupSummary describirGrupo(@PathVariable String groupId) {
        return service.describirGrupo(groupId);
    }
}
