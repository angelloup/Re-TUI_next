package ohi.andre.consolelauncher.managers;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class WebhookManager {

    private static final String FILE_NAME = "webhooks.xml";
    private static final String ROOT_NAME = "webhooks";
    private static final String WEBHOOK_TAG = "webhook";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String URL_ATTRIBUTE = "url";
    private static final String BODY_ATTRIBUTE = "body";

    private List<Webhook> webhooks;
    private Context context;
    private File file;

    public WebhookManager(Context context) {
        this.context = context;
        this.file = new File(Tuils.getFolder(), FILE_NAME);
        reload();
    }

    public void reload() {
        webhooks = new ArrayList<>();
        try {
            Object[] o = XMLPrefsManager.buildDocument(file, ROOT_NAME);
            if (o == null) return;

            Document d = (Document) o[0];
            Element root = (Element) o[1];
            NodeList nodes = root.getElementsByTagName(WEBHOOK_TAG);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Element) {
                    Element e = (Element) node;
                    String name = e.getAttribute(NAME_ATTRIBUTE);
                    String url = e.getAttribute(URL_ATTRIBUTE);
                    String body = e.getAttribute(BODY_ATTRIBUTE);
                    if (name != null && !name.isEmpty()) {
                        webhooks.add(new Webhook(name, url, body));
                    }
                }
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    public Webhook getWebhook(String name) {
        for (Webhook w : webhooks) {
            if (w.name.equalsIgnoreCase(name)) return w;
        }
        return null;
    }

    public List<Webhook> getWebhooks() {
        return webhooks;
    }

    public boolean add(String name, String url, String body) {
        if (name == null || name.trim().length() == 0 || url == null || url.trim().length() == 0) {
            return false;
        }

        try {
            Object[] o = XMLPrefsManager.buildDocument(file, ROOT_NAME);
            if (o == null) {
                return false;
            }

            Document d = (Document) o[0];
            Element root = (Element) o[1];

            removeMatchingNodes(root, name);

            Element element = d.createElement(WEBHOOK_TAG);
            element.setAttribute(NAME_ATTRIBUTE, name.trim());
            element.setAttribute(URL_ATTRIBUTE, url.trim());
            element.setAttribute(BODY_ATTRIBUTE, body == null ? Tuils.EMPTYSTRING : body);
            root.appendChild(element);

            XMLPrefsManager.writeTo(d, file);
            reload();
            return true;
        } catch (Exception e) {
            Tuils.log(e);
            return false;
        }
    }

    public boolean remove(String name) {
        if (name == null || name.trim().length() == 0) {
            return false;
        }

        try {
            Object[] o = XMLPrefsManager.buildDocument(file, ROOT_NAME);
            if (o == null) {
                return false;
            }

            Document d = (Document) o[0];
            Element root = (Element) o[1];
            boolean removed = removeMatchingNodes(root, name);

            if (removed) {
                XMLPrefsManager.writeTo(d, file);
                reload();
            }

            return removed;
        } catch (Exception e) {
            Tuils.log(e);
            return false;
        }
    }

    private boolean removeMatchingNodes(Element root, String name) {
        NodeList nodes = root.getElementsByTagName(WEBHOOK_TAG);
        List<Node> toRemove = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }

            Element element = (Element) node;
            String existingName = element.getAttribute(NAME_ATTRIBUTE);
            if (existingName != null && existingName.equalsIgnoreCase(name.trim())) {
                toRemove.add(node);
            }
        }

        for (Node node : toRemove) {
            root.removeChild(node);
        }

        return !toRemove.isEmpty();
    }

    public static class Webhook {
        public String name;
        public String url;
        public String bodyTemplate;

        public Webhook(String name, String url, String bodyTemplate) {
            this.name = name;
            this.url = url;
            this.bodyTemplate = bodyTemplate;
        }

        public String substitute(String[] args) {
            return substitutePlain(bodyTemplate, args);
        }

        public String render(String[] args, boolean jsonBody) throws JSONException {
            if (!jsonBody) {
                return substitute(args);
            }

            String template = bodyTemplate == null ? Tuils.EMPTYSTRING : bodyTemplate.trim();
            if (template.startsWith("{")) {
                JSONObject object = new JSONObject(template);
                return replaceJsonObject(object, args).toString();
            } else if (template.startsWith("[")) {
                JSONArray array = new JSONArray(template);
                return replaceJsonArray(array, args).toString();
            }

            return substitute(args);
        }

        private static String substitutePlain(String template, String[] args) {
            String result = template == null ? Tuils.EMPTYSTRING : template;
            for (int i = 0; i < args.length; i++) {
                result = result.replace("%" + (i + 1), args[i]);
            }
            return result;
        }

        private static JSONObject replaceJsonObject(JSONObject object, String[] args) throws JSONException {
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                object.put(key, replaceJsonValue(object.get(key), args));
            }
            return object;
        }

        private static JSONArray replaceJsonArray(JSONArray array, String[] args) throws JSONException {
            for (int i = 0; i < array.length(); i++) {
                array.put(i, replaceJsonValue(array.get(i), args));
            }
            return array;
        }

        private static Object replaceJsonValue(Object value, String[] args) throws JSONException {
            if (value instanceof JSONObject) {
                return replaceJsonObject((JSONObject) value, args);
            } else if (value instanceof JSONArray) {
                return replaceJsonArray((JSONArray) value, args);
            } else if (value instanceof String) {
                return substitutePlain((String) value, args);
            }

            return value;
        }
    }
}
