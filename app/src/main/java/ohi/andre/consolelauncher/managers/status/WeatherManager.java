package ohi.andre.consolelauncher.managers.status;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.managers.HTMLExtractManager;
import ohi.andre.consolelauncher.managers.TuiLocationManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.Tuils;

public class WeatherManager extends StatusManager {

    private String key;
    private String url;
    private final StatusUpdateListener listener;
    private final int size;

    private boolean fixedLocation = false;
    private double lastLatitude, lastLongitude;
    private boolean hasLocation = false;

    public static final String ACTION_WEATHER_GOT_LOCATION = "ohi.andre.consolelauncher.WEATHER_GOT_LOCATION";

    public WeatherManager(Context context, long delay, int size, StatusUpdateListener listener) {
        super(context, delay);
        this.size = size;
        this.listener = listener;

        if (XMLPrefsManager.wasChanged(Behavior.weather_key, false)) {
            key = XMLPrefsManager.get(Behavior.weather_key);
        } else {
            key = Behavior.weather_key.defaultValue();
        }

        String where = XMLPrefsManager.get(Behavior.weather_location);
        if (where == null || where.length() == 0 || (!Tuils.isNumber(where) && !where.contains(","))) {
            TuiLocationManager l = TuiLocationManager.instance(context);
            l.add(ACTION_WEATHER_GOT_LOCATION);
        } else {
            fixedLocation = true;
            if (where.contains(",")) {
                String[] split = where.split(",");
                where = "lat=" + split[0] + "&lon=" + split[1];
            } else {
                where = "id=" + where;
            }
            setUrl(where);
        }
    }

    @Override
    protected void update() {
        updateWeather();
    }

    public void updateWeather() {
        if (!fixedLocation && !hasLocation) {
            return;
        }

        if (!fixedLocation) {
            setUrl(lastLatitude, lastLongitude);
        }

        if (url != null) {
            Intent i = new Intent(HTMLExtractManager.ACTION_WEATHER);
            i.putExtra(XMLPrefsManager.VALUE_ATTRIBUTE, url);
            i.putExtra(HTMLExtractManager.BROADCAST_COUNT, HTMLExtractManager.broadcastCount);
            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(i);
        }
    }

    public void setLocation(double lat, double lon) {
        this.lastLatitude = lat;
        this.lastLongitude = lon;
        this.hasLocation = true;
        updateWeather();
    }

    private void setUrl(String where) {
        url = "https://api.openweathermap.org/data/2.5/weather?" + where + "&appid=" + key + "&units=" + XMLPrefsManager.get(Behavior.weather_temperature_measure);
    }

    private void setUrl(double latitude, double longitude) {
        url = "https://api.openweathermap.org/data/2.5/weather?" + "lat=" + latitude + "&lon=" + longitude + "&appid=" + key + "&units=" + XMLPrefsManager.get(Behavior.weather_temperature_measure);
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
