//package org.goobi.production.search.lucene;
//
///**
// * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
// * 
// * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
// * 
// * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
// * 
// * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
// * Temple Place, Suite 330, Boston, MA 02111-1307 USA
// * 
// * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
// * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
// * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
// * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
// * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
// * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
// * exception statement from your version.
// */
//import org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter;
//import org.hibernate.Criteria;
//import org.hibernate.criterion.Restrictions;
//
///**
// * This implementation extends the hibernate userdefined filter in order to use
// * the methods used there in order to filter out user assigned steps
// * 
// * all what needs to happen seems at this point to be shielding the filter
// * string from the super class so where the filter string applies it can be send
// * to the lucene search function instead of using the old hibernate stuff
// * 
// * @author Wulf
// * 
// * 
// *         //TODO make sure that all what needs to be overwritten is overwritten
// */
//
//@Deprecated
//public class LuceneStepFilter extends UserDefinedStepFilter {
//
//	private static final long serialVersionUID = -1554720835061645707L;
//
//	String filter = null;
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter#
//	 * getCriteria()
//	 */
//	@Override
//	public Criteria getCriteria() {
//
//		return super.getCriteria().add(Restrictions.in("id", LuceneSearch.getSearchEngine().getSearchResults(filter)));
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.goobi.production.flow.statistics.hibernate.UserDefinedStepFilter#
//	 * setFilter(java.lang.String)
//	 */
//	@Override
//	public void setFilter(String newFilter) {
//		super.setFilter("");
//		filter = newFilter;
//
//	}
//
//}
