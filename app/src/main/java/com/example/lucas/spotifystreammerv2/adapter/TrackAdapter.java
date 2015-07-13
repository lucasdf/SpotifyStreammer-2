package com.example.lucas.spotifystreammerv2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lucas.spotifystreammerv2.R;
import com.example.lucas.spotifystreammerv2.Data.ParcelableSpotifyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Lucas on 24/06/2015.
 */
public class TrackAdapter extends ArrayAdapter<ParcelableSpotifyTrack> {
    private static class ViewHolder {
        TextView name;
        ImageView picture;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.list_artists_textview);
            picture = (ImageView) view.findViewById(R.id.list_artists_imageview);
        }
    }

    public TrackAdapter(Context context, ArrayList<ParcelableSpotifyTrack> tracks) {
        super(context, R.layout.list_artist_detail, tracks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ParcelableSpotifyTrack track = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_artist_detail, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.name = (TextView) convertView.findViewById(R.id.artist_detail_list_textview);
            viewHolder.picture = (ImageView) convertView.findViewById(R.id.artist_detail_list_imageview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.name.setText(track.name + "\r\n" + track.album.name);

        // Populate the image: default image if there isn't one, the first one if there is only one, and the closest to 200px if there is more than one
        if (track.album.images.size() < 1) {
            Picasso.with(getContext()).load("http://placehold.it/50?text=").into(viewHolder.picture);
        } else if (track.album.images.size() == 1) {
            Picasso.with(getContext()).load(track.album.images.get(0).url).into(viewHolder.picture);
        } else if (track.album.images.size() > 1) {
            Image selectedImg = track.album.images.get(0);
            for (Image img : track.album.images) {
                if (img.width == 200) {
                    selectedImg = img;
                    break;
                } else if (img.width > 200 && img.width < selectedImg.width) {
                    selectedImg = img;
                }
            }
            Picasso.with(getContext()).load(selectedImg.url).into(viewHolder.picture);
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
