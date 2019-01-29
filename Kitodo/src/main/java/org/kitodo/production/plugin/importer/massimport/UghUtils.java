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

package org.kitodo.production.plugin.importer.massimport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.config.ConfigCore;

public class UghUtils {
    private static final Logger logger = LogManager.getLogger(UghUtils.class);

    /**
     * Private constructor to hide the implicit public one.
     */
    private UghUtils() {

    }

    /**
     * In einem String die Umlaute auf den Grundbuchstaben reduzieren.
     */
    // TODO: Try to replace this with an external library
    public static String convertUmlaut(String line) {
        try (BufferedReader in = open("kitodo_opacUmlaut.txt")) {
            String str;
            while ((str = in.readLine()) != null) {
                if (str.length() > 0) {
                    line = line.replaceAll(str.split(" ")[0], str.split(" ")[1]);
                }
            }
        } catch (IOException e) {
            logger.error("IOException bei Umlautkonvertierung", e);
        }
        return line;
    }

    /**
     * The function open() opens a file. In a user session context, the file is
     * taken from the web application’s deployment directory
     * (…/WEB-INF/classes), if not, it is taken from the CONFIG_DIR specified in
     * the CONFIG_FILE.
     * <p/>
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
        String path = ConfigCore.getKitodoConfigDirectory();
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            path = FilenameUtils.concat(session.getServletContext().getRealPath("/WEB-INF"), "classes");
        }
        String file = FilenameUtils.concat(path, fileName);
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

}
