package de.sub.goobi.helper.archive;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;

@Ignore("Crashing") 
public class ProcessSwapOutTaskTest {
   static Prozess proz = null;
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      proz = new ProzessDAO().get(119);
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      
   }

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void swapTest(){
      proz.setSwappedOutGui(false);
      swapOut();
   }
   
   private void swapOut() {
      ProcessSwapOutTask psot = new ProcessSwapOutTask();
      psot.initialize(proz);
      psot.execute();
      assertTrue(proz.isSwappedOutGui());
   }

   private void swapIn() {
      ProcessSwapInTask psot = new ProcessSwapInTask();
      psot.initialize(proz);
      psot.execute();
      assertFalse(proz.isSwappedOutGui());
   }

}
