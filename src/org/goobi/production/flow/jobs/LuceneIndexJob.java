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

package org.goobi.production.flow.jobs;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.goobi.production.search.lucene.LuceneIndex;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;


/**
 * 
 * @author Robert Sehr
 *
 */
public class LuceneIndexJob extends AbstractGoobiJob {
	private static final Logger logger = Logger.getLogger(LuceneIndexJob.class);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.jobs.SimpleGoobiJob#initialize()
	 */
	@Override
	public String getJobName() {
		return "LuceneIndexJob";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.jobs.SimpleGoobiJob#execute()
	 */
	@Override
	public void execute() {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		ScrollableResults allProcesses = crit.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
		LuceneIndex li = LuceneIndex.initialize();
		int count = 0;
		try {
			LuceneIndex.openLuceneIndex(true);
			while (allProcesses.next()) {
				Prozess p = (Prozess) allProcesses.get(0);
				li.addObject(p);
				count++;
				session.evict(p);
				if (count % 20 == 0) {
					// flush a batch of updates and release memory:
					session.flush();
					session.clear();				}
			}
			LuceneIndex.closeIndex();
		} catch (CorruptIndexException e) {
			Helper.setFehlerMeldung("index is coorupt", e);
			logger.error("index is coorupt", e);
		} catch (LockObtainFailedException e) {
			Helper.setFehlerMeldung("index is write-locked", e);
			logger.error("index is write-locked", e);
			logger.error(e);
		} catch (IOException e) {
			Helper.setFehlerMeldung("no index folder found", e);
			logger.error("no index folder found", e);
		}
		

	}
}
