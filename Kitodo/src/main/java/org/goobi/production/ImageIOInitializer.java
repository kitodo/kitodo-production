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

package org.goobi.production;

import javax.imageio.ImageIO;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Listener to set up static ImageIO library with web application class loader.
 * <p/>
 * This listener works as a workaround for problems occurring when bootstrapping
 * web applications using ImageIO library within Tomcat when using
 * JreMemoryLeakPreventionListener.
 *
 * <p>
 * Bug and solution described here:
 * https://bugs.launchpad.net/goobi-production/+bug/788160
 * </p>
 *
 */
@WebListener
public class ImageIOInitializer implements ServletContextListener {

    static {
        // makes sure, plugins get loaded via web application class loader
        // and are available for later calls from the application
        ImageIO.scanForPlugins();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing is done here
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing is done here
    }

}
