package ohi.andre.consolelauncher.managers.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.tuils.PrivateIOReceiver;
import ohi.andre.consolelauncher.tuils.PublicIOReceiver;
import ohi.andre.consolelauncher.tuils.Tuils;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static final String ACTION_MUSIC_CHANGED = "ohi.andre.consolelauncher.music_changed";
    public static final String ACTION_MUSIC_CONTROL = "ohi.andre.consolelauncher.music_control";
    
    public static final String SONG_TITLE = "song_title";
    public static final String SONG_SINGER = "song_singer";
    public static final String SONG_DURATION = "song_duration";
    public static final String SONG_POSITION = "song_position";
    public static final String MUSIC_PLAYING = "music_playing";
    public static final String MUSIC_SOURCE = "music_source";
    public static final String SOURCE_INTERNAL = "internal";
    public static final String SOURCE_EXTERNAL = "external";
    public static final String MUSIC_CONTROL = "music_control";
    public static final String CONTROL_PREVIOUS = "previous";
    public static final String CONTROL_PLAY_PAUSE = "play_pause";
    public static final String CONTROL_NEXT = "next";

    public static final String EXTRA_CONTROL_CMD = "control_cmd";
    public static final int CONTROL_NEXT_INT = 1;
    public static final int CONTROL_PREV_INT = 2;
    public static final int CONTROL_PLAY_PAUSE_INT = 3;

    public static final int NOTIFY_ID=100001;

    private MediaPlayer player;
    private List<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = Tuils.EMPTYSTRING;
    private boolean shuffle=false;

    private long lastNotificationChange;

//    do not touch the song playback from here

    public void onCreate(){
        super.onCreate();
        songPosn=0;
        player = new MediaPlayer();
        initMusicPlayer();

        lastNotificationChange = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_CONTROL_CMD)) {
            int cmd = intent.getIntExtra(EXTRA_CONTROL_CMD, -1);
            switch (cmd) {
                case CONTROL_NEXT_INT:
                    playNext();
                    break;
                case CONTROL_PREV_INT:
                    playPrev();
                    break;
                case CONTROL_PLAY_PAUSE_INT:
                    if (isPlaying()) pausePlayer();
                    else playPlayer();
                    break;
            }
        }

        updateForegroundStatus();
        return START_NOT_STICKY;
    }

    private void broadcastMusicState() {
        Intent intent = new Intent(ACTION_MUSIC_CHANGED);
        intent.putExtra(SONG_TITLE, songTitle);
        intent.putExtra(MUSIC_PLAYING, isPlaying());
        intent.putExtra(SONG_POSITION, getPosn());
        intent.putExtra(SONG_DURATION, getDur());
        intent.putExtra(MUSIC_SOURCE, SOURCE_INTERNAL);
        
        if (songs != null && songPosn >= 0 && songPosn < songs.size()) {
            Song s = songs.get(songPosn);
            intent.putExtra(SONG_SINGER, s.getSinger());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        updateForegroundStatus();
    }

    private void updateForegroundStatus() {
        if (songTitle == null || songTitle.isEmpty() || !isPlaying()) {
            stopForeground(false);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastNotificationChange < 1000) return;
        lastNotificationChange = now;

        Notification notification = buildNotification(this, songTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFY_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFY_ID, notification);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(songTitle == null || songTitle.length() == 0) return;
        mp.start();
        broadcastMusicState();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(List<Song> theSongs) {
        songs = theSongs;
        if(shuffle) Collections.shuffle(songs);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return super.onUnbind(intent);
    }

    public String playSong(){
        try {
            player.reset();
        } catch (Exception e) {
//            no need to log this error, as this will occur everytime
            Tuils.log(e);
        }

        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();

        long id = playSong.getID();
        if(id == -1) {
            String path = playSong.getPath();
            try {
                player.setDataSource(path);
            } catch (IOException e) {
                Tuils.log(e);
                Tuils.toFile(e);
                return null;
            }
        } else {
            long currSong = playSong.getID();
            Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
            try {
                player.setDataSource(getApplicationContext(), trackUri);
            }
            catch(Exception e) {
                Tuils.log(e);
                Tuils.toFile(e);
                return null;
            }
        }
        player.prepareAsync();

        return playSong.getTitle();
    }

    public void setSong(int songIndex){
        songPosn = songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    private static Notification buildNotification(Context context, String songTitle) {
        String channelId = "music_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Music Playback", NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null, null);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent notIntent = new Intent(context, LauncherActivity.class);
        PendingIntent pendInt = PendingIntent.getActivity(context, 0, notIntent, Tuils.pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT));

        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String label = "cmd";
            RemoteInput remoteInput = new RemoteInput.Builder(PrivateIOReceiver.TEXT)
                    .setLabel(label)
                    .build();

            Intent i = new Intent(context, PublicIOReceiver.class);
            i.setAction(PublicIOReceiver.ACTION_CMD);
            i.putExtra(MainManager.MUSIC_SERVICE, true);

            int flags = Tuils.pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = (flags & ~PendingIntent.FLAG_IMMUTABLE) | PendingIntent.FLAG_MUTABLE;
            }

            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.mipmap.ic_launcher, label,
                    PendingIntent.getBroadcast(context.getApplicationContext(), 10, i, flags))
                    .addRemoteInput(remoteInput)
                    .build();

            builder.addAction(action);
        }

        notification = builder.build();
        return notification;
    }

    public int getPosn(){
        try {
            return player != null ? player.getCurrentPosition() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getDur(){
        try {
            return player != null ? player.getDuration() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isPlaying(){
        try {
            return player != null && player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public void pausePlayer(){
        if (isPlaying()) {
            player.pause();
        }
        broadcastMusicState();
    }

    public void stop() {
        try {
            player.stop();
        } catch (Exception e) {}

        try {
            player.release();
        } catch (Exception e) {}

        songTitle = null;
        broadcastMusicState();
        setSong(0);
    }

    public void playPlayer() {
        if (player != null) {
            player.start();
        }
        broadcastMusicState();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public String playPrev(){
        if(songs.size() == 0) return getString(R.string.no_songs);
        songPosn = previous();
        return playSong();
    }

    public String playNext() {
        if(songs.size() == 0) return getString(R.string.no_songs);
        songPosn = next();
        return playSong();
    }

    private int next() {
        int pos = songPosn + 1;
        if(pos == songs.size()) pos = 0;
        return pos;
    }

    private int previous() {
        int pos = songPosn - 1;
        if(pos < 0) pos = songs.size() - 1;
        return pos;
    }

    public int getSongIndex() {
        return songPosn;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        player.release();
        songs.clear();

        stopForeground(true);
    }

    public void setShuffle(boolean shuffle){
        this.shuffle = shuffle;
    }

}
