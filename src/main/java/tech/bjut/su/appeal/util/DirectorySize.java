package tech.bjut.su.appeal.util;

import java.io.File;

public class DirectorySize {

    public static long getUsed(File dir) {
        if (!dir.isDirectory()) {
            return dir.length();
        }

        long size = 0;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += getUsed(file);
            }
        }

        return size;
    }

    public static long getFree(File dir) {
        return dir.getUsableSpace();
    }

    public static long getTotal(File dir) {
        return getUsed(dir) + getFree(dir);
    }
}
