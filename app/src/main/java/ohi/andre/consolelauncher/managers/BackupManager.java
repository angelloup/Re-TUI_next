package ohi.andre.consolelauncher.managers;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ohi.andre.consolelauncher.BuildConfig;
import ohi.andre.consolelauncher.tuils.Tuils;

public final class BackupManager {

    private static final String BACKUP_SUFFIX = ".retui-backup";
    private static final String MANIFEST_FILE = "manifest.txt";
    private static final long MAX_BACKUP_BYTES = 32L * 1024L * 1024L;

    private BackupManager() {}

    public static String defaultBackupName() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        return "retui-backup-" + stamp + BACKUP_SUFFIX;
    }

    public static void exportBackup(Context context, Uri uri) throws Exception {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("Backup destination is required");
        }

        OutputStream out = new BufferedOutputStream(context.getContentResolver().openOutputStream(uri, "w"));
        if (out == null) {
            throw new IllegalArgumentException("Unable to open backup destination");
        }

        ZipOutputStream zip = new ZipOutputStream(out);
        try {
            addTextEntry(zip, MANIFEST_FILE,
                    "type=retui-backup\n"
                            + "schema=1\n"
                            + "appVersion=" + BuildConfig.VERSION_NAME + "\n");
            File root = Tuils.getFolder();
            addDirectory(zip, root, root);
        } finally {
            zip.close();
        }
    }

    public static void importBackup(Context context, Uri uri) throws Exception {
        if (context == null || uri == null) {
            throw new IllegalArgumentException("Backup package is required");
        }

        File tempDir = new File(Tuils.getFolder(), ".restore-importing");
        if (tempDir.exists()) {
            Tuils.delete(tempDir);
        }
        if (!tempDir.mkdirs()) {
            throw new IllegalStateException("Unable to create restore folder");
        }

        boolean hasManifest = false;
        long totalBytes = 0;
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(context.getContentResolver().openInputStream(uri)));
        byte[] buffer = new byte[8192];
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory()) continue;
                if (!isSafeEntry(name)) {
                    throw new IllegalArgumentException("Unsafe backup package");
                }

                if (MANIFEST_FILE.equals(name)) {
                    hasManifest = true;
                }

                File out = new File(tempDir, name);
                File parent = out.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Unable to restore backup folder");
                }

                FileOutputStream stream = new FileOutputStream(out, false);
                try {
                    int read;
                    while ((read = zip.read(buffer)) != -1) {
                        totalBytes += read;
                        if (totalBytes > MAX_BACKUP_BYTES) {
                            throw new IllegalArgumentException("Backup package is too large");
                        }
                        stream.write(buffer, 0, read);
                    }
                } finally {
                    stream.close();
                }
            }
        } finally {
            zip.close();
        }

        if (!hasManifest) {
            throw new IllegalArgumentException("Backup package is incomplete");
        }

        restoreDirectory(tempDir, Tuils.getFolder());
        Tuils.delete(tempDir);
    }

    private static void addDirectory(ZipOutputStream zip, File root, File dir) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = relativeName(root, file);
            if (!isBackupCandidate(file, name)) continue;

            if (file.isDirectory()) {
                addDirectory(zip, root, file);
            } else if (file.isFile()) {
                addFileEntry(zip, name, file);
            }
        }
    }

    private static boolean isBackupCandidate(File file, String name) {
        if (name == null || name.length() == 0) return false;
        if (name.startsWith(".restore-importing") || name.endsWith(BACKUP_SUFFIX)) return false;
        if (name.startsWith("crash.txt")) return false;
        return file.isDirectory() || file.isFile();
    }

    private static void addTextEntry(ZipOutputStream zip, String name, String text) throws Exception {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(text.getBytes("UTF-8"));
        zip.closeEntry();
    }

    private static void addFileEntry(ZipOutputStream zip, String name, File file) throws Exception {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                zip.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
        zip.closeEntry();
    }

    private static void restoreDirectory(File source, File target) throws Exception {
        File[] files = source.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (MANIFEST_FILE.equals(file.getName())) continue;
            File dest = new File(target, relativeName(source, file));
            if (file.isDirectory()) {
                if (!dest.exists() && !dest.mkdirs()) {
                    throw new IllegalStateException("Unable to restore folder: " + dest.getName());
                }
                restoreDirectory(file, dest);
            } else if (file.isFile()) {
                File parent = dest.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Unable to restore folder: " + parent.getName());
                }
                if (dest.exists()) {
                    Tuils.insertOld(dest);
                }
                copyFile(file, dest);
            }
        }
    }

    private static void copyFile(File source, File dest) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(source));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dest, false));
        byte[] buffer = new byte[8192];
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    private static String relativeName(File root, File file) {
        String rootPath = root.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (!filePath.startsWith(rootPath)) return file.getName();
        String name = filePath.substring(rootPath.length());
        if (name.startsWith(File.separator)) name = name.substring(1);
        return name.replace(File.separatorChar, '/');
    }

    private static boolean isSafeEntry(String name) {
        return name != null
                && name.length() > 0
                && !name.startsWith("/")
                && !name.contains("\\")
                && !name.contains("..");
    }
}
