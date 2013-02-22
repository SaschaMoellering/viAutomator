package com.zanox.coreservice.vmware.core.helper;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.jboss.security.plugins.PBEUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Class to store the logging-configuration
 * 
 * Sascha Moellering (sascha.moellering@zanox.com)
 * 
 */
public class LoginConfiguration {

	private String username;
	private String password;
	private String vmwareUrl;

	private static String SALT = "df34bn67he32";
	private static String MASTER = "vmwaremasterpassword";
	private static String ALGORITHM = "PBEwithMD5andDES";

	private static Logger logger = LoggerFactory.getLogger(LoginConfiguration.class);

	/**
	 * Constructor of <code>LoginConfigurationMBean</code>
	 * 
	 * @param username Username of the vmware-user
	 * @param password Password of the vmware-user
	 * @param vmwareUrl URL to VSphere-Server
	 */
	public LoginConfiguration(final String username, final String password, final String vmwareUrl) {
		this.username = username;
		this.password = password;
		this.vmwareUrl = vmwareUrl;
	}

	public String getUserName() {
		return username;
	}

	public String getPassword() {
		return decode(password);
	}

	public String getOriginalPassword() {
		return password;
	}

	public String getVmwareUrl() {
		return vmwareUrl;
	}

	/**
	 * Method to encrypt the password of the VMWare-user
	 * 
	 * @param password The plain password of the VMWare-user
	 * @return An encrypted password
	 * @throws Exception if something goes wrong
	 */
	public static String encode(final String password) throws Exception {
		byte[] salt = SALT.substring(0, 8).getBytes();
		int count = 15;
		char[] masterPassword = MASTER.toCharArray();
		byte[] passwordToEncode = password.getBytes("UTF-8");
		PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
		PBEKeySpec keySpec = new PBEKeySpec(masterPassword);
		SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
		SecretKey cipherKey = factory.generateSecret(keySpec);

		return PBEUtils.encode64(passwordToEncode, ALGORITHM, cipherKey, cipherSpec);
	}

	/**
	 * Method to decrypt the password of the VMWare-user
	 * 
	 * @param password The encrypted password of the VMWare-user
	 * @return Exception if something goes wrong
	 */
	public static String decode(final String password) {
		try {
			byte[] salt = SALT.substring(0, 8).getBytes();
			int count = 15;
			char[] masterPassword = MASTER.toCharArray();
			PBEParameterSpec cipherSpec = new PBEParameterSpec(salt, count);
			PBEKeySpec keySpec = new PBEKeySpec(masterPassword);
			SecretKeyFactory factory;
			factory = SecretKeyFactory.getInstance(ALGORITHM);
			SecretKey cipherKey;
			cipherKey = factory.generateSecret(keySpec);

			return PBEUtils.decode64(password, ALGORITHM, cipherKey, cipherSpec);
		} catch (Exception e) {
			logger.error("Decoding-Error", e);
			throw new RuntimeException("could not decode password " + e.getMessage()); // To change body of catch statement use File | Settings | File
																						// Templates.
		}
	}
}
