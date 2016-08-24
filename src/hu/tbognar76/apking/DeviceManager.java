package hu.tbognar76.apking;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

public class DeviceManager {

	public ArrayList<DeviceApp> apps = new ArrayList<DeviceApp>();
	public JadbConnection jadb;

	private String runCommandonDevice(JadbDevice device, String command) {
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};
		try {
			device.executeShell(output, command);
		} catch (Exception e) {
			System.out.println(e);
		}
		return output.toString();
	}

	public void getInstalledPackagesFromPhone() {

		try {

			jadb = new JadbConnection();
			jadb.getHostVersion();

			List<JadbDevice> devices = jadb.getDevices();

			JadbDevice device = devices.get(0);
			System.out.println("STARTED");
			System.out.println(devices);
			System.out.println(device.getSerial());
			// System.out.println(device.getState());
			// device.executeShell(System.out, "ls","","-la");
			// device.executeShell(System.out, "pm list packages -f","","");
			String command = "";

			// command = "dumpsys package flipboard.app | grep version";
			/*
			 * versionCode=2848 targetSdk=23 versionName=3.4.0
			 */

			// Get The packages
			//command = "pm list packages -f | sed -e 's/.*=//' | sort";
			command = "pm list packages -f";
			String result = runCommandonDevice(device, command);
			System.out.println("GOT PACKAGES FROM DEVICE");
			
			
			// Get the versions by packagename
			for (String s : result.split("\\r?\\n")) {
				s=s.substring(s.lastIndexOf("=")+1);
				//s=s.replace(".*=/", "");
				
				if (s.startsWith("WARNING")){
					continue;
				}
				if (s.startsWith("android")){
					continue;
				}
				
				if (s.startsWith("ccc71.at")){
					continue;
				}
				
				DeviceApp app = new DeviceApp();
				app.packageName = s;
				
				
				command = "dumpsys package " + s + " | grep version";
				String resultVersion = runCommandonDevice(device, command);
				// result.split("[?\\n");
				resultVersion=resultVersion.replaceAll("\n", " ");
				resultVersion=resultVersion.replaceAll("versionCode=", "|versionCode=");
				resultVersion=resultVersion.replaceAll("targetSdk=", "|targetSdk=");
				resultVersion=resultVersion.replaceAll("versionName=", "|versionName=");
				
				for (String ss : resultVersion.split("\\|")){
					if ((!ss.equals("")) && (!ss.equals(" "))) {

						if (ss.contains("versionCode=")) {
							app.versionCode = ss.substring(ss.lastIndexOf("=") + 1);
						}
						if (ss.contains("targetSdk=")) {
							app.targetSdk = ss.substring(ss.lastIndexOf("=") + 1);
						}
						if (ss.contains("versionName=")) {
							app.versionName = ss.substring(ss.lastIndexOf("=") + 1);
						}

					}
					
				}	
				
				//System.out.println(app +   "!!!!    " + resultVersion);
				
				apps.add(app);

			}

			// DeviceApp app = new DeviceApp();

			
			//for (DeviceApp app : apps){
			//	System.out.println(app);
			//}
			// System.out.println(result);
			//System.out.println(apps.size());
			//System.out.println("");
			//System.out.println("END");
		} catch (Exception e) {
			// org.junit.Assume.assumeNoException(e);
		}
	}
	
	
	
	
}
