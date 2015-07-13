package com.example.lucas.spotifystreammerv2.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lucas.spotifystreammerv2.R;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyTrack;
import com.example.lucas.spotifystreammerv2.Utility.Utility;
import com.example.lucas.spotifystreammerv2.activity.MainActivity;
import com.example.lucas.spotifystreammerv2.adapter.TrackAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by Lucas on 24/06/2015.
 */
public class TopTracksFragment extends Fragment {
    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    public static final String ARTIST_ID = "artist_id";
    public static final String BUNDLE_TRACKLIST = "TOPTRACKSFRAGMENT_TRACKLIST";
    public static final String BUNDLE_SELECTED_TRACKDATA = "TOPTRACKSFRAGMENT_TRACKSELECTED";

    public static String artistId; // The artist ID to get the tracks from, provided by Intent.EXTRA_TEXT
    public static TrackAdapter mTrackAdapter;

    private ListView mListView;

    public interface Callback {
        public void onTrackSelected(Bundle b);
    }

    public TopTracksFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        // Save the track list
        ArrayList<ParcelableSpotifyTrack> trackList = new ArrayList<>();

        for (int i = 0; i < mTrackAdapter.getCount(); i++) {
            ParcelableSpotifyTrack tempTrack = mTrackAdapter.getItem(i);
            trackList.add(tempTrack);
        }

        saveInstanceState.putParcelableArrayList(MainActivity.BUNDLE_TRACK_LIST, trackList);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        mTrackAdapter = new TrackAdapter(getActivity(), new ArrayList<ParcelableSpotifyTrack>());
        mListView = (ListView) rootView.findViewById(R.id.artist_detail_listview);
        mListView.setAdapter(mTrackAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Bundle args = new Bundle();
                ArrayList<ParcelableSpotifyTrack> tracksList = new ArrayList<ParcelableSpotifyTrack>();
                for (int i = 0; i < mTrackAdapter.getCount(); i++) {
                    ParcelableSpotifyTrack tempTrack = mTrackAdapter.getItem(i);
                    int tempPosition = mTrackAdapter.getPosition(tempTrack);
                    tracksList.add(tempPosition, tempTrack);
                }
                args.putParcelableArrayList(MainActivity.BUNDLE_TRACK_LIST, tracksList);
                args.putParcelable(MainActivity.BUNDLE_TRACK_DATA, mTrackAdapter.getItem(position));

//                int tempPosition = position;
//                args.putInt(BUNDLE_SELECTED_TRACKPOS, tempPosition);

                ((Callback) getActivity())
                        .onTrackSelected(args);

            }
        });

        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();

        // If there is a savedInstance, use the data inside it. If there isn't, download the list of track
        // using the argument (artist id) provided
        if (savedInstanceState != null && savedInstanceState.containsKey(MainActivity.BUNDLE_TRACK_LIST)) {
            ArrayList<ParcelableSpotifyTrack> trackList = savedInstanceState.getParcelableArrayList(MainActivity.BUNDLE_TRACK_LIST);;
            mTrackAdapter.addAll(trackList);
            Log.d(LOG_TAG, "Data restored from savedInstance");
        } else if (arguments != null) {
            artistId = arguments.getString(ARTIST_ID);
            FetchTracksSearchTask fetchTracksSearchTask = new FetchTracksSearchTask();
            fetchTracksSearchTask.execute(artistId);
            Log.d(LOG_TAG, "There is no data to restore, download it");
        }

        return rootView;
    }

    // Displays a toast if there is no track (or if all tracks were filtered for being 'explicit')
    // The ideal would be to check for it before displaying the fragment :(
    private void postSearch (String status) {
        if (status.equals("NO_RESULT_FOUND")) {
            Context context = getActivity();
            CharSequence text = getString(R.string.search_artist_field_no_result_found);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.TOP, (int) getActivity().findViewById(R.id.artist_detail_listview).getX(),
                    (int)getActivity().findViewById(R.id.artist_detail_listview).getY());
            toast.show();
        }
    }

    // AsyncTask to fetch the top tracks
    public class FetchTracksSearchTask extends AsyncTask<String, String, ParcelableSpotifyTrack[]> {
        private final String LOG_TAG = FetchTracksSearchTask.class.getSimpleName();

        @Override
        protected ParcelableSpotifyTrack[] doInBackground(String... params) {
            if (params[0] == null) {
                return null;
            }
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Map<String, Object> map = new HashMap<>();
                // Get the country parameter from the SharedPreferences
                map.put("country", Utility.getLocationPreference(getActivity()));

                Tracks results = spotify.getArtistTopTrack(params[0], map);
                ParcelableSpotifyTrack[] trackArr = new ParcelableSpotifyTrack[results.tracks.size()];

                // Get the preference regarding songs marked as 'explicit'
                Boolean showExplicit = Utility.getExplicitSongsPreference(getActivity());

                int counter = 0;
                for (int i = 0; i < results.tracks.size(); i++) {
                    Log.d(LOG_TAG, "Download completed " + results.tracks.get(i).name);
                    // Add the track to the array if it's not explicit or if explicit preference is enabled
                    if (!results.tracks.get(i).explicit || showExplicit) {
                        trackArr[counter] = new ParcelableSpotifyTrack(results.tracks.get(i));
                        counter++;
                    }
                    // If it is the last iteration and some tracks were skipped, rebuild the array
                    if (i == results.tracks.size() - 1 && i != counter) {
                        List<ParcelableSpotifyTrack> tempTrackList = new ArrayList<>();
                        for (ParcelableSpotifyTrack track : trackArr) {
                            if (track != null) {
                                tempTrackList.add(track);
                            }
                        }
                        // Rebuild the array so there is no null values inside it
                        trackArr = tempTrackList.toArray(new ParcelableSpotifyTrack[counter]);
                    }
                }
                // If there is no result in the final array, publish the progress to display a toast warning the user
                if ( trackArr.length < 1) {
                    publishProgress("NO_RESULT_FOUND");
                }

                return trackArr;
            } catch (Exception e) {e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(ParcelableSpotifyTrack[] result) {
            // Update the adapter
            if (result != null) {
                mTrackAdapter.clear();
                mTrackAdapter.addAll(result);
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            postSearch(progress[0].toString());
        }
    }
}

