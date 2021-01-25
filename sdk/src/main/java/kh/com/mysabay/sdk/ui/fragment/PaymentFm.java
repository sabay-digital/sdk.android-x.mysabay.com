package kh.com.mysabay.sdk.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import kh.com.mysabay.sdk.adapter.BankProviderAdapter;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.billing.Security;
import kh.com.mysabay.sdk.databinding.FmPaymentBinding;
import kh.com.mysabay.sdk.databinding.PartialBankProviderBinding;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.googleVerify.DataBody;
import kh.com.mysabay.sdk.pojo.googleVerify.GoogleVerifyBody;
import kh.com.mysabay.sdk.pojo.googleVerify.ReceiptBody;
import kh.com.mysabay.sdk.pojo.mysabay.MySabayItemResponse;
import kh.com.mysabay.sdk.pojo.mysabay.ProviderResponse;
import kh.com.mysabay.sdk.pojo.profile.UserProfileItem;
import kh.com.mysabay.sdk.pojo.shop.ShopItem;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.utils.FontUtils;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.viewmodel.StoreApiVM;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

/**
 * Created by Tan Phirum on 3/15/20
 * Gmail phirumtan@gmail.com
 */
public class PaymentFm extends BaseFragment<FmPaymentBinding, StoreApiVM> implements PurchasesUpdatedListener {

    public static final String TAG = PaymentFm.class.getSimpleName();
    public static final String EXT_KEY_DATA = "EXT_KEY_DATA";

    private ShopItem mData;
    private static String PURCHASE_ID = "android.test.purchased";
    private MaterialDialog dialogBank;
    private Double balanceCoin;
    private Double balanceGold;
    private ClipboardManager myClipboard;
    private ClipData myClip;

    private BillingClient billingClient;
    public static final String PREF_FILE= "MyPref";
    public static final String PURCHASE_KEY= "purchase";

    @NotNull
    @Contract("_ -> new")
    public static PaymentFm newInstance(ShopItem item) {
        Bundle args = new Bundle();
        args.putParcelable(EXT_KEY_DATA, item);
        PaymentFm f = new PaymentFm();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null)
            mData = getArguments().getParcelable(EXT_KEY_DATA);

        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fm_payment;
    }

    @Override
    public void initializeObjects(@NotNull View v, Bundle args) {
        AppItem item = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        mViewBinding.viewMainPayment.setBackgroundResource(colorCodeBackground());
        mViewBinding.materialCardView.setBackgroundResource(colorCodeBackground());
        mViewBinding.btnPay.setTextColor(textColorCode());
        mViewBinding.cdSabayId.setBackgroundResource(colorCodeBackground());
        if (item.mysabayUserId != null)
            mViewBinding.tvMysabayid.setText(String.format(getString(R.string.mysabay_id), item.mysabayUserId.toString()));

        viewModel.setShopItemSelected(mData);
        viewModel.getMySabayCheckoutWithGraphQL(v.getContext(), mData.id);

        onBillingSetupFinished();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        MySabaySDK.getInstance().trackPageView(getContext(), "/sdk/checkout-screen", "/sdk/checkout-screen");

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
                MessageUtil.displayDialog(getContext(), "Service Disconnect");
            }
        });
    }

    boolean verifyInstallerId(Context context) {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    @Override
    public void assignValues() {
        viewModel.getNetworkState().observe(this, this::showProgressState);

        mViewBinding.btnPay.setEnabled(false);
        mViewBinding.btnPay.setBackgroundResource(R.color.secondary);
        viewModel.getItemSelected().observe(this, data -> {
            if (data != null) {
                mViewBinding.tvPoint.setText(data.displayName);
                mViewBinding.tvPrice.setText(data.toUSDPrice());
                mViewBinding.tvTotal.setText(data.toUSDPrice());
                mViewBinding.btnPay.setText(String.format(getString(R.string.pay), data.toUSDPrice()));
            }
        });

        MySabaySDK.getInstance().getUserProfile(info -> {
            Gson g = new Gson();
            if (info !=null) {
                UserProfileItem userProfile = g.fromJson(info, UserProfileItem.class);
                balanceCoin = userProfile.coin;
                balanceGold = userProfile.gold;

                if (balanceCoin > 0) {
                    String sabayCoin = "<b>" + userProfile.toSabayCoin() + "</b>";
                    mViewBinding.tvSabayCoinBalance.setText(Html.fromHtml(sabayCoin));
                    mViewBinding.tvMySabay.setText(getString(R.string.mysabay));
                }
                if (balanceGold > 0) {
                    String sabayGold = "<b style=\"color:blue;\">" + userProfile.toSabayGold() + "</b>";
                    mViewBinding.dividerBalance.setVisibility(balanceCoin > 0 ? View.VISIBLE : View.GONE);
                    mViewBinding.tvSabayGoldBalance.setText(Html.fromHtml(sabayGold));
                } else {
                    mViewBinding.tvSabayGoldBalance.setVisibility(View.GONE);
                    mViewBinding.dividerBalance.setVisibility(View.GONE);
                }

                if (balanceCoin > 0 || balanceGold > 0) {
                    mViewBinding.sabayBalance.setVisibility(View.VISIBLE);
                } else {
                    mViewBinding.sabayBalance.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getMySabayProvider().observe(this, mySabayItem -> {
                if (mySabayItem.size() > 0) {
                    for (MySabayItemResponse item : mySabayItem) {
                        mViewBinding.btnMysabay.setVisibility(View.VISIBLE);
                        if (checkMySabayProvider(mySabayItem)) {
                            if (item.type.equals("pre-auth")) {
                                for (ProviderResponse providerResponse : item.providers) {
                                    mViewBinding.btnLabel.setText(providerResponse.label);
                                    RequestOptions options = new RequestOptions()
                                            .placeholder(R.mipmap.sabay_account)
                                            .error(R.mipmap.sabay_account)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .priority(Priority.HIGH);
                                    Glide.with(getContext())
                                            .load(providerResponse.info.logo)
                                            .apply(options)
                                            .into(mViewBinding.imgMysabayLogo);
                                }
                            }
                        } else {
                            mViewBinding.btnMysabay.setVisibility(View.GONE);
                        }
                        if (item.type.equals("iap")) {
                            for (ProviderResponse providerResponse : item.providers) {
                                if (providerResponse.code.equals("play_store")) {
                                    LogUtil.info("Iap", providerResponse.label);
                                    //  if (verifyInstallerId(getActivity())) {
                                    mViewBinding.btnInAppPurchase.setVisibility(View.VISIBLE);
                                    //  } else {
                                    //    mViewBinding.btnInAppPurchase.setVisibility(View.GONE);
                                    //  }
                                    mViewBinding.lblInAppPurchase.setText(providerResponse.label);
                                    RequestOptions options = new RequestOptions()
                                            .placeholder(R.mipmap.iap)
                                            .error(R.mipmap.iap)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .priority(Priority.HIGH);

                                    Glide.with(getContext())
                                            .load(providerResponse.info.logo)
                                            .apply(options)
                                            .into(mViewBinding.imgInAppBillingLogo);
                                } else {
                                    mViewBinding.btnInAppPurchase.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
        });

        viewModel.getThirdPartyProviders().observe(this, data -> {
            if (data.size() > 0)
                showBankProviders(getContext(), data);
        });

        kh.com.mysabay.sdk.pojo.thirdParty.Data paidMethod = gson.fromJson(MySabaySDK.getInstance().getMethodSelected(), kh.com.mysabay.sdk.pojo.thirdParty.Data.class);
        if (paidMethod != null) {
            mViewBinding.btnPreAuthPay.setText(paidMethod.serviceName);
            mViewBinding.btnPreAuthPay.setVisibility(paidMethod.isPaidWith ? View.VISIBLE : View.GONE);
//            mViewBinding.rdbPreAuthPay.setChecked(paidMethod.isPaidWith);
        }
    }

    @Override
    public void addListeners() {
        AppItem item = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);

        mViewBinding.btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("text", item.mysabayUserId.toString());
                myClipboard.setPrimaryClip(myClip);
                MessageUtil.displayToast(v.getContext(), "Copied");
            }
        });

        final int[] checkedId = new int[1];

        mViewBinding.btnInAppPurchase.setOnClickListener(v -> {
            checkedId[0] = v.getId();
            ShopItem data = viewModel.getItemSelected().getValue();
            mViewBinding.tvTotal.setText(data.toUSDPrice());
            mViewBinding.btnPay.setText(String.format(getString(R.string.pay), data.toUSDPrice()));
            mViewBinding.btnPay.setEnabled(true);
            mViewBinding.btnPay.setBackgroundResource(R.color.colorYellow);
            mViewBinding.tvInAppPurchase.setTextColor(textColorCode());
            mViewBinding.btnInAppPurchase.setBackgroundResource(R.drawable.shape_button_primary);
            mViewBinding.lblInAppPurchase.setTextColor(textColorCode());
            mViewBinding.tvMySabay.setTextColor(0xFFE3B852);
            mViewBinding.btnMysabay.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.tvThirdBankProvider.setTextColor(0xFFE3B852);
            mViewBinding.btnThirdBankProvider.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.btnPreAuthPay.setTextColor(0xFFE3B852);
            mViewBinding.btnPreAuthPay.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.btnLabel.setTextColor(0xFF828181);
            mViewBinding.imgOtherPaymentLogo.setImageResource(R.mipmap.other_payment_option);
        });

        mViewBinding.btnMysabay.setOnClickListener(v -> {
            checkedId[0] = v.getId();
            ShopItem data = viewModel.getItemSelected().getValue();
            mViewBinding.tvTotal.setText(String.format(data.priceInSG <= balanceGold ? data.toRoundSabayGold() : data.toRoundSabayCoin()));
            mViewBinding.tvMySabay.setTextColor(textColorCode());
            mViewBinding.btnMysabay.setBackgroundResource(R.drawable.shape_button_primary);
            mViewBinding.tvInAppPurchase.setTextColor(0xFFE3B852);
            mViewBinding.btnInAppPurchase.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.lblInAppPurchase.setTextColor(0xFFE3B852);
            mViewBinding.tvThirdBankProvider.setTextColor(0xFFE3B852);
            mViewBinding.btnThirdBankProvider.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.btnPreAuthPay.setTextColor(0xFFE3B852);
            mViewBinding.btnLabel.setTextColor(textColorCode());
            mViewBinding.btnPreAuthPay.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.imgOtherPaymentLogo.setImageResource(R.mipmap.other_payment_option);
            if (data.priceInSC <= balanceCoin || data.priceInSG <= balanceGold) {
                mViewBinding.btnPay.setText(String.format(getString(R.string.pay), data.priceInSG <= balanceGold ? data.toRoundSabayGold() : data.toRoundSabayCoin()));
                mViewBinding.btnPay.setEnabled(true);
                mViewBinding.btnPay.setBackgroundResource(R.color.colorYellow);

            } else {
                mViewBinding.btnPay.setText(String.format(getString(R.string.pay), data.toRoundSabayCoin()));
                mViewBinding.btnPay.setEnabled(false);
                mViewBinding.btnPay.setBackgroundResource(R.color.secondary);
            }
        });

        mViewBinding.btnPreAuthPay.setOnClickListener(v -> {
            checkedId[0] = v.getId();
            ShopItem data = viewModel.getItemSelected().getValue();
            mViewBinding.tvTotal.setText(data.toUSDPrice());
            mViewBinding.btnPay.setText(String.format(getString(R.string.pay), data.toUSDPrice()));
            mViewBinding.btnPay.setEnabled(true);
            mViewBinding.btnPay.setBackgroundResource(R.color.colorYellow);
            mViewBinding.btnPreAuthPay.setTextColor(textColorCode());
            mViewBinding.btnPreAuthPay.setBackgroundResource(R.drawable.shape_button_primary);
            mViewBinding.tvMySabay.setTextColor(0xFFE3B852);
            mViewBinding.btnMysabay.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.tvInAppPurchase.setTextColor(0xFFE3B852);
            mViewBinding.btnInAppPurchase.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.lblInAppPurchase.setTextColor(0xFF828181);
            mViewBinding.tvThirdBankProvider.setTextColor(0xFFE3B852);
            mViewBinding.btnThirdBankProvider.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.imgOtherPaymentLogo.setImageResource(R.mipmap.other_payment_option);
            mViewBinding.btnLabel.setTextColor(0xFF828181);
        });

        mViewBinding.btnThirdBankProvider.setOnClickListener(v -> {
            checkedId[0] = v.getId();
            ShopItem data = viewModel.getItemSelected().getValue();
            mViewBinding.tvTotal.setText(data.toUSDPrice());
            mViewBinding.btnPay.setText(String.format(getString(R.string.pay), data.toUSDPrice()));
            mViewBinding.btnPay.setEnabled(true);
            mViewBinding.btnPay.setBackgroundResource(R.color.colorYellow);
            mViewBinding.imgOtherPaymentLogo.setImageResource(R.mipmap.payment_options_selected);
            mViewBinding.tvThirdBankProvider.setTextColor(textColorCode());
            mViewBinding.btnThirdBankProvider.setBackgroundResource(R.drawable.shape_button_primary);
            mViewBinding.tvMySabay.setTextColor(0xFFE3B852);
            mViewBinding.btnMysabay.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.tvInAppPurchase.setTextColor(0xFFE3B852);
            mViewBinding.btnInAppPurchase.setBackgroundResource(R.drawable.payment_button);
            mViewBinding.lblInAppPurchase.setTextColor(0xFF828181);
            mViewBinding.btnPreAuthPay.setTextColor(0xFFE3B852);
            mViewBinding.btnPreAuthPay.setBackgroundResource(R.drawable.payment_button);
            viewModel.get3PartyCheckout(v.getContext());
            mViewBinding.btnLabel.setTextColor(0xFF828181);
        });

        mViewBinding.btnPay.setOnClickListener(v -> {
            if (checkedId[0] == R.id.btn_in_app_purchase) {
                ProviderResponse data =  viewModel.getInAppPurchaseProvider(v.getContext());
                if (viewModel.getItemSelected().getValue() != null) {
//                    if (!BuildConfig.DEBUG)
                        PURCHASE_ID = data.id;
                        purchase(v, PURCHASE_ID);
                } else
                    MessageUtil.displayDialog(v.getContext(), "sorry your device not support in app purchase");

            } else if (checkedId[0] == R.id.btn_mysabay) {
                ShopItem data = viewModel.getItemSelected().getValue();
                if (data == null) return;

                MessageUtil.displayDialog(v.getContext(), getString(R.string.payment_confirmation),
                        String.format(getString(R.string.are_you_pay_with_my_sabay_provider), balanceGold >= data.priceInSG ? data.toRoundSabayGold() : data.toRoundSabayCoin()), getString(R.string.cancel),
                        getString(R.string.confirm), colorCodeBackground(), null,
                        (dialog, which) -> viewModel.postToPaidWithMySabayProvider(v.getContext(), balanceGold));

            } else if (checkedId[0] == R.id.btn_third_bank_provider) {
                viewModel.get3PartyCheckout(v.getContext());
            } else if (checkedId[0] == R.id.btn_pre_auth_pay) {
                ProviderResponse paidItem = gson.fromJson(MySabaySDK.getInstance().getMethodSelected(), ProviderResponse.class);
                if (paidItem != null)
                    viewModel.postToPaidWithBank((StoreActivity) getActivity(), paidItem);
            } else
                MessageUtil.displayToast(v.getContext(), getString(R.string.please_choose_payment_option));
        });

        mViewBinding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });

        mViewBinding.btnClose.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
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

    private void showBankProviders(Context context, List<ProviderResponse> data) {
        if (dialogBank != null) {
            dialogBank.dismiss();
        }
        PartialBankProviderBinding view = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.partial_bank_provider, null, false);
        view.viewPaymentProvider.setBackgroundResource(colorCodeBackground());
        RecyclerView rcv = view.bankRcv;
        BankProviderAdapter adapter = new BankProviderAdapter(context, data, item -> {
            viewModel.postToPaidWithBank((StoreActivity) getActivity(), (ProviderResponse) item);
            if (dialogBank != null)
                dialogBank.dismiss();
            viewModel._thirdPartyItemMediatorLiveData.setValue(new ArrayList<>());
            dialogBank = null;
        });
        rcv.setLayoutManager(new LinearLayoutManager(context));
        rcv.setHasFixedSize(true);
        rcv.setAdapter(adapter);
        dialogBank = new MaterialDialog.Builder(context)
                .typeface(FontUtils.getTypefaceKhmerBold(context), FontUtils.getTypefaceKhmer(context))
                .customView(view.getRoot(), true)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .backgroundColorRes(colorCodeBackground())
                .positiveColorRes(R.color.colorYellow)
                .positiveText(R.string.label_close).onPositive((dialog, which) -> {
                    dialog.dismiss();
                    dialogBank = null;
                    viewModel._thirdPartyItemMediatorLiveData.setValue(new ArrayList<>());

                }).build();
        dialogBank.show();
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
                    public void onSkuDetailsResponse(BillingResult billingResult, List<com.android.billingclient.api.SkuDetails> skuDetailsList) {
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
            MessageUtil.displayToast(getContext(),"Error "+billingResult.getDebugMessage());
        }
    }

    void handlePurchases(List<Purchase>  purchases) {
        for(Purchase purchase:purchases) {
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                MessageUtil.displayDialog(getContext(), "Invalid Purchase");
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

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            String base64Key = MySabaySDK.getInstance().getSdkConfiguration().licenseKey;
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean checkMySabayProvider(List<MySabayItemResponse> mySabayItemResponses) {
        boolean isFound = false;
        for (MySabayItemResponse mySabayItemResponse: mySabayItemResponses) {
            if (mySabayItemResponse.type.equals("pre-auth")) {
                isFound = true;
            }
        }
        return  isFound;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }

}

