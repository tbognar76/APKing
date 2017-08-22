package hu.tbognar76.apking;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.*;

import org.apache.commons.io.FilenameUtils;

public class ZipTools {

	String filename = "c:/eclipse/workspace/Zip/MAPS.ME-6.5.2-Google-arm.zip";

	static Integer ZIP_BAD = 0;
	static Integer ZIP_GOOD = 1;
	static Integer ZIP_WITH_TRASH = 2;
	
	public void check(){
		System.out.println("RESULT: " + chechkZip(this.filename));
	}
	
	
	public Integer chechkZip(String filename) {
		ArrayList<ZipEntry> ZipEntryList = new ArrayList<ZipEntry>();
		Integer out= ZIP_GOOD;
		boolean isbad = false;
		boolean istrash = false;
		boolean isgood = true;
		
		try {
			final int BUFFER = 2048;
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(this.filename);
			CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				//System.out.println("Extracting: " + entry.getName() + "  "+ entry.getSize() + "  "+ entry.isDirectory() + "  " );
				int count;
				byte data[] = new byte[BUFFER];
				// write the files to the disk
				
				ZipEntryList.add(entry);
				
				/*
				System.out.print(FilenameUtils.getName(entry.getName()));
				System.out.print("  ");
				System.out.print(FilenameUtils.getExtension(entry.getName()));
				System.out.print("  ");
				System.out.print(FilenameUtils.getPath(entry.getName()));
				*/
				
				System.out.println("  ");
				// OBBZIP specification: 
				// rule 1: APK file on root
				//    BAD: no apk files in the root
				//    GOOD: only "apk" files in the root,
                //    WITH_TRASH : apk and more files  

				// rule 2: OBB directory must exist
				//    BAD:  No directory from the root
				//    Good: Only 1 directory with (. on it's name)
			    //    TRASH: multiple directory 

				// rule 3: OBB file in the directory must exist
				//    BAD: no obb files
				//    GOOD: just obb files
				//    TRASH: more files than obb
				
				
				 
				
				/*
				FileOutputStream fos = new FileOutputStream(entry.getName());
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
				*/
			}
			zis.close();
			//System.out.println("Checksum:" + checksum.getChecksum().getValue());
			
			//process rules
			
			
			int dircount = 0; 
			int apkcount = 0;
			int obbcount = 0;
			int trashcount = 0;
			for (ZipEntry ze:ZipEntryList){
				if (ze.isDirectory()){
					dircount ++;
					continue;
				} else {
					
					//if the file is apk
					if (FilenameUtils.getExtension(ze.getName()).toLowerCase().equals("apk")){
						//has to be in the root
						if (FilenameUtils.getPath(ze.getName()).equals("")){
							apkcount++;
							continue;
						} else {
							//trash is not in the root
							trashcount++;
							continue;
						}
					}
					
					//if the file is obb
					if (FilenameUtils.getExtension(ze.getName()).toLowerCase().equals("obb")){
						//has to be in directory
						if (!FilenameUtils.getPath(ze.getName()).equals("")){	
							obbcount++;
							continue;
						} else {
							//trash if in the root
							trashcount++;
							continue;
						}	
					}
					trashcount++;
				}
			}
			
			if (dircount==0) {
				isbad = true;
			}
			if (dircount>1) {
				istrash = true;
			}
			
			if (apkcount==0){
				isbad = true;
			}
			
			if (obbcount==0){
				isbad = true;
			}
			
			if (trashcount != 0) {
				istrash = true;
			}
			
			if (istrash && isgood){
				out = ZIP_WITH_TRASH;
			}
			
			if (isbad){
				out = ZIP_BAD;
			}
			
			System.out.print(dircount + " " + apkcount  + " " +  obbcount  + " " + trashcount + " " ); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}

}
