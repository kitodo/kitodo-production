/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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

package org.goobi.mq;

import javax.jms.MapMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import de.sub.goobi.helper.enums.ReportLevel;

public class WebServiceResult {
	private static final Logger logger = Logger.getLogger(ActiveMQDirector.class);
	
	private String queueName;
	private String id;
	private ReportLevel level;
	private String message = null;
	
	public WebServiceResult(String queueName, String id, ReportLevel level,
			String message){
		this.queueName = queueName;
		this.id = id;
		this.level = level;
		this.message = message;
	}
	
	public WebServiceResult(String queueName, String id, ReportLevel level){
		this.queueName = queueName;
		this.id = id;
		this.level = level;
	}
	
	public void send() {
		if (ActiveMQDirector.getResultsTopic() == null) {

			// If reporting to ActiveMQ is disabled, write log message
			logger.log(level == ReportLevel.SUCCESS ? Level.INFO : Level.WARN,
					"Processing message \"" + id + '@' + queueName
							+ "\" reports " + level.toLowerCase() + "."
							+ (message != null ? " (" + message + ")" : ""));
		} else {
			try {
				MapMessage report = ActiveMQDirector.getSession().createMapMessage();

				DateTime now = new DateTime();
				DateTimeFormatter iso8601formatter = ISODateTimeFormat.dateTime();
				report.setString("timestamp", iso8601formatter.print(now));
				report.setString("queue", queueName);
				report.setString("id", id);
				report.setString("level", level.toLowerCase());
				if (message != null)
					report.setString("message", message);

				ActiveMQDirector.getResultsTopic().send(report);

			} catch (Exception exce) {
				logger.fatal("Error sending report  for \"" + id + '@'
						+ queueName + "\" (" + level.toLowerCase()
						+ (message != null ? ": " + message : "")
						+ "): Giving up.", exce);
			}
		}
	}
}
