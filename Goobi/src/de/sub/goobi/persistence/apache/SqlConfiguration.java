/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.persistence.apache;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
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
            ClassLoader classLoader = getClass().getClassLoader();
            URL fileResource = classLoader.getResource("hibernate.cfg.xml");
            if (fileResource == null) {
                throw new RuntimeException("Could not find file hibernate.cfg.xml through class loader!");
            }

            File f = new File(fileResource.toURI());
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
                    this.dbPoolMaxSize = Integer.parseInt(property.getText().trim());
                    if(logger.isDebugEnabled()){
                        logger.debug("found max poolsize element: " + this.dbPoolMaxSize);
                    }
                }else if (property.getAttribute("name").getValue().equals("hibernate.c3p0.min_size")) {
                    this.dbPoolMinSize = Integer.parseInt(property.getText().trim());
                    if(logger.isDebugEnabled()){
                        logger.debug("found min poolsize element: " + this.dbPoolMinSize);
                    }
                }

            }

        } catch (JDOMException e1) {
            logger.error(e1);
        } catch (IOException e1) {
            logger.error(e1);
        } catch (URISyntaxException e) {
            logger.error(e);
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
