package in.continuousloop.winnie.api;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import retrofit2.adapter.rxjava.HttpException;

public @Getter @Setter class ApiResponse {

    private JsonNode meta;
    private JsonNode response;
    private boolean success;
    private HttpException httpException;

    public ApiResponse() {
        success = true;
    }
}