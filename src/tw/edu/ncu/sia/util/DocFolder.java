package tw.edu.ncu.sia.util;

import java.io.File;

import javax.swing.JTextArea;


public class DocFolder {
	public static void show(JTextArea textArea){
		textArea.setCaretPosition(textArea.getDocument().getLength());
		File dir = new File(Config.docfolder);
		File[] files = dir.listFiles();
		if (files == null) {
			textArea.append("\nNo file in the docfolder!");
		} else {
			textArea.append(" File(s) or folder(s) in the docfolder");
			textArea.append("\n***********************************");
		    for (int i=0; i<files.length; i++) {
			  // Get filename of file or directory
			  if(files[i].isDirectory()){
			  	textArea.append("\n" + files[i].getName() + " <dir>");
			  }else{
				  textArea.append("\n" + files[i].getName());  
			  }
			}
		    textArea.append("\n***********************************");
			textArea.append("\nTotal: " + files.length);
		}
	}
}
