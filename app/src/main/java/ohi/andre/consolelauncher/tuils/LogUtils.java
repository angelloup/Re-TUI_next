package ohi.andre.consolelauncher.tuils;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.io.OutputStream;
import java.util.Arrays;

public class LogUtils {

    private static final String TAG = "TUI";

    public static void log(Object o) {
        Log.e(TAG, String.valueOf(o));
    }

    public static void log(Throwable e) {
        Log.e(TAG, e.getMessage(), e);
    }

    public static void log(Object o, Object o2) {
        Log.e(TAG, String.valueOf(o) + " -- " + String.valueOf(o2));
    }

    public static void log(Object o, Object o2, OutputStream to) {
        try {
            if (o instanceof Object[] && o2 instanceof Object[]) {
                to.write((Arrays.toString((Object[]) o) + " -- " + Arrays.toString((Object[]) o2)).getBytes());
            } else {
                to.write((String.valueOf(o) + " -- " + String.valueOf(o2)).getBytes());
            }
        } catch (Exception e) {
            log(e);
        }
    }

    public static void toFile(String s) {
        if (s == null) return;
        try {
            File f = new File(FileUtils.getInternalFolder(), "crash.txt");
            PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));
            pw.println(new Date().toString());
            pw.println(s);
            pw.println();
            pw.flush();
            pw.close();
        } catch (Exception e1) {}
    }

    public static void toFile(Object o) {
        if (o == null) return;
        try {
            File f = new File(FileUtils.getInternalFolder(), "crash.txt");
            PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));
            pw.println(new Date().toString());
            if (o instanceof Throwable) {
                ((Throwable) o).printStackTrace(pw);
            } else {
                pw.println(o.toString());
            }
            pw.println();
            pw.flush();
            pw.close();
        } catch (Exception e) {}
    }
}
