package kh.com.mysabay.sdk.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.databinding.FragmentVerifiedBinding;
import kh.com.mysabay.sdk.pojo.login.LoginItem;
import kh.com.mysabay.sdk.receiver.MessageListener;
import kh.com.mysabay.sdk.receiver.SmsBroadcastReceiver;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.utils.KeyboardUtils;
import kh.com.mysabay.sdk.utils.LogUtil;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.utils.SdkTheme;
import kh.com.mysabay.sdk.viewmodel.UserApiVM;

/**
 * Created by Tan Phirum on 3/7/20
 * Gmail phirumtan@gmail.com
 */
public class VerifiedFragment extends BaseFragment<FragmentVerifiedBinding, UserApiVM> {

    public static final String TAG = VerifiedFragment.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private SmsBroadcastReceiver smsBroadcastReceiver;


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
            mViewBinding.btnVerify.setTextColor(textColorCode());
        this.viewModel = LoginActivity.loginActivity.viewModel;
        checkForSmsPermission();

        smsBroadcastReceiver = new SmsBroadcastReceiver();
        getContext().registerReceiver(smsBroadcastReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

    }

    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECEIVE_MMS)) {
                LogUtil.info("AAA", "Permission already dined");
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECEIVE_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }

        } else {
           LogUtil.info("AAA", "Permission already granted");
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
//            Log.i("TAG", "permission granted");
//        } else {
//            Log.i("TAG", "permission denied");
//        }
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Thanks", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_LONG).show();;
                }
            }
            break;
        }
    }

    @Override
    public void assignValues() {
        viewModel.getResponseLogin().observe(this, item -> {
            if (item != null && item.data.verifyCode > 0)
                MessageUtil.displayDialog(getContext(), String.valueOf(item.data.verifyCode));
//                mViewBinding.edtVerifyCode.setText(String.valueOf(item.data.verifyCode));
        });
    }

    @Override
    public void addListeners() {
        mViewBinding.edtVerifyCode.setAnimateText(true);
        mViewBinding.edtVerifyCode.setOnPinEnteredListener(str -> {
            LoginItem item = viewModel.getResponseLogin().getValue();
            if (item == null) return;

            if (Integer.parseInt(str.toString()) == item.data.verifyCode) {
                KeyboardUtils.hideKeyboard(getContext(), mViewBinding.edtVerifyCode);
                viewModel.postToVerified(getContext(), Integer.parseInt(str.toString()));
            } else {
                KeyboardUtils.hideKeyboard(getContext(), mViewBinding.edtVerifyCode);
                //MessageUtil.displayToast(getContext(), "verified failed");
                mViewBinding.edtVerifyCode.setError(true);
                mViewBinding.edtVerifyCode.postDelayed(() ->
                        mViewBinding.edtVerifyCode.setText(null), 1000);
            }
        });

        viewModel.liveNetworkState.observe(this, this::showProgressState);

        mViewBinding.tvResendOtp.setOnClickListener(v -> {
            mViewBinding.edtVerifyCode.setText("");
            viewModel.resendOTP(v.getContext());
        });

        mViewBinding.btnVerify.setOnClickListener(v -> {
            String code = mViewBinding.edtVerifyCode.getText() != null ? mViewBinding.edtVerifyCode.getText().toString() : "";
            if (!StringUtils.isEmpty(code)) {
                KeyboardUtils.hideKeyboard(v.getContext(), v);
                viewModel.postToVerified(v.getContext(), Integer.parseInt(code));
            } else
                MessageUtil.displayToast(v.getContext(), getString(R.string.verify_code_required));
        });

        //
        mViewBinding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
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
