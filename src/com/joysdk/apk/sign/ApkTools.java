package com.joysdk.apk.sign;

import java.io.IOException;

import com.joysdk.apk.util.ShellUtil;


public class ApkTools {

    private String apktool="/Users/Sunxc/opt/android_sdk/sdk/tools/apktool";
    
    private String jarsignfile="/Users/Sunxc/workspace/ProjectTest/apk/tools/jarsigner.jar";
    
    private String newName;
    
    public boolean unZipApk(String apkPath) throws Exception{
        String topath=apkPath.substring(0, apkPath.lastIndexOf("."));
        return unZipApk(apkPath, topath);
    }
    
    public boolean unZipApk(String apkPath, String topath) throws Exception{
        System.out.println("解压文件:");
        String[] commands=new String[]{apktool, "d", apkPath, topath};
        return ShellUtil.runScript(commands)==0;
//        return File.separator+apkname.substring(0, apkname.lastIndexOf("."));
    }
    
    public boolean zipApk(String distPath){
        System.out.println("打包apk文件:");
        String[] commands=new String[]{apktool, "b", distPath};
        return ShellUtil.runScript(commands)==0;
    }
    
    public boolean signApk(String apkPath, String topath, String keystoreFile){
        System.out.println("签名apk文件:");
        String[] commands=new String[]{"java", "-jar", jarsignfile, "-verbose", "-storepass", "yunyoyo", "-keystore",
            keystoreFile, "-signedjar", topath, apkPath, "downjoy"};
        return ShellUtil.runScript(commands)==0;
    }
    
    public boolean runAntScript(String buildFile, String androidJar, String apkname, String keystore, String storepass, String keypass, String alias){
        String[] commands=new String[]{"/usr/local/apache-ant-1.8.4/bin/ant", "-f", 
            buildFile, "-Dapkname="+apkname, "-DandroidJar="+androidJar, "-Dkeystore="+keystore, 
            "-Dstorepass="+storepass, "-Dkeypass="+keypass, "-Dalias="+alias};
        return ShellUtil.runScript(commands)==0;
    }
    
//    
//    public boolean signApkWithKeystore(String apkname, String channelId, String filepath){
//        try {
//            System.out.println("签名apk文件:");
//            String topath=currentPath + "\\temp\\dist\\";
//            String name=apkname.substring(0, apkname.lastIndexOf("."));
//            newName=name+"_sign.apk";
//            String content=FileUtil.getFileContent(new File(currentPath + "\\" + filepath));
//            content=content.replace("#path#", topath+apkname);
//            content=content.replace("#name#", newName);
//            String[] commands=content.split(",");
//            return runScript(commands)==0;
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//    
//    public boolean zipalign(String apkname){
//        String newapkpath=currentPath+"\\" + newName;
//        apkname=apkname.substring(0, apkname.lastIndexOf("."));
//        String[] commands=new String[]{"zipalign", "-v", "4", newapkpath, apkname+"_zipalign.apk" };
//        return runScript(commands)==0;
//    }
    
    
    
    public static void main(String[] args) throws IOException {
        String buildFile="/Users/Sunxc/workspace/ProjectTest/apk/temp/build.xml";
        String androidJar="/Users/Sunxc/opt/android_sdk/sdk/platforms/android-17/android.jar";
        String apkfile="/Users/Sunxc/workspace/ProjectTest/apk/temp/joysdk_xxx.apk";
        String keystore="/Users/Sunxc/workspace/ProjectTest/apk/tools/downjoy.keystore";
        String storepass="yunyoyo";
        String keypass="yunyoyo";
        String alias="downjoy";
        ApkTools apktools=new ApkTools();
//        apktools.runAntScript(buildFile, androidJar, apkfile, keystore, storepass, keypass, alias);
        apktools.zipApk("/usr/local/temp/workspace/mxm/");
    }
     
}
