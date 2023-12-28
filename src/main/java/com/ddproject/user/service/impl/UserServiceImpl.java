package com.ddproject.user.service.impl;

import com.ddproject.global.exception.CustomException;
import com.ddproject.global.exception.ErrorCode;
import com.ddproject.user.domain.User;
import com.ddproject.user.dto.CheckRequestDto;
import com.ddproject.user.dto.PasswordDto;
import com.ddproject.user.dto.SignupResponseDto;
import com.ddproject.user.dto.SignupUserDto;
import com.ddproject.user.repository.UserRepository;
import com.ddproject.user.service.UserService;
import com.ddproject.user.validation.SignupValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SignupValidator signupValidator;
    private final PasswordEncoder passwordEncoder;


    // TODO : implement
    @Override
    public SignupResponseDto signup(SignupUserDto signupUserDto) {
        userRepository.findByUsername(signupUserDto.getUsername()).ifPresent(it -> {
            throw new CustomException(ErrorCode.DUPLICATED_USER_NAME, String.format("%s is duplicated", signupUserDto.getUsername()));
        });

        User user = User.builder()
                .username(signupUserDto.getUsername())
                .password(passwordEncoder.encode(signupUserDto.getPassword()))
                .email(signupUserDto.getEmail())
                .build();

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, SignupResponseDto.class);
    }

    @Override
    public boolean validateSignup(CheckRequestDto checkRequestDto) {
        // 검증할 필드 id, email 등등
        String type = checkRequestDto.getType();

        // pw, id 등등
        if(type.equals("checkId")) {
            String id = checkRequestDto.getId();
            return signupValidator.validateId(id);
        } else if(type.equals("checkEmail")) {
            String email = checkRequestDto.getEmail();
            return signupValidator.validateEmail(email);
        }

        return false;
    }

    // TODO : implement
    @Override
    public void changePw(PasswordDto passwordDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        boolean isCorrect = passwordEncoder.matches(passwordDto.getCurrentPw(), user.getPassword());
        
        if (isCorrect && !passwordDto.getCurrentPw().equals(passwordDto.getChangePw())) {
            user.changePw(passwordEncoder.encode(passwordDto.getChangePw()));
            userRepository.save(user);
        } else {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }
}
