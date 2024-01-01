package yhproject.playground.wiremock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import yhproject.playground.wiremock.dto.ExternalApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExternalApiService {

    private final ObjectMapper objectMapper;

    private final String requestUrl;

    public ExternalApiService(ObjectMapper objectMapper, String requestUrl) {
        this.objectMapper = objectMapper;
        this.requestUrl = requestUrl;
    }

    public String getData() {
        try {
            int MAX_RETRY = 3;
            for (int i = 0; i < MAX_RETRY; i++) {
                ExternalApiResponse response = request();
                // 200대 응답을 받으면 바로 결과 데이터를 반환
                if (is2xxStatusCode(response.getStatusCode())) {
                    return response.getResult();
                }
                // 500대 응답을 받으면 3회까지 재시도
                if (is5xxStatusCode(response.getStatusCode())) {
                    System.out.printf("외부 API 서버 응답 오류: %d (재시도 %d/%d)"
                            .formatted(response.getStatusCode(), i, MAX_RETRY));
                    // 재시도 전 1초 대기
                    waitBeforeRetry(1000);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("외부 API 요청 수행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private ExternalApiResponse request() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(requestUrl))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            ExternalApiResponse result = objectMapper.readValue(response.body(), ExternalApiResponse.class);
            result.setStatusCode(response.statusCode());
            return result;
        }
    }

    private boolean is2xxStatusCode(int statusCode) {
        return statusCode / 100 == 2;
    }

    private boolean is5xxStatusCode(int statusCode) {
        return statusCode / 100 == 5;
    }

    private void waitBeforeRetry(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.println("외부 API 재시도를 위한 대기 중 문제 발생: " + e.getMessage());
        }
    }

}
