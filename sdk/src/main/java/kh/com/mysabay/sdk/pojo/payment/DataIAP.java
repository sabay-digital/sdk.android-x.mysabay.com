package kh.com.mysabay.sdk.pojo.payment;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class DataIAP {

    private Float priceInUsd;
    private Float priceInSc;
    private String hash;
    private String assetCode;
    private String packageId;

    public DataIAP withPriceInUsd(Float priceInUsd) {
        this.priceInUsd = priceInUsd;
        return this;
    }

    public DataIAP withPriceInSc(Float priceInSc) {
        this.priceInSc = priceInSc;
        return this;
    }

    public DataIAP withHash(String hash) {
        this.hash = hash;
        return this;
    }

    public DataIAP withAssetCode(String assetCode) {
        this.assetCode = assetCode;
        return this;
    }

    public DataIAP withPackageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public Float getPriceInUsd() {
        return priceInUsd;
    }

    public Float getPriceInSc() {
        return priceInSc;
    }

    public String getHash() {
        return hash;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public String getPackageId() {
        return packageId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("priceInUsd", priceInUsd).append("priceInSc", priceInSc).append("hash", hash).append("assetCode", assetCode).append("packageId", packageId).toString();
    }
}
