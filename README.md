#APKing

##Having lot of APK files on your PC ? 

###Here is tool to maintain your local APK repository easily.
 
##It helps to categorise the APK files: 
You can create any custom structure of folders as a repository, where you store your APK files
You need to put the new files into the "inPath" folder, and APKking will move that file(s) into the repository using a following mechanism:

* If you already have the APK in older version it will put the new one next to it (in a same folder)
* If not, it will determine it's category from google play, and creates a folder in your repository 
* It collects the older versions of your APK files in the repository and moves them to a delete folder (for archiving)
* If you connect your phone before you run APKing it will create an update from the repository if a newer versions of the apps has been found in it.

##Good to know about it:
* Console based application, no GUI
* Easy to setup : just setup the ini file, need to create some directories, and run start.bat
* It doesn't rename any file (it stores all usage informations in a filename if you wish)
* It doesn't modify or change any files
* It doesn't delete any files
* It just move files from inPath to the collection, and from collection a to delete folder
* APKing is fast using caches (determine the APK files's meta information is slow, so it using cache files to store this information)

##Environment:
* Any OS with Java 6 or later
* Internet connection needed (connecting to google play)
* ADB connection to your phone is optional

##Prepare and run :
What you need to do is set "apking.ini" , and simply run the APKing with the start.sh or start.bat . 
There is no parameters to add. 
Optionally if you connects your phone via ADB, then it creates an update package for you. 

##Step by step what it does:
1. Moves APK files from "inPath" to a repository ("outPath")
1.a. If you have a APK file or it's or earlier version in your repository (identified by it's package name) then put the new files next to the old one, and the old one will be moved to a "deletePath" later 
1.b. If the apk is new in your repository, then APKing grab it's category from GooglePlay
2. Move old versions to a "deletePath" 
3. Check the apps at your connected phone, and copies the updates from repository to  "updatePath" folder

##Using a following libraries:
[https://jsoup.org/](https://jsoup.org/)

[https://github.com/vidstige/jadb](https://github.com/vidstige/jadb)

[https://github.com/caoqianli/apk-parser](https://github.com/caoqianli/apk-parser)

[https://commons.apache.org/proper/commons-io/](https://commons.apache.org/proper/commons-io/)


###APKing created by tbognar76. If you use my app, please respect my time and my work and buy me beer (yes, I like it). Just press a button bellow:

[![alt paypal](https://img.shields.io/badge/Donate-PayPal-green.svg "Donate me! I like beers!")](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=9STUF7VAX9RTE&lc=HU&item_name=I%20like%20beers&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted)



 
			