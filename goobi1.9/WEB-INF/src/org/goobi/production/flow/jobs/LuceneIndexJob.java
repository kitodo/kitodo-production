package org.goobi.production.flow.jobs;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
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

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.helper.Helper;


/**
 * 
 * @author Robert Sehr
 *
 */
@Deprecated
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
