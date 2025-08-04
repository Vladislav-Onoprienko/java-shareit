package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepositoryImpl;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        UserMapper userMapper = new UserMapper();
        userService = new UserService(userRepository, userMapper);
    }

    //Проверяем создание пользователя с уникальным Email
    @Test
    void createUser_ShouldSaveUser_WhenEmailIsUnique() {
        UserDto userDto = new UserDto(null, "User", "user@mail.com");
        UserDto savedUser = userService.createUser(userDto);

        assertNotNull(savedUser.getId());
        assertEquals("user@mail.com", savedUser.getEmail());
    }

    //Проверяека возникновения ConflictException при попытке создать пользователя с существующим email
    @Test
    void createUser_ShouldThrowConflict_WhenEmailExists() {
        UserDto userDto1 = new UserDto(null, "User1", "user@mail.com");
        userService.createUser(userDto1);

        UserDto userDto2 = new UserDto(null, "User2", "user@mail.com");
        assertThrows(ConflictException.class, () -> userService.createUser(userDto2));
    }

    // Проверяем обновление имени и email пользователя
    @Test
    void updateUser_ShouldUpdateNameAndEmail() {
        UserDto userDto = new UserDto(null, "OldName", "old@mail.com");
        UserDto savedUser = userService.createUser(userDto);

        UserDto updateDto = new UserDto(savedUser.getId(), "NewName", "new@mail.com");
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertEquals("NewName", updatedUser.getName());
        assertEquals("new@mail.com", updatedUser.getEmail());
    }

    // Проверка возникновения NotFoundException при запросе несуществующего пользователя
    @Test
    void getUserById_ShouldThrowNotFound_WhenUserNotExists() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }
}

