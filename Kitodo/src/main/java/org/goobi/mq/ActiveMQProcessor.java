//CHECKSTYLE:OFF
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
//CHECKSTYLE:ON

package org.goobi.mq;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.ReportLevel;

import java.util.HashMap;
import java.util.Map;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

/**
 * The class ActiveMQProcessor offers general services, such as making the ncoming messages available as MapMessages
 * and publishing the results. When I came clear that this code would be necessary for every processor, I thought
 * an abstract class would be the right place for it. ActiveMQProcessor also provides a place to save the checker for
 * the ActiveMQDirector, to be able to shut it down later.
 *
 * @author Matthias Ronge 	&lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class ActiveMQProcessor implements MessageListener {

	protected String queueName; // the queue name will be available here
	private MessageConsumer checker;

	/**
	 * Implement the method process() to let your service actually do what you want him to do.
	 *
	 * @param ticket A MapMessage which can be processor-specific except that it requires to have a field “id”.
	 */
	protected abstract void process(MapMessageObjectReader ticket) throws Exception;

	/**
	 * Instantiating the class ActiveMQProcessor always requires to pass the name of the queue it should be attached to.
	 * That means, your constructor should look like this:
	 *
	 * <pre>
	 * 	  public MyServiceProcessor(){
	 * 		  super(ConfigMain.getParameter("activeMQ.myService.queue", null));
	 * 	  }
	 * </pre>
	 *
	 * <p>If the parameter is not set in goobi_config.properties, it will return “null” and so prevents it from being
	 * set up in ActiveMQDirector.</p>
	 *
	 * @param queueName the queue name, if configured, or “null” to prevent the processor from being connected.
	 */
	public ActiveMQProcessor(String queueName) {
		this.queueName = queueName;
	}

	/**
	 * The method onMessage() provides a corset which checks the message being a MapMessage, performs a type safe type
	 * cast, and extracts the job’s ID to be able to send useful results to the results topic, which it does after
	 * the abstract method process() has finished.
	 *
	 * <p>Since this will be the same for all processors which use MapMessages, I extracted the portion into the
	 * abstract class.</p>
	 *
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message arg) {
		MapMessageObjectReader ticket = null;
		String ticketID = null;

		try {
			// Basic check ticket
			if (arg instanceof MapMessage) {
				ticket = new MapMessageObjectReader((MapMessage) arg);
			} else {
				throw new IllegalArgumentException("Incompatible types.");
			}
			ticketID = ticket.getMandatoryString("id");

			// turn on logging
			Map<String, String> loggingConfig = new HashMap<String, String>();
			loggingConfig.put("queueName", queueName);
			loggingConfig.put("id", ticketID);
			Helper.activeMQReporting = loggingConfig;

			// process ticket
			process(ticket);

			// turn off logging again
			Helper.activeMQReporting = null;

			// if everything ‘s fine, report success
			new WebServiceResult(queueName, ticketID, ReportLevel.SUCCESS).send();

		} catch (Exception exce) {
			// report any errors
			new WebServiceResult(queueName, ticketID, ReportLevel.FATAL, exce.getMessage()).send();
		}
	}

	/**
	 * This method is used to get the queue name upon initialisation.
	 *
	 * @return the queue name
	 */
	public String getQueueName() {
		return queueName;
	}

	/**
	 * The parent object which is there to check for new messages and to trigger the method onMessage() is saved
	 * inside the class, to have it lately for shutting down the service again.
	 *
	 * @param checker the MessageConsumer object responsible for checking messages
	 */

	public void saveChecker(MessageConsumer checker) {
		this.checker = checker;
	}

	/**
	 * This method is used to get back the message checking object upon shutdown.
	 *
	 * @return the MessageConsumer object responsible for checking messages
	 */
	public MessageConsumer getChecker() {
		return checker;
	}
}
