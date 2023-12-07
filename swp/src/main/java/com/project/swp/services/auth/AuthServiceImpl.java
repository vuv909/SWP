package com.project.swp.services.auth;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.swp.dto.ChangePasswordDTO;
import com.project.swp.dto.CheckMailDTO;
import com.project.swp.dto.ForgotPasswordDto;
import com.project.swp.dto.SignupRequest;
import com.project.swp.dto.UserDto;
import com.project.swp.entities.User;
import com.project.swp.enums.UserRole;
import com.project.swp.repository.UserRepo;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepo userRepo;

	private final JavaMailSender javaMailSender;

	private final JavaMailSenderImpl javaMailSenderImp;

	@Autowired
	private RestTemplate restTemplate;

	public AuthServiceImpl(UserRepo userRepo, JavaMailSender javaMailSender, JavaMailSenderImpl javaMailSenderImp) {
		super();

		this.userRepo = userRepo;
		this.javaMailSender = javaMailSender;
		this.javaMailSenderImp = javaMailSenderImp;

	}

	@PostConstruct
	// no se chay bat cu khi nao ban chay application
	public void createAdminAccount() {
		User adminAccount = userRepo.findByRole(UserRole.ADMIN);
		if (adminAccount == null) {
			User user = new User();
			user.setName("admin");
			user.setEmail("admin@test.com");
			user.setPassword(new BCryptPasswordEncoder().encode("123"));
			user.setRole(UserRole.ADMIN);
			userRepo.save(user);
		}
	}

	@Override
	public boolean sendVerificationeEmail(User user, Long otpVerificationMail) {
		try {
			String subject = "Please verify your registration";
			String senderName = "Verify account";
			String mailContent = "<p>Dear " + user.getName() + ",</p>";
			mailContent += "<p>Please click the link below to verify your account !!!</p>";
			mailContent += "<h3>This is your OTP code : " + otpVerificationMail + "</h3>";
			mailContent += "<p>Thank you</p>";

			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom("thaimih834@gmail.com", senderName);
			helper.setTo(user.getEmail());
			helper.setSubject(subject);
			helper.setText(mailContent, true);

			Properties props = javaMailSenderImp.getJavaMailProperties();
			props.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS

			javaMailSender.send(message);
			return true;
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public ResponseEntity<?> verifyAgainUser(String email) {
		Optional<User> user = userRepo.findFirstByEmail(email);
		String otpString = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L) + "";
		Long otpVerificationMail = Long.parseLong(otpString);

		if (user.isPresent()) {
			boolean send = sendVerificationeEmail(user.get(), otpVerificationMail);
			if (send == true) {
				user.get().setOtpVerifyMail(otpVerificationMail);
				User update = userRepo.save(user.get());
				UserDto userDto = new UserDto();
				userDto.setId(user.get().getId());
				return ResponseEntity.ok(userDto);
			} else {
				Map<String, Object> error = new HashMap<>();
				error.put("errorSendOtp", "Otp send not successfully !!!");
				return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
			}
		}
		Map<String, Object> error = new HashMap<>();
		error.put("errorUser", "Can find user !!!");
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> createUser(SignupRequest signupRequest)
			throws UnsupportedEncodingException, MessagingException {

		CheckMailDTO response = restTemplate.getForObject(
				"https://emailverification.whoisxmlapi.com/api/v3?apiKey=at_7GFRvMURmX39VzkJkrPlMOai69aOX&emailAddress="
						+ signupRequest.getEmail(),
				CheckMailDTO.class);

		if (response != null && response.isSmtpCheck()) {

			Optional<User> findUserByEmail = userRepo.findFirstByEmail(signupRequest.getEmail());

			if (findUserByEmail.isPresent()) {
				Map<String, Object> error = new HashMap<String, Object>();
				error.put("errorExist", "Email is existed in my website !!!");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
			}

			// random 10 character long
			String otpString = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L) + "";
			String verificationCode = RandomStringUtils.random(64, true, true);
			Long otpVerificationMail = Long.parseLong(otpString);

			User user = new User();
			user.setName(signupRequest.getName());
			user.setEmail(signupRequest.getEmail());
			user.setPassword(new BCryptPasswordEncoder().encode(signupRequest.getPassword()));
			user.setRole(UserRole.CUSTOMER);
			user.setEnabled(false);
			user.setVerificationCode(verificationCode);
			user.setOtpVerifyMail(otpVerificationMail);

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 5); // Adding 1 minute to the current time

			Date currentDatePlusOneMinute = calendar.getTime();

			user.setTimeOtpEmailValid(currentDatePlusOneMinute);
			boolean checkEmail = sendVerificationeEmail(user, otpVerificationMail);

			if (checkEmail == true) {

				User userCreate = userRepo.save(user);
				UserDto createdUserDto = new UserDto();
				createdUserDto.setId(userCreate.getId());
				createdUserDto.setName(userCreate.getName());
				createdUserDto.setEmail(userCreate.getEmail());
				createdUserDto.setUserRole(userCreate.getRole());
				Map<String, Object> error = new HashMap<String, Object>();

				return ResponseEntity.ok(createdUserDto);
			} else {
				Map<String, Object> error = new HashMap<String, Object>();
				error.put("errorSend", "Error when send OTP !!!");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
			}
		} else {
			Map<String, String> error = new HashMap<String, String>();
			error.put("errorEmailValid", "Email is not valid !!!");
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public UserDto findFirstByEmail(String email) {
		Optional<User> optionalUser = userRepo.findFirstByEmail(email);
		if (optionalUser.isPresent()) {
			UserDto userDto = new UserDto();
			userDto.setId(optionalUser.get().getId());
			userDto.setUserRole(optionalUser.get().getRole());
			userDto.setEnabled(optionalUser.get().isEnabled());
			return userDto;
		}
		return null;
	}

	@Override
	public ResponseEntity<?> verifyUser(Long otp, String email) {
		Optional<User> optionalUser = userRepo.findFirstByEmailAndOtpVerifyMail(email, otp);
		if (optionalUser.isPresent()) {

			Date currentDate = new Date();
			Date checkDate = optionalUser.get().getTimeOtpEmailValid();

			if (checkDate.after(currentDate)) {
				optionalUser.get().setEnabled(true);
				User saveUser = userRepo.save(optionalUser.get());
				UserDto update = new UserDto();
				update.setId(optionalUser.get().getId());
				update.setEnabled(optionalUser.get().isEnabled());
				return ResponseEntity.ok(update);
			} else if (checkDate.before(currentDate)) {
				Map<String, String> error = new HashMap<String, String>();
				error.put("expiredOtp", "OTP is expired !!!");
				return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
			} else {
				optionalUser.get().setEnabled(true);
				User saveUser = userRepo.save(optionalUser.get());
				UserDto update = new UserDto();
				update.setId(optionalUser.get().getId());
				update.setEnabled(optionalUser.get().isEnabled());
				return ResponseEntity.ok(update);
			}

		}
		Map<String, String> error = new HashMap<String, String>();
		error.put("errorEmail", "Email is not exist !!!");
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> forgotPassword(ForgotPasswordDto forgotPasswordDto) {

		String otpString = ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L) + "";
		Long otpVerificationMail = Long.parseLong(otpString);

		Optional<User> optionalUser = userRepo.findFirstByEmail(forgotPasswordDto.getEmail());

		if (optionalUser.isPresent()) {

			boolean checkEmail = sendVerificationeEmail(optionalUser.get(), otpVerificationMail);

			if (checkEmail == true) {

				Date currentDate = new Date();
				Date checkDate = optionalUser.get().getTimeOtpEmailValid();

				if (checkDate.after(currentDate)) {
					optionalUser.get().setPassword(new BCryptPasswordEncoder().encode(forgotPasswordDto.getPassword()));
					userRepo.save(optionalUser.get());
					Map<String, Object> success = new HashMap<String, Object>();
					success.put("success", "Change password successfully !!!");
					return ResponseEntity.ok(success);
				} else if (checkDate.before(currentDate)) {
					Map<String, Object> error = new HashMap<String, Object>();
					error.put("errorOtp", "OTP is invalid time !!!");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
				} else {

					optionalUser.get().setPassword(new BCryptPasswordEncoder().encode(forgotPasswordDto.getPassword()));
					userRepo.save(optionalUser.get());
					Map<String, Object> success = new HashMap<String, Object>();
					success.put("success", "Change password successfully !!!");
					return ResponseEntity.ok(success);
				}

			} else {
				Map<String, Object> error = new HashMap<String, Object>();
				error.put("errorSend", "Error when send OTP !!!");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
			}

		}
		Map<String, String> error = new HashMap<String, String>();
		error.put("errorEmail", "Email or OTP is not exist !!!");
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> changePassword(ChangePasswordDTO changePasswordDTO) {

		Optional<User> optionalUser = userRepo.findFirstByEmail(changePasswordDTO.getEmail());

		if (optionalUser.isPresent()) {
			optionalUser.get().setPassword(new BCryptPasswordEncoder().encode(changePasswordDTO.getPassword()));
			userRepo.save(optionalUser.get());
			Map<String, Object> success = new HashMap<String, Object>();
			success.put("success", "Change password successfully !!!");
			return ResponseEntity.ok(success);
		}

		Map<String, String> error = new HashMap<String, String>();
		error.put("errorEmail", "Email is not exist !!!");
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

}
