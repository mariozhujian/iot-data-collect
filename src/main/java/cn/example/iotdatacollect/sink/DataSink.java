package cn.example.iotdatacollect.sink;

import cn.example.iotdatacollect.model.DeviceData;

/**
 * 统一数据出口
 */
@FunctionalInterface
public interface DataSink {
    void publish(DeviceData data);
}
