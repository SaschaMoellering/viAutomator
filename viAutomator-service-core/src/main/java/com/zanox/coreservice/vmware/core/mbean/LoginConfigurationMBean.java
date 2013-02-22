package com.zanox.coreservice.vmware.core.mbean;

/**
 * 
 * MBean-interface to encode/decode passwords
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 * 
 */
public interface LoginConfigurationMBean {

	/**
	 * MBean-method to encode a plain-text-password
	 * 
	 * @param password The plain-text-password
	 * @return The encrypted password
	 * @throws Exception If something goes wrong
	 */
	public String encode(String password) throws Exception;

	/**
	 * MBean-method to decode an encrypted password
	 * 
	 * @param password The encrypted password
	 * @return The decrypted password
	 */
	public String decode(String password);
}
