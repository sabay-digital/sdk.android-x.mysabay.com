package kh.com.mysabay.sdk.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mysabay.sdk.DeleteTokenMutation;
import com.mysabay.sdk.LoginWithFacebookMutation;
import com.mysabay.sdk.LoginWithMySabayMutation;
import com.mysabay.sdk.LoginWithPhoneMutation;
import com.mysabay.sdk.UserProfileQuery;
import com.mysabay.sdk.VerifyMySabayMutation;
import com.mysabay.sdk.VerifyOtpCodMutation;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.SdkConfiguration;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.NetworkState;
import kh.com.mysabay.sdk.pojo.login.LoginItem;
import kh.com.mysabay.sdk.pojo.login.LoginResponseItem;
import kh.com.mysabay.sdk.pojo.login.SubscribeLogin;
import kh.com.mysabay.sdk.pojo.logout.LogoutResponseItem;
import kh.com.mysabay.sdk.pojo.profile.UserProfileItem;
import kh.com.mysabay.sdk.repository.UserRepo;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.ui.fragment.MySabayLoginFm;
import kh.com.mysabay.sdk.ui.fragment.VerifiedFragment;
import kh.com.mysabay.sdk.utils.AppRxSchedulers;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.webservice.AbstractDisposableObs;

/**
 * Created by Tan Phirum on 3/8/20
 * Gmail phirumtan@gmail.com
 */
public class UserApiVM extends ViewModel {

    private static final String TAG = UserApiVM.class.getSimpleName();

//    private final UserRepo userRepo;

    ApolloClient apolloClient;

    private final AppRxSchedulers appRxSchedulers;
    @Inject
    Gson gson;

    private final MediatorLiveData<String> _msgError = new MediatorLiveData<>();
    private final MediatorLiveData<NetworkState> _networkState;
    private final MediatorLiveData<String> _loginMySabay;
    private MediatorLiveData<LoginItem> _responseLogin;
    public LiveData<NetworkState> liveNetworkState;
    private final MediatorLiveData<String> _login;
    public LiveData<String> login;
    public LiveData<String> loginMySabay;
    private final SdkConfiguration sdkConfiguration;

    public CompositeDisposable mCompositeDisposable;

    @Inject
    public UserApiVM(ApolloClient apolloClient, AppRxSchedulers appRxSchedulers) {
//        this.userRepo = userRepo;
        this.apolloClient = apolloClient;
        this.appRxSchedulers = appRxSchedulers;
        this._networkState = new MediatorLiveData<>();
        this._responseLogin = new MediatorLiveData<>();
        this._loginMySabay = new MediatorLiveData<>();
        this.liveNetworkState = _networkState;
        this._login = new MediatorLiveData<>();
        this.login = _login;
        this.loginMySabay = _loginMySabay;
        this.mCompositeDisposable = new CompositeDisposable();
        this.sdkConfiguration = MySabaySDK.getInstance().getSdkConfiguration();
    }

    public void setLoginItemData(LoginItem item) {
        _responseLogin.setValue(item);
    }

    public LiveData<LoginItem> getResponseLogin() {
        return _responseLogin;
    }

