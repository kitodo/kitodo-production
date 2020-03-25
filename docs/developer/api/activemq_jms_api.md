Active MQ web services for Kitodo
=================================

JMS
---

Active Message Queue is an open source Java Messaging (JMS) implementation
provided by the Apache Software Foundation. It is intended to be used to
connect software components in a flexible way. The core is the Active MQ
server which can be pictured like a post office. The mail boxes are named
“queue” or “topic”. Queues work as expected: A producer sends a message where
a consumer can pick it up. Topics can be pictured as black boards: The main
difference is: A message read from a queue is removed from the queue. A message
read from a topic is still available to others. Consumer clients can actively
check the server or may register listeners with the server to be notified of
new messages.

API Implementation
------------------

This behaviour has already been implemented to Kitodo: The org.goobi.mq.
ActiveMQDirector is a ServletContextListener which is registered in web.xml.
On application startup, it registers all consumers from its “services” variable
to the server configured in “activeMQ.hostURL”.

The elements of this variable are classes extending the abstract class
ActiveMQProcessor. This class implements the MessageListener and provides
facilities to handle exceptions and to store the consumer which is required on
shutdown to disconnect.

To implement another web service processor, you have to implement a class which
extends ActiveMQProcessor and implements its abstract void process(MapMessage).
Here is the right place to do whatever your processor is intended to do. There
is a class MapMessageObjectReader which shall be used to type safe retrieve
complex objects from MapMessages. You must add your new class to the “services”
variable of ActiveMQDirector then.

The Kitodo server administrator shall be in control which processors are being
started, and which queue names they listen on. Implementation of this
configurability is designed this way: The implementing class must pass its
queue name to the constructor of the parent class. This is done by implementing
the constructor like in the following skeleton. If the queue name is not
configured, it will return null which will prevent the ActiveMQDirector from
registering it to the server. Inside the class, the queue name is available in
the global variable “queueName” which is set by the parent class.  The
implementation may use arbitrary “activeMQ.myService.*” entries in
goobi_config.properties for configuration.

### Service processor skeleton sample

	package org.goobi.mq.processores;

	import org.goobi.mq.*;
	import de.sub.goobi.config.ConfigCore;
	import de.sub.goobi.helper.enums.ReportLevel;

	public class MyServiceProcessor extends ActiveMQProcessor {

		public MyServiceProcessor() {
			super(ConfigMain.getParameter("activeMQ.myService.queue", null));
		}

		@Override
		protected void process(MapMessageObjectReader args) throws Exception {
			// TODO Auto-generated method stub
		}
	}

### Processor Response

Responses from processors are designed to be handled as WebServiceResult
objects. Those objects are MapMessages which send themselves to a topic
configured in “activeMQ.results.topic”. They consist of the Strings “queue”
(the name of the queue the job ticket was sent to), “id” (a String “id” in
the MapMessage which is mandatory), “level” and an optional “message”. When
designing the MapMessage layout to parameterise your web service processor,
please keep in mind that a String element “id” is mandatory.

If process() terminates without error, it is meant to have done its job
successfully and a WebServiceResult with level “success” will be sent. If
process() returns an exception, a WebServiceResult with level “fatal” will be
sent. The exception will be returned as the “message” String. You may also use
the WebServiceResult class to send messages with the levels “error”, “warn”,
“info”, “debug”, “verbose” and “ludicrous” which are meant to be informative
only:
        new WebServiceResult(queueName, args.getMandatoryString("id"),
                ReportLevel.INFO, "Remote host is down, trying again later.")
                .send();

Process Creation Service
------------------------

Kitodo.Production is equipped with a web service interface to automatically
create new processes based on a given template. This allows the digitization
process to be initiated from outside the application, for example by assigning
a new digital ID to a record in a library catalogue (or—at choice of the
library—by duplicating a record and assigning a new digital ID to the
duplicate) and then running a script.

The web service infrastructure is providet by an Active MQ server (see
http://activemq.apache.org/ for details) which needs to be downloaded and
started. Without further configuration, it provides everything necessary on
port 61616 of the machine in question.

The “activeMQ.hostURL” must be set in goobi_config.properties to point to this
server. The “activeMQ.createNewProcess.queue” must be set to point to a queue
of your choice where Kitodo.Production shall pick up orders to create new
processes.

Orders must be javax.jms.MapMessage objects with the following key-value-pairs
provided:

	String template
		name of the process template to use
	String opac
		Cataloge to use for lookup
	String field
		Field to look into, usually 12 (PPN)
	String value
		Value to look for, id of physical medium
	String id
		Ticket ID (used in log responses)
	List<String> collections
		Collections to be selected
	Map<String, String> userFields (optional)
		May be used to populates AdditionalField entries

Here is a sample java client to do the job. It expects to be passed from the
command line the Active MQ host (e.g. tcp://localhost:61616), the queue name
and the parameters as listed above.

To run this application, the following JARs from the ActiveMQ server’s /lib
folder are required on the classpath:

* activemq-core
* geronimo-j2ee-management_1.1_spec
* genonimo-jms_1.1_spec
* log4j2
* slf4j-api
* slf4j-log4j12

### Main.java

	import java.util.*;
	import javax.jms.*;
	import org.apache.activemq.ActiveMQConnectionFactory;

	public class Main {
		public static int main(String[] args) { try {

			// Check arguments
			if (args.length < 8 || (args.length % 2) != 0) {
				System.out.println("Parameters: Active MQ host, queue name, "
						+ "template name, opac name,");
				System.out.println("            no. of search field, search "
						+ "string, digital id, collection name,");
				System.out.println("            [additional details field, "
						+ "value, [add. details field, value, [...");
				return 1;
			}

			// Connect to server
			Connection connection = new ActiveMQConnectionFactory(args[0])
					.createConnection();
			connection.start();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(args[1]);
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create job ticket
			MapMessage message = session.createMapMessage();
			message.setString("template", args[2]);
			message.setString("opac", args[3]);
			message.setString("field", args[4]);
			message.setString("value", args[5]);
			message.setString("id", args[6]);
			List<String> collections = new ArrayList<String>();
			collections.add(args[7]);
			message.setObject("collections", collections);
			Map<String, String> userFields = new HashMap<String, String>();
			for (int i = 8; i < args.length; i += 2)
				userFields.put(args[i], args[i + 1]);
			if (userFields.size() != 0)
				message.setObject("userFields", userFields);

			// Send job ticket
			producer.send(message);

			// Shutdown
			session.close();
			connection.close();
		} catch (Exception e) {	e.printStackTrace(); return 2; }
		return 0;
	}	}


Service to finalize steps
-------------------------

Kitodo.Production is equipped with a web service interface to automatically
finalize steps. This allows external software contributing to a workflow to
report their success from outside the application. Additionally, properties
can be populated and a message can be added to the processes’ log (in former
versions of Kitodo known as “wiki field”).

The web service infrastructure is provided by an Active MQ server (see
http://activemq.apache.org/ for details) which needs to be downloaded and
started. Without further configuration, it provides everything necessary on
port 61616 of the machine in question.

The “activeMQ.hostURL” must be set in goobi_config.properties to point to this
server. The “activeMQ.finaliseStep.queue” must be set to point to a queue
of your choice where Kitodo.Production shall pick up orders to finalize steps.

Orders must be javax.jms.MapMessage objects with the following key-value-pairs
provided:

	String id
		ID of the step to close (do not mix up with the process ID)
	Map<String, String> properties (optional)
		May be used to populates properties
	String message
		Message to be added to the processes’ log.

