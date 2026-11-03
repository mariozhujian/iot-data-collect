package cn.example.iotdatacollect.model;

import java.time.Instant;

/**
 * 统一采集结果
 */
public record DeviceData(
        String deviceId,
        String pointId,
        Object value,
        Instant timestamp,
        String quality
) {
}
