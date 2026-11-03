package cn.example.iotdatacollect.manager;


import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.protocol.DeviceCollector;
import cn.example.iotdatacollect.protocol.ModbusCollector;
import cn.example.iotdatacollect.protocol.MqttCollector;
import cn.example.iotdatacollect.sink.DataSink;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @description: TODO
 * @author: mario
 * @date: 2026/11/3
 */
@Service
public class CollectionManager {

    private final Map<DeviceConfig.Protocol,
            Supplier<DeviceCollector>> factories;

    private final Map<String, DeviceCollector> running =
            new ConcurrentHashMap<>();

    private final DataSink sink;


    public CollectionManager(DataSink sink,
                             ApplicationContext context) {
        this.sink = sink;

        factories = Arrays.stream(DeviceConfig.Protocol.values())
                .collect(Collectors.toMap(
                        p -> p,
                        p -> () -> switch (p) {
                            case MODBUS -> context.getBean(
                                    cn.example.iotdatacollect.protocol.ModbusCollector.class);
                            case MQTT -> context.getBean(
                                    cn.example.iotdatacollect.protocol.MqttCollector.class);
                            case OPC_UA -> context.getBean(
                                    cn.example.iotdatacollect.protocol.OpcUaCollector.class);
                        }
                ));
    }

    public synchronized void start(DeviceConfig config) throws Exception {
        if (running.containsKey(config.deviceId())) {
            throw new IllegalStateException("设备已运行");
        }

        DeviceCollector collector = factories.get(config.protocol()).get();

        try {
            collector.start(config, sink::publish);
            running.put(config.deviceId(), collector);
        } catch (Exception e) {
            collector.close();
            throw e;
        }
    }

    public synchronized void stop(String deviceId) throws Exception {
        DeviceCollector collector = running.remove(deviceId);
        if (collector != null) {
            collector.close();
        }
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() throws Exception {
        for (String deviceId : List.copyOf(running.keySet())) {
            stop(deviceId);
        }
    }
}
