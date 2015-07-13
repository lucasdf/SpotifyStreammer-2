package com.example.lucas.spotifystreammerv2.Data;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.ArtistSimple;

/**
 * Created by Lucas on 18/06/2015.
 */
public class ParcelableSpotifyArtistSimple extends ArtistSimple implements Parcelable {
    public static final Creator<ParcelableSpotifyArtistSimple> CREATOR
            = new Creator<ParcelableSpotifyArtistSimple>() {

        public ParcelableSpotifyArtistSimple createFromParcel(Parcel in) {
            return new ParcelableSpotifyArtistSimple(in);
        }

        public ParcelableSpotifyArtistSimple[] newArray(int size) {
            return new ParcelableSpotifyArtistSimple[size];
        }
    };

    public ParcelableSpotifyArtistSimple(String href, String id, String name, String type, String uri) {
        this.href = href;
        this.id = id;
        this.name = name;
        this.type = type;
        this.uri = uri;
    }

    public ParcelableSpotifyArtistSimple(Parcel in) {
        this.href = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.type = in.readString();
        this.uri = in.readString();
    }

    public ParcelableSpotifyArtistSimple(ArtistSimple in) {
        this.href = in.href;
        this.id = in.id;
        this.name = in.name;
        this.type = in.type;
        this.uri = in.uri;
    }

    public ParcelableSpotifyArtistSimple() {
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
    }
}
