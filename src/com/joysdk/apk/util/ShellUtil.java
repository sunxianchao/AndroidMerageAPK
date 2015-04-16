package com.joysdk.apk.util;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;


public class ShellUtil {

    public static int runScript(String... command){
        StringBuilder sb=new StringBuilder();
        for(String c: command){
            sb.append(c).append(" ");
        }
        String sCommandString=sb.toString().trim();
        System.out.println(sCommandString);
        CommandLine oCmdLine = CommandLine.parse(sCommandString);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        int iExitValue=0;
        try {
            iExitValue=oDefaultExecutor.execute(oCmdLine);
        } catch (ExecuteException e) {
            System.err.println("Execution failed.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("permission denied.");
            e.printStackTrace();
        }
        return iExitValue;
    }
}
