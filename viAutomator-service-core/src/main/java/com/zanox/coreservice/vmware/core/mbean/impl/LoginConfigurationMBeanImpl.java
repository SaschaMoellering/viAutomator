package com.zanox.coreservice.vmware.core.mbean.impl;

import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;

import com.zanox.coreservice.vmware.core.helper.LoginConfiguration;
import com.zanox.coreservice.vmware.core.mbean.LoginConfigurationMBean;

/**
 * 
 * MBean-Implementation for the LoginConfigurationMBean
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 * 
 */

@Service(objectName = "com.zanox:service=LoginConfigurationMBean")
@Management(LoginConfigurationMBean.class)
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
