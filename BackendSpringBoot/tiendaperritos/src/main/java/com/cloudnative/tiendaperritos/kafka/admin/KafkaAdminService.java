package com.cloudnative.tiendaperritos.kafka.admin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KafkaAdminService {

    private static final long TIMEOUT_SECONDS = 10;
    private final String bootstrapServers;

    public KafkaAdminService(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public List<KafkaTopicSummary> listarTopics() {
        try (AdminClient admin = crearAdmin()) {
            Set<String> names = admin.listTopics(new ListTopicsOptions().listInternal(false))
                    .names().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (names.isEmpty()) {
                return List.of();
            }
            Map<String, TopicDescription> descriptions =
                    admin.describeTopics(names).allTopicNames().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return names.stream().sorted().map(name -> {
                TopicDescription description = descriptions.get(name);
                int replicationFactor = description.partitions().isEmpty()
                        ? 0 : description.partitions().get(0).replicas().size();
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
                try {
                    Config config = admin.describeConfigs(List.of(resource))
                            .all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).get(resource);
                    Map<String, String> values = config.entries().stream()
                            .filter(entry -> !entry.isDefault())
                            .collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value));
                    return new KafkaTopicSummary(name, description.partitions().size(), replicationFactor, values);
                } catch (Exception e) {
                    throw new IllegalStateException("No se pudo consultar la configuración del topic " + name, e);
                }
            }).toList();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudieron listar los topics de Kafka", e);
        }
    }

    public void crearTopic(CreateKafkaTopicRequest request) {
        try (AdminClient admin = crearAdmin()) {
            NewTopic topic = new NewTopic(
                    request.name(), request.partitions(), (short) request.replicationFactor());
            if (request.configurations() != null && !request.configurations().isEmpty()) {
                topic.configs(request.configurations());
            }
            admin.createTopics(List.of(topic)).all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear el topic " + request.name(), e);
        }
    }

    public void actualizarTopic(String name, UpdateKafkaTopicRequest request) {
        try (AdminClient admin = crearAdmin()) {
            if (request.partitions() != null) {
                TopicDescription description = admin.describeTopics(List.of(name))
                        .allTopicNames().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).get(name);
                int actuales = description.partitions().size();
                if (request.partitions() < actuales) {
                    throw new IllegalArgumentException("Kafka no permite reducir la cantidad de particiones");
                }
                if (request.partitions() > actuales) {
                    admin.createPartitions(Map.of(name, NewPartitions.increaseTo(request.partitions())))
                            .all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
            }
            if (request.configurations() != null && !request.configurations().isEmpty()) {
                ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
                Collection<AlterConfigOp> operations = request.configurations().entrySet().stream()
                        .map(entry -> new AlterConfigOp(
                                new ConfigEntry(entry.getKey(), entry.getValue()),
                                AlterConfigOp.OpType.SET))
                        .toList();
                admin.incrementalAlterConfigs(Map.of(resource, operations))
                        .all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo actualizar el topic " + name, e);
        }
    }

    public void eliminarTopic(String name) {
        try (AdminClient admin = crearAdmin()) {
            admin.deleteTopics(List.of(name)).all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo eliminar el topic " + name, e);
        }
    }

    public List<KafkaConsumerGroupSummary> listarGrupos() {
        try (AdminClient admin = crearAdmin()) {
            Collection<ConsumerGroupListing> listings =
                    admin.listConsumerGroups().all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Set<String> ids = listings.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toSet());
            if (ids.isEmpty()) {
                return List.of();
            }
            Map<String, ConsumerGroupDescription> descriptions =
                    admin.describeConsumerGroups(ids).all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return descriptions.values().stream()
                    .map(this::resumirGrupo)
                    .sorted((a, b) -> a.groupId().compareTo(b.groupId()))
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudieron consultar los consumer groups", e);
        }
    }

    public KafkaConsumerGroupSummary describirGrupo(String groupId) {
        try (AdminClient admin = crearAdmin()) {
            ConsumerGroupDescription description = admin.describeConsumerGroups(List.of(groupId))
                    .all().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).get(groupId);
            if (description == null) {
                throw new IllegalArgumentException("No existe el consumer group " + groupId);
            }
            return resumirGrupo(description);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo consultar el consumer group " + groupId, e);
        }
    }

    private KafkaConsumerGroupSummary resumirGrupo(ConsumerGroupDescription description) {
        Set<String> topics = description.members().stream()
                .flatMap(member -> member.assignment().topicPartitions().stream())
                .map(TopicPartition::topic)
                .collect(Collectors.toCollection(TreeSet::new));
        return new KafkaConsumerGroupSummary(
                description.groupId(),
                description.state().toString(),
                description.members().size(),
                topics);
    }

    private AdminClient crearAdmin() {
        return AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }
}
