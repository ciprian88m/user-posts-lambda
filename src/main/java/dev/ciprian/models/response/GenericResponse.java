package dev.ciprian.models.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GenericResponse {

    @JsonIgnore
    private boolean valid;
    private int statusCode;
    private String errorMessage;

    public GenericResponse(boolean valid, int statusCode) {
        this.valid = valid;
        this.statusCode = statusCode;
    }

    public GenericResponse(boolean valid, int statusCode, String errorMessage) {
        this.valid = valid;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
