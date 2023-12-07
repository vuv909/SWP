package com.project.swp.dto;

public class CheckMailDTO {
    private String username;
    private String domain;
    private String emailAddress;
    private boolean formatCheck;
    private boolean smtpCheck;
    private boolean dnsCheck;
    private boolean freeCheck;
    private boolean disposableCheck;
    private boolean catchAllCheck;


    public CheckMailDTO() {
    }


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getDomain() {
		return domain;
	}


	public void setDomain(String domain) {
		this.domain = domain;
	}


	public String getEmailAddress() {
		return emailAddress;
	}


	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	public boolean isFormatCheck() {
		return formatCheck;
	}


	public void setFormatCheck(boolean formatCheck) {
		this.formatCheck = formatCheck;
	}


	public boolean isSmtpCheck() {
		return smtpCheck;
	}


	public void setSmtpCheck(boolean smtpCheck) {
		this.smtpCheck = smtpCheck;
	}


	public boolean isDnsCheck() {
		return dnsCheck;
	}


	public void setDnsCheck(boolean dnsCheck) {
		this.dnsCheck = dnsCheck;
	}


	public boolean isFreeCheck() {
		return freeCheck;
	}


	public void setFreeCheck(boolean freeCheck) {
		this.freeCheck = freeCheck;
	}


	public boolean isDisposableCheck() {
		return disposableCheck;
	}


	public void setDisposableCheck(boolean disposableCheck) {
		this.disposableCheck = disposableCheck;
	}


	public boolean isCatchAllCheck() {
		return catchAllCheck;
	}


	public void setCatchAllCheck(boolean catchAllCheck) {
		this.catchAllCheck = catchAllCheck;
	}

  
   
}
