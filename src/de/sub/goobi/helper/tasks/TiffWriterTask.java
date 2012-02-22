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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.sub.goobi.beans.Prozess;

public class TiffWriterTask extends LongRunningTask {

   @Override
   public void initialize(Prozess inProzess) {
      super.initialize(inProzess);
      setTitle("Tiffwriter: " + inProzess.getTitel());
   }

   /**
    * Aufruf als Thread
    * ================================================================*/
   public void run() {
      setStatusProgress(2);
      String imageFolder = "";
      /* ---------------------
       * Imageordner ermitteln
       * -------------------*/
      try {
         imageFolder = getProzess().getImagesDirectory();
      } catch (Exception e) {
    	  logger.error(e);
         setStatusMessage("Error while getting process data folder: " + e.getClass().getName() + " - "
               + e.getMessage());
         setStatusProgress(-1);
         return;
      }
      if (imageFolder.equals("")) {
         setStatusMessage("No imagefolder found");
         setStatusProgress(-1);
         return;
      }

      ArrayList<File> myTifs = new ArrayList<File>();
      listAllTifFiles(new File(imageFolder), myTifs);
      logger.trace(myTifs.size());

      int progressStepSizePerImage = 50 / myTifs.size();
      for (File file : myTifs) {
         setStatusProgress(getStatusProgress() + progressStepSizePerImage);
         logger.trace(getStatusProgress() + ": " + file.getAbsolutePath());
      }

      /* ---------------------
       * Abschluss
       * -------------------*/
      setStatusMessage("done");
      setStatusProgress(100);
      
   }

   //TODO Make this public and move it to FileUtils
   // Process only files under dir
   private void listAllTifFiles(File dir, List<File> inFiles) {
      FilenameFilter tiffilter = new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".tif");
         }
      };
      FileFilter folderFilter = new FileFilter() {
         public boolean accept(File file) {
            return file.isDirectory();
         }
      };
      File[] folders = dir.listFiles(folderFilter);
      for (int i = 0; i < folders.length; i++) {
         listAllTifFiles(folders[i], inFiles);
      }

      File[] tiffiles = dir.listFiles(tiffilter);
      inFiles.addAll(Arrays.asList(tiffiles));

   }

}
