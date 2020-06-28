package kh.com.mysabay.sdk.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.view.View;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import kh.com.mysabay.sdk.MySabaySDK;
import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.adapter.ShopAdapter;
import kh.com.mysabay.sdk.base.BaseFragment;
import kh.com.mysabay.sdk.databinding.FmShopBinding;
import kh.com.mysabay.sdk.pojo.AppItem;
import kh.com.mysabay.sdk.pojo.profile.UserProfileItem;
import kh.com.mysabay.sdk.pojo.shop.Data;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.utils.MessageUtil;
import kh.com.mysabay.sdk.viewmodel.StoreApiVM;

/**
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class ShopsFragment extends BaseFragment<FmShopBinding, StoreApiVM> {

    public static final String TAG = ShopsFragment.class.getSimpleName();

    private ShopAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private String mySabayId;

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

        mAdapter = new ShopAdapter(v.getContext());
        mAdapter.setHasStableIds(true);
        mLayoutManager = new GridLayoutManager(v.getContext(), getResources().getInteger(R.integer.layout_size));
        mViewBinding.rcv.setLayoutManager(mLayoutManager);
        mViewBinding.rcv.setAdapter(mAdapter);

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
                if (StringUtils.equalsIgnoreCase(ob.cashierName, Data.PLAY_STORE))
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
            if (userProfile.data.balance > 0) {
                String sabayCoin = "<b>" + userProfile.data.toSabayCoin() + "</b> ";
                String sabayGold = "<b>" + userProfile.data.toSabayGold() + "</b> ";
                mViewBinding.sabayBalance.setVisibility(View.VISIBLE);
                mViewBinding.tvSabayCoinBalance.setText(Html.fromHtml(sabayCoin));
                mViewBinding.tvSabayGoldBalance.setText(Html.fromHtml(sabayGold));
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
}