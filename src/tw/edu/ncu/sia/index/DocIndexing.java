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
	JTextArea textArea = null;
	private long fileCount;
	public Stack<SolrInputDocument> errorDocs;
	public int timesOfError;
	public ConcurrentLinkedQueue<File> theFiles;
	public static final int MAX_THREAD_NUM = 3;
	private Date start;
	private boolean workIsDone; 
	public DocIndexing(){
		errorDocs = new Stack<SolrInputDocument>();
		fileCount = 0;
		timesOfError = 0;
		theFiles =new ConcurrentLinkedQueue<File>();
		workIsDone = false;
	}
	/** Index all text files under a directory. */
	public void preProcess(String docName, JTextArea ta){
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
		startqueueIndex(file);
		startParallexIndexing();
	}
	void startqueueIndex(File file){
		try {
			new QueueIndexer(file).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void startParallexIndexing(){
		//Executor executor = Executors.newFixedThreadPool(MAX_THREAD_NUM);
		
		for(int i=0;i<MAX_THREAD_NUM;i++){ //don't change i initialize value or it won't show time
			//executor.execute(new IndexerThread(i));
			new IndexerThread(i).start();
		}
	}
	
	void indexDocs(File file){
		if (file.getName().endsWith(".txt")) {
				fileCount++;   //count of files
				processTXT(file);			
			}
		else if(file.getName().endsWith(".htm")||FilenameUtils.getExtension(file.getName()).equals("html")){
				fileCount++;
				processHTML(file);
		}
		textArea.append("\n["+fileCount+"] "+ file.getName());
		
	}
	
	void processTXT(File file){
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
	void processHTML(File file){
		// makes a solr document
		SolrInputDocument solrdoc = new SolrInputDocument();
		String fileID = file.getAbsolutePath().replace('\\', '/');
		fileID = fileID.substring(fileID.indexOf("docfolder")+9);
		solrdoc.addField("id", fileID);
				
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
	
	void processPDF(File file) throws IOException, SolrServerException{
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
	
	private void errorReport(String id,Exception e){
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
	//bottleNeck!! It need a lot of time to queue thouthands of file
	//TODO maybe use Proudcer & Consumer Design Pattern?
	class QueueIndexer extends Thread{
		// do not try to index files that cannot be read
		File dir;
		QueueIndexer(File dir)throws Exception{
			this.dir = dir;
		}
		@Override
		public void run(){
			if (dir.canRead()) {
				if (dir.isDirectory()) {
					String[] files = dir.list();
					// an IO error could occur
					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							File targetFile = new File(dir, files[i]);
							theFiles.add(targetFile);//Add to Queue
						}
					}
				}
			}
			workIsDone = true;
		}
		
		
	}
	/** The thread can be designed to process multiple types of input doc,
	 *   e.g., a single doc, multiple doc names separated by ",", or a folder name.
	 */
	class IndexerThread extends Thread {
		private int id;
		IndexerThread(int id){
			this.id = id;
		}
		@Override
		public void run() {
			File file;
			while(true){
				if((file = theFiles.poll()) != null)
					indexDocs(file);
				else if(workIsDone){
					break;
				}
				else{
					yield();
				}
			}
			
			if(id == 0){//Only one Thread need to report the Error condition
				try {
					ServerUtil.commit();
				} catch (Exception e) {
					e.printStackTrace();
					errorReport("commit/commit error",e);
				}
				long timeSpended = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - start.getTime());
				textArea.append("\n" + timeSpended + " total seconds");
				textArea.append("\n Total Error number:"+timesOfError);
			}
		}
	}
}


