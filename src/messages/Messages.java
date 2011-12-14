package messages;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import de.sub.goobi.config.ConfigMain;

public class Messages {
	private static final String BUNDLE_NAME = "messages.intmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	 static  ResourceBundle localBundle;

	private Messages() {
	}

	public static String getString(String key) {
		File file = new File(ConfigMain.getParameter("localMessages"));
		if (file.exists()) {
			// Load local message bundle from file system only if file exists; if value not exists in bundle, use default bundle from classpath

			try {
				URL resourceURL = file.toURI().toURL();
				URLClassLoader urlLoader = new URLClassLoader(new URL[] { resourceURL });
				localBundle = ResourceBundle.getBundle("messages", FacesContext.getCurrentInstance().getViewRoot().getLocale(), urlLoader);
			} catch (Exception e) {
			}
			
		}

		// Load local message bundle from classpath
		try {
			if (localBundle != null) {
				if (localBundle.containsKey(key)) {
					String trans = localBundle.getString(key);
					return trans;
				}
				if (localBundle.containsKey(key.toLowerCase())) {
					return localBundle.getString(key.toLowerCase());
				}
			}
		} catch (RuntimeException e) {
		}
		try {
			String msg = RESOURCE_BUNDLE.getString(key);
			return msg;
		} catch (RuntimeException e) {
			return key;
		}
	}
}
