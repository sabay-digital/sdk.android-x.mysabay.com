package kh.com.mysabay.sdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kh.com.mysabay.sdk.utils.LogUtil;

public interface MessageListener {
    void messageReceived(String message);
}
