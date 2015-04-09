package de.sub.goobi.persistence;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.context.internal.ManagedSessionContext;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;

public class HibernateSessionConversationFilter implements Filter {
	private static final Logger log = Logger.getLogger(HibernateSessionConversationFilter.class);

	private SessionFactory sf;

	public static final String HIBERNATE_SESSION_KEY = "hibernateSession";
	public static final String END_OF_CONVERSATION_FLAG = "endOfConversation";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		org.hibernate.Session currentSession;
		HttpSession httpSession;

		try {
			// Try to get a Hibernate Session from the HttpSession
			httpSession = ((HttpServletRequest) request).getSession();
			Session disconnectedSession = (Session) httpSession.getAttribute(HIBERNATE_SESSION_KEY);

			// Start a new conversation or in the middle?
			if (disconnectedSession == null) {
				log.debug(">>> New conversation");
				currentSession = this.sf.openSession();
				currentSession.setFlushMode(FlushMode.MANUAL);
			} else {
				currentSession = disconnectedSession;
			}

		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper(Helper.getTranslation("err_noConnectionEstablished") + " Establishing Sessions", e));
		}

		try {
			ManagedSessionContext.bind(currentSession);
		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper(Helper.getTranslation("err_noConnectionEstablished") + " ManagedSessionContext", e));
		}

		try {
			currentSession.beginTransaction();
		} catch (HibernateException e) {
			log.error("no database connection available", e);
			ManagedSessionContext.unbind(this.sf);
			throw new ServletException(new GUIExceptionWrapper(Helper.getTranslation("err_noConnectionEstablished") + " BeginTransaction", e));
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
				currentSession = ManagedSessionContext.unbind(this.sf);
				currentSession.getTransaction().commit();

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

			currentSession = ManagedSessionContext.unbind(this.sf);

			// for the purpose of rollback we catch exception here and notify
			// gui of error
			try {
				currentSession.getTransaction().commit();
			} catch (Exception e) {
				try {
					log.error("error while committing", e);
					if (currentSession.getTransaction().isActive()) {
						currentSession.getTransaction().rollback();
						currentSession.close();
						throw new ServletException(new GUIExceptionWrapper("Unexpected Error while trying to commit and rollback active transaction."
								+ "Data is probably not saved", e));
					}
				} catch (Throwable thrbl) {
					if (currentSession != null) {
						currentSession.close();
					}
					throw new ServletException(new GUIExceptionWrapper("Unexpected Error while trying to commit inactive transaction."
							+ "Data is probably not saved", e));
				}
			}

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

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Obtaining SessionFactory from static HibernateUtil singleton");
		this.sf = HibernateUtil.getSessionFactory();
	}

	@Override
	public void destroy() {
		HibernateUtil.getSessionFactory().close();
		log.debug("##################### Hibernate-SessionFactory closed #################################");
	}
}
