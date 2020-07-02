package kh.com.mysabay.sdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import kh.com.mysabay.sdk.utils.LogUtil;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = SmsBroadcastReceiver.class.getSimpleName();
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    String msg = "";
    private static MessageListener messageListener;

    @Override
    public void onReceive(Context context, Intent intent) {
       if (intent.getAction().equals(SMS_RECEIVED)) {
           Bundle dataBundle = intent.getExtras();
           if (dataBundle != null) {
               Object[] mypdu = (Object[])dataBundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[mypdu.length];
                for (int i = 0; i < mypdu.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = dataBundle.getString("format");
                        messages[i] = SmsMessage.createFromPdu((byte[])mypdu[i], format);
                    } else {
                        messages[i] = SmsMessage.createFromPdu((byte[]) mypdu[i]);
                    }

                    msg = messages[i].getMessageBody();
                    messageListener.messageReceived(msg);
                }
           }
       }
    }

    public static void bindListener(MessageListener listener){
        messageListener = listener;
    }
}
