package de.sub.goobi.persistence.apache;

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
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
public class SqlConfiguration {


	private String dbDriverName = "com.mysql.jdbc.Driver";
	private String dbUser = "goobi";
	private String dbPassword = "CHANGEIT";
	private String dbURI = "jdbc:mysql://localhost/goobi?autoReconnect=true&amp;autoReconnectForPools=true";
	private static final Logger logger = Logger.getLogger(MySQLHelper.class);
					

	private int dbPoolMinSize = 1;
	private int dbPoolMaxSize = 20;

	private static SqlConfiguration sqlConfiguration = new SqlConfiguration();

	private SqlConfiguration() {
		try {
			File f = new File(Loader.getResource("hibernate.cfg.xml").getFile());
			if(logger.isInfoEnabled()){
				logger.info("loading configuration from " + f.getAbsolutePath());
			}
			SAXBuilder sb = new SAXBuilder(false);
			sb.setValidation(false);
			sb.setFeature("http://xml.org/sax/features/validation", false);
			sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			Document doc = sb.build(f);
			logger.debug("could read configuration file");
			Element root = doc.getRootElement();
			logger.debug("found root element");
			Element sessionFactory = root.getChild("session-factory");
			logger.debug("found session-factory element");
			@SuppressWarnings("unchecked")
			List<Element> properties = sessionFactory.getChildren("property");
			if(logger.isDebugEnabled()){
				logger.debug("found " + properties.size() + " property elements");
			}
			for (Element property : properties) {
				if (property.getAttribute("name").getValue().equals("hibernate.connection.url")) {
					this.dbURI = property.getText().replace("&", "&amp;").trim();
					if(logger.isDebugEnabled()){
						logger.debug("found uri element: " + this.dbURI);
					}
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.driver_class")) {
					this.dbDriverName = property.getText().trim();
					if(logger.isDebugEnabled()){
						logger.debug("found driver element: " + this.dbDriverName);
					}
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.username")) {
					this.dbUser = property.getText().trim();
					if(logger.isDebugEnabled()){
						logger.debug("found user element: " + this.dbUser);
					}
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.password")) {
					this.dbPassword = property.getText().trim();
					if(logger.isDebugEnabled()){
						logger.debug("found password element: " + this.dbPassword);
					}
				} else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.max_size")) {
					this.dbPoolMaxSize = new Integer(property.getText().trim()).intValue();
					if(logger.isDebugEnabled()){
						logger.debug("found max poolsize element: " + this.dbPoolMaxSize);
					}
				}else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.min_size")) {
					this.dbPoolMinSize = new Integer(property.getText().trim()).intValue();
					if(logger.isDebugEnabled()){
						logger.debug("found min poolsize element: " + this.dbPoolMinSize);
					}
				}

			}

		} catch (JDOMException e1) {
			logger.error(e1);
		} catch (IOException e1) {
			logger.error(e1);
		}
	}

	public static SqlConfiguration getInstance() {
		return sqlConfiguration;
	}

	public String getDbDriverName() {
		return this.dbDriverName;
	}

	public String getDbUser() {
		return this.dbUser;
	}

	public String getDbPassword() {
		return this.dbPassword;
	}

	public String getDbURI() {
		return this.dbURI;
	}

	public int getDbPoolMinSize() {
		return this.dbPoolMinSize;
	}

	public int getDbPoolMaxSize() {
		return this.dbPoolMaxSize;
	}

	public static void main(String[] args) {
		SqlConfiguration.getInstance();
	}

}
