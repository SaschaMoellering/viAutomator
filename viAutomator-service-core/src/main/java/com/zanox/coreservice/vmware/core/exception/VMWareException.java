/**
 * 
 */
package com.zanox.coreservice.vmware.core.exception;

/**
 * 
 * Non-technical exception to catch the technical-ones
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 * 
 */
public class VMWareException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8779050125374959365L;

	/**
	 * 
	 * @param exc
	 */
	public VMWareException(Exception exc) {
		super(exc);
	}

	public VMWareException(String cause) {
		super(cause);
	}

	/**
	 * 
	 * @param cause
	 * @param exc
	 */
	public VMWareException(String cause, VMWareException exc) {
		super(cause, exc);
	}

    public VMWareException(String cause, Exception exc) {
        super(cause, exc);
    }
}
