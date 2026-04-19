package ohi.andre.consolelauncher.commands.main.raw;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.WebhookManager;
import ohi.andre.consolelauncher.tuils.SimpleMutableEntry;
import ohi.andre.consolelauncher.tuils.Tuils;

public class webhook extends ParamCommand {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");

    // Using WeakReference to allow suggestions while avoiding memory leaks
    private static WeakReference<MainPack> packRef;

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        add {
            @Override
            public String exec(ExecutePack pack) {
                MainPack info = (MainPack) pack;
                List<String> split = Tuils.splitArgs(info.lastCommand);
                if (split.size() < 5) return "Usage: webhook -add [name] [url] [body_template]";
                
                String name = split.get(2);
                String url = split.get(3);
                
                List<String> bodyParts = split.subList(4, split.size());
                String body = Tuils.toPlanString(bodyParts, Tuils.SPACE);
                
                boolean saved = info.webhookManager.add(name, url, body);
                return saved ? "Webhook " + name + " saved." : "Unable to save webhook " + name + ".";
            }

            @Override
            public int[] args() {
                return new int[]{CommandAbstraction.TEXTLIST};
            }
        },
        rm {
            @Override
            public String exec(ExecutePack pack) {
                ArrayList<String> args = pack.getList();
                if (args == null || args.isEmpty()) return "Usage: webhook -rm [name]";
                String name = args.get(0);
                boolean removed = ((MainPack) pack).webhookManager.remove(name);
                return removed ? "Webhook " + name + " removed." : "Webhook " + name + " not found.";
            }

            @Override
            public int[] args() {
                return new int[]{CommandAbstraction.TEXTLIST};
            }
        },
        ls {
            @Override
            public String exec(ExecutePack pack) {
                List<WebhookManager.Webhook> hooks = ((MainPack) pack).webhookManager.getWebhooks();
                if (hooks == null || hooks.isEmpty()) return "No webhooks configured.";
                StringBuilder sb = new StringBuilder();
                for (WebhookManager.Webhook w : hooks) {
                    sb.append(w.name).append(" -> ").append(w.url).append(Tuils.NEWLINE);
                }
                return sb.toString().trim();
            }

            @Override
            public int[] args() {
                return new int[0];
            }
        };

