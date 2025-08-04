package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryImplTest {
    private UserRepositoryImpl userRepository;
    private User user1, user2;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        user1 = new User(1L, "user1@mail.com", "User1");
        user2 = new User(2L, "user2@mail.com", "User2");
    }

    //Проверяем сохранение нового пользователя
    @Test
    void save_ShouldAddNewUser() {
        User savedUser = userRepository.save(user1);
        assertEquals(user1, savedUser);
        assertEquals(1, userRepository.findAll().size());
    }

    //Проверяем возвращение пользователя по ID
    @Test
    void findById_ShouldReturnUser_WhenExists() {
        userRepository.save(user1);
        Optional<User> foundUser = userRepository.findById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals(user1, foundUser.get());
    }

    //Проверяем возвращение всех пользователей
    @Test
    void findAll_ShouldReturnAllUsers() {
        userRepository.save(user1);
        userRepository.save(user2);
        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    //Проверяем удаление пользователя по ID
    @Test
    void deleteById_ShouldRemoveUser() {
        userRepository.save(user1);
        assertTrue(userRepository.deleteById(1L));
        assertEquals(0, userRepository.findAll().size());
    }
}
