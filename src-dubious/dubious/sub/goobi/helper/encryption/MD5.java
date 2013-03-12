package dubious.sub.goobi.helper.encryption;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



/**
 * <u>MD5-Klasse, zum erzeugen von MD5-Hashes aus Zeichenketten</u><br><br>
 * <b>Class:</b> MD5<br><br>
 * <b>Java-Version:</b> 1.5x<br><br>
 * <b>&copy; Copyright:</b> Karsten Bettray - 2006<br><br>
 * <b>License:</b> Free for non commercial use, for all educational institutions (Schools, Universities ...) and it members<br>
 * @author Karsten Bettray (Universit&auml;t Duisburg-Essen)<br><br>
 * @version 0.1<br>
 *
 */
public class MD5 {
   private String text = null;
   private String hash = null;

   /**
    * <u>Konstruktor mit &Uuml;bergabe der zu verifizierenden Zeichenkette</u>
    * @param text
    */
   public MD5(String text) {
      this.text = text;
   }

   /**
    * <u>Zur&uuml;ckgabe des MD5-Hashes, bei Initialisierter Membervariable 'text'</u>
    * @return
    */
   public String getMD5() {
      if (this.text == null) {
		return null;
	} else {
		return makeMD5();
	}
   }

   /**
    * <u>Zur&uuml;ckgabe des MD5-Hashes</u>
    * @param text
    * @return
    */
   public String getMD5(String text) {
      this.text = text;

      if (text == null) {
		return null;
	} else {
		return makeMD5();
	}
   }

   /**
    * <u>MD5-Hash erzeugen</u>
    * @return
    */
   private String makeMD5() {
      MessageDigest md = null;
      byte[] encryptMsg = null;

      try {
         md = MessageDigest.getInstance("MD5"); // getting a 'MD5-Instance'
         encryptMsg = md.digest(this.text.getBytes()); // solving the MD5-Hash
      } catch (NoSuchAlgorithmException e) {
      }

      String swap = ""; // swap-string for the result
      String byteStr = ""; // swap-string for current hex-value of byte
      StringBuffer strBuf = new StringBuffer();

      for (int i = 0; i <= encryptMsg.length - 1; i++) {

         byteStr = Integer.toHexString(encryptMsg[i]); // swap-string for current hex-value of byte

         switch (byteStr.length()) {
            case 1: // if hex-number length is 1, add a '0' before
               swap = "0" + Integer.toHexString(encryptMsg[i]);
               break;

            case 2: // correct hex-letter
               swap = Integer.toHexString(encryptMsg[i]);
               break;

            case 8: // get the correct substring
               swap = (Integer.toHexString(encryptMsg[i])).substring(6, 8);
               break;
         }
         strBuf.append(swap); // appending swap to get complete hash-key
      }
      this.hash = strBuf.toString(); // String with the MD5-Hash

      return this.hash; // returns the MD5-Hash
   }

   /**
    * <u>Setzt den Text, aus dem der MD5-Hash ermittelt werden soll</u>
    * @param text The text to set.
    */
   public void setText(String text) {
      this.text = text;
   }

   /**
    * <u>Gibt den Text, aus dem der MD5-Hash ermittelt werden soll zur&uuml;ck</u>
    * @return Returns the text.
    */
   public String getText() {
      return this.text;
   }

   /**
    * <u>Konstruktor mit &Uuml;bergabe der zu verifizierenden Zeichenkette</u>
    * @return Returns the hash.
    */
   public String getHash() {
      return this.hash;
   }

}

// MD5-Hash of "Test": 0cbc6611f5540bd0809a388dc95a615b
