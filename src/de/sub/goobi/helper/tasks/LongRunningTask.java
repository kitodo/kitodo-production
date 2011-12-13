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
