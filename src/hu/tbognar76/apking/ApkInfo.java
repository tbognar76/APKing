package hu.tbognar76.apking;

import java.io.Serializable;

public class ApkInfo implements Serializable {
	
		private static final long serialVersionUID = -3953443257972929886L;
		
		public String Filedate = null;
		public String filename = null; // or the zip filename
		public Long filesize = null;
		public String fullpath = null;
		public String packname = null;
		public String name = null;
		public String version = null;
		public Long versioncode = null;
		boolean isObb = false;
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(this.name);
			sb.append(" - ");
			sb.append(this.version);
			sb.append(" - ");
			sb.append(this.filename);
			sb.append(" - ");
			sb.append(this.packname);
			sb.append(" - ");
			sb.append(this.fullpath);
			
			return sb.toString(); 
		}
		
	}