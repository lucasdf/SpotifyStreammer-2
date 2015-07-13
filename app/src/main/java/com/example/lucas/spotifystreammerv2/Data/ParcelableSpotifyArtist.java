package com.example.lucas.spotifystreammerv2.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Lucas on 26/06/2015.
 */
public class ParcelableSpotifyArtist extends ParcelableSpotifyArtistSimple {
    public List<ParcelableSpotifyImage> images;
    public int popularity;
    public static final Parcelable.Creator<ParcelableSpotifyArtist> CREATOR
            = new Parcelable.Creator<ParcelableSpotifyArtist>() {

        public ParcelableSpotifyArtist createFromParcel(Parcel in) {
            return new ParcelableSpotifyArtist(in);
        }

        public ParcelableSpotifyArtist[] newArray(int size) {
            return new ParcelableSpotifyArtist[size];
        }
    };

    public ParcelableSpotifyArtist(Parcel in) {
        super(in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
        this.images = new ArrayList<ParcelableSpotifyImage>();
        in.readTypedList(images, ParcelableSpotifyImage.CREATOR);
        this.popularity = in.readInt();
    }

    public ParcelableSpotifyArtist(Artist in) {
        super(in.href, in.id, in.name, in.type, in.uri);
        this.images = new ArrayList<ParcelableSpotifyImage>();
        for (Image theImage : in.images) {
            images.add(new ParcelableSpotifyImage(theImage));
        }
        this.popularity = in.popularity;
    }

    public ParcelableSpotifyArtist() {
        //super();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(href);
        out.writeString(id);
        out.writeString(name);
        out.writeString(type);
        out.writeString(uri);
        out.writeTypedList(images);
        out.writeInt(popularity);
    }
}
