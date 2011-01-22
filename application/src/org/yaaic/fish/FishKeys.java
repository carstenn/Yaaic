package org.yaaic.fish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.os.Environment;

public class FishKeys
{
    public static FishKeys instance;
    private final Properties keys = new Properties();
    //Get sdcard path
    private final String exStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //Get file separator char
    private final String fileSeparator = File.separator;
    //Set blowcrypt working path eg. /sdcard/.yaaic
    private final String path = exStoragePath + fileSeparator + ".yaaic";
    //Set blowcrypt working path eg. /sdcard/.yaaic
    private final String fishkeys = path + "/fishkeys.properties";

    private boolean exStorageAvailable = false;
    private boolean exStorageWriteable = false;

    private FishKeys()
    {
        checkExternalStorageState();
        if(exStorageAvailable && exStorageWriteable) {
            File bcPath = new File(path);
            if(!bcPath.exists()) {
                bcPath.mkdirs();
            }

            File bcProperties = new File(fishkeys);
            if(!bcProperties.exists()) {
                try {
                    bcProperties.createNewFile();
                } catch (Exception e) {
                    // TODO Let the user know and disable Fish
                    //Do nothing for now...
                    //e.printStackTrace();
                }
            }

            try {
                keys.load(new FileInputStream(bcProperties));
            }
            catch (Exception e) {
                // TODO Let the user know and disable Fish
                //Do nothing for now...
                //e.printStackTrace();
            }
        }

    }

    public static FishKeys getInstance()
    {
        if(instance == null) {
            instance = new FishKeys();
        }

        return instance;
    }

    private void checkExternalStorageState()
    {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            exStorageAvailable = true;
            exStorageWriteable = true;
        } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            exStorageAvailable = true;
            exStorageWriteable = false;
        } else {
            exStorageAvailable = false;
            exStorageWriteable = false;
        }
    }

    public String getKey(String server, String conversation)
    {
        String key = null;
        checkExternalStorageState();
        if(exStorageAvailable) {
            key = keys.getProperty(server + "." + conversation);
        } else {
            key = null;
        }

        return key;
    }

    public void setKey(String server, String conversation, String key)
    {
        checkExternalStorageState();
        if(exStorageAvailable && exStorageWriteable) {
            keys.setProperty(server + "." + conversation, key);
            try {
                keys.store(new FileOutputStream(new File(fishkeys)), null);
            }
            catch (FileNotFoundException e) {
                // TODO Let the user know and disable Fish
                //Do nothing for now...
                //e.printStackTrace();
            }
            catch (IOException e) {
                // TODO Let the user know and disable Fish
                //Do nothing for now...
                //e.printStackTrace();
            }
        }
    }



}
