package de.sub.goobi.helper.tasks;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class LongRunningTaskManager {
   static LinkedList<LongRunningTask> tasks = new LinkedList<LongRunningTask>();
   private static LongRunningTaskManager lrtm;
//   Helper help = new Helper();
   static boolean running = false;
   Timer autoRunTimer;

   /**
    * Singleton-Zugriff
    * ================================================================*/
   public static LongRunningTaskManager getInstance() {
      if (lrtm == null) {
         lrtm = new LongRunningTaskManager();
      }
      return lrtm;
   }

   /**
    * privater Konstruktor
    * ================================================================*/
   private LongRunningTaskManager() {
      /* --------------------------------
       * Nachrichtensystem initialisieren
       * --------------------------------*/
      int delay = 5000;
      int period = 2000;
      autoRunTimer = new Timer();
      autoRunTimer.scheduleAtFixedRate(new TimerTask() {
         public void run() {
            LongRunningTaskManager.check_autoRunningTasks();
         }
      }, delay, period);
   }

   /**
    * Diese Methode überprüft, ob wir was neues in unserem Container haben.
    * ================================================================
    */
   private static void check_autoRunningTasks() {
      if (!running)
         return;
      for (LongRunningTask lrt : tasks) {
         if (lrt.getStatusProgress() > 0 && lrt.getStatusProgress() < 100)
            return;
      }
      if (tasks.size() > 0) {
         for (LongRunningTask lrt : tasks) {
            if (lrt.getStatusProgress() == 0) {
               lrt.execute();
               return;
            }
         }
      }
   }

   /**
    * alle Tasks der Warteschlange zurückgeben
    * ================================================================*/
   public LinkedList<LongRunningTask> getTasks() {
      return tasks;
   }

   /**
    * Reihenfolge eines Tasks nach oben
    * ================================================================*/
   public void moveTaskUp(LongRunningTask inTask) {
      if (tasks.getFirst() == inTask)
         return;
      int id = tasks.indexOf(inTask) - 1;
      removeTask(inTask);
      tasks.add(id, inTask);
   }

   /**
    * Reihenfolge eines Tasks nach unten
    * ================================================================*/
   public void moveTaskDown(LongRunningTask inTask) {
      if (tasks.getLast() == inTask)
         return;
      int id = tasks.indexOf(inTask) + 1;
      removeTask(inTask);
      tasks.add(id, inTask);
   }

   /**
    * LongRunningTask in Warteschlange einreihen
    * ================================================================*/
   public void addTask(LongRunningTask inTask) {
      tasks.add(inTask);
   }

   /**
    * LongRunningTask aus der Warteschlange entfernen
    * ================================================================*/
   public void removeTask(LongRunningTask inTask) {
      tasks.remove(inTask);
   }

   /**
    * LongRunningTask aus der Warteschlange entfernen
    * ================================================================*/
   public void replaceTask(LongRunningTask oldTask, LongRunningTask newTask) {
      int id = tasks.indexOf(oldTask);
      tasks.set(id, newTask);
   }

   /**
    * LongRunningTask als Thread ausführen
    * ================================================================*/
   public void executeTask(LongRunningTask inTask) {
      inTask.start();
   }

   /**
    * LongRunningTask abbrechen
    * ================================================================*/
   public void cancelTask(LongRunningTask inTask) {
      inTask.cancel();
   }

   /**
    * abgeschlossene Tasks aus der Liste entfernen
   * ================================================================*/
   public void clearFinishedTasks() {
      for (LongRunningTask lrt : new LinkedList<LongRunningTask>(tasks)) {
         if (lrt.getStatusProgress() == 100)
            tasks.remove(lrt);
      }
   }
   
   /**
    * alle Tasks aus der Liste entfernen
   * ================================================================*/
   public void clearAllTasks() {
      for (LongRunningTask lrt : new LinkedList<LongRunningTask>(tasks)) {
         if (lrt.getStatusProgress() == 100 || lrt.getStatusProgress() < 1)
            tasks.remove(lrt);
      }
   }

   public boolean isRunning() {
      return running;
   }

    public void setRunning(boolean running) {
      LongRunningTaskManager.running = running;
   }

}
