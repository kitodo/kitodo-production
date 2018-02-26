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

package org.kitodo.selenium;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.helper.Timer;

public class IndexingST extends BaseTestSelenium {

    private static final Logger logger = LogManager.getLogger(IndexingST.class);

    @Test
    public void reindexingTest() throws Exception {
        Assert.assertTrue(true);
        final float MAXIMUM_TIME_SEC = 40;
        String indexingProgress = "";
        Pages.getIndexingPage().goTo().startReindexingAll();

        Timer timer = new Timer();
        timer.start();
        while (!Pages.getIndexingPage().isIndexingComplete()
                && timer.getElapsedTimeAfterStartSec() < MAXIMUM_TIME_SEC) {
            indexingProgress = Pages.getIndexingPage().getIndexingProgress();
            logger.debug("Indexing at: " + indexingProgress + "%");
            Thread.sleep(Browser.getDelayIndexing());
        }
        timer.stop();
        Thread.sleep(Browser.getDelayIndexing());

        logger.info("Reindexing took: " + timer.getElapsedTimeSec() + " s");
        Assert.assertTrue("Reindexing took to long. Maximum time reached at: " + indexingProgress,
            timer.getElapsedTimeSec() < MAXIMUM_TIME_SEC);
    }
}
