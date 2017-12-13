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

import de.sub.goobi.helper.enums.ReportLevel;
import javax.jms.MapMessage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class WebServiceResult {
    private static final Logger logger = LogManager.getLogger(WebServiceResult.class);

    private String queueName;
    private String id;
    private ReportLevel level;
    private String message = null;

    /**
     * Constructor.
     *
     * @param queueName
     *            String
     * @param id
     *            String
     * @param level
     *            String
     * @param message
     *            String
     */
    public WebServiceResult(String queueName, String id, ReportLevel level, String message) {
        this.queueName = queueName;
        this.id = id;
        this.level = level;
        this.message = message;
    }

    /**
     * Constructor.
     *
     * @param queueName
     *            String
     * @param id
     *            String
     * @param level
     *            ReportLevel object
     */
    public WebServiceResult(String queueName, String id, ReportLevel level) {
        this.queueName = queueName;
        this.id = id;
        this.level = level;
    }

    /**
     * Send.
     */
    public void send() {
        if (ActiveMQDirector.getResultsTopic() == null) {

            // If reporting to ActiveMQ is disabled, write log message
            logger.log(level == ReportLevel.SUCCESS ? Level.INFO : Level.WARN,
                "Processing message \"" + id + '@' + queueName + "\" reports " + level.toLowerCase() + "."
                        + (message != null ? " (" + message + ")" : ""));
        } else {
            try {
                MapMessage report = ActiveMQDirector.getSession().createMapMessage();

                DateTime now = new DateTime();
                report.setString("timestamp", ISODateTimeFormat.dateTime().print(now));
                report.setString("queue", queueName);
                report.setString("id", id);
                report.setString("level", level.toLowerCase());
                if (message != null) {
                    report.setString("message", message);
                }

                ActiveMQDirector.getResultsTopic().send(report);

            } catch (Exception exce) {
                logger.fatal("Error sending report  for \"" + id + '@' + queueName + "\" (" + level.toLowerCase()
                        + (message != null ? ": " + message : "") + "): Giving up.",
                    exce);
            }
        }
    }
}
