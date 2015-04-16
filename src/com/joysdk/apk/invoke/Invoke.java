package com.joysdk.apk.invoke;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import sun.security.tools.JarSigner;
import brut.apktool.Main;
import brut.common.BrutException;

import com.joysdk.apk.xml.ManifestMerageUtil;
import com.joysdk.apk.xml.MergerFilesUtil;

public class Invoke {
    
    private static Properties properties;
    private static Map<String, String> pathMap;
    
    public Invoke(){}
    
    public Invoke(Properties properties){
        Invoke.properties = properties;
    }
    
    
    public static void setProperties(Properties properties) {
        Invoke.properties=properties;
    }

    
    public static void setPathMap(Map<String, String> pathMap) {
        Invoke.pathMap=pathMap;
    }

    /**
     * 签名文件
     * @param apkPath, 要签名的文件
     * @throws Exception 
     */
    public void signApk(String apkPath) throws Exception{
        System.out.println("签名apk文件:"+apkPath);
        String apkStorePath = properties.getProperty("apk.store.path");
        if(apkStorePath == null || apkStorePath.length() == 0){
            apkStorePath = pathMap.get("workspace.dir");
        }
        apkStorePath = apkStorePath + "temp.apk";
        String[] args = {"-verbose", "-storepass", properties.getProperty("keystore.storepass"), 
                         "-keystore", properties.getProperty("keystore.file"), 
                         "-signedjar", apkStorePath, apkPath, properties.getProperty("keystore.alias")};
        JarSigner.main(args);
    }
    
    
    /**
     * 合并assets、lib、smail文件的内容
     * @throws IOException
     */
    public void merageAll() throws IOException{
        //9、合并assets
        MergerFilesUtil.merageFolder(pathMap.get("srcAssetsPath"), pathMap.get("tempAssetsPath"), true);
        MergerFilesUtil.merageFolder(pathMap.get("targetAssetsPath"), pathMap.get("tempAssetsPath"), true);

        //10、合并lib
        MergerFilesUtil.merageFolder(pathMap.get("srcLibPath"), pathMap.get("tempLibPath"), true);
        MergerFilesUtil.merageFolder(pathMap.get("targetLibPath"), pathMap.get("tempLibPath"), true);
        
        //11、合并smali文件，但是不覆盖package下的文件
        MergerFilesUtil.merageFolder(pathMap.get("srcSmaliPath"), pathMap.get("tempSmaliPath"), true);
        MergerFilesUtil.merageFolder(pathMap.get("targetSmaliPath"), pathMap.get("tempSmaliPath"), true);
        
    }
    
    
    /**
     * 清空创建的临时目录，并且拷贝相关的文件
     * @throws IOException
     */
    public void cleanBuildPath() throws IOException{
        File workDir = new File(pathMap.get("temp"));
        FileUtils.deleteDirectory(workDir);
        new File(properties.getProperty("workspace.dir")+"_temp").renameTo(new File(pathMap.get("temp")));
        
        File srcFile = new File(properties.getProperty("workspace.dir") + "AndroidManifest.xml");
        File desFile = new File(pathMap.get("temp") + "AndroidManifest.xml");
        desFile.delete();
        FileUtils.moveFileToDirectory(srcFile, workDir, false);
    }
    
    public void done() throws IOException{
        FileUtils.deleteDirectory(new File(pathMap.get("temp")));
        FileUtils.deleteDirectory(new File(pathMap.get("srcApkPath")));
        FileUtils.deleteDirectory(new File(pathMap.get("targetApkPath")));
    }
    
    
    /**
     * 生成一个apktool使用的yml文件，只替换了apkFileName文件名，其他的没有进行替换，似乎没有任何影响
     * @throws IOException
     */
    public void initWithYmlFile() throws IOException{
        File targetPath=new File(pathMap.get("targetApkPath"));
        File ymlFile=FileUtils.getFile(targetPath, "apktool.yml");
        FileUtils.copyFileToDirectory(ymlFile, new File(pathMap.get("temp")));
        ymlFile=FileUtils.getFile(pathMap.get("temp"), "apktool.yml");
        List<String> lines=FileUtils.readLines(ymlFile, "utf-8");
        
        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            if(line.contains("apkFileName")){
                line = line.replaceAll("apkFileName:.*", "apkFileName: temp.apk");
                lines.remove(i);
                lines.add(i, line);
                break;
            }
        }
        
