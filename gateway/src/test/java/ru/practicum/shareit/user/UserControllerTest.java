package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserController userController;

    private UserDto userDto;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUser_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userController.createUser(userDto);

        assertEquals(expectedResponse, response);
        verify(userClient).createUser(userDto);
    }

    @Test
    void updateUser_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userClient.updateUser(eq(userId), any(UserDto.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userController.updateUser(userId, userDto);

        assertEquals(expectedResponse, response);
        verify(userClient).updateUser(userId, userDto);
    }

    @Test
    void getUserById_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userClient.getUserById(eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userController.getUserById(userId);

        assertEquals(expectedResponse, response);
        verify(userClient).getUserById(userId);
    }

    @Test
    void deleteUser_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userClient.deleteUser(eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userController.deleteUser(userId);

        assertEquals(expectedResponse, response);
        verify(userClient).deleteUser(userId);
    }

    @Test
    void getAllUsers_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(userClient.getAllUsers())
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userController.getAllUsers();

        assertEquals(expectedResponse, response);
        verify(userClient).getAllUsers();
    }
}
