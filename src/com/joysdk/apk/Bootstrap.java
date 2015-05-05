package com.joysdk.apk;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import com.joysdk.apk.invoke.Invoke;
import com.joysdk.apk.util.BuildApk;
import com.joysdk.apk.util.PropertiesUtil;

public class Bootstrap {

    private static String currentPath=System.getProperty("user.dir");

    private static Options allOpt=new Options();
    
    private static Options merageOpt=new Options();
    
    private static Options channelOpt=new Options();
    
    public static void main(String[] args) throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = null;
        createOptions();
        String formatstr = "makeapktool ";
        try {
            commandLine = parser.parse(allOpt, args, false);
            if(commandLine.hasOption("-m") || commandLine.hasOption("--merage")){
                autoMerageApk(commandLine);
            }else if(commandLine.hasOption("-c") || commandLine.hasOption("--channel")){
                // -a /Users/Sunxc/workspace/MakeApk/workspace/mhmh_mp3g_sdk.apk -c '1 2 3 4 5 6 7'
                createChannelApk(commandLine);
            }else{
                throw new ParseException("");
            }
        } catch (ParseException e) {
//            formatter.printHelp(formatstr, option); // 如果发生异常，则打印出帮助信息
            formatter.printHelp(formatstr + "-mf /xx/xx/config.properties", merageOpt);
            formatter.printHelp(formatstr + "-a /xx/xx/xx.apk -c 1 2 3", channelOpt);
        }

    }

    /**
     * 分发渠道包
     * @param apkFilePath
     * @throws Exception
     */
    public static void createChannelApk(CommandLine commandLine) throws Exception {
        String apkFilePath=null;
        String[] channelIds=null;
        if(commandLine.hasOption("a")){
            apkFilePath=commandLine.getOptionValue("a");
        }else{
            throw new ParseException("请输入apk 文件路径");
        }
        
        String apkFileName = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator)+1, apkFilePath.lastIndexOf(".apk"));
        
        if(commandLine.hasOption("c")){
            String channels=commandLine.getOptionValue("c");
            if(channels.length()>0){
                channelIds=channels.split(" ");
            }
        }else{
            throw new ParseException("请输入apk 文件路径");
        }
        
        ChannelApk apk = new ChannelApk();
        Invoke invoke = new Invoke();
        String tmpPath = currentPath + File.separator +"temp";
        File f=new File(tmpPath);
        if(!f.exists()){
            f.mkdirs();
        }
        invoke.cmdDecodeApk(apkFilePath, "-fo", tmpPath);
        for(String channelId : channelIds){
            apk.updateChannelId(channelId);
            invoke.cmdBuildApk(tmpPath);
            File distFile = new File(tmpPath+File.separator+"dist"+File.separator+apkFileName+".apk");
            File newApkFile = new File(currentPath+File.separator+"channelApk"+File.separator+channelId+"_"+apkFileName);
            FileUtils.copyFile(distFile, newApkFile);
        }
    }

    // 自动生产渠道打包文件
    public static void autoMerageApk(CommandLine cli) throws Exception {
        String configFile=null;
        if(cli.hasOption("f")){
            configFile=cli.getOptionValue("f");
        }else{
            configFile=currentPath + File.separator + "src" + File.separator + "configs.properties";
        }
        Properties pros=PropertiesUtil.loadFile(configFile);
        Invoke invoke=new Invoke(pros);

        String workspace=pros.getProperty("workspace.dir" + File.separator, currentPath + File.separator + "workspace" + File.separator);

        // apk的路径
        String srcApkFile=pros.getProperty("src.apk");
        String targetApkFile=pros.getProperty("target.apk");

        Map<String, String> pathMap=invoke.initWithCreatePath(workspace, srcApkFile, targetApkFile);
        Invoke.setPathMap(pathMap);

        // 指定解压后的目录
        invoke.cmdDecodeApk(srcApkFile, "-fo", pathMap.get("srcApkPath"));
        invoke.cmdDecodeApk(targetApkFile, "-fo",  pathMap.get("targetApkPath"));
        invoke.createManifestXmlFile();
        invoke.merageResAndXmlFiles();
        invoke.initWithYmlFile();
        BuildApk.main(new String[]{pathMap.get("temp")});
        invoke.cmdDecodeApk(pathMap.get("temp") + "temp.apk", "-fo", pros.getProperty("workspace.dir")+"_temp", "-f");
        invoke.cleanBuildPath();
        invoke.merageAll();
        invoke.cmdBuildApk(pathMap.get("temp"));
        invoke.signApk(pathMap.get("temp") + "dist" + File.separator + "temp.apk");
        invoke.done();
    }
    
    private static Options createOptions(){
        OptionBuilder.withLongOpt("merage"); 
        OptionBuilder.hasArg(false); 
        OptionBuilder.withDescription("m 自动生成渠道包"); 
        Option merageOption = OptionBuilder.create("m");
        merageOpt.addOption(merageOption);
        allOpt.addOption(merageOption);

        OptionBuilder.withLongOpt("file"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("配置文件路径，如果同该脚本放在同一目录该参数可省略， 否则必须要指定configs.properties的路径"); 
        Option fileOption = OptionBuilder.create("f");
        merageOpt.addOption(fileOption);
        allOpt.addOption(fileOption);
        
        OptionBuilder.withLongOpt("channel"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("c 分发渠道包功能"); 
        Option channelOption = OptionBuilder.create("c");
        channelOpt.addOption(channelOption);
        allOpt.addOption(channelOption);

        OptionBuilder.withLongOpt("apkfile"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("要进行分包处理的apk路径"); 
        Option apkOption = OptionBuilder.create("a");
        channelOpt.addOption(apkOption);
        allOpt.addOption(apkOption);
        
        OptionBuilder.withLongOpt("encode"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("apk加壳"); 
        Option encodeApkOption = OptionBuilder.create("e");
//        channelOpt.addOption(encodeApkOption);
        
        
        allOpt.addOption(merageOption);
        allOpt.addOption(channelOption);
        return allOpt;
    }
}
