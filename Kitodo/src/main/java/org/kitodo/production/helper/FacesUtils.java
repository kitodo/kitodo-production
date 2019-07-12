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

package org.kitodo.production.helper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * The class FacesUtils contains an omnium-gatherum of functions that perform
 * recurring tasks related to JavaServer Faces.
 */
public class FacesUtils {

    /**
     * Private constructor to hide the implicit public one.
     */
    private FacesUtils() {

    }

    /**
     * Sends a byte[] of data in the HTTP response
     * of a user interaction as a file download. Calling this procedure is only
     * sensible during the invoke application phase of the JSF life cycle, i.e.
     * in procedures that are designed to provide the action for a JSF command
     * link or command button.
     *
     * @param data
     *            the content of the file
     * @param saveAsName
     *            a file name proposed to the user
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void sendDownload(byte[] data, String saveAsName) throws IOException {
        String disposition;

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        response.reset();
        try {
            disposition = "attachment; filename*=UTF-8''".concat(new URI(null, saveAsName, null).toASCIIString());
            response.setHeader("Content-Disposition", disposition);
        } catch (URISyntaxException e) {
            response.setHeader("Content-Disposition", "attachment; filename=Course.xml");
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
        context.responseComplete();
    }
}
