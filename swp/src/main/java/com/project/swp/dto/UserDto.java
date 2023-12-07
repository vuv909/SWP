package com.project.swp.dto;


import com.project.swp.enums.UserRole;

import lombok.Data;

public class UserDto {

	private Long id;

	private String name;

	private String email;

	private String password;

	private UserRole userRole;

	private boolean enabled;
	
	private Long otpVerifyMail;

	public Long getOtpVerifyMail() {
		return otpVerifyMail;
	}

	public void setOtpVerifyMail(Long otpVerifyMail) {
		this.otpVerifyMail = otpVerifyMail;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

}
