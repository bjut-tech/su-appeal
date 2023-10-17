package tech.bjut.su.appeal.dto;

import lombok.Data;

@Data
public class ServerStatusDto {

    private double cpuUsage;

    private double memoryUsed;

    private double memoryMax;

    private double diskFree;

    private double diskTotal;

    public double getMemoryUsage() {
        return Math.min(memoryUsed / memoryMax, 1.0);
    }

    public double getDiskUsage() {
        return 1 - diskFree / diskTotal;
    }
}
