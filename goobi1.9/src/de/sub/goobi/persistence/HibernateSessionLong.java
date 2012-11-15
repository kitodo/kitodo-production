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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;


//TODO: Fix for Hibernate-Session-Management, old Version reactivated

public class HibernateSessionLong {
   private static final Logger mylogger = Logger.getLogger(HibernateSessionLong.class);

   //	protected static ThreadLocal hibernateHolder = new ThreadLocal();
   protected static SessionFactory factory;
   private Session sess;

   /*===============================================================*/

   /**
    * ONLY ever call this method from within the context of a servlet request
    * (specifically, one that has been associated with this filter).  If you
    * want a Hibernate session at some other time, call getSessionFactory()
    * and open/close the session yourself.
    *
    * @return an appropriate Session object
    */
   public Session getSession() throws HibernateException {
    
      if (this.sess == null) {
         if (factory == null) {
            mylogger.debug("getSession() - hibernate-Factory initialisieren", null);
            factory = new Configuration().configure().buildSessionFactory();
         }
         mylogger.debug("getSession() - hibernate-Session initialisieren", null);
         this.sess = factory.openSession();
      
      }
      if (!this.sess.isOpen()) {
    	 this.sess = factory.openSession(); 
      }
    
      return this.sess;
   }

   /*===============================================================*/

   /**
    * @return the hibernate session factory
    */
   public static SessionFactory getSessionFactory() {
      if (factory == null) {
         mylogger.debug("getSessionFactory() - hibernate-Factory initialisieren", null);
         factory = new Configuration().configure().buildSessionFactory();
      }
      return factory;
   }

   /*===============================================================*/

   /**
    * This is a simple method to reduce the amount of code that needs
    * to be written every time hibernate is used.
    */
   public static void rollback(Transaction tx) {
      if (tx != null) {
         try {
            tx.rollback();
         } catch (HibernateException ex) {
            // Probably don't need to do anything - this is likely being
            // called because of another exception, and we don't want to
            // mask it with yet another exception.
         }
      }
   }

   public Session getNewSession() throws HibernateException {
	   this.sess.clear();
	   this.sess.close();
	   this.sess=null;
	   return getSession();
   }
   
}

