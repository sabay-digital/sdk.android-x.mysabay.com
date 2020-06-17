package kh.com.mysabay.sdk.pojo.logout;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class LogoutResponseItem implements Parcelable {

    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("data")
    @Expose
    public String data;
    public final static Creator<LogoutResponseItem> CREATOR = new Creator<LogoutResponseItem>() {


        @SuppressWarnings({
                "unchecked"
        })
        public LogoutResponseItem createFromParcel(Parcel in) {
            return new LogoutResponseItem(in);
        }

        public LogoutResponseItem[] newArray(int size) {
            return (new LogoutResponseItem[size]);
        }

    };

    protected LogoutResponseItem(Parcel in) {
        this.status = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.data = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public LogoutResponseItem() {
    }

    /**
     * @param data
     * @param status
     */
    public LogoutResponseItem(Integer status, String data) {
        super();
        this.status = status;
        this.data = data;
    }

    public LogoutResponseItem withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public LogoutResponseItem withData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LogoutResponseItem)) {
            return false;
        }
        LogoutResponseItem rhs = ((LogoutResponseItem) other);
        return new EqualsBuilder().append(data, rhs.data).append(status, rhs.status).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(status);
        dest.writeValue(data);
    }

    public int describeContents() {
        return 0;
    }

}
