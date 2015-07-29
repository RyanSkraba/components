package org.talend.component.common;

import org.talend.component.ComponentProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("userPasswordProperties")
public class UserPasswordProperties extends ComponentProperties {

	@JsonProperty
	public String userId;

	@JsonProperty
	public String password;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
