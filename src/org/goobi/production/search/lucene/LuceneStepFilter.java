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

package org.goobi.production.search.lucene;
import org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/** This implementation extends the hibernate userdefined filter in order to use the 
 *  methods used there in order to filter out user assigned steps
 *  
 *   all what needs to happen seems at this point to be shielding the filter 
 *   string from the super class so where the filter string applies it can
 *   be send to the lucene search function instead of useing the old hibernate
 *   stuff 
 * 
 * @author Wulf
 *
 *
 *	//TODO make sure that all what needs to be overwritten is overwritten
 */

public class LuceneStepFilter extends UserDefinedStepFilter {

	
	private static final long serialVersionUID = -1554720835061645707L;

	String filter = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter#getCriteria()
	 */
	public Criteria getCriteria(){
	
		return super.getCriteria().add(Restrictions.in("id", LuceneSearch.getSearchEngine()
				.getSearchResults(this.filter)));
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter#setFilter(java.lang.String)
	 */
	public void setFilter(String newFilter){
		super.setFilter("");
		this.filter = newFilter;
		
	}	

}
