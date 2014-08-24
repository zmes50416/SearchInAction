package tw.edu.ncu.sia.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;


public class ServerUtil {
	public static CommonsHttpSolrServer server = null;

	public static void initialize() throws Exception {
		String url = Config.hosturl;
		server = new StreamingUpdateSolrServer(url,150,10);
		server.setSoTimeout(1000); // socket read timeout
		server.setConnectionTimeout(1000);
		server.setDefaultMaxConnectionsPerHost(100);
		server.setMaxTotalConnections(100);
		server.setFollowRedirects(false); // defaults to false
		server.setAllowCompression(false);
		server.setMaxRetries(1); // defaults to 0. > 1 not recommended.
	}

	public static void addDocument(SolrInputDocument doc) throws Exception {
		server.add(doc);
		server.commit();
	}

	public void deleteDocByID(String id) throws Exception {
		server.deleteById(id);
		server.commit();
	}
	
	public void deleteSingleProject(String pid) throws Exception {
		server.deleteByQuery("id:/" + pid.trim() + "/*");
		server.commit();
	}
	public void deleteAllDoc() throws Exception{
		server.deleteByQuery("*:*");
		server.commit();
	}
	
	public static SolrDocumentList query(String queryStr) throws Exception {
		SolrQuery query = new SolrQuery();
		query.setQuery(queryStr);
		SolrDocumentList docs = null;
		try {
			QueryResponse rsp = server.query(query);
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
			QueryResponse rsp = server.query(query);
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
				QueryResponse qr = server.query(query);
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
					server.add(doc);
					server.commit();
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
		QueryResponse rsp = server.query(query);
		SolrDocumentList docs = rsp.getResults();
		query.setParam("rows",""+docs.getNumFound());
		query.setSortField("id", ORDER.asc);
		rsp = server.query(query);
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
			return server.query(s);
		} catch (SolrServerException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void main(String args[]) throws Exception {
		ServerUtil test = new ServerUtil();
		test.initialize();
		//test.addDocTest();
		// test.deleteDocTest();
		test.search("field:new methodbody:new");
	}
}
