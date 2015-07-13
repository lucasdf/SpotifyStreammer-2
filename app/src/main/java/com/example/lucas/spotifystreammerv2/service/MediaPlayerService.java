package com.example.lucas.spotifystreammerv2.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.lucas.spotifystreammerv2.R;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyArtistSimple;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyTrack;
import com.example.lucas.spotifystreammerv2.Utility.Utility;
import com.example.lucas.spotifystreammerv2.activity.MainActivity;

import java.util.ArrayList;

/**
 * Created by Lucas on 07/07/2015.
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    // Binder given to client
    private final IBinder mBinder = new LocalBinder();

    // Media Player
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private ArrayList<ParcelableSpotifyTrack> mTrackList;
    private int mTrackPos;
    private ResultReceiver mResultReceiver; // send messages to the activity

    // Intent-filter keys (used by the notification actions)
    private static String KEY_NEXT = "PLAY_NEXT";
    private static String KEY_LAST = "PLAY_LAST";
    private static String KEY_PAUSE = "PLAY_PAUSE";
    private static String KEY_PLAY = "PLAY_PLAY";

    @Override
    public void onCreate() {
        super.onCreate();
        mTrackPos = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
        // Register the actions used by the notification intents
        IntentFilter filter = new IntentFilter();
        filter.addAction(KEY_NEXT);
        filter.addAction(KEY_LAST);
        filter.addAction(KEY_PAUSE);
        filter.addAction(KEY_PLAY);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mBroadcastReceiver,filter);
    }
    // The BroadcastReceiver to allow the notifications to communicate with the service
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(KEY_NEXT)) {
                playNextSong();
            } else if (action.equals(KEY_LAST)) {
                playPreviousSong();
            } else if (action.equals(KEY_PAUSE)) {
                pause();
            } else if (action.equals(KEY_PLAY)) {
                resume();
            }
        }
    };

    // This is the old onStart method that will be called on the pre-2.0
    // platform.
    @Override
    public void onStart(Intent intent, int startId) {
        mResultReceiver = intent.getParcelableExtra("receiverTag");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mResultReceiver = intent.getParcelableExtra("receiverTag");
        // Make the service to continue run until it is explicitly stopped
        return START_STICKY;
    }

    public void initMusicPlayer(){
        // Init the MusicPlayer options and prepare it.
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    // Set the track list
    public void setList(ArrayList<ParcelableSpotifyTrack> trackList){
        mTrackList = trackList;
    }
    // Set the position of the track currently playing
    public void setTrackPosition(int trackPos){
        mTrackPos = trackPos;
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    // Reset the player and prepare the song set on mTrackPos
    public void playSong() {
        mMediaPlayer.reset();
        ParcelableSpotifyTrack playSong = mTrackList.get(mTrackPos);
        try{
            mMediaPlayer.setDataSource(playSong.preview_url);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mMediaPlayer.prepareAsync();
    }
    // When MP is prepared, start the song and send a message to the activity (so it can update the DialogFragment and place the "Now Playing" button)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        resume();
        Bundle b=new Bundle();
        b.putString("message", "New music starting!");
        mResultReceiver.send(0, b);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    // When the music finishes, play the next song
    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextSong();
    }
    // Set the mTrackPos to the next song and call playSong() to prepare the player
    public void playNextSong() {
        int index = mTrackPos + 1;
        if (index < mTrackList.size() - 1) {
            mTrackPos = index;
            playSong();
        } else {
            mTrackPos = 0;
            // Send a notification
            playSong();
        }
    }
    // Set the mTrackPos to the previous song and call playSong() to prepare the player
    public void playPreviousSong() {
        int index = mTrackPos - 1;
        if (index > 0) {
            mTrackPos = index;
            playSong();
        } else {
            mTrackPos = 0;
            playSong();
        }
    }
    // Return the Track currently being played
    public ParcelableSpotifyTrack getPlayingTrack()
    {
        if (mMediaPlayer != null && mTrackList.get(mTrackPos) != null) {
            return mTrackList.get(mTrackPos);
        } else return null;
    }
    // Return the position of the song (seconds played)
    public int getCurrentPosition(){
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition() / 1000;
        } else return -1;
    }
    // Return a boolean indicating if the MP is playing
    public boolean isPlaying(){
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        } else return false;
    }
    // Pause the MP
    public void pause(){
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            // Since the the song is now playing, update the notification to show a "play" button
            notifySong();
        }
    }
    // Resume the MP
    public void resume(){
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            // Since the the song is now playing, update the notification to show a "pause" button
            notifySong();
        }
    }
    // Toggle the MP
    public boolean toggleMediaPlayer(){
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                pause();
                return false;
            }
            resume();
            return true;
        } else return false;
    }
    // seekTo a new position inside the song
    public void seekTo(int progress) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(progress);
        }
    }

    // Create and display the notifications
    private void notifySong() {
        Context context = this;
        Boolean isNotificationEnabled = Utility.getNotificationPreference(this);

        // If notifications are disabled, get out of here
        if (!isNotificationEnabled) {
            Log.d("MediaPlayerService", "Notification not enabled");
            return;
        }

        // Get the notification data (artist name, track name, etc)
        ParcelableSpotifyTrack playingTrack = mTrackList.get(mTrackPos);
        String artistName = "";
        for (ParcelableSpotifyArtistSimple theArtist : playingTrack.artists) {
            artistName += theArtist.name + " ";
        }

        int iconId = R.drawable.ic_media_play;
        String title = playingTrack.name;
        String contentText = artistName;

        // Setup the PendingIntents used on the action buttons
        Intent nextIntent = new Intent(KEY_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);

        Intent lastIntent = new Intent(KEY_LAST);
        PendingIntent lastPendingIntent = PendingIntent.getBroadcast(this, 0, lastIntent, 0);

        Intent pauseIntent = new Intent(KEY_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);

        Intent playIntent = new Intent(KEY_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);

          Intent resultIntent = new Intent(context, MainActivity.class);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_play_dark);

        // Setup the notification layout. If the music is playing, show a "Pause" button, otherwise show a "Play" button.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(iconId)
                        //.setLargeIcon(icon)
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .addAction(android.R.drawable.ic_media_previous, "Last", lastPendingIntent);
        if (isPlaying()) {
            mBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent);
        } else {
            mBuilder.addAction(android.R.drawable.ic_media_play, "Play", playPendingIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent);
        }

        // Build the notification and display it
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());




//        RemoteViews remoteViews = new RemoteViews(getPackageName(),
//                R.id.notification_music);
//        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(
//                this).setContent(
//                remoteViews);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//
//        NotificationManager mNotificationManager =
//                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());


    }
}
