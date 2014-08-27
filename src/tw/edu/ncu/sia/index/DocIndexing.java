package tw.edu.ncu.sia.index;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.concurrent.*;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import tw.edu.ncu.sia.util.Config;
import tw.edu.ncu.sia.util.ServerUtil;

/** Index all text files under a directory. */
public class DocIndexing {
	public static JTextArea textArea = null;
	private static long fileCount = 0;
	public static Stack<SolrInputDocument> errorDocs = new Stack<SolrInputDocument>();
	public static int timesOfError=0;
	public static ConcurrentLinkedQueue<File> theFiles = new ConcurrentLinkedQueue<File>();
	private static Date start;
	/** Index all text files under a directory. */
	public static void preProcess(String docName, JTextArea ta){
		ServerUtil.testServerConnected();
		fileCount = 0;
		textArea = ta;
		File file = null;
		if(docName!=null){
			file = new File(Config.docfolder + "/" + docName);
		}else{
			return;
		}
		
		if (!file.exists() || file == null || !file.canRead()) {
			textArea.append("\nFile '"
							+ file.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			return;
		}

		start = new Date();
		textArea.append("\nIndexing a doc:\n  " + file.getName());
		queueIndex(file);
		
	}
	static void queueIndex(File dir){
		// do not try to index files that cannot be read
		if (dir.canRead()) {
			if (dir.isDirectory()) {
				String[] files = dir.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						File targetFile = new File(dir, files[i]);
						theFiles.add(targetFile);//Add to Queue
						//indexDocs(targetFile);
						if(i==files.length-1){	//When File Adding Finished, remember to commit it
							startParallexIndexing();
						}
					}
				}
			}
		}
	}
	static void startParallexIndexing(){
		Executor executor = Executors.newFixedThreadPool(2);
		executor.execute(new Runnable(){

			@Override
			public void run() {
				File file;
				while((file = theFiles.poll()) != null){
					indexDocs(file);
				}
				try {
					ServerUtil.commit();
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Date end = new Date();
				textArea.append("\n" + (end.getTime() - start.getTime())+ " total milliseconds");
				textArea.append("\n Total Error number:"+DocIndexing.timesOfError);
			}
			
		});
		executor.execute(new Runnable(){

			@Override
			public void run() {
				File file;
				while((file = theFiles.poll()) != null){
					indexDocs(file);
				}
				try {
					ServerUtil.commit();
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
	}
	static void indexDocs(File file){
		if (file.getName().endsWith(".txt")) {
				fileCount++;   //count of files
				textArea.append("\n[" + fileCount + "] " + file.getName());
				processTXT(file);
			
			
			}
		else if(file.getName().endsWith(".htm")||FilenameUtils.getExtension(file.getName()).equals("html")){
				fileCount++;
				textArea.append("\n["+fileCount+"] "+ file.getName());
				processHTML(file);
		}
	}
	
	static void processTXT(File file){
		// makes a solr document
		SolrInputDocument solrdoc = new SolrInputDocument();
		String fileID = file.getAbsolutePath().replace('\\', '/');
		fileID = fileID.substring(fileID.indexOf("docfolder")+9);
		solrdoc.addField("id", fileID);
		System.err.println("*** id:" + fileID);
				
		solrdoc.addField("text", getFileTxt(file));
		
		// Added to Server and wait for commit
		try {
			ServerUtil.addDocument(solrdoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	static void processHTML(File file){
		// makes a solr document
		SolrInputDocument solrdoc = new SolrInputDocument();
		String fileID = file.getAbsolutePath().replace('\\', '/');
		fileID = fileID.substring(fileID.indexOf("docfolder")+9);
		solrdoc.addField("id", fileID);
//		System.out.println("*** id:" + fileID);
				
		solrdoc.addField("text", getFileTxt(file));
		//  Added to Server and wait for commitr
		try {
			ServerUtil.addDocument(solrdoc);
		} catch (Exception e) {
			timesOfError++;
			errorDocs.add(solrdoc);
			errorReport(fileID,e);
		}
		
	}
	
	static  StringBuffer getFileTxt(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuffer sourceStr = new StringBuffer();
			String str = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			while((str = br.readLine()) != null)
				sourceStr.append("\n").append(str);
			reader.close();
			return sourceStr;
		} catch (Exception e) {
			return null;
		}

	}
	
	static void processPDF(File file) throws IOException, SolrServerException{
		  SolrServer server = new CommonsHttpSolrServer(Config.hosturl);

		  ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
		  up.addFile(file);
		  String id = file.getName().substring(file.getName().lastIndexOf('/')+1);
		  System.out.println(id);

		  up.setParam("literal.id", id);
		  up.setParam("uprefix", "attr_");
		  up.setParam("fmap.content", "attr_content");
		  up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		  server.request(up);
	}
	
	private static void errorReport(String id,Exception e){
		try {
			String dirName = FilenameUtils.getPath(id).replaceAll("/", "");
			FileWriter eStream = new FileWriter(dirName+"ErrorReport.txt",true);
			BufferedWriter eWriter = new BufferedWriter(eStream);
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			eWriter.write(time+"--"+id);
			eWriter.write(":"+e.getMessage());
			eWriter.newLine();
			eWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	/** The thread can be designed to process multiple types of input doc,
	 *   e.g., a single doc, multiple doc names separated by ",", or a folder name.
	 */
	class IndexerThread extends Thread {
		public void run() {
			File file;
			while((file = theFiles.poll()) != null){
				indexDocs(file);
			}
		}
	}
}


