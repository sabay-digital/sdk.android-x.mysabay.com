package kh.com.mysabay.sdk.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import android.text.Html;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.adapter.ShopAdapter;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.billing.Security;
import kh.com.mysabay.sdk.databinding.FmShopBinding;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.googleVerify.DataBody;
import kh.com.mysabay.sdk.pojo.googleVerify.GoogleVerifyBody;
import kh.com.mysabay.sdk.pojo.googleVerify.ReceiptBody;
import kh.com.mysabay.sdk.pojo.profile.UserProfileItem;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.viewmodel.StoreApiVM;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

/**
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class ShopsFragment extends BaseFragment<FmShopBinding, StoreApiVM> implements PurchasesUpdatedListener {

    public static final String TAG = ShopsFragment.class.getSimpleName();

    private ShopAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private String mySabayId;

    private static String PURCHASE_ID = "android.test.purchased";
    private BillingClient billingClient;
    public static final String PREF_FILE= "MyPref";
    public static final String PURCHASE_KEY= "purchase";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NotNull
    @Contract(" -> new")
    public static ShopsFragment newInstance() {
        return new ShopsFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fm_shop;
    }

    @Override
    public void initializeObjects(@NotNull View v, Bundle args) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        
        mViewBinding.viewMainShop.setBackgroundResource(colorCodeBackground());
        mViewBinding.rcv.setBackgroundResource(colorCodeBackground());
        mViewBinding.cdSabayId.setBackgroundResource(colorCodeBackground());
        if (appItem.mysabayUserId != null)
            mViewBinding.tvMysabayid.setText(String.format(getString(R.string.mysabay_id), appItem.mysabayUserId.toString()));

        if (getContext() != null)
            viewModel.getShopFromServerGraphQL(getContext());

        mAdapter = new ShopAdapter(v.getContext(), item -> {
            //    if (!BuildConfig.DEBUG)
            if (verifyInstallerId(getActivity())) {
//                PURCHASE_ID = item.packageId;
//                purchase(v, item.packageId);
//            } else {
//                MessageUtil.displayDialog(getActivity(), getString(R.string.application_do_not_support_in_app_purchase));
            }
        });

        mLayoutManager = new GridLayoutManager(v.getContext(), getResources().getInteger(R.integer.layout_size));
        mViewBinding.rcv.setLayoutManager(mLayoutManager);
        mViewBinding.rcv.setAdapter(mAdapter);

        onBillingSetupFinished();

        viewModel.getNetworkState().observe(this, this::showProgressState);
        viewModel.getShopItem().observe(this, item -> {
            mLayoutManager.setSpanCount(getResources().getInteger(R.integer.layout_size));
            mViewBinding.rcv.setLayoutManager(mLayoutManager);
            mAdapter.clear();
            mAdapter.insert(item);
            mAdapter.notifyDataSetChanged();
        });

        MySabaySDK.getInstance().trackPageView(getContext(), "/sdk/product-screen", "/sdk/product-screen");
        MySabaySDK.getInstance().setEcommerce(getContext());
    }

    @Override
    public void assignValues() {
        viewModel.getNetworkState().observe(this, this::showProgressState);
        MySabaySDK.getInstance().getUserProfile(info -> {
            if (info != null) {
                Gson g = new Gson();
                UserProfileItem userProfile = g.fromJson(info, UserProfileItem.class);
                if (userProfile.coin > 0) {
                    String sabayCoin = "<b>" + userProfile.toSabayCoin() + "</b> ";
                    mViewBinding.tvSabayCoinBalance.setText(Html.fromHtml(sabayCoin));
                }
                if (userProfile.gold > 0) {
                    String sabayGold = "<b>" + userProfile.toSabayGold() + "</b> ";
                    mViewBinding.tvSabayGoldBalance.setText(Html.fromHtml(sabayGold));
                    mViewBinding.deviderBalance.setVisibility(userProfile.coin > 0 ? View.VISIBLE : View.GONE);
                } else {
                    mViewBinding.tvSabayGoldBalance.setVisibility(View.GONE);
                    mViewBinding.deviderBalance.setVisibility(View.GONE);
                }
                if (userProfile.gold > 0 || userProfile.coin > 0) {
                    mViewBinding.sabayBalance.setVisibility(View.VISIBLE);
                } else {
                    mViewBinding.sabayBalance.setVisibility(View.GONE);
                }
            } else {
                MessageUtil.displayDialog(getActivity(), getString(R.string.msg_can_not_connect_server));
            }
        });
    }

    @Override
    public void addListeners() {
        assert mViewBinding.btnClose != null;
        AppItem item = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);

        mViewBinding.btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("text", mySabayId);
                myClipboard.setPrimaryClip(myClip);
                MessageUtil.displayToast(v.getContext(), "Copied");
            }
        });

        mViewBinding.btnClose.setOnClickListener(v -> {
            if (v.getContext() instanceof StoreActivity)
                ((StoreActivity) v.getContext()).onBackPressed();
        });
    }

    @Override
    public View assignProgressView() {
        return mViewBinding.viewEmpty.progressBar;
    }

    @Override
    public View assignEmptyView() {
        return mViewBinding.viewEmpty.viewRetry;
    }

    @Override
    protected Class<StoreApiVM> getViewModel() {
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

    boolean verifyInstallerId(Context context) {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    public void onBillingSetupFinished() {
        billingClient = BillingClient.newBuilder(getContext())
                .enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(INAPP);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
                    if(queryPurchases!=null && queryPurchases.size()>0){
                        handlePurchases(queryPurchases);
                    }
                    else{
                        savePurchaseValueToPref(false);
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getContext().getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }
    private void savePurchaseValueToPref(boolean value){
        getPreferenceEditObject().putBoolean(PURCHASE_KEY,value).commit();
    }

    public void purchase(View view, String productId) {
        //check if service is already connected
        if (billingClient.isReady()) {
            initiatePurchase(productId);
        }
        //else reconnect service
        else{
            billingClient = BillingClient.newBuilder(getContext()).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase(productId);
                    } else {
                        MessageUtil.displayToast(getContext(),"Error "+billingResult.getDebugMessage());
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }

    private void initiatePurchase(String productId) {
        List<String> skuList = new ArrayList<>();
        skuList.add(productId);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(getActivity(), flowParams);
                            }
                            else{
                                MessageUtil.displayToast(getContext(),"Purchase Item not Found");
                            }
                        } else {
                            MessageUtil.displayToast(getContext(), billingResult.getDebugMessage());
                        }
                    }

                });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if(alreadyPurchases!=null){
                handlePurchases(alreadyPurchases);
            }
        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            MessageUtil.displayToast(getContext(),"Purchase Canceled");
        }
        // Handle any other error msgs
        else {
            MessageUtil.displayToast(getContext(),"Error " +billingResult.getDebugMessage());
        }
    }

    //handle purchase
    void handlePurchases(List<Purchase>  purchases) {
        for(Purchase purchase:purchases) {

            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                return;
            }

            if (!purchase.isAcknowledged()) {
                handlePurchase(purchase);
            }

            if (purchase != null && StringUtils.equals(purchase.getSku(), PURCHASE_ID)) {
                try {
                    GoogleVerifyBody googleVerifyBody = new GoogleVerifyBody();
                    ReceiptBody receiptBody = new ReceiptBody();
                    receiptBody.withSignature(purchase.getSignature());
                    DataBody dataBody = new DataBody(purchase.getOrderId(), purchase.getPackageName(), purchase.getSku(),
                            purchase.getPurchaseTime(), purchase.getPurchaseState(), purchase.getPurchaseToken());
                    receiptBody.withData(dataBody);
                    googleVerifyBody.withReceipt(receiptBody);
                    viewModel.postToVerifyAppInPurchase(getActivity(), googleVerifyBody);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }

    }

    // handle consume purchase
    void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    LogUtil.info("Consume Purchase", "consume");
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    //verify signature
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = MySabaySDK.getInstance().getSdkConfiguration().licenseKey;
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(billingClient != null){
            billingClient.endConnection();
            billingClient = null;
        }
    }

}