        static Param get(String p) {
            if (p == null) return null;
            p = p.toLowerCase();
            for (Param p1 : values()) {
                if (p.equals(p1.label()))
                    return p1;
            }
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];
            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }
            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_webhook);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return null;
        }
    }

    private static class WebhookParam implements ohi.andre.consolelauncher.commands.main.Param {
        private final WebhookManager.Webhook w;
        public WebhookParam(WebhookManager.Webhook w) { this.w = w; }
        @Override public int[] args() { return new int[]{CommandAbstraction.TEXTLIST}; }
        @Override public String exec(ExecutePack pack) {
            ArrayList<String> list = pack.getList();
            String[] args = list != null ? list.toArray(new String[0]) : new String[0];
            return triggerWebhook((MainPack) pack, w, args);
        }
        @Override public String label() { return w.name; }
        @Override public String onNotArgEnough(ExecutePack pack, int n) { return null; }
        @Override public String onArgNotFound(ExecutePack pack, int index) { return null; }
    }

    @Override
    public SimpleMutableEntry<Boolean, ohi.andre.consolelauncher.commands.main.Param> getParam(MainPack pack, String param) {
        packRef = new WeakReference<>(pack);
        ohi.andre.consolelauncher.commands.main.Param p = Param.get(param);
        if(p != null) return new SimpleMutableEntry<>(false, p);

        String firstWord = param.split(" ")[0];
        String webhookName = firstWord.startsWith(Tuils.MINUS) ? firstWord.substring(1) : firstWord;
        WebhookManager.Webhook w = pack.webhookManager.getWebhook(webhookName);
        if (w != null) {
            return new SimpleMutableEntry<>(false, new WebhookParam(w));
        }

        return super.getParam(pack, param);
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        packRef = new WeakReference<>(pack);
        return Param.get(param);
    }

    @Override
    public String[] params() {
        String[] labels = Param.labels();
        MainPack pack = packRef != null ? packRef.get() : null;
        if (pack == null || pack.webhookManager == null) return labels;

        List<WebhookManager.Webhook> hooks = pack.webhookManager.getWebhooks();
        if (hooks == null || hooks.isEmpty()) return labels;

        String[] all = new String[labels.length + hooks.size()];
        System.arraycopy(labels, 0, all, 0, labels.length);
        for (int i = 0; i < hooks.size(); i++) {
            all[labels.length + i] = hooks.get(i).name;
        }
        return all;
    }

    @Override
    protected String doThings(ExecutePack pack) {
        final MainPack info = (MainPack) pack;
        packRef = new WeakReference<>(info);
        String input = info.lastCommand;
        if (input == null) return null;

        List<String> split = Tuils.splitArgs(input);
        
        if (split.size() < 2) {
            List<WebhookManager.Webhook> hooks = info.webhookManager.getWebhooks();
            StringBuilder sb = new StringBuilder();
            sb.append(info.context.getString(helpRes())).append(Tuils.NEWLINE);
            if (hooks != null && !hooks.isEmpty()) {
                sb.append(Tuils.NEWLINE).append("Configured Webhooks:").append(Tuils.NEWLINE);
                for (WebhookManager.Webhook w : hooks) sb.append("  • ").append(w.name).append(Tuils.NEWLINE);
            }
            return sb.toString().trim();
        }

        String sub = split.get(1);
        if (sub.startsWith("-")) return null;

        WebhookManager.Webhook w = info.webhookManager.getWebhook(sub);
        String[] webhookArgs = new String[0];
        
        if (w == null && sub.contains(" ")) {
            List<String> subSplit = Tuils.splitArgs(sub);
            if (!subSplit.isEmpty()) {
                w = info.webhookManager.getWebhook(subSplit.get(0));
                if (w != null) {
                    webhookArgs = subSplit.subList(1, subSplit.size()).toArray(new String[0]);
                }
            }
        } else if (w != null) {
            if (split.size() > 2) {
                webhookArgs = split.subList(2, split.size()).toArray(new String[0]);
            }
        }

        if (w != null) {
            return triggerWebhook(info, w, webhookArgs);
        }

        return "Webhook [" + sub + "] not found. Use 'webhook -ls' to see available hooks.";
    }

    private static String triggerWebhook(final MainPack info, final WebhookManager.Webhook w, String[] webhookArgs) {
        final boolean jsonBody = w.bodyTemplate != null
                && (w.bodyTemplate.trim().startsWith("{") || w.bodyTemplate.trim().startsWith("["));

        final String bodyContent;
        try {
            bodyContent = w.render(webhookArgs, jsonBody);
        } catch (JSONException e) {
            return "Webhook [" + w.name + "] template error: " + e.getMessage();
        }

        if (webhookArgs.length > 0) {
            info.historyManager.add(w.name, formatArgsForHistory(webhookArgs));
        }

        MediaType mediaType = jsonBody ? JSON : TEXT;
        RequestBody body = RequestBody.create(bodyContent, mediaType);
        Request request = new Request.Builder().url(w.url).post(body).build();
        final Handler handler = new Handler(Looper.getMainLooper());
        
        info.client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                final String error = e.toString();
                handler.post(() -> Tuils.sendOutput(info.context, "Webhook [" + w.name + "] Error: " + error));
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try (Response r = response) {
                    final String resBody = r.body() != null ? r.body().string() : "Empty Response";
                    final int code = r.code();
                    handler.post(() -> Tuils.sendOutput(info.context, "Webhook [" + w.name + "] Response [" + code + "]: " + resBody));
                }
            }
        });
        return "Triggering webhook: " + w.name;
    }

    private static String formatArgsForHistory(String[] webhookArgs) {
        List<String> formatted = new ArrayList<>(webhookArgs.length);
        for (String arg : webhookArgs) {
            formatted.add(quoteArg(arg));
        }
        return Tuils.toPlanString(formatted, Tuils.SPACE);
    }

    private static String quoteArg(String arg) {
        if (arg == null) {
            return "\"\"";
        }

        boolean needsQuotes = arg.length() == 0;
        for (int i = 0; i < arg.length() && !needsQuotes; i++) {
            char c = arg.charAt(i);
            if (Character.isWhitespace(c) || c == '"' || c == '\\') {
                needsQuotes = true;
            }
        }

        if (!needsQuotes) {
            return arg;
        }

        String escaped = arg.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    @Override public int priority() { return 3; }
    @Override public int helpRes() { return R.string.help_webhook; }
}
