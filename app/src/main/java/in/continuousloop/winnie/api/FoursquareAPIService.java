package in.continuousloop.winnie.api;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface FoursquareAPIService {

    @GET("venues/explore")
    Observable<ApiResponse> getVenues(@Query("ll") String latLong,
                                      @Query("client_id") String clientId,
                                      @Query("client_secret") String clientSecret,
                                      @Query("v") String version,
                                      @Query("m") String mapper);
}
