package kh.com.mysabay.sdk.pojo.payment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DataPayment implements Parcelable {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("price_in_usd")
    @Expose
    public Float priceInUsd;
    @SerializedName("price_in_sc")
    @Expose
    public Float priceInSc;
    @SerializedName("hash")
    @Expose
    public String hash;
    @SerializedName("asset_code")
    @Expose
    public String assetCode;
    @SerializedName("package_id")
    @Expose
    public String packageId;

    protected DataPayment(Parcel in) {
        this.packageId = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.hash = ((String) in.readValue((String.class.getClassLoader())));
        this.assetCode = ((String) in.readValue((String.class.getClassLoader())));
        this.priceInUsd = ((Float) in.readValue((Float.class.getClassLoader())));
        this.priceInSc = ((Float) in.readValue((Float.class.getClassLoader())));
    }

    public static final Creator<DataPayment> CREATOR = new Creator<DataPayment>() {
        @Override
        public DataPayment createFromParcel(Parcel in) {
            return new DataPayment(in);
        }

        @Override
        public DataPayment[] newArray(int size) {
            return new DataPayment[size];
        }
    };

    /**
     * No args constructor for use in serialization
     */
    public DataPayment() {
    }

    /**
     *
     * @param name
     * @param priceInUsd
     * @param priceInSc
     * @param hash
     * @param assetCode
     * @param packageId
     */
    public DataPayment(String name, Float priceInUsd, Float priceInSc, String hash, String assetCode, String packageId) {
        this.name = name;
        this.priceInUsd = priceInUsd;
        this.priceInSc = priceInSc;
        this.hash = hash;
        this.assetCode = assetCode;
        this.packageId = packageId;
    }

    public DataPayment withName(String name) {
        this.name = name;
        return this;
    }
    public DataPayment withPriceInUsd(Float priceInUsd) {
        this.priceInUsd = priceInUsd;
        return this;
    }

    public DataPayment withPriceInSc(Float priceInSc) {
        this.priceInSc = priceInSc;
        return this;
    }

    public DataPayment withHash(String hash) {
        this.hash = hash;
        return this;
    }

    public DataPayment withAssetCode(String assetCode) {
        this.assetCode = assetCode;
        return this;
    }

    public DataPayment withPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(packageId).append(name).append(priceInUsd).append(priceInSc).append(hash).append((assetCode)).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("priceInUsd", priceInUsd).append("priceInSc", priceInSc).append("hash", hash).append("assetCode", assetCode).append("packageId", packageId).toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DataPayment)) {
            return false;
        }
        DataPayment rhs = ((DataPayment) other);
        return new EqualsBuilder().append(name, rhs.name).append(packageId, rhs.packageId).append(priceInUsd, rhs.priceInUsd).append(priceInSc, rhs.priceInSc).append(assetCode, rhs.assetCode).append(hash, rhs.hash).isEquals();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(priceInUsd);
        dest.writeFloat(priceInSc);
        dest.writeString(hash);
        dest.writeString(assetCode);
        dest.writeString(packageId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
