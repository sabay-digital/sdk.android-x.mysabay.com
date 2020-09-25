package kh.com.mysabay.sdk.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import kh.com.mysabay.sdk.Globals;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.SdkConfiguration;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.NetworkState;
import kh.com.mysabay.sdk.pojo.googleVerify.GoogleVerifyBody;
import kh.com.mysabay.sdk.pojo.googleVerify.GoogleVerifyResponse;
import kh.com.mysabay.sdk.pojo.mysabay.MySabayItem;
import kh.com.mysabay.sdk.pojo.payment.PaymentBody;
import kh.com.mysabay.sdk.pojo.payment.PaymentResponseItem;
import kh.com.mysabay.sdk.pojo.payment.SubscribePayment;
import kh.com.mysabay.sdk.pojo.shop.Data;
import kh.com.mysabay.sdk.pojo.shop.ShopItem;
import kh.com.mysabay.sdk.pojo.thirdParty.payment.ResponseItem;
import kh.com.mysabay.sdk.repository.StoreRepo;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.ui.fragment.BankVerifiedFm;
import kh.com.mysabay.sdk.ui.fragment.PaymentFm;
import kh.com.mysabay.sdk.utils.AppRxSchedulers;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.webservice.AbstractDisposableObs;

/**
 * Created by Tan Phirum on 3/8/20
 * Gmail phirumtan@gmail.com
 */
public class StoreApiVM extends ViewModel {

    private static final String TAG = StoreApiVM.class.getSimpleName();

    private final StoreRepo storeRepo;
    private final SdkConfiguration sdkConfiguration;

    @Inject
    AppRxSchedulers appRxSchedulers;
    @Inject
    Gson gson;

    private final MediatorLiveData<String> _msgError = new MediatorLiveData<>();
    private final MediatorLiveData<NetworkState> _networkState;
    private final MediatorLiveData<ShopItem> _shopItem;
    private final CompositeDisposable mCompos;
    private final MediatorLiveData<Data> mDataSelected;
    private final MediatorLiveData<MySabayItem> mySabayItemMediatorLiveData;
    public final MediatorLiveData<List<kh.com.mysabay.sdk.pojo.mysabay.Data>> _thirdPartyItemMediatorLiveData;


