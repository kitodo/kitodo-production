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
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.goobi.production.search.lucene.LuceneIndex;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.GenericJDBCException;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

/**
 * 
 * @author Robert Sehr
 * 
 */
public class LuceneIndexJobIDVersion extends AbstractGoobiJob {
	private static final Logger logger = Logger.getLogger(LuceneIndexJobIDVersion.class);

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
	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);

		ProjectionList proj = Projections.projectionList();
		proj.add(Projections.id());
		crit = crit.setProjection(proj);

		List<Object> list = crit.list();

		LuceneIndex li = LuceneIndex.initialize();

		try {
			LuceneIndex.openLuceneIndex(true);
		} catch (CorruptIndexException e) {
			Helper.setFehlerMeldung("index is corrupt", e);
			logger.error("index is corrupt", e);
		} catch (LockObtainFailedException e) {
			Helper.setFehlerMeldung("index is write-locked", e);
			logger.error("index is write-locked", e);
		} catch (IOException e) {
			Helper.setFehlerMeldung("no index folder found", e);
			logger.error("no index folder found", e);
		}

		ProzessDAO store = new ProzessDAO();

		for (Object obj : list) {
			try {
				Prozess p = store.get((Integer) obj);
				li.addObject(p);
				session.evict(p);

				session.clear();

				// TODO: Remove this calls to Helper
			} catch (GenericJDBCException e) {
				Helper.setFehlerMeldung("unknown jdbc Exception", e);
				logger.error("unknown jdbc Exception", e);
			} catch (DAOException e) {
				Helper.setFehlerMeldung("unknown dao Exception", e);
				logger.error("unknown dao Exception", e);
			}
		}

		try {
			LuceneIndex.closeIndex();
		} catch (CorruptIndexException e) {
			Helper.setFehlerMeldung("index is corrupt", e);
			logger.error("index is corrupt", e);
		} catch (IOException e) {
			Helper.setFehlerMeldung("no index folder found", e);
			logger.error("no index folder found", e);
		}
	}
}
