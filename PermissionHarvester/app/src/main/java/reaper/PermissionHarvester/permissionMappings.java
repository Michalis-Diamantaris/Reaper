/*
 * PermissionHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */
package reaper.PermissionHarvester;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;

public class permissionMappings extends ContextWrapper{
    // variable to hold context
    private  Context context;
    public permissionMappings(Context base){
        super(base);
    }
    public void getMappingsFromFile(String APIversion, String SDKorFramework) {
        String fileName = "API"+APIversion+""+SDKorFramework;
        String[] LineElements;
        String line = null;
        String Class = null;
        String Function = null;
        try {
            // read the text file in the assets dir.
            InputStream inputStream = getAssets().open("permissionMappings/"+fileName);
            // Wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(inputStream));

            while((line = bufferedReader.readLine()) != null)
            {
                LineElements = line.split(" ");
                Class = LineElements[0];
                Function = LineElements[1];
                if (SDKorFramework.equals("SDK"))
                {
                    permissionMappingsSDK.add(Class+" "+Function);
                }
                else if (SDKorFramework.equals("Framework"))
                {
                    permissionMappingsFramework.add((Class+" "+Function));
                }
                else if (SDKorFramework.equals("PII"))
                {
                    PII.add((Class+" "+Function));
                }
            }
            //close file
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.e("Reaper","Unable to open file '" +fileName + "'");
        }
        catch(IOException ex) {
            Log.e("Reaper", "Error reading file '"+ fileName + "'");
        }
    }

    //something to store permission protected calls and PII
    public static Set<String> permissionMappingsSDK = new HashSet<String>();
    public static Set<String> permissionMappingsFramework = new HashSet<String>();
    public static Set<String> PII = new HashSet<String>();

    /*public static class doubles {
        String className;
        String functionName;;
        public doubles(String className, String functionName) {
            this.className = className;
            this.functionName = functionName;
        }
        public String toString()
        {
            String toString="["+className+" "+functionName+"]";
            return toString;
        }
    }
    public static ArrayList<doubles> permissionMappingsSDK= new ArrayList<>();
    public static ArrayList<doubles> permissionMappingsFramework= new ArrayList<>();
    public static ArrayList<doubles> PII= new ArrayList<>();
    */
    @SuppressLint("LongLogTag")
    public static void doublesArraytoString() {
        Log.e("Reaper permissionMappingsSDK", String.valueOf(permissionMappingsSDK.size()));
        Log.e("Reaper permissionMappingsFramework", String.valueOf(permissionMappingsFramework.size()));
        Log.e("Reaper PII", String.valueOf(PII.size()));
        /*int i;
        i=0;for (String s : permissionMappingsSDK)
        {
            Log.e("Reaper","permissionMappingsSDK[" + i + "]: " + s);i++;
        }

        i=0;for (String s : permissionMappingsFramework)
        {
            Log.e("Reaper","permissionMappingsFramework[" + i + "]: " + s);i++;
        }

        i=0;for (String s : PII)
        {
            Log.e("Reaper","PII[" + i + "]: " + s);i++;
        }*/
    }
}
