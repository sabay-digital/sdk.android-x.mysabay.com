package kh.com.mysabay.sdk.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.view.View;

import com.anjlab.android.iab.v3.BillingCommunicationException;
import com.anjlab.android.iab.v3.BillingHistoryRecord;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.PurchaseData;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import kh.com.mysabay.sdk.BuildConfig;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.adapter.ShopAdapter;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.callback.ShopListener;
import kh.com.mysabay.sdk.databinding.FmShopBinding;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.googleVerify.DataBody;
import kh.com.mysabay.sdk.pojo.googleVerify.GoogleVerifyBody;
import kh.com.mysabay.sdk.pojo.googleVerify.ReceiptBody;
import kh.com.mysabay.sdk.pojo.profile.UserProfileItem;
import kh.com.mysabay.sdk.pojo.shop.Data;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.ui.holder.ShopItmVH;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.viewmodel.StoreApiVM;

/**
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class ShopsFragment extends BaseFragment<FmShopBinding, StoreApiVM> implements ShopListener, BillingProcessor.IBillingHandler {

    public static final String TAG = ShopsFragment.class.getSimpleName();

    private ShopAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private String mySabayId;

    private BillingProcessor bp;
    private static String PURCHASE_ID = "android.test.purchased";

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
        mViewBinding.viewMainShop.setBackgroundResource(colorCodeBackground());
        mViewBinding.rcv.setBackgroundResource(colorCodeBackground());
        ShopItmVH.bindListener(this);

        mAdapter = new ShopAdapter(v.getContext());
        mAdapter.setHasStableIds(true);
        mLayoutManager = new GridLayoutManager(v.getContext(), getResources().getInteger(R.integer.layout_size));
        mViewBinding.rcv.setLayoutManager(mLayoutManager);
        mViewBinding.rcv.setAdapter(mAdapter);

        bp = new BillingProcessor(v.getContext(), MySabaySDK.getInstance().getSdkConfiguration().licenseKey, MySabaySDK.getInstance().getSdkConfiguration().merchantId, this);
        bp.initialize();

        MySabaySDK.getInstance().getUserProfile(info -> {
            Gson g = new Gson();
            UserProfileItem userProfile = g.fromJson(info, UserProfileItem.class);
            mySabayId = userProfile.data.mysabayUserId.toString();
            mViewBinding.tvMysabayid.setText(String.format(getString(R.string.mysabay_id), userProfile.data.mysabayUserId.toString()));
        });

        viewModel.getNetworkState().observe(this, this::showProgressState);
        viewModel.getShopItem().observe(this, item -> {
            mLayoutManager.setSpanCount(getResources().getInteger(R.integer.layout_size));
            mViewBinding.rcv.setLayoutManager(mLayoutManager);
            mAdapter.clear();
            for (Data ob : item.data) {
//                if (StringUtils.equalsIgnoreCase(ob.p, Data.PLAY_STORE))
                    mAdapter.insert(ob);
            }

            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void assignValues() {
        viewModel.getNetworkState().observe(this, this::showProgressState);

        if (getContext() != null)
            viewModel.getShopFromServer(getContext());

        MySabaySDK.getInstance().getUserProfile(info -> {
            Gson g = new Gson();
            UserProfileItem userProfile = g.fromJson(info, UserProfileItem.class);
            if (userProfile.data.balance.coin > 0) {
                String sabayCoin = "<b>" + userProfile.data.toSabayCoin() + "</b> ";
                mViewBinding.tvSabayCoinBalance.setText(Html.fromHtml(sabayCoin));
            }
            if (userProfile.data.balance.gold > 0) {
                String sabayGold = "<b>" + userProfile.data.toSabayGold() + "</b> ";
                mViewBinding.tvSabayGoldBalance.setText(Html.fromHtml(sabayGold));
                mViewBinding.deviderBalance.setVisibility(View.VISIBLE);
            } else {
                mViewBinding.tvSabayGoldBalance.setVisibility(View.GONE);
                mViewBinding.deviderBalance.setVisibility(View.GONE);
            }
            if (userProfile.data.balance.gold > 0 || userProfile.data.balance.coin > 0) {
                mViewBinding.sabayBalance.setVisibility(View.VISIBLE);
            } else {
                mViewBinding.sabayBalance.setVisibility(View.GONE);
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

    @Override
    public void shopInfo(Data item) {
        if (bp.isOneTimePurchaseSupported() && (item != null)) {
//            if (!BuildConfig.DEBUG)
                PURCHASE_ID = item.packageId;
            boolean isPurchase = bp.purchase(getActivity(), PURCHASE_ID);
            boolean isConsumePurchase = bp.consumePurchase(PURCHASE_ID);

            LogUtil.info(TAG, "purchase =" + isPurchase + ", comsumePurcase = " + isConsumePurchase);
        } else
            MessageUtil.displayDialog(getContext(), "sorry your device not support in app purchase");

    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        LogUtil.debug(TAG, "product id " + productId + " TransactionDetails :" + details + " : " + bp.getPurchaseTransactionDetails(productId));
        if (details == null) {
            SkuDetails skuDetails = bp.getPurchaseListingDetails(productId);
            LogUtil.debug(TAG, "skuDetails =" + skuDetails);
        } else {
            PurchaseInfo info = details.purchaseInfo;
            PurchaseData purchaseData = info.purchaseData;
            GoogleVerifyBody googleVerifyBody = new GoogleVerifyBody();
            ReceiptBody receiptBody = new ReceiptBody();
            receiptBody.withSignature(info.signature);
            DataBody dataBody = new DataBody(purchaseData.orderId, purchaseData.packageName, purchaseData.productId,
                    purchaseData.purchaseTime == null ? 0 : purchaseData.purchaseTime.getTime(), purchaseData.purchaseState.ordinal(), purchaseData.purchaseToken);
            receiptBody.withData(dataBody);
            googleVerifyBody.withReceipt(receiptBody);
            if (getActivity() != null)
                viewModel.postToVerifyAppInPurchase(getActivity(), googleVerifyBody);
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
        LogUtil.debug(TAG, "onPurchaseHistoryRestored");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            LogUtil.info(TAG, "user cancel purchase");
        }
        LogUtil.debug(TAG, "error code " + errorCode + " smg " + (error != null ? error.toString() : ""));
    }

    @Override
    public void onBillingInitialized() {
        LogUtil.debug(TAG, "onBillingInitialized");
        try {
            if (bp.isRequestBillingHistorySupported(Constants.PRODUCT_TYPE_MANAGED)) {
                Bundle extraParams = new Bundle();

                List<BillingHistoryRecord> lsBilling = bp.getPurchaseHistory(Constants.PRODUCT_TYPE_MANAGED, extraParams);
                LogUtil.debug(TAG, "listBilling " + lsBilling.size() + " payload ");

            }

        } catch (BillingCommunicationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (bp != null) bp.release();
        super.onDestroy();
    }
}