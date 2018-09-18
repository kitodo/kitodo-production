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

package org.goobi.mq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ReportLevel;
import org.kitodo.helper.Helper;

/**
 * The class ActiveMQProcessor offers general services, such as making the
 * incoming messages available as MapMessages and publishing the results. When I
 * came clear that this code would be necessary for every processor, I thought
 * an abstract class would be the right place for it. ActiveMQProcessor also
 * provides a place to save the checker for the ActiveMQDirector, to be able to
 * shut it down later.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class ActiveMQProcessor implements MessageListener {

    private String queueName; // the queue name will be available here
    private MessageConsumer checker;

    /**
     * Implement the method process() to let your service actually do what you
     * want him to do.
     *
     * @param ticket
     *            A MapMessage which can be processor-specific except that it
     *            requires to have a field “id”.
     */
    protected abstract void process(MapMessageObjectReader ticket)
            throws DAOException, DataException, IOException, JMSException, PreferencesException, ReadException, WriteException;

    /**
     * Instantiating the class ActiveMQProcessor always requires to pass the
     * name of the queue it should be attached to. That means, your constructor
     * should look like this:
     * 
     * <pre>
     * public MyServiceProcessor() {
     *     super(ConfigCore.getParameter("activeMQ.myService.queue", null));
     * }
     * </pre>
     *
     * <p>
     * If the parameter is not set in kitodo_config.properties, it will return
     * “null” and so prevents it from being set up in ActiveMQDirector.
     * </p>
     *
     * @param queueName
     *            the queue name, if configured, or “null” to prevent the
     *            processor from being connected.
     */
    public ActiveMQProcessor(String queueName) {
        this.queueName = queueName;
    }

    /**
     * The method onMessage() provides a corset which checks the message being a
     * MapMessage, performs a type safe type cast, and extracts the job’s ID to
     * be able to send useful results to the results topic, which it does after
     * the abstract method process() has finished.
     *
     * <p>
     * Since this will be the same for all processors which use MapMessages, I
     * extracted the portion into the abstract class.
     * </p>
     *
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message arg) {
        MapMessageObjectReader ticket;
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
            Map<String, String> loggingConfig = new HashMap<>();
            loggingConfig.put("queueName", queueName);
            loggingConfig.put("id", ticketID);
            Helper.setActiveMQReporting(loggingConfig);

            // process ticket
            process(ticket);

            // turn off logging again
            Helper.setActiveMQReporting(null);

            // if everything ‘s fine, report success
            new WebServiceResult(queueName, ticketID, ReportLevel.SUCCESS).send();
        } catch (Exception e) {
            // report any errors
            new WebServiceResult(queueName, ticketID, ReportLevel.FATAL, e.getMessage()).send();
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
     * The parent object which is there to check for new messages and to trigger
     * the method onMessage() is saved inside the class, to have it lately for
     * shutting down the service again.
     *
     * @param checker
     *            the MessageConsumer object responsible for checking messages
     */

    public void saveChecker(MessageConsumer checker) {
        this.checker = checker;
    }

    /**
     * This method is used to get back the message checking object upon
     * shutdown.
     *
     * @return the MessageConsumer object responsible for checking messages
     */
    public MessageConsumer getChecker() {
        return checker;
    }
}
