package cn.example.iotdatacollect.config;


import cn.example.iotdatacollect.model.DevicePoint;

import java.util.List;

/**
 * @description: TODO
 * @author: mario
 * @date: 2026/11/3
 */
public record DeviceConfig(
        String deviceId,
        Protocol protocol,
        String host,
        int port,
        int slaveId,
        long intervalMs,
        List<DevicePoint> points
) {

    public enum Protocol {
        MODBUS, MQTT, OPC_UA
    }
}
