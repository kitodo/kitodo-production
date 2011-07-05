package de.sub.goobi.helper.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//TODO: What's the licence of this file?


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
      if (text == null)
         return null;
      else
         return makeMD5();
   }

   /**
    * <u>Zur&uuml;ckgabe des MD5-Hashes</u>
    * @param text
    * @return
    */
   public String getMD5(String text) {
      this.text = text;

      if (text == null)
         return null;
      else
         return makeMD5();
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
         encryptMsg = md.digest(text.getBytes()); // solving the MD5-Hash
      } catch (NoSuchAlgorithmException e) {
         System.out.println("No Such Algorithm Exception!");
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
      hash = strBuf.toString(); // String with the MD5-Hash

      return hash; // returns the MD5-Hash
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
      return text;
   }

   /**
    * <u>Konstruktor mit &Uuml;bergabe der zu verifizierenden Zeichenkette</u>
    * @return Returns the hash.
    */
   public String getHash() {
      return hash;
   }

}

// MD5-Hash of "Test": 0cbc6611f5540bd0809a388dc95a615b
