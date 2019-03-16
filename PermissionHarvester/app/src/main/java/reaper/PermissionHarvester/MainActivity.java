/*
 * PermissionHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */

package reaper.PermissionHarvester;
import static reaper.PermissionHarvester.PermissionHarvester.modulePrefs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import reaper.PermissionHarvester.R;


public class MainActivity extends Activity {
    SharedPreferences sharedpreferences;

    public static List<String> allApps = new ArrayList<>();
    public static List<String> selectedApps = new ArrayList<String>();

    public void insertUnique(String tmp)
    {
        int exists=0;
        for (String  item : selectedApps)
        {
            if (item==tmp)
            {
                exists=1;
            }
        }
        if (exists==0)
        {
            selectedApps.add(tmp);
        }
    }
    @SuppressLint({"WorldReadableFiles", "LongLogTag"})
    public void saveArray()
    {
        SharedPreferences sharedpreferences;
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N)
        {
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_WORLD_READABLE);
        }
        else{
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        //editor.clear();
        editor.putInt("selectedAppsSize", selectedApps.size());
        editor.apply();
        editor.commit();

        for(int i=0;i<selectedApps.size();i++)
            editor.putString("selectedApps_" + i, selectedApps.get(i));
        editor.apply();
        editor.commit();
    }
    @SuppressLint({"WorldReadableFiles", "LongLogTag"})
    public String[] loadArray()
    {
        SharedPreferences sharedpreferences;
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N)
        {
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_WORLD_READABLE);
        }
        else{
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_PRIVATE);
        }

        int size = sharedpreferences.getInt("selectedAppsSize", 0);
        String array[] = new String[size];
        for(int i=0;i<size;i++)
        {
            array[i] = sharedpreferences.getString("selectedApps_" + i, null);
            insertUnique(array[i]);
        }
        return array;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences.Editor editor = null;
        Log.e("Reaper ","MainActivity");
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N)
        {
            sharedpreferences = getSharedPreferences(modulePrefs, Context.MODE_WORLD_READABLE);
            editor = sharedpreferences.edit();
            Log.e("Reaper SP","MODE_WORLD_READABLE");
        }
        else
        {
            sharedpreferences = getSharedPreferences(modulePrefs, Context.MODE_PRIVATE);
            editor = sharedpreferences.edit();
        }
        String APIversion=null;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            //API 16
            APIversion="16";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //API 17
            APIversion="17";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //API 18
            APIversion="18";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            //API 19
            APIversion="19";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            //API 21
            APIversion="21";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            //API 22
            APIversion="22";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            //API 23
            APIversion="23";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            //API 24
            APIversion="24";
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            //API 25
            APIversion="25";
            Log.e("Reaper API version: ",APIversion);
        }
        if (APIversion!=null) {
            //Initiate permissionMappings Class
            permissionMappings mappings = new permissionMappings(this);
            //Get mappings from files
            mappings.getMappingsFromFile(APIversion, "SDK");
            mappings.getMappingsFromFile(APIversion, "Framework");
            mappings.getMappingsFromFile(APIversion, "PII");
            //print sizes for debugging
            mappings.doublesArraytoString();
            //add set to shared preferences
            editor.putStringSet("SDK", mappings.permissionMappingsSDK);
            editor.apply();
            editor.putStringSet("Framework", mappings.permissionMappingsFramework);
            editor.apply();
            editor.putStringSet("PII", mappings.PII);
            editor.apply();
        }
        else
        {
            Log.e("Reaper","No Mappings available for this API yet");
        }

        //Get list of installed package names
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<PackageInfo> pkgAppsList = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < pkgAppsList.size(); i++) {
            if (!allApps.contains(pkgAppsList.get(i).packageName)) {
                allApps.add(pkgAppsList.get(i).packageName);
            }
        }
        //Exclude reaper.UIHarvester, reaper.PermissionHarvester and android
        allApps.remove("reaper.UIHarvester");
        allApps.remove("reaper.PermissionHarvester");
        allApps.remove("android");

        //create an instance of ListView
        final ListView checkListView = (ListView) findViewById(R.id.checkable_list);
        //set multiple selection mode
        checkListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //supply data allApps to ListView
        ArrayAdapter<String> ArrayAdapter = new ArrayAdapter<String>(this, R.layout.checkablerelativelayout, R.id.checkedTextViewId, allApps);
        checkListView.setAdapter(ArrayAdapter);

        String tmparray[] = loadArray();
        if (tmparray!=null) {
            for (int i = 0; i < tmparray.length; i++)
            {
                for (int j = 0; j < allApps.size(); j++)
                {
                    String currentItem = (String) checkListView.getAdapter().getItem(j);
                    if (tmparray[i].contains(currentItem)) {
                        checkListView.setItemChecked(j, true);
                    }
                }
            }
        }
        saveArray();

        checkListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position).toString();

                if (selectedApps.contains(selectedItem)) {
                    selectedApps.remove(selectedItem); //remove deselected item from the list of selected items
                    checkListView.setItemChecked(position, false);
                    saveArray();
                }
                else {
                    selectedApps.add(selectedItem); //add selected item to the list of selected items
                    checkListView.setItemChecked(position, true);
                    saveArray();
                }
            }
        });


    }
}
