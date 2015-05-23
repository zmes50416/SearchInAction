package tw.edu.ncu.sia.index;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.*;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.common.SolrInputDocument;

import tw.edu.ncu.sia.util.Config;
import tw.edu.ncu.sia.util.ServerUtil;

/** Index all text files under a directory. */
public class DocIndexing extends Observable{
	public Queue<File> errorDocs;
	public Stack<File> fileToWork;
	public volatile int timesOfError;
	public static final int MAX_THREAD_NUM = 15;
	public static ExecutorService taskExecutor = Executors.newSingleThreadExecutor();//Using MultiThread will cause lot more error
	public DocIndexing(){
		errorDocs = new ConcurrentLinkedQueue<File>();
		timesOfError = 0;
	}
	/** Index all text files under a directory. */
	public void preProcess(File documentDir){
		if (!documentDir.exists() || !documentDir.canRead()) {
			System.err.println("\nFile '"
							+ documentDir.getAbsolutePath()
							+ "' does not exist or is not readable, please check the path");
			return;
		}else{
			taskExecutor.execute(new consumeTask(this,documentDir));
		}
	}
	
	void processTXT(File file) throws SolrServerException, IOException{
		// makes a solr document
		SolrInputDocument solrdoc = new SolrInputDocument();
		String fileID = file.getAbsolutePath().replace('\\', '/');
		fileID = fileID.substring(fileID.indexOf("docfolder")+9);
		solrdoc.addField("id", fileID);
		System.err.println("*** id:" + fileID);
				
		solrdoc.addField("text", getFileTxt(file));
		
		// Added to Server and wait for commit
		ServerUtil.addDocument(solrdoc);
		
	}
	void processHTML(File file) throws SolrServerException, IOException{
		// makes a solr document
		
		SolrInputDocument solrdoc = new SolrInputDocument();
		String fileID = file.getAbsolutePath().replace('\\', '/');
		fileID = fileID.substring(fileID.indexOf("docfolder")+9);
		solrdoc.addField("id", fileID);
		try {
			String content = FileUtils.readFileToString(file);
			solrdoc.addField("text", content);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
			ServerUtil.addDocument(solrdoc);
		
	}
	
	static  StringBuffer getFileTxt(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			StringBuffer sourceStr = new StringBuffer();
			String str = "";
			while((str = br.readLine()) != null){
				sourceStr.append("\n").append(str);
			}
			return sourceStr;
		} catch (Exception e) {
			return null;
		}
		

	}
	
	void processPDF(File file) throws IOException, SolrServerException{
		  SolrServer server = new HttpSolrServer(Config.hosturl);

		  ContentStreamUpdateRequest up = new ContentStreamUpdateRequest("/update/extract");
		  up.addFile(file,"application/pdf");
		  String id = file.getName().substring(file.getName().lastIndexOf('/')+1);
		  System.out.println(id);

		  up.setParam("literal.id", id);
		  up.setParam("uprefix", "attr_");
		  up.setParam("fmap.content", "attr_content");
		  up.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
		  server.request(up);
	}
	
	private void errorReport(String id,Exception errorEvent){
		try {
			String dirName = FilenameUtils.getPath(id).replaceAll("/", "");
			FileWriter eStream = new FileWriter(dirName+"ErrorReport.txt",true);
			BufferedWriter eWriter = new BufferedWriter(eStream);
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			eWriter.write(time+"--"+id);
			eWriter.write(":"+errorEvent.getMessage());
			eWriter.newLine();
			eWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 方便再度重試失敗文件
	 */
	public void retryError(){
		Stack<File> temp = new Stack<File>();
		while (!errorDocs.isEmpty()) {
			File retryDoc = errorDocs.poll();
			try {
				if (retryDoc.getName().endsWith(".txt")) {
					processTXT(retryDoc);
				} else if (retryDoc.getName().endsWith(".htm")
						|| FilenameUtils.getExtension(retryDoc.getName())
								.equals("html")) {
					processHTML(retryDoc);
				}
			} catch (SolrServerException | IOException e) {
				temp.push(retryDoc);
			}
		}
		errorDocs.addAll(temp);
		this.setChanged();
		this.notifyObservers("Retry finished. " + errorDocs.size() + "error left");
		
	}
	private class consumeTask implements Runnable{
		DocIndexing indexer;
		File documentDir;
		consumeTask(DocIndexing indexer,File dir){
			this.indexer = indexer;
			documentDir = dir;
		}
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_NUM);
			
			for(File document:documentDir.listFiles()){ //don't change i initialize value or it won't show time
				executor.execute(new DocumentIndexer(document));
			}
			
			executor.shutdown();
			try {
				executor.awaitTermination(Integer.MAX_VALUE,TimeUnit.MILLISECONDS);
				long end = System.currentTimeMillis();
				indexer.setChanged();
				indexer.notifyObservers("Dir "+documentDir.getName()+"have finished process, Total time of "+(end-start));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}						
		}
		
		
	}
	
	class DocumentIndexer implements Runnable{
		private File document;
		DocumentIndexer(File dir){
			this.document = dir;
		}
		@Override
		public void run(){
			if (document.canRead()) {
				indexDocument(document);
			}
		}
		
		void indexDocument(File file) {
			try {
				if (file.getName().endsWith(".txt")) {
					processTXT(file);
				} else if (file.getName().endsWith(".htm")
						|| FilenameUtils.getExtension(file.getName()).equals("html")) {
					processHTML(file);
				}
			} catch (SolrServerException | IOException e) {
				timesOfError++;
				errorDocs.add(file);
				String fileID = file.getAbsolutePath().replace('\\', '/');
				fileID = fileID.substring(fileID.indexOf("docfolder") + 9);
				errorReport(fileID, e);
				System.err.println("wait for one second");
				synchronized (DocIndexing.taskExecutor) {
					try {
						taskExecutor.wait(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	
}


