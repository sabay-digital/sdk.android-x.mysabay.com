package kh.com.mysabay.sdk;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.mysabay.sdk.DeleteTokenMutation;
import com.mysabay.sdk.RefreshTokenMutation;
import com.mysabay.sdk.UserProfileQuery;
import com.mysabay.sdk.VerifyTokenQuery;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import kh.com.mysabay.sdk.callback.LoginListener;
import kh.com.mysabay.sdk.callback.PaymentListener;
import kh.com.mysabay.sdk.callback.RefreshTokenListener;
import kh.com.mysabay.sdk.callback.UserInfoListener;
import kh.com.mysabay.sdk.di.BaseAppComponent;
import kh.com.mysabay.sdk.di.DaggerBaseAppComponent;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.NetworkState;
import kh.com.mysabay.sdk.pojo.login.LoginItem;
import kh.com.mysabay.sdk.pojo.login.SubscribeLogin;
import kh.com.mysabay.sdk.pojo.payment.SubscribePayment;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.utils.AppRxSchedulers;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Tan Phirum on 3/11/20
 * Gmail phirumtan@gmail.com
 */
@Singleton
public class MySabaySDK {

    private static final String TAG = MySabaySDK.class.getSimpleName();

    @Inject
    ApolloClient apolloClient;
    @Inject
    Gson gson;
    @Inject
    AppRxSchedulers appRxSchedulers;

    private SharedPreferences mPreferences;
    public BaseAppComponent mComponent;
    public Application mAppContext;

    private static MySabaySDK mySabaySDK;
    private SdkConfiguration mSdkConfiguration;

    private LoginListener loginListner;
    private PaymentListener mPaymentListener;
    private final MediatorLiveData<NetworkState> _networkState;

    @Inject
    public MySabaySDK(Application application, SdkConfiguration configuration) {
        LogUtil.debug(TAG, "init MySabaySDK");
        mySabaySDK = this;
        this.mAppContext = application;
        this.mComponent = DaggerBaseAppComponent.create();
        this._networkState = new MediatorLiveData<>();
        mSdkConfiguration = configuration;
        this.mComponent.inject(this);
        EventBus.getDefault().register(this);
    }

    public static class Impl {
        public static synchronized void setDefaultInstanceConfiguration(Application application, SdkConfiguration configuration) {
            new MySabaySDK(application, configuration);
        }
    }

    @Contract(pure = true)
    public static MySabaySDK getInstance() {
        if (mySabaySDK == null)
            throw new NullPointerException("initialize mysabaySdk in application");
        if (mySabaySDK.mAppContext == null)
            throw new NullPointerException("Please provide application context");
        if (mySabaySDK.mSdkConfiguration == null)
            throw new RuntimeException("This sdk is need SdkConfiguration");
        return mySabaySDK;
    }

    /**
     * Show the login screen
     *
     * @param listener return token when login success, failed message if login failed
     */
    public void showLoginView(LoginListener listener) {
        if (listener != null)
            this.loginListner = listener;
        AppItem item = gson.fromJson(getAppItem(), AppItem.class);
        if (item != null) {
            _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
            apolloClient.query(new VerifyTokenQuery(item.token)).enqueue(new ApolloCall.Callback<VerifyTokenQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<VerifyTokenQuery.Data> response) {
                    apolloClient.mutate(new RefreshTokenMutation(item.refreshToken)).enqueue(new ApolloCall.Callback<RefreshTokenMutation.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<RefreshTokenMutation.Data> response) {
                            item.withToken(response.getData().sso_refreshToken().accessToken());
                            item.withExpired(response.getData().sso_refreshToken().expire());
                            item.withRefreshToken(response.getData().sso_refreshToken().refreshToken());
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                                    MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
                                    EventBus.getDefault().post(new SubscribeLogin(item.token, null));
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                            EventBus.getDefault().post(new SubscribeLogin(null, e));
                            LogUtil.info(TAG, e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LogUtil.info(TAG, "Token is invalid");
                    apolloClient.mutate(new RefreshTokenMutation(item.refreshToken)).enqueue(new ApolloCall.Callback<RefreshTokenMutation.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<RefreshTokenMutation.Data> response) {
                            item.withToken(response.getData().sso_refreshToken().accessToken());
                            item.withExpired(response.getData().sso_refreshToken().expire());
                            item.withRefreshToken(response.getData().sso_refreshToken().refreshToken());
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                                    MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
                                    EventBus.getDefault().post(new SubscribeLogin(item.token, null));
                                }
                            });
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                            LogUtil.info(TAG, e.getMessage());
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mAppContext.startActivity(new Intent(mAppContext, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                }
                            });
                        }
                    });
                }
            });

