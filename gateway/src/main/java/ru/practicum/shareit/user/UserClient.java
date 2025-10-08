package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post(API_PREFIX, userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserDto userDto) {
        Map<String, Object> parameters = Map.of("userId", userId);
        return patch(API_PREFIX + "/{userId}", userId, parameters, userDto);
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        Map<String, Object> parameters = Map.of("userId", userId);
        return get(API_PREFIX + "/{userId}", userId, parameters);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        Map<String, Object> parameters = Map.of("userId", userId);
        return delete(API_PREFIX + "/{userId}", userId, parameters);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get(API_PREFIX);
    }
}