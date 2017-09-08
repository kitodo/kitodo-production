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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.FilenameFilter;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.GoobiVersion;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.services.ServiceManager;

/**
 * Helper form.
 *
 * @author Wulf Riebensahm
 */
@Named("HelperForm")
@SessionScoped
public class HelperForm implements Serializable {
    private static final long serialVersionUID = -5872893771807845586L;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(HelperForm.class);
    static final String MAIN_JSF_PATH = "/pages";
    private static final String IMAGE_PATH = "/pages/images";
    private static final String CSS_PATH = "/css";

    public String getBuildVersion() {
        return GoobiVersion.getBuildversion();
    }

    public String getVersion() {
        return GoobiVersion.getBuildversion();
    }

    public String getApplicationLogo() {
        String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
        logo += ConfigCore.getParameter("ApplicationLogo", "kitodo-header-logo.svg");

        return logo;
    }

    public String getApplicationHeaderBackground() {
        String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
        logo += ConfigCore.getParameter("ApplicationHeaderBackground", "goobi_meta_verlauf.jpg");
        /* wenn ein Background angegeben wurde, dann diesen jetzt strecken */
        if (logo.length() > 0) {
            logo = "background: url(" + logo + ") repeat-x;";
        }
        return logo;
    }

    // TODO: Change the defaults
    public String getApplicationHeaderTitle() {
        return ConfigCore.getParameter("ApplicationHeaderTitle", "Goobi - Universitätsbibliothek Göttingen");
    }

    public String getApplicationTitle() {
        return ConfigCore.getParameter("ApplicationTitle", "http://goobi.gdz.uni-goettingen.de");
    }

    public String getApplicationTitleStyle() {
        return ConfigCore.getParameter("ApplicationTitleStyle", "font-size:17; font-family:verdana; color: black;");
    }

    public String getApplicationWebsiteUrl() {
        return getServletPathAsUrl();
    }

    public String getApplicationWebsiteMsg() {
        String rueck = ConfigCore.getParameter("ApplicationWebsiteMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationHomepageMsg() {
        String rueck = ConfigCore.getParameter("ApplicationHomepageMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationTechnicalBackgroundMsg() {
        String rueck = ConfigCore.getParameter("ApplicationTechnicalBackgroundMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationImpressumMsg() {
        String rueck = ConfigCore.getParameter("ApplicationImpressumMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationIndividualHeader() {
        return ConfigCore.getParameter("ApplicationIndividualHeader", "");
    }

    public boolean getAnonymized() {
        return ConfigCore.getBooleanParameter("anonymize");
    }

    public List<SelectItem> getRegelsaetze() throws DAOException {
        List<SelectItem> myPrefs = new ArrayList<>();
        List<Ruleset> temp = serviceManager.getRulesetService().getByQuery("from Ruleset ORDER BY title");
        for (Ruleset ruleset : temp) {
            myPrefs.add(new SelectItem(ruleset, ruleset.getTitle(), null));
        }
        return myPrefs;
    }

    public List<SelectItem> getDockets() {
        List<SelectItem> answer = new ArrayList<>();
        try {
            List<Docket> temp = serviceManager.getDocketService().getByQuery("from Docket ORDER BY title");
            for (Docket d : temp) {
                answer.add(new SelectItem(d, d.getTitle(), null));
            }
        } catch (DAOException e) {
            logger.error(e);
        }

        return answer;
    }

    public List<String> getFileFormats() {
        ArrayList<String> ffs = new ArrayList<>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (!ffh.equals(MetadataFormat.RDF)) {
                ffs.add(ffh.getName());
            }
        }
        return ffs;
    }

    public List<String> getFileFormatsInternalOnly() {
        ArrayList<String> ffs = new ArrayList<>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (ffh.isUsableForInternal()) {
                if (!ffh.equals(MetadataFormat.RDF)) {
                    ffs.add(ffh.getName());
                }
            }
        }
        return ffs;
    }

    public String getServletPathAsUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getRequestContextPath() + "/";
    }

    public String getServletPathWithHostAsUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath(); // /mywebapp
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

    public boolean getMessagesExist() {
        return FacesContext.getCurrentInstance().getMessages().hasNext();
    }

    /**
     * Get all css files from root folder.
     * 
     * @return list of css files
     */
    public List<SelectItem> getCssFiles() {
        List<SelectItem> list = new ArrayList<>();
        FilenameFilter filter = new FileNameEndsWithFilter(".css");
        ArrayList<URI> uris = serviceManager.getFileService().getSubUris(filter, URI.create(CSS_PATH));
        for (URI uri : uris) {
            list.add(new SelectItem("/css/" + uri.toString(), uri.toString()));
        }
        return list;
    }

    /**
     * Method returns a valid css file, which is the suggestion unless
     * suggestion is not available if not available default.css is returned.
     * 
     * @param cssFileName
     *            suggested css file
     * 
     * @return valid css file
     */
    public String getCssLinkIfExists(String cssFileName) {
        FilenameFilter filter = new FileNameEndsWithFilter(".css");
        ArrayList<URI> uris = serviceManager.getFileService().getSubUris(filter, URI.create(CSS_PATH));
        for (URI uri : uris) {
            if ((CSS_PATH + uri).equals(cssFileName)) {
                return cssFileName;
            }
        }
        return CSS_PATH + "/default.css";
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public String getLogoUrl() {
        return getServletPathWithHostAsUrl() + "/pages/images/template/kitodo-homepage-logo.svg";
    }

    public boolean getMassImportAllowed() {
        boolean value = false;
        if (ConfigCore.getBooleanParameter("massImportAllowed", false)) {
            return !PluginLoader.getPluginList(PluginType.Import).isEmpty();
        }
        return value;
    }

    public boolean getIsIE() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        return request.getHeader("User-Agent").contains("MSIE");
    }

    public String getUserAgent() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        return request.getHeader("User-Agent");
    }

    /**
     * Returning value of configuration parameter withUserStepDoneSearch. Used
     * for enabling/disabling search for done steps by user.
     *
     * @return boolean
     */
    public boolean getUserStepDoneSearchEnabled() {
        return ConfigCore.getBooleanParameter("withUserStepDoneSearch");
    }
}
