Reaper is a dynamic analysis system that traces the permissions requested by apps in real time and distinguishes those requested by the app's core functionality from those requested by third-party libraries linked with the app.

The core functionality of Reaper consists of two Xposed modules:
reaper.PermissionHarvester
reaper.UIHarvester

The modules provided in this repo are slightly modified from the ones used in the original experiments in order to be more user friendly.
Moreover, elements captured by UIHarvester contain more features such as "Text size", "color", "Resource Id", "Tag", etc. 

Prerequisites: Xposed Framework

How to install:

reaper.PermissionHarvester

Download the module,compile and build the apk.
1)install the module (do not activate yet and do not reboot)
2)adb shell su chmod 711 /data/data/reaper.PermissionHarvester
3)Open the PermissionHarvester app activity 
4)adb shell su chmod 664 /data/data/reaper.PermissionHarvester/shared_prefs/PermissionHarvesterPrefs.xml
5)Activate the module
6)Reboot 


reaper.UIHarvester

Download the module,compile and build the apk.
1)Install the module (do not activate yet and do not reboot)
2)adb shell su chmod 711 /data/data/reaper.UIHarvester
3)Open the UIHarvester app activity 
4)adb shell su chmod 664 /data/data/reaper.UIHarvester/shared_prefs/UIHarvesterPrefs.xml
5)Activate the module
6)Reboot 

These steps are necessary because of the SELinux policy and in order to activate the shared preferences used in both modules.


Logcat Output

Both modules output all the information to logcat. 
For easiest parsing of the results in UIHarvester the information for every element can also be exported to logcat in an encoded base64 form. 

Example:

The encoded string from the logcat 
encoded="UIHarvesterT2JqZWN0IGdldENsYXNzOiBjbGFzcyBhbmRyb2lkLnN1cHBvcnQudjcud2lkZ2V0LkFwcENvbXBhdFRleHRWaWV3CmN1cnJlbnRQYWNrYWdlTmFtZTogY29tLmltZGIubW9iaWxlCmlzQ2xpY2thYmxlOiAwCmlzQ29udGV4dENsaWNrYWJsZTogMApoYXNPbkNsaWNrTGlzdGVuZXJzOiAwCmlzTG9uZ0NsaWNrYWJsZTogMAppc1ByZXNzZWQ6IDAKaXNGb2N1c2FibGU6IDAKaXNFbmFibGVkOiAxCmdldEltcG9ydGFudEZvckFjY2Vzc2liaWxpdHk6IDEKaXNTaG93bjogMQpoYXNXaW5kb3dGb2N1czogMQpwYXJlbnRDbGlja2FibGU6IDEKRGlzcGxheVNpemU6IDEwODAgMTc5NApDb29yZHM6IDU3IDY1ODYKTG9jYXRpb246IERvd25MZWZ0CmdldFRhZzogbnVsbApSZXNvdXJjZUlkOiBzdHJpbmcKVGV4dDogVGhlIExlZ28gTW92aWUgMjogVGhlIFNlY29uZCBQYXJ0CmdldFRleHRTaXplOiAzNy4wCmdldFR5cGVmYWNlOiAwClRleHRMZW5ndGg6IDMzCmdldEN1cnJlbnRUZXh0Q29sb3I6ICNGRkZGRkYKZ2V0V2lkdGg6IDMyNApnZXRIZWlnaHQ6IDEwMwo="

can be easily decoded using python's base64 decode function


print encoded.replace("UIHarvester","").decode('base64')

Both tools output a lot of information to the logcat. It is wise to change the logcat buffer to a bigger value.

Paper
For technical details and evaluation results, please refer to our publication:
REAPER: Real-time App Analysis for Augmenting the Android Permission System
http://www.reaper.gr/data/codas049-diamantarisA.pdf


Designed by Michalis Diamantaris and Jason Polakis, implemented by Michalis Diamantaris.
