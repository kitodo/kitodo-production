package org.goobi.production.cli.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2012, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Prozess;

public class WikiFieldHelper {

	private static final Logger logger = Logger.getLogger(WikiFieldHelper.class);

	
	private static final String TAG_ERROR = "<font color=\"#FF0000\">";
	private static final String TAG_WARN = "<font color=\"#FF6600\">";
	private static final String TAG_INFO = "<font color=\"#0033CC\">";
	private static final String TAG_DEBUG = "<font color=\"#CCCCCC\">";
	private static final String TAG_USER = "<font color=\"#006600\">";
	private static final String ENDTAG = "</font>";

	private static final String BREAK = "<br/>";

	public static String getWikiMessage(Prozess p, String currentWikifieldcontent, String type, String value) {
		String message = "";
		if (currentWikifieldcontent != null && currentWikifieldcontent.length() > 0) {
			message += currentWikifieldcontent;
			message += BREAK;
		}

		if (type.equals("error")) {
			message += TAG_ERROR;
		} else if (type.equals("debug")) {
			message += TAG_DEBUG;
		} else if (type.equals("user")) {
			message += TAG_USER;
		} else if (type.equals("warn")) {
			message += TAG_WARN;
		}else {
			message += TAG_INFO;
		}  

		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date());
		String processname = "";
		if (p != null) {
			processname = "processname: " + p.getTitel() + ", message: ";
		}
		logger.info(timestamp + " " + processname + " " + value );
		message = message + timestamp + ": " + value + ENDTAG;
		return message;
	}
}
