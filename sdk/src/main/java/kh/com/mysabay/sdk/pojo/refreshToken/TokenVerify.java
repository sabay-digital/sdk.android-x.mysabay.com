package kh.com.mysabay.sdk.pojo.refreshToken;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TokenVerify implements Parcelable {

    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("message")
    @Expose
    public String message;
    public final static Creator<RefreshTokenItem> CREATOR = new Creator<RefreshTokenItem>() {


        @NotNull
        @Contract("_ -> new")
        @SuppressWarnings({
                "unchecked"
        })
        public RefreshTokenItem createFromParcel(Parcel in) {
            return new RefreshTokenItem(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public RefreshTokenItem[] newArray(int size) {
            return (new RefreshTokenItem[size]);
        }

    };

    protected TokenVerify(@NotNull Parcel in) {
        this.status = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public TokenVerify() {
    }

    /**
     * @param status
     * @param message
     */
    public TokenVerify(Integer status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public TokenVerify withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public TokenVerify withMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(message).append(status).toHashCode();
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RefreshTokenItem)) {
            return false;
        }
        TokenVerify rhs = ((TokenVerify) other);
        return new EqualsBuilder().append(message, rhs.message).append(status, rhs.status).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(status);
        dest.writeValue(message);
    }

    public int describeContents() {
        return 0;
    }

}