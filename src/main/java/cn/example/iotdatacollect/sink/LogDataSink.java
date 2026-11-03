package cn.example.iotdatacollect.sink;


import cn.example.iotdatacollect.model.DeviceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @description: TODO
 * @author: mario
 * @date: 2026/11/3
 */
@Component
public class LogDataSink implements DataSink {

    private static final Logger log = LoggerFactory.getLogger(LogDataSink.class);


    @Override
    public void publish(DeviceData data) {
        log.info("采集数据：{}", data);
    }
}