    public void postToLoginWithGraphql(Context context, String appSecret, String phone, String dialCode) {
        _login.setValue(phone);

        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        LoginWithPhoneMutation loginWithPhoneMutation = new LoginWithPhoneMutation(dialCode + phone);
        apolloClient.mutate(loginWithPhoneMutation).enqueue(new ApolloCall.Callback<LoginWithPhoneMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithPhoneMutation.Data> response) {
                if (response.getData() != null) {
                    LoginItem item = new LoginItem();
                    item.withPhone(dialCode + phone);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayToast(context, "Otp code has sent to " + dialCode + phone);
                            setLoginItemData(item);
                        }
                    });
                    if (context instanceof LoginActivity)
                        ((LoginActivity) context).initAddFragment(new VerifiedFragment(), VerifiedFragment.TAG, true);
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayToast(context, "Login with phonnumber failed");
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
                        MessageUtil.displayDialog(context, "Login Error! Please try again");
                    }
                });
            }
        });
    }

    public void verifyOTPWithGraphql(Context context, int code) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        LoginItem item = getResponseLogin().getValue();
        if (item == null) {
            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR, "Something went wrong, please login again"));
            return;
        }

        apolloClient.mutate(new VerifyOtpCodMutation(item.phone, Integer.toString(code))).enqueue(new ApolloCall.Callback<VerifyOtpCodMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<VerifyOtpCodMutation.Data> response) {
                if (response.getData() != null) {

                    AppItem appItem = new AppItem(item.appSecret, response.getData().sso_verifyOTP().accessToken(), response.getData().sso_verifyOTP().refreshToken(), null, response.getData().sso_verifyOTP().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_verifyOTP().accessToken(), null));
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                           MessageUtil.displayDialog(context, "OTP Verification failed");
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                LogUtil.info("OnError", e.toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                        EventBus.getDefault().post(new SubscribeLogin("", e));
                        LogUtil.error("verify code response with status", e.getLocalizedMessage());
                        MessageUtil.displayDialog(context, e.getMessage());
                    }
                });
            }
        });
    }

    public void postToLoginFacebookWithGraphql(@NotNull Activity context, String token) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new LoginWithFacebookMutation(token)).enqueue(new ApolloCall.Callback<LoginWithFacebookMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithFacebookMutation.Data> response) {
                LogUtil.info("Success", response.getData().toString());
                AppItem appItem = new AppItem(response.getData().sso_loginFacebook().accessToken(), response.getData().sso_loginFacebook().refreshToken(), response.getData().sso_loginFacebook().expire());
                String encrypted = gson.toJson(appItem);
                MySabaySDK.getInstance().saveAppItem(encrypted);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                        EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_loginFacebook().accessToken(), null));
                        LoginActivity.loginActivity.finish();
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
                        EventBus.getDefault().post(new SubscribeLogin("", e));
                        LogUtil.error("Login with facebook", e.getLocalizedMessage());
                        MessageUtil.displayDialog(context, "Login with facebook is error");
                    }
                });
            }
        });
    }

    public void postToLoginMySabayWithGraphql(Context context, String username, String password) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new LoginWithMySabayMutation(username, password)).enqueue(new ApolloCall.Callback<LoginWithMySabayMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithMySabayMutation.Data> response) {
                if (response.getData() != null) {
                    AppItem appItem = new AppItem(response.getData().sso_loginMySabay().accessToken(), response.getData().sso_loginMySabay().refreshToken(), response.getData().sso_loginMySabay().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_loginMySabay().accessToken(), null));
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, "MySabay Login failed");
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                LogUtil.info("OnError", e.toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                        EventBus.getDefault().post(new SubscribeLogin("", e));
                        MessageUtil.displayDialog(context, "Login with MySabay is error");
                    }
                });
            }
        });
    }
    public void postToVerifyMySabayWithGraphql(Context context, String username, String password) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new VerifyMySabayMutation(username, password)).enqueue(new ApolloCall.Callback<VerifyMySabayMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<VerifyMySabayMutation.Data> response) {
                LogUtil.info("Verify with MySabay Success", response.getData().toString());
                if (response.getData() != null) {
                    AppItem appItem = new AppItem(response.getData().sso_verifyMySabay().accessToken(), response.getData().sso_verifyMySabay().refreshToken(), response.getData().sso_verifyMySabay().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_verifyMySabay().accessToken(), null));
                            ((LoginActivity) context).initAddFragment(new VerifiedFragment(), VerifiedFragment.TAG, true);
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, "Verify Mysabay account failed");
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                LogUtil.info("OnError", e.toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                        EventBus.getDefault().post(new SubscribeLogin("", e));
                        MessageUtil.displayDialog(context, "Login with MySabay is error");
                    }
                });
            }
        });
    }

    public void resendOTPWithGraphQL(Context context) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        LoginItem item = getResponseLogin().getValue();
        apolloClient.mutate(new LoginWithPhoneMutation(item.phone)).enqueue(new ApolloCall.Callback<LoginWithPhoneMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithPhoneMutation.Data> response) {
                if (response.getData() != null) {
                    item.withPhone(item.phone);
                    item.withExpire(response.getData().sso_loginPhone().otpExpiry());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _responseLogin.setValue(item);
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayToast(context, "Otp code has sent to " + item.phone);
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
                        MessageUtil.displayDialog(context, "Send otp code Error! Please try again");
                    }
                });
            }
        });
    }

    public void postToGetUserProfileWithGraphQL(@NotNull Activity context, String token) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        apolloClient.query(new UserProfileQuery()).enqueue(new ApolloCall.Callback<UserProfileQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserProfileQuery.Data> response) {
                if (response.getData() != null) {
                    EventBus.getDefault().post(new SubscribeLogin(token, null));
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            MySabaySDK.getInstance().saveAppItem(gson.toJson(appItem));
                            context.runOnUiThread(context::finish);

                        }
                    });
                } else {
                    EventBus.getDefault().post(new SubscribeLogin("", response.getData()));
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                        EventBus.getDefault().post(new SubscribeLogin("", e));
                        MessageUtil.displayDialog(context, "Get user profile failed");
                    }
                });
            }
        });
    }


//    public void postToLogin(Context context, String appSecret, String phone, String dialCode) {
//        _login.setValue(phone);
//        this.userRepo.getUserLogin(appSecret, dialCode + phone ).subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread())
//                .subscribe(new AbstractDisposableObs<LoginItem>(context, _networkState, null) {
//                    @Override
//                    protected void onSuccess(LoginItem item) {
////                        if (item.status == 200) {
////                            item.data.withPhone(dialCode + phone);
////                            item.data.withAppSecret(appSecret);
////                            _responseLogin.setValue(item);
////
////                            MessageUtil.displayToast(context, item.data.message);
////                            if (context instanceof LoginActivity)
////                                ((LoginActivity) context).initAddFragment(new VerifiedFragment(), VerifiedFragment.TAG, true);
//////                                ((LoginActivity) context).initAddFragment(new MySabayLoginConfirmFragment(), MySabayLoginConfirmFragment.TAG, true);
////                        }
//                    }
//
//                    @Override
//                    protected void onErrors(Throwable error) {
//                        LogUtil.error(TAG, error.getLocalizedMessage());
//                    }
//                });
//    }
//
//    public void resendOTP(Context context) {
//        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
//        LoginItem item = getResponseLogin().getValue();
//        if (item == null) {
//            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR, "Something went wrong, please login again"));
//            return;
//        }
//        this.userRepo.getUserLogin(item.appSecret, item.phone).subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread())
//                .subscribe(new AbstractDisposableObs<LoginItem>(context, _networkState) {
//                    @Override
//                    protected void onSuccess(LoginItem item1) {
////                        if (item1.status == 200) {
////                            item1.data.withPhone(item.data.phone);
////                            item1.data.withAppSecret(item.data.appSecret);
////                            _responseLogin.setValue(item1);
////                            MessageUtil.displayToast(context, item1.data.message);
////                        }
//                    }
//
//                    @Override
//                    protected void onErrors(Throwable error) {
//                        LogUtil.error(TAG, error.getLocalizedMessage());
//                    }
//                });
//    }
//
//    public void postToVerified(Context context, int code) {
//        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
//        LoginItem item = getResponseLogin().getValue();
//        if (item == null) {
//            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR, "Something went wrong, please login again"));
//            return;
//        }
//        mCompositeDisposable.add(this.userRepo.postVerifyCode(item.appSecret, item.phone, code).subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread()).subscribe(response -> {
//                    if (response.status == 200) {
//                        LogUtil.info("refresh-Token", response.data.refreshToken);
//                        if (response.data != null) {
//                            AppItem appItem = new AppItem(item.appSecret, response.data.accessToken, response.data.refreshToken, response.data.uuid, response.data.expire);
//                            String encrypted = gson.toJson(appItem);
//                            MySabaySDK.getInstance().saveAppItem(encrypted);
//                            MessageUtil.displayToast(context, "verified code success");
//
//                            EventBus.getDefault().post(new SubscribeLogin(response.data.accessToken, null));
//
//                            LoginActivity.loginActivity.finish();
//                        } else {
//                            EventBus.getDefault().post(new SubscribeLogin("", response.data));
//                            LogUtil.error(TAG, "verified data is null");
//                            MessageUtil.displayDialog(context, "verified data is null");
//                        }
//                    } else {
//                        EventBus.getDefault().post(new SubscribeLogin("", response.data));
//                        JsonParser parser = new JsonParser();
//                        JsonObject obj = parser.parse(response.toString()).getAsJsonObject();
//                        String errMsg = obj.get("message").getAsString();
//                        MessageUtil.displayDialog(context, errMsg);
//                    }
//
//                }, throwable -> {
//                    _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
//                    EventBus.getDefault().post(new SubscribeLogin("", throwable));
//                    LogUtil.error("verify code response with status", throwable.getLocalizedMessage());
//                    MessageUtil.displayDialog(context, "Verify Code is not match");
//                }));
//    }
//
//    public void postToLoginWithMySabay(Context context, String appSecret) {
//        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
//        mCompositeDisposable.add(this.userRepo.postLoginWithMySabay(appSecret).subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String o) throws Exception {
//                        _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
//                        _loginMySabay.setValue(o);
//                        if (context instanceof LoginActivity)
//                            ((LoginActivity) context).initAddFragment(new MySabayLoginFm(), MySabayLoginFm.TAG, true);
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) {
//                        _networkState.setValue(new NetworkState(NetworkState.Status.ERROR, throwable.getLocalizedMessage()));
//                    }
//                }));
//    }
//
//    public void postToGetUserProfile(@NotNull Activity context, String token) {
//        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
//        this.userRepo.getUserProfile(MySabaySDK.getInstance().appSecret(), token)
//                .subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread())
//                .subscribe(new AbstractDisposableObs<UserProfileItem>(context, _networkState) {
//                    @Override
//                    protected void onSuccess(UserProfileItem userProfileItem) {
//                        if (userProfileItem.data != null) {
//                            EventBus.getDefault().post(new SubscribeLogin(token, null));
//                            appItem.withUuid(userProfileItem.data.uuid);
//                            MySabaySDK.getInstance().saveAppItem(gson.toJson(appItem));
//                            context.runOnUiThread(context::finish);
//                        } else
//                            EventBus.getDefault().post(new SubscribeLogin("", userProfileItem.data));
//                    }
//
//                    @Override
//                    protected void onErrors(Throwable error) {
//                        EventBus.getDefault().post(new SubscribeLogin("", error));
//                    }
//                });
//    }
//
//    public void postToLoginWithFacebook(@NotNull Activity context, String token) {
//        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
//        userRepo.loginWithFacebook("aog", token)
//                .subscribeOn(appRxSchedulers.io())
//                .observeOn(appRxSchedulers.mainThread()).subscribe(
//                new AbstractDisposableObs<LoginResponseItem>(context, _networkState, null) {
//                    @Override
//                    protected void onSuccess(LoginResponseItem refreshTokenItem) {
//                        LogUtil.info("onSuccess", refreshTokenItem.accessToken);
//
//                        AppItem appItem = new AppItem(null, refreshTokenItem.accessToken, refreshTokenItem.refreshToken, null, refreshTokenItem.expire);
//                        String encrypted = gson.toJson(appItem);
//                        MySabaySDK.getInstance().saveAppItem(encrypted);
//
//                        MessageUtil.displayToast(context, "verified code success");
//                        EventBus.getDefault().post(new SubscribeLogin(refreshTokenItem.accessToken, null));
//
//                        LoginActivity.loginActivity.finish();
//                    }
//
//                    @Override
//                    protected void onErrors(@NotNull Throwable error) {
//                        LogUtil.info("onErrors", error.toString());
//                    }
//                });
//
//    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtil.debug(TAG, "onClearerd call");
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }
}