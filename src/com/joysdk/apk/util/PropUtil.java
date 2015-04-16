package com.joysdk.apk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropUtil {

    private static Properties prop;
    
    private PropUtil(){}
    public static Properties  getInstance(File file){
        if(prop == null){
            FileInputStream fis=null;
            try {
                prop=new Properties();
                fis = new FileInputStream(file); 
                prop.load(fis);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                if(fis != null)
                    try {
                        fis.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return prop;
    }

    public static String get(String key) {
        return prop.getProperty(key);
    }
}
