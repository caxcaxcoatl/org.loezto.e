package org.loezto.e.model;

public class IncorrectVersionException extends EDatabaseException {

	enum VersionComparison {
		Lower, Higher, Unknown
	};

	VersionComparison versionComparison;
	String dbVersion;
	String requestedVersion;

	public VersionComparison getVersionComparison() {
		return versionComparison;
	}

	public void setVersionComparison(VersionComparison versionComparison) {
		this.versionComparison = versionComparison;
	}

	public String getDbVersion() {
		return dbVersion;
	}

	public void setDbVersion(String dbVersion) {
		this.dbVersion = dbVersion;
	}

	public String getRequestedVersion() {
		return requestedVersion;
	}

	public void setRequestedVersion(String requestedVersion) {
		this.requestedVersion = requestedVersion;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7819194928273021201L;
}
