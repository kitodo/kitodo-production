package de.sub.goobi.helper.tasks;
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
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.sub.goobi.Beans.Prozess;

public class TiffWriterTask extends LongRunningTask {

   @Override
   public void initialize(Prozess inProzess) {
      super.initialize(inProzess);
      setTitle("Tiffwriter: " + inProzess.getTitel());
   }

   /**
    * Aufruf als Thread
    * ================================================================*/
   @Override
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
         @Override
		public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".tif");
         }
      };
      FileFilter folderFilter = new FileFilter() {
         @Override
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
   //Draft
   /*
   public static List<File> listAllFiles (File dir, String extension) {
	   if (!dir.isDirectory()) {
		   throw new IllegalStateException("Expected a directory, got: " dir.getAbsolutePath());
	   }
	   List<File> files = new ArrayList<File>();
	   
	   
	   return files;
   }
   */
}
