package com.joysdk.apk;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;


public class ChannelApk {

    String currentPath=System.getProperty("user.dir");
    
    public List<String> readChannelId() throws Exception{
        String channelsFile = currentPath+File.separator+"channels.txt";
        List<String> lines=FileUtils.readLines(new File(channelsFile), "utf-8");
        return lines;
    }
    
    public void updateChannelId(String channelId) throws Exception{
        String filePath=currentPath + File.separator +"temp"+File.separator+"res"+File.separator+"raw"+File.separator+"yunyoyo.bin";
        File file = new File(filePath);
        List<String> lines=FileUtils.readLines(file, "utf-8");
        
        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            if(line.contains("PROMPT_CHANNEL")){
                line = line.replaceAll("PROMPT_CHANNEL=.*", "PROMPT_CHANNEL="+channelId);
                lines.remove(i);
                lines.add(i, line);
                break;
            }
        }
        
        FileUtils.writeLines(new File(filePath), lines);
    }
}
