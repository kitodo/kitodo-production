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

package org.kitodo.production.interfaces.activemq;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ReportLevel;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The class ActiveMQProcessor offers general services, such as making the
 * incoming messages available as MapMessages and publishing the results. When I
 * came clear that this code would be necessary for every processor, I thought
 * an abstract class would be the right place for it. ActiveMQProcessor also
 * provides a place to save the checker for the ActiveMQDirector, to be able to
 * shut it down later.
 */
public abstract class ActiveMQProcessor implements MessageListener {
    /**
     * The name of the queue from which this processor is processing messages.
     */
    private String queueName;

    /**
     * The message consumer object that actually receives the messages and
     * executes {@link #process(MapMessageObjectReader)}.
     */
    private MessageConsumer messageConsumer;

    /**
     * Must be implemented to let the service do what it should do.
     *
     * @param ticket
     *            an object providing access to the fields of the received map
     *            message
     */
    protected abstract void process(MapMessageObjectReader ticket) throws DAOException, JMSException;

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
     *
     * @param queueName
     *            the queue name, if configured, or “null” to prevent the
     *            processor from being connected.
     */
    public ActiveMQProcessor(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Provides a corset which checks the message being a MapMessage, performs a
     * type safe type cast, and extracts the job’s ID to be able to send useful
     * results to the results topic, which it does after the abstract method
     * process() has finished.
     *
     * <p>
     * Since this will be the same for all processors which use MapMessages, I
     * extracted the portion into the abstract class.
     */
    @Override
    public void onMessage(Message arg) {
        MapMessageObjectReader message;
        String ticketID = null;

        try {
            // Basic check message
            if (arg instanceof MapMessage) {
                message = new MapMessageObjectReader((MapMessage) arg);
            } else {
                throw new IllegalArgumentException("Incompatible types.");
            }
            ticketID = message.getMandatoryString("id");

            // turn on logging
            Map<String, String> loggingConfig = new HashMap<>();
            loggingConfig.put("queueName", queueName);
            loggingConfig.put("id", ticketID);
            Helper.setActiveMQReporting(loggingConfig);

            // set default user
            Optional<String> optionalLogin = ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_USER);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            if (optionalLogin.isPresent()) {
                if (Objects.isNull(securityContext.getAuthentication())) {
                    User user = ServiceManager.getUserService().getByLogin(optionalLogin.get());
                    SecurityUserDetails securityUserDetails = new SecurityUserDetails(user);
                    Authentication auth = new UsernamePasswordAuthenticationToken(securityUserDetails, null,
                            securityUserDetails.getAuthorities());
                    Client clientId = ServiceManager.getClientService().getById(user.getClients().get(0).getId());
                    securityUserDetails.setSessionClient(clientId);
                    securityContext.setAuthentication(auth);
                } else {
                    optionalLogin = Optional.empty();
                }
            }

            // process message
            process(message);

            if (optionalLogin.isPresent()) {
                securityContext.setAuthentication(null);
            }

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
     * Returns the queue name. Maybe null if the processor is not active.
     *
     * @return the queue name
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Sets the message consumer to have it later for shutting down the service.
     *
     * @param messageConsumer
     *            the MessageConsumer object responsible for checking messages
     */

    public void setMessageConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    /**
     * Returns the message consumer. Maybe null. Used for shutdown.
     *
     * @return the message consumer
     */
    public MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }
}
