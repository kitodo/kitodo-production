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

package de.sub.goobi.persistence;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.context.ManagedSessionContext;

import de.sub.goobi.helper.Messages;
import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;

public class HibernateSessionConversationFilterWithoutTransaction implements Filter {
	private static final Logger log = Logger.getLogger(HibernateSessionConversationFilterWithoutTransaction.class);

	private SessionFactory sf;

	public static final String HIBERNATE_SESSION_KEY = "hibernateSession";
	public static final String END_OF_CONVERSATION_FLAG = "endOfConversation";

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		org.hibernate.classic.Session currentSession;
		HttpSession httpSession;

		try {
			// Try to get a Hibernate Session from the HttpSession
			httpSession = ((HttpServletRequest) request).getSession();
			Session disconnectedSession = (Session) httpSession.getAttribute(HIBERNATE_SESSION_KEY);

			// Start a new conversation or in the middle?
			if (disconnectedSession == null) {
				log.debug(">>> New conversation");
				currentSession = sf.openSession();
				currentSession.setFlushMode(FlushMode.MANUAL);
			} else {
				currentSession = (org.hibernate.classic.Session) disconnectedSession;
			}

		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper(Messages.getString("err_noConnectionEstablished") + " Establishing Sessions", e));
		}

		try {
			ManagedSessionContext.bind(currentSession);
		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper(Messages.getString("err_noConnectionEstablished") + " ManagedSessionContext", e));
		}

		// #################################
		// #################################
		// #################################
		// #################################
		// #### entering next filter ####

		try {
			chain.doFilter(request, response);

			// #################################
			// #################################
			// #################################
			// #################################

			// cleanup after unhandled exception in program flow
		} catch (Exception e) {

			try {
				currentSession = ManagedSessionContext.unbind(sf);

			} catch (Exception e2) {
				e.setStackTrace(e2.getStackTrace());
				currentSession.disconnect();
			}

			try {
				currentSession.disconnect();
			} catch (Exception e3) {
				e.setStackTrace(e3.getStackTrace());
			}

			throw new ServletException(new GUIExceptionWrapper("Unexpected Error after calling chain.doFilter", e));
		}

		try {

			currentSession = ManagedSessionContext.unbind(sf);

			// now we update the hibernate session in the http session
			// or delete it in there
			// end or continue the long-running conversation?
			if (request.getAttribute(END_OF_CONVERSATION_FLAG) != null || request.getParameter(END_OF_CONVERSATION_FLAG) != null) {
				currentSession.close();
				httpSession.setAttribute(HIBERNATE_SESSION_KEY, null);
				httpSession.setMaxInactiveInterval(60);
				log.debug("<<< End of conversation");
			} else {
				//this is the only regular way aout of this filter while
				//http session is running
				httpSession.setAttribute(HIBERNATE_SESSION_KEY, currentSession);
			}

		} catch (StaleObjectStateException staleEx) {

			// Row was updated or deleted by another transaction (or
			// unsaved-value mapping was incorrect)
			throw new ServletException(new GUIExceptionWrapper("StaleObject", staleEx));

		} catch (Throwable thrbl) {
			throw new ServletException(new GUIExceptionWrapper("Unknown Exception - Execution was terminated abnormally", thrbl));
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Obtaining SessionFactory from static HibernateUtil singleton");
		sf = HibernateUtil.getSessionFactory();
	}

	public void destroy() {
		HibernateUtil.getSessionFactory().close();
		log.debug("##################### Hibernate-SessionFactory closed #################################");
	}
}
