Having lot of APK file on your PC ? 

APKing by tbognar76. Please respect my time and my work and donate me with some bucks via paypal : bognar.tamas@gmail.com

Here is tool to maintain an APK repository easily. 
It helps to categories the APK files : 
- You can create any custom structure of folders in the repository folder 
- You need just put the new files into the "inPath" folder, and APKking will put a file into the repository using a following mechanism:
- - If you already have the APK in older version it will put the new on next to it (in a same folder)
- - If not, it will determine it's category from google play, and creates a folder in a repository 
- - It collects the older versions of your APK files in the repository and moves them to a delete folder
- - If you connect your phone before you run APKing it will create an update from the repository if a newer version has been found in it.

More:
- Console based application, no GUI
- easy to setup
- It doesn't rename any file. (it can hold any usage informations in a filename)
- APKing is fast using caches (determine the APK files's meta information is slow, so it using cache files to hold these informations)

Environment:
- Java 6 or later
- Internet connection
- ADB connection with your phone
- Windows (developed in windows vista, but it may work on any environment where java available)

Prepare and run :
What you need to do is set "apking.ini" , and simply run the APKking with the sh or bat. 
There is no parameters to add. 
Optionally if you connects your phone via ADB, then it creates an update package for you. 

Step by step what it does:
1. Moves APK files from "inPath" to a repository ("outPath")
1.a. If you have a APK file or it's or earlier version in your repository (identified by it's package name) then put the new files next to the old one, and the old one will be moved to a "deletePath" later 
1.b. If the apk is new in your repository, then APKing grab it's category from GooglePlay

If you put a file to "inPath" a APKing tries to find 

In the repository folder 

Ini file setup:

// IN folder 	
inPath=C:/APK/-00-APPSET/APK_IN
// Repository folder
outPath=C:/APK/-00-APPSET/APK_READY
// Where to move the older versions
deletePath=C:/APK/-00-APPSET/APK_DELETE
// COPY files here from the repository
updatePath=C:/APK/-00-APPSET/APK_UPDATE/
// Where to create and use the cache files
serialCache=C:/APK/-00-APPSET/cacheAPKKING_APK_READY.ser
phoneCache=C:/APK/-00-APPSET/cacheAPKKING_ADB_PHONE.ser
// Some more switches
isMoveFromIN=true
isMoveToDeleteFolder=true
isCopyNewerFilesToUpdatePath=true


Using a following librarys:
https://jsoup.org/
https://github.com/vidstige/jadb
https://github.com/caoqianli/apk-parser
https://commons.apache.org/proper/commons-io/


			