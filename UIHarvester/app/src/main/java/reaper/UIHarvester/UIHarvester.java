/*
 * UIHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */
package reaper.UIHarvester;

import android.annotation.SuppressLint;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class UIHarvester extends XC_MethodHook implements  IXposedHookZygoteInit, IXposedHookLoadPackage {
    public String TagBase64="UIHarvester";
    public String Tag="";
    public int boolToInt(Boolean tmp) {
        return tmp.compareTo(false);
    }
    public String[] concat(String[] s1, String[] s2) {
        String[] erg = new String[s1.length + s2.length];

        System.arraycopy(s1, 0, erg, 0, s1.length);
        System.arraycopy(s2, 0, erg, s1.length, s2.length);

        return erg;
    }
    private Point ScreenCoords(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }
    public String toBase64(String tmp){
        byte[] data = tmp.getBytes(StandardCharsets.UTF_8);
        String base64String = Base64.encodeToString(data, Base64.DEFAULT);
        return base64String.replace("\n", "");
    }
    public Point getDisplaySize(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
    public String GetApproximateLocation(float tmp,float splitGrid,String dim){
        if (tmp / splitGrid  < 1){
            if (dim=="X")return "Left";else if (dim=="Y")return "Up";
        }
        else if ( (tmp / splitGrid >= 1) && ( tmp/splitGrid < 2) ) {
            return "Center";
        }
        else if ( (tmp/splitGrid >= 2)){
            if (dim=="X")return "Right";else if (dim=="Y")return "Down";
        }
        return "ERROR";
    }
    private String getLocation(float x, float y, Context context) {
        String location;
        String locationX;
        String locationY;
        //Get Display Size
        Point size = getDisplaySize(context);
        int displayX = size.x;
        int displayY = size.y;

        locationX=GetApproximateLocation(x,displayX/3,"X");
        locationY=GetApproximateLocation(y,displayY/3,"Y");
        location=locationY+locationX;
        return location;
    }
    public void printing(String[] args) {
        String nl = "\n";
        String complete = "";
        for (String tmp : args) {
            if (tmp != null) {
                complete += (tmp);
                complete += (nl);
            }
        }
        if (complete!="") {
            prefs.makeWorldReadable();
            prefs.reload();
            int logcatOutputPlain = prefs.getInt("logcatOutputPlain", 0);
            int logcatOutputBase64 = prefs.getInt("logcatOutputBase64", 0);

            if (logcatOutputPlain==1)
            {
                XposedBridge.log(complete);
            }
            if (logcatOutputBase64==1)
            {
                XposedBridge.log(TagBase64 + toBase64(complete));
            }
            System.out.flush();
        }
    }
    public int checkParentClickable(View view){
        int clickable=0;
        while(view != null){
            if(view.getParent() instanceof View) {
                view = (View) view.getParent();
            }
            else{
                view = null;
            }
            if (view!=null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view.isContextClickable() ==true)
                {
                    clickable=1;
                }
                if ( view.hasOnClickListeners()==true ||
                        view.isClickable()==true ||
                        view.isLongClickable()==true ||
                        view.isFocusable()==true ||
                        view.isPressed()==true )
                {
                    clickable=1;
                }
            }
        }
        return clickable;
    }
    public String[] checkIfScrollable(View view){
        String tmp[] = new String[15];int i = 0;
        Point Spoint = ScreenCoords(view);
        //tmp[i++] = (Tag+"#####################After###########################");
        while(view != null){
            if(view.getParent() instanceof View) {
                view = (View) view.getParent();
            }
            else{
                view = null;
            }

            if(view instanceof ListView){
                tmp[i++] = (Tag+"Parent is: ListView");
                break;
            }
            else if(view instanceof ScrollView){
                tmp[i++] = (Tag+"Parent is: ScrollView");
                break;
            }
            else if(view instanceof GridView){
                tmp[i++] = (Tag+"Parent is: GridView");
                break;
            }
            else if(view instanceof HorizontalScrollView){
                tmp[i++] = (Tag+"Parent is: HorizontalScrollView");
                break;
            }
        }
        if (view != null) {
            tmp[i++] = (Tag+"Parent ScrollHorizLeft: " + (view.canScrollHorizontally(-1)));
            tmp[i++] = (Tag+"Parent ScrollHorizRight: " + (view.canScrollHorizontally(1)));
            tmp[i++] = (Tag+"Parent ScrollVerticallyUp: " + (view.canScrollVertically(-1)));
            tmp[i++] = (Tag+"Parent ScrollVerticallyDown: " + (view.canScrollVertically(1)));
            tmp[i++] = (Tag+"Parent ScrollHorizLeftCoords: " + Spoint.x+" "+Spoint.y );
            tmp[i++] = (Tag+"Parent ScrollHorizRightCoords: " + Spoint.x+" "+Spoint.y );
            tmp[i++] = (Tag+"Parent ScrollVerticallyUpCoords: " + Spoint.x+" "+Spoint.y );
            tmp[i++] = (Tag+"Parent ScrollVerticallyDownCoords: " + Spoint.x+" "+Spoint.y );
        }
        return tmp;
    }

    public void hook_onDraw() throws ClassNotFoundException {
        final Class<?> hookedClasses[] = {Class.forName("android.view.View")};
        for (final Class<?> hookedClass : hookedClasses) {
            //XposedBridge.log("hookedClasses: "+hookedClass);

            final String hookedMethod = "onDraw";

            XposedBridge.hookAllMethods(hookedClass, hookedMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    prefs.makeWorldReadable();
                    prefs.reload();
                    apps = new HashSet(appList.apps);
                    int size = prefs.getInt("selectedAppsSize", 0);
                    String array[] = new String[size];
                    for(int i=0;i<size;i++) {
                        array[i] = prefs.getString("selectedApps_" + i, null);
                        apps.add(array[i]);
                    }
                }
                @SuppressLint("WrongConstant")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (apps.contains(AndroidAppHelper.currentPackageName()))
                    {
                        //View
                        View tmpView = (View) param.thisObject;

                        //getContext
                        Field ctx = hookedClass.getDeclaredField("mContext");
                        ctx.setAccessible(true);
                        Context context = (Context) ctx.get(param.thisObject);

                        //UIHarvester Stuff
                        String tmp[] = new String[500];
                        int i = 0;

                        //Coordinates
                        Point Spoint = ScreenCoords(tmpView);

                        if (Spoint.x < 0) {
                            Spoint.x = Spoint.x * -1;
                        }
                        if (Spoint.y < 0) {
                            Spoint.y = Spoint.y * -1;
                        }
                        int Xcoord = Spoint.x + 1;
                        int Ycoord = Spoint.y + 1;
                        Point Cpoint = new Point(Spoint.x + (tmpView.getWidth() / 2), Spoint.y + (tmpView.getHeight() / 2));

                        if (tmpView.hasWindowFocus())
                        {

                            //UIHarvester
                            tmp[i++] = (Tag + "Object getClass: " + (param.thisObject.getClass().toString()));
                            tmp[i++] = (Tag + "currentPackageName: " + AndroidAppHelper.currentPackageName());

                            //True or False
                            tmp[i++] = (Tag + "isClickable: " + boolToInt(tmpView.isClickable()));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                tmp[i++] = (Tag + "isContextClickable: " + boolToInt(tmpView.isContextClickable()));
                            } else {
                                tmp[i++] = (Tag + "isContextClickable: " + 0);
                            }

                            tmp[i++] = (Tag + "hasOnClickListeners: " + boolToInt(tmpView.hasOnClickListeners()));
                            tmp[i++] = (Tag + "isLongClickable: " + boolToInt(tmpView.isLongClickable()));
                            tmp[i++] = (Tag + "isPressed: " + boolToInt(tmpView.isPressed()));
                            tmp[i++] = (Tag + "isFocusable: " + boolToInt(tmpView.isFocusable()));
                            tmp[i++] = (Tag + "isEnabled: " + boolToInt(tmpView.isEnabled()));
                            tmp[i++] = (Tag + "getImportantForAccessibility: " + tmpView.getImportantForAccessibility());
                            tmp[i++] = (Tag + "isShown: " + boolToInt(tmpView.isShown()));
                            tmp[i++] = (Tag + "hasWindowFocus: " + boolToInt(tmpView.hasWindowFocus()));
                            tmp[i++] = (Tag + "parentClickable: " + checkParentClickable((View) param.thisObject));

                            try {
                                View parent = null;
                                parent = (View) ((View) param.thisObject).getParent();
                                tmp = concat(tmp, checkIfScrollable(((View) param.thisObject)));
                            } catch (ClassCastException exc) {

                            }

                            //Coordinates and Location
                            tmp[i++] = (Tag + "DisplaySize: " + getDisplaySize(context).x + " " + getDisplaySize(context).y);
                            tmp[i++] = (Tag + "Coords: " + Xcoord + " " + Ycoord);
                            tmp[i++] = (Tag + "Location: " + getLocation(Spoint.x, Spoint.y, context));

                            //XML Resources
                            try {
                                tmp[i++] = (Tag + "getTag: " + ((View) param.thisObject).getTag());
                            } catch (Resources.NotFoundException e) {
                                tmp[i++] = (Tag + "getTag: " + "null");
                            }
                            try {
                                tmp[i++] = (Tag + "ResourceId: " + ((View) param.thisObject).getResources().getResourceEntryName(((View) param.thisObject).getId()));
                            } catch (Resources.NotFoundException e) {
                                tmp[i++] = (Tag + "ResourceId: " + "null");
                            }
                            Method[] methods = param.thisObject.getClass().getMethods();
                            int text = 0;
                            for (Method tmpObject : methods) {
                                if (tmpObject.getName() == "getText") {
                                    tmp[i++] = (Tag + "Text: " + ((TextView) param.thisObject).getText().toString().replace("\n", "").replace("\r", ""));
                                    //more features
                                    tmp[i++] = (Tag + "getTextSize: " + ((TextView) param.thisObject).getTextSize());
                                    tmp[i++] = (Tag + "getTypeface: " + ((TextView) param.thisObject).getTypeface().getStyle());
                                    tmp[i++] = (Tag + "TextLength: " + ((TextView) param.thisObject).getText().toString().replace("\n", "").replace("\r", "").length());

                                    String hexColor = String.format("#%06X", (0xFFFFFF & ((TextView) param.thisObject).getCurrentTextColor()));
                                    tmp[i++] = (Tag + "getCurrentTextColor: " + hexColor);
                                    text = 1;
                                }
                            }
                            if (text == 0) {
                                tmp[i++] = (Tag + "Text: " + "null");
                            }

                            tmp[i++] = (Tag + "getWidth: " + tmpView.getWidth());
                            tmp[i++] = (Tag + "getHeight: " + tmpView.getHeight());
                            if (tmpView.getBackground() instanceof ColorDrawable) {

                                int color = ((ColorDrawable) tmpView.getBackground()).getColor();
                                String hexColor = String.format("#%06X", color);
                                tmp[i++] = (Tag + "getColor: " + hexColor);
                            }
                            //End Features
                        }
                        printing(tmp);
                    }
                }
            });
        }
    }
    public void hook_Toast() throws ClassNotFoundException {
        final Class<?> hookedClasses[] = {Class.forName("android.widget.Toast")};
        for (final Class<?> hookedClass : hookedClasses) {
            final String hookedMethod = "makeText";
            XposedBridge.hookAllMethods(hookedClass, hookedMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    prefs.makeWorldReadable();
                    prefs.reload();
                    apps = new HashSet(appList.apps);
                    int size = prefs.getInt("selectedAppsSize", 0);
                    String array[] = new String[size];
                    for(int i=0;i<size;i++) {
                        array[i] = prefs.getString("selectedApps_" + i, null);
                        apps.add(array[i]);
                    }
                }
                @SuppressLint("WrongConstant")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (apps.contains(AndroidAppHelper.currentPackageName()))
                    {
                        //getContext
                        Context context = (Context) param.args[0];
                        //UIHarvester Stuff
                        String tmp[] = new String[50];
                        int i = 0;
                        tmp[i++] = (Tag + "Object getClass: " + hookedClass);
                        tmp[i++] = (Tag + "currentPackageName: " + AndroidAppHelper.currentPackageName());

                        tmp[i++] = (Tag + "isClickable: " + "null");
                        tmp[i++] = (Tag + "isContextClickable: " + 0);
                        tmp[i++] = (Tag + "hasOnClickListeners: " + "null");
                        tmp[i++] = (Tag + "isLongClickable: " + "null");
                        tmp[i++] = (Tag + "isPressed: " + "null");
                        tmp[i++] = (Tag + "isFocusable: " + "null");
                        tmp[i++] = (Tag + "isEnabled: " + "null");
                        tmp[i++] = (Tag + "getImportantForAccessibility: " + "null");
                        tmp[i++] = (Tag + "isShown: " + "null");
                        tmp[i++] = (Tag + "hasWindowFocus: " + "null");
                        //Coordinates and Location
                        tmp[i++] = (Tag + "DisplaySize: " + getDisplaySize(context).x + " " + getDisplaySize(context).y);
                        tmp[i++] = (Tag + "Coords: 0  0");
                        tmp[i++] = (Tag + "Location: " + "null");
                        //XML Resources
                        tmp[i++] = (Tag + "getTag: " + "null");
                        tmp[i++] = (Tag + "ResourceId: " + "null");
                        //more features
                        tmp[i++] = (Tag + "getWidth: " + "null");
                        tmp[i++] = (Tag + "getHeight: " + "null");
                        tmp[i++] = (Tag + "getColor: " + "null");

                        try {
                            int arg1 = (int) param.args[1];
                            tmp[i++] = (Tag + "Text: " + arg1);
                            CharSequence getText = context.getText((int) param.args[1]);
                            tmp[i++] = (Tag + "Text: " + getText);
                            tmp[i++] = (Tag + "TextLength: " + (getText.toString().replace("\n", "").replace("\r", "").length()));
                        } catch (ClassCastException exc) {
                        }
                        try {
                            CharSequence arg1 = (CharSequence) param.args[1];
                            tmp[i++] = (Tag + "Text: " + arg1);
                            tmp[i++] = (Tag + "TextLength: " + (arg1.toString().replace("\n", "").replace("\r", "").length()));
                        } catch (ClassCastException exc) {
                        }
                        printing(tmp);
                    }
                }
            });
        }
    }

    public static Set apps = new HashSet(appList.apps);
    public static XSharedPreferences prefs ;

    public static final String packageName = UIHarvester.class.getPackage().getName();
    public static final String modulePrefs = "UIHarvesterPrefs";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        prefs =new XSharedPreferences(packageName,modulePrefs);
        prefs.makeWorldReadable();
        prefs.reload();


        apps = new HashSet(appList.apps);
        int size = prefs.getInt("selectedAppsSize", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++) {
            array[i] = prefs.getString("selectedApps_" + i, null);
            apps.add(array[i]);
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hook_onDraw();
        hook_Toast();
    }
}
