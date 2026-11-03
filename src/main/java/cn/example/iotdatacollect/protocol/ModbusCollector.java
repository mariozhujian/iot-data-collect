package cn.example.iotdatacollect.protocol;


import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.model.DeviceData;
import cn.example.iotdatacollect.model.DevicePoint;
import org.eclipse.milo.opcua.sdk.core.typetree.DataType;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @description: Modbus TCP 采集实现
 * 这里使用一个设备对应一个 ModbusMaster，定时读取寄存器，而不是每次采集都重新连接。
 * @author: mario
 * @date: 2026/11/3
 */
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class ModbusCollector implements DeviceCollector{

    private ModbusMaster master;
    private ScheduledExecutorService scheduler;

    @Override
    public DeviceConfig.Protocol protocol() {
        return DeviceConfig.Protocol.MODBUS;
    }

    @Override
    public void start(DeviceConfig config, Consumer<DeviceData> consumer) throws Exception {
        IpParameters params = new IpParameters();
        params.setHost(config.host());
        params.setPort(config.port());

        master = new ModbusFactory()
                .createTcpMaster(params, true);

        master.setTimeout(3000);
        master.setRetries(1);
        master.init();

        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(() -> {
            for (DevicePoint point : config.points()) {
                try {
                    int offset = Integer.parseInt(point.address());

                    // 示例：读取 Holding Register 的有符号 16 位整数
                    Number value = master.getValue(
                            BaseLocator.holdingRegister(
                                    config.slaveId(),
                                    offset,
                                    DataType.TWO_BYTE_INT_SIGNED
                            )
                    );

                    consumer.accept(new DeviceData(
                            config.deviceId(),
                            point.pointId(),
                            value,
                            Instant.now(),
                            "GOOD"
                    ));

                } catch (Exception e) {
                    // 单点异常不能中断整台设备的采集
                    System.err.println(
                            "Modbus 采集失败: " + point.pointId()
                                    + " - " + e.getMessage()
                    );
                }
            }
        }, 0, config.intervalMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (master != null) {
            master.destroy();
        }
    }
}
