package cn.example.iotdatacollect.manager;

import cn.example.iotdatacollect.config.DeviceConfig;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 设备采集
 */
@Component
public class CollectionStartup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CollectionStartup.class);

    private final CollectionManager manager;
    private final Environment environment;
    private ScheduledExecutorService scheduler;

    public CollectionStartup(CollectionManager manager, Environment environment) {
        this.manager = manager;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<DeviceConfig> devices = Binder.get(environment)
                .bind("industrial.devices", Bindable.listOf(DeviceConfig.class))
                .orElse(List.of());
        if (devices.isEmpty()) {
            log.warn("未配置采集设备");
            return;
        }

        Set<String> started = new HashSet<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            for (DeviceConfig device : devices) {
                if (started.contains(device.deviceId())) {
                    continue;
                }
                try {
                    manager.start(device);
                    started.add(device.deviceId());
                    log.info("设备 {} 开始采集", device.deviceId());
                } catch (Exception e) {
                    log.warn("设备 {} 启动失败，5 秒后重试: {}", device.deviceId(), e.toString());
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
