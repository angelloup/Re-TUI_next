package ohi.andre.consolelauncher.tuils;

import android.app.ActivityManager;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SystemUtils {

    public static final int TERA = 0;
    public static final int GIGA = 1;
    public static final int MEGA = 2;
    public static final int KILO = 3;
    public static final int BYTE = 4;

    private static long totalRam = -1;

    public static double freeRam(ActivityManager activityManager, ActivityManager.MemoryInfo memory) {
        activityManager.getMemoryInfo(memory);
        return memory.availMem;
    }

    public static long totalRam() {
        if (totalRam != -1) return totalRam;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("MemTotal")) {
                    String[] split = line.split("\\s+");
                    totalRam = Long.parseLong(split[1]);
                    return totalRam;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static double getAvailableInternalMemorySize(int unit) {
        File path = Environment.getDataDirectory();
        return getAvailableSpace(path, unit);
    }

    public static double getTotalInternalMemorySize(int unit) {
        File path = Environment.getDataDirectory();
        return getTotalSpace(path, unit);
    }

    public static double getAvailableExternalMemorySize(int unit) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            return getAvailableSpace(path, unit);
        } else {
            return 0;
        }
    }

    public static double getTotalExternalMemorySize(int unit) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            return getTotalSpace(path, unit);
        } else {
            return 0;
        }
    }

    public static double getAvailableSpace(File path, int unit) {
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return formatSize(availableBlocks * blockSize, unit);
    }

    public static double getTotalSpace(File path, int unit) {
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return formatSize(totalBlocks * blockSize, unit);
    }

    public static double formatSize(long size, int unit) {
        double d = size;
        for (int i = 0; i < unit; i++) {
            d /= 1024.0;
        }
        return round(d, 2);
    }

    public static double percentage(double v, double total) {
        return round(v * 100.0 / total, 2);
    }

    public static double round(double d, int decimalPlace) {
        double power = Math.pow(10, decimalPlace);
        return Math.round(d * power) / power;
    }
}
