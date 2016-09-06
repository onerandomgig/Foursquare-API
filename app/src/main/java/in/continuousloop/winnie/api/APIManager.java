package in.continuousloop.winnie.api;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import in.continuousloop.winnie.constants.AppConstants;
import in.continuousloop.winnie.mapping.FourSquareResponseMapper;

import in.continuousloop.winnie.model.FourSquareVenue;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Observable;

/**
 * This class handles all API requests.
 */
public class APIManager {

    private static APIManager mSingletonInstance;
    private static final String TAG = "WN/APIManager";

    private FoursquareAPIService foursquareAPIService;
    private WinnieAPIService winnieAPIService;

    private static final String WINNIE_BASE_URL = "https://api.winnielabs.com";
    private static final String BASE_URL = "https://api.foursquare.com/v2/";

    private APIManager() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(5, 60, TimeUnit.SECONDS))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor((chain) -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (response.cacheResponse() != null) {
                        Log.d(TAG, request.method() + " " + response.code()
                                + " to " + request.url().toString() + " served from cache");
                    } else {
                        Log.d(TAG, request.method() + " " + response.code()
                                + " to " + request.url().toString() + " served from network");
                    }
                    return response;
                });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        foursquareAPIService = retrofit.create(FoursquareAPIService.class);
        winnieAPIService = retrofit.create(WinnieAPIService.class);
    }

    /**
     * Get the singleton instance of the APIManager.
     *
     * @return {@link APIManager}
     */
    public static synchronized APIManager getInstance() {
        if (mSingletonInstance == null) {
            mSingletonInstance = new APIManager();
        }

        return mSingletonInstance;
    }

    /**
     * Get venues at the specified lat. long. Delegates actual request to {@link FoursquareAPIService}
     * @param latitude  - Lat. of a location
     * @param longitude - Long. of a location
     *
     * @return - List of {@link FourSquareVenue} as an observable
     */
    public Observable<List<FourSquareVenue>> getVenuesAtLocation(double latitude, double longitude) {

        return foursquareAPIService.getVenues(latitude+","+longitude,
                AppConstants.FSQ_CLIENT_ID,
                AppConstants.FSQ_CLIENT_SECRET,
                "20160902",
                "foursquare")
                .flatMap(response -> FourSquareResponseMapper.mapVenuesResponse(response.getResponse()));
    }
}
