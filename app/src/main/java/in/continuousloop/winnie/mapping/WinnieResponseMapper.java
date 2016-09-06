package in.continuousloop.winnie.mapping;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

import in.continuousloop.winnie.model.FourSquareVenue;
import rx.Observable;

/**
 * A mapper class to map the json response from the winnie api.
 */
public class WinnieResponseMapper {

    /**
     * Map the winnie venues explore api response to app object model.
     *
     * @param aResponse - A JSONObject wrapper around the api response
     */
    public static Observable<List<FourSquareVenue>> mapVenuesResponse(JsonNode aResponse) {
        return Observable.empty();
    }
}
