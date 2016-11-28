package de.sub.goobi.helper.servletfilter;

//CHECKSTYLE:OFF
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
//CHECKSTYLE:ON

import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;
import de.sub.goobi.persistence.HibernateSessionLong;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;

// TODO: Previous Hibernate-Filter for old manual Hibernate-Session-Management, old version, reactivated, because
// de.sub.goobi.Persistence.HibernateSessionConversationFilter does not work like it should
public class HibernateSessionFilter2 implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/*===============================================================*/

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest myRequest = (HttpServletRequest) request;
		HibernateSessionLong hsl = null;
		Session session = null;

		/*
		 * die Hibernate-Session connecten
		 */
		// das Managed Bean aus der Session holen
		hsl = (HibernateSessionLong) myRequest.getSession().getAttribute("HibernateSessionLong");
		// wenn das Managed Bean bereits in der Http-Session war,
		// dann daraus die Hibernate-Session ermitteln
		if (hsl != null) {
			session = hsl.getSession();
		}
		if (session != null && !session.isConnected()) {
		}

		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper("Unexpected Error.", e));
		} finally {

			/*
			 * die Hibernate-Session von der Datenbank trennen
			 */
			// das Managed Bean aus der Session holen
			hsl = (HibernateSessionLong) myRequest.getSession().getAttribute("HibernateSessionLong");
			// wenn das Managed Bean bereits in der Http-Session war,
			// dann daraus die Hibernate-Session ermitteln
			if (hsl != null) {
				session = hsl.getSession();
				// gibt es eine Hibernate-Session und ist diese mit der DB verbunden,
				// dann wird diese jetzt getrennt
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
			}
		}
	}

	/*===============================================================*/

	/**
	*/
	@Override
	public void destroy() {
		// Nothing necessary
	}

}
