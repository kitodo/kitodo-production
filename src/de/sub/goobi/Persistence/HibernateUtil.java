package de.sub.goobi.Persistence;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Beans.LdapGruppe;
import de.sub.goobi.Beans.ProjectFileGroup;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Regelsatz;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Beans.Vorlage;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueck;
import de.sub.goobi.Beans.Werkstueckeigenschaft;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;

	static {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void clearSession() {
		sessionFactory.evict(Benutzer.class);
		sessionFactory.evict(Benutzergruppe.class);
		sessionFactory.evict(LdapGruppe.class);
		sessionFactory.evict(ProjectFileGroup.class);
		sessionFactory.evict(Projekt.class);
		sessionFactory.evict(Prozess.class);
		sessionFactory.evict(Prozesseigenschaft.class);
		sessionFactory.evict(Regelsatz.class);
		sessionFactory.evict(Schritt.class);
		sessionFactory.evict(Schritteigenschaft.class);
		sessionFactory.evict(Vorlage.class);
		sessionFactory.evict(Vorlageeigenschaft.class);
		sessionFactory.evict(Werkstueck.class);
		sessionFactory.evict(Werkstueckeigenschaft.class);
	}

}
