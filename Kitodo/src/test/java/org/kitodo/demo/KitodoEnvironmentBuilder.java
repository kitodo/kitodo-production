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

package org.kitodo.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.MockDatabase;

public class KitodoEnvironmentBuilder {

    private static final Logger logger = LogManager.getLogger(KitodoEnvironmentBuilder.class);

    /**
     * Sets up in-memory elastic search and database server and inserts some test
     * data.
     *
     * @throws Exception
     *         if the environment could not be started or populated
     */
    public static void setUpEnvironment() throws Exception {
        System.out.println("Starting ElasticSearch server ...");
        MockDatabase.startNode();
        System.out.println("Inserting test data ...");
        MockDatabase.insertProcessesFull();
        System.out.println("Starting Database server ...");
        MockDatabase.startDatabaseServer();
    }

    /**
     * Main method to make this class executable from command-line.
     *
     * @param args
     *            The command-line arguments.
     */
    public static void main(String[] args) {
        try {
            setUpEnvironment();
        } catch (Throwable e) {
            logger.error("Failed to set up the Kitodo demo environment!", e);
            System.exit(1);
        }
        System.out.println(
            "Kitodo is running now. You can access the application by the URL: http://localhost:8080/kitodo/pages/login");
        System.out.println("The login can be done with der username \"kowal\" and password \"test\"");
        System.out.println("You can stop the application by pressing Ctrl + c");
    }
}
