package org.goobi.production.flow.jobs;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
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
import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.Import.GoobiHotfolder;
import org.goobi.production.cli.CommandLineInterface;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Persistence.BatchDAO;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.exceptions.DAOException;

/**
 * 
 * @author Robert Sehr
 * 
 */
public class HotfolderJob extends AbstractGoobiJob {
	private static final Logger logger = Logger.getLogger(HotfolderJob.class);

	// private int templateId = 944;

	// public HotfolderJob(int templateId) {
	// this.templateId = templateId;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.jobs.SimpleGoobiJob#initialize()
	 */
	@Override
	public String getJobName() {
		return "HotfolderJob";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.jobs.SimpleGoobiJob#execute()
	 */
	@Override
	public void execute() {
		// logger.error("TEST123");
		if (ConfigMain.getBooleanParameter("runHotfolder", false)) {
			logger.trace("1");
			List<GoobiHotfolder> hotlist = GoobiHotfolder.getInstances();
			logger.trace("2");
			for (GoobiHotfolder hot : hotlist) {
				logger.trace("3");
				// GoobiHotfolder hot = new GoobiHotfolder(new
				// File(ConfigMain.getParameter("GoobiHotfolder",
				// "/opt/digiverso/goobi/hotfolder")));
				// templateId = ConfigMain.getIntParameter("GoobiHotfolderId",
				// 944);
				List<File> list = hot.getCurrentFiles();
				logger.trace("4");
				long size = getSize(list);
				logger.trace("5");
				try {
					if (size > 0) {
						logger.trace("6");
						Thread.sleep(10000);
						logger.trace("7");
						list = hot.getCurrentFiles();
						logger.trace("8");
						if (size == getSize(list)) {
							logger.trace("9");
							ProzessDAO dao = new ProzessDAO();
							Prozess template = dao.get(hot.getTemplate());
							dao.refresh(template);
							logger.trace("10");
							List<String> metsfiles = hot.getFileNamesByFilter(GoobiHotfolder.filter);
							logger.trace("11");
							HashMap<String, Integer> failedData = new HashMap<String, Integer>();
							logger.trace("12");
							
							
							
							Batch batch = new Batch();	
							
							
							for (String filename : metsfiles) {
								logger.debug("found file: " + filename);
								logger.trace("13");

								int returnValue = CommandLineInterface.generateProcess(filename, template, hot.getFolderAsFile(),
										hot.getCollection(), hot.getUpdateStrategy(), batch);
								logger.trace("14");
								if (returnValue != 0) {
									logger.trace("15");
									failedData.put(filename, returnValue);
									logger.trace("16");
								} else {
									logger.debug("finished file: " + filename);
								}
							}
							if (!failedData.isEmpty()) {
								// // TODO Errorhandling
								logger.trace("17");
								for (String filename : failedData.keySet()) {

									logger.error("error while importing file: " + filename + " with error code " + failedData.get(filename));
								}
							}
							new BatchDAO().save(batch); 
						} else {
							logger.trace("18");
							return;
						}
						logger.trace("19");
					}
				} catch (InterruptedException e) {
					logger.error(e);
					logger.trace("20");
				} catch (DAOException e) {
					logger.error(e);
					logger.trace("21");
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

	private long getSize(List<File> list) {
		long size = 0;
		for (File f : list) {
			if (f.isDirectory()) {
				File[] subdir = f.listFiles();
				for (File sub : subdir) {
					size += sub.length();
				}
			} else {
				size += f.length();
			}
		}
		return size;
	}
}
