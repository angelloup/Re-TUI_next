package ohi.andre.consolelauncher.tuils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextUtils {

    public static String toPlanString(List<String> strings, String separator) {
        if (strings == null) return "";
        StringBuilder output = new StringBuilder();
        for (int count = 0; count < strings.size(); count++) {
            output.append(strings.get(count));
            if (count < strings.size() - 1) {
                output.append(separator);
            }
        }
        return output.toString();
    }

    public static String toPlanString(Object[] objs, String separator) {
        if (objs == null) return "";
        StringBuilder output = new StringBuilder();
        for (int count = 0; count < objs.length; count++) {
            output.append(objs[count]);
            if (count < objs.length - 1) {
                output.append(separator);
            }
        }
        return output.toString();
    }

    public static String removeUnncesarySpaces(String string) {
        if (string == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        boolean escaped = false;
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                sb.append(c);
                continue;
            }
            if (c == '\"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                sb.append(c);
                continue;
            }
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                sb.append(c);
                continue;
            }

            if (Character.isWhitespace(c) && !inDoubleQuote && !inSingleQuote) {
                if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                    sb.append(" ");
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    public static List<String> splitArgs(String input) {
        List<String> args = new ArrayList<>();
        if (input == null) return args;

        StringBuilder currentArg = new StringBuilder();
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaped) {
                currentArg.append(c);
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '\"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (Character.isWhitespace(c) && !inDoubleQuote && !inSingleQuote) {
                if (currentArg.length() > 0) {
                    args.add(currentArg.toString());
                    currentArg.setLength(0);
                }
            } else {
                currentArg.append(c);
            }
        }
        if (currentArg.length() > 0) {
            args.add(currentArg.toString());
        }
        return args;
    }
}
