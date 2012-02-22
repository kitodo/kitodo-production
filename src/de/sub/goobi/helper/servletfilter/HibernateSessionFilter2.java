/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper.servletfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;

import de.sub.goobi.persistence.HibernateSessionLong;
import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;

// TODO: Previous Hibernate-Filter for old manual Hibernate-Session-Management, old version, reactivated, because 
// de.sub.goobi.persistence.HibernateSessionConversationFilter does not work like it should
public class HibernateSessionFilter2 implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/*===============================================================*/

	@SuppressWarnings("deprecation")
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest myRequest = (HttpServletRequest) request;
		HibernateSessionLong hsl = null;
		Session session = null;

		/* --------------------------------
		 * die Hibernate-Session connecten
		 * --------------------------------*/
		// das Managed Bean aus der Session holen
		hsl = (HibernateSessionLong) myRequest.getSession().getAttribute("HibernateSessionLong");
		// wenn das Managed Bean bereits in der Http-Session war, 
		// dann daraus die Hibernate-Session ermitteln
		if (hsl != null)
			session = hsl.getSession();
		if (session != null && !session.isConnected())
			session.reconnect();

		try {
			chain.doFilter(request, response);
		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper("Unexpected Error.", e));
		} finally {

			/* --------------------------------
			 * die Hibernate-Session von der Datenbank trennen
			 * --------------------------------*/
			// das Managed Bean aus der Session holen
			hsl = (HibernateSessionLong) myRequest.getSession().getAttribute("HibernateSessionLong");
			// wenn das Managed Bean bereits in der Http-Session war, 
			// dann daraus die Hibernate-Session ermitteln
			if (hsl != null) {
				session = hsl.getSession();
				// gibt es eine Hibernate-Session und ist diese mit der DB verbunden, 
				// dann wird diese jetzt getrennt
				if (session != null && session.isConnected())
					session.disconnect();
			}
		}
	}

	/*===============================================================*/

	/**
    */
	public void destroy() {
		// Nothing necessary
	}

}
