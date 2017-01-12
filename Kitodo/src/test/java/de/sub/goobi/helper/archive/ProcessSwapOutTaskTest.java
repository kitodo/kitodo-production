/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

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
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.persistence.ProzessDAO;

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
		psot.run();
      assertTrue(proz.isSwappedOutGui());
   }

	@SuppressWarnings("unused")
   private void swapIn() {
      ProcessSwapInTask psot = new ProcessSwapInTask();
      psot.initialize(proz);
		psot.run();
      assertFalse(proz.isSwappedOutGui());
   }

}
