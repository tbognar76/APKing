package hu.tbognar76.apking;

import java.io.Serializable;

public class DeviceApp implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6119323249832866998L;
	public String packageName;
	public String versionCode;
	public String versionName;
	public String targetSdk;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("packageName=");
		sb.append(packageName);
		sb.append("  ");
		sb.append("versionCode=");
		sb.append(versionCode);
		sb.append("  ");
		sb.append("versionName=");
		sb.append(versionName);
		sb.append("  ");
		sb.append("targetSdk=");
		sb.append(targetSdk);
		sb.append("  ");
		return sb.toString();

	}
}