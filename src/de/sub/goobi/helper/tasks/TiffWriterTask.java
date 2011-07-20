package de.sub.goobi.helper.tasks;

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
   public void run() {
      setStatusProgress(2);
      String imageFolder = "";
      /* ---------------------
       * Imageordner ermitteln
       * -------------------*/
      try {
         imageFolder = getProzess().getImagesDirectory();
      } catch (Exception e) {
         e.printStackTrace();
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
