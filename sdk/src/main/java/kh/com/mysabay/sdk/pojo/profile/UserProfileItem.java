package kh.com.mysabay.sdk.pojo.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Tan Phirum on 3/10/20
 * Gmail phirumtan@gmail.com
 */
public class UserProfileItem implements Parcelable {

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("userID")
    @Expose
    public Integer userID;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("coin")
    @Expose
    public Double coin;
    @SerializedName("gold")
    @Expose
    public Double gold;
    @SerializedName("vipPoints")
    @Expose
    public Double vipPoints;
    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("createdAt")
    @Expose
    public String createdAt;
    @SerializedName("updatedAt")
    @Expose
    public String updatedAt;
    @SerializedName("localPayEnabled")
    @Expose
    public Boolean localPayEnabled;


    public final static Creator<UserProfileItem> CREATOR = new Creator<UserProfileItem>() {


        @NotNull
        @Contract("_ -> new")
        @SuppressWarnings({
                "unchecked"
        })
        public UserProfileItem createFromParcel(Parcel in) {
            return new UserProfileItem(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public UserProfileItem[] newArray(int size) {
            return (new UserProfileItem[size]);
        }

    };

    protected UserProfileItem(@NotNull Parcel in) {
        this.id = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.userID = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.displayName = ((String) in.readValue((String.class.getClassLoader())));
        this.gold = ((Double) in.readValue((Double.class.getClassLoader())));
        this.coin = ((Double) in.readValue((Double.class.getClassLoader())));
        this.status = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.vipPoints = ((Double) in.readValue((Double.class.getClassLoader())));
        this.createdAt = ((String) in.readValue((String.class.getClassLoader())));
        this.updatedAt = ((String) in.readValue((String.class.getClassLoader())));
        this.localPayEnabled = ((Boolean) in.readValue((Integer.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public UserProfileItem() {
    }

    /**
     * @param id
     * @param userID
     * @param coin
     * @param gold
     * @param vipPoints
     * @param localPayEnabled
     * @param status
     * @param createdAt
     * @param updatedAt
     */
    public UserProfileItem(Integer id, Integer userID, Double coin, Double gold, Double vipPoints, Boolean localPayEnabled,
                Integer status, String createdAt, String updatedAt) {
        super();
        this.id = id;
        this.userID = userID;
        this.coin = coin;
        this.gold = gold;
        this.vipPoints = vipPoints;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.localPayEnabled = localPayEnabled;
    }

    public UserProfileItem withId(Integer id) {
        this.id = id;
        return this;
    }

    public UserProfileItem withUserId(Integer userID) {
        this.userID = userID;
        return this;
    }

    public UserProfileItem withCoin(Double coin) {
        this.coin = coin;
        return this;
    }

    public UserProfileItem withGold(Double gold) {
        this.gold = gold;
        return this;
    }

    public UserProfileItem withVipPoints(Double vipPoints) {
        this.vipPoints = vipPoints;
        return this;
    }

    public  UserProfileItem withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public UserProfileItem withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public UserProfileItem withCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UserProfileItem withUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public UserProfileItem withLocalPayEnabled(Boolean localPayEnabled ) {
        this.localPayEnabled = localPayEnabled;
        return this;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("userID", userID)
                .append("displayName", displayName).append("gold", gold).append("coin", coin)
                .append("vipPoints", vipPoints).append("status", status).append("createdAt", createdAt)
                .append("updatedAt", updatedAt).append("localPayEnabled", localPayEnabled).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userID).append(id).append(displayName).append(createdAt).append(coin).append(gold).append(vipPoints).append(status).append(updatedAt).append(localPayEnabled).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UserProfileItem)) {
            return false;
        }
        UserProfileItem rhs = ((UserProfileItem) other);
        return new EqualsBuilder().append(userID, rhs.userID).append(id, rhs.id).append(displayName, rhs.displayName)
                .append(coin, rhs.coin).append(gold, rhs.gold).append(vipPoints, rhs.vipPoints)
                .append(status, rhs.status).append(updatedAt, rhs.updatedAt)
                .append(createdAt, rhs.createdAt).append(localPayEnabled, rhs.localPayEnabled).isEquals();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(userID);
        dest.writeValue(displayName);
        dest.writeValue(coin);
        dest.writeValue(gold);
        dest.writeValue(vipPoints);
        dest.writeValue(status);
        dest.writeValue(localPayEnabled);
        dest.writeValue(createdAt);
        dest.writeValue(updatedAt);
    }

    public int describeContents() {
        return 0;
    }

    public String toSabayCoin() {
        return (String.format("%,.2f", coin)) + " SC";
    }

    public String toSabayGold() {
        return (String.format("%,.2f", gold)) + " SG";
    }

}