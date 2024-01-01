package yhproject.playground.wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import yhproject.playground.wiremock.service.ExternalApiService;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class ExternalApiTests {

    ExternalApiService service;

    ObjectMapper objectMapper = new ObjectMapper();

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension
            .newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

    final String EXTERNAL_API_URI = "/data";

    @BeforeEach
    void setUp() {
        String requestUrl = "http://localhost:%s".formatted(wireMockExtension.getPort()) + EXTERNAL_API_URI;
        service = new ExternalApiService(objectMapper, requestUrl);
    }

    @Test
    void request_success() throws JsonProcessingException {
        // given
        String expectedData = "data of external API";
        Map<String, String> response = Map.of(
                "id", "349851320",
                "result", expectedData
        );
        wireMockExtension.stubFor(get(WireMock.urlPathEqualTo(EXTERNAL_API_URI))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsBytes(response))));

        // when
        String data = service.getData();

        // then
        assertEquals(expectedData, data);
        wireMockExtension.verify(1, getRequestedFor(urlPathEqualTo(EXTERNAL_API_URI)));
    }


    @Test
    void request_fail() throws JsonProcessingException {
        // given
        Map<String, String> errorResponse = Map.of(
                "error", "Server temporary unavailable."
        );
        wireMockExtension.stubFor(get(WireMock.urlPathEqualTo(EXTERNAL_API_URI))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody(objectMapper.writeValueAsBytes(errorResponse))));

        // when
        String data = service.getData();

        // then
        assertNull(data);
        int MAX_RETRY = 3;
        wireMockExtension.verify(MAX_RETRY, getRequestedFor(urlPathEqualTo(EXTERNAL_API_URI)));
    }

    @Test
    void request_retry_success() throws JsonProcessingException {
        // given
        String expectedData = "data of external API";
        Map<String, String> errorResponse = Map.of(
                "error", "Server temporary unavailable."
        );
        Map<String, String> successResponse = Map.of(
                "id", "349851320",
                "result", expectedData
        );
        wireMockExtension.stubFor(get(WireMock.urlPathEqualTo(EXTERNAL_API_URI))
                .inScenario("External server temporary unavailable")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody(objectMapper.writeValueAsBytes(errorResponse)))
                .willSetStateTo("External server becomes available again"));
        wireMockExtension.stubFor(get(WireMock.urlPathEqualTo(EXTERNAL_API_URI))
                .inScenario("External server temporary unavailable")
                .whenScenarioStateIs("External server becomes available again")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsBytes(successResponse))));

        // when
        String data = service.getData();

        // then
        assertEquals(expectedData, data);
        wireMockExtension.verify(2, getRequestedFor(urlPathEqualTo(EXTERNAL_API_URI)));
    }

}
