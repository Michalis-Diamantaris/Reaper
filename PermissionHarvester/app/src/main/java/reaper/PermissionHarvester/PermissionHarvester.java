/*
 * PermissionHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */
package reaper.PermissionHarvester;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static reaper.PermissionHarvester.hookClassMethods.createHooks;

public class PermissionHarvester implements  IXposedHookZygoteInit, IXposedHookLoadPackage {

    public static Set apps = new HashSet(appList.apps);
    public static Set<String> defaultHashset = new HashSet<String>();
    public static XSharedPreferences pref ;
    public static final String PermissionHarvesterpackageName = PermissionHarvester.class.getPackage().getName();
    public static final String modulePrefs = "PermissionHarvesterPrefs";

    /******************************************************************/
    /***************Hooking Zygote*************************************/
    /******************************************************************/
    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        pref = new XSharedPreferences(PermissionHarvesterpackageName , modulePrefs);
        pref.makeWorldReadable();
        pref.reload();
        try
        {
            // Hook ActivityManagerService through ActivityThread
            Class<?> at = Class.forName("android.app.ActivityThread");

            XposedBridge.hookAllMethods(at, "systemMain", new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
                {
                    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    final Class<?> ams = Class.forName("com.android.server.am.ActivityManagerService", false, loader);

                    String permissionCheckFunctions[]={"checkPermission","checkPermissionWithToken"};
                    for (final String permissionCheckFunction : permissionCheckFunctions)
                    {
                        XposedBridge.hookAllMethods(ams, permissionCheckFunction, new XC_MethodHook()
                        {
                            String method = permissionCheckFunction;

                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable
                            {

                                Field ctx = ams.getDeclaredField("mContext");
                                ctx.setAccessible(true);
                                Context context = (Context) ctx.get(param.thisObject);
                                PackageManager pm = context.getPackageManager();
                                String packagename = pm.getNameForUid((int) param.args[2]);


                                String tmp[] = new String[5];
                                int i = 0;
                                tmp[i++] = ("Reaper -Zygote[Permission]: -" + (String) param.args[0].toString() + " -" + hookClassMethods.print_Permission_Result((int) param.getResult())+ " -" + Binder.getCallingUid() + " -" + Binder.getCallingPid()+ " -" +AndroidAppHelper.currentPackageName()+ " -" +packagename);
                                hookClassMethods.print(tmp);

                            }
                        });
                    }
                }
            });
        }
        catch(Throwable ex)
        {
            XposedBridge.log("Reaper exception in initZygote");
        }
    }
    /******************************************************************/
    /***************Hooking LoadPackage********************************/
    /******************************************************************/
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        pref =new XSharedPreferences(PermissionHarvesterpackageName ,modulePrefs);
        //pref = new XSharedPreferences("reaper.PermissionHarvester","settings");
        pref.makeWorldReadable();
        pref.reload();

        Set<String> permissionMappingsSDK = pref.getStringSet("SDK",defaultHashset);
        Set<String> permissionMappingsFramework = pref.getStringSet("Framework",defaultHashset);
        Set<String> PII = pref.getStringSet("PII",defaultHashset);
        // print sizes for debugging purposes
        XposedBridge.log("Reaper Hook["+lpparam.packageName+"]: "+String.valueOf(permissionMappingsSDK.size())+" permissionMappingsSDK.");
        XposedBridge.log("Reaper Hook["+lpparam.packageName+"]: "+String.valueOf(permissionMappingsFramework.size())+" permissionMappingsFramework.");
        XposedBridge.log("Reaper Hook["+lpparam.packageName+"]: "+String.valueOf(PII.size())+" PII.");
        //Create the Hooks
        //for the SDK
        createHooks(permissionMappingsSDK);
        //for the Framework
        createHooks(permissionMappingsFramework);
        //for PII without permission
        createHooks(PII);
        //Hook other PII that cannot be hooked using the general method
        hookClassMethods.hookSettingsSecure();

    }
}
