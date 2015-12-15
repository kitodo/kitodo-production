package de.sub.goobi.helper;

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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import org.goobi.io.SafeFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;


public class CopyFile {

   // program options initialized to default values
   private static int bufferSize = 4 * 1024;

   public static Long copyFile(SafeFile srcFile, SafeFile destFile) throws IOException {
      InputStream in = srcFile.createFileInputStream();
      OutputStream out = destFile.createFileOutputStream();

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

   public static Long createChecksum(SafeFile file) throws IOException {
      InputStream in = file.createFileInputStream();
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

   public static Long start(SafeFile srcFile, SafeFile destFile) throws IOException {
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
	public static void copyDirectory(SafeFile srcDir, SafeFile dstDir) throws IOException {
		if (srcDir.isDirectory()) {
			if (!dstDir.exists()) {
				dstDir.mkdir();
			}

			String[] children = srcDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new SafeFile(srcDir, children[i]), new SafeFile(dstDir, children[i]));
			}
		} else {
			copyFile(srcDir, dstDir);
		}
	}
}
