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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.context.ManagedSessionContext;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;

public class HibernateSessionConversationFilter implements Filter {
	private static final Logger log = Logger.getLogger(HibernateSessionConversationFilter.class);

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
				// log.debug("< Continuing conversation");
				currentSession = (org.hibernate.classic.Session) disconnectedSession;
			}

		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper(Helper.getTranslation("err_noConnectionEstablished") + " Establishing Sessions", e));
		}

		try {
			// log.debug("Starting a database transaction");
			// log.debug("Binding the current Session");
			ManagedSessionContext.bind(currentSession);
		} catch (Exception e) {
			throw new ServletException(new GUIExceptionWrapper(Helper.getTranslation("err_noConnectionEstablished") + " ManagedSessionContext", e));
		}

		try {
			currentSession.beginTransaction();
		} catch (HibernateException e) {
			log.error("no database connection available", e);
			ManagedSessionContext.unbind(sf);
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
			// log.debug("Storing Session in the HttpSession");
			try {
				currentSession = ManagedSessionContext.unbind(sf);
				//				currentSession.flush();
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

			currentSession = ManagedSessionContext.unbind(sf);
			//			currentSession.flush();

			// for the purpose of rollback we catch exception here and notify
			// gui of error
			try {
				currentSession.getTransaction().commit();
			} catch (Exception e) {
				try {
					log.error("error while commiting", e);
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

	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Obtaining SessionFactory from static HibernateUtil singleton");
		sf = HibernateUtil.getSessionFactory();
	}

	public void destroy() {
		HibernateUtil.getSessionFactory().close();
		log.debug("##################### Hibernate-SessionFactory closed #################################");
	}
}
