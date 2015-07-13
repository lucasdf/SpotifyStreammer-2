package com.example.lucas.spotifystreammerv2.Data;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Lucas on 18/06/2015.
 */
public class ParcelableSpotifyImage extends Image implements Parcelable {
    public static final Creator<ParcelableSpotifyImage> CREATOR
            = new Creator<ParcelableSpotifyImage>() {

        public ParcelableSpotifyImage createFromParcel(Parcel in) {
            return new ParcelableSpotifyImage(in);
        }

        public ParcelableSpotifyImage[] newArray(int size) {
            return new ParcelableSpotifyImage[size];
        }
    };

    public ParcelableSpotifyImage(Parcel in) {
        this.width = in.readInt();
        this.height = in.readInt();
        this.url = in.readString();
    }

    public ParcelableSpotifyImage(Image in) {
        this.width = in.width;
        this.height = in.height;
        this.url = in.url;
    }

    public ParcelableSpotifyImage(int width, int height, String url) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public ParcelableSpotifyImage() {
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeInt(width);
        out.writeInt(height);
        out.writeString(url);
    }
}
