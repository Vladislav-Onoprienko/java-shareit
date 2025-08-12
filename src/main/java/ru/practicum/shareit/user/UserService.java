package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto.getEmail());
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Попытка создания пользователя с занятым email: {}", userDto.getEmail());
            throw new ConflictException("Email уже занят");
        }
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя с ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userRepository.existsByEmail(userDto.getEmail()) && !userDto.getEmail().equals(existingUser.getEmail())) {
                throw new ConflictException("Этот email уже используется другим пользователем");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return userMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("Попытка удаления несуществующего пользователя с ID: {}", userId);
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }
}
