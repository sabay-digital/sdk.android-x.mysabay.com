package kh.com.mysabay.sdk.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import kh.com.mysabay.sdk.BuildConfig;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.databinding.PartialBankProviderVerifiedBinding;
import kh.com.mysabay.sdk.pojo.onetime.OneTime;
import kh.com.mysabay.sdk.pojo.shop.ShopItem;
import kh.com.mysabay.sdk.pojo.thirdParty.payment.Data;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.viewmodel.StoreApiVM;

/**
 * Created by Tan Phirum on 4/1/20
 * Gmail phirumtan@gmail.com
 */
public class BankVerifiedFm extends BaseFragment<PartialBankProviderVerifiedBinding, StoreApiVM> {

    public static final String TAG = BankVerifiedFm.class.getSimpleName();
    private static final String EXT_KEY_PaymentResponseItem = "PaymentResponseItem";
    public static final String EXT_KEY_DATA = "EXT_KEY_DATA";
    public static final String PSP_CODE = "EXT_PSP_CODE";

    private ShopItem mData;
    private Data mPaymentResponseItem;
    private String pspCode;
    private boolean isFinished = false;

    @NotNull
    public static BankVerifiedFm newInstance(Data item, ShopItem shopItem, String pspCode) {
        Bundle args = new Bundle();
        args.putParcelable(EXT_KEY_PaymentResponseItem, item);
        args.putParcelable(EXT_KEY_DATA, shopItem);
        args.putString(PSP_CODE, pspCode);
        BankVerifiedFm f = new BankVerifiedFm();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            mPaymentResponseItem = getArguments().getParcelable(EXT_KEY_PaymentResponseItem);
            mData = getArguments().getParcelable(EXT_KEY_DATA);
            pspCode= getArguments().getString(PSP_CODE);
        }
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    @Override
    public int getLayoutId() {
        return R.layout.partial_bank_provider_verified;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initializeObjects(View v, Bundle args) {
        viewModel.setShopItemSelected(mData);
        if (args != null) {
            mViewBinding.wv.restoreState(args);
        } else {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
            mViewBinding.wv.getSettings().setJavaScriptEnabled(true);
            mViewBinding.wv.addJavascriptInterface(new OneTime(), "onetime");
            mViewBinding.wv.getSettings().setLoadsImagesAutomatically(true);
            mViewBinding.wv.getSettings().setLoadWithOverviewMode(true);
            mViewBinding.wv.getSettings().setUseWideViewPort(true);
            mViewBinding.wv.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            mViewBinding.wv.getSettings().setSupportZoom(true);
            mViewBinding.wv.getSettings().setBuiltInZoomControls(true);
            mViewBinding.wv.getSettings().setDisplayZoomControls(false);
            mViewBinding.wv.getSettings().setDomStorageEnabled(true);
            mViewBinding.wv.getSettings().setMinimumFontSize(1);
            mViewBinding.wv.getSettings().setMinimumLogicalFontSize(1);
            mViewBinding.wv.clearHistory();
            mViewBinding.wv.clearCache(true);

            mViewBinding.viewWeb.setBackgroundResource(colorCodeBackground());
            mViewBinding.wv.setWebViewClient(new WebViewClient() {
                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    LogUtil.debug(TAG, "shouldInterceptRequest : ");
                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    isFinished = true;
                    LogUtil.debug(TAG, "onPageFinished " + url);
                    if (url.contains("https://explorer.ssn.digital/v1/payments/")) {
                        if (getActivity() == null) return;

                        mViewBinding.btnBack.setVisibility(View.VISIBLE);
                        mViewBinding.btnClose.setVisibility(View.VISIBLE);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.debug(TAG, "payment completed");
                            }
                        });
                        LogUtil.debug(TAG, "payment success");
                    }
                }
            });

            String html;
            if (pspCode.equals("wing")) {
                html = wingFormValidate(mPaymentResponseItem);
            } else {
                html = scriptFormValidate(mPaymentResponseItem);
            }

            mViewBinding.wv.loadDataWithBaseURL(MySabaySDK.getInstance().storeApiUrl(), html, "text/html", "utf-8", MySabaySDK.getInstance().storeApiUrl());
        }
    }

    @Override
    public void assignValues() {
    }

    @Override
    public void addListeners() {
        mViewBinding.btnBack.setOnClickListener(v -> {
            if (mViewBinding.wv.canGoBack())
                mViewBinding.wv.goBack();
            else {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        mViewBinding.btnClose.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().finish();
        });
    }

    @Override
    public View assignProgressView() {
        return null;
    }

    @Override
    public View assignEmptyView() {
        return null;
    }

    @Override
    protected Class getViewModel() {
        return StoreApiVM.class;
    }

    @Override
    protected void onOnlineCallback() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((StoreActivity) context).userComponent.inject(this);
        // Now you can access loginViewModel here and onCreateView too
        // (shared instance with the Activity and the other Fragment)
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewBinding.wv.saveState(outState);
    }

    @NotNull
    @Contract(pure = true)
    private String scriptFormValidate(@NotNull Data item) {
        LogUtil.info("Data",  "'" + new Gson().toJson(item) + "'");

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bulma@0.8.0/css/bulma.min.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <script src=\"https://code.jquery.com/jquery-3.4.1.js\"></script>\n" +
                "    <h1>Please Wait</h1>\n" +
                "    <form id=\"frm\" action=\"" + item.requestUrl + "\" method=\"post\">\n" +
                "        <input type=\"hidden\" name=\"hash\" value=\"" + item.hash + "\">\n" +
                "        <input type=\"hidden\" name=\"signature\" value=\"" + item.signature + "\">\n" +
                "        <input type=\"hidden\" name=\"public_key\" value=\"" + item.publicKey + "\">\n" +
                "        <input type=\"hidden\" name=\"redirect\" value=\"" + item.redirect + "\">\n" +
                "    </form>\n" +
                "    <script>\n" +
                "        $( document ).ready(function() {\n" +
                "            $(\"#frm\").submit()\n" +
                "        });\n" +
                "    </script>\n" +
                "\u200B\n" +
                "</body>\n" +
                "</html>";
    }

    @NotNull
    @Contract(pure = true)
    private String wingFormValidate(@NotNull Data item) {
        LogUtil.info("Data",  "'" + new Gson().toJson(item) + "'");

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bulma@0.8.0/css/bulma.min.css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <script src=\"https://code.jquery.com/jquery-3.4.1.js\"></script>\n" +
                "    <h1>Please Wait</h1>\n" +
                "    <form id=\"frm\" action=\"" + item.requestUrl + "\" method=\"post\">\n" +
                "        <input type=\"hidden\" name=\"hash\" value=\"" + item.hash + "\">\n" +
                "        <input type=\"hidden\" name=\"signature\" value=\"" + item.signature + "\">\n" +
                "        <input type=\"hidden\" name=\"public_key\" value=\"" + item.publicKey + "\">\n" +
                "        <input type=\"hidden\" name=\"redirect\" value=\"" + item.redirect + "\">\n" +
                "        <input type=\"hidden\" name=\"wing_authorization\" value=\"" + "{&quot;username&quot;: &quot;" + item.wingAuthorization.userName + "&quot;," +
                "        &quot;rest_api_key&quot;: &quot;" + item.wingAuthorization.restApiKey + "&quot;, &quot;biller_code&quot;: &quot;" + item.wingAuthorization.billerCode + "&quot;," +
                "        &quot;password&quot;: &quot;" + item.wingAuthorization.password + "&quot;, &quot;currency&quot;: &quot;" + item.wingAuthorization.currency + "&quot;, " +
                "        &quot;sandbox&quot;: &quot;" + item.wingAuthorization.sandbox + "&quot; }" + "\">\n" +
                "    </form>\n" +
                "    <script>\n" +
                "        $( document ).ready(function() {\n" +
                "            $(\"#frm\").submit()\n" +
                "        });\n" +
                "    </script>\n" +
                "\u200B\n" +
                "</body>\n" +
                "</html>";
    }
}