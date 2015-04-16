package com.joysdk.apk.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class ManifestMerageUtil {

    /**
     * 合并AndroidManifest.xml文件
     * @param sourcePath
     * @param targetPath
     * @param toPath
     */
    public static void merageManifestXML(String sourceManifestFile, String targetManifestFile, Properties properties){
        SAXReader saxReader = new SAXReader();
        Map<String, Element> userPermissionsMap=new LinkedHashMap <String, Element>();
        Map<String, Element> activityMap=new LinkedHashMap<String, Element>();
        try {
            Document srcDoc=saxReader.read(new File(sourceManifestFile));
            Document targetDoc=saxReader.read(new File(targetManifestFile));
            
            Element srcRoot=srcDoc.getRootElement();
            Element targetRoot=targetDoc.getRootElement();
            
            List<Element> srcUserPermissions=srcRoot.elements("uses-permission");
            List<Element> targetUserPermissions=targetRoot.elements("uses-permission");
            addElementToMap(userPermissionsMap, srcUserPermissions, "name");
            addElementToMap(userPermissionsMap, targetUserPermissions, "name");
            
            List<Element> permissions=srcRoot.elements("permission");
            List<Element> targetPermissions=targetRoot.elements("permission");
            addElementToMap(userPermissionsMap, permissions, "name");
            addElementToMap(userPermissionsMap, targetPermissions, "name");
            
            Element srcAppNode=srcRoot.element("application");
            Element targetAppNode=targetRoot.element("application");
            List<Element> srcAppChildrens=srcAppNode.elements();
            List<Element> targetAppChildrens=targetAppNode.elements();
            addElementToMap(activityMap, srcAppChildrens, "name");
            addElementToMap(activityMap, targetAppChildrens, "name");
            
            Document newFileDoc = DocumentHelper.createDocument();
            Element newRoot=newFileDoc.addElement("manifest");
            copyNodeAttribute(targetRoot, newRoot);
            if(newRoot.attribute("xmlns:android") == null){
                newRoot.addAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
            }
            newRoot.attribute("package").setValue(properties.getProperty("apk.package.name"));
            if(newRoot.attribute("versionName") == null){
                newRoot.addAttribute("versionName", properties.getProperty("apk.version.name"));
            }
            if(newRoot.attribute("versionCode") == null){
                newRoot.addAttribute("versionCode", properties.getProperty("apk.version.code"));
            }
            
            // 生成只有manifest节点的文件用于生成R文件
            String manifestFile=new File(targetManifestFile).getParent() + File.separator +"AndroidManifest.xml";
            writeXML(manifestFile, newFileDoc);
            
            Iterator<String> it=userPermissionsMap.keySet().iterator();
            while(it.hasNext()){
                String key=it.next();
                newRoot.add((Element)userPermissionsMap.get(key).clone());
            }
            
            Element newAppNode=newRoot.addElement("application");
            copyNodeAttribute(srcAppNode, newAppNode);
            
            it=activityMap.keySet().iterator();
            while(it.hasNext()){
                String key=it.next();
                newAppNode.add((Element)activityMap.get(key).clone());
            }
            String tempManifestFile= properties.getProperty("workspace.dir") + File.separator +"AndroidManifest.xml";
            writeXML(tempManifestFile, newFileDoc);
            
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private static void writeXML(String fileName, Document doc){
        XMLWriter xmlWriter=null;
        try {
            FileWriter fileWriter = new FileWriter(fileName);  
            OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
            xmlFormat.setEncoding("utf-8");
            xmlWriter = new XMLWriter(fileWriter, xmlFormat);  
            xmlWriter.write(doc);
        } catch(Exception e) {
            e.printStackTrace();
        }finally{
            try {
                xmlWriter.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static String getPackageName(String manifest){
        SAXReader saxReader = new SAXReader();
        try {
            Document srcDoc=saxReader.read(new File(manifest));
            Element root=srcDoc.getRootElement();
            return root.attribute("package").getStringValue();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 将元素集合中的元素按照某个属性的唯一性添加到map中
     * @param fillMap
     * @param elements
     * @param attrName
     * @return
     */
    private static Map<String, Element> addElementToMap(Map<String, Element> fillMap, List<Element> elements, String attrName){
        if(fillMap == null){
            fillMap=new HashMap<String, Element>();
        }
        for(Element element: elements){
            Attribute attr=element.attribute(attrName);
            fillMap.put(attr.getStringValue(), element);
        }
        return fillMap;
    }
    
    /**
     * 复制某个节点属性
     * @param srcElement
     * @param targetElement
     */
    private static void copyNodeAttribute(Element srcElement, Element targetElement){
        Iterator<Attribute> it=srcElement.attributes().iterator();
        while(it.hasNext()){
            Attribute at=it.next();
            targetElement.add((Attribute)at.clone());
        }
    }
    
}