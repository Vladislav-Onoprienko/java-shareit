package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BaseClient baseClient;

    @BeforeEach
    void setUp() {
        baseClient = new BaseClient(restTemplate);
    }

    @Test
    void get_WithUserId_ShouldSetHeaders() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("test");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.get("/test", 1L);

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(
                eq("http://server:9090/test"),
                eq(HttpMethod.GET),
                argThat(entity -> entity.getHeaders().containsKey("X-Sharer-User-Id")),
                eq(Object.class)
        );
    }

    @Test
    void post_WithBody_ShouldSendRequest() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("test");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.post("/test", 1L, "request body");

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(
                eq("http://server:9090/test"),
                eq(HttpMethod.POST),
                argThat(entity -> entity.getBody().equals("request body")),
                eq(Object.class)
        );
    }

    @Test
    void patch_WithParameters_ShouldUseParameters() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("test");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.patch("/test/{id}", 1L, Map.of("id", 123), "body");

        assertEquals(expectedResponse, response);
        verify(restTemplate).exchange(
                eq("http://server:9090/test/{id}"),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("id", 123))
        );
    }

    @Test
    void handleHttpError_ShouldReturnErrorResponse() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = baseClient.get("/test", 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}