package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({UserService.class, UserMapperImpl.class})
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        UserDto result = userService.createUser(userDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowConflictException() {
        User existingUser = User.builder()
                .name("Existing User")
                .email("duplicate@example.com")
                .build();
        entityManager.persist(existingUser);
        entityManager.flush();

        UserDto newUserDto = UserDto.builder()
                .name("New User")
                .email("duplicate@example.com")
                .build();

        assertThrows(ConflictException.class, () -> userService.createUser(newUserDto));
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {
        User existingUser = User.builder()
                .name("Old Name")
                .email("old@example.com")
                .build();
        entityManager.persist(existingUser);
        entityManager.flush();

        UserDto updateDto = UserDto.builder()
                .name("New Name")
                .email("new@example.com")
                .build();

        UserDto result = userService.updateUser(existingUser.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserDto result = userService.getUserById(user.getId());

        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("Test User");
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {
        User user = User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        userService.deleteUser(user.getId());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }
}