//            userRepo.getVerifyToken(item.appSecret, item.token).subscribeOn(appRxSchedulers.io())
//                    .observeOn(appRxSchedulers.mainThread())
//                    .subscribe(new AbstractDisposableObs<TokenVerify>(mAppContext, _networkState) {
//                        @Override
//                        protected void onSuccess(TokenVerify tokenVerify) {
//                            LogUtil.info(TAG, "Token is valid");
//                            userRepo.postRefreshToken(item.appSecret,item.refreshToken)
//                                    .subscribeOn(appRxSchedulers.io())
//                                    .observeOn(appRxSchedulers.mainThread()).subscribe(
//                                    new AbstractDisposableObs<RefreshTokenItem>(mAppContext, _networkState, null) {
//                                        @Override
//                                        protected void onSuccess(RefreshTokenItem refreshTokenItem) {
//                                                if (refreshTokenItem.status == 200) {
//                                                    item.withToken(refreshTokenItem.data.accessToken);
//                                                    item.withExpired(refreshTokenItem.data.expire);
//                                                    item.withRefreshToken(refreshTokenItem.data.refreshToken);
//                                                    MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
//                                                    EventBus.getDefault().post(new SubscribeLogin(item.token, null));
//                                                } else
//                                                    onErrors(new Error(gson.toJson(refreshTokenItem)));
//                                        }
//
//                                        @Override
//                                        protected void onErrors(@NotNull Throwable error) {
//                                            LogUtil.info(TAG, error.getMessage());
//                                        }
//                                    });
//                        }
//
//                        @Override
//                        protected void onErrors(Throwable error) {
//                            LogUtil.info(TAG, "Token is invalid");
//                            userRepo.postRefreshToken(item.appSecret,item.refreshToken)
//                                    .subscribeOn(appRxSchedulers.io())
//                                    .observeOn(appRxSchedulers.mainThread()).subscribe(
//                                    new AbstractDisposableObs<RefreshTokenItem>(mAppContext, _networkState, null) {
//                                        @Override
//                                        protected void onSuccess(RefreshTokenItem refreshTokenItem) {
//                                            if (refreshTokenItem.status == 200) {
//                                                item.withToken(refreshTokenItem.data.accessToken);
//                                                item.withExpired(refreshTokenItem.data.expire);
//                                                item.withRefreshToken(refreshTokenItem.data.refreshToken);
//                                                MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
//                                                EventBus.getDefault().post(new SubscribeLogin(item.token, null));
//                                            } else
//                                                onErrors(new Error(gson.toJson(refreshTokenItem)));
//                                        }
//
//                                        @Override
//                                        protected void onErrors(@NotNull Throwable error) {
//                                            LogUtil.info(TAG, error.getMessage());
//                                            mAppContext.startActivity(new Intent(mAppContext, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                                        }
//                                    });
//                        }
//                    });
        } else {
            mAppContext.startActivity(new Intent(mAppContext, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    /**
     * validate if user login
     *
     * @return true has logged , false otherwise
     */
    public boolean isLogIn() {
        return !StringUtils.isBlank(MySabaySDK.getInstance().getAppItem());
    }

    public void logout() {
        AppItem item = gson.fromJson(getAppItem(), AppItem.class);
        if (item != null) {
          logoutWithGraphQl(item.refreshToken);
        }
    }

    public void logoutWithGraphQl(String refreshToken) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
            apolloClient.mutate(new DeleteTokenMutation(refreshToken)).enqueue(new ApolloCall.Callback<DeleteTokenMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<DeleteTokenMutation.Data> response) {
                    LogUtil.info("Success", response.getData().toString());
                    clearAppItem();
                    if (LoginManager.getInstance() != null) {
                        LogUtil.info("Facebook", "Logout");
                        LoginManager.getInstance().logOut();
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LogUtil.info("OnError", e.toString());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                        }
                    });
                }
            });
    }

    /**
     * Get user profile
     *
     * @param listener
     */
    public void getUserProfile(UserInfoListener listener) {
        AppItem item = gson.fromJson(getAppItem(), AppItem.class);

        apolloClient.query(new UserProfileQuery()).enqueue(new ApolloCall.Callback<UserProfileQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserProfileQuery.Data> response) {
                if (response.getData() != null) {
                    if (listener != null) {
                        item.withEnableLocaPay(response.getData().sso_userProfile().localPayEnabled());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
                                listener.userInfo(gson.toJson(response.getData().sso_userProfile()));
                            }
                        });
                    } else {
                        onFailure(new ApolloException("UserInfoListener required!!!"));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                           LogUtil.info("Get User Profile", "Failed");
//                           listener.userInfo("Get User Profile failed");
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                    }
                });
            }
        });
    }

    /**
     * Show the shop item
     *
     * @param listener return with item purchase transaction or failed message
     */
    public void showStoreView(PaymentListener listener) {
        if (listener == null) return;

        this.mPaymentListener = listener;
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        if (appItem == null || StringUtils.isBlank(appItem.token)) {
            MessageUtil.displayToast(mAppContext, "You need to login first");
            return;
        }

//        userRepo.getVerifyToken(appItem.appSecret, appItem.token).subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread())
//                .subscribe(new AbstractDisposableObs<TokenVerify>(mAppContext, _networkState) {
//
//                    @Override
//                    protected void onSuccess(TokenVerify tokenVerify) {
//                        LogUtil.info(TAG, tokenVerify.message);
//                        mAppContext.startActivity(new Intent(mAppContext, StoreActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                    }
//
//                    @Override
//                    protected void onErrors(Throwable error) {
//                        LogUtil.info(TAG, error.getMessage());
//                        MessageUtil.displayToast(mAppContext, "Token is invalid");
//                    }
//                });
    }

    /**
     * @param listener
     */
    public void refreshToken(RefreshTokenListener listener) {
        AppItem item = gson.fromJson(getAppItem(), AppItem.class);

        apolloClient.mutate(new RefreshTokenMutation(item.refreshToken)).enqueue(new ApolloCall.Callback<RefreshTokenMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<RefreshTokenMutation.Data> response) {
                LogUtil.info("Success", response.getData().toString());
                if (listener != null) {
                    item.withToken(response.getData().sso_refreshToken().accessToken());
                    item.withExpired(response.getData().sso_refreshToken().expire());
                    item.withRefreshToken(response.getData().sso_refreshToken().refreshToken());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
                            listener.refreshSuccess(response.getData().sso_refreshToken().refreshToken());
                        }
                    });
                } else {
                    onFailure(new ApolloException("RefreshTokenListener required!!!"));
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                LogUtil.info("OnError", e.toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                        if (listener != null) listener.refreshFailed(e);
                    }
                });
            }
        });

