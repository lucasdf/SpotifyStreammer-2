package com.example.lucas.spotifystreammerv2.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyTrack;
import com.example.lucas.spotifystreammerv2.R;
import com.example.lucas.spotifystreammerv2.Utility.MyResultReceiver;
import com.example.lucas.spotifystreammerv2.fragment.ArtistSearchFragment;
import com.example.lucas.spotifystreammerv2.fragment.PlayerDialogFragment;
import com.example.lucas.spotifystreammerv2.fragment.TopTracksFragment;
import com.example.lucas.spotifystreammerv2.service.MediaPlayerService;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements ArtistSearchFragment.Callback, TopTracksFragment.Callback, PlayerDialogFragment.Callback, MyResultReceiver.Receiver {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    // Fragments tags
    private static final String TOPTRACKSFRAG_TAG = "TTTAG";
    private static final String ARTISTSEARCH_TAG = "ASTAG";
    private static final String MAINRECOMMENDATION_TAG = "MRTAG";
    private static final String PLAYERDIALOG_TAG = "PDTAG";
    // These static strings are used by this activity and all fragments to store data in bundles
    // Not sure if grouping them in the MainActivity is the best option, but it helped to be more organized
    public static final String BUNDLE_TRACK_DATA = "TRACKDATA";
    public static final String BUNDLE_TRACK_LIST = "TRACKLIST";


    private ShareActionProvider mShareActionProvider;
    private boolean mTwoPane;
    private static ArrayList<ParcelableSpotifyTrack> tracks;
    private MediaPlayerService mMediaPlayerService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;
    public MyResultReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set-up the fragments according to the screen size (phone/tablet)
        if(findViewById(R.id.artist_toptracks_container) != null) {
            mTwoPane = true;

        } else
        {
            mTwoPane = false;
            if (savedInstanceState == null) {
                FragmentManager fragmentTransaction = getSupportFragmentManager();
                fragmentTransaction.beginTransaction()
                        .replace(R.id.container_main, new ArtistSearchFragment(), ARTISTSEARCH_TAG)
                        .commit();
            }
        }

        // Setup the receiver to receive messages from the MediaPlayerService
        mReceiver = new MyResultReceiver(new Handler());
        mReceiver.setReceiver(this);

    }

    // Make a connection with the MediaPlayerService
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "Connected to service");
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder)service;
            mMediaPlayerService = binder.getService();
            mMusicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };

    // Start the MediaPlayerService
    @Override
    protected void onStart() {
        super.onStart();
        if(mPlayIntent ==null){
            Log.d(LOG_TAG, "Activity created!");
            mPlayIntent = new Intent(this, MediaPlayerService.class);
            mPlayIntent.putExtra("receiverTag", mReceiver);
            bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // If there is a music playing, update the menu to show "Playing now" and "Share" options
        if (mMediaPlayerService.isPlaying() == true) {
            MenuItem player = menu.findItem(R.id.action_player);
            player.setVisible(true);

            MenuItem share = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
            ParcelableSpotifyTrack currentTrack = getPLayingTrack();
            if (currentTrack != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
            share.setVisible(true);
            this.invalidateOptionsMenu();
        }
        return true;
    }

    // Share intent
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getPLayingTrack().preview_url);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        // If "Playing now" is selected, show the PlayerDialogFragment
        if (id == R.id.action_player) {
            PlayerDialogFragment playerFragment = (PlayerDialogFragment) getSupportFragmentManager().findFragmentByTag(PLAYERDIALOG_TAG);
            ParcelableSpotifyTrack track = mMediaPlayerService.getPlayingTrack();
            // If the playerFragment is not visible and there is a track data, create the fragment and display it
            if (null == playerFragment && track != null) {
                FragmentManager fm = getSupportFragmentManager();
                Bundle args = new Bundle();
                args.putParcelable(BUNDLE_TRACK_DATA, track);

                if (mTwoPane) {
                    // Create and show the dialog.
                    android.support.v4.app.DialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(args);
                    playerDialogFragment.show(fm, PLAYERDIALOG_TAG);
                } else {
                    android.support.v4.app.DialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(args);
                    fm.beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.container_main, playerDialogFragment, PLAYERDIALOG_TAG)
                            .commit();
                }
            }
            return true;
        }
        // If the Settings was selected, start the SettingsActivity
        else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Callback from ArtistSearchFragment to deal with artist selection
    @Override
    public void onItemSelected(String artistId) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment
            Bundle args = new Bundle();
            args.putString(TopTracksFragment.ARTIST_ID, artistId);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artist_toptracks_container, fragment, TOPTRACKSFRAG_TAG)
                    .commit();
        }
        // Not tablet, display the TopTrackFragment in a new screen
        else {
            Bundle args = new Bundle();
            args.putString(TopTracksFragment.ARTIST_ID, artistId);
            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container_main, fragment, TOPTRACKSFRAG_TAG)
                    .commit();
        }
    }

    // Callback to deal with track selection
    @Override
    public void onTrackSelected(Bundle bundle) {

        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment prev = fm.findFragmentByTag(PLAYERDIALOG_TAG);
        if (prev != null) {
            getSupportFragmentManager().beginTransaction().remove(prev).commit();
        }

        tracks = bundle.getParcelableArrayList(BUNDLE_TRACK_LIST);
        ParcelableSpotifyTrack selectedTrack = bundle.getParcelable(BUNDLE_TRACK_DATA);
        // If there is a music playing, only start a new song if the selected music is different from the one playing
        if (mMediaPlayerService.isPlaying())
        {
            if (mMediaPlayerService.getPlayingTrack() != selectedTrack) {
                mMediaPlayerService.setList(tracks);
                mMediaPlayerService.setTrackPosition(tracks.indexOf(selectedTrack));
                mMediaPlayerService.playSong();
            }
        } else
        {
            mMediaPlayerService.setList(tracks);
            mMediaPlayerService.setTrackPosition(tracks.indexOf(selectedTrack));
            mMediaPlayerService.playSong();
            this.invalidateOptionsMenu();
        }
        // If it's in twoPane mode, display the PlayerDialogFragment, otherwise display it as full-screen
        if (mTwoPane) {
            // Create and show the dialog.
            android.support.v4.app.DialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(bundle);
            playerDialogFragment.show(fm, PLAYERDIALOG_TAG);

        } else {

            android.support.v4.app.DialogFragment playerDialogFragment = PlayerDialogFragment.newInstance(bundle);
            fm.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container_main, playerDialogFragment, PLAYERDIALOG_TAG)
                    .commit();
        }
    }

    // Stop the service
    @Override
    protected void onDestroy() {
        stopService(mPlayIntent);
        mMediaPlayerService = null;
        super.onDestroy();
    }

    // Get results from the service
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        // If the track changes and the PlayerDialogFragment is visible, update the screen information
        if (resultCode == 0) {
            FragmentManager fm = getSupportFragmentManager();
            PlayerDialogFragment prev = (PlayerDialogFragment) fm.findFragmentByTag(PLAYERDIALOG_TAG);
            //android.support.v4.app.Fragment prev = fm.findFragmentById("dialog");
            if (prev != null) {
                prev.updateUI(mMediaPlayerService.getPlayingTrack());
            }
        }
    }

    // All the methods below are Callbacks implementation from the PlayerDialogFragment
    // It allows the Fragment to communicate with the MediaPlayerService
    @Override
    public int getCurrentPosition() {
        if (mMusicBound) {
            return mMediaPlayerService.getCurrentPosition();
        } else return -1;
    }

    @Override
    public void pauseMusicPlayer() {
        if (mMusicBound) {
            mMediaPlayerService.pause();
        }
    }

    @Override
    public void resumeMusicPlayer() {
        if (mMusicBound) {
            mMediaPlayerService.resume();
        }
    }

    @Override
    public void setNextMusic() {
        if (mMusicBound) {
            mMediaPlayerService.playNextSong();
        }
    }

    @Override
    public void setPreviousMusic() {
        if (mMusicBound) {
            mMediaPlayerService.playPreviousSong();
        }
    }

    @Override
    public boolean toggleMediaPlayer() {
        if (mMusicBound) {
            this.invalidateOptionsMenu();
            return mMediaPlayerService.toggleMediaPlayer();
        } else return false;
    }

    @Override
    public boolean isPlaying() {
        if (mMusicBound) {
            return mMediaPlayerService.isPlaying();
        } else return false;
    }

    @Override
    public void seekTo(int progress) {
        if (mMusicBound) {
            mMediaPlayerService.seekTo(progress);
        }
    }

    public ParcelableSpotifyTrack getPLayingTrack() {
        if (mMusicBound) {
            return mMediaPlayerService.getPlayingTrack();
        } else return null;
    }
}