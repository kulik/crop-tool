package com.tac.cropmodule;

import android.os.Parcel;
import android.os.Parcelable;

import org.opencv.core.Point;

public class Pin implements Parcelable {
    public volatile float x;
    public volatile float y;
    int radius;

    public Pin(float x, float y, int r) {
        this.x = x;
        this.y = y;
        this.radius = r;
    }

    public Point point() {
        return new Point(x, y);
    }

    public void set(float x, float y, int r) {
        this.x = x;
        this.y = y;
        this.radius = r;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
        dest.writeInt(this.radius);
    }

    private Pin(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.radius = in.readInt();
    }

    public static final Parcelable.Creator<Pin> CREATOR = new Parcelable.Creator<Pin>() {
        public Pin createFromParcel(Parcel source) {
            return new Pin(source);
        }

        public Pin[] newArray(int size) {
            return new Pin[size];
        }
    };
}