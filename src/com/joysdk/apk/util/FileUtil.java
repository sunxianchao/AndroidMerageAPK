package com.joysdk.apk.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;

public class FileUtil {

    public static String getFileContent(File file) throws IOException {
        byte[] buf=getFileBinaryContent(file);
        return new String(buf);
    }

    public static String getFileContent(File file, String charSet) throws IOException {
        if(null == charSet || charSet.length() == 0) {
            return getFileContent(file);
        }
        byte[] buf=getFileBinaryContent(file);
        return new String(buf, charSet);
    }

    public static byte[] getFileBinaryContent(File file) throws IOException {
        byte[] buffer=new byte[1024];
        int len=-1;
        InputStream is=null;
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try {
            is=new FileInputStream(file);
            while((len=is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch(IOException ex) {
            throw ex;
        } finally {
            if(null != is) {
                try {
                    is.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bos.toByteArray();
    }

    public static void appendToFile(String content, File file) {
        File dir=file.getParentFile();
        if(!dir.exists()) {
            dir.mkdirs();
        }
        OutputStream fos=null;
        try {
            fos=new FileOutputStream(file, true);
            OutputStreamWriter write=new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter writer=new BufferedWriter(write);
            writer.write(content);
            writer.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(null != fos) {
                try {
                    fos.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 获取文件扩展名
     * @param fileName
     * @return
     */
    public static String getExtension(String fileName) {
        int ind=fileName.lastIndexOf('.');
        String ext=null;
        if(ind > 0) {
            ext=fileName.substring(ind).toLowerCase();
        }
        return ext;
    }
    
    
    /**
     * 删除某个目录中的文件
     * @param fileDir
     * @param exclude 排除不需要删除的文件
     * @param useChild exclude 参数是否应用于子目录中
     */
    public static void deleteFiles(String fileDir, boolean useChild, int level, String... exclude){
        File theDir = new File(fileDir);
        for(File f : theDir.listFiles()){
            boolean canDelete = true;
            
            if(!useChild && level > 1){
                if(f.isDirectory()){// 直接删除目录，方便起见就使用这个方法进行删除
                    deleteFiles(f.getAbsolutePath(), false, level+1, exclude);
                }
                f.delete();
                continue;
            }
            
            for(String name : exclude){
                if(name.equals(f.getName())){
                    canDelete = false;
                    break;
                }
            }
            
            if(canDelete){
                if(f.isDirectory()){
                    deleteFiles(f.getAbsolutePath(), useChild, level+1, exclude);
                }
                f.delete();
                continue;
            }
        }
        
    }
    
    
    
    /**
     * 复制一个目录及其子目录、文件到另外一个目录
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }
    
    
    
    
    public static void main(String[] args) throws IOException {
        FileUtils.copyFile(new File("/Users/Sunxc/workspace/MakeApk/build.xml"), new File("/Users/Sunxc/workspace/MakeApk/workspace/aaa.xml"));
    }
}
