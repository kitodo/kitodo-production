package de.sub.goobi.Forms;

//TODO: Use generics.
//TODO: use consts for files and URL fragments
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.goobi.production.GoobiVersion;

import de.sub.goobi.Beans.Regelsatz;
import de.sub.goobi.Persistence.RegelsatzDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;

public class HelperForm {
	// Helper help = new Helper();

	public static final String MAIN_JSF_PATH = "/newpages";
	public static final String IMAGE_PATH = "/newpages/images";
	public static final String CSS_PATH = "/css";

	public String getBuildVersion() {
		return GoobiVersion.BUILDVERSION;
	}

	public String getVersion() {
		return GoobiVersion.BUILDVERSION;
	}

	/**
	 * @author Wulf
	 * @param none
	 * @return returns dynamically resolved path for Version Logo
	 */
	public String getApplicationVersionLogo() {
		String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
		logo += ConfigMain.getParameter("ApplicationVersionLogo", "Goobi151Logo.jpg");
		return logo;

	}

	public String getApplicationLogo() {
//		GetMethod method = null;
//		try {
//			HttpClient httpclient = new HttpClient();
//			method = new GetMethod("http://is.gd/feWO5");
//			int statusCode = httpclient.executeMethod(method);
//			if (statusCode == HttpStatus.SC_OK) {
//				return method.getURI().getURI();
//			}
//		} catch (URIException e) {
//			// do nothing, no internet connection found, using local image
//		} catch (HttpException e) {
//			// do nothing, no internet connection found, using local image
//		} catch (IOException e) {
//			// do nothing, no internet connection found, using local image
//		} finally {
//			method.releaseConnection();
//		}
		String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
		logo += ConfigMain.getParameter("ApplicationLogo", "goobi_meta_klein.jpg");
		
		
		return logo;
	}

	public String getApplicationHeaderBackground() {
		String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
		logo += ConfigMain.getParameter("ApplicationHeaderBackground", "goobi_meta_verlauf.jpg");
		/* wenn ein Background angegeben wurde, dann diesen jetzt strecken */
		if (logo.length() > 0) {
			logo = "background: url(" + logo + ") repeat-x;";
		}
		return logo;
	}

	// TODO: Change the defaults
	public String getApplicationHeaderTitle() {
		String rueck = ConfigMain.getParameter("ApplicationHeaderTitle", "Goobi - Universitätsbibliothek Göttingen");
		return rueck;
	}

	public String getApplicationTitle() {
		String rueck = ConfigMain.getParameter("ApplicationTitle", "http://goobi.gdz.uni-goettingen.de");
		return rueck;
	}

	public String getApplicationTitleStyle() {
		String rueck = ConfigMain.getParameter("ApplicationTitleStyle", "font-size:17; font-family:verdana; color: black;");
		return rueck;
	}

	public String getApplicationWebsiteUrl() {
		return getServletPathAsUrl();
	}

	public String getApplicationWebsiteMsg() {
		String rueck = ConfigMain.getParameter("ApplicationWebsiteMsg", getApplicationWebsiteUrl());
		return Helper.getTranslation(rueck);
	}

	public String getApplicationHomepageMsg() {
		String rueck = ConfigMain.getParameter("ApplicationHomepageMsg", getApplicationWebsiteUrl());
		return Helper.getTranslation(rueck);
	}

	public String getApplicationTechnicalBackgroundMsg() {
		String rueck = ConfigMain.getParameter("ApplicationTechnicalBackgroundMsg", getApplicationWebsiteUrl());
		return Helper.getTranslation(rueck);
	}

	public String getApplicationImpressumMsg() {
		String rueck = ConfigMain.getParameter("ApplicationImpressumMsg", getApplicationWebsiteUrl());
		return Helper.getTranslation(rueck);
	}

	public String getApplicationIndividualHeader() {
		String rueck = ConfigMain.getParameter("ApplicationIndividualHeader", "");
		return rueck;
	}

	public boolean getAnonymized() {
		return ConfigMain.getBooleanParameter("anonymize");
	}

	// TODO: Use generics
	public List<SelectItem> getRegelsaetze() throws DAOException {
		List<SelectItem> myPrefs = new ArrayList<SelectItem>();
		// TODO: Avoid SQL here
		List<Regelsatz> temp = new RegelsatzDAO().search("from Regelsatz ORDER BY titel");
		for (Iterator<Regelsatz> iter = temp.iterator(); iter.hasNext();) {
			Regelsatz an = (Regelsatz) iter.next();
			myPrefs.add(new SelectItem(an, an.getTitel(), null));
		}
		return myPrefs;
	}

	public List<String> getFileFormats() {
		ArrayList<String> ffs = new ArrayList<String>();
		for (MetadataFormat ffh : MetadataFormat.values()) {
			if (!ffh.equals(MetadataFormat.RDF)) {
				ffs.add(ffh.getName());
			}
		}
		return ffs;
	}

	public List<String> getFileFormatsInternalOnly() {
		ArrayList<String> ffs = new ArrayList<String>();
		for (MetadataFormat ffh : MetadataFormat.values()) {
			if (ffh.isUsableForInternal())
				if (!ffh.equals(MetadataFormat.RDF)) {
					ffs.add(ffh.getName());
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
		String reqUrl = scheme + "://" + serverName + ":" + serverPort + contextPath;
		// String reqUrl = request.getRequestURL().toString();
		return reqUrl;
	}

	// TODO: Try to avoid Iterators, usr for loops instead
	@SuppressWarnings( { "unchecked", "unused" })
	public boolean getMessagesExist() {
		boolean rueck = false;
		FacesContext context = FacesContext.getCurrentInstance();
		for (Iterator it = context.getClientIdsWithMessages(); it.hasNext();) {
			Object o = it.next();
		}
		for (Iterator it = context.getMessages(); it.hasNext();) {
			FacesMessage o = (FacesMessage) it.next();
			rueck = true;
		}
		return rueck;
	}

	public List<SelectItem> getCssFiles() {
		List<SelectItem> myList = new ArrayList<SelectItem>();

		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath("/css") + File.separator;
		File cssDir = new File(filename);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".css"));
			}
		};

		String[] dateien = cssDir.list(filter);
		for (String string : dateien) {
			myList.add(new SelectItem("/css/" + string, string));
		}
		return myList;
	}

	/*
	 * method returns a valid css file, which is the suggestion unless suggestion is not available if not available default.css is returned
	 * 
	 * @author Wulf
	 * 
	 * @param suggested css file
	 * 
	 * @return valid css file
	 */
	public String getCssLinkIfExists(String cssFileName) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath(CSS_PATH) + File.separator;
		File cssDir = new File(filename);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".css"));
			}
		};

		String[] dateien = cssDir.list(filter);
		for (String string : dateien) {
			if ((CSS_PATH + "/" + string).equals(cssFileName)) {
				return cssFileName;
			}
		}
		return CSS_PATH + "/default.css";
	}

	public TimeZone getTimeZone() {
		return TimeZone.getDefault();
	}

	public String getLogoUrl() {
//		GetMethod method = null;
//		try {
//			HttpClient httpclient = new HttpClient();
//			method = new GetMethod("http://is.gd/feWHt");
//			int statusCode = httpclient.executeMethod(method);
//			if (statusCode == HttpStatus.SC_OK) {
//				return method.getURI().getURI();
//			}
//		} catch (URIException e) {
//			// do nothing, no internet connection found, using local image
//		} catch (HttpException e) {
//			// do nothing, no internet connection found, using local image
//		} catch (IOException e) {
//			// do nothing, no internet connection found, using local image
//		} finally {
//			method.releaseConnection();
//		}
		return getServletPathWithHostAsUrl() + "/newpages/images/template/goobiVersionLogoBig.jpg";
	}
}
