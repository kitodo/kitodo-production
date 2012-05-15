package de.sub.goobi.Persistence.apache;

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

	// TODO aus Konfig holen

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
			logger.info("loading configuration from " + f.getAbsolutePath());
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
			List<Element> properties = sessionFactory.getChildren("property");
			logger.debug("found " + properties.size() + " property elements");
			for (Element property : properties) {
				if (property.getAttribute("name").getValue().equals("hibernate.connection.url")) {
					this.dbURI = property.getText().replace("&", "&amp;").trim();
					logger.debug("found uri element: " + this.dbURI);
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.driver_class")) {
					this.dbDriverName = property.getText().trim();
					logger.debug("found driver element: " + this.dbDriverName);
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.username")) {
					this.dbUser = property.getText().trim();
					logger.debug("found user element: " + this.dbUser);
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.password")) {
					this.dbPassword = property.getText().trim();
					logger.debug("found password element: " + this.dbPassword);
				} else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.max_size")) {
					this.dbPoolMaxSize = new Integer(property.getText().trim()).intValue();
					logger.debug("found max poolsize element: " + this.dbPoolMaxSize);
				}else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.min_size")) {
					this.dbPoolMinSize = new Integer(property.getText().trim()).intValue();
					logger.debug("found min poolsize element: " + this.dbPoolMinSize);
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
