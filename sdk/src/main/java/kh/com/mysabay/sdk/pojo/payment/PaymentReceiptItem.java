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

import kh.com.mysabay.sdk.pojo.googleVerify.DataBody;

public class PaymentReceiptItem implements Parcelable {


    @SerializedName("status")
    @Expose
    public Integer status;
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
    @SerializedName("receipt")
    @Expose
    public DataBody receipt;
    public final static Parcelable.Creator<PaymentReceiptItem> CREATOR = new Creator<PaymentReceiptItem>() {


        @NotNull
        @Contract("_ -> new")
        public PaymentReceiptItem createFromParcel(Parcel in) {
            return new PaymentReceiptItem(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public PaymentReceiptItem[] newArray(int size) {
            return (new PaymentReceiptItem[size]);
        }

    };

    protected PaymentReceiptItem(@NotNull Parcel in) {
        this.status = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.hash = ((String) in.readValue((String.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.amount = ((String) in.readValue((String.class.getClassLoader())));
        this.pspAssetCode = ((String) in.readValue((String.class.getClassLoader())));
        this.packageId = ((String) in.readValue((String.class.getClassLoader())));
        this.bonus = ((String) in.readValue((String.class.getClassLoader())));
        this.receipt = (DataBody) in.readValue(DataBody.class.getClassLoader());
    }

    /**
     * No args constructor for use in serialization
     */
    public PaymentReceiptItem() {
    }

    /**
     * @param status
     * @param data
     * @param hash
     * @param message
     * @param amount
     * @param pspAssetCode
     * @param packageId
     * @param bonus
     * @param receipt
     */
    public PaymentReceiptItem(Integer status, Data data, String hash, String message, String amount, String pspAssetCode, String packageId, String bonus, DataBody receipt) {
        super();
        this.status = status;
        this.hash = hash;
        this.message = message;
        this.amount = amount;
        this.pspAssetCode = pspAssetCode;
        this.packageId = packageId;
        this.bonus = bonus;
        this.receipt = receipt;
    }

    public PaymentReceiptItem withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public PaymentReceiptItem withHash(String hash) {
        this.hash = hash;
        return this;
    }

    public PaymentReceiptItem withMessage(String message) {
        this.message = message;
        return this;
    }

    public PaymentReceiptItem withAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public PaymentReceiptItem withPspAssetCode(String pspAssetCode) {
        this.pspAssetCode = pspAssetCode;
        return this;
    }

    public PaymentReceiptItem withPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public PaymentReceiptItem withBonus(String bonus) {
        this.bonus = bonus;
        return this;
    }

    public PaymentReceiptItem withDataReceipt(DataBody receipt) {
        this.receipt = receipt;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("hash", hash).append("message", message)
                                               .append("amount", amount).append("pspAssetCode", pspAssetCode).append("packageId", packageId)
                                               .append("bonus", bonus).append(receipt).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(amount).append(pspAssetCode).append(message)
                                    .append(hash).append(packageId).append(bonus).append(receipt).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PaymentReceiptItem) == false) {
            return false;
        }
        PaymentReceiptItem rhs = ((PaymentReceiptItem) other);
        return new EqualsBuilder().append(status, rhs.status).append(amount, rhs.amount).append(pspAssetCode, rhs.pspAssetCode)
                                  .append(message, rhs.message).append(hash, rhs.hash).append(packageId, rhs.packageId)
                                  .append(bonus, rhs.bonus).append(receipt, rhs.receipt).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(status);
        dest.writeValue(hash);
        dest.writeValue(message);
        dest.writeValue(amount);
        dest.writeValue(pspAssetCode);
        dest.writeValue(packageId);
        dest.writeValue(bonus);
        dest.writeValue(receipt);
    }

    public int describeContents() {
        return 0;
    }
}
