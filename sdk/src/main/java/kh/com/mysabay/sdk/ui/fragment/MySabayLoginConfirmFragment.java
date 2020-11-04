package kh.com.mysabay.sdk.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import kh.com.mysabay.sdk.Globals;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.databinding.FmConfrimLoginMysabayBinding;
import kh.com.mysabay.sdk.pojo.NetworkState;
import kh.com.mysabay.sdk.ui.activity.LoginActivity;
import kh.com.mysabay.sdk.viewmodel.UserApiVM;

public class MySabayLoginConfirmFragment extends BaseFragment<FmConfrimLoginMysabayBinding, UserApiVM> {

    public static final String TAG = MySabayLoginConfirmFragment.class.getSimpleName();

    private FragmentManager mManager;

    @NotNull
    @Contract(" -> new")
    public static MySabayLoginConfirmFragment newInstance() {
        return new MySabayLoginConfirmFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fm_confrim_login_mysabay;
    }

    @Override
    public void initializeObjects(View v, Bundle args) {
        this.viewModel = LoginActivity.loginActivity.viewModel;
        mManager = getFragmentManager();
    }

    @Override
    public void assignValues() {
        new Handler().postDelayed(() -> showProgressState(new NetworkState(NetworkState.Status.SUCCESS)), 500);
    }

    @Override
    public void addListeners() {
        mViewBinding.btnConfirmMysabay.setOnClickListener(v -> {
            initAddFragment(new VerifiedFragment(), VerifiedFragment.TAG, true);
        });
        mViewBinding.btnClose.setOnClickListener(v -> {
            if (v.getContext() instanceof LoginActivity)
                ((LoginActivity) v.getContext()).finish();
        });
    }

    public void initAddFragment(Fragment f, String tag) {
        initAddFragment(f, tag, false);
    }

    public void initAddFragment(Fragment f, String tag, boolean isBack) {
        Globals.initAddFragment(mManager, f, tag, isBack);
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
