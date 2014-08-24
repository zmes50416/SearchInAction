package tw.edu.ncu.sia.util;

import javax.swing.JOptionPane;

public class Config {
	public static String hosturl = "http://localhost/searchweb";
	public static String queryPage = "/action/query.jsp";
	public static String docfolder = "E:/enwiki-20120104-pages-articles";
	public static String title = "Search In Action Example";
	public static String version = "v0.2"; 
	public static String author = "John";
	public static String authorEmail = "youremailaddr@mail.com"; 
	public static String updatedTime = "2012/01/20"; 
	
	public static void setServerURL(){
		String url = JOptionPane.showInputDialog("Server URL:"
				+ "\n(e.g., " + hosturl + " )");
		if(url==null||url.equals("")){
			JOptionPane.showMessageDialog(null, "Default Server URL:\n" + hosturl);
		}else{
			hosturl = url;
			JOptionPane.showMessageDialog(null, "Server URL:" + hosturl);
		}
	}
	
	public static void setDocFolder(){
		String folder = JOptionPane.showInputDialog("DocFolder:"
				+ "\n(e.g.," + docfolder + " )");
		if(folder==null||folder.equals("")){
			JOptionPane.showMessageDialog(null, "Default Folder:\n" + docfolder);
		}else{
			docfolder = folder;
			JOptionPane.showMessageDialog(null, "Folder: " + docfolder);
		}
	}
}
