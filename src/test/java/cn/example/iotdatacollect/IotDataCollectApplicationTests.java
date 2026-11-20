package cn.example.iotdatacollect;

import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.manager.CollectionManager;
import cn.example.iotdatacollect.sink.DataSink;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(IotDataCollectApplicationTests.TestConfig.class)
class IotDataCollectApplicationTests {

    @Autowired
    TestCollectionManager manager;

    @Test
    void startsConfiguredDevices() {
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                assertEquals(Set.of("PLC_001", "MQTT_001", "OPC_001"), Set.copyOf(manager.started)));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        TestCollectionManager testCollectionManager(DataSink sink, ApplicationContext context) {
            return new TestCollectionManager(sink, context);
        }
    }

    static class TestCollectionManager extends CollectionManager {
        final List<String> started = new CopyOnWriteArrayList<>();

        TestCollectionManager(DataSink sink, ApplicationContext context) {
            super(sink, context);
        }

        @Override
        public synchronized void start(DeviceConfig config) {
            started.add(config.deviceId());
        }
    }

}
