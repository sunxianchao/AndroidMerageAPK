package com.joysdk.apk.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


/**
 * 合并文件夹及xml文件的操作封装
 * 对于文件夹的合并使用apache commons-io的copyDirectory方法不能确切的知道哪个文件被覆盖
 * @author Sunxc
 *
 */
public class MergerFilesUtil {

    /**
     * 传递两个目录,进行合并，将sourcePath合并到targetPath目录
     * @param sourcePath
     * @param targetPath
     * @param isCover
     */
    public static void merageFolder(String sourcePath, String targetPath, boolean isCover){
        File sourceDir=new File(sourcePath);
        File targetDir=new File(targetPath);
        if(sourceDir==null || !sourceDir.exists()){
            return;//throw new RuntimeException("源文件夹为空或不存在" + sourceDir.getAbsolutePath());
        }
        if(targetDir==null || !targetDir.exists()){
            if(!targetDir.mkdirs()){
                throw new RuntimeException("目标文件夹为空或不存在" + targetDir.getAbsolutePath());
            }
        }
        File[] files=sourceDir.listFiles();
        if(files != null && files.length > 0){
            for(File file: files){
                doMerageDir(file, sourcePath, targetPath, isCover);
            }
        }
        
    }
    
    private static void doMerageDir(File file, String sourcePath, String targetPath, boolean isCover){
        if(file.isDirectory()){
            File[] files=file.listFiles();
            if(files != null && files.length>0){
                for(File f: files){
                    doMerageDir(f, sourcePath, targetPath, isCover);
                }
            }else{// 没有文件是空目录则创建
                String relativeDir=file.getPath().replace(sourcePath, "");
                File targetDir=new File(targetPath + File.separator + relativeDir);
                if(!targetDir.mkdirs()){
                    throw new RuntimeException("文件夹无法创建："+targetDir.getAbsolutePath());
                }
            }
        }else if(file.isFile()){
            String srcRelativePath=file.getPath().replace(sourcePath, "");// 获取文件的相对路径 test/a.txt
            File targetFile=new File(targetPath + File.separator + srcRelativePath); // 获取目标文件的路径 /usr/test/a.txt
            if(targetFile.exists() && isCover){// 如果目标目录存在并且是可以覆盖的那么就覆盖
                if(!targetFile.delete()){
                    throw new RuntimeException("文件无法覆盖："+targetFile.getAbsolutePath());
                }
                System.out.println("文件存在，已覆盖:"+targetFile.getAbsolutePath());
            }
            if(!targetFile.exists()){
                String pDir=targetFile.getParentFile().getPath();
                File pDirFile=new File(pDir);
                if(!pDirFile.exists()){
                    if(!pDirFile.mkdirs()){
                        throw new RuntimeException("文件夹无法创建："+pDirFile.getAbsolutePath());
                    }
                    System.out.println("目标文件夹不存在，创建"+pDirFile.getName());
                }
            }
            try {
                FileUtils.copyFileToDirectory(file, targetFile.getParentFile(), false);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * 指定目下合并相同名称的文件，如果没有则复制到目标目录
     * @param sourcePath
     * @param targetPath
     * @param isCover
     */
    public static void merageXML(String sourcePath, String targetPath, boolean isCover){
        File sourceDir=new File(sourcePath);
        File targetDir=new File(targetPath);
        if(sourceDir==null || !sourceDir.exists()){
            throw new RuntimeException("源文件夹为空或不存在" + sourceDir.getAbsolutePath());
        }

        File[] files=sourceDir.listFiles();
        if(files != null && files.length > 0){
            for(File file: files){
                try {
                    doMerageXML(file, sourcePath, targetPath, isCover);
                } catch(IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("文件合并出错："+file.getAbsolutePath());
                }
            }
        }
        
    }
    
    private static void doMerageXML(File file, String sourcePath, String targetPath, boolean isCover) throws IOException {
        if(file.isDirectory()){
            File[] files=file.listFiles();
            if(files != null && files.length>0){
                for(File f: files){
                    doMerageXML(f, sourcePath, targetPath, isCover);
                }
            }
        }else if(file.isFile()){
            String srcRelativePath=file.getPath().replace(sourcePath, "");
            File targetFile=new File(targetPath + File.separator + srcRelativePath);
            if(file.getName().startsWith("public")){
                return;
            }
            if(targetFile.exists()){
                parseXml(file, targetFile, srcRelativePath, isCover);
            }else{// 不存在直接复制
                String path=targetFile.getParentFile().getPath();
                File pdir=new File(path);
                if(!pdir.exists()){
                    if(!pdir.mkdir()){
                        throw new RuntimeException("文件复制失败："+file.getAbsolutePath());
                    }
                }
                FileUtils.copyFileToDirectory(file, targetFile.getParentFile(), false);
            }
        }
    }

    private static void parseXml(File file, File targetFile, String srcRelativePath, boolean isCover) {
        XMLWriter xmlWriter=null;
        SAXReader saxReader = new SAXReader();
        Map<String, Element> elemetsMap=new HashMap<String, Element>();
        try {
            Document srcDoc=saxReader.read(file);
            Document targetDoc=saxReader.read(targetFile);
            Element srcRoot=srcDoc.getRootElement();
            List<Element> srcList=srcRoot.elements();
            for(Element element: srcList){
                Attribute attrName=element.attribute("name");
                elemetsMap.put(attrName.getStringValue(), element);
            }
            
            Element targetRoot=targetDoc.getRootElement();
            List<Element> targetList=targetRoot.elements();
            for(Element element: targetList){
                Attribute attrName=element.attribute("name");
                if(elemetsMap.containsKey(attrName.getStringValue())){
                    if(isCover){// 允许去覆盖
                        elemetsMap.remove(attrName.getStringValue());
                    }else{
                        throw new RuntimeException("节点名称冲突，请更改"+file.getAbsolutePath()+"\n"+targetFile.getAbsolutePath()+"\n"+attrName.asXML());
                    }
                }
                
            }
            
            Iterator<String> it=elemetsMap.keySet().iterator();
            while(it.hasNext()){
                String key=it.next();
                targetRoot.add((Element)elemetsMap.get(key).clone());
            }
            FileWriter fileWriter = new FileWriter(targetFile.getAbsoluteFile());  
            OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
            xmlFormat.setEncoding("utf-8");  
            xmlWriter = new XMLWriter(fileWriter, xmlFormat);  
            xmlWriter.write(targetDoc);  
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally{
            try {
                xmlWriter.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    public static void main(String[] args) {
        //merageFolder("/HDD/a", "/HDD/b", true);
        String path1="/Volumes/HDD/android_sign/apk_channel_mac/9947(com.yunyouyou.test)/res/values/";
        File srcFile=new File(path1);
        String path2="/Volumes/HDD/android_sign/apk_channel_mac/joysdk_91/res/values/";
        File tarFile=new File(path2);
        //merageXML(path1, path2, true);
    }
}
