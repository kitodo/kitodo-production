/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.goobi.production.flow.statistics.hibernate.ParametersData;
import org.goobi.production.search.api.ISearch;

import de.sub.goobi.config.ConfigMain;

/**
 * This class provides the Lucene search engine for goobi.
 * 
 * @author Robert Sehr
 * 
 */
public class LuceneSearch implements ISearch {
	static Analyzer analyser;
	private static String index_path = "";
	private static String analyser_path = "";
	static LuceneSearch search;
	private static final Logger logger = Logger.getLogger(LuceneSearch.class);
	private Version luceneVersion = Version.LUCENE_24;

	/**
	 * constructor
	 */

	private LuceneSearch() {
		index_path = ConfigMain.getParameter("index_path");
		analyser = new StandardAnalyzer(luceneVersion);
	}

	/**
	 * 
	 * @return the instance of the search engine
	 */

	public static LuceneSearch getSearchEngine() {
		if (search == null) {
			search = new LuceneSearch();
		}
		return search;
	}

	/**
	 * Method overloads the
	 * 
	 * @param inQuery
	 * @param param
	 * @return
	 * @author Wulf Riebensahm
	 */
	public ArrayList<Integer> getSearchResults(String inQuery, ParametersData param) {
		return search(inQuery, param);
	}

	/*
	 * this class tokenize the query, mappes the tokens to lucene SearchEnums and queries Lucene. It returns an ArrayList of ids of processes
	 */

	private ArrayList<Integer> search(String inQuery, ParametersData param) {

		String luceneQuery = "";

		// Don't use Java 1.5, even sun says you shouldn't use 1.5 longer
		if (inQuery.length() == 0) {
			luceneQuery = "all:true";
		} else {
			StrTokenizer tokenizer = new StrTokenizer(inQuery, ' ', '\"');
			while (tokenizer.hasNext()) {
				luceneQuery += " ";
				String subquery = LuceneIndex.normalize(tokenizer.nextToken());

				if (subquery.startsWith("stepopen")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.stepopen.getLuceneTitle() + sub;
					} else {
						luceneQuery += subquery;
					}
				} else if (subquery.startsWith("stepdone")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);

						// for production statistics we need an integer value
						try {
							param.setStepDone(Integer.parseInt(sub));
						} catch (Exception e) {
							// no need to do anything here
						}

						luceneQuery += SearchEnums.stepdone.getLuceneTitle() + sub;
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("stepinwork")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.stepinwork.getLuceneTitle() + sub;
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("steplocked")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.steplocked.getLuceneTitle() + sub;
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("proj")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.project.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("vorl")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.vorl.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("idin")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.processId.getLuceneTitle() + sub;
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("proc")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.processTitle.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("werk")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.werk.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-stepopen")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.stepopen.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-stepdone")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.stepdone.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-stepinwork")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.stepinwork.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-steplocked")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.steplocked.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				}

				else if (subquery.startsWith("-proj")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.project.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-vorl")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += SearchEnums.template.getLuceneTitle() + "true";
						luceneQuery += "-" + SearchEnums.processTitle.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-idin")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.processId.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-proc")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.processTitle.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else if (subquery.startsWith("-werk")) {
					int count = subquery.indexOf(":");
					if (count >= 1) {
						String sub = subquery.substring(count);
						luceneQuery += "-" + SearchEnums.werk.getLuceneTitle() + sub + "*";
					} else {
						luceneQuery += subquery + "*";
					}
				} else {
					luceneQuery += subquery + "*";
				}
			}
		}
		logger.trace(luceneQuery);
		IndexSearcher isearcher;
		ArrayList<Integer> myhits = new ArrayList<Integer>();
		try {
			isearcher = getMyIndexSearcher();
			QueryParser parser = new QueryParser(luceneVersion, SearchEnums.processTitle.getLuceneTitle(), analyser);
			parser.setDefaultOperator(QueryParser.AND_OPERATOR);
			Query query;
			query = parser.parse(luceneQuery);
			TopDocs hits = isearcher.search(query, 25000);
			if (hits != null && hits.totalHits > 0) {
				for (int i = 0; i < hits.totalHits; i++) {
					Document hitDoc = isearcher.doc(hits.scoreDocs[i].doc);
					myhits.add(Integer.parseInt(hitDoc.get("id")));
				}
			}
			isearcher.close();
		} catch (IOException e) {
			logger.error("IOException while searching lucene index", e);
		} catch (ParseException e) {
			logger.error("ParseException while searching lucene index", e);
		}
		return myhits;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.search.interfaces.ISearch#getSearchCount(String inQuery)
	 */
	public int getSearchCount(String inQuery) {
		return search(inQuery, null).size();
	}

	/*
	 * returns new instance of IndexSearcher
	 */
	private static IndexSearcher getMyIndexSearcher() throws IOException {
		return new IndexSearcher(IndexReader.open(FSDirectory.open(new File(index_path)), false));
	}

	public ArrayList<Integer> getSearchResults(String query) {
		return search(query, null);
	}

}
