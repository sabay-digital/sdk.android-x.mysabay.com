package kh.com.mysabay.sdk.ui.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.databinding.PartialShopItemBinding;
import kh.com.mysabay.sdk.ui.activity.StoreActivity;
import kh.com.mysabay.sdk.ui.fragment.PaymentFm;

/**
 * Created by Tan Phirum on 3/13/20
 * Gmail phirumtan@gmail.com
 */
public class ShopItmVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    public PartialShopItemBinding viewBinding;

    public ShopItmVH(@NonNull View itemView) {
        super(itemView);
        this.viewBinding = DataBindingUtil.bind(itemView);
        if (this.viewBinding != null)
            this.viewBinding.card.setOnClickListener(this);
    }


    @Override
    public void onClick(@NotNull View v) {
        if (v.getId() == R.id.card) {
            if (v.getContext() instanceof StoreActivity)
                ((StoreActivity) v.getContext()).initAddFragment(PaymentFm.newInstance(), PaymentFm.TAG, true);
        }
    }
}
