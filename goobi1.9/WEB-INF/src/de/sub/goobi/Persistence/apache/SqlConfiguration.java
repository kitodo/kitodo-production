package de.sub.goobi.Persistence.apache;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class SqlConfiguration {

	// TODO aus Konfig holen

	private String dbDriverName = "com.mysql.jdbc.Driver";
	private String dbUser = "root";
	private String dbPassword = "goobi";
	private String dbURI = "jdbc:mysql://localhost/goobi?autoReconnect=true&amp;autoReconnectForPools=true";
							

	private int dbPoolMinSize = 1;
	private int dbPoolMaxSize = 20;

	private static SqlConfiguration sqlConfiguration = new SqlConfiguration();

	private SqlConfiguration() {
		String file = "hibernate.cfg.xml";
		File f = new File(file);
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(f);
			Element root = doc.getRootElement();
			Element sessionFactory = root.getChild("session-factory");
			List<Element> properties = sessionFactory.getChildren("property");
			for (Element property : properties) {
				if (property.getAttribute("name").getValue().equals("hibernate.connection.url")) {
					this.dbURI = property.getText().replace("&", "&amp;").trim();
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.driver_class")) {
					this.dbDriverName = property.getText().trim();
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.username")) {
					this.dbUser = property.getText().trim();
				} else if (property.getAttribute("name").getValue().equals("hibernate.connection.password")) {
					this.dbPassword = property.getText().trim();
				} else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.max_size")) {
					this.dbPoolMaxSize = new Integer(property.getText().trim()).intValue();
				}else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.min_size")) {
					this.dbPoolMinSize = new Integer(property.getText().trim()).intValue();
				}

			}

		} catch (JDOMException e1) {

		} catch (IOException e1) {

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
