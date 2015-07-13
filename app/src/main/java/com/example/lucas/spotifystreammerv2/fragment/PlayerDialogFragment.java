package com.example.lucas.spotifystreammerv2.fragment;

//import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.lucas.spotifystreammerv2.R;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyArtistSimple;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyTrack;
import com.example.lucas.spotifystreammerv2.activity.MainActivity;
import com.squareup.picasso.Picasso;

/**
 * Created by Lucas on 26/06/2015.
 */
public class PlayerDialogFragment extends DialogFragment {
    private static int SPOTIFY_PREVIEW_DURATION = 30;
    private static ParcelableSpotifyTrack mPlayingTrack;

    private ImageButton mButton;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private SeekBar mSeekBar;
    private Handler mHandler;;
    private TextView mArtistName;
    private TextView mAlbumName;
    private ImageView mPicture;
    private TextView mMusicName;

    public static PlayerDialogFragment newInstance (Bundle bundle) {
        PlayerDialogFragment f = new PlayerDialogFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        int tempPosition = 0;
        Bundle b = getArguments();
        if (savedInstanceState != null) {
            mPlayingTrack = savedInstanceState.getParcelable(MainActivity.BUNDLE_TRACK_DATA);
        } else if (b != null) {
            mPlayingTrack = b.getParcelable(MainActivity.BUNDLE_TRACK_DATA);
        }

        mArtistName = (TextView) rootView.findViewById(R.id.player_artistname_textview);

        String artist = "";
        for (ParcelableSpotifyArtistSimple theartist : mPlayingTrack.artists) {
            artist += theartist.name + " ";
        }
        mArtistName.setText(artist);

        mAlbumName = (TextView) rootView.findViewById(R.id.player_albumname_textview);
        mAlbumName.setText(mPlayingTrack.album.name);

        mPicture = (ImageView) rootView.findViewById(R.id.player_albumimage_imageview);
        Picasso.with(this.getActivity()).load(mPlayingTrack.album.images.get(0).url).into(mPicture);

        mMusicName = (TextView) rootView.findViewById(R.id.player_musicname_textview);
        mMusicName.setText(mPlayingTrack.name);

        mNextButton = (ImageButton) rootView.findViewById(R.id.player_next_imagebutton);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Callback) getActivity()).setNextMusic();
            }
        });

        mPreviousButton = (ImageButton) rootView.findViewById(R.id.player_previous_imagebutton);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((Callback) getActivity()).setPreviousMusic();
            }
        });

        mButton = (ImageButton) rootView.findViewById(R.id.player_playpause_imagebutton);
        Boolean isPlaying = ((Callback) getActivity()).isPlaying();
        if (isPlaying == true) {
            mButton.setImageResource(android.R.drawable.ic_media_pause);
        }

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Boolean isPlaying = ((Callback) getActivity()).toggleMediaPlayer();
                if (isPlaying) {
                    mButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seekbar_seekbarview);
        mSeekBar.setMax(SPOTIFY_PREVIEW_DURATION);
        mHandler = new Handler();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    int mCurrentPosition = ((Callback) getActivity()).getCurrentPosition();
                    if (mCurrentPosition != -1) {
                        mSeekBar.setProgress(mCurrentPosition);
                    }
                    mHandler.postDelayed(this, 1000);
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ((Callback) getActivity()).seekTo(progress * 1000);
                }
            }
        });

        return rootView;
    }

    public void updateUI (ParcelableSpotifyTrack playingTrack) {

        if (mPlayingTrack != null && mPlayingTrack != playingTrack)
        {
            String artist = "";
            for (ParcelableSpotifyArtistSimple theartist : playingTrack.artists) {
                artist = artist.concat(theartist.name);
            }
            mArtistName.setText(artist);

            mAlbumName.setText(playingTrack.album.name);

            Picasso.with(this.getActivity()).load(playingTrack.album.images.get(0).url).into(mPicture);

            mMusicName.setText(playingTrack.name);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(MainActivity.BUNDLE_TRACK_DATA, mPlayingTrack);
    }

    public interface Callback {
        int getCurrentPosition();
        void resumeMusicPlayer();
        void pauseMusicPlayer();
        void setNextMusic();
        void setPreviousMusic();
        boolean toggleMediaPlayer();
        boolean isPlaying();
        void seekTo(int progress);
    }
}
