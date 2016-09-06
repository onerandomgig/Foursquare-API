package in.continuousloop.winnie.mapping;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import in.continuousloop.winnie.model.FourSquareVenue;
import rx.Observable;

/**
 * A mapper class to map the json response from the foursquare api.
 *
 * NOTE: This response mapper handles mapping manually as opposed to using Jackson/GSON
 * because the api response is huge but only a few fields are required.
 */
public class FourSquareResponseMapper {

    private static final String TAG_GROUPS = "groups";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_VENUE = "venue";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_TIPS = "tips";

    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LONG = "lng";
    private static final String TAG_CANONICAL_URL = "canonicalUrl";

    /**
     * Map the foursquare venues explore api response to app object model.
     *
     * @param aResponse - A JSONObject wrapper around the api response
     */
    public static Observable<List<FourSquareVenue>> mapVenuesResponse(JsonNode aResponse) {

        try {
            return Observable.just(aResponse.get(TAG_GROUPS))
                    .filter(groups -> groups != null)
                    .map(groups -> groups.get(0).get(TAG_ITEMS))
                    .filter(items -> items != null)
                    .map(items -> {

                        JsonNode venueNode;
                        JsonNode tipsNode;
                        FourSquareVenue venue;
                        List<FourSquareVenue> venues = new ArrayList<>();

                        for (JsonNode item : items) {
                            venueNode = item.get(TAG_VENUE);
                            tipsNode = item.get(TAG_TIPS);

                            venue = new FourSquareVenue();
                            venue.setId(venueNode.get(TAG_ID).asText());
                            venue.setName(venueNode.get(TAG_NAME).asText());

                            venue.setLatitude(Float.parseFloat(venueNode.get(TAG_LOCATION).get(TAG_LAT).asText()));
                            venue.setLongitude(Float.parseFloat(venueNode.get(TAG_LOCATION).get(TAG_LONG).asText()));

                            if (venueNode.get(TAG_CATEGORIES) != null && venueNode.get(TAG_CATEGORIES).get(0) != null) {
                                venue.setCategoryName(venueNode.get(TAG_CATEGORIES).get(0).get(TAG_NAME).asText());
                            }

                            if (tipsNode != null && tipsNode.get(0) != null) {
                                venue.setUrl(tipsNode.get(0).get(TAG_CANONICAL_URL).asText());
                            }

                            venues.add(venue);
                        }

                        return venues;
                    });

        } catch (Exception ex) {
            Log.e(FourSquareResponseMapper.class.getName(), "mapVenuesResponse: Error parsing venues response", ex);
            return Observable.empty();
        }
    }
}
