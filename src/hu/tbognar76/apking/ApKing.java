package hu.tbognar76.apking;

//Copyright 2016 tbognar76
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;

public class ApKing {

	private Initialization init = null;
	
	public class Cat {
		String cat1 = null;
		String cat2 = null;
	}

	public class CApkInfo {
		ComparableVersion cmp = null;
		ApkInfo apkinfo = null;
		boolean max = false;
	}

	

	// works with package name to determine the duplications
	HashMap<String, ArrayList<ApkInfo>> packageHash = null;

	// works with filename and size as cache
	HashMap<String, ApkInfo> serialInHash = null;
	HashMap<String, ApkInfo> serialOutHash = null;

	private DeviceManager dmanager = null;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// START / MAIN
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void start() {
		this.init = new Initialization();
		
		// Reads the property file
		this.init.initResources();
		if (this.init.isCopyNewerFilesToUpdatePath) {
			// UPDATE PATH MUST BE EMPTY IF SET
			File ufile = new File(this.init.updatePath);
			if (ufile.isDirectory()) {
				if (ufile.list().length > 0) {
					System.out.println("Update Directory is not empty! : " + this.init.updatePath);
					System.exit(0);
				}
			} else {
				System.out.println("This is not a directory : this.updatePath");
				System.exit(0);
			}
		}

		System.out.println("----------------------------------------------");

		// STEP1 : REFRESH THE CACHES
		refreshCachefromAPK_READY();

		// STEP 2: MANAGE IN_FOLDER
		System.out.println("----------------------------------------------");
		System.out.println("IN FOLDER PROCESSING:");
		File[] infiles = new File(this.init.inPath).listFiles();
		moveFilesFromAPK_INtoAPK_READY(infiles, true, true);

		// STEP3 : REFRESH THE CACHES AGAIN
		System.out.println("----------------------------------------------");
		refreshCachefromAPK_READY();

		// STEP4 : REPORT and MOVE TO DELETE FOLDER
		System.out.println("----------------------------------------------");
		System.out.println("REPORT AND DELETE:");
		reportDuplications(true);
		// reportDuplications(false);

		// STEP 5 : update needed on phone
		System.out.println("----------------------------------------------");
		System.out.println("PHONE TOOLS:");

		dmanager = new DeviceManager();
		dmanager.getInstalledPackagesFromPhone();
		writePhoneCache();
		// readPhoneCache();
		System.out.println("Installed packages on Phone : " + this.dmanager.apps.size());

		// reportPhoneRecognized();

		System.out.println("SAME VERSION -----------------------------------");
		reportPhoneSameVersion();
		System.out.println("NEWER IN DB ------------------------------------");
		reportPhoneNewerInDB();
		System.out.println("NEWER ON PHONE----------------------------------");
		reportPhoneNEWEROnPhone();

		// STEP 6 : create catalog
		if (this.init.isCatalogGenerated){
			createCatalog();
		}
		
		
		System.out.println("----------------------------------------------");
		System.out.println("READY!!!!!!!!!!!");
	}

