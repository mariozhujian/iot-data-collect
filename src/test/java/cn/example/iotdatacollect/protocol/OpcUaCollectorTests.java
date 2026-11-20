package cn.example.iotdatacollect.protocol;

import cn.example.iotdatacollect.config.DeviceConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OpcUaCollectorTests {

    @Test
    void rejectsNonPositiveIntervalBeforeConnecting() {
        DeviceConfig config = new DeviceConfig(
                "opc", DeviceConfig.Protocol.OPC_UA, "localhost", 4840, 0, 0, List.of());

        assertThrows(IllegalArgumentException.class,
                () -> new OpcUaCollector().start(config, data -> {}));
    }
}
