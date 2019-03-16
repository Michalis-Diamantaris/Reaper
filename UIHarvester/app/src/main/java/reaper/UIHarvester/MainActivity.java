/*
 * UIHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */
package reaper.UIHarvester;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static reaper.UIHarvester.UIHarvester.modulePrefs;


public class MainActivity extends Activity {


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
    public void loadOutputSettings()
    {
        //Get sharedPrefs for output mode
        SharedPreferences sharedpreferences;
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N)
        {
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_WORLD_READABLE);
        }
        else{
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        int logcatOutputPlain=sharedpreferences.getInt("logcatOutputPlain",-1);
        int logcatOutputBase64=sharedpreferences.getInt("logcatOutputBase64",-1);

        CheckBox outputplain = (CheckBox) findViewById(R.id.outputplain);
        CheckBox outputbase64 = (CheckBox) findViewById(R.id.outputbase64);
        if (logcatOutputPlain == 1)
        {
            //set logcat output as plain
            outputplain.setChecked(true);

        }
        if (logcatOutputBase64 == 1)
        {
            //set logcat output as base64
            outputbase64.setChecked(true);
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
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(reaper.UIHarvester.R.layout.activity_main);


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

        //Set saved prefferences
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
        //set OnItemClickListener
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


        loadOutputSettings();

    }
    @SuppressLint("LongLogTag")
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        SharedPreferences sharedpreferences;
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N)
        {
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_WORLD_READABLE);
        }
        else{
            sharedpreferences = this.getApplication().getSharedPreferences(modulePrefs, Context.MODE_PRIVATE);
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.outputplain:
                if (checked)
                {
                    editor.putInt("logcatOutputPlain",1 );
                    editor.apply();
                    editor.commit();
                }
                else {
                    editor.putInt("logcatOutputPlain",0 );
                    editor.apply();
                    editor.commit();
                }
                break;
            case R.id.outputbase64:
                if (checked) {
                    editor.putInt("logcatOutputBase64",1 );
                    editor.apply();
                    editor.commit();
                }
            else {
                    editor.putInt("logcatOutputBase64",0 );
                    editor.apply();
                    editor.commit();
                }
                break;
        }
    }
}