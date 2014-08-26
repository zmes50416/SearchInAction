package tw.edu.ncu.sia.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Config {
	public static Properties pref = loadConfig();
	public static String hosturl = pref.getProperty("ServerURL");
	public static String queryPage = "/action/query.jsp";
	public static String docfolder =  pref.getProperty("docPath");
	public static String title = "Search In Action Example";
	public static String version = "v0.2"; 
	public static String author = "John";
	public static String authorEmail = "youremailaddr@mail.com"; 
	public static String updatedTime = "2012/01/20"; 
	
	public static Properties loadConfig(){
		Properties properties = new Properties();
		try {
			properties.loadFromXML(new FileInputStream("setting.xml"));
		} catch (InvalidPropertiesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}
	
	
	public static void setServerURL(){
		String url = JOptionPane.showInputDialog("Server URL:"
				+ "\n(e.g., " + hosturl + " )");
		if(url==null||url.equals("")){
			JOptionPane.showMessageDialog(null, "Default Server URL:\n" + hosturl);
		}else{
			saveSetting("ServerURL", url);
			hosturl = pref.getProperty("ServerURL");
			JOptionPane.showMessageDialog(null, "Server URL:" + hosturl);
		}
	}
	
	public static void setDocFolder(){
		String folder = JOptionPane.showInputDialog("DocFolder:"
				+ "\n(e.g.," + docfolder + " )");
		if(folder==null||folder.equals("")){
			JOptionPane.showMessageDialog(null, "Default Folder:\n" + docfolder);
		}else{
			saveSetting("docPath", folder);
			docfolder = pref.getProperty("docPath");
			JOptionPane.showMessageDialog(null, "Folder: " + docfolder);
		}
	}
	public static boolean saveSetting(String key, String value){
		pref.setProperty(key, value);
		FileOutputStream setStream;
		try {
			setStream = new FileOutputStream("setting.xml");
			pref.storeToXML(setStream, "setting");
			setStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
