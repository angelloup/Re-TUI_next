package ohi.andre.consolelauncher.managers.status;

import ohi.andre.consolelauncher.UIManager;

public interface StatusUpdateListener {
    void onUpdate(UIManager.Label label, CharSequence text);
}
