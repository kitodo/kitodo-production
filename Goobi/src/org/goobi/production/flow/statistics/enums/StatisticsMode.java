package org.goobi.production.flow.statistics.enums;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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

import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.hibernate.StatQuestCorrections;
import org.goobi.production.flow.statistics.hibernate.StatQuestProduction;
import org.goobi.production.flow.statistics.hibernate.StatQuestProjectAssociations;
import org.goobi.production.flow.statistics.hibernate.StatQuestStorage;
import org.goobi.production.flow.statistics.hibernate.StatQuestThroughput;
import org.goobi.production.flow.statistics.hibernate.StatQuestUsergroups;
import org.goobi.production.flow.statistics.hibernate.StatQuestVolumeStatus;

import de.sub.goobi.helper.Helper;

/**
 * Enum for all statistic modes,
 * 
 * for backward compatibility we will contain old datasets of previous chartings
 * 
 * @author Steffen Hankiewicz
 * @author Wulf Riebensahm
 * @version 20.10.2009
 ****************************************************************************/

public enum StatisticsMode {

	

	SIMPLE_RUNTIME_STEPS("runtimeOfSteps", null, false, true, false), 
	PROJECTS("projectAssociation", StatQuestProjectAssociations.class, false,false, false), 
	STATUS_VOLUMES("statusOfVolumes", StatQuestVolumeStatus.class, false, false, false), 
	USERGROUPS("statusForUsers",StatQuestUsergroups.class, false, false, false),
	// the following statistcs are the new statistics from june 2009
	THROUGHPUT("productionThroughput", StatQuestThroughput.class, true, false, true), 
	CORRECTIONS("errorTracking", StatQuestCorrections.class, false,	false, true), 
	STORAGE("storageCalculator", StatQuestStorage.class, false, false, true), 
	PRODUCTION("productionStatistics",	StatQuestProduction.class, false, false, true);

	private IStatisticalQuestion question;
	private String title;
	private Boolean renderIncludeLoops;
	private Boolean isSimpleStatistic;
	private Boolean restrictDate;

	/**
	 * private constructor,
	 ****************************************************************************/
	private StatisticsMode(String inTitle, Class<? extends IStatisticalQuestion> inQuestion, Boolean renderIncludeLoops, Boolean isSimpleStatistic,
			Boolean restrictDate) {
		title = inTitle;
		if (inQuestion != null) {
			try {
				question = inQuestion.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		this.renderIncludeLoops = renderIncludeLoops;
		this.isSimpleStatistic = isSimpleStatistic;
		this.restrictDate = restrictDate;
	}

	/**
	 * return boolean, if it is an old simple jfreechart statistic
	 * 
	 * @return if it is as simple old statistic
	 ****************************************************************************/
	public Boolean getRestrictedDate() {
		return restrictDate;
	}

	/**
	 * return boolean, if it is an old simple jfreechart statistic
	 * 
	 * @return if it is as simple old statistic
	 ****************************************************************************/
	public Boolean getIsSimple() {
		return isSimpleStatistic;
	}

	/**
	 * return localized title of statistic view from standard-jsf-messages-files
	 * 
	 * @return title of statistic question mode
	 ****************************************************************************/
	public String getTitle() {
		return Helper.getTranslation(title);
	}

	/**
	 * return our implementation initialized
	 * 
	 * @return the implemented {@link IStatisticalQuestion}
	 ****************************************************************************/
	public IStatisticalQuestion getStatisticalQuestion() {
		return question;
	}

	/**
	 * return StatisticsMode by given {@link IStatisticalQuestion}-Class
	 * 
	 * @return {@link StatisticsMode}
	 ****************************************************************************/
	public static StatisticsMode getByClassName(Class<? extends IStatisticalQuestion> inQuestion) {
		for (StatisticsMode sm : values()) {
			if (sm.getStatisticalQuestion() != null && sm.getStatisticalQuestion().getClass().getName().equals(inQuestion.getName())) {
				return sm;
			}
		}
		return PRODUCTION;

	}

	public Boolean isRenderIncludeLoops() {
		return this.renderIncludeLoops;
	}

	public String getMode() {
		return this.title;
	}

}
