package com.itpteam11.visualisemandai;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the User data model. It is Parcelable to pass between activity or fragment
 */
public class User implements Parcelable {
    private String name;
    private String status;
    private String email;
    private String type;
    private Double latitude;
    private Double longitude;
    private String account_status;
    private Map<String, String> group = new HashMap<String, String>();
    private Map<String, Boolean> service = new HashMap<String, Boolean>();

    public User() {}

    //Reconstruct User object from Parcelable
    public User(Parcel parcel) {
        name = parcel.readString();
        status = parcel.readString();
        email = parcel.readString();
        type = parcel.readString();
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
        account_status = parcel.readString();

        final int groupSize = parcel.readInt();
        for(int i=0; i<groupSize; i++) {
            String key = parcel.readString();
            String value = parcel.readString();
            group.put(key, value);
        }

        final int serviceSize = parcel.readInt();
        for(int i=0; i<serviceSize; i++) {
            String key = parcel.readString();
            Boolean value = parcel.readByte() != 0; //If byte == 1, value is true
            service.put(key, value);
        }
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }
    public String getType() { return type; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getAccount_Status() { return account_status; }
    public Map<String, String> getGroup() { return group; }
    public Map<String, Boolean> getService() { return service; }

    @Override
    public int describeContents() {
        return 0;
    }

    //Breakdown User object to be Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(email);
        dest.writeString(type);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(account_status);

        if(group != null) {
            dest.writeInt(group.size());
            for (Map.Entry<String, String> item : group.entrySet()) {
                dest.writeString(item.getKey());
                dest.writeString(item.getValue());
            }
        }
        else {
            dest.writeInt(0);
        }

        if(service != null) {
            dest.writeInt(service.size());
            for (Map.Entry<String, Boolean> item : service.entrySet()) {
                dest.writeString(item.getKey());
                dest.writeByte((byte) (item.getValue() ? 1 : 0)); //If value is true, byte == 1
            }
        }
        else {
            dest.writeInt(0);
        }
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
