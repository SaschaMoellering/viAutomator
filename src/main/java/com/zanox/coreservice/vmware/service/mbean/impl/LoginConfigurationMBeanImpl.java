package com.zanox.coreservice.vmware.service.mbean.impl;

import com.zanox.coreservice.vmware.service.helper.LoginConfiguration;
import com.zanox.coreservice.vmware.service.mbean.LoginConfigurationMBean;

/**
 * 
 * MBean-Implementation for the LoginConfigurationMBean
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 * 
 */

public class LoginConfigurationMBeanImpl implements LoginConfigurationMBean {

	public LoginConfigurationMBeanImpl() {
	}

	/**
	 * MBean-method to encode a plain-text-password
	 * 
	 * @param password The plain-text-password
	 * @return The encrypted password
	 * @throws Exception If something goes wrong
	 */
	public String encode(String password) throws Exception {
		return LoginConfiguration.encode(password);
	}

	/**
	 * MBean-method to decode an encrypted password
	 * 
	 * @param password The encrypted password
	 * @return The decrypted password
	 */
	public String decode(String password) {
		return LoginConfiguration.decode(password);
	}
}
