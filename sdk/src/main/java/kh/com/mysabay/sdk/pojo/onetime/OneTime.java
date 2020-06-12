package kh.com.mysabay.sdk.pojo.onetime;

import android.webkit.JavascriptInterface;

import kh.com.mysabay.sdk.utils.LogUtil;

public class OneTime {
    @JavascriptInterface
    public void postMessage(Object object) {
        LogUtil.info("Message", "test");
    }
}