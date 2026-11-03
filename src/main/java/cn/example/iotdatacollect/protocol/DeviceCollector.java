package cn.example.iotdatacollect.protocol;


import cn.example.iotdatacollect.config.DeviceConfig;
import cn.example.iotdatacollect.model.DeviceData;

import java.util.function.Consumer;

/**
 * @description: 定义统一协议接口
 * @author: mario
 * @date: 2026/11/3
 */
public interface DeviceCollector extends AutoCloseable{

    /**
     * 协议
     * @return
     */
    DeviceConfig.Protocol protocol();

    /**
     *
     * @param config
     * @param consumer
     * @throws Exception
     */
    void start(DeviceConfig config, Consumer<DeviceData> consumer) throws Exception;

    /**
     * 断开连接
     * @throws Exception
     */
    @Override
    public void close() throws Exception;
}
