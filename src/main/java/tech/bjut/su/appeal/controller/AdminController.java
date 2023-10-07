package tech.bjut.su.appeal.controller;

import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.dto.ServerStatusDto;

import java.util.Set;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AppProperties properties;

    private final MetricsEndpoint metrics;

    public AdminController(
        AppProperties properties,
        MetricsEndpoint metrics
    ) {
        this.properties = properties;
        this.metrics = metrics;
    }

    @GetMapping("/admins")
    public Set<String> getAdmins() {
        return properties.getAuth().getAdmin();
    }

    @GetMapping("/server/status")
    public ServerStatusDto getServerStatus() {
        ServerStatusDto response = new ServerStatusDto();

        response.setCpuUsage(metrics.metric("system.cpu.usage", null).getMeasurements().get(0).getValue());
        response.setMemoryUsed(metrics.metric("jvm.memory.used", null).getMeasurements().get(0).getValue());
        response.setMemoryMax(metrics.metric("jvm.memory.max", null).getMeasurements().get(0).getValue());
        response.setDiskFree(metrics.metric("disk.free", null).getMeasurements().get(0).getValue());
        response.setDiskTotal(metrics.metric("disk.total", null).getMeasurements().get(0).getValue());

        return response;
    }
}
