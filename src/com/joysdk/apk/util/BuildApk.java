package com.joysdk.apk.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import brut.common.BrutException;
import brut.util.OS;

/**
 * 编译一个只有资源文件的apk，目的是拿到合并资源后的R文件
 * @author Sunxc
 *
 */
public class BuildApk {
    
    private final static String GEN = "gen"+File.separator;
    private final static String BIN = "bin"+File.separator;
    private final static String RES = "res"+File.separator;
    private final static String MANIFESTFILE = "AndroidManifest.xml";

    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            throw new Exception("请输入项目完整路径，需要加目录符号 如/usr/local/myproject/");
        }
        String projectDir = args[0];
        String androidJarPath = PropertiesUtil.getValue("android.buildjar.path");
        genRJava(projectDir, androidJarPath);
        compilRClass(androidJarPath, projectDir);
        class2Dex(projectDir);
        packageRes(projectDir, androidJarPath);
        buildApk(projectDir);
    }
    
    
    public static void genRJava(String projectDir, String androidJarPath) throws BrutException{
        String genDir = projectDir+GEN;
        String resDir = projectDir+RES;
        String manifestXmlFilePath = projectDir + MANIFESTFILE;
        List<String> cmd = new ArrayList<String>();
        cmd.add(PropertiesUtil.getValue("aapt.tool.path"));
        cmd.add("package");
        cmd.add("-f");
        cmd.add("-m");
        cmd.add("-J");
        cmd.add(genDir);
        cmd.add("-S");
        cmd.add(resDir);
        cmd.add("-I");
        cmd.add(androidJarPath);
        cmd.add("-M");
        cmd.add(manifestXmlFilePath);
        File genFileDir=new File(genDir);
        if(!genFileDir.exists()){
            genFileDir.mkdirs();
        }
        File resFileDir=new File(resDir);
        if(!resFileDir.exists()){
            resFileDir.mkdirs();
        }
        String[] strArr=(String[])cmd.toArray(new String[0]);
        OS.exec(strArr);
    }
    
    public static void compilRClass(String androidJarPath, String projectDir) throws BrutException, DocumentException{
        String genDir = projectDir+GEN;
        String binDir = projectDir+BIN;
        String manifestXmlFilePath = projectDir + MANIFESTFILE;
        String packageName = getPackageName(manifestXmlFilePath);
        String packagePath = packageName.replaceAll("\\.", File.separator);
        List<String> cmd = new ArrayList<String>();
        cmd.add("javac");
        cmd.add("-bootclasspath");
        cmd.add(androidJarPath);
        cmd.add("-d");
        cmd.add(binDir);
        cmd.add("-source");
        cmd.add("1.6");
        cmd.add("-target");
        cmd.add("1.6");
        cmd.add(genDir+packagePath+"/R.java");
        File classFileDir=new File(binDir);
        if(!classFileDir.exists()){
            classFileDir.mkdirs();
        }
        String[] strArr=(String[])cmd.toArray(new String[0]);
        OS.exec(strArr);
    }
    
    private static String getPackageName(String manifestFile) throws DocumentException{
        SAXReader saxReader = new SAXReader();
        Document srcDoc=saxReader.read(new File(manifestFile));
        Element srcRoot=srcDoc.getRootElement();
        return srcRoot.attributeValue("package");
    }
    
    public static void class2Dex(String projectDir) throws BrutException{
        String binDir = projectDir+File.separator+BIN;
        List<String> cmd = new ArrayList<String>();
        cmd.add(PropertiesUtil.getValue("dx.tool.path"));
        cmd.add("--dex");
        cmd.add("--output="+binDir+"classes.dex");
        cmd.add(binDir);
        String[] strArr=(String[])cmd.toArray(new String[0]);
        OS.exec(strArr);
    }
    
    public static void packageRes(String projectDir, String androidJarPath) throws BrutException{
        String resDir = projectDir+RES;
        String binDir = projectDir+BIN;
        String manifestXmlFilePath = projectDir + MANIFESTFILE;
        List<String> cmd = new ArrayList<String>();
        cmd.add(PropertiesUtil.getValue("aapt.tool.path"));
        cmd.add("package");
        cmd.add("-f");
        cmd.add("-M");
        cmd.add(manifestXmlFilePath);
        cmd.add("-S");
        cmd.add(resDir);
        cmd.add("-I");
        cmd.add(androidJarPath);
        cmd.add("-F");
        cmd.add(binDir+"resources.ap_");
        String[] strArr=(String[])cmd.toArray(new String[0]);
        OS.exec(strArr);
    }
    
    public static void buildApk(String projectDir) throws BrutException{
        String binDir = projectDir+BIN;
        List<String> cmd = new ArrayList<String>();
        cmd.add(PropertiesUtil.getValue("apkbuilder.tool.path"));
        cmd.add(projectDir+"temp.apk");
        cmd.add("-v");
        cmd.add("-u");
        cmd.add("-z");
        cmd.add(binDir+"resources.ap_");
        cmd.add("-f");
        cmd.add(binDir+"classes.dex");
        String[] strArr=(String[])cmd.toArray(new String[0]);
        OS.exec(strArr);
    }
    
    
}
