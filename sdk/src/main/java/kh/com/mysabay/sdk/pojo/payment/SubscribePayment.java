package kh.com.mysabay.sdk.pojo.payment;

/**
 * Created by Tan Phirum on 3/28/20
 * Gmail phirumtan@gmail.com
 */
public class SubscribePayment {

    public final String type;
    public final Object data;

    public SubscribePayment(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
