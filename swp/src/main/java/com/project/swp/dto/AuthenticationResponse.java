package com.project.swp.dto;

import com.project.swp.enums.UserRole;

public class AuthenticationResponse{
  private String jwt;
  private UserRole userRole;
  private Long userId;
  private String msg;
public String getJwt() {
	return jwt;
}
public void setJwt(String jwt) {
	this.jwt = jwt;
}
public UserRole getUserRole() {
	return userRole;
}
public void setUserRole(UserRole userRole) {
	this.userRole = userRole;
}
public Long getUserId() {
	return userId;
}
public void setUserId(Long userId) {
	this.userId = userId;
}
public String getMsg() {
	return msg;
}
public void setMsg(String msg) {
	this.msg = msg;
}


  
}
