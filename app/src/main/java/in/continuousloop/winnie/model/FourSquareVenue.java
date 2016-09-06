package in.continuousloop.winnie.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Model for a single foursquare venue
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public @Getter @Setter class FourSquareVenue {

    private String id;
    private String name;
    private String categoryName;
    private String url;

    private float latitude;
    private float longitude;
}
