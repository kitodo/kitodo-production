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

import org.kitodo.MockDatabase;

public class KitodoEnvironmentBuilder {

    /**
     * Sets up in-memory elastic search and database server and inserts some test
     * data.
     */
    public static void setUpEnvironment() {
        try {
            System.out.println("Starting ElasticSearch server ...");
            MockDatabase.startNode();
            System.out.println("Starting Database server ...");
            MockDatabase.insertProcessesFull();
            MockDatabase.startDatabaseServer();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Main method to make this class executable from command-line.
     * 
     * @param args
     *            The command-line arguments.
     */
    public static void main(String[] args) {
        setUpEnvironment();
        System.out.println(
            "Kitodo is running now. You can access the application by the URL: http://localhost:8080/kitodo/pages/login.jsf");
        System.out.println("The login can be done with der username \"kowal\" and password \"test\"");
        System.out.println("You can stop the application by pressing Ctrl + c");
    }
}
