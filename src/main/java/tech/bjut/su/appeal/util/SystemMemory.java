package tech.bjut.su.appeal.util;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class SystemMemory {

    private static OperatingSystemMXBean mxBean = null;

    public static long getUsed() {
        try {
            return readMemoryInfo("MemTotal") - readMemoryInfo("MemAvailable");
        } catch (IOException e) {
            initializeBeans();
            return mxBean.getTotalMemorySize() - mxBean.getFreeMemorySize();
        }
    }

    public static long getTotal() {
        try {
            return readMemoryInfo("MemTotal");
        } catch (IOException e) {
            initializeBeans();
            return mxBean.getTotalMemorySize();
        }
    }

    private static long readMemoryInfo(String key) throws IOException {
        long memory = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(key)) {
                    memory = extractMemoryValue(line);
                    break;
                }
            }
        }

        return memory;
    }

    private static long extractMemoryValue(String line) {
        String[] parts = line.split("\\s+");
        return Long.parseLong(parts[1]) * 1024;
    }

    private static void initializeBeans() {
        if (mxBean == null) {
            mxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        }
    }
}
