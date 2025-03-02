package com.chargepoint.csms.authentication;

import com.chargepoint.csms.lib.AuthenticationResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Testcontainers
public class TestContainerBase {

    @Autowired
    private KafkaProperties kafkaProperties;

    protected KafkaConsumer<String, AuthenticationResponse> consumer;

    @Container
    static final ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
                    .asCompatibleSubstituteFor("apache/kafka")
    );

    protected static Connection connection;

    @Container
    static final MySQLContainer mySQLContainer =  new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("driverDb")
            .withUsername("root")
            .withPassword("rootPass")
            .withInitScript("mock-data.sql");  // Loads mock data

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "*");
    }

    @DynamicPropertySource
    static void overrideMysqlProperties(DynamicPropertyRegistry registry) {
        String modifiedr2dbcUrl = mySQLContainer.getJdbcUrl()
                .replace("jdbc", "r2dbc")
                .concat("?sslMode=DISABLED");
        registry.add("spring.r2dbc.url",() -> modifiedr2dbcUrl);
        registry.add("spring.r2dbc.username", () -> "driver_user");
        registry.add("spring.r2dbc.password", () -> "userPass");
    }

    @BeforeAll
    public static void setUp() {
        try {
            connection = DriverManager.getConnection(
                mySQLContainer.getJdbcUrl(),
                mySQLContainer.getUsername(),
                mySQLContainer.getPassword()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @BeforeEach
    public void kafkaConsumer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  kafkaProperties.getConsumer().getKeyDeserializer());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getValueDeserializer());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group-" + UUID.randomUUID());
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("authentication-responses"));
    }

    @AfterEach
    public void refreshDB() {
        ScriptUtils.executeSqlScript(
                connection,
                new ClassPathResource("refresh-db.sql")
        );
    }

    @AfterEach
    public void resetKafkaConsumer() {
        Set<TopicPartition> partitions = consumer.assignment();
        consumer.seekToBeginning(partitions);
        consumer.close();
    }
}
