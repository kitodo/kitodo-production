package de.unigoettingen.sub.search.opac;

import java.net.URLEncoder;

public class Query {

	private String queryUrl;
	private int queryTermNumber = 0;
	
	public static final String AND = "*";
	public static final String OR = "%2B"; //URL-encoded +
	public static final String NOT = "-";
//	public static final String AND = "AND";
//	public static final String OR = "OR";
//	public static final String NOT = "-";
	private static final String FIRST_OPERATOR = "SRCH";
	
	
	private static final String OPERATOR = "&ACT";
	private static final String QUERY = "&TRM";
	private static final String FIELD = "&IKT";
	
	public Query() {
		super();
	}

	public Query(String query, String fieldNumber) {
		super();
		this.addQuery(null, query, fieldNumber);
	}

	//operation must be Query.AND, .OR, .NOT 
	 public void addQuery(String operation, String query, String fieldNumber){
		 
		 //ignore boolean operation for first term
		 if (queryTermNumber == 0){
			 queryUrl = OPERATOR + queryTermNumber + "=" + FIRST_OPERATOR;
		 }else{
			 queryUrl += OPERATOR + queryTermNumber + "=" + operation;
		 }
		 
		 
		 queryUrl += FIELD + queryTermNumber + "=" + fieldNumber;
		 
		 try{
			 queryUrl += QUERY + queryTermNumber + "=" + 
			 	URLEncoder.encode(query , GetOpac.URL_CHARACTER_ENCODING);
		 }catch (Exception e) {
			 e.printStackTrace();
		}
		 
		 queryTermNumber++;
	 }
	 
	 public String getQueryUrl(){
		 return queryUrl;
	 }
	 
}