    @Inject
    public StoreApiVM(StoreRepo storeRepo) {
        this.storeRepo = storeRepo;
        this._networkState = new MediatorLiveData<>();
        this._shopItem = new MediatorLiveData<>();
        this.mCompos = new CompositeDisposable();
        this.mDataSelected = new MediatorLiveData<>();
        this.mySabayItemMediatorLiveData = new MediatorLiveData<>();
        this._thirdPartyItemMediatorLiveData = new MediatorLiveData<>();
        this.sdkConfiguration = MySabaySDK.getInstance().getSdkConfiguration();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mCompos != null) {
            mCompos.dispose();
            mCompos.clear();
        }
    }

    /**
     * List all item from server
     *
     * @param context
     */
    public void getShopFromServer(@NotNull Context context) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        storeRepo.getShopItem(sdkConfiguration.appSecret, appItem.token).subscribeOn(appRxSchedulers.io())
                .observeOn(appRxSchedulers.mainThread()).subscribe(new AbstractDisposableObs<ShopItem>(context, _networkState) {
            @Override
            protected void onSuccess(ShopItem item) {
                LogUtil.info("ITEM", item.toString());
                if (item.status == 200)
                    _shopItem.setValue(item);
                else MessageUtil.displayDialog(context, "something went wrong.");
            }

            @Override
            protected void onErrors(Throwable error) {
                LogUtil.error(TAG, error.getLocalizedMessage());
            }
        });
    }

    public LiveData<ShopItem> getShopItem() {
        return _shopItem;
    }

    public LiveData<NetworkState> getNetworkState() {
        return _networkState;
    }

    public LiveData<List<kh.com.mysabay.sdk.pojo.mysabay.Data>> getThirdPartyProviders() {
        return _thirdPartyItemMediatorLiveData;
    }

    public LiveData<MySabayItem> getMySabayProvider() {
        return mySabayItemMediatorLiveData;
    }

    public void setShopItemSelected(Data data) {
        _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
        this.mDataSelected.setValue(data);
    }

    public LiveData<Data> getItemSelected() {
        return this.mDataSelected;
    }

    /**
     * Check user has authorize to use with mysabay payment or not
     *
     * @param context
     */
    public void getMySabayCheckout(@NotNull Context context, String packageCode) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        storeRepo.getMySabayCheckout(sdkConfiguration.appSecret, appItem.token, packageCode).subscribeOn(appRxSchedulers.io())
                .observeOn(appRxSchedulers.mainThread()).subscribe(new Observer<MySabayItem>() {
            @Override
            public void onSubscribe(Disposable d) {
                mCompos.add(d);
            }

            @Override
            public void onNext(MySabayItem mySabayItem) {
                mySabayItemMediatorLiveData.setValue(mySabayItem);
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.debug(TAG, "error " + e.getLocalizedMessage());
                //        get3PartyCheckout(context);
            }

            @Override
            public void onComplete() {
                //      get3PartyCheckout(context);
            }
        });

    }

    /**
     * show list all bank provider
     *
     * @param context
     */
    public void get3PartyCheckout(@NotNull Context context) {
        if (getMySabayProvider().getValue() == null) return;

        MySabayItem mySabayItem = getMySabayProvider().getValue();
        List<kh.com.mysabay.sdk.pojo.mysabay.Data> result = new ArrayList<>();

        for (kh.com.mysabay.sdk.pojo.mysabay.Data item : mySabayItem.data) {
            if (item.paymentType.equals("onetime")) {
                result.add(item);
            }
        }
        _thirdPartyItemMediatorLiveData.setValue(result);
    }

    /**
     * show list all bank provider
     *
     * @param context
     */
    public kh.com.mysabay.sdk.pojo.mysabay.Data getInAppPurchaseProvider(@NotNull Context context) {
        if (getMySabayProvider().getValue() == null) return new kh.com.mysabay.sdk.pojo.mysabay.Data();

        MySabayItem mySabayItem = getMySabayProvider().getValue();
        kh.com.mysabay.sdk.pojo.mysabay.Data provider  = new kh.com.mysabay.sdk.pojo.mysabay.Data();

        for (kh.com.mysabay.sdk.pojo.mysabay.Data item : mySabayItem.data) {
            if (item.paymentType.equals("iap")) {
                provider = item;
            }
        }
        return provider;
    }

    public void postToVerifyAppInPurchase(@NotNull Context context, @NotNull GoogleVerifyBody body) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        mCompos.add(storeRepo.postToVerifyGoogle(sdkConfiguration.appSecret, appItem.token, body).subscribeOn(appRxSchedulers.io())
                .observeOn(appRxSchedulers.mainThread()).subscribe(new Consumer<GoogleVerifyResponse>() {
                    @Override
                    public void accept(GoogleVerifyResponse googleVerifyResponse) throws Exception {
                        EventBus.getDefault().post(new SubscribePayment(Globals.APP_IN_PURCHASE, body));
                        ((Activity) context).finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MessageUtil.displayDialog(context, "Error" + throwable.getMessage());
                    }
                }));
    }

    /**
     * This method is use to buy item with mysabay payment
     *
     * @param context
     */
    public void postToPaidWithMySabayProvider(Context context, Float balanceGold) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        Data shopItem = getItemSelected().getValue();
        if (getMySabayProvider().getValue() == null) return;

        List<kh.com.mysabay.sdk.pojo.mysabay.Data> listMySabayProvider = new ArrayList<>();
        for (kh.com.mysabay.sdk.pojo.mysabay.Data item : getMySabayProvider().getValue().data) {
            if (item.paymentType.equals("pre-authorized")) {
                if (item.pspCode.equals("sabay")) {
                    listMySabayProvider.add(0, item);
                } else {
                    listMySabayProvider.add(item);
                }
            }
        }

        if (listMySabayProvider.size() > 0 && shopItem != null) {
            PaymentBody body;

//            if (balanceGold >= shopItem.priceInSG) {
//                body = new PaymentBody(appItem.uuid, shopItem.priceInSG.toString(), listMySabayProvider.get(0).pspCode.toLowerCase(), listMySabayProvider.get(0).pspAssetCode.toLowerCase(), shopItem.packageCode);
//            } else {}
                body = new PaymentBody(appItem.uuid, shopItem.priceInSc.toString(), listMySabayProvider.get(0).pspCode.toLowerCase(), listMySabayProvider.get(0).pspAssetCode.toLowerCase(), shopItem.packageCode);

            storeRepo.postToPaid(sdkConfiguration.appSecret, appItem.token, body).subscribeOn(appRxSchedulers.io())
                    .observeOn(appRxSchedulers.mainThread())
                    .subscribe(new AbstractDisposableObs<PaymentResponseItem>(context, _networkState) {
                        @Override
                        protected void onSuccess(PaymentResponseItem item) {
                            EventBus.getDefault().post(new SubscribePayment(Globals.MY_SABAY, item));
                            ((Activity) context).finish();
                        }

                        @Override
                        protected void onErrors(Throwable error) {
                            LogUtil.info("Payment-Error",  error.getMessage());
                            EventBus.getDefault().post(new SubscribePayment(Globals.MY_SABAY, error.getMessage()));
                        }
                    });
        }
    }


    public void postToPaidWithBank(StoreActivity context, kh.com.mysabay.sdk.pojo.mysabay.Data data) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        Data shopItem = getItemSelected().getValue();

        if (data != null && shopItem != null) {
            PaymentBody body = new PaymentBody(appItem.uuid, shopItem.priceInUsd.toString(), data.pspCode.toLowerCase(), data.pspAssetCode.toLowerCase(), data.packageCode);
            Gson gson = new Gson();
            String json = gson.toJson(body);
            LogUtil.info("PaymentBody", json);
            storeRepo.postToChargeOneTime(sdkConfiguration.appSecret, appItem.token, body).subscribeOn(appRxSchedulers.io())
                    .observeOn(appRxSchedulers.mainThread())
                    .subscribe(new AbstractDisposableObs<ResponseItem>(context, _networkState) {
                        @Override
                        protected void onSuccess(ResponseItem response) {
                            if (response.status == 200) {
//                                MySabaySDK.getInstance().saveMethodSelected(gson.toJson(data.withIsPaidWith(false)));
                                LogUtil.info("PaymentBody", response.toString());
                                context.initAddFragment(BankVerifiedFm.newInstance(response.data, shopItem), PaymentFm.TAG, true);
                            } else
                                MessageUtil.displayDialog(context, gson.toJson(response));
                        }

                        @Override
                        protected void onErrors(Throwable error) {
                            MessageUtil.displayDialog(context, gson.toJson(error));
                            LogUtil.info(TAG, "error " + error.getLocalizedMessage());
                        }
                    });
        }
    }
}

