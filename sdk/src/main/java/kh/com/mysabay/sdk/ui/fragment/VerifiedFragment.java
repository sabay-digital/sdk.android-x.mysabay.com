package kh.com.mysabay.sdk.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.databinding.FragmentVerifiedBinding;
import kh.com.mysabay.sdk.pojo.login.LoginItem;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.utils.KeyboardUtils;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.utils.SdkTheme;
import kh.com.mysabay.sdk.viewmodel.UserApiVM;
import kh.com.mysabay.sdk.webservice.Constant;

/**
 * Created by Tan Phirum on 3/7/20
 * Gmail phirumtan@gmail.com
 */
public class VerifiedFragment extends BaseFragment<FragmentVerifiedBinding, UserApiVM> {

    public static final String TAG = VerifiedFragment.class.getSimpleName();
    private static final long START_TIME_IN_MILLIS = 60000;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;

    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private long mEndTime;

    public VerifiedFragment() {
        super();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_verified;
    }

    @Override
    public void initializeObjects(View v, Bundle args) {
        mViewBinding.viewMainVerified.setBackgroundResource(colorCodeBackground());
        mViewBinding.btnBack.setBackgroundResource(colorCodeBackground());
        if (MySabaySDK.getInstance().getSdkConfiguration().sdkTheme == SdkTheme.Light)
            mViewBinding.tvResendOtp.setTextColor(getResources().getColor(R.color.colorWhite700));
           this.viewModel = LoginActivity.loginActivity.viewModel;

        MySabaySDK.getInstance().trackPageView(getActivity(), "/sdk/otp-screen", "/sdk/otp-screen");
    }

    @Override
    public void assignValues() {
        viewModel.getResponseLogin().observe(this, item -> {
            if (item != null && item.verifyCode > 0)
                MessageUtil.displayDialog(getContext(), String.valueOf(item.verifyCode), colorCodeBackground());
//                mViewBinding.edtVerifyCode.setText(String.valueOf(item.data.verifyCode));
        });
        startTimer();
    }

    @Override
    public void addListeners() {
        mViewBinding.edtVerifyCode.setAnimateText(true);
        mViewBinding.edtVerifyCode.setOnPinEnteredListener(str -> {
            LoginItem item = viewModel.getResponseLogin().getValue();

            if (item == null) return;

            if (Integer.parseInt(str.toString()) != 0) {
                if (MySabaySDK.getInstance().getSdkConfiguration().isSandBox) {
                    if (Integer.parseInt(str.toString()) != 0) {
                        KeyboardUtils.hideKeyboard(getContext(), mViewBinding.edtVerifyCode);
                        viewModel.verifyOTPWithGraphql(getContext(), Integer.parseInt(str.toString()));
                    } else {
                        KeyboardUtils.hideKeyboard(getContext(), mViewBinding.edtVerifyCode);
                        mViewBinding.edtVerifyCode.setError(true);
                        mViewBinding.edtVerifyCode.postDelayed(() ->
                                mViewBinding.edtVerifyCode.setText(null), 1000);
                    }
                } else {
                    MySabaySDK.getInstance().trackEvents(getActivity(),"sdk-" + Constant.sso, Constant.tab, "verify-otp");
                    viewModel.verifyOTPWithGraphql(getContext(), Integer.parseInt(str.toString()));
                }
            }
        });

        viewModel.liveNetworkState.observe(this, this::showProgressState);

        mViewBinding.tvResendOtp.setOnClickListener(v -> {
            MySabaySDK.getInstance().trackEvents(getActivity(),"sdk-" + Constant.sso, Constant.tab, "resend-otp");
            mViewBinding.edtVerifyCode.setText("");
            viewModel.resendOTPWithGraphQL(v.getContext());
            mTimeLeftInMillis = START_TIME_IN_MILLIS;
            startTimer();

        });

        mViewBinding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });
    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;

                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateButtons();
            }
        }.start();
        mTimerRunning = true;
        updateButtons();
    }

    private void updateCountDownText() {
        if (mTimerRunning) {
            mViewBinding.tvTimer.setText("Didn’t get OTP code? request again in: " + mTimeLeftInMillis / 1000 + "s");
        } else {
            mViewBinding.tvTimer.setText("Didn’t get OTP code? request again in ");
        }
    }

    private void updateButtons() {
        if (mTimerRunning) {
            mViewBinding.tvResendOtp.setEnabled(false);
            mViewBinding.tvResendOtp.setTextColor(0xFF3a3a3c);
        } else {
            mViewBinding.tvResendOtp.setEnabled(true);
            mViewBinding.tvResendOtp.setTextColor(0xFFE3B852);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTimeLeftInMillis = savedInstanceState.getLong("millisLeft");
            mEndTime = savedInstanceState.getLong("endTime");
            updateCountDownText();
            updateButtons();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", mTimeLeftInMillis);
        outState.putLong("endTime", mEndTime);
    }

    @Override
    public void onStop() {
        super.onStop();
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
    protected Class<UserApiVM> getViewModel() {
        return UserApiVM.class;
    }

    @Override
    protected void onOnlineCallback() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((LoginActivity) context).userComponent.inject(this);
        // Now you can access loginViewModel here and onCreateView too
        // (shared instance with the Activity and the other Fragment)
    }

}
