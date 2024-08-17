package tech.bjut.su.appeal.util;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;

@Component
@Lazy
public class SystemMemory {

    private OperatingSystemMXBean mxBean = null;

    public long getUsed() {
        try {
            return readMemoryInfo("MemTotal") - readMemoryInfo("MemAvailable");
        } catch (IOException e) {
            initializeBeans();
            return mxBean.getTotalMemorySize() - mxBean.getFreeMemorySize();
        }
    }

    public long getTotal() {
        try {
            return readMemoryInfo("MemTotal");
        } catch (IOException e) {
            initializeBeans();
            return mxBean.getTotalMemorySize();
        }
    }

    private long readMemoryInfo(String key) throws IOException {
        long memory = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(key)) {
                    String[] parts = line.split("\\s+");
                    memory = Long.parseLong(parts[1]) * 1024;
                }
            }
        }

        return memory;
    }

    private synchronized void initializeBeans() {
        if (mxBean == null) {
            mxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        }
    }
}
