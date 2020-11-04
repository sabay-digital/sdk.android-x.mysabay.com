package kh.com.mysabay.sdk.pojo.thirdParty.payment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WingAuthorization implements Parcelable {

    @SerializedName("username")
    @Expose
    public String userName;
    @SerializedName("rest_api_key")
    @Expose
    public String restApiKey;
    @SerializedName("password")
    @Expose
    public String password;
    @SerializedName("biller_code")
    @Expose
    public String billerCode;
    @SerializedName("currency")
    @Expose
    public String currency;
    @SerializedName("sandbox")
    @Expose
    public Integer sandbox;

    public final static Creator<WingAuthorization> CREATOR = new Creator<WingAuthorization>() {


        @SuppressWarnings({
                "unchecked"
        })
        public WingAuthorization createFromParcel(Parcel in) {
            return new WingAuthorization(in);
        }

        public WingAuthorization[] newArray(int size) {
            return (new WingAuthorization[size]);
        }

    };

    protected WingAuthorization(Parcel in) {
        this.userName = ((String) in.readValue((String.class.getClassLoader())));
        this.restApiKey = ((String) in.readValue((String.class.getClassLoader())));
        this.password = ((String) in.readValue((String.class.getClassLoader())));
        this.billerCode = ((String) in.readValue((String.class.getClassLoader())));
        this.currency = ((String) in.readValue((String.class.getClassLoader())));
        this.sandbox = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public WingAuthorization() {
    }

    /**
     *
     * @param userName
     * @param restApiKey
     * @param password
     * @param billerCode
     * @param currency
     * @param sandbox
     */
    public WingAuthorization(String userName, String restApiKey, String password, String billerCode, String currency, Integer sandbox) {
        super();
        this.userName = userName;
        this.restApiKey = restApiKey;
        this.password = password;
        this.billerCode = billerCode;
        this.currency = currency;
        this.sandbox = sandbox;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("username", userName).append("restApiKey", restApiKey).append("password", password)
                .append("billerCode", billerCode).append("currency", currency).append("sandbox", sandbox).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(userName).append(restApiKey).append(password).append(billerCode).append(currency).append(sandbox).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WingAuthorization)) {
            return false;
        }
        WingAuthorization rhs = ((WingAuthorization) other);
        return new EqualsBuilder().append(userName, rhs.userName).append(restApiKey, rhs.restApiKey).append(password, rhs.password)
                .append(billerCode, rhs.billerCode).append(currency, rhs.currency).append(sandbox, rhs.sandbox).isEquals();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(userName);
        dest.writeValue(restApiKey);
        dest.writeValue(password);
        dest.writeValue(billerCode);
        dest.writeValue(currency);
        dest.writeValue(sandbox);
    }

    public int describeContents() {
        return 0;
    }

}
