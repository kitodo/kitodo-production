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

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.LdapGruppe;
import de.sub.goobi.beans.ProjectFileGroup;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Schritteigenschaft;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;

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
