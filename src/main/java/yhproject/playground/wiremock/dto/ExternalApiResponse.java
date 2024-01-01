package yhproject.playground.wiremock.dto;

public class ExternalApiResponse {

    private long id;
    private int statusCode;
    private String result;
    private String error;

    public long getId() {
        return id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

}
