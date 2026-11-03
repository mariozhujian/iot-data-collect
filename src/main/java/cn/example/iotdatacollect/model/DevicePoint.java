package cn.example.iotdatacollect.model;


/**
 * @description: 统一设备配置
 * @author: mario
 * @date: 2026/11/3
 */
public record DevicePoint(
        String pointId,
        String address,
        String dataType
) {}
