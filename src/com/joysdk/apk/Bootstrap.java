package com.joysdk.apk;

import java.io.File;
import java.util.List;
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

    public static void main(String[] args) throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = null;
        Options option=createOptions();
        String formatstr = "makeapktool ";
        try {
            commandLine = parser.parse(option, args, false);
            if(commandLine.hasOption("m")){
                autoMerageApk(commandLine);
            }else if(commandLine.hasOption("c")){
                createChannelApk(commandLine);
            }else if(commandLine.hasOption("s")){
                
            }else{
                throw new ParseException("");
            }
        } catch (ParseException e) {
            formatter.printHelp(formatstr, option); // 如果发生异常，则打印出帮助信息
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
        String apkFileName = apkFilePath.substring(apkFilePath.lastIndexOf(File.separator));
        
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
        invoke.cmdDecodeApk(apkFilePath, tmpPath);
        for(String channelId : channelIds){
            apk.updateChannelId(channelId);
            invoke.cmdBuildApk(tmpPath);
            File distFile = new File(tmpPath+File.separator+"dist"+File.separator+apkFileName);
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
        Options options=new Options();
        
        OptionBuilder.withLongOpt("merage"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("m 自动生成渠道包"); 
        Option merageOption = OptionBuilder.create("m");
        options.addOption(merageOption);
        
        OptionBuilder.withLongOpt("channel"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("c 分发渠道包功能"); 
        Option channelOption = OptionBuilder.create("c");
        options.addOption(channelOption);

        OptionBuilder.withLongOpt("encode"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("apk加壳"); 
        Option encodeApkOption = OptionBuilder.create("e");
        options.addOption(encodeApkOption);
        

        OptionBuilder.withLongOpt("file"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("配置文件路径，如果同该脚本放在同一目录该参数可省略， 否则必须要指定configs.properties的路径"); 
        Option fileOption = OptionBuilder.create("f");
        options.addOption(fileOption);
        

        OptionBuilder.withLongOpt("apkfile"); 
        OptionBuilder.hasArg(true); 
        OptionBuilder.withDescription("要进行分包处理的apk路径"); 
        Option apkOption = OptionBuilder.create("a");
        options.addOption(apkOption);
        
        return options;
    }
}
