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

package de.sub.goobi.helper.tasks;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;

public class LongRunningTask extends Thread {
	protected static final Logger logger = Logger.getLogger(LongRunningTask.class);
	   
	private int statusProgress = 0;
   private String statusMessage = "";
   private String longMessage ="";
   private String title = "MasterTask";
   private Prozess prozess;
   private boolean isSingleThread = true;
   
   public void initialize(Prozess inProzess){
      prozess = inProzess;
   }

   public void execute() {
      statusProgress = 1;
      statusMessage = "running";
      isSingleThread = false;
      run();
   }

   public void cancel() {
      statusMessage = "stopping";
      this.interrupt();
   }

   protected void stopped() {
      statusMessage = "stopped";
      statusProgress = -1;
   }

   @Override
   public void run() {
      /* ---------------------
       * Simulierung einer lang laufenden Aufgabe
      * -------------------*/
      for (int i = 0; i < 100; i++) {
         /* prüfen, ob der Thread unterbrochen wurde,
          * wenn ja, stopped() */
         if (this.isInterrupted()) {
            stopped();
            return;
         }
         /* lang dauernde Schleife zur Simulierung einer langen Aufgabe */
         for (double j = 0; j < 10000000; j++) {
         }
         setStatusProgress(i);
      }
      setStatusMessage("done");
      setStatusProgress(100);
   }

   /**
    * Prozess-Getter 
    * ================================================================*/
   public Prozess getProzess() {
      return prozess;
   }

   /**
    * Status des Tasks in Angabe von Prozent
    * ================================================================*/
   public int getStatusProgress() {
      return statusProgress;
   }

   /**
    * Meldung über den aktuellen Task 
    * ================================================================*/
   public String getStatusMessage() {
      return statusMessage;
   }

   /**
    * Titel des aktuellen Task 
    * ================================================================*/
   public String getTitle() {
      return title;
   }

   /**
    * Setter für Fortschritt nur für vererbte Klassen
    * ================================================================*/
   protected void setStatusProgress(int statusProgress) {
      this.statusProgress = statusProgress;
   }

   /**
    * Setter für Statusmeldung nur für vererbte Klassen
    * ================================================================*/
   protected void setStatusMessage(String statusMessage) {
      this.statusMessage = statusMessage;
      if (!isSingleThread){
         Helper.setMeldung(statusMessage);
         logger.debug(statusMessage);
      }
   }

   /**
    * Setter für Titel nur für vererbte Klassen
    * ================================================================*/
   protected void setTitle(String title) {
      this.title = title;
   }

   /**
    * Setter für Prozess nur für vererbte Klassen
    * ================================================================*/
   protected void setProzess(Prozess prozess) {
      this.prozess = prozess;
   }

   public String getLongMessage() {
      return longMessage;
   }

   public void setLongMessage(String inlongMessage) {
      this.longMessage = inlongMessage;
   }
   
   

}
