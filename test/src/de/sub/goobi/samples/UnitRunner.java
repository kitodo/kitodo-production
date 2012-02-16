package de.sub.goobi.samples;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class UnitRunner {

   public static void main(String[] args) {
      Result r = JUnitCore.runClasses(BenutzerTest.class, BenutzergruppenTest.class);

      for (Failure f: r.getFailures()) {
         System.out.println("---------------------- " + f.getTestHeader() + "------------------------\n");
         System.out.println(f.getTrace());
      }

      if (!r.wasSuccessful())
         System.out.println(r.getFailureCount() + " Fehler insgesamt");
   }
}
