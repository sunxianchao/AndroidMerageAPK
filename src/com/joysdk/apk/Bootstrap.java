package com.joysdk.apk;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.joysdk.apk.invoke.Invoke;
import com.joysdk.apk.util.BuildApk;
import com.joysdk.apk.util.PropertiesUtil;

public class Bootstrap {

    private static String currentPath=System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            throw new Exception("非法参数");
        }

        autoMerageApk();
//        String forWhat=args[0];
//        if(forWhat.equalsIgnoreCase("m")) {
//            autoMerageApk();
//        } else if(forWhat.equalsIgnoreCase("c")) {// 自有包分包工具
//            createChannelApk(args[1]);
//        } else if(forWhat.equalsIgnoreCase("e")) {// apk加壳操作
//
//        }

    }

    /**
     * 分发渠道包
     * @param apkFilePath
     * @throws Exception
     */
    public static void createChannelApk(String apkFilePath) throws Exception {
        String apkFileName = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator));
        ChannelApk apk = new ChannelApk();
        Invoke invoke = new Invoke();
        List<String> channelids=apk.readChannelId();
        String tmpPath = currentPath + File.separator +"temp";
        invoke.cmdDecodeApk(apkFilePath, tmpPath);
        for(String channelId : channelids){
            apk.updateChannelId(channelId);
            invoke.cmdBuildApk(tmpPath);
            File distFile = new File(tmpPath+File.separator+"dist"+File.separator+apkFileName);
            File newApkFile = new File(currentPath+File.separator+"channelApk"+File.separator+channelId+"_"+apkFileName);
            FileUtils.copyFile(distFile, newApkFile);
        }
    }

    // 自动生产渠道打包文件
    public static void autoMerageApk() throws Exception {
        String srcPath=currentPath + File.separator + "src" + File.separator;
        Properties pros=PropertiesUtil.loadFile(srcPath + "configs.properties");
        Invoke invoke=new Invoke(pros);

        String workspace=pros.getProperty("workspace.dir" + File.separator, currentPath + File.separator + "workspace" + File.separator);

        // apk的路径
        String srcApkFile=pros.getProperty("src.apk");
        String targetApkFile=pros.getProperty("target.apk");

        Map<String, String> pathMap=invoke.initWithCreatePath(workspace, srcApkFile, targetApkFile);
        Invoke.setPathMap(pathMap);

        // 指定解压后的目录
        invoke.cmdDecodeApk(srcApkFile, "-o", pathMap.get("srcApkPath"));
        invoke.cmdDecodeApk(targetApkFile, "-o",  pathMap.get("targetApkPath"));
        invoke.createManifestXmlFile();
        invoke.merageResAndXmlFiles();
        invoke.initWithYmlFile();
        BuildApk.main(new String[]{pathMap.get("temp")});
        invoke.cmdDecodeApk(pathMap.get("temp") + "temp.apk", "-o", pros.getProperty("workspace.dir")+"_temp", "-f");
        invoke.cleanBuildPath();
        invoke.merageAll();
        invoke.cmdBuildApk(pathMap.get("temp"));
        invoke.signApk(pathMap.get("temp") + "dist" + File.separator + "temp.apk");
        invoke.done();
    }
}
