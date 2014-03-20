/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2013 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.helper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * The class FacesUtils contains an omnium-gatherum of functions that perform
 * recurring tasks related to JavaServer Faces.
 * 
 * TODO: Most of the static functions currently located in “Helper.java” do
 * belong here.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class FacesUtils {

	/**
	 * The procedure sendDownload() sends a byte[] of data in the HTTP response
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
