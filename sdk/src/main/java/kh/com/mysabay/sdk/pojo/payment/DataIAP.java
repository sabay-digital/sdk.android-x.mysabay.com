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

import kh.com.mysabay.sdk.pojo.googleVerify.ReceiptBody;

public class DataIAP implements Parcelable {

    @SerializedName("receipt")
    @Expose
    public ReceiptBody receipt;
    @SerializedName("package_id")
    @Expose
    public String packageId;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("price_in_usd")
    @Expose
    public Float priceInUsd;
    @SerializedName("price_in_sc")
    @Expose
    public Float priceInSc;

    public final static Parcelable.Creator<DataIAP> CREATOR = new Creator<DataIAP>() {


        @NotNull
        @Contract("_ -> new")
        public DataIAP createFromParcel(Parcel in) {
            return new DataIAP(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public DataIAP[] newArray(int size) {
            return (new DataIAP[size]);
        }

    };

    protected DataIAP(@NotNull Parcel in) {
        this.receipt = ((ReceiptBody) in.readValue((ReceiptBody.class.getClassLoader())));
        this.packageId = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.priceInUsd = ((Float) in.readValue((Float.class.getClassLoader())));
        this.priceInSc = ((Float) in.readValue((Float.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public DataIAP() {
    }

    /**
     *
     * @param receipt
     * @param packageId
     * @param name
     * @param priceInUsd
     * @param priceInSc
     */
    public DataIAP(ReceiptBody receipt, String packageId, String name, Float priceInUsd, Float priceInSc) {
        super();
        this.receipt = receipt;
        this.packageId = packageId;
        this.name = name;
        this.priceInUsd = priceInUsd;
        this.priceInSc = priceInSc;
    }

    public DataIAP withReceiptBody(ReceiptBody receipt) {
        this.receipt = receipt;
        return this;
    }

    public DataIAP withPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public DataIAP withName(String name) {
        this.name = name;
        return this;
    }

    public DataIAP withPriceInUsd(Float priceInUsd) {
        this.priceInUsd = priceInUsd;
        return this;
    }

    public DataIAP withPriceInSc(Float priceInSc) {
        this.priceInSc = priceInSc;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("recipt", receipt).append("packageId", packageId).append("name", name).append("priceInUsd", priceInUsd).append("priceInSc", priceInSc).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(receipt).append(packageId).append(name).append(priceInUsd).append(priceInSc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof kh.com.mysabay.sdk.pojo.googleVerify.ReceiptBody)) {
            return false;
        }
        DataIAP rhs = ((DataIAP) other);
        return new EqualsBuilder().append(receipt, rhs.receipt).append(packageId, rhs.packageId).append(name, rhs.name).append(priceInUsd, rhs.priceInUsd).append(priceInSc, rhs.priceInSc).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(receipt);
        dest.writeValue(packageId);
        dest.writeValue(name);
        dest.writeValue(priceInUsd);
        dest.writeValue(priceInSc);
    }

    public int describeContents() {
        return 0;
    }

}
