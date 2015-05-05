package com.joysdk.apk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesUtil {
    //为了读了properties文件  
    private static Properties properties = null;  
    //输入流，输出流---为了读取txt文件  
    private static InputStream in = null;  
    private BufferedReader br = null;  
    
    /** 
     * 处理propertise文件 
     * @param fileConfig  文件的明（路径） 
     * @param flag 为了区分非propertise文件（利用构造方法的重载） 
     * @throws Exception 
     */  
    public static Properties loadFile(String fileConfig) throws Exception{ //构造方法  
        //获取输入流  
        try {
            String[] files=fileConfig.split(",");
            properties = new Properties();  
            for(String file : files){
                in = new FileInputStream(new File(file));
                //这里要说明一下，如果说这个方法是在静态代码块中写的话，那么this是不能用的，this是代表该类的对象，把this改成Object即可，而相应的属性变量也需要改为静态的  
                properties.load(in);  
            }
            in.close();  
        } catch (Exception e) {  
            System.out.println("------读取properties配置文件" + fileConfig + "时，出错:" + e.getMessage().toString());
            throw new Exception();
        }  
        return properties;
    }  
    /** 
     * 读取properties文件，根据key获取key值所对应的value值 
     * @param key  
     * @return value 值 
     */  
    public static String getValue(String key){  
        String value = null;  
        try {  
            value = properties.getProperty(key);  
        } catch (Exception e) {  
            System.out.println("根据properties的key值获取value值时出错:" + e);  
        }  
        return value;  
    }  
  
    /** 
     * 一行一行读取文本文件 
     * @return 这个文本文件 
     */  
    public String getTextLines(){  
        StringBuffer sb = new StringBuffer();  
        String temp = null;  
        try {  
            while((temp=br.readLine()) != null){  
                if(temp.trim().length() > 0 && (!temp.trim().startsWith("#"))){//这里说明文本文件里面也可以用#注释  
                    sb.append(temp);  
                    sb.append("\n");  
                }  
            }  
        } catch (IOException e) {  
            System.out.println("在读取文本文件时出错：" + e.getMessage().toString());  
        }finally{  
            if(in != null){  
                try {  
                    in.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
            if(br != null){  
                try {  
                    br.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
              
        }  
        return sb.toString();  
    }
    
    public static Properties getProperties() {
        return properties;
    }
    
    
}
