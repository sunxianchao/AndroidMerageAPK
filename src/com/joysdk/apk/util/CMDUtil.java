package com.joysdk.apk.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class CMDUtil {

    private static CMDUtil instance;
    
    private CMDUtil(){}
    
    public static CMDUtil getInstance(){
        if(instance == null){
            instance=new CMDUtil();
        }
        return instance;
    }
    
    public int runCmd(String[] commands) {
        StringBuilder buf=new StringBuilder("正在执行：");
        for(int i=0; i < commands.length; i++) {
            buf.append(commands[i]).append(" ");
        }
        System.out.print(buf.toString());
        try {
            Process process=Runtime.getRuntime().exec(commands);
            WatchThread wt = new WatchThread(process); 
            wt.start(); 
            process.waitFor();
            wt.setOver(true);
            int leng=0;
            byte b[]=null;
            BufferedInputStream stream=new BufferedInputStream(process.getInputStream());
            while((leng=stream.available()) > 0) {
                b=new byte[leng];
                stream.read(b);
                String inf=new String(b, "GBK");
                System.out.println("\n信息2：" + inf.trim());
            }
            BufferedInputStream errstream=new BufferedInputStream(process.getErrorStream());
            leng=errstream.available();
            b=new byte[leng];
            errstream.read(b);
            String merror=new String(b, "GBK");
            System.out.println("\n信息1：" + merror.trim());

            if(process.exitValue() != 0) {
                new Exception().printStackTrace();
                System.exit(-1);
            }
            return process.exitValue();//成功执行返回0
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    class WatchThread extends Thread {
        Process p;

        boolean over;

        public WatchThread(Process p) {
            this.p=p;
            over=false;
        }

        public void run() {
            try {
                if(p == null)
                    return;
                BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
                while(true) {
                    if(p == null || over) {
                        break;
                    }
                    while(br.readLine() != null);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public void setOver(boolean over) {
            this.over=over;
        }
    }
}
