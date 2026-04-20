package ohi.andre.consolelauncher.tuils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

public class IOUtils {

    public static String readerToString(Reader initialReader) throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = initialReader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        initialReader.close();
        return buffer.toString();
    }

    public static String convertStreamToString(InputStream is) {
        if (is == null) return Tuils.EMPTYSTRING;

        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : Tuils.EMPTYSTRING;
    }

    public static void copy(File from, File to) throws Exception {
        download(new FileInputStream(from), to);
    }

    public static long download(InputStream in, File file) throws Exception {
        OutputStream out = new FileOutputStream(file, false);

        byte data[] = new byte[1024];

        long bytes = 0;

        int count;
        while ((count = in.read(data)) != -1) {
            out.write(data, 0, count);
            bytes += count;
        }

        out.flush();
        out.close();
        in.close();

        return bytes;
    }

    public static void write(File file, String separator, String... ss) throws Exception {
        FileOutputStream headerStream = new FileOutputStream(file, false);

        for(int c = 0; c < ss.length - 1; c++) {
            headerStream.write(ss[c].getBytes());
            headerStream.write(separator.getBytes());
        }
        headerStream.write(ss[ss.length - 1].getBytes());

        headerStream.flush();
        headerStream.close();
    }

    public static void deleteContentOnly(File dir) {
        File[] files = dir.listFiles();
        if(files == null) return;

        for(File f : files) {
            if(f.isDirectory()) delete(f);
            f.delete();
        }
    }

    public static void delete(File dir) {
        File[] files = dir.listFiles();
        if(files == null) {
            dir.delete();
            return;
        }

        for(File f : files) {
            if(f.isDirectory()) delete(f);
            f.delete();
        }
        dir.delete();
    }

    public static String inputStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : Tuils.EMPTYSTRING;
    }
}
