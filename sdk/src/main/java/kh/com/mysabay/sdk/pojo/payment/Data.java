package kh.com.mysabay.sdk.pojo.payment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Data  implements Parcelable {

    @SerializedName("hash")
    @Expose
    public String hash;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("amount")
    @Expose
    public String amount;
    @SerializedName("psp_asset_code")
    @Expose
    public String pspAssetCode;
    @SerializedName("package_id")
    @Expose
    public String packageId;
    @SerializedName("bonus")
    @Expose
    public String bonus;

    public final static Parcelable.Creator<Data> CREATOR = new Creator<Data>() {


        @NotNull
        @Contract("_ -> new")
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public Data[] newArray(int size) {
            return (new Data[size]);
        }

    };

    protected Data(@NotNull Parcel in) {
        this.hash = ((String) in.readValue((String.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.amount = ((String) in.readValue((String.class.getClassLoader())));
        this.pspAssetCode = ((String) in.readValue((String.class.getClassLoader())));
        this.packageId = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public Data() {
    }

    /**
     *
     * @param hash
     * @param message
     * @param amount
     * @param pspAssetCode
     * @param packageId
     * @param bonus
     */
    public Data(String hash, String message, String amount, String pspAssetCode, String packageId, String bonus) {
        super();
        this.hash = hash;
        this.message = message;
        this.amount = amount;
        this.pspAssetCode = pspAssetCode;
        this.packageId = packageId;
        this.bonus = bonus;
    }

    public Data withHash(String hash) {
        this.hash = hash;
        return this;
    }

    public Data withMessage(String message) {
        this.message = message;
        return this;
    }

    public Data withAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public Data withPspAssetCode(String pspAssetCode) {
        this.pspAssetCode = pspAssetCode;
        return this;
    }

    public Data withPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public Data withBonus(String bonus) {
        this.bonus = bonus;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("hash", hash).append("message", message).append("amount", amount).append("pspAssetCode", pspAssetCode).append("packageId", packageId).append("bonus", bonus).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(amount).append(pspAssetCode).append(message).append(hash).append(packageId).append(bonus).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Data) == false) {
            return false;
        }
        Data rhs = ((Data) other);
        return new EqualsBuilder().append(amount, rhs.amount).append(pspAssetCode, rhs.pspAssetCode).append(message, rhs.message).append(hash, rhs.hash).append(packageId, rhs.packageId).append(bonus, rhs.bonus).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(hash);
        dest.writeValue(message);
        dest.writeValue(amount);
        dest.writeValue(pspAssetCode);
        dest.writeValue(packageId);
        dest.writeValue(bonus);
    }

    public int describeContents() {
        return 0;
    }

}
