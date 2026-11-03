package cn.example.iotdatacollect.protocol;


import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.model.DeviceData;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
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
        String endpoint = "opc.tcp://" + config.host() + ":" + config.port();

        client = OpcUaClient.create(endpoint,
                endpoints -> endpoints.stream()
                        .fielt);
    }

    @Override
    public void close() throws Exception {

    }
}
