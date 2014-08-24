package tw.edu.ncu.sia.index;

import javax.swing.JTextArea;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import tw.edu.ncu.sia.util.ServerUtil;


public class IndexStatus {
	public static void indexed(JTextArea textArea) throws Exception{
			ServerUtil.initialize();
			// initialize query
			SolrQuery query = new SolrQuery();
			//show info of all files
			query.setQuery("*:*");
			QueryResponse rsp = ServerUtil.execQuery(query);
			SolrDocumentList docs = rsp.getResults();
			textArea.append("\n\n=== Show indexed docs ===");
			textArea.append("\n 1. Docs indexed:" + docs.getNumFound());
		}

}