//        userRepo.postRefreshToken(item.appSecret,item.refreshToken)
//                .subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread()).subscribe(
//                        new AbstractDisposableObs<RefreshTokenItem>(mAppContext, _networkState, null) {
//            @Override
//            protected void onSuccess(RefreshTokenItem refreshTokenItem) {
//                if (listener != null) {
//                    if (refreshTokenItem.status == 200) {
//                        item.withToken(refreshTokenItem.data.accessToken);
//                        item.withExpired(refreshTokenItem.data.expire);
//                        item.withRefreshToken(refreshTokenItem.data.refreshToken);
//                        MySabaySDK.getInstance().saveAppItem(gson.toJson(item));
//                        listener.refreshSuccess(refreshTokenItem.data.refreshToken);
//                    } else
//                        onErrors(new Error(gson.toJson(refreshTokenItem)));
//                } else {
//                    onErrors(new NullPointerException("RefreshTokenListener required!!!"));
//                }
//            }
//
//            @Override
//            protected void onErrors(@NotNull Throwable error) {
//                if (listener != null) listener.refreshFailed(error);
//            }
//        });
    }

    /**
     * @return with token that valid
     */
    public String currentToken() {
        AppItem item = gson.fromJson(getAppItem(), AppItem.class);
        return item.token;
    }

    /**
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid() {
        AppItem item = gson.fromJson(getAppItem(), AppItem.class);
        if (System.currentTimeMillis() == item.expire)
            return false;
        else return true;
    }


    @Subscribe
    public void onLoginEvent(SubscribeLogin event) {
        if (loginListner != null) {
            if (!StringUtils.isBlank(event.accessToken)) {
                loginListner.loginSuccess(event.accessToken);
            } else
                loginListner.loginFailed(event.error);
        } else {
            LogUtil.debug(TAG, "loginListerner null " + gson.toJson(event));
        }
    }

    @Subscribe
    public void onPaymentEvent(SubscribePayment event) {
        if (mPaymentListener != null) {
            mPaymentListener.purchaseSuccess(event);
        } else
            LogUtil.debug(TAG, "loginListerner null " + gson.toJson(event));
    }

    public void destroy() {
        EventBus.getDefault().unregister(this);
        loginListner = null;
        mPaymentListener = null;
        mySabaySDK = null;
        mAppContext = null;
    }

    public SharedPreferences getPreferences(Activity context) {
        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(Globals.PREF_NAME, MODE_PRIVATE);
        return mPreferences;
    }

    public SharedPreferences getPreferences() {
        if (mPreferences == null)
            mPreferences = mAppContext.getSharedPreferences(Globals.PREF_NAME, MODE_PRIVATE);
        return mPreferences;
    }

    public void saveAppItem(String item) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(Globals.EXT_KEY_APP_ITEM, item);
        editor.apply();
    }

    public void clearAppItem() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(Globals.EXT_KEY_APP_ITEM);
        editor.commit();
    }

    public String getAppItem() {
        return getPreferences().getString(Globals.EXT_KEY_APP_ITEM, null);
    }

    public void saveMethodSelected(String item) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(Globals.EXT_KEY_PAYMENT_METHOD, item);
        editor.apply();
    }

    public String getMethodSelected() {
        return getPreferences().getString(Globals.EXT_KEY_PAYMENT_METHOD, "");
    }

    public SdkConfiguration getSdkConfiguration() {
        return mSdkConfiguration;
    }

    public void setSdkConfiguration(SdkConfiguration mSdkConfiguration) {
        mSdkConfiguration = mSdkConfiguration;
    }

    public String appSecret() {
        return mSdkConfiguration.isSandBox ? "9c85c50a4362f687cd4507771ba81db5cf50eaa0b3008f4f943f77ba3ac6386b" : "d41faee946f531794d18a152eafeb5fd8fc81ce4de520e97fcfe41fefdd0381c";
    }

    public String userApiUrl() {
        return mSdkConfiguration.isSandBox ? "https://gateway.master.sabay.com/graphql/" : "https://user.mysabay.com/";
    }

    public String storeApiUrl() {
        return mSdkConfiguration.isSandBox ? "https://store.testing.mysabay.com/" : "https://store.mysabay.com/";
    }

    public String serviceCode() {
        return mSdkConfiguration.serviceCode;
    }
}
