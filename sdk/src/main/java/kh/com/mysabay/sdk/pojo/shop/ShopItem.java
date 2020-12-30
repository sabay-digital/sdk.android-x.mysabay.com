package kh.com.mysabay.sdk.pojo.shop;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class ShopItem implements Parcelable {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("packageCode")
    @Expose
    public String packageCode;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("salePrice")
    @Expose
    public Double priceInUsd;
    @SerializedName("priceInSabayCoin")
    @Expose
    public Double priceInSC;
    @SerializedName("priceInSabayGold")
    @Expose
    public Double priceInSG;
    @SerializedName("paymentServiceProvider")
    @Expose
    public List<PaymentServiceProvider> paymentServiceProvider;

    public final static Creator<ShopItem> CREATOR = new Creator<ShopItem>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ShopItem createFromParcel(Parcel in) {
            return new ShopItem(in);
        }

        public ShopItem[] newArray(int size) {
            return (new ShopItem[size]);
        }

    };

    protected ShopItem(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.packageCode = ((String) in.readValue((String.class.getClassLoader())));
        this.displayName = ((String) in.readValue((String.class.getClassLoader())));
        this.priceInUsd = ((Double) in.readValue((Double.class.getClassLoader())));
        this.priceInSC = ((Double) in.readValue((Double.class.getClassLoader())));
        this.priceInSG = ((Double) in.readValue((Double.class.getClassLoader())));
        in.readList(this.paymentServiceProvider, (PaymentServiceProvider.class.getClassLoader()));
    }

    /**
     * No args constructor for use in serialization
     */
    public ShopItem() {
    }

    /**
     * @param id
     * @param packageCode
     * @param displayName
     * @param priceInUsd
     * @param priceInSC
     * @param priceInSG
     */
    public ShopItem(String id, String packageCode, String displayName, Double priceInUsd, Double priceInSC, Double priceInSG) {
        super();
        this.id = id;
        this.packageCode = packageCode;
        this.displayName = displayName;
        this.priceInUsd = priceInUsd;
        this.priceInSC = priceInSC;
        this.priceInSG = priceInSG;
    }

    public ShopItem withId(String id) {
        this.id = id;
        return this;
    }

    public ShopItem withPackageCode(String packageCode) {
        this.packageCode = packageCode;
        return this;
    }


    public ShopItem withName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ShopItem withPriceInUsd(Double priceInUsd) {
        this.priceInUsd = priceInUsd;
        return this;
    }

    public ShopItem withPriceInSc(Double priceInSC) {
        this.priceInSC = priceInSC;
        return this;
    }

    public ShopItem withPriceInSG(Double priceInSG) {
        this.priceInSG = priceInSG;
        return this;
    }

    public  ShopItem withPaymentServiceProvider(List<PaymentServiceProvider> paymentServiceProvider) {
        this.paymentServiceProvider = paymentServiceProvider;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("packageCode", packageCode)
                .append("displayName", displayName).append("priceInUsd", priceInUsd).append("priceInSC", priceInSC)
                .append("priceInSG", priceInSG).append("paymentServiceProvider", paymentServiceProvider).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(packageCode).append(priceInUsd).append(priceInSC).append(priceInSG).append(displayName).append(paymentServiceProvider).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ShopItem)) {
            return false;
        }
        ShopItem rhs = ((ShopItem) other);
        return new EqualsBuilder().append(id, rhs.id).append(packageCode, rhs.packageCode).append(priceInUsd, rhs.priceInUsd).append(priceInSC, rhs.priceInSC)
                .append(priceInSG, rhs.priceInSG).append(displayName, rhs.displayName).append(paymentServiceProvider, rhs.paymentServiceProvider).isEquals();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(packageCode);
        dest.writeValue(displayName);
        dest.writeValue(priceInUsd);
        dest.writeValue(priceInSC);
        dest.writeValue(priceInSG);
        dest.writeValue(paymentServiceProvider);
    }

    public int describeContents() {
        return 0;
    }

    public String toUSDPrice() {
        return "$ " + this.priceInUsd;
    }

    public String toSabayCoin() {
        return  this.priceInSC + " SC";
    }

    public String toRoundSabayCoin() {
        return  Math.round(this.priceInSC) + " SC";
    }

    public String toRoundSabayGold() {
        return  Math.round(this.priceInSG) + " SG";
    }

}