package com.example.lucas.spotifystreammerv2.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lucas.spotifystreammerv2.R;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyArtist;
import com.example.lucas.spotifystreammerv2.adapter.ArtistsAdapter;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by Lucas on 24/06/2015.
 */
public class ArtistSearchFragment extends Fragment {
    private final String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    private ArtistsAdapter mArtistAdapter;
    private FetchArtistSearchTask runningTask;
    private ListView mListView;
    private EditText mSearchField;
    private int mPosition = ListView.INVALID_POSITION;

    private String searchText;
    // These bundles keys are used to store information inside the bundles
    private static final String BUNDLE_SELECTED_KEY = "selected_position";
    private static final String BUNDLE_SEARCHTEXT_KEY = "search_text";
    private static final String BUNDLE_ARTISTLIST_KEY = "artist_list";


    public interface Callback {
        public void onItemSelected(String artistId);
    }

    public ArtistSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(BUNDLE_SEARCHTEXT_KEY, searchText);
        ArrayList<ParcelableSpotifyArtist> artistList = new ArrayList<ParcelableSpotifyArtist>();

        if (mArtistAdapter != null) {
            for (int i = 0; i < mArtistAdapter.getCount(); i++) {
                ParcelableSpotifyArtist tempArtist = mArtistAdapter.getItem(i);
                artistList.add(tempArtist);
            }
            savedInstanceState.putParcelableArrayList(BUNDLE_ARTISTLIST_KEY, artistList);
            if (mPosition != ListView.INVALID_POSITION) {
                savedInstanceState.putInt(BUNDLE_SELECTED_KEY, mPosition);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mArtistAdapter = new ArtistsAdapter(getActivity(), new ArrayList<ParcelableSpotifyArtist>());
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        // Initialize the listView and set the content to mArtistAdapter
        mListView = (ListView) rootView.findViewById(R.id.listview_artists_search);
        mListView.setAdapter(mArtistAdapter);

//        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(BUNDLE_SEARCHTEXT_KEY);
            ArrayList<ParcelableSpotifyArtist> artistList = savedInstanceState.getParcelableArrayList(BUNDLE_ARTISTLIST_KEY);
            Log.d(LOG_TAG, "Restoring from instance " + artistList.size() + " results");
            mArtistAdapter.addAll(artistList);
            Log.d(LOG_TAG, mArtistAdapter.getCount() + " added to the adapter");

            mPosition = savedInstanceState.getInt(BUNDLE_SELECTED_KEY);
            mListView.smoothScrollToPosition(mPosition);
        }
        else if (mArtistAdapter.getCount() < 1 && searchText != null && !searchText.isEmpty())
        {
            performSearch(searchText);
        }

        // Set the item click listener to init the Top Tracks Activity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // clear the focus from mSearchField and hide the keyboard
                mSearchField.clearFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                // Call the onItemSelected on MainActivity and send the artistId
                String artistId = mArtistAdapter.getItem(position).id;
                ((Callback) getActivity())
                        .onItemSelected(artistId);
                mPosition = position;
            }
        });

        // Initialize the search field
        mSearchField = (EditText) rootView.findViewById(R.id.search_artist_field);
        mSearchField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If the text is not null, perform the search, if it is null then clear the adapter and cancel the running search if it exists
                if(!s.toString().isEmpty())
                {
                    if (!s.toString().equals(searchText)) {
                        searchText = s.toString();
                        performSearch(s.toString());
                    }
                } else {
                    if (runningTask != null){
                        runningTask.cancel(true);
                    }
                    mArtistAdapter.clear();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return rootView;
    }

    private void performSearch(String search) {
        if (runningTask == null) {
            runningTask = new FetchArtistSearchTask();
            runningTask.execute(search);
        } else {
            runningTask.cancel(true);
            runningTask = new FetchArtistSearchTask();
            runningTask.execute(search);
        }
    }

    private void postSearch (String status) {
        if (status.equals("NO_RESULT_FOUND")) {
            Context context = getActivity();
            CharSequence text = getString(R.string.search_artist_field_no_result_found);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.TOP, (int) getActivity().findViewById(R.id.listview_artists_search).getX(),
                    (int)getActivity().findViewById(R.id.listview_artists_search).getY());
            toast.show();
        }
    }

    public class FetchArtistSearchTask extends AsyncTask<String, String, ParcelableSpotifyArtist[]> {
        private final String LOG_TAG = FetchArtistSearchTask.class.getSimpleName();

        @Override
        protected ParcelableSpotifyArtist[] doInBackground(String... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                ArtistsPager results = spotify.searchArtists(params[0]);
                ParcelableSpotifyArtist[] artistArr = new ParcelableSpotifyArtist[results.artists.items.size()];

                if ( results.artists.items.size() < 1) {
                    publishProgress("NO_RESULT_FOUND");
                }

                for (int i = 0; i < results.artists.items.size(); i++) {
                    artistArr[i] = new ParcelableSpotifyArtist(results.artists.items.get(i));
                    Log.d(LOG_TAG, "Download completed " + artistArr[i].name);
                }

                return artistArr;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ParcelableSpotifyArtist[] result) {
            if (result != null) {
                mArtistAdapter.clear();
                mArtistAdapter.addAll(result);
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            postSearch(progress[0].toString());
        }
    }
}
