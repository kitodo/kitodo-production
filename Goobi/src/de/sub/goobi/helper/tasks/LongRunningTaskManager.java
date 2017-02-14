package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class LongRunningTaskManager {
   static LinkedList<LongRunningTask> tasks = new LinkedList<LongRunningTask>();
   private static LongRunningTaskManager lrtm;
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
      this.autoRunTimer = new Timer();
      this.autoRunTimer.scheduleAtFixedRate(new TimerTask() {
         @Override
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
      if (!running) {
		return;
	}
      for (LongRunningTask lrt : tasks) {
         if (lrt.getStatusProgress() > 0 && lrt.getStatusProgress() < 100) {
			return;
		}
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
      if (tasks.getFirst() == inTask) {
		return;
	}
      int id = tasks.indexOf(inTask) - 1;
      removeTask(inTask);
      tasks.add(id, inTask);
   }

   /**
    * Reihenfolge eines Tasks nach unten
    * ================================================================*/
   public void moveTaskDown(LongRunningTask inTask) {
      if (tasks.getLast() == inTask) {
		return;
	}
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
         if (lrt.getStatusProgress() == 100) {
			tasks.remove(lrt);
		}
      }
   }
   
   /**
    * alle Tasks aus der Liste entfernen
   * ================================================================*/
   public void clearAllTasks() {
      for (LongRunningTask lrt : new LinkedList<LongRunningTask>(tasks)) {
         if (lrt.getStatusProgress() == 100 || lrt.getStatusProgress() < 1) {
			tasks.remove(lrt);
		}
      }
   }

   public boolean isRunning() {
      return running;
   }

    public void setRunning(boolean running) {
      LongRunningTaskManager.running = running;
   }

}
