package org.goobi.production.importer;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.sub.goobi.config.ConfigMain;

/********************************************************************************************************
 * Import Data from the XML-File, created by FireburnExporter to the GoobiDB.
 * 
 * @author Igor Toker
 * 
 *********************************************************************************************************/

public class FireburnDataImport {
	private static final Logger logger = Logger.getLogger(FireburnDataImport.class);
	/** XML File with properties to import */
	private static String filename;

	/** XML File for the content that couldn't be imported */
	private final static String notfoundFilename = "propertiesWithoutProcess.xml";

	private final static String cdName = "CDArchiveNumber";
	private final static String cdAnzahl = "CDArchivenumberOfCDs";
	private final static String archivType = "CDArchiveType";
	private final static String size = "CDArchiveSize";

	HashSet<FireburnProperty> pList = new HashSet<FireburnProperty>();

	@XStreamAlias("data")
	@XStreamImplicit
	HashSet<FireburnProperty> pNotFoundList = new HashSet<FireburnProperty>();
	HashSet<FireburnProperty> pFoundList = new HashSet<FireburnProperty>();

	private Connection connection;
	Statement stmt;

	public FireburnDataImport() {
		try {
			this.connection = connectToDB();
			this.stmt = this.connection.createStatement();
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		} catch (ConfigurationException e) {
			logger.error(e);
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	/**************************************************************************
	 * Entry Point
	 * 
	 * @param args
	 * @throws JDOMException
	 * @throws IOException
	 * @throws ParseException
	 * 
	 ***************************************************************************/
	public static void main(String[] args) throws JDOMException, IOException, ParseException {
		filename = ConfigMain.getParameter("tempfolder") + "fireburn.xml";
		// debug
		long time1 = System.currentTimeMillis();

		FireburnDataImport fdi = new FireburnDataImport();
		// get all properties from xml file
		fdi.pList.addAll(fdi.loadDataFromXml(filename));
		if(logger.isDebugEnabled()){
			logger.debug("Data is loaded from XML,  " + fdi.pList.size() + " Properties.");
		}

		// debug
		boolean search1 = true;
		if (search1) {
			// -----------------------------------------------------------------------
			// Search for storeIdentifier in the Process Table (with title)
			// -----------------------------------------------------------------------
			logger.debug("Search in process title..");
			logger.debug("-----------------------------------------------------------------------");
			try {
				for (FireburnProperty p : fdi.pList) {
					String processId = fdi.getProcessId(p.titel);
					// process Id found
					if (processId != null) {
						fdi.pFoundList.add(p);
						// write to Goobi
						fdi.writeToGoobiDB(p, processId);
					
					}
					// processId is not found.
					else {
						fdi.pNotFoundList.add(p);
					}
				}
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}
			if(logger.isDebugEnabled()){
				logger.debug("Found: " + fdi.pFoundList.size());
				logger.debug("Not found: " + fdi.pNotFoundList.size());
			}
		}
		boolean search2 = true;
		if (search2) {
			// -----------------------------------------------------------------------
			// search in Vorlageneigenschaften
			// -----------------------------------------------------------------------
			logger.debug("Search in 'Vorlageeigenschaften'..");
			logger.debug("-----------------------------------------------------------------------");
			// prepare lists
			fdi.pFoundList.clear();
			fdi.pList.clear();
			fdi.pList.addAll(fdi.pNotFoundList);
			fdi.pNotFoundList.clear();
			try {
				for (FireburnProperty p : fdi.pList) {
					String storeIdentifier = null;
					// search
					storeIdentifier = fdi.getStoreIdentifierFromVorlageneigenschaften(p);
					if (storeIdentifier != null) {
						fdi.pFoundList.add(p);
						fdi.writeToGoobiDB(p, storeIdentifier);
					} else {
						fdi.pNotFoundList.add(p);
					}
				}
			} catch (ClassNotFoundException e) {
				logger.error(e);
			} catch (SQLException e) {
				logger.error(e);
			}
			if(logger.isDebugEnabled()){
				logger.debug("Found: " + fdi.pFoundList.size());
				logger.debug("Not found: " + fdi.pNotFoundList.size());
			}
		}
		// -----------------------------------------------------------------------
		// search in Werkstueckeeigenschaften
		// -----------------------------------------------------------------------
		logger.debug("Search in 'Werkstueckeeigenschaften'..");
		logger.debug("-----------------------------------------------------------------------");
		// prepare lists
		fdi.pFoundList.clear();
		fdi.pList.clear();
		fdi.pList.addAll(fdi.pNotFoundList);
		fdi.pNotFoundList.clear();
		try {
			for (FireburnProperty p : fdi.pList) {
				String storeIdentifier = null;
				// search
				storeIdentifier = fdi.getStoreIdentifierFromWerkstueckeeigenschaften(p);
				if (storeIdentifier != null) {
					fdi.pFoundList.add(p);
					fdi.writeToGoobiDB(p, storeIdentifier);
				} else {
					fdi.pNotFoundList.add(p);
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		}
		if(logger.isDebugEnabled()){
			logger.debug("Found: " + fdi.pFoundList.size());
			logger.debug("Not found: " + fdi.pNotFoundList.size());
		}
		// -----------------------------------------------------------------------
		// write all properties without process in separate xml file
		// -----------------------------------------------------------------------
		XStream xstream = new XStream();
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.processAnnotations(FireburnDataImport.class);
		xstream.processAnnotations(FireburnProperty.class);
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(new File(notfoundFilename)), StandardCharsets.UTF_8);
		xstream.toXML(fdi.pNotFoundList, fw);
		// DEBUG
		long time2 = System.currentTimeMillis();
		long time = time2 - time1;
		logger.debug("Execution time (ms): " + time);
	}

	/***********************************************************************************
	 * Connect to Goobi DB.
	 * 
	 * @return {@link Connection}
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ConfigurationException
	 * @throws IOException
	 ************************************************************************************/
	private Connection connectToDB() throws ClassNotFoundException, SQLException, ConfigurationException, IOException {
		Connection connection = null;
		// Load the JDBC driver
		String driverName = "org.gjt.mm.mysql.Driver"; // MySQL MM JDBC driver
		Class.forName(driverName);
		// Create a connection to the database
		Configuration cfg = new Configuration().configure();
		String serverName = "localhost";
		String db = cfg.getProperty("hibernate.connection.url");
		String mydatabase = db.substring(db.lastIndexOf("/") + 1);
		String url = "jdbc:mysql://" + serverName + "/" + mydatabase; // a JDBC url

		String username = cfg.getProperty("hibernate.connection.username");

		String password = cfg.getProperty("hibernate.connection.password");

		connection = DriverManager.getConnection(url, username, password);
		return connection;
	}

	/****************************************************************************************
	 * Get StoreIdentifier from Goobi DB for this Title
	 * 
	 * @param title
	 *            String
	 * @return String or null if ProzessID is not found
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 *****************************************************************************************/
	private String getProcessId(String title) throws ClassNotFoundException, SQLException {
		String retString = null;
		String sqlstring = "SELECT ProzesseID FROM prozesse WHERE Titel='" + title + "'";
		try (ResultSet rs = this.stmt.executeQuery(sqlstring)) {
			while (rs.next()) {
				retString = rs.getString("ProzesseID");
			}
		}

		return retString;
	}

	/*************************************************************************************
	 * Get StoreIdentifier from Vorlageneigenschaften.
	 * 
	 * @param p
	 *            - FireburnProperty
	 * @return String - StoreIdendifier
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 **************************************************************************************/
	private String getStoreIdentifierFromWerkstueckeeigenschaften(FireburnProperty p) throws ClassNotFoundException, SQLException {
		String weId = getWerkstueckeeigenschaftenId(p);
		String processId = null;
		if (weId != null) {
			// search prozessID
			String sql = "SELECT ProzesseID FROM werkstuecke WHERE WerkstueckeID='" + weId + "';";
			try (ResultSet rs = this.stmt.executeQuery(sql)) {
				if (rs.next()) {
					processId = rs.getString("prozesseID");
				}
			}
		}
		return processId;
	}

	/*************************************************************************************
	 * Get StoreIdentifier from Vorlageneigenschaften.
	 * 
	 * @param p
	 *            - FireburnProperty
	 * @return String - StoreIdendifier
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 **************************************************************************************/
	private String getStoreIdentifierFromVorlageneigenschaften(FireburnProperty p) throws ClassNotFoundException, SQLException {
		String vorlagenId = getVorlagenId(p);
		String processId = null;
		if (vorlagenId != null) {
			String sql = "SELECT ProzesseID FROM vorlagen WHERE VorlagenID='" + vorlagenId + "';";
			try (ResultSet rs = this.stmt.executeQuery(sql)) {
				if (rs.next()) {
					processId = rs.getString("prozesseID");
				}
			}
		}
		return processId;
	}

	/****************************************************************************************
	 * Gets WerkstueckeeigenschaftenId from FireburnProperty
	 * 
	 * @param p
	 *            FireburnProperty
	 * @return String - VorlagenId
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * 
	 *****************************************************************************************/
	private String getWerkstueckeeigenschaftenId(FireburnProperty p) throws ClassNotFoundException, SQLException {
		// get ppn
		// -----------------------------------------------------------------------
		int substringIndex = p.titel.indexOf("PPN");
		if (substringIndex == -1) {
			substringIndex = p.titel.indexOf("ppn");
			if (substringIndex == -1) {
				return null;
			}
		}
		String fullppn = p.titel.substring(substringIndex).toUpperCase();
		String ppn = p.titel.substring(substringIndex).substring(3);

		// Search with short PPN
		// -----------------------------------------------------------------------
		String sql = "SELECT werkstueckeID FROM werkstueckeeigenschaften WHERE Wert='" + ppn + "';";
		try (ResultSet rs = this.stmt.executeQuery(sql)) {
			if (rs.next()) {
				String weId = rs.getString("werkstueckeID");
				if(logger.isDebugEnabled()){
					logger.debug("weId, gefunden mit shortPPN: " + weId + "  Title: " + p.titel);
				}
				return weId;
			}
		}

		// Search with full PPN
		// -----------------------------------------------------------------------
		sql = "SELECT werkstueckeID FROM werkstueckeeigenschaften WHERE Wert='" + fullppn + "';";
		try (ResultSet rs = this.stmt.executeQuery(sql)) {
			if (rs.next()) {
				String weId = rs.getString("werkstueckeID");
				if(logger.isDebugEnabled()){
					logger.debug("weId, gefunden mit fullPPN: " + weId + "  Title: " + p.titel);
				}
				return weId;
			}
		}

		return null;
	}

	/****************************************************************************************
	 * Gets VorlagenId
	 * 
	 * @param p
	 *            FireburnProperty
	 * @return String - VorlagenId
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * 
	 *****************************************************************************************/
	private String getVorlagenId(FireburnProperty p) throws ClassNotFoundException, SQLException {
		// get ppn
		// -----------------------------------------------------------------------
		int substringIndex = p.titel.indexOf("PPN");
		if (substringIndex == -1) {
			substringIndex = p.titel.indexOf("ppn");
			if (substringIndex == -1) {
				return null;
			}
		}
		String fullppn = p.titel.substring(substringIndex).toUpperCase();
		String ppn = p.titel.substring(substringIndex).substring(3);

		// Search with short PPN
		// -----------------------------------------------------------------------
		String sql = "SELECT vorlagenID FROM vorlageneigenschaften WHERE Wert='" + ppn + "';";
		try (ResultSet rs = this.stmt.executeQuery(sql)) {
			if (rs.next()) {
				String vorlagenId = rs.getString("vorlagenID");
				if(logger.isDebugEnabled()){
					logger.debug("VorlagenId, gefunden mit shortPPN: " + vorlagenId + "  Title: " + p.titel);
				}
				return vorlagenId;
			}
		}

		// Search with full PPN
		// -----------------------------------------------------------------------
		sql = "SELECT vorlagenID FROM vorlageneigenschaften WHERE Wert='" + fullppn + "';";
		try (ResultSet rs = this.stmt.executeQuery(sql)) {
			if (rs.next()) {
				String vorlagenId = rs.getString("vorlagenID");
				if(logger.isDebugEnabled()){
					logger.debug("VorlagenId, gefunden mit fullPPN: " + vorlagenId + "  Title: " + p.titel);
					logger.debug(vorlagenId);
				}
				return vorlagenId;
			}
		}

		return null;
	}

	/******************************************************************************************
	 * Write Property to GoobiDB
	 * 
	 * @param p
	 *            FireburnProperty
	 * @param processId
	 *            String
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 *****************************************************************************************/
	private void writeToGoobiDB(FireburnProperty p, String processId) throws ClassNotFoundException, SQLException {
		String sql = "INSERT INTO prozesseeigenschaften(prozesseID, Titel, Wert, IstObligatorisch, DatentypenID, Auswahl, creationDate)"

		+ " VALUES ('" + processId + "','" + cdName + "','" + p.cdName + "',false,'5',false,'" + p.date + "')" + ","

		+ "('" + processId + "','" + archivType + "','" + p.type + "',false,'5',false,'" + p.date + "')" + ","

		+ "('" + processId + "','" + cdAnzahl + "','" + p.cdnumber + "',false,'5',false,'" + p.date + "'),"

		+ "('" + processId + "','" + size + "','" + p.size + "',false,'5',false,'" + p.date + "')" + ";";
		// Execute the insert statement
		this.stmt.executeUpdate(sql);
		// logger.debug(sql);
		if(logger.isDebugEnabled()){
			logger.debug("Write to Goobi: " + p.cdName + "  " + processId + "     " + p.date);
		}

	}

	/****************************************************************************************
	 * Loads properties from XML-File to {@link FireburnProperty}
	 * 
	 * @param filename
	 *            - String
	 * @return HashMap: Key - Title, Value - Cd Name
	 * @throws IOException
	 * @throws JDOMException
	 * @throws ParseException
	 *****************************************************************************************/
	@SuppressWarnings("rawtypes")
	private ArrayList<FireburnProperty> loadDataFromXml(String filename) throws JDOMException, IOException, ParseException {
		ArrayList<FireburnProperty> returnList = new ArrayList<FireburnProperty>();
		Document doc = new SAXBuilder().build(new File(filename));
		Element rootElement = doc.getRootElement();
		List ePropList = rootElement.getChildren("property");
		for (Object oProp : ePropList) {
			Element eProp = (Element) oProp;
			returnList.add(new FireburnProperty(eProp.getAttributeValue("cdName"), eProp.getAttributeValue("titel"), eProp.getAttributeValue("date"),
					Integer.parseInt(eProp.getAttributeValue("cdnumber")),
					eProp.getAttributeValue("type"),
					Long.parseLong(eProp.getAttributeValue("size"))

			));
		}
		return returnList;
	}
}
