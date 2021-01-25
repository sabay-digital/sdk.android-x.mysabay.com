package kh.com.mysabay.sdk.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import kh.com.mysabay.sdk.R;

import kh.com.mysabay.sdk.ui.holder.CountryItem;

public class CountryAdapter extends ArrayAdapter<CountryItem> {

    public CountryAdapter(Context context, ArrayList<CountryItem> countryList) {
        super(context, 0, countryList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }
    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.country_spinner_row, parent, false
            );
        }
        TextView tvCountryCod = convertView.findViewById(R.id.tv_country_code);
        TextView tvDialingCode = convertView.findViewById(R.id.tv_dialing_code);
        CountryItem currentItem = getItem(position);
        if (currentItem != null) {
            tvCountryCod.setText(currentItem.getCode());
            tvDialingCode.setText(currentItem.getDial_code());
        }
        return convertView;
    }
}