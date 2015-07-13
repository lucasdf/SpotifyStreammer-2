package com.example.lucas.spotifystreammerv2.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Lucas on 18/06/2015.
 */
public class ParcelableAlbumSimple extends AlbumSimple implements Parcelable{
    public List<ParcelableSpotifyImage> images;
    public static final Creator<ParcelableAlbumSimple> CREATOR
            = new Creator<ParcelableAlbumSimple>() {

        public ParcelableAlbumSimple createFromParcel(Parcel in) {
            return new ParcelableAlbumSimple(in);
        }

        public ParcelableAlbumSimple[] newArray(int size) {
            return new ParcelableAlbumSimple[size];
        }
    };

    public ParcelableAlbumSimple(Parcel in) {
        this.album_type = in.readString();
        this.available_markets = new ArrayList<String>();
        in.readStringList(available_markets);
        //public Map<String, String> external_urls;
        this.href = in.readString();
        this.id = in.readString();
        this.images = new ArrayList<ParcelableSpotifyImage>();
        in.readTypedList(images, ParcelableSpotifyImage.CREATOR);
        this.name = in.readString();
        this.type = in.readString();
        this.uri = in.readString();
    }

    public ParcelableAlbumSimple(AlbumSimple in) {
        this.album_type = in.album_type;
        this.available_markets = in.available_markets;
        this.href = in.href;
        this.id = in.id;
        this.images = new ArrayList<ParcelableSpotifyImage>();
        for (Image theImage : in.images) {
            images.add(new ParcelableSpotifyImage(theImage));
        }
        this.name = in.name;
        this.type = in.type;
        this.uri = in.uri;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeString(album_type);
        out.writeStringList(available_markets);
        out.writeString(href);
        out.writeString(id);
        out.writeTypedList(images);
        out.writeString(name);
        out.writeString(type);
        out.writeString(uri);
    }
}
