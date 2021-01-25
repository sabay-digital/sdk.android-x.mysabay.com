package kh.com.mysabay.sdk.viewmodel;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.exception.ApolloNetworkException;
import com.apollographql.apollo.request.RequestHeaders;
import com.google.gson.Gson;
import com.mysabay.sdk.CheckExistingLoginQuery;
import com.mysabay.sdk.CreateMySabayLoginMutation;
import com.mysabay.sdk.CreateMySabayLoginWithPhoneMutation;
import com.mysabay.sdk.LoginWithFacebookMutation;
import com.mysabay.sdk.LoginWithMySabayMutation;
import com.mysabay.sdk.LoginWithPhoneMutation;
import com.mysabay.sdk.SendCreateMySabayWithPhoneOTPMutation;
import com.mysabay.sdk.UserProfileQuery;
import com.mysabay.sdk.VerifyMySabayMutation;
import com.mysabay.sdk.VerifyOtpCodMutation;
import com.mysabay.sdk.type.Sso_LoginProviders;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.SdkConfiguration;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.NetworkState;
import kh.com.mysabay.sdk.pojo.login.LoginItem;
import kh.com.mysabay.sdk.pojo.login.SubscribeLogin;
import kh.com.mysabay.sdk.pojo.mysabay.MySabayAccount;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.ui.fragment.MySabayLoginConfirmFragment;
import kh.com.mysabay.sdk.ui.fragment.VerifiedFragment;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.utils.RSA;
import kh.com.mysabay.sdk.webservice.Constant;

/**
 * Created by Tan Phirum on 3/8/20
 * Gmail phirumtan@gmail.com
 */
public class UserApiVM extends ViewModel {

    private static final String TAG = UserApiVM.class.getSimpleName();

