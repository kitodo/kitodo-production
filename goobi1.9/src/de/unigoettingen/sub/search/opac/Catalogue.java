package de.unigoettingen.sub.search.opac;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Catalogue {

	//dir of local iktLists
	private static final String DIR = "";
	
	/** The url path part of the search item list on the server*/
    private static final String IKTLIST = "/XML=1.0/IKTLIST";
	
	/**
     * alternative local location of the search item list 
     * */
    private static final String IKT_LIST_FILE_SUB ="IKTLIST-SUB.xml";
    private static final String IKT_LIST_FILE_GBV ="IKTLIST-GBV.xml";
    private static final String IKT_LIST_FILE_JAL ="IKTLIST-JAL.xml";
	private static final String IKT_LIST_FILE_SWB ="IKTLIST-SWB.xml";
    private static final String IKT_LIST_FILE_HEBIS ="IKTLIST-HEBIS.xml";
    private static final String IKT_LIST_FILE_DNB ="IKTLIST-DNB.xml";
    private static final String IKT_LIST_FILE_ZDB ="IKTLIST-ZDB.xml";
    private static final String IKT_LIST_FILE_GEO_GUIDE ="IKTLIST-GEO-GUIDE.xml";
    private static final String IKT_LIST_FILE_VKI ="IKTLIST-VKI.xml";
    private static final String IKT_LIST_FILE_VKI_AALG ="IKTLIST-VKI_AALG.xml";
    private static final String IKT_LIST_FILE_OLC ="IKTLIST-OLC.xml";
    private static final String IKT_LIST_FILE_VKI_AAC ="IKTLIST-VKI_AAC.xml";
    private static final String IKT_LIST_FILE_VKI_AACOLC ="IKTLIST-VKI_AACOLC.xml";
    private static final String IKT_LIST_FILE_TIB ="IKTLIST-TIB.xml";
    private static final String IKT_LIST_FILE_SBB ="IKTLIST-SBB.xml";
    
    public static final String SUB_OPAC = "SUB"; 
    public static final String VKI_OPAC = "VKI"; 
    public static final String VKI_AALG_OPAC = "VKI_AALG"; 
    public static final String VKI_AAC_OPAC = "VKI_AAC";
    public static final String VKI_AAC_OLCOPAC = "VKI_AAC_OLC";
    public static final String GBV_OPAC = "GBV";
    public static final String JAL_OPAC = "JAL";
    public static final String OLC_MATH_OPAC = "OLC_MATH";
    public static final String HEBIS_OPAC = "HEBIS";
    public static final String SWB_OPAC = "SWB";
    public static final String DNB_OPAC = "DNB"; 
    public static final String ZDB_OPAC = "ZDB";
    public static final String GEO_GUIDE_OPAC = "GEO_GUIDE";
    public static final String TIB_OPAC = "TIB";    
    public static final String SBB_OPAC = "SBB"; 
    
    private String iktList;
    private String catalogue;
    private String description;
    private String cbs = "";
    
    private String dataBase; 
    private String serverAddress;
    private int port;
    
    private String charset = "iso-8859-1";
    
    private HashMap<String, String> picaToKey;
    private HashMap<String, String> picaToDescription;
    
    private boolean verbose = false;
    
	public Catalogue(String opac) throws IOException{
		super();
		this.picaToKey = new HashMap<String, String>();
		this.picaToDescription = new HashMap<String, String>();
		this.catalogue = opac;
		if (opac.equals(GBV_OPAC)){
			setGbv();
		}else if (opac.equals(VKI_OPAC)){
            setVki();
		}else if (opac.equals(VKI_AALG_OPAC)){
            setVkiAalg();
		}else if (opac.equals(VKI_AAC_OPAC)){
            setVkiAac();
		}else if (opac.equals(VKI_AAC_OLCOPAC)){
            setVkiAacolc();
        }else if (opac.equals(OLC_MATH_OPAC)){
            setOlcMath();
        }else if (opac.equals(SWB_OPAC)){
			setSwb();
		}else if (opac.equals(HEBIS_OPAC)){
			setHebis();
        }else if (opac.equals(ZDB_OPAC)){
            setZdb();
        }else if (opac.equals(DNB_OPAC)){
            setDnb();
        }else if (opac.equals(GEO_GUIDE_OPAC)){
            setGeoGuide();
        }else if (opac.equals(JAL_OPAC)){
            setJalEmden();
        }else if (opac.equals(TIB_OPAC)){
           setTib();
        }else if (opac.equals(SBB_OPAC)){
            setSbb();
        }else { // as default use SUB
			setSub();
		}
		this.parseIktList(this.retrieveIktList());
	}
	
	public Catalogue(String description, String serverAddress, int port, String cbs, String database) throws IOException{
       this.picaToKey = new HashMap<String, String>();
       this.picaToDescription = new HashMap<String, String>();
       this.description = description;
       this.serverAddress = serverAddress;
       this.port = port;
       this.dataBase = database;
       this.iktList = "";
       this.cbs=cbs;
       this.parseIktList(this.retrieveIktList());
   }
	
	public Catalogue(String description, String serverAddress, int port, String charset, String cbs, String database) throws IOException {
		this(description, serverAddress, port,cbs, database);
		this.charset = charset;
		
	}
	
    /***********************************************************************
     * Tries to retrieve the search item key list. First it tries a local
     * location, if that fails it tries the catalogue system. 
     *
     * @return The list as a xml string
     * @throws IOException If the retrieval of the list failed.
     **********************************************************************/
    private String retrieveIktList() throws IOException{
        //try to use local list
        try {
        		File iktlistFile = new File(DIR + this.iktList);
            if (this.verbose){
                System.out.println("Trying to load the IKTLIST from the file: "+
                		iktlistFile.getAbsolutePath());            
            }
            BufferedInputStream listStream = 
            	new BufferedInputStream(new FileInputStream(iktlistFile));
            StringBuffer result = new StringBuffer();
            
            
            //read until EOF
            int input = listStream.read(); 
            while (input != -1) {
                result.append((char) input);
                input = listStream.read();
            }
            listStream.close();
            
            return result.toString();
        } //we didn't succeed retrieving the search item list from the local file
        //so lets get it from the server
        catch(Exception e) {
    	String requestUrl = "http://"+ this.serverAddress + ":" + this.port + "/" + 
    		this.dataBase + IKTLIST;
            if (this.verbose){
                System.out.println("Retrieving IKTLIST for opac " + 
                		this.description + ": " + requestUrl);
            }
            HttpClient opacClient = new HttpClient();
            GetMethod opacRequest = new GetMethod(requestUrl);
            
            opacClient.executeMethod(opacRequest);
            return opacRequest.getResponseBodyAsString();
        }
    }
    
    /***********************************************************************
     * Parses the search key list and puts mnemonic->key and 
     * mnemonic->description into hashmaps.
     *
     * @param iktList The search key list as xml string
     **********************************************************************/
    private void parseIktList(String iktList){
        if (this.verbose){
            System.out.println("Parsing the IKTLIST: " + iktList);
        }
        
        InputSource iktSource = new InputSource(new StringReader(iktList));
        DocumentBuilder docBuilder = null;
        Document document = null;
        try{
    			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    			//get xml-docment
    	        document = docBuilder.parse(iktSource);
        }catch (Exception e) {
        		e.printStackTrace();
        }
        
        //get all keys
        NodeList keys = document.getElementsByTagName("KEY");
            
        //go through all keys and put them into their hashmap
        for (int i = 0; i < keys.getLength(); i++) {
            this.picaToKey.put(((Element) keys.item(i)).getAttribute("mnemonic"),
                          keys.item(i).getFirstChild().getNodeValue());
            this.picaToDescription.put(((Element) keys.item(i)).getAttribute("mnemonic"),
                                  ((Element) keys.item(i)).getAttribute("description").toString());
        }
    }

	private void setGbv() {
		this.dataBase = "2.1";
		this.serverAddress = "gso.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_GBV;
		this.description = "GVK";
	}

	private void setVki() {
		this.dataBase = "1.85";
		this.serverAddress = "gso.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_VKI;
		this.description = "VKI";
	}

	private void setVkiAalg() {
		this.dataBase = "8.2";
		this.serverAddress = "gso.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_VKI_AALG;
		this.description = "VKI-AALG";
	}
	
	private void setVkiAac(){
		this.dataBase = "2.97";
		this.serverAddress = "gso.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_VKI_AAC;
		this.description = "VKI-AAC";
	}
	
	private void setVkiAacolc(){
		this.dataBase = "2.297";
		this.serverAddress = "gso.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_VKI_AACOLC;
		this.description = "VKI-AAC+OLC";
	}

	private void setOlcMath() {
		this.dataBase = "2.77";
		this.serverAddress = "gso.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_OLC;
		this.description = "OLC-MATH";
	}

	
	private void setJalEmden(){
		this.dataBase = "1";
		this.serverAddress = "emdbs2.fho-emden.de";
		this.port = 8080;
		this.iktList = IKT_LIST_FILE_JAL;
		this.description = "JAL";
	}

    
    private void setZdb() {
        this.dataBase = "1.1";
        this.serverAddress = "dispatch.opac.d-nb.de";
        this.port = 80;
        this.iktList = IKT_LIST_FILE_ZDB;
        this.description = "ZDB";
    }

    private void setDnb() {
        this.dataBase = "4.1";
        this.serverAddress = "dispatch.opac.d-nb.de";
        this.port = 80;
        this.iktList = IKT_LIST_FILE_DNB;
        this.description = "DNB";
    }

	private void setHebis() {
		this.dataBase = "2.1";
		this.serverAddress = "cbsopac.rz.uni-frankfurt.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_HEBIS;
		this.description = "HeBIS";
	}

	private void setSub() {
		this.serverAddress = "opac.sub.uni-goettingen.de";
        this.port = 80;
        this.dataBase = "1";
		this.iktList = IKT_LIST_FILE_SUB;
		this.description = "SUB Goettingen";
	}

	private void setSwb() {
		this.dataBase = "2.1";
		this.serverAddress = "pollux.bsz-bw.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_SWB;
		this.description = "SWB";
	}
	
	private void setGeoGuide() {
		this.dataBase = "8.4";
		this.serverAddress = "gso4.gbv.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_GEO_GUIDE;
		this.description = "GEO_GUIDE";
	}
	
    private void setTib () {
       this.dataBase = "1";
       this.serverAddress = "opc4.tib.uni-hannover.de";
       this.port = 8080;
       this.iktList = IKT_LIST_FILE_TIB;
       this.description = "TIB";
   }
    
    private void setSbb () {
		this.dataBase = "1";
		this.serverAddress = "stabikat.de";
		this.port = 80;
		this.iktList = IKT_LIST_FILE_SBB;
		this.description = "SBB";
	}

	public String getCatalogue() {
		return this.catalogue;
	}

	public String getDataBase() {
		return this.dataBase;
	}

	public String getIktList() {
		return this.iktList;
	}

	public HashMap<String, String> getPicaToDescription() {
		return this.picaToDescription;
	}

	public HashMap<String, String> getPicaToKey() {
		return this.picaToKey;
	}

	public int getPort() {
		return this.port;
	}

	public String getServerAddress() {
		return this.serverAddress;
	}
	
	public String getCharset () {
		return this.charset;
	}
	
    /***********************************************************************
     * Returns a String of all search keys, their mnemonic and their
     * description. The result is constructed from the hashmaps.
     *
     * @return String of all search keys, their mnemonic and their
     * description
     **********************************************************************/
    public String iktListToString(){

        //get hashmap iterator
        Iterator<String> iktListIterator = this.picaToKey.keySet().iterator();
        StringBuffer result = new StringBuffer();
        //while we have another pica key
        while (iktListIterator.hasNext()) {
            String picaMnemonic = iktListIterator.next();
            result.append(picaMnemonic + " (" +
                                 this.picaToDescription.get(picaMnemonic) +
                                 "): " + this.picaToKey.get(picaMnemonic) + "\n");
        }
        return result.toString();
    }

	public String getDescription() {
		return this.description;
	}

	public boolean isVerbose() {
		return this.verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	//eingefügt cm 8.5.2007
	public String getIktNr (String key) {
		return this.picaToKey.get(key);
	}

	/**
	 * @param cbs the cbs to set
	 */
	public void setCbs(String cbs) {
		this.cbs = cbs;
	}

	/**
	 * @return the cbs
	 */
	public String getCbs() {
		return this.cbs;
	}
	

}
