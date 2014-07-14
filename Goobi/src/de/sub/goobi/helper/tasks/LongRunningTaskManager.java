package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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

import de.sub.goobi.helper.tasks.TaskManager.Actions;

@Deprecated
public class LongRunningTaskManager {
   private static LongRunningTaskManager lrtm;

   /**
    * Singleton-Zugriff
    * ================================================================*/
	@Deprecated
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
   }

   /**
    * alle Tasks der Warteschlange zurückgeben
    * ================================================================*/
	@SuppressWarnings("unchecked")
	@Deprecated
   public LinkedList<LongRunningTask> getTasks() {
		return new LinkedList(TaskManager.getTaskList());
   }

   /**
    * Reihenfolge eines Tasks nach oben
    * ================================================================*/
	@Deprecated
   public void moveTaskUp(LongRunningTask inTask) {
		TaskManager.runEarlier(inTask);
   }

   /**
    * Reihenfolge eines Tasks nach unten
    * ================================================================*/
	@Deprecated
   public void moveTaskDown(LongRunningTask inTask) {
		TaskManager.runLater(inTask);
   }

   /**
    * LongRunningTask in Warteschlange einreihen
    * ================================================================*/
	@Deprecated
   public void addTask(LongRunningTask inTask) {
		TaskManager.addTask(inTask);
   }

   /**
    * LongRunningTask aus der Warteschlange entfernen
    * ================================================================*/
	@Deprecated
   public void removeTask(LongRunningTask inTask) {
		inTask.interrupt(Actions.DELETE_IMMEDIATELY);
   }

   /**
    * LongRunningTask aus der Warteschlange entfernen
    * ================================================================*/
	@Deprecated
	// This is not needed any longer!
   public void replaceTask(LongRunningTask oldTask, LongRunningTask newTask) {
   }

   /**
    * LongRunningTask als Thread ausführen
    * ================================================================*/
	@Deprecated
	// Accidental complexity: call run() on task
   public void executeTask(LongRunningTask inTask) {
      inTask.start();
   }

   /**
    * LongRunningTask abbrechen
    * ================================================================*/
	@Deprecated
   public void cancelTask(LongRunningTask inTask) {
		inTask.interrupt(Actions.PREPARE_FOR_RESTART);
   }

   /**
    * abgeschlossene Tasks aus der Liste entfernen
   * ================================================================*/
	@Deprecated
   public void clearFinishedTasks() {
		TaskManager.removeAllFinishedTasks();
	}
   
   /**
    * alle Tasks aus der Liste entfernen
   * ================================================================*/
	@Deprecated
   public void clearAllTasks() {
		TaskManager.stopAndDeleteAllTasks();
   }

	@Deprecated
   public boolean isRunning() {
		return TaskManager.isAutoRunningThreads();
   }

	@Deprecated
    public void setRunning(boolean running) {
		TaskManager.setAutoRunningThreads(running);
   }

}
