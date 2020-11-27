package kh.com.mysabay.sdk.webservice.api;

import io.reactivex.Observable;
import kh.com.mysabay.sdk.pojo.login.LoginItem;
import kh.com.mysabay.sdk.pojo.login.LoginResponseItem;
import kh.com.mysabay.sdk.pojo.logout.LogoutResponseItem;
import kh.com.mysabay.sdk.pojo.profile.UserProfileItem;
import kh.com.mysabay.sdk.pojo.refreshToken.RefreshTokenItem;
import kh.com.mysabay.sdk.pojo.refreshToken.TokenVerify;
import kh.com.mysabay.sdk.pojo.verified.VerifiedItem;
import kh.com.mysabay.sdk.webservice.Constant;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Tan Phirum on 3/7/20
 * Gmail phirumtan@gmail.com
 */
public interface UserApi {

    @GET("api/"+ Constant.userAPIVersion +"/user/login/{phone}")
    Observable<LoginItem> getUserLogin(@Header("app_secret") String appSecret, @Path("phone") String phone);

    @FormUrlEncoded
    @POST("api/"+ Constant.userAPIVersion +"/user/verify_code")
    Observable<VerifiedItem> postVerifyCode(@Header("app_secret") String appSecret,
                                            @Field("phone") String phone, @Field("verify_code") int verifyCode);
    @FormUrlEncoded
    @POST("api/"+ Constant.userAPIVersion +"/user/refresh/token")
    Observable<RefreshTokenItem> postRefreshToken(@Header("app_secret") String appSecret, @Field("refresh_token") String refreshToken);

    @Headers({
            "Accept: */*"
    })
    @POST("api/"+ Constant.userAPIVersion +"/user/mysabay/login")
    Observable<String> postLoginWithMySabay(@Header("app_secret") String appSecret);

    @GET("api/"+ Constant.userAPIVersion +"/user/profile")
    Observable<UserProfileItem> getUserProfile(@Header("app_secret") String appSecret, @Header("Authorization") String token);

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "api/"+ Constant.userAPIVersion +"/user/logout", hasBody = true)
    Observable<LogoutResponseItem> logout(@Header("app_secret") String appSecret, @Field("refresh_token") String refreshToken, @Query("all") String all);

    @GET("api/"+ Constant.userAPIVersion +"/user/verify/token")
    Observable<TokenVerify> getVerifyToken(@Header("app_secret") String appSecret, @Header("Authorization") String token);

    @FormUrlEncoded
    @POST("api/v1.6"+"/user/login/facebook")
    Observable<LoginResponseItem> loginWithFacebook(@Header("service-code") String serviceCode, @Field("token") String token);

}
