package hu.tbognar76.apking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;

public class Initialization {

	
	// VERSION
	static String inifile = "./apking.ini";
		
	public String inPath = null;
	public String outPath = null;
	public String deletePath = null;
	public String updatePath = null;
	public String serialCache = null;
	public String phoneCache = null;
	public String catalogHtml = null;
	public String catalogPic = null;
	public boolean isCatalogGenerated = true;
	public boolean isCatalogPicForced = true;
	public boolean isMoveFromIN = false;
	public boolean isMoveToDeleteFolder = false;
	public boolean isCopyNewerFilesToUpdatePath = false;

	
	public void initResources() {
		PropertyResourceBundle rb = getRescource();

		this.inPath = rb.getString("inPath");
		this.outPath = rb.getString("outPath");
		this.deletePath = rb.getString("deletePath");
		this.serialCache = rb.getString("serialCache");
		this.updatePath = rb.getString("updatePath");
		this.phoneCache = rb.getString("phoneCache");
		this.catalogHtml = rb.getString("catalogHtml");
		this.catalogPic = rb.getString("catalogPic");
		
		this.isCatalogGenerated = rb.getString("isCatalogGenerated").equals("true");
		this.isCatalogPicForced = rb.getString("isCatalogPicForced").equals("true");
		this.isMoveFromIN = rb.getString("isMoveFromIN").equals("true");
		this.isMoveToDeleteFolder = rb.getString("isMoveToDeleteFolder").equals("true");
		this.isCopyNewerFilesToUpdatePath = rb.getString("isCopyNewerFilesToUpdatePath").equals("true");

		makeDir(inPath);
		makeDir(outPath);
		makeDir(deletePath);
		makeDir(updatePath);

		System.out.println("IN FOLDER ROOT   : " + this.inPath);
		System.out.println("OUT FOLDER ROOT  : " + this.outPath);
		System.out.println("DELETE FOLDER    : " + this.deletePath);
		System.out.println("UPDATE OUT DIR   : " + this.updatePath);
		System.out.println("Serial Cache     : " + this.serialCache);
		System.out.println("Phone Cache      : " + this.phoneCache);
		System.out.println("Move from IN     : " + this.isMoveFromIN);
		System.out.println("Move to Delete   : " + this.isMoveToDeleteFolder);
		System.out.println("Copy to UPDATE   : " + this.isCopyNewerFilesToUpdatePath);
		
		System.out.println("Catalog HTML     : " + this.catalogHtml);
		System.out.println("Catalog Pic      : " + this.catalogPic);
		System.out.println("Catalog Generated: " + this.isCatalogGenerated);
		System.out.println("Catalog Pic Force: " + this.isCatalogPicForced);
		
	}
	
	private PropertyResourceBundle getRescource() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(inifile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return new PropertyResourceBundle(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;

	}
	
	private void makeDir(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			if (dir.mkdir()) {
				// System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
	}
}
