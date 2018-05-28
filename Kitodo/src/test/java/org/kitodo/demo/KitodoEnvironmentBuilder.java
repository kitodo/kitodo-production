package org.kitodo.demo;

import org.kitodo.MockDatabase;

public class KitodoEnvironmentBuilder {

    /**
     * Sets up in-memory elastic search and database server and inserts some demo data.
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

	public static void main(String[] args) {
        setUpEnvironment();
        System.out.println("Kitodo is running now. You can access the application by the URL: http://localhost:8080/kitodo/pages/login.jsf");
        System.out.println("You can stop the application by pressing Ctrl + c");
	}
}
