package hu.tbognar76.apking;

public class Starter {

	public static void main(String[] args) {
		System.out.println("-----------------------------------------------------");
		System.out.println("APKing created by tbognar76. ");
		System.out.println("If you use my app, please respect my time and my work and buy me beer ");
		System.out.println("(yes, I like it) :)");
		System.out.println("Via paypal : bognar.tamas@gmail.com"); 
		System.out.println("-----------------------------------------------------");
		System.out.println(" ");
		System.out.println("-APKing STARTED--------------------------------------");
		
		
		ApKing apkking = new ApKing();
		apkking.start();
		
		//ZipTools zipTools = new ZipTools();
		//zipTools.check();
		
		//ResourceDownloadTool rdt = new ResourceDownloadTool();
		//rdt.start();
		
	}
	
}
