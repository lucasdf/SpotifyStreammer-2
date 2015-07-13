package com.example.lucas.spotifystreammerv2.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Lucas on 18/06/2015.
 */
public class ParcelableSpotifyTrack extends Track implements Parcelable {
    public List<ParcelableSpotifyArtistSimple> artists;
    public ParcelableAlbumSimple album;
    public static final Creator<ParcelableSpotifyTrack> CREATOR
            = new Creator<ParcelableSpotifyTrack>() {

        public ParcelableSpotifyTrack createFromParcel(Parcel in) {
            return new ParcelableSpotifyTrack(in);
        }

        public ParcelableSpotifyTrack[] newArray(int size) {
            return new ParcelableSpotifyTrack[size];
        }
    };

    public ParcelableSpotifyTrack(Parcel in) {
        this.album = in.readParcelable(ParcelableAlbumSimple.class.getClassLoader());
        /*        new ArrayList<ParcelableAlbumSimple>();
        in.readTypedList(album, ParcelableAlbumSimple.CREATOR);*/
        this.artists = new ArrayList<ParcelableSpotifyArtistSimple>();
        in.readTypedList(artists, ParcelableSpotifyArtistSimple.CREATOR);
        this.available_markets = new ArrayList<String>();
        in.readStringList(available_markets);
        this.disc_number = in.readInt();
        this.duration_ms = in.readLong();
        this.explicit = in.readByte() != 0;
        //this.external_urls = Map<String, String>();
        this.href = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.preview_url = in.readString();
        this.track_number = in.readInt();
        this.type = in.readString();
        this.uri = in.readString();
    }

    public ParcelableSpotifyTrack(Track in) {
        this.album = new ParcelableAlbumSimple(in.album);
        this.artists = new ArrayList<ParcelableSpotifyArtistSimple>();
        for (ArtistSimple theartist : in.artists) {
            artists.add(new ParcelableSpotifyArtistSimple(theartist));
        }
        this.available_markets = in.available_markets;
        this.disc_number = in.disc_number;
        this.duration_ms = in.duration_ms;
        this.explicit = in.explicit;
        //this.external_urls = Map<String, String>();
        this.href = in.href;
        this.id = in.id;
        this.name = in.name;
        this.preview_url = in.preview_url;
        this.track_number = in.track_number;
        this.type = in.type;
        this.uri = in.uri;


    }
    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeParcelable(album, flags);
        out.writeTypedList(artists);
        out.writeStringList(available_markets);
        out.writeInt(disc_number);
        out.writeLong(duration_ms);
        out.writeByte((byte) (explicit ? 1 : 0));
        out.writeString(href);
        out.writeString(id);
        out.writeString(name);
        out.writeString(preview_url);
        out.writeInt(track_number);
        out.writeString(type);
        out.writeString(uri);
    }




}