	private void reportPhoneNewerInDB() {
		ArrayList<String> log = new ArrayList<String>();
		for (DeviceApp app : this.dmanager.apps) {
			// HashMap<String, ArrayList<ApkInfo>> packageHash = null;
			if (this.packageHash.get(app.packageName) != null) {
				// System.out.println(app.packageName);
				ArrayList<ApkInfo> aai = this.packageHash.get(app.packageName);
				for (ApkInfo ai : aai) {
					if (new ComparableVersion(ai.version).compareTo(new ComparableVersion(app.versionName)) > 0) {
						String line = concatWithPos(concatWithPos(ai.name, ai.version, 35), "on phone: "
								+ app.versionName, 50);
						log.add(line);
						// ai.fullpath
						if (this.init.isCopyNewerFilesToUpdatePath) {
							Path sourcePath = Paths.get(ai.fullpath);
							Path destinationPath = Paths.get(this.init.updatePath + ai.filename);
							try {
								Files.copy(sourcePath, destinationPath);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
				}
			}
		}

		Collections.sort(log, (p1, p2) -> p1.compareTo(p2));
		for (String line : log) {
			System.out.println(line);
		}

	}

	private void reportPhoneNEWEROnPhone() {
		ArrayList<String> log = new ArrayList<String>();
		for (DeviceApp app : this.dmanager.apps) {
			// HashMap<String, ArrayList<ApkInfo>> packageHash = null;
			if (this.packageHash.get(app.packageName) != null) {
				// System.out.println(app.packageName);
				ArrayList<ApkInfo> aai = this.packageHash.get(app.packageName);
				for (ApkInfo ai : aai) {
					if (new ComparableVersion(ai.version).compareTo(new ComparableVersion(app.versionName)) < 0) {
						String line = concatWithPos(concatWithPos(ai.name, ai.version, 35), "on phone: "
								+ app.versionName, 50);
						log.add(line);
					}
				}
			}
		}

		Collections.sort(log, (p1, p2) -> p1.compareTo(p2));
		for (String line : log) {
			System.out.println(line);
		}

	}

	private void reportPhoneSameVersion() {
		ArrayList<String> log = new ArrayList<String>();
		for (DeviceApp app : this.dmanager.apps) {
			// HashMap<String, ArrayList<ApkInfo>> packageHash = null;
			if (this.packageHash.get(app.packageName) != null) {
				// System.out.println(app.packageName);
				ArrayList<ApkInfo> aai = this.packageHash.get(app.packageName);
				for (ApkInfo ai : aai) {
					if (new ComparableVersion(ai.version).compareTo(new ComparableVersion(app.versionName)) == 0) {
						String line = concatWithPos(concatWithPos(ai.name, ai.version, 35), "on phone: "
								+ app.versionName, 50);
						log.add(line);
					}
				}
			}
		}

		Collections.sort(log, (p1, p2) -> p1.compareTo(p2));
		for (String line : log) {
			// System.out.println(line);
		}
		System.out.println("Number of: " + log.size());

	}

	private void reportPhoneRecognized() {
		for (DeviceApp app : this.dmanager.apps) {
			// HashMap<String, ArrayList<ApkInfo>> packageHash = null;
			if (this.packageHash.get(app.packageName) != null) {
				ArrayList<ApkInfo> aai = this.packageHash.get(app.packageName);
				for (ApkInfo ai : aai) {
					System.out.println(concatWithPos(concatWithPos(ai.name, ai.version, 35), "on phone: "
							+ app.versionName, 50));
				}
			}
		}
	}

	private void refreshCachefromAPK_READY() {
		File[] files = new File(this.init.outPath).listFiles();
		// REREAD THE CACHES
		// Init VARIABLES
		this.packageHash = new HashMap<String, ArrayList<ApkInfo>>();
		this.serialInHash = new HashMap<String, ApkInfo>();
		this.serialOutHash = new HashMap<String, ApkInfo>();
		// Loads the cache, fills the "serialInHash"
		readInCache();
		// Update structures from APK_READY
		// Using INCACHE
		// Filling OUTCACHE
		// Filling packageHash group by package

		updateHashFromAPK_READY(files);
		// Writes down the changes
		writeOutCache();
		System.out.println("Cache refreshed! (IN:" + this.serialInHash.size() + " OUT:" + this.serialOutHash.size()	+ ")");

	}

	

	// REPORTS from "packageHash"
	// DELETES OLD VERSIONS IF PARAMETER "TRUE"
	private void reportDuplications(boolean toDelete) {

		for (Map.Entry<String, ArrayList<ApkInfo>> entry : this.packageHash.entrySet()) {
			String key = entry.getKey();
			ArrayList<ApkInfo> value = entry.getValue();
			if (value.size() > 1) {
				ArrayList<CApkInfo> cvalue = new ArrayList<ApKing.CApkInfo>();
				System.out.println("");

				List<Version> versions = new ArrayList<Version>();

				boolean versionerror = false;
				boolean versionerror2 = false;

				for (ApkInfo ai : value) {

					CApkInfo cai = new CApkInfo();

					cai.apkinfo = ai;

					// System.out.println(ai.fullpath + "     " + ai.version);
					try {
						versions.add(new Version(ai.version));

					} catch (Exception e) {
						versionerror = true;
					}

					try {
						cai.cmp = new ComparableVersion(ai.version);
					} catch (Exception e) {
						versionerror2 = true;
					}
					cvalue.add(cai);

				}

				if (versionerror2) {
					System.out.println("BAD VERSION FORMAT 2");
					for (ApkInfo ais : value) {
						System.out.println("#del " + ais.fullpath);
					}

				} else {
					CApkInfo maxi = Collections.max(cvalue, new Comparator<CApkInfo>() {
						@Override
						public int compare(CApkInfo left, CApkInfo right) {
							return left.cmp.compareTo(right.cmp);
						}
					});
					maxi.max = true;

					String warning = "";
					if (versionerror || versionerror2) {
						warning = "WARNING!! ";
					}

					String vers = "";
					for (CApkInfo aic : cvalue) {
						if (aic.max) {
							vers = vers + "-->" + aic.apkinfo.version + "<-- , ";
						} else {
							vers = vers + aic.apkinfo.version + " , ";
						}
					}
					System.out.println("::" + warning + maxi.apkinfo.name + " // " + vers);

					for (CApkInfo aic : cvalue) {
						String max = "";
						if (aic.max) {
							max = "rem #";
							// A LEGUJABB
							System.out.println("!OK " + aic.apkinfo.fullpath);

						} else {
							// REGEBBI , TOROLNI KELL (MOVE TO DELETEFOLDER)
							System.out.println("del " + aic.apkinfo.fullpath);

							if (toDelete) {
								String renamedfile = this.init.deletePath + "/"
										+ FilenameUtils.getName(aic.apkinfo.fullpath);
								File file = new File(aic.apkinfo.fullpath);
								if (file.renameTo(new File(renamedfile))) {

								} else {
									System.out.println("File is failed to move!" + renamedfile);
								}
							}
						}

					}

				}
				// System.out.println("MAX: " + maxi.apkinfo.version +
				// "   "+maxi.apkinfo.filename);

			}
			// do stuff
		}

	}



	// USES packageHash
	private void moveFilesFromAPK_INtoAPK_READY(File[] files, boolean isSamePackageFeature,
			boolean isGooglePlayCategoryFeauture) {
		for (File file : files) {
			if (file.isDirectory()) {
				// System.out.println("Directory: " + file.getName());
				moveFilesFromAPK_INtoAPK_READY(file.listFiles(), isSamePackageFeature, isGooglePlayCategoryFeauture); // Calls
																														// same
				// method again.
			} else {
				// System.out.println("File: " + file.getName());
				// System.out.println("File: " + file.getPath());

				// it will put the new APK in a same folder where the old one
				// was (in a same folder) (by package name)
				ApkInfo apkinfo = null;
				ApkMeta apkMeta = null;
				apkMeta = getPackage(file.getPath());

				if (isSamePackageFeature) {
					if (packageHash.containsKey(apkMeta.getPackageName())) {
						// FOUND
						ArrayList<ApkInfo> apkinfos = packageHash.get(apkMeta.getPackageName());
						// If more then the select the first
						apkinfo = apkinfos.get(0);
						// }
						// apkinfo.fullpath

						String pathOLD = FilenameUtils.getFullPath(apkinfo.fullpath);
						String filenameOLD = FilenameUtils.getName(apkinfo.fullpath);
						String filenameNEW = FilenameUtils.getName(file.getPath());
						if (filenameOLD.equals(filenameNEW)) {
							filenameNEW = FilenameUtils.getBaseName(file.getPath()) + "_(new)."
									+ FilenameUtils.getExtension(file.getPath());
						}

						if (this.init.isMoveFromIN) {
							if (file.renameTo(new File(pathOLD + filenameNEW))) {

							} else {
								System.out.println("File is failed to move next to its prev version! " + pathOLD
										+ filenameNEW);
							}
						}
						System.out.println("Moved " + concatWithPos(filenameNEW, " to " + pathOLD, 30));
					}
				}

				if (isGooglePlayCategoryFeauture && apkinfo == null) {

					Cat cc = new Cat();

					if (apkMeta != null) {
						String packname = apkMeta.getPackageName();
						// System.out.println(file.getName()+"        "+packname);
						if (packname == null || packname.equals("")) {
							System.out.println("ERROR PASING PACKAGE: " + file.getName());
							cc.cat1 = "Unknown";
							cc.cat2 = "Unknown Package";
						} else {
							//
							// Google Play parser call!
							//
							cc = getCategoryFromGooglePlayStore(packname);
						}
					} else {
						System.out.println("ERROR PARSING PACKAGE 2: " + file.getName());
						cc.cat1 = "Unknown";
						cc.cat2 = "Unknown Package";
					}

					String renamedfile = this.init.outPath + "/" + cc.cat1 + "/" + cc.cat2 + "/" + file.getName();
					String renamedpath1 = this.init.outPath + "/" + cc.cat1 + "/";
					String renamedpath2 = this.init.outPath + "/" + cc.cat1 + "/" + cc.cat2 + "/";

					// create dirs move files
					if (this.init.isMoveFromIN) {
						makeDir(renamedpath1);
						makeDir(renamedpath2);

						if (file.renameTo(new File(renamedfile))) {

						} else {
							System.out.println("File is failed to move!" + renamedfile);
						}
					}
					System.out.println(concatWithPos(concatWithPos(cc.cat1, cc.cat2, 12), file.getName(), 40));
				}
			}
		}
	}

	// List all files in APK_READY
	// Using INCACHE
	// Filling OUTCACHE
	// Filling packageHash group by package
	private void updateHashFromAPK_READY(File[] files) {
		
		for (File file : files) {
			if (file.isDirectory()) {
				// System.out.println("Directory: " + file.getName());
				updateHashFromAPK_READY(file.listFiles()); // Calls same method
															// again.
			} else {
				// System.out.println("File: " + file.getName());
				// System.out.println("File: " + file.getPath());

				ApkInfo ai = null;
				if (!isInCache(file)) {
					ai = getApkInfo(file);
					addToOutCache(ai);
				} else {
					ai = getFromCache(file);
				}

				if (packageHash.containsKey(ai.packname)) {
					// if we have
					packageHash.get(ai.packname).add(ai);
				} else {
					ArrayList<ApkInfo> l = new ArrayList<ApkInfo>();
					l.add(ai);
					packageHash.put(ai.packname, l);
				}
							
				/*
				 * if (apkMeta != null) { String packname =
				 * apkMeta.getPackageName(); //
				 * System.out.println(file.getName()+"        "+packname); if
				 * (packname == null || packname.equals("")) {
				 * System.out.println("ERROR PASING PACKAGE: " +
				 * file.getName()); } else { hm.get(packname) } } else {
				 * System.out.println("ERROR PARSING PACKAGE 2: " +
				 * file.getName());
				 * 
				 * }
				 * 
				 * String renamedfile = outPath + "/" + cc.cat1 + "/" + cc.cat2
				 * + "/" + file.getName(); String renamedpath1 = outPath + "/" +
				 * cc.cat1 + "/"; String renamedpath2 = outPath + "/" + cc.cat1
				 * + "/" + cc.cat2 + "/";
				 */

				// System.out.println(addStr(
				// addStr(cc.cat1,cc.cat2,12),file.getName(),40));
			}
		}
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

	private Cat getCategoryFromGooglePlayStore(String packageName) {
		Cat cc = new Cat();
		cc.cat1 = "Unknown";
		cc.cat2 = "Unknown";

		Document doc = null;
		try {
			doc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + URI.create(packageName) + "&hl=en")
					.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return cc;
		}
		// <span itemprop="genre">Életstílus</span>

		/*
		 * Elements link = doc.select(".document-subtitle category"); String
		 * linkHref = link.attr("href"); // "http://example.com/" String
		 * linkText = link.text(); // "example""
		 */

		Elements genres = doc.select("span[itemprop=genre]");
		cc.cat2 = genres.first().text();
		/*
		 * for (Element e : genres) { // System.out.println(e.text()); if
		 * (!out.equals("")) { out = out + " "; } out = out + e.text();
		 * 
		 * }
		 */

		// <div class="content" itemprop="softwareVersion"> 2.6.9.0 </div>

		Elements versions = doc.select("div[itemprop=softwareVersion]");
		// System.out.println(versions.first().text());

		// <a class="document-subtitle category"
		// href="/store/apps/category/GAME_ADVENTURE"> <span
		// itemprop="genre">Kalandjátékok</span> </a>

		Elements maincat = doc.getElementsByClass("category");

		if (maincat != null) {
			Element p = maincat.first();
			if (p != null) {
				String href = maincat.attr("href");
				if (href != null) {

					if ((href.lastIndexOf("GAME") != -1) || (href.lastIndexOf("FAMILY") != -1)) {
						cc.cat1 = "Game";
					} else {
						cc.cat1 = "Application";
					}

				}
				// cc.cat1 = maincat.attr("href");
			}
		}

		// <img alt="PEGI 3" class="document-subtitle content-rating-badge"
		// src="//lpfw=h28">
		// <span class="document-subtitle content-rating-title">PEGI 3</span>
		Elements pegi = doc.getElementsByClass("content-rating-title");
		if (pegi != null) {
			Element p = pegi.first();
			if (p != null) {
				// cc.cat1 = p.text();

			}
		}

		return cc;
	}

	private ApkMeta getPackage(String filePath) {
		ApkParser apkParser = null;
		try {
			apkParser = new ApkParser(new File(filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// set a locale to translate resource tag into specific strings in
		// language the locale specified, you set locale to Locale.ENGLISH then
		// get apk title 'WeChat' instead of '@string/app_name' for example
		Locale locale = Locale.ENGLISH;
		apkParser.setPreferredLocale(locale);

		/*
		 * String xml = null; try { xml = apkParser.getManifestXml(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } System.out.println(xml);
		 */

		// String xml2 = apkParser.transBinaryXml(xmlPathInApk);
		// System.out.println(xml2);

		ApkMeta apkMeta = null;
		try {
			apkMeta = apkParser.getApkMeta();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
		// System.out.println(apkMeta);

		/*
		 * Set<Locale> locales = null; try { locales = apkParser.getLocales(); }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } for (Locale l : locales) {
		 * System.out.println(l); }
		 */

		try {
			apkParser.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return apkMeta;

	}

	private ApkInfo getApkInfo(File file) {
		String filePath = file.getPath();
		ApkInfo ai = new ApkInfo();
		ApkParser apkParser = null;
		try {
			apkParser = new ApkParser(new File(filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: " + filePath);
			e.printStackTrace();
		}
		// set a locale to translate resource tag into specific strings in
		// language the locale specified, you set locale to Locale.ENGLISH then
		// get apk title 'WeChat' instead of '@string/app_name' for example
		Locale locale = Locale.ENGLISH;
		apkParser.setPreferredLocale(locale);

		/*
		 * String xml = null; try { xml = apkParser.getManifestXml(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } System.out.println(xml);
		 */

		// String xml2 = apkParser.transBinaryXml(xmlPathInApk);
		// System.out.println(xml2);

		ApkMeta apkMeta = null;
		try {
			apkMeta = apkParser.getApkMeta();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}

		ai.packname = apkMeta.getPackageName();
		ai.version = apkMeta.getVersionName();
		ai.versioncode = apkMeta.getVersionCode();
		ai.name = apkMeta.getName();

		ai.filename = file.getName();
		ai.fullpath = file.getPath();
		ai.filesize = file.length();
		// System.out.println(apkMeta);
		// Files.getAttribute( file.getPath(), "basic:createdAt", arg2)
		/*
		 * Set<Locale> locales = null; try { locales = apkParser.getLocales(); }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } for (Locale l : locales) {
		 * System.out.println(l); }
		 */

		try {
			apkParser.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ai;

	}

	private String concatWithPos(String s1, String s2, int pos) {
		if ((s1 == null) || (s2 == null) || ("".equals(s1)) || ("".equals(s2)))
			return s1 + s2;
		String out = s1;

		for (; out.length() < pos; out = out + " ") {
		}

		out = out.substring(0, pos);
		return out + s2;
	}

	ApkInfo ai = null;

	private boolean isInCache(File file) {
		String key = file.getName() + file.length();

		return this.serialInHash.containsKey(key);
	}

	private void addToOutCache(ApkInfo ai) {
		String key = ai.filename + ai.filesize;
		if (!this.serialOutHash.containsKey(key)) {
			this.serialOutHash.put(key, ai);
		}
	}

	private ApkInfo getFromCache(File file) {
		String key = file.getName() + file.length();
		ApkInfo val = this.serialInHash.get(key);
		// if the directory has changed
		val.fullpath = file.getPath();

		this.serialOutHash.put(key, val);
		return val;
	}

	private void writeOutCache() {
		try {
			FileOutputStream fileOut = new FileOutputStream(this.init.serialCache);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.serialOutHash);
			/*
			 * for (Map.Entry<String, ApkInfo> entry :
			 * this.serialHash.entrySet()) { out.writeObject(entry.getValue());
			 * }
			 */

			out.close();
			fileOut.close();
			// System.out.println("Cache writen: " + this.serialCache +
			// " cache size: " + this.serialOutHash.size());
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	private void readInCache() {
		try {
			FileInputStream fileIn = new FileInputStream(this.init.serialCache);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.serialInHash = (HashMap<String, ApkInfo>) in.readObject();
			in.close();
			fileIn.close();
			// System.out.println("Cache loaded: " + this.serialCache +
			// " cache size: " + this.serialInHash.size());
		} catch (IOException i) {
			System.out.println("No Cache!!");
			return;
		} catch (ClassNotFoundException c) {

			c.printStackTrace();
			return;
		}

	}

	private void writePhoneCache() {
		try {
			FileOutputStream fileOut = new FileOutputStream(this.init.phoneCache);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.dmanager.apps);
			out.close();
			fileOut.close();
			// System.out.println("Cache writen: " + this.serialCache +
			// " cache size: " + this.serialOutHash.size());
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	private void readPhoneCache() {
		try {
			FileInputStream fileIn = new FileInputStream(this.init.phoneCache);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			this.dmanager.apps = (ArrayList<DeviceApp>) in.readObject();
			in.close();
			fileIn.close();
			// System.out.println("Cache loaded: " + this.serialCache +
			// " cache size: " + this.serialInHash.size());
		} catch (IOException i) {
			System.out.println("No Cache!!");
			return;
		} catch (ClassNotFoundException c) {

			c.printStackTrace();
			return;
		}

	}
	
	private void createCatalog(){
		System.out.println("----------------------------------------------");
		System.out.println("Generating catalog to : " + this.init.catalogHtml);
		File catfile = new File(this.init.catalogHtml+"/index.html");
		
		HashMap<String,String> types = new HashMap<String, String>();
	
		try{	
			FileUtils.copyFile(new File("html/noicon.png"), new File(this.init.catalogHtml+"/"+this.init.catalogPic+"noicon.png"));
			
			FileWriter fileWriter = new FileWriter(catfile);
			
			//fileWriter.write("<html><head></head><body>");
			String fhead = FileUtils.readFileToString(new File("html/catalog_head.html")); 
	        String ftail = FileUtils.readFileToString(new File("html/catalog_tail.html"));
			
			StringBuffer ftags = new StringBuffer();
			StringBuffer felements = new StringBuffer();
			
			for (Map.Entry<String, ApkInfo> entry : this.serialOutHash.entrySet()) {
				
				String key = entry.getKey();
				ApkInfo ai = entry.getValue();
				//System.out.println(value.toString());
				
				String fullpath = ai.fullpath;
				fullpath = fullpath.replace("\\", "/");
				fullpath = fullpath.replace(this.init.outPath, "");
				fullpath = fullpath.replace(ai.filename, "");
				fullpath = fullpath.replace(" ", "-");
				fullpath = fullpath.trim();
				
				for (String s : fullpath.split("/")){
					types.put(s,s);
				}

				fullpath = fullpath.replace("/", " ");
				fullpath = fullpath.trim();
				StringBuffer sb = new StringBuffer();
				
				sb.append("<div class=\"elem "+fullpath+"\">\n");
				
				String iname=ai.packname;
				if (!isLocalVersionGooglePlayImage(ai.packname)){
					iname="noicon";
				}
				
				 
				
				sb.append("<img class=\"coverimg\" src=\""+this.init.catalogPic+"/"+ iname +".png\"></img>");
				sb.append("<div class=\"name\">");
				sb.append("<a href=\"https://play.google.com/store/apps/details?id=" + URI.create(ai.packname) + "&hl=en\" target=\"_blank\" >"+ai.name+"</a>");
				sb.append(" ( <span class=\"version\">"+ ai.version+"</span>)");
				sb.append("</div>\n");
				sb.append("</div>\n");

				felements.append(sb);
			}
			
			ftags.append("<div id=\"selector\">");
			for (Map.Entry<String, String> entry : types.entrySet()) {
				
				ftags.append("<a id=\""+ entry.getKey()+ "-button\" href=\"#\" class=\"button\" >"+ entry.getKey()+ "</a>");
			}
			ftags.append("</div>");
			
			fileWriter.write(fhead);
			fileWriter.write(ftags.toString());
			fileWriter.write(felements.toString());
			fileWriter.write(ftail);
			
			fileWriter.flush();
			fileWriter.close();
		
	} catch (IOException e) {
		e.printStackTrace();
	} 
		System.out.println("Done!");
	}
	
	private boolean isLocalVersionGooglePlayImage(String packageName){
		
		String fname = this.init.catalogHtml+"/"+this.init.catalogPic+"/"+packageName+".png";
		
		if (!this.init.isCatalogPicForced) { 
			File t = new File(fname); 
			if (t.exists()){
				//System.out.println("------"+fname);
				if (t.length()>1){
					return true;
				}
				
				return false;
			} else {
				// WORK TO DO BELOW
			}
		}
		
		Document doc = null;
	
			try {
				doc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + URI.create(packageName) + "&hl=en")
						.get();

				//Joni jó, de néha nem
				//Elements img =  doc.getElementsByClass("cover-image");
				Elements img =  doc.select("div.cover-container img");
				
				String uu = "http:"+img.first().attr("src");
				uu=uu.replace("=w300", "=w120");
				URL url = new URL(uu); 
					
				FileUtils.copyURLToFile(url, new File(fname));
				
			} catch (Exception e) {
				
				try {
					FileUtils.write(new File(fname), "-");
					return false;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		
		return true;
	}
	
}
