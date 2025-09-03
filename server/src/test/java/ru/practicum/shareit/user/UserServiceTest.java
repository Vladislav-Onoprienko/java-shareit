package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        UserDto userDto = UserDto.builder()
                .name("Alex")
                .email("alex@email.com")
                .build();

        User user = new User();
        user.setId(1L);
        user.setName("Alex");
        user.setEmail("alex@email.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Alex");
        savedUser.setEmail("alex@email.com");

        UserDto expectedDto = UserDto.builder()
                .id(1L)
                .name("Alex")
                .email("alex@email.com")
                .build();

        when(userRepository.existsByEmail("alex@email.com")).thenReturn(false);
        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alex", result.getName());
        assertEquals("alex@email.com", result.getEmail());

        verify(userRepository).existsByEmail("alex@email.com");
        verify(userMapper).toEntity(userDto);
        verify(userRepository).save(user);
        verify(userMapper).toDto(savedUser);
    }

    @Test
    void createUser_ShouldThrowConflictExceptionWhenEmailExists() {
        UserDto userDto = UserDto.builder()
                .name("Alex")
                .email("alex@email.com")
                .build();

        when(userRepository.existsByEmail("alex@email.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(userDto));

        verify(userRepository).existsByEmail("alex@email.com");
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Alex");
        existingUser.setEmail("alex@email.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@email.com");

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@email.com", result.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("updated@email.com");
        verify(userRepository).save(existingUser);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateUser_ShouldUpdateOnlyName() {
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Alex");
        existingUser.setEmail("alex@email.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("alex@email.com");

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("alex@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("alex@email.com", result.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(existingUser);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateUser_ShouldUpdateOnlyEmail() {
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .email("updated@email.com")
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Alex");
        existingUser.setEmail("alex@email.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Alex");
        updatedUser.setEmail("updated@email.com");

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Alex")
                .email("updated@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        UserDto result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Alex", result.getName());
        assertEquals("updated@email.com", result.getEmail());

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("updated@email.com");
        verify(userRepository).save(existingUser);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateUser_ShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, updateDto));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowConflictExceptionWhenEmailAlreadyUsed() {
        Long userId = 1L;
        UserDto updateDto = UserDto.builder()
                .email("existing@email.com")
                .build();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Alex");
        existingUser.setEmail("alex@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(userId, updateDto));

        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("existing@email.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Alex");
        user.setEmail("alex@email.com");

        UserDto expectedDto = UserDto.builder()
                .id(userId)
                .name("Alex")
                .email("alex@email.com")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        UserDto result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Alex", result.getName());
        assertEquals("alex@email.com", result.getEmail());

        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_ShouldThrowNotFoundException() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Alex");
        user1.setEmail("alex@email.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Max");
        user2.setEmail("max@email.com");

        UserDto dto1 = UserDto.builder()
                .id(1L)
                .name("Alex")
                .email("alex@email.com")
                .build();

        UserDto dto2 = UserDto.builder()
                .id(2L)
                .name("Max")
                .email("max@email.com")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alex", result.get(0).getName());
        assertEquals("Max", result.get(1).getName());

        verify(userRepository).findAll();
        verify(userMapper).toDto(user1);
        verify(userMapper).toDto(user2);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_ShouldThrowNotFoundException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}
