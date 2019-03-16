/*
 * PermissionHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */

package reaper.PermissionHarvester;

import android.app.AndroidAppHelper;
import android.content.ContentResolver;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


public class hookClassMethods extends PermissionHarvester {

    public static String makeSHA1Hash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes("UTF-8");
        md.update(buffer);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
        }
        return hexStr;
    }
    public static String hashStacktrace(StackTraceElement[] tmp) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String tmpHash = null;
        String hash = null;
        for (int i = 0; i < tmp.length; i++) {
            tmpHash += tmp[i].toString();
        }
        hash = makeSHA1Hash(tmpHash);
        return hash;
    }
    public static String print_Permission_Result(int result) {
        if (result == 0)
            return "GRANTED";
        else if (result == -1)
            return "DENIED";
        else
            return "Unknown Option: [" + result + "]";
    }
    public static String[] getStacktrace(StackTraceElement[] tmp, String method) {
        String completeStack[] = new String[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            completeStack[i] = (" Reaper StackTrace[" + i + "]["+ method +"]: " + tmp[i]);
        }
        return completeStack;
    }
    public static String[] concat(String[] s1, String[] s2) {
        String[] erg = new String[s1.length + s2.length];

        System.arraycopy(s1, 0, erg, 0, s1.length);
        System.arraycopy(s2, 0, erg, s1.length, s2.length);

        return erg;
    }
    public static void print(String[] args) {
        String nl = "\n";
        String complete = "";
        for (String tmp : args) {
            if (tmp==null)
                continue;
            complete += (tmp);
            complete += (nl);

        }
        System.out.flush();
        XposedBridge.log(complete);
        System.out.flush();
    }
    public static void generalHook(final String ClassName, final String FunctionCall) throws ClassNotFoundException {
        try {
            final Class<?> hookedClass = Class.forName(ClassName);
            XposedBridge.hookAllMethods(hookedClass, FunctionCall, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    PermissionHarvester.pref.makeWorldReadable();
                    PermissionHarvester.pref.reload();
                    PermissionHarvester.apps = new HashSet(appList.apps);
                    int size = PermissionHarvester.pref.getInt("selectedAppsSize", 0);
                    String array[] = new String[size];
                    for(int i=0;i<size;i++) {
                        array[i] = PermissionHarvester.pref.getString("selectedApps_" + i, null);
                        PermissionHarvester.apps.add(array[i]);
                    }

                    String args="";
                    if (param.args!=null) {
                        args="<";
                        for (int i = 0; i < param.args.length; i++) {
                            if (i>0)
                            {
                                args=args+", ";
                            }
                            if (param.args[i]!=null)
                            {
                                args = args + param.args[i].toString() ;
                            }
                            else
                            {
                                args = args+ "null" ;
                            }
                        }
                        args = " " + args + ">";
                    }
                    else
                    {
                        args="NoArgs";
                    }
                    //print(tmp);
                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (PermissionHarvester.apps.contains(AndroidAppHelper.currentPackageName())) {
                        try {

                            int i = 0;
                            String tmp[] = new String[5];
                            tmp[i++] = ("Reaper -handleLoadPackage[" + AndroidAppHelper.currentPackageName() + "] -Class: " + hookedClass.getName() + " -Function: " + FunctionCall);
                            //print(tmp);
                            /*needed for stacktrace*/
                            tmp[i++] = ("Reaper -StackTrace[" + hookedClass.getName() + "." + FunctionCall + "][-]: " + hashStacktrace(Thread.currentThread().getStackTrace()));
                            String[] Stacktrace = getStacktrace(Thread.currentThread().getStackTrace(), hookedClass.getName() + "." + FunctionCall);
                            String[] complete = concat(tmp, Stacktrace);
                            print(complete);

                        } catch (Throwable ex) {
                            XposedBridge.log("Reaper Catch in Class: " + hookedClass.getName() + " method: " + FunctionCall);
                            System.out.flush();
                        }
                    }
                }
            });
        } catch (Throwable ex) {
            System.out.flush();
        }
    }
    public static void hookSettingsSecure() throws ClassNotFoundException    {
        XposedHelpers.findAndHookMethod("android.provider.Settings.Secure",Thread.currentThread().getContextClassLoader(),
                "getString", ContentResolver.class,String.class, new XC_MethodHook() {
            String hookedClass="android.provider.Settings.Secure";
            String FunctionCall="getString";
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                PermissionHarvester.pref.makeWorldReadable();
                PermissionHarvester.pref.reload();
                PermissionHarvester.apps = new HashSet(appList.apps);
                int size = PermissionHarvester.pref.getInt("selectedAppsSize", 0);
                String array[] = new String[size];
                for (int i = 0; i < size; i++) {
                    array[i] = PermissionHarvester.pref.getString("selectedApps_" + i, null);
                    PermissionHarvester.apps.add(array[i]);
                }
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (PermissionHarvester.apps.contains(AndroidAppHelper.currentPackageName())) {
                    try {
                        int i = 0;
                        String tmp[] = new String[5];
                        tmp[i++] = ("Reaper -handleLoadPackage[" + AndroidAppHelper.currentPackageName() + "] -Class: " + hookedClass + " -Function: " + FunctionCall);

                        /*needed for stacktrace*/
                        tmp[i++] = ("Reaper -StackTrace[" + hookedClass + "." + FunctionCall + "][-]   \t : " + hashStacktrace(Thread.currentThread().getStackTrace()));
                        String[] Stacktrace = getStacktrace(Thread.currentThread().getStackTrace(), hookedClass + "." + FunctionCall);
                        String[] complete = concat(tmp, Stacktrace);
                        print(complete);
                    } catch (Throwable ex) {
                        XposedBridge.log("Reaper Catch in Class: " + hookedClass + " method: " + FunctionCall);
                        System.out.flush();
                    }
                }
            }
        });
    }
    public static void createHooks(Set<String> tmp) throws ClassNotFoundException {
        String[] Elements;
        for (String s : tmp)
        {
            Elements = s.split(" ");
            generalHook(Elements[0],Elements[1]);
        }
    }
}
