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
    private static final String CSS_PATH = "/WEB-INF/resources/css/old/userStyles/";

    public String getVersion() {
        return GoobiVersion.getBuildversion();
    }

    /**
     * Get application logo.
     *
     * @return link to logo
     */
    public String getApplicationLogo() {
        String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
        logo += ConfigCore.getParameter("ApplicationLogo", "kitodo-header-logo.svg");
        return logo;
    }

    /**
     * Get application header background.
     *
     * @return css style for header
     */
    public String getApplicationHeaderBackground() {
        String headerImage = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
        headerImage += ConfigCore.getParameter("ApplicationHeaderBackground", "goobi_meta_verlauf.jpg");
        /* wenn ein Background angegeben wurde, dann diesen jetzt strecken */
        if (headerImage.length() > 0) {
            headerImage = "background: url(" + headerImage + ") repeat-x;";
        }
        return headerImage;
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
        String result = ConfigCore.getParameter("ApplicationWebsiteMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(result);
    }

    public String getApplicationHomepageMsg() {
        String result = ConfigCore.getParameter("ApplicationHomepageMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(result);
    }

    public String getApplicationTechnicalBackgroundMsg() {
        String result = ConfigCore.getParameter("ApplicationTechnicalBackgroundMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(result);
    }

    public String getApplicationImpressumMsg() {
        String result = ConfigCore.getParameter("ApplicationImpressumMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(result);
    }

    public String getApplicationIndividualHeader() {
        return ConfigCore.getParameter("ApplicationIndividualHeader", "");
    }

    public boolean getAnonymized() {
        return ConfigCore.getBooleanParameter("anonymize");
    }

    /**
     * Get rulesets.
     * 
     * @return list of rulesets as SelectItems
     */
    public List<SelectItem> getRegelsaetze() {
        List<SelectItem> myPrefs = new ArrayList<>();
        List<Ruleset> temp = serviceManager.getRulesetService().getByQuery("from Ruleset ORDER BY title");
        for (Ruleset ruleset : temp) {
            myPrefs.add(new SelectItem(ruleset, ruleset.getTitle(), null));
        }
        return myPrefs;
    }

    /**
     * Get dockets.
     *
     * @return list of dockets as SelectItems
     */
    public List<SelectItem> getDockets() {
        List<SelectItem> answer = new ArrayList<>();
        List<Docket> temp = serviceManager.getDocketService().getByQuery("from Docket ORDER BY title");
        for (Docket d : temp) {
            answer.add(new SelectItem(d, d.getTitle(), null));
        }
        return answer;
    }

    /**
     * Get file formats.
     *
     * @return list of file formats as Strings
     */
    public List<String> getFileFormats() {
        ArrayList<String> ffs = new ArrayList<>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (!ffh.equals(MetadataFormat.RDF)) {
                ffs.add(ffh.getName());
            }
        }
        return ffs;
    }

    /**
     * Get only internal file formats.
     *
     * @return list of internal file formats as Strings
     */
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

    /**
     * Get servlet path.
     * 
     * @return servlet path as String
     */
    public String getServletPathAsUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getRequestContextPath() + "/";
    }

    /**
     * Get servlet path with host.
     * 
     * @return servlet path with host as String
     */
    public String getServletPathWithHostAsUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath(); // /mywebapp
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

    /**
     * Check if message exists.
     * 
     * @return true or false
     */
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
            list.add(new SelectItem(uri.toString(), uri.toString()));
        }
        return list;
    }

    /**
     * Method returns a valid css file, which is the suggestion unless suggestion is
     * not available if not available default.css is returned.
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
            if (uri.toString().equals(cssFileName)) {
                return cssFileName;
            }
        }
        return "/old/userStyles/default.css";
    }

    public String getLogoUrl() {
        return getServletPathWithHostAsUrl() + "/pages/images/template/kitodo-homepage-logo.svg";
    }

    /**
     * Check if mass import is allowed.
     * 
     * @return true or false
     */
    public boolean getMassImportAllowed() {
        return ConfigCore.getBooleanParameter("massImportAllowed", false)
                && !PluginLoader.getPluginList(PluginType.Import).isEmpty();
    }

    /**
     * Check if web browser is IE.
     * 
     * @return true or false
     */
    public boolean getIsIE() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        return request.getHeader("User-Agent").contains("MSIE");
    }

    /**
     * Get user agent.
     * 
     * @return user agent as String
     */
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
