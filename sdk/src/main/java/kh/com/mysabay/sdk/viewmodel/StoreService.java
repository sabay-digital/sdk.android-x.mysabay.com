package kh.com.mysabay.sdk.viewmodel;

import androidx.lifecycle.ViewModel;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.mysabay.sdk.Checkout_getPaymentServiceProviderForProductQuery;
import com.mysabay.sdk.GetProductsByServiceCodeQuery;
import com.mysabay.sdk.type.Store_PagerInput;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import kh.com.mysabay.sdk.callback.DataCallback;

public class StoreService extends ViewModel {

    private static final String TAG = UserApiVM.class.getSimpleName();
    private ApolloClient apolloClient;

    @Inject
    public StoreService(ApolloClient apolloClient) {
        this.apolloClient = apolloClient;
    }

    public void getShopFromServerGraphQL(String serviceCode, String token, DataCallback<GetProductsByServiceCodeQuery.Store_listProduct> callbackData) {
        Store_PagerInput pager = Store_PagerInput.builder().page(1).limit(20).build();
        apolloClient.query(new GetProductsByServiceCodeQuery(serviceCode, new Input<>(pager, true))).toBuilder()
                .requestHeaders(RequestHeaders.builder()
                        .addHeader("Authorization", "Bearer " + token).build())
                .build()
                .enqueue(new ApolloCall.Callback<GetProductsByServiceCodeQuery.Data>() {

                    @Override
                    public void onResponse(@NotNull Response<GetProductsByServiceCodeQuery.Data> response) {
                        if(response.getData() != null) {
                            callbackData.onSuccess(response.getData().store_listProduct());
                        } else {
                            callbackData.onFailed("Get shop from server Failed");
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        callbackData.onFailed(e);
                    }
        });
    }

    public void getMySabayCheckout(String itemId, DataCallback<Checkout_getPaymentServiceProviderForProductQuery.Checkout_getPaymentServiceProviderForProduct> callbackData) {
        apolloClient.query(new Checkout_getPaymentServiceProviderForProductQuery(itemId)).enqueue(new ApolloCall.Callback<Checkout_getPaymentServiceProviderForProductQuery.Data>() {

            @Override
            public void onResponse(@NotNull Response<Checkout_getPaymentServiceProviderForProductQuery.Data> response) {
                if(response.getData() != null) {
                    callbackData.onSuccess(response.getData().checkout_getPaymentServiceProviderForProduct());
                } else {
                    callbackData.onFailed("Get MySabay checkout failed");
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                callbackData.onFailed(e);
            }
        });
    }
}
