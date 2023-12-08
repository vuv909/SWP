package com.project.swp.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.annotations.Check;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.project.swp.dto.AuthenticationRequest;
import com.project.swp.dto.AuthenticationResponse;
import com.project.swp.dto.ChangePasswordDTO;
import com.project.swp.dto.CheckMailDTO;
import com.project.swp.dto.ForgotPasswordDto;
import com.project.swp.dto.HashToken;
import com.project.swp.dto.OtpEmailDto;
import com.project.swp.dto.SignupRequest;
import com.project.swp.dto.RefreshTokenDto;
import com.project.swp.dto.TokenObject;
import com.project.swp.dto.UserDto;
import com.project.swp.entities.User;
import com.project.swp.repository.UserRepo;
import com.project.swp.services.auth.AuthService;
import com.project.swp.services.jwt.UserDetailsServiceImpl;
import com.project.swp.utils.JwtUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	private final AuthenticationManager authenticationManager;

	private final UserDetailsServiceImpl userDetailsServiceImpl;

	private final JwtUtil jwtUtil;

	private final UserRepo userRepo;

	@Autowired
	private RestTemplate restTemplate;

	public AuthController(AuthService authService, AuthenticationManager authenticationManager,
			UserDetailsServiceImpl userDetailsServiceImpl, JwtUtil jwtUtil, UserRepo userRepo) {
		super();
		this.authService = authService;
		this.authenticationManager = authenticationManager;
		this.userDetailsServiceImpl = userDetailsServiceImpl;
		this.jwtUtil = jwtUtil;
		this.userRepo = userRepo;
	}

	// hash token
	@PostMapping("/refreshtoken")
	public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDto token) {
		if (token == null) {
			return new ResponseEntity<>("Token not valid !!!", HttpStatus.BAD_REQUEST);
		} else {
			return authService.refreshToken(token.getId(), token.getEmail());
		}
	}

	// hash token
	@PostMapping("/tokencode")
	public ResponseEntity<?> tokenValue(@RequestBody HashToken token) {

		String tokenValue = jwtUtil.extractUserName(token.getToken());
		if (tokenValue == null) {
			return new ResponseEntity<>("Token not valid !!!", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(tokenValue, HttpStatus.ACCEPTED);

	}

	@PostMapping("/signup")
	public ResponseEntity<?> signupUser(@RequestBody SignupRequest signupRequest)
			throws UnsupportedEncodingException, MessagingException {

		String passwordRegex = "^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
		Pattern patternPassword = Pattern.compile(passwordRegex);
		Matcher matcherPassword = patternPassword.matcher(signupRequest.getPassword());

		if (matcherPassword.matches()) {
			return authService.createUser(signupRequest);
		} else {
			Map<String, String> error = new HashMap<String, String>();
			error.put("errorPassword",
					"Password at least 8 characrter , 1 uppercase , 1 special character and should not contain any whitespace characters.  !!!");
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping("/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest,
			HttpServletResponse response) throws IOException {

		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(authenticationRequest.getEmail());

		if (matcher.matches()) {
			try {
				authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
						authenticationRequest.getEmail(), authenticationRequest.getPassword()));
			} catch (BadCredentialsException e) {

				String error = "Incorrect username or password";
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
			} catch (DisabledException disabledException) {
				String error = "User not active";
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
			}

			final UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(authenticationRequest.getEmail());
			TokenObject tokenObject = new TokenObject();
			Optional<User> optionalUser = userRepo.findFirstByEmail(userDetails.getUsername());

			if (optionalUser.isPresent() && Boolean.TRUE.equals(optionalUser.get().isEnabled())) {

				tokenObject.setId(optionalUser.get().getId());
				tokenObject.setEmail(optionalUser.get().getEmail());
				tokenObject.setRole(optionalUser.get().getRole());

				final String jwt = jwtUtil.generateToken(tokenObject);
				final String refreshToken = jwtUtil.generateRefreshToken(tokenObject);

				AuthenticationResponse authenticationResponse = new AuthenticationResponse();

				optionalUser.get().setRefreshToken(refreshToken);
				userRepo.save(optionalUser.get());

				authenticationResponse.setJwt(jwt);
				authenticationResponse.setUserRole(optionalUser.get().getRole());
				authenticationResponse.setUserId(optionalUser.get().getId());

				Map<String, Object> success = new HashMap<String, Object>();
				success.put("jwt", jwt);

				return ResponseEntity.ok(success);
			} else {
				Map<String, String> error = new HashMap<String, String>();
				error.put("errorLogin", "Your account is not verified !!!");
				return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			Map<String, String> error = new HashMap<String, String>();
			error.put("errorEmail", "Email is not correct format !!!");
			return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// verify OTP
	@PostMapping("/verifyOtpEmail")
	public ResponseEntity<?> verifyOtpEmail(@RequestBody OtpEmailDto opt) {

		return authService.verifyUser(opt.getOtp(), opt.getEmail());

	}

	@PostMapping("/verifyAgainOtpEmail")
	public ResponseEntity<?> verifyAgainOtpEmail(@RequestBody UserDto userDtoVerify) {

		return authService.verifyAgainUser(userDtoVerify.getEmail());
	}

//	@PostMapping("/check")
//	public ResponseEntity<?> verifyAgainOtpEmail(@RequestBody OtpEmailDto opt) {
//
//		CheckMailDTO response = restTemplate.getForObject(
//				"https://emailverification.whoisxmlapi.com/api/v3?apiKey=at_7GFRvMURmX39VzkJkrPlMOai69aOX&emailAddress="
//						+ opt.getEmail(),
//				CheckMailDTO.class);
//
//		if (response != null && response.isSmtpCheck()) {
//			// SMTP check passed, handle accordingly
//			return new ResponseEntity<>("SMTP check passed", HttpStatus.ACCEPTED);
//		} else {
//			// SMTP check failed or data is null
//			return new ResponseEntity<>("SMTP check failed or data is null", HttpStatus.BAD_REQUEST);
//		}
//
//	}

	// forgot password
//	@PostMapping("/forgotpassword")
//	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
//		return authService.forgotPassword(forgotPasswordDto);
//	}

	// change password
	@PostMapping("/changepassword")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {

		String passwordRegex = "^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
		Pattern patternPassword = Pattern.compile(passwordRegex);
		Matcher matcherPassword = patternPassword.matcher(changePasswordDTO.getPassword());

		if (matcherPassword.matches()) {
			return authService.changePassword(changePasswordDTO);
		} else {
			Map<String, String> error = new HashMap<String, String>();
			error.put("errorPassword",
					"Password at least 8 characrter , 1 uppercase , 1 special character and should not contain any whitespace characters.  !!!");
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}
	}

}
