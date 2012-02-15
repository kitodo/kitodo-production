package de.sub.goobi.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import de.sub.goobi.beans.Benutzer;

@Ignore("Crashing") 
public class BenutzerTest {


   @Test 
   public void testLogin1() {
      Benutzer b = new Benutzer();
      b.setLogin("ein Name");
      System.out.println(b.getPasswortCrypt());
   }

   @Test
   @Ignore ("hallo") 
   public void testLogin2() {
      Benutzer b = new Benutzer();
      b.setLogin("ein Name");
//      b.setMitMassendownload(true);
      assertTrue("wert falsch" ,b.isMitMassendownload());
      assertEquals("ein Name", b.getLogin());
   }
}
