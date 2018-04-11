package hu.tbognar76.apking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CreateCatalogToolTester {

	public void start(){
		
		String packageName = "com.yodo1.rodeo.safari";
		System.out.println("Resource Download Started!");
		
		getResourceFromGooglePlayStoreReal(packageName);
		//getResourceFromGooglePlayStore(packageName);
	}
	
	
	private void getResourceFromGooglePlayStoreReal(String packageName){
		String outputFolder="d:/eclipse/git/APKing/out/";
		String name=packageName+".png";
		//FileOutputStream out = (new FileOutputStream(new java.io.File(outputFolder + name)));
		
		Document doc = null;
		try {
			doc = Jsoup.connect("https://play.google.com/store/apps/details?id=" + URI.create(packageName) + "&hl=en")
					.get();
			
			
			//Elements img = doc.getElementsByTag("img");
			
			Elements img =  doc.getElementsByClass("cover-image");
	        
			//Elements img2 = doc.select("img.cover-image");
			//System.out.println("KEP: "+   img.first().attr("src"));
			
			URL url = new URL("http:"+img.first().attr("src")); 
					
			FileUtils.copyURLToFile(url, new File(outputFolder+name));
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return;
		}
		
		
		
		
		/*
		Elements pic = doc.getElementsByClass("img.cover-image");
		if (pic != null) {
			Element p = pic.first();
			if (p != null) {
				//out.write(p.baseUri());
			}
		}
		*/
		//out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
		//out.close();
		
	}
	
	
	private void getImages(String src) throws IOException {

	    int indexName = src.lastIndexOf("/");

	    if (indexName == src.length()) {
	        src = src.substring(1, indexName);
	    }

	    indexName = src.lastIndexOf("/");
	    String name = src.substring(indexName, src.length());

	    System.out.println(name);
	}
	
	public class Cat {
		String cat1 = null;
		String cat2 = null;
	}
	private void getResourceFromGooglePlayStore(String packageName) {
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
			return ;
		}
		// <span itemprop="genre">Eletstilus</span>

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

		
		System.out.println(cc.cat1 + "   " + cc.cat2);
		
		return ;
	}
	
	
	
	
	
}