        FileUtils.writeLines(ymlFile, lines);
    }
    
    
    /**
     * 合并res 中的目录，如果是values开头的目录需要进行文件合并
     * 但是public.xml是无用的可以不复制
     */
    public void merageResAndXmlFiles(){
        File srcResFile=new File(pathMap.get("srcResPath"));
        File[] srclist=srcResFile.listFiles();
        for(File file: srclist){
            if(file.getName().equals(".DS_Store")){
                continue;
            }
            if(file.getName().startsWith("values")){
                for(File f: file.listFiles()){
                    MergerFilesUtil.merageXML(f.getParentFile().getPath()+File.separator, pathMap.get("tempResPath")+file.getName(), true);
                }
            }else{
                MergerFilesUtil.merageFolder(srcResFile+File.separator+file.getName(), pathMap.get("tempResPath")+file.getName(), true);
            }
        }
        
        File targetResFile=new File(pathMap.get("targetResPath"));
        File[] targetlist=targetResFile.listFiles();
        for(File file: targetlist){
            if(file.getName().equals(".DS_Store")){
                continue;
            }
            if(file.getName().startsWith("values")){
                for(File f: file.listFiles()){
                    MergerFilesUtil.merageXML(f.getParentFile().getPath()+File.separator, pathMap.get("tempResPath")+file.getName(), true);
                }
            }else{
                MergerFilesUtil.merageFolder(targetResFile.getAbsolutePath()+File.separator+file.getName(), pathMap.get("tempResPath")+file.getName(), true);
            }
        }

    }
    
    /**
     * 创建空的manifest文件和已经合并后的文件,生成的R文件就在渠道定义的package目录下了
     * @throws IOException
     */
    public void createManifestXmlFile() throws IOException{
        FileUtils.copyFileToDirectory(new File(pathMap.get("targetManifest")), new File(pathMap.get("temp")));
        // 3、先生成一个只含有manifest节点的文件，再生成一个合并两个工程的manifest文件
        ManifestMerageUtil.merageManifestXML(pathMap.get("sourceManifest"), pathMap.get("tempManifestXml"), properties);
    }
    
    // 解压apk
    public void cmdDecodeApk(String apkPath, String... args) throws IOException, InterruptedException, BrutException{
        String[] arr = {"d", apkPath};
        String[] result = Arrays.copyOf(arr, arr.length + args.length);
        System.arraycopy(args, 0, result, arr.length, args.length);
        Main.main(result);
    }
    
    // 生成apk
    public void cmdBuildApk(String apkPath, String... args) throws IOException, InterruptedException, BrutException{
        String[] arr = {"b", apkPath};
        String[] result = Arrays.copyOf(arr, arr.length + args.length);
        System.arraycopy(args, 0, result, arr.length, args.length);
        Main.main(result);
    }
    
    public static void main(String[] args) throws Exception {
        Invoke i=new Invoke();
        i.signApk("/Users/Sunxc/workspace/MakeApk/workspace/temp/dist/temp.apk");
    }
    
    /**
     * 创建apk反编译后的所有目录，放在map中以便后面访问使用
     * @param srcApk
     * @param targetApk
     * @return
     * @throws Exception 
     */
    public Map<String, String> initWithCreatePath(String workspace, String srcApk, String targetApk) throws Exception{
        Map<String, String> pathMap = new HashMap<String, String>();

        File wsPath = new File(workspace);
        if(!wsPath.exists()){
            wsPath.mkdirs();
        }
        pathMap.put("temp", workspace+"temp"+File.separator);
        pathMap.put("tempManifestXml", workspace+"temp"+File.separator+"AndroidManifest.xml");
        pathMap.put("tempResPath", workspace+"temp"+File.separator+"res"+File.separator);
        pathMap.put("tempAssetsPath", workspace+"temp"+File.separator+"assets"+File.separator);
        pathMap.put("tempLibPath", workspace+"temp"+File.separator+"lib"+File.separator);
        pathMap.put("tempSmaliPath", workspace+"temp"+File.separator+"smali"+File.separator);
        
        File srcApkFile = new File(srcApk);
        File targetApkFile = new File(targetApk);
        if(srcApkFile.exists()){
            pathMap.put("srcApk", srcApk);
        }else{
            throw new Exception("src apk not found");
        }
        if(targetApkFile.exists()){
            pathMap.put("targetApk", targetApk);
        }else{
            throw new Exception("target apk not found");
        }
        String srcApkPath=srcApk.substring(0, srcApk.lastIndexOf("."))+File.separator;
        String targetApkPath=targetApk.substring(0, targetApk.lastIndexOf("."))+File.separator;
        pathMap.put("srcApkPath", srcApkPath);
        pathMap.put("targetApkPath", targetApkPath);
        
        String srcResPath=srcApkPath+"res"+File.separator;
        String targetResPath=targetApkPath+"res"+File.separator;
        pathMap.put("srcResPath", srcResPath);
        pathMap.put("targetResPath", targetResPath);
        
        String srcAssetsPath=srcApkPath+"assets"+File.separator;
        String targetAssetsPath=targetApkPath+"assets"+File.separator;
        pathMap.put("srcAssetsPath", srcAssetsPath);
        pathMap.put("targetAssetsPath", targetAssetsPath);

        String srcLibPath=srcApkPath+"lib"+File.separator;
        String targetLibPath=targetApkPath+"lib"+File.separator;
        pathMap.put("srcLibPath", srcLibPath);
        pathMap.put("targetLibPath", targetLibPath);

        String srcSmaliPath=srcApkPath+"smali"+File.separator;
        String targetSmaliPath=targetApkPath+"smali"+File.separator;
        pathMap.put("srcSmaliPath", srcSmaliPath);
        pathMap.put("targetSmaliPath", targetSmaliPath);
        
        String sourceManifest=srcApkPath+"AndroidManifest.xml";
        String targetManifest=targetApkPath+"AndroidManifest.xml";
        pathMap.put("sourceManifest", sourceManifest);
        pathMap.put("targetManifest", targetManifest);
        
        return pathMap;
        
    }
    
}
