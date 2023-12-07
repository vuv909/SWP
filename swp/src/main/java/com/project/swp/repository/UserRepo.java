package com.project.swp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.swp.entities.User;
import com.project.swp.enums.UserRole;



@Repository
public interface UserRepo extends JpaRepository<User, Long> {

	Optional<User> findById(Long customerId);

	Optional<User> findFirstByEmail(String email);

	User findByRole(UserRole admin);

	Optional<User> findFirstByOtpVerifyMail(Long otp);

	Optional<User> findFirstByEmailAndOtpVerifyMail(String email, Long otp);


}