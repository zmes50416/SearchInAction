package tw.edu.ncu.sia.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.solr.client.solrj.response.UpdateResponse;

/**
 * <p>access ServerConnection, can test or update server. should choose between Common or StreamUpdated ServerClass </p>
 * {@code
 * 		ServerUtil.testServerConnected() will connect Server to Config.url
 * 		
 * }
 * @param args
 * @throws Exception
 */
public class ServerUtil {
	
	private static int BATCHSIZE = Integer.parseInt(Config.pref.getProperty("BatchSize","50000"));//how many docs Added before Commit, higher should increase speed but haven't know the side effect yet
	private static int docsize = 0;
	private volatile static SolrServer server=null; // Singleton Design pattern only access it by getServer() to ensure connection
	
	public static SolrServer getServer(){
		if(server==null){
			synchronized(ServerUtil.class){
				if(server==null){
					initialize();
				}
			}
		}
		return server;
			
	}

	private static Boolean initialize(){
		String url = Config.hosturl;
		ConcurrentUpdateSolrServer mServer = new ConcurrentUpdateSolrServer(url,200,10); // last two parameter will determined by Computer Power. Higher mean more speedy Index
		mServer.setSoTimeout(5000); // socket read timeout
		mServer.setConnectionTimeout(3000);
		server = mServer;
		return true;
	}
	// If using batch adding method, Remember to call commit last time
	public static void commit() throws SolrServerException, IOException{
		UpdateResponse commitRespond = getServer().commit(true, true);
		if(commitRespond.getStatus()!=0){ //Dont trust status, it only show connection problem, see admin log to prevent Server side problem(ex:undifined field)
			throw new SolrServerException("committing failed");
		}
	}
	//Dont do commit on every document added, batch add then commit
	public static void addDocument(SolrInputDocument doc) throws SolrServerException, IOException{
	
		UpdateResponse respond = getServer().add(doc);
		if(docsize++ >= BATCHSIZE){
			commit();
			docsize = 0;
		}
	}

	public static void deleteDocByID(String id) throws Exception {
		getServer().deleteById(id);
		getServer().commit();
	}
	
	public static void deleteSingleProject(String pid) throws Exception {
		getServer().deleteByQuery("id:/" + pid.trim() + "/*");
		getServer().commit();
	}
	public static void deleteAllDoc() throws Exception{
		getServer().deleteByQuery("*:*");
		getServer().commit();
	}
	
	public static SolrDocumentList query(String queryStr) throws Exception {
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		SolrDocumentList docs = null;
		try {
			QueryResponse rsp = getServer().query(query);
			docs = rsp.getResults();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}finally{
			return docs;
		}
	
	}
	
	
	public void search(String queryStr){
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		//query.setSortField("file", ORDER.asc);
		query.setHighlight(true).setHighlightSnippets(3);
		query.setParam("hl.fl", "*"); 
		//hl.fragsize
		//query.setParam("hl.fragsize", "300");
		//only the filed was assigned in a query, the highlight snippet can be showed.
		query.setParam("hl.requireFieldMatch", "true"); 
		
		try {
			QueryResponse rsp = getServer().query(query);
			SolrDocumentList docs = rsp.getResults();
			System.out.println("Count:" + docs.getNumFound());
			System.out.println("Time:" + rsp.getQTime());
			for (SolrDocument doc : docs) {
				String id = (String) doc.getFieldValue("id");
				String file = (String) doc.getFieldValue("file");
				System.out.println("\n" + file);
				
				//highlight
				System.out.println("****highlight begin***");
				if (rsp.getHighlighting().get(id) != null) {
					List<String> highlightSnippets =
					rsp.getHighlighting().get(id).get("methodbody");
					List<String> highlightSnippets1 =
						rsp.getHighlighting().get(id).get("field");
					
					if(highlightSnippets!=null){
						for(String s : highlightSnippets){
							System.out.println("[methodbody] " + s);
						}
					}
					
					if(highlightSnippets1!=null){
						for(String s : highlightSnippets1){
							System.out.println("[field] " + s);
						}
					}
					
				}
				System.out.println("****highlight end***");
			
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	public void fieldTopTermsToRows(int terms) throws Exception{
		SolrQuery query = new SolrQuery();
		query.setTerms(true);
		query.setTermsLimit(terms); //amount of response terms for example, 50
		//query.setTermsPrefix("n");
		query.setQueryType("/terms");
		
		String[] fields = new String[]{"file","class","method","comment","package","import"
				,"super","interface","field","methodsign","methodbody","return"};
		try {
			for(String field : fields){
				query.addTermsField(field);
				QueryResponse qr = getServer().query(query);
				TermsResponse resp = qr.getTermsResponse();
				List<Term> items = resp.getTerms(field);
				System.out.println("==field: " + field + " =======");
				int i = 1;
				String fieldValue = "";
				for(Term t : items){
					System.out.println("[" + (i++) + "] " + t.getTerm() + "  (fq:" + t.getFrequency() + ")");
					for(int j=0;j<t.getFrequency();j++){
						fieldValue = fieldValue + " " + t.getTerm();
					}
				}
				System.out.println("\n** Field value: " + fieldValue);
				if(items!=null){
					SolrInputDocument doc = new SolrInputDocument();
					doc.addField("id", "meta_" + field, 1.0f);
					doc.addField("metafield_value", fieldValue, 1.0f);
					getServer().add(doc);
					getServer().commit();
				}
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HashSet<String> getIndexedProj() throws Exception{
		initialize();
		// initialize query
		SolrQuery query = new SolrQuery();
		//show info of all files
		query.setQuery("file:*");
		QueryResponse rsp = getServer().query(query);
		SolrDocumentList docs = rsp.getResults();
		query.setParam("rows",""+docs.getNumFound());
		query.setSortField("id", ORDER.asc);
		rsp = getServer().query(query);
		docs = rsp.getResults();
		HashSet<String> projects = new HashSet<String>();
		if(docs.getNumFound()>0){
			for (SolrDocument doc : docs) {
				String id = (String) doc.getFieldValue("id");
				if(!id.equals("metarow")){
					projects.add(id.substring(1,id.indexOf("/", 1)));	
				}
			}
		}
		return projects;
	}
	
	public static QueryResponse execQuery(SolrQuery s){
		try {
			return getServer().query(s);
		} catch (SolrServerException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static boolean testServerConnected(){
			try {
				SolrPingResponse ping = getServer().ping();
				System.out.println("Server connected, ping: "+ping.getQTime()+"ms");
				return true;
			} catch (SolrServerException | IOException e) {
				e.printStackTrace();
				return false;
			}
	}
}
