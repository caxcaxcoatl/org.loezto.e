package org.loezto.e.model;

public class EDatabaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String reason = "";

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	@Override
	public String getMessage() {
		return reason;
	}

	public EDatabaseException(Exception e) {
		initCause(e);
	}

	public EDatabaseException() {
	}

	public EDatabaseException(String message) {
		this.reason = message;
	}

}
