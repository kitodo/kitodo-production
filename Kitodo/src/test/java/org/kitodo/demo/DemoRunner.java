package org.kitodo.demo;

import org.kitodo.MockDatabase;

public class DemoRunner {


	public static void main(String[] args) {
		try {
			System.out.println("Starting ElasticSearch server ...");
			MockDatabase.startNode();
			System.out.println("Starting database server ...");
			MockDatabase.insertProcessesFull();
			MockDatabase.startDatabaseServer();
			System.out.println("Kitodo is running now. You can try the demo application by accessing: localhost:8080/kitodo/pages/login.jsf");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
