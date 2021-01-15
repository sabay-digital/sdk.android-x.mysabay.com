package kh.com.mysabay.sdk.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Tan Phirum on 01/14/20
 * Gmail phirumtan@gmail.com
 */
public class Persona implements Parcelable {

    @SerializedName("uuid")
    @Expose
    public String uuid;

    @SerializedName("serviceCode")
    @Expose
    public String serviceCode;

    @SerializedName("mysabayUserID")
    @Expose
    public Integer mysabayUserID;

    @SerializedName("serviceUserID")
    @Expose
    public String serviceUserID;

    @SerializedName("serviceDisplayName")
    @Expose
    public String serviceDisplayName;

    @SerializedName("status")
    @Expose
    public Integer status;

    @SerializedName("lastLogin")
    @Expose
    public String lastLogin;

    public Persona(String uuid, String serviceCode, Integer mysabayUserID, String serviceUserID, String serviceDisplayName, Integer status, String lastLogin) {
        this.uuid = uuid;
        this.serviceCode = serviceCode;
        this.mysabayUserID = mysabayUserID;
        this.serviceUserID = serviceUserID;
        this.serviceDisplayName = serviceDisplayName;
        this.status = status;
        this.lastLogin = lastLogin;
    }

    public Persona() { }


    protected Persona(@NotNull Parcel in) {
        uuid = in.readString();
        serviceCode = in.readString();
        mysabayUserID = in.readInt();
        serviceUserID = in.readString();
        uuid = in.readString();
        serviceDisplayName = in.readString();
        status = in.readInt();
        lastLogin = in.readString();
    }

    public static final Creator<Persona> CREATOR = new Creator<Persona>() {
        @Override
        public Persona createFromParcel(Parcel in) {
            return new Persona(in);
        }

        @Override
        public Persona[] newArray(int size) {
            return new Persona[size];
        }
    };

    public Persona withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Persona withServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
        return this;
    }

    public Persona withMysabayUserID(Integer mysabayUserID) {
        this.mysabayUserID = mysabayUserID;
        return this;
    }

    public Persona withServiceUserID(String serviceUserID) {
        this.serviceUserID = serviceUserID;
        return this;
    }

    public Persona withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Persona withLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }

    public Persona withServiceDisplayName(String serviceDisplayName) {
        this.serviceDisplayName = serviceDisplayName;
        return this;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(serviceCode);
        dest.writeInt(mysabayUserID);
        dest.writeString(serviceUserID);
        dest.writeString(serviceDisplayName);
        dest.writeInt(status);
        dest.writeString(lastLogin);
    }
}