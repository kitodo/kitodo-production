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

package de.sub.goobi.helper;
//TODO: Create a utility class for file operations

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.zip.CRC32;


public class CopyFile {

   // program options initialized to default values
   private static int bufferSize = 4 * 1024;

   public static Long copyFile(File srcFile, File destFile) throws IOException {
      InputStream in = new FileInputStream(srcFile);
      OutputStream out = new FileOutputStream(destFile);

      //TODO use a better checksumming algorithm like SHA-1
      CRC32 checksum = new CRC32();
      checksum.reset();

      byte[] buffer = new byte[bufferSize];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) >= 0) {

         checksum.update(buffer, 0, bytesRead);

         out.write(buffer, 0, bytesRead);
      }
      out.close();
      in.close();
      return Long.valueOf(checksum.getValue());

   }

   public static Long createChecksum(File file) throws IOException {
      InputStream in = new FileInputStream(file);
      CRC32 checksum = new CRC32();
      checksum.reset();
      byte[] buffer = new byte[bufferSize];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) >= 0) {
         checksum.update(buffer, 0, bytesRead);
      }
      in.close();
      return Long.valueOf(checksum.getValue());
   }

   public static Long start(File srcFile, File destFile) throws IOException {
      // make sure the source file is indeed a readable file
      if (!srcFile.isFile() || !srcFile.canRead()) {
         System.err.println("Not a readable file: " + srcFile.getName());
      }

      // copy file, optionally creating a checksum
      Long checksumSrc = copyFile(srcFile, destFile);

      // copy timestamp of last modification
      if (!destFile.setLastModified(srcFile.lastModified())) {
         System.err.println("Error: Could not set " + "timestamp of copied file.");
      }

      // verify file
      Long checksumDest = createChecksum(destFile);
      if (checksumSrc.equals(checksumDest)) {
         return checksumDest;
      } else {
         return Long.valueOf(0);
      }

   }
   
	
	/**
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it
	 * will be created.
	 */
	public static void copyDirectory(File srcDir, File dstDir) throws IOException {
		if (srcDir.isDirectory()) {
			if (!dstDir.exists()) {
				dstDir.mkdir();
			}

			String[] children = srcDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
			}
		} else {
			copyFile(srcDir, dstDir);
		}
	}
}
