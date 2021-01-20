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

import kh.com.mysabay.sdk.pojo.Persona;

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
    @SerializedName("profileName")
    @Expose
    public String profileName;
    @SerializedName("coin")
    @Expose
    public Double coin;
    @SerializedName("gold")
    @Expose
    public Double gold;
    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("localPayEnabled")
    @Expose
    public Boolean localPayEnabled;

    @SerializedName("persona")
    @Expose
    public Persona persona;

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
        this.profileName = ((String) in.readValue((String.class.getClassLoader())));
        this.gold = ((Double) in.readValue((Double.class.getClassLoader())));
        this.coin = ((Double) in.readValue((Double.class.getClassLoader())));
        this.status = ((Integer) in.readValue((Integer.class.getClassLoader())));
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
     * @param localPayEnabled
     * @param status
     */
    public UserProfileItem(Integer id, Integer userID, Double coin, Double gold, Boolean localPayEnabled, Integer status) {
        super();
        this.id = id;
        this.userID = userID;
        this.coin = coin;
        this.gold = gold;
        this.status = status;
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

    public  UserProfileItem withProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public UserProfileItem withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public UserProfileItem withLocalPayEnabled(Boolean localPayEnabled ) {
        this.localPayEnabled = localPayEnabled;
        return this;
    }

    public UserProfileItem withPersona(Persona persona  ) {
        this.persona = persona;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("userID", userID)
                .append("profileName", profileName).append("gold", gold).append("coin", coin)
                .append("status", status).append("localPayEnabled", localPayEnabled).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userID).append(id).append(profileName).append(coin).append(gold).append(status).append(localPayEnabled).toHashCode();
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
        return new EqualsBuilder().append(userID, rhs.userID).append(id, rhs.id).append(profileName, rhs.profileName)
                .append(coin, rhs.coin).append(gold, rhs.gold).append(status, rhs.status)
                .append(localPayEnabled, rhs.localPayEnabled).isEquals();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(userID);
        dest.writeValue(profileName);
        dest.writeValue(coin);
        dest.writeValue(gold);
        dest.writeValue(status);
        dest.writeValue(localPayEnabled);
        dest.writeValue(persona);
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