package cn.example.iotdatacollect.protocol;


import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.model.DeviceData;
import cn.example.iotdatacollect.model.DevicePoint;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @description: MQTT 与 Modbus 的最大区别是：MQTT 不需要定时发起采集请求，设备主动上报消息，客户端通过 Topic 接收。
 *
 * 需要继续优化的地方：
 * - 当前回调返回的是原始字符串，实际应根据 JSON Path 或字段映射解析温度、压力、转速等数据。
 * - Topic 可能使用 + 或 # 通配符，当前的 equals() 只适用于精确 Topic。
 * - 生产环境建议使用稳定的 Client ID，并合理设置持久会话和 QoS。
 * - 当前没有显式约束回调执行时间，不能在回调中同步写入慢速数据库。
 * @author: mario
 * @date: 2026/11/3
 */
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class MqttCollector implements DeviceCollector {

    private MqttAsyncClient client;


    @Override
    public DeviceConfig.Protocol protocol() {
        return DeviceConfig.Protocol.MQTT;
    }

    @Override
    public void start(DeviceConfig config, Consumer<DeviceData> consumer) throws Exception {
        String broker = "tcp://" + config.host() + ":" + config.port();
        // 1. 连接 MQTT Broker
        client = new MqttAsyncClient(
                broker,
                "collector-" + UUID.randomUUID()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(5);
        options.setKeepAliveInterval(30);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                //这里利用 Eclipse Paho 自带的自动重连机制，而不是自己重复编写重连线程。
                // Paho 会尝试自动重连
            }

            @Override
            public void messageArrived(
                    String topic,
                    MqttMessage message) {
                // 3. 接收设备上报的消息
                String payload = new String(message.getPayload(),  StandardCharsets.UTF_8);
                // 2. 订阅配置的 Topic
                for (DevicePoint point : config.points()) {
                    if (!point.address().equals(topic)) {
                        continue;
                    }
                    // 4. 将消息解析成 DeviceData
                    // 5. 通过 consumer 输出
                    consumer.accept(new DeviceData(
                            config.deviceId(),
                            point.pointId(),
                            payload,
                            Instant.now(),
                            "GOOD"
                    ));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 采集端暂不发布 MQTT 消息
            }
        });

        client.connect(options).waitForCompletion(5000);

        for (DevicePoint point : config.points()) {
            client.subscribe(point.address(), 1).waitForCompletion(5000);
        }
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            if (client.isConnected()) {
                client.disconnect().waitForCompletion(3000);
            }
            client.close();
        }
    }
}
