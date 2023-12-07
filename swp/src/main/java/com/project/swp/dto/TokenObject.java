package com.project.swp.dto;

import com.project.swp.enums.UserRole;

public class TokenObject {

	private Long id;

	private String email;

	private UserRole role;

	public TokenObject() {
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	@Override
	public String toString() {

		return "ID:" + getId() + ",Email:" + getEmail() + ",Role:" + getRole();
	}

}