    ApolloClient apolloClient;

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
    public UserApiVM(ApolloClient apolloClient) {
        this.apolloClient = apolloClient;
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

    public void loginWithPhoneNumber(Context context, String phone, String dialCode) {
        _login.setValue(phone);

        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        LoginWithPhoneMutation loginWithPhoneMutation = new LoginWithPhoneMutation(dialCode + phone);
        apolloClient.mutate(loginWithPhoneMutation).enqueue(new ApolloCall.Callback<LoginWithPhoneMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithPhoneMutation.Data> response) {
                if (response.getData() != null) {
                    LoginItem item = new LoginItem();
                    item.withPhone(dialCode + phone);
                    item.withExpire(response.getData().sso_loginPhone().otpExpiry());
                    item.withVerifyMySabay(response.getData().sso_loginPhone().verifyMySabay());
                    item.withMySabayUserName(response.getData().sso_loginPhone().mySabayUsername());
                    if (response.getData().sso_loginPhone().verifyMySabay()) {
                        if (context instanceof LoginActivity) {
                            ((LoginActivity) context).initAddFragment(new MySabayLoginConfirmFragment(), MySabayLoginConfirmFragment.TAG, true);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                                    setLoginItemData(item);
                                    MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-phone-number-success");
                                }
                            });
                        }
                    } else {
                        if (context instanceof LoginActivity) {
                            ((LoginActivity) context).initAddFragment(new VerifiedFragment(), VerifiedFragment.TAG, true);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                                    MessageUtil.displayToast(context, "Otp code has sent to " + dialCode + phone);
                                    setLoginItemData(item);
                                }
                            });
                        }

                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayToast(context, "Login with phone number failed");
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-phone-number-failed");
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                LogUtil.info("err", e.getMessage());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof ApolloNetworkException) {
                            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR, context.getString(R.string.msg_can_not_connect_internet)));
                        } else {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, "Login Error! Please try again");
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-phone-number-failed");
                        }
                    }
                });
            }
        });
    }

    public void verifyOTPCode(Context context, int code) {
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

                    AppItem appItem = new AppItem(item.mySabayUsername, item.verifyMySabay, response.getData().sso_verifyOTP().accessToken(), response.getData().sso_verifyOTP().refreshToken(), response.getData().sso_verifyOTP().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-otp-success");
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_verifyOTP().accessToken(), null));
                            getUserProfile(context);
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, "OTP Verification failed");
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-otp-failed");
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
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-otp-failed");
                    }
                });
            }
        });
    }

    public void loginWithFacebook(@NotNull Activity context, String token) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new LoginWithFacebookMutation(token)).enqueue(new ApolloCall.Callback<LoginWithFacebookMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithFacebookMutation.Data> response) {
                if (response.getData() != null) {
                AppItem appItem = new AppItem(response.getData().sso_loginFacebook().accessToken(), response.getData().sso_loginFacebook().refreshToken(), response.getData().sso_loginFacebook().expire());
                String encrypted = gson.toJson(appItem);
                MySabaySDK.getInstance().saveAppItem(encrypted);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-facebook-success");
                        EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_loginFacebook().accessToken(), null));
                        getUserProfile(context);
                        LoginActivity.loginActivity.finish();
                    }
                });

                } else {
                    MessageUtil.displayDialog(context, "Login with facebook failed");
                    MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-facebook-failed");
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
                        LogUtil.error("Login with facebook", e.getLocalizedMessage());
                        MessageUtil.displayDialog(context, "Login with facebook failed");
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-facebook-success-failed");
                    }
                });
            }
        });
    }

    public void loginWithMySabayAccount(Context context, String username, String password) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new LoginWithMySabayMutation(username, RSA.sha256String(password))).enqueue(new ApolloCall.Callback<LoginWithMySabayMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LoginWithMySabayMutation.Data> response) {
                if (response.getData() != null) {
                    AppItem appItem = new AppItem(response.getData().sso_loginMySabay().accessToken(), response.getData().sso_loginMySabay().refreshToken(), response.getData().sso_loginMySabay().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-mysabay-success");
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_loginMySabay().accessToken(), null));
                            getUserProfile(context);
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, context.getString(R.string.msg_can_not_connect_server));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-mysabay-failed");
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
                        MessageUtil.displayDialog(context, "Login with MySabay Failed");
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "login-with-mysabay-failed");
                    }
                });
            }
        });
    }


    public void verifyMySabayAccount(Context context, String username, String password) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new VerifyMySabayMutation(username, RSA.sha256String(password))).enqueue(new ApolloCall.Callback<VerifyMySabayMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<VerifyMySabayMutation.Data> response) {
                if (response.getData() != null) {
                    AppItem appItem = new AppItem(response.getData().sso_verifyMySabay().accessToken(), response.getData().sso_verifyMySabay().refreshToken(), response.getData().sso_verifyMySabay().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-mysabay-success");
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_verifyMySabay().accessToken(), null));
                            getUserProfile(context);
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, context.getString(R.string.msg_can_not_connect_server));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-mysabay-failed");
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
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-mysabay-failed");
                    }
                });
            }
        });
    }

    public void resendOTP(Context context) {
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
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "resent-otp-success");
                            MessageUtil.displayToast(context, "Otp code has sent to " + item.phone);
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                            MessageUtil.displayDialog(context, context.getString(R.string.msg_can_not_connect_server));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "resent-otp-failed");
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
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "resent-otp-failed");
                    }
                });
            }
        });
    }

    public void getUserProfile(@NotNull Activity context, String token) {
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

    public void createMySabayAccount(Context context, String username, String password) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        apolloClient.mutate(new CreateMySabayLoginMutation(username, RSA.sha256String(password))).enqueue(new ApolloCall.Callback<CreateMySabayLoginMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateMySabayLoginMutation.Data> response) {
                if (response.getData() != null) {
                    AppItem appItem = new AppItem(response.getData().sso_createMySabayLogin().accessToken(), response.getData().sso_createMySabayLogin().refreshToken(), response.getData().sso_createMySabayLogin().expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "register-mysabay-success");
                            getUserProfile(context);
                            EventBus.getDefault().post(new SubscribeLogin(response.getData().sso_createMySabayLogin().accessToken(), null));
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayToast(context, "Create mysabay account failed");
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "register-mysabay-failed");
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
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "register-mysabay-failed");
                    }
                });
            }
        });
    }

    public ApolloQueryCall<CheckExistingLoginQuery.Data> checkExistingLogin( String login) {
        return apolloClient.query(new CheckExistingLoginQuery(login, Sso_LoginProviders.SABAY));
    }

    public void createMySabayWithPhoneOTP(Context context, String username, String password) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        LoginItem item = getResponseLogin().getValue();
        apolloClient.mutate(new SendCreateMySabayWithPhoneOTPMutation(item.phone)).enqueue(new ApolloCall.Callback<SendCreateMySabayWithPhoneOTPMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<SendCreateMySabayWithPhoneOTPMutation.Data> response) {
                if (response.getData() != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "create-mysabay-with-otp-success");
                            MySabayAccount mySabayAccount = new MySabayAccount(username, password, item.phone);
                            ((LoginActivity) context).initAddFragment(VerifiedFragment.newInstance(mySabayAccount), VerifiedFragment.TAG, true);
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                            MessageUtil.displayDialog(context, context.getString(R.string.msg_can_not_connect_server));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "create-mysabay-with-otp-failed");
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
                        MessageUtil.displayDialog(context, "Create mysabay account with otp failed");
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "create-mysabay-with-otp-failed");
                    }
                });
            }
        });
    }

    public void createMySabayLoginWithPhone(Context context, String username, String password, String phoneNumber, String otpCode) {
        _networkState.setValue(new NetworkState(NetworkState.Status.LOADING));
        LoginItem item = getResponseLogin().getValue();

        apolloClient.mutate(new CreateMySabayLoginWithPhoneMutation(username, RSA.sha256String(password), phoneNumber, otpCode)).enqueue(new ApolloCall.Callback<CreateMySabayLoginWithPhoneMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateMySabayLoginWithPhoneMutation.Data> response) {
                if (response.getData() != null) {
                    CreateMySabayLoginWithPhoneMutation.Sso_createMySabayLoginWithPhone data = response.getData().sso_createMySabayLoginWithPhone();
                    AppItem appItem = new AppItem(item.mySabayUsername, item.verifyMySabay, data.accessToken(), data.refreshToken(), data.expire());
                    String encrypted = gson.toJson(appItem);
                    MySabaySDK.getInstance().saveAppItem(encrypted);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-otp-success");
                            getUserProfile(context);
                            EventBus.getDefault().post(new SubscribeLogin(data.accessToken(), null));
                            LoginActivity.loginActivity.finish();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                            MessageUtil.displayDialog(context, "OTP Verification failed");
                            MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-otp-failed");
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
                        MySabaySDK.getInstance().trackEvents(context, "sdk-" + Constant.sso, Constant.process, "verify-otp-failed");
                    }
                });
            }
        });
    }

    public void getUserProfile(Context context) {
        AppItem appItem = gson.fromJson(MySabaySDK.getInstance().getAppItem(), AppItem.class);
        apolloClient.query(new UserProfileQuery()).toBuilder()
                .requestHeaders(RequestHeaders.builder()
                        .addHeader("Authorization", "Bearer " + appItem.token).build())
                .build()
                .enqueue(new ApolloCall.Callback<UserProfileQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<UserProfileQuery.Data> response) {
                        if (response.getData() != null) {
                            appItem.withEnableLocaPay(response.getData().sso_userProfile().localPayEnabled());
                            appItem.withMySabayUserId(response.getData().sso_userProfile().userID());
                            appItem.withMySabayUsername(response.getData().sso_userProfile().profileName());
                            appItem.withUuid(response.getData().sso_userProfile().persona().uuid());
                            MySabaySDK.getInstance().setCustomUserId(context, response.getData().sso_userProfile().persona().uuid());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MySabaySDK.getInstance().saveAppItem(gson.toJson(appItem));
                                        _networkState.setValue(new NetworkState(NetworkState.Status.SUCCESS));
                                    }
                                });
                        } else {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    MessageUtil.displayDialog(context, "Get UserProfile Failed");
                                    _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LogUtil.info("Error", e.getMessage());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                _networkState.setValue(new NetworkState(NetworkState.Status.ERROR));
                                MessageUtil.displayDialog(context, "Get UserProfile Failed");
                            }
                        });
                    }
                });
    }

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
