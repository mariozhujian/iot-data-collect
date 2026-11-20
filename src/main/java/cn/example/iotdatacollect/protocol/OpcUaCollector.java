package cn.example.iotdatacollect.protocol;


import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.model.DeviceData;
import cn.example.iotdatacollect.model.DevicePoint;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @description: Eclipse Milo 连接 OPC UA Server。
 * @author: mario
 * @date: 2026/11/3
 */
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class OpcUaCollector implements DeviceCollector {

    private OpcUaClient client;
    private ScheduledExecutorService scheduler;

    @Override
    public DeviceConfig.Protocol protocol() {
        return DeviceConfig.Protocol.OPC_UA;
    }

    @Override
    public void start(DeviceConfig config, Consumer<DeviceData> consumer) throws Exception {
        if (config.intervalMs() <= 0) {
            throw new IllegalArgumentException("OPC UA 采集间隔必须大于 0");
        }
        // 1. 连接 OPC UA Server
        String endpoint = "opc.tcp://" + config.host() + ":" + config.port();
        client = OpcUaClient.create(endpoint);
        client.connect();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            for (DevicePoint point : config.points()) {
                try {
                    // 2. 建立 Session
                    // 3. 创建 Subscription
                    // 4. 监控指定 NodeId
                    // 5. 节点数据变化时输出
                    DataValue dataValue = client.readValue(
                            0, TimestampsToReturn.Both, NodeId.parse(point.address()));
                    StatusCode status = dataValue.getStatusCode();
                    String quality = status == null || status.isGood() ? "GOOD"
                            : status.isBad() ? "BAD" : "UNCERTAIN";

                    consumer.accept(new DeviceData(
                            config.deviceId(),
                            point.pointId(),
                            dataValue.getValue() == null ? null : dataValue.getValue().getValue(),
                            Instant.now(),
                            quality
                    ));
                } catch (Exception e) {
                    System.err.println("OPC UA 采集失败: " + point.pointId()
                            + " - " + e.getMessage());
                }
            }
        }, 0, config.intervalMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws Exception {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (client != null) {
            client.disconnect();
        }
    }
}
