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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.awaitility.Duration;
import org.awaitility.core.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Pages;

import static org.awaitility.Awaitility.with;

public class IndexingST extends BaseTestSelenium {

    @Test
    public void reindexingTest() throws Exception {
        Assert.assertTrue(true);
        Pages.getSystemPage().goTo().startReindexingAll();

        Predicate<String> isIndexingFinished = (d) -> {
            if (Objects.nonNull(d)) {
                return d.equals("100%");
            }
            return false;
        };

        with().conditionEvaluationListener(
            condition -> System.out.printf("%s (elapsed time %dms, remaining time %dms)\n", condition.getDescription(),
                condition.getElapsedTimeInMS(), condition.getRemainingTimeInMS())).await("Wait for reindexing")
                .pollDelay(5, TimeUnit.SECONDS).atMost(90, TimeUnit.SECONDS).pollInterval(Duration.ONE_SECOND)
                .ignoreExceptions()
                .until(() -> isIndexingFinished.matches(Pages.getSystemPage().getIndexingProgress()));
    }
}
