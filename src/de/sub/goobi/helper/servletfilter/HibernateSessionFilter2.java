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

import de.sub.goobi.Persistence.HibernateSessionLong;
import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;

// TODO: Previous Hibernate-Filter for old manual Hibernate-Session-Management, old version, reactivated, because 
// de.sub.goobi.Persistence.HibernateSessionConversationFilter does not work like it should
public class HibernateSessionFilter2 implements Filter {
	//   private static final Logger mylogger = Logger.getLogger(HibernateSessionFilter2.class);

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/*===============================================================*/

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//      long startzeit = System.currentTimeMillis();
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
			//         mylogger.debug("Requestzeit: " + (System.currentTimeMillis() - startzeit) + " ms");
		}
	}

	/*===============================================================*/

	/**
    */
	public void destroy() {
		// Nothing necessary
	}

}
