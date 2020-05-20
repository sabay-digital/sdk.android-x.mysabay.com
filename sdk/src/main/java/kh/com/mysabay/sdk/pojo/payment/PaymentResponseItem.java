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

/**
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class PaymentResponseItem implements Parcelable {

    @SerializedName("status")
    @Expose
    public Integer status;
    @SerializedName("data")
    @Expose
    public Data data;
    public final static Parcelable.Creator<PaymentResponseItem> CREATOR = new Creator<PaymentResponseItem>() {


        @NotNull
        @Contract("_ -> new")
        public PaymentResponseItem createFromParcel(Parcel in) {
            return new PaymentResponseItem(in);
        }

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public PaymentResponseItem[] newArray(int size) {
            return (new PaymentResponseItem[size]);
        }

    };

    protected PaymentResponseItem(@NotNull Parcel in) {
        this.status = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.data = ((Data) in.readValue((Data.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public PaymentResponseItem() {
    }

    /**
     * @param status
     * @param data
     */
    public PaymentResponseItem(Integer status, Data data) {
        super();
        this.status = status;
        this.data = data;
    }

    public PaymentResponseItem withStatus(Integer status) {
        this.status = status;
        return this;
    }

    public PaymentResponseItem withData(Data data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("status", status).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PaymentResponseItem) == false) {
            return false;
        }
        PaymentResponseItem rhs = ((PaymentResponseItem) other);
        return new EqualsBuilder().append(data, rhs.data).append(status, rhs.status).isEquals();
    }

    public void writeToParcel(@NotNull Parcel dest, int flags) {
        dest.writeValue(status);
        dest.writeValue(data);
    }

    public int describeContents() {
        return 0;
    }

}