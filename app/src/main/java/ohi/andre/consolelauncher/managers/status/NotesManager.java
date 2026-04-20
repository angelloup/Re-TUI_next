package ohi.andre.consolelauncher.managers.status;

import android.content.Context;

import ohi.andre.consolelauncher.UIManager;
import ohi.andre.consolelauncher.tuils.UIUtils;

public class NotesManager extends StatusManager {

    private final ohi.andre.consolelauncher.managers.NotesManager notesManager;
    private final StatusUpdateListener listener;
    private final int size;

    public NotesManager(Context context, long delay, int size, ohi.andre.consolelauncher.managers.NotesManager notesManager, StatusUpdateListener listener) {
        super(context, delay);
        this.size = size;
        this.notesManager = notesManager;
        this.listener = listener;
    }

    @Override
    protected void update() {
        if (notesManager != null && notesManager.hasChanged) {
            if (listener != null) {
                listener.onUpdate(UIManager.Label.notes, UIUtils.span(context, size, notesManager.getNotes()));
            }
        }
    }
}
