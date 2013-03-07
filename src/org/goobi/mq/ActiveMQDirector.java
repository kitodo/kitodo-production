/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.mq;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.goobi.mq.processors.CreateNewProcessProcessor;
import org.goobi.mq.processors.CreateNewProcessWithLogicalStructureData;
import org.goobi.mq.processors.FinaliseStepProcessor;

import de.sub.goobi.config.ConfigMain;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class ActiveMQDirector is the head of all Active MQ processors. It
 * implements the ServletContextListener interface and is − if configured in
 * web.xml − called automatically upon server starup. Its job is to connect to
 * the Active MQ server and register the listeners configured.
 * 
 * The ActiveMQDirector should ALWAYS be declared in web.xml. The Active MQ
 * services are intended to be run in case that “activeMQ.hostURL” is configured
 * in the GoobiConfig.properties file. To disable the service, the entry there
 * should be emptied or commented out. Otherwise, a valid configuration would
 * not start working and an administrator will hardly have a chance to find out
 * why without inspecting the source code.
 * 
 * The class ActiveMQDirector also provides a basic ExceptionListener
 * implementation as required for the connection.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class ActiveMQDirector implements ServletContextListener,
		ExceptionListener {
	private static final Logger logger = Logger
			.getLogger(ActiveMQDirector.class);

	// *** CONFIGURATION ***
	// When implementing new Services, add them to this list:

	protected static ActiveMQProcessor[] services;
	static{
		services = new ActiveMQProcessor[] {
			new CreateNewProcessProcessor(),
			new FinaliseStepProcessor(),
            new CreateNewProcessWithLogicalStructureData()
		};
	}

	protected static Connection connection = null;
	protected static Session session = null;
	protected static MessageProducer resultsTopic;
	private static AtomicBoolean isReconnecting = new AtomicBoolean(false);
	private static AtomicInteger currentCountOfReconnectAttempts = new AtomicInteger(1);

	/**
	 * The method contextInitialized() is called by the web container on startup
	 * and is used to start up the active MQ connection. All processors from
	 * services[] are registered.
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 *      .ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent initialisation) {
		setupConnection();
	}

	/**
	 * Setup a configured ActiveMQ server connection.
	 *
	 */
	protected void setupConnection() {
		String activeMQHost = ConfigMain.getParameter("activeMQ.hostURL", null);
		if (activeMQHost != null) {
			session = connectToServer(activeMQHost);
			if (session != null) {
				registerListeners(services);
				if (ConfigMain.getParameter("activeMQ.results.topic", null) != null) {
					resultsTopic = setUpReportChannel(ConfigMain.getParameter("activeMQ.results.topic"));
				}
			}
		}
	}

	/**
	 * Sets up a connection to an active MQ server. The connection object is
	 * global because it is needed later to shut down the connection.
	 * 
	 * @param server
	 *            should be “tcp://{host}:{port}” or “vm://localhost” in case
	 *            that the server is run inside the same virtual machine
	 * @return the session object or “null” upon error
	 */
	protected Session connectToServer(String server) {
		try {
			connection = new ActiveMQConnectionFactory(server).createConnection();
			connection.start();
			connection.setExceptionListener(this); // → ActiveMQDirector.onException()
			return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			logger.error("Error connecting to ActiveMQ server. Reason: " + e.getMessage() + " Try to reconnect.");
			reconnectToServer();
		}
		return null;
	}

	/**
	 * This method registers the listeners with the active MQ server.
	 * 
	 * If a queue name was configured for a service, a MessageConsumer is set up
	 * to listen on that queue and, in case of incoming messages, make the
	 * service process the message. The message checker is saved inside the
	 * service to be able to shut it down later.
	 */
	protected void registerListeners(ActiveMQProcessor[] processors) {
		for (ActiveMQProcessor processor : processors) {
			if (processor.getQueueName() != null) {
				MessageConsumer messageChecker = null;
				try {
					Destination queue = session.createQueue(processor.getQueueName());
					messageChecker = session.createConsumer(queue);
					messageChecker.setMessageListener(processor);
					processor.saveChecker(messageChecker);
				} catch (Exception e) {
					logger.fatal("Error setting up monitoring for \"" + processor.getQueueName() + "\": Giving up.", e);
				}
			}
		}
	}

	/**
	 * This sets up a connection to the topic the results shall be written to.
	 * The delivery mode is set so “persistent” to allow consumers not online
	 * with the server in the moment of message submission to read the messages
	 * later. The log messages are set to be kept on the server for 7 days. This
	 * value can be overridden from the GoobiConfig.properties parameter
	 * “activeMQ.results.timeToLive”. The time to live must be specified in
	 * milliseconds; 0 disables the oblivion. (See also:
	 * http://docs.oracle.com/javaee/6/api/javax/jms/MessageProducer.html#setTimeToLive%28long%29 )
	 * 
	 * @param topic
	 *            name of the active MQ topic
	 * @return a MessageProducer object ready for writing or “null” on error
	 */
	protected MessageProducer setUpReportChannel(String topic) {
		MessageProducer result;
		try {
			Destination channel = session.createTopic(topic);
			result = session.createProducer(channel);
			result.setDeliveryMode(DeliveryMode.PERSISTENT);
			result.setTimeToLive(ConfigMain.getLongParameter("activeMQ.results.timeToLive", 604800000));
			return result;
		} catch (Exception e) {
			logger.fatal("Error setting up report channel \"" + topic + "\": Giving up.", e);
		}
		return null;
	}

	/**
	 * This method is referenced from this.connectToServer() − see there.
	 * 
	 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
	 */
	@Override
	public void onException(JMSException exce) {
		Throwable cause = exce.getCause();
		if (cause.getClass().getCanonicalName().equals("java.io.EOFException")) {
			logger.warn("Connection to ActiveMQ server got lost. Try to reconnect.");
			reconnectToServer();
		} else {
			logger.error(exce);
		}

	}

	/**
	 * Reestablish a connection to an ActiveMQ server.
	 *
	 */
	protected void reconnectToServer() {
		long waitTime = ConfigMain.getLongParameter("activeMQ.reconnectWaitTime", 60000);
		int maxReconnectAttemptCount = ConfigMain.getIntParameter("activeMQ.reconnectMaximumAttemptCount", 5);

		if (isReconnecting.get()) {
			logger.warn("Already try to reconnect. Abort this attempt.");
			return;
		}

		if (currentCountOfReconnectAttempts.get() > maxReconnectAttemptCount) {
			logger.fatal("Maximum attempts to reconnect reached. Could not connect to server in given time and attempts.");
			return;
		}

		logger.debug("Current try: " + currentCountOfReconnectAttempts.get());
		try {
			isReconnecting.set(true);
			cleanupServerConnection();
			logger.debug("Waiting " + waitTime + " milli seconds.");
			Thread.sleep(waitTime);
			logger.debug("Reconnect to ActiveQM server.");
			setupConnection();
			isReconnecting.set(false);
			if (session != null && connection != null) {
				logger.debug("Reconnecting was successful.");
				currentCountOfReconnectAttempts.set(1);
			} else {
				logger.error("Could not reestablish connection to ActiveMQ server!");
				int newValue = currentCountOfReconnectAttempts.incrementAndGet();
				logger.debug("new try value: " + newValue);
				reconnectToServer();
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Any class that wants to create new Active MQ Messages needs read access
	 * to the session, since Active MQ messages don’t have a constructor.
	 * 
	 * @return the session object
	 */
	public static Session getSession() {
		return session;
	}

	/**
	 * Instances of WebServiceResult can be sent by calling their send() method.
	 * Therefore, they need read access on their topic.
	 * 
	 * @return the resultsTopic object
	 */
	public static MessageProducer getResultsTopic() {
		return resultsTopic;
	}

	/**
	 * The method contextDestroyed is called by the web container on shutdown.
	 * It shuts down all listeners, the session and last, the connection.
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 *      ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent destruction) {
		// Shut down all watchers on any queues
		for (ActiveMQProcessor service : services) {
			MessageConsumer watcher = service.getChecker();
			if (watcher != null) {
				try {
					watcher.close();
				} catch (JMSException e) {
					logger.error(e);
				}
			}
		}

		cleanupServerConnection();
	}

	/**
	 * Close open session and connection.
	 */
	protected void cleanupServerConnection() {
		if (session != null) {
			try {
				session.close();
			} catch (JMSException jmse) {
				logger.error("Could not close session! Reason: " + jmse.getMessage());
			}
		}

		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException jmse) {
				logger.error("Could not close connection! Reason: " + jmse.getMessage());
			}
		}

		session = null;
		connection = null;
	}

}
