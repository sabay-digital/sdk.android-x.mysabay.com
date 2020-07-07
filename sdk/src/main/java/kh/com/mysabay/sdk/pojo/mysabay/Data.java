package kh.com.mysabay.sdk.pojo.mysabay;

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
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class Data implements Parcelable {

    @SerializedName("psp_code")
    @Expose
    public String pspCode;
    @SerializedName("psp_name")
    @Expose
    public String pspName;
    @SerializedName("psp_asset_code")
    @Expose
    public String pspAssetCode;
    @SerializedName("package_id")
    @Expose
    public String packageId;
    @SerializedName("package_code")
    @Expose
    public String packageCode;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("label")
    @Expose
    public String label;
    @SerializedName("price_in_usd")
    @Expose
    public String priceInUsd;
    @SerializedName("price_in_sabay_coin")
    @Expose
    public String priceInSC;
    @SerializedName("logo")
    @Expose
    public String logo;
    @SerializedName("request_url")
    @Expose
    public String requestUrl;
    @SerializedName("payment_type")
    @Expose
    public String paymentType;

    public final static Creator<Data> CREATOR = new Creator<Data>() {

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
        this.pspCode = ((String) in.readValue((String.class.getClassLoader())));
        this.pspName = ((String) in.readValue((String.class.getClassLoader())));
        this.pspAssetCode = ((String) in.readValue((String.class.getClassLoader())));
        this.packageId = ((String) in.readValue((String.class.getClassLoader())));
        this.packageCode = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.label = ((String) in.readValue((String.class.getClassLoader())));
        this.priceInUsd = ((String) in.readValue((String.class.getClassLoader())));
        this.priceInSC = ((String) in.readValue((String.class.getClassLoader())));
        this.logo = ((String) in.readValue((String.class.getClassLoader())));
        this.requestUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.paymentType = ((String) in.readValue((String.class.getClassLoader())));
        this.pspName = ((String) in.readValue((String.class.getClassLoader())));
        this.pspAssetCode = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public Data() {
    }

    /**
     *
     * @param pspCode
     * @param pspName
     * @param pspAssetCode
     * @param packageId
     * @param name
     * @param packageCode
     * @param label
     * @param priceInUsd
     * @param priceInSC
     * @param logo
     * @param requestUrl
     * @param paymentType
     */
    public Data(String pspCode, String pspName, String pspAssetCode, String packageId, String name, String packageCode, String label,
                String priceInUsd, String priceInSC, String logo, String requestUrl, String paymentType) {
        super();
        this.pspCode = pspCode;
        this.pspName = pspName;
        this.pspAssetCode = pspAssetCode;
        this.packageId = packageId;
        this.name = name;
        this.packageCode = packageCode;
        this.label = label;
        this.priceInUsd = priceInUsd;
        this.priceInSC = priceInSC;
        this.logo = logo;
        this.requestUrl = requestUrl;
        this.paymentType = paymentType;
    }

    public Data withPspCode(String pspCode) {
        this.pspCode = pspCode;
        return this;
    }

    public Data withPspName(String pspName) {
        this.pspName = pspName;
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

    public Data withPackageCode(String packageCode) {
        this.packageCode = packageCode;
        return this;
    }

    public Data withName(String name) {
        this.name = name;
        return this;
    }
    public Data withLabel(String label) {
        this.label = label;
        return this;
    }
    public Data withPriceInUsd(String priceInUsd) {
        this.priceInUsd = priceInUsd;
        return this;
    }
    public Data withPriceInSc(String priceInSC) {
        this.priceInSC = priceInSC;
        return this;
    }
    public Data withLogo(String logo) {
        this.logo = logo;
        return this;
    }
    public Data withRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }
    public Data withPaymentType(String paymentType) {
        this.paymentType = paymentType;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pspCode", pspCode).append("pspName", pspName).append("packageId", packageId)
                .append("packageCode", packageCode).append("name", name).append("label", label).append("priceInUsd", priceInUsd)
                .append("priceInSC", priceInSC).append("logo", logo).append("requestUrl", requestUrl).append("paymentType", paymentType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pspCode).append(pspName).append(pspAssetCode).append(packageId).append(packageCode).append(name).append(label)
                .append(priceInUsd).append(priceInSC).append(logo).append(requestUrl).append(paymentType).toHashCode();
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Data)) {
            return false;
        }
        Data rhs = ((Data) other);
        return new EqualsBuilder().append(pspCode, rhs.pspCode).append(pspName, rhs.pspName).append(pspAssetCode, rhs.pspAssetCode).append(packageId, rhs.packageId).append(name, rhs.name).append(packageCode, rhs.packageCode)
                .append(label, rhs.label).append(priceInUsd, rhs.priceInUsd).append(priceInSC, rhs.priceInSC).append(logo, rhs.logo).append(requestUrl, rhs.requestUrl).append(paymentType, rhs.paymentType).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(pspCode);
        dest.writeValue(pspName);
        dest.writeValue(pspAssetCode);
        dest.writeValue(packageId);
        dest.writeValue(packageCode);
        dest.writeValue(name);
        dest.writeValue(label);
        dest.writeValue(priceInUsd);
        dest.writeValue(priceInSC);
        dest.writeValue(logo);
        dest.writeValue(requestUrl);
        dest.writeValue(paymentType);
    }

    public int describeContents() {
        return 0;
    }

}