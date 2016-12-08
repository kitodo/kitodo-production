package org.kitodo.production.plugin.importer.massimport;

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
import de.sub.goobi.config.ConfigMain;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.goobi.production.constants.Parameters;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class UghUtils {
	private static final Logger myLogger = Logger.getLogger(UghUtils.class);

	/**
	 * In einem String die Umlaute auf den Grundbuchstaben reduzieren ================================================================
	 */
	// TODO: Try to replace this with an external library
	public static String convertUmlaut(String line) {
		try (BufferedReader in = open("goobi_opacUmlaut.txt")) {
			String str;
			while ((str = in.readLine()) != null) {
				if (str.length() > 0) {
					line = line.replaceAll(str.split(" ")[0], str.split(" ")[1]);
				}
			}
		} catch (IOException e) {
			myLogger.error("IOException bei Umlautkonvertierung", e);
		}
		return line;
	}

	/**
	 * The function open() opens a file. In a user session context, the file is
	 * taken from the web application’s deployment directory
	 * (…/WEB-INF/classes), if not, it is taken from the CONFIG_DIR specified in
	 * the CONFIG_FILE.
	 * 
	 * TODO: Community needs to decide: Is this behaviour really what we want?
	 * Shouldn’t it <em>always</em> be the configured directory?
	 * 
	 * @param fileName
	 *            File to open
	 * @return a BufferedReader for reading the file
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a
	 *             regular file, or for some other reason cannot be opened for
	 *             reading
	 * @throws UnsupportedEncodingException
	 *             If the named charset is not supported
	 */
	private static BufferedReader open(String fileName) throws IOException {
		String path = ConfigMain.getParameter(Parameters.CONFIG_DIR);
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			path = FilenameUtils.concat(session.getServletContext().getRealPath("/WEB-INF"), "classes");
		}
		String file = FilenameUtils.concat(path, fileName);
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
	}

}
