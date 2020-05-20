package kh.com.mysabay.sdk.callback;

import kh.com.mysabay.sdk.pojo.payment.SubscribePayment;

/**
 * Created by Tan Phirum on 3/28/20
 * Gmail phirumtan@gmail.com
 */
public interface PaymentListener {

    void purchaseSuccess(SubscribePayment data);

    void purchaseFailed(Object dataError);
}
