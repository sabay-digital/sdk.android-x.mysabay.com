package kh.com.mysabay.sdk.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.databinding.FmLoginMysabayBinding;
import kh.com.mysabay.sdk.pojo.NetworkState;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.viewmodel.UserApiVM;

public class MySabayLoginFragment extends BaseFragment<FmLoginMysabayBinding, UserApiVM> {

    public static final String TAG = MySabayLoginFragment.class.getSimpleName();

    @NotNull
    @Contract(" -> new")
    public static MySabayLoginFragment newInstance() {
        return new MySabayLoginFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fm_login_mysabay;
    }

    @Override
    public void initializeObjects(View v, Bundle args) {
        this.viewModel = LoginActivity.loginActivity.viewModel;
    }

    @Override
    public void assignValues() {
        new Handler().postDelayed(() -> showProgressState(new NetworkState(NetworkState.Status.SUCCESS)), 500);
    }

    @Override
    public void addListeners() {
        mViewBinding.btnClose.setOnClickListener(v -> {
            if (v.getContext() instanceof LoginActivity)
                ((LoginActivity) v.getContext()).onBackPressed();
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
        return null;
    }

    @Override
    protected void onOnlineCallback() {

    }
}
