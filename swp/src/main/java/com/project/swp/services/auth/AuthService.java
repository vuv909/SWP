package com.project.swp.services.auth;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.project.swp.dto.ChangePasswordDTO;
import com.project.swp.dto.ForgotPasswordDto;
import com.project.swp.dto.SignupRequest;
import com.project.swp.dto.UserDto;
import com.project.swp.entities.User;

import jakarta.mail.MessagingException;

public interface AuthService {

	ResponseEntity<?> createUser(SignupRequest signupRequest) throws UnsupportedEncodingException, MessagingException;

	boolean sendVerificationeEmail(User user, Long otpVerificationMail) throws UnsupportedEncodingException, MessagingException;

	UserDto findFirstByEmail(String email);

	ResponseEntity<?> verifyUser(Long otp, String email);

	ResponseEntity<?> verifyAgainUser(String email);

	ResponseEntity<?> forgotPassword(ForgotPasswordDto forgotPasswordDto);

	ResponseEntity<?> changePassword(ChangePasswordDTO changePasswordDTO);
}
