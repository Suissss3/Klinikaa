package com.example.klinika;

import android.os.Parcel;
import android.os.Parcelable;

public class UserItem implements Parcelable {
    public String uid;
    public String name;

    public UserItem(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    protected UserItem(Parcel in) {
        uid = in.readString();
        name = in.readString();
    }

    public static final Creator<UserItem> CREATOR = new Creator<UserItem>() {
        @Override
        public UserItem createFromParcel(Parcel in) {
            return new UserItem(in);
        }

        @Override
        public UserItem[] newArray(int size) {
            return new UserItem[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(name);
    }
}
