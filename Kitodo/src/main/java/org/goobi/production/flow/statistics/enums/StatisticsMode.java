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

package org.goobi.production.flow.statistics.enums;

import de.sub.goobi.helper.Helper;

import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.hibernate.StatQuestCorrections;
import org.goobi.production.flow.statistics.hibernate.StatQuestProduction;
import org.goobi.production.flow.statistics.hibernate.StatQuestProjectAssociations;
import org.goobi.production.flow.statistics.hibernate.StatQuestStorage;
import org.goobi.production.flow.statistics.hibernate.StatQuestUsergroups;
import org.goobi.production.flow.statistics.hibernate.StatQuestVolumeStatus;

/**
 * Enum for all statistic modes, for backward compatibility we will contain old
 * datasets of previous chartings.
 *
 * @author Steffen Hankiewicz
 * @author Wulf Riebensahm
 * @version 20.10.2009
 */
public enum StatisticsMode {

    SIMPLE_RUNTIME_STEPS("runtimeOfSteps", null, false, true, false),
    PROJECTS("projectAssociation", StatQuestProjectAssociations.class, false, false, false),
    STATUS_VOLUMES("statusOfVolumes", StatQuestVolumeStatus.class, false, false, false),
    USERGROUPS("statusForUsers", StatQuestUsergroups.class, false, false, false),
    CORRECTIONS("errorTracking", StatQuestCorrections.class, false, false, true),
    STORAGE("storageCalculator", StatQuestStorage.class, false, false, true),
    PRODUCTION("productionStatistics", StatQuestProduction.class, false, false, true);

    private IStatisticalQuestion question;
    private String title;
    private Boolean renderIncludeLoops;
    private Boolean isSimpleStatistic;
    private Boolean restrictDate;

    /**
     * private constructor.
     */
    StatisticsMode(String inTitle, Class<? extends IStatisticalQuestion> inQuestion, Boolean renderIncludeLoops,
            Boolean isSimpleStatistic, Boolean restrictDate) {
        title = inTitle;
        if (inQuestion != null) {
            try {
                question = inQuestion.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.renderIncludeLoops = renderIncludeLoops;
        this.isSimpleStatistic = isSimpleStatistic;
        this.restrictDate = restrictDate;
    }

    /**
     * return boolean, if it is an old simple jfreechart statistic.
     *
     * @return if it is as simple old statistic
     **/
    public Boolean getRestrictedDate() {
        return restrictDate;
    }

    /**
     * return boolean, if it is an old simple jfreechart statistic.
     *
     * @return if it is as simple old statistic
     */
    public Boolean getIsSimple() {
        return isSimpleStatistic;
    }

    /**
     * return localized title of statistic view from
     * standard-jsf-messages-files.
     *
     * @return title of statistic question mode
     */
    public String getTitle() {
        return Helper.getTranslation(title);
    }

    /**
     * return our implementation initialized.
     *
     * @return the implemented {@link IStatisticalQuestion}
     */
    public IStatisticalQuestion getStatisticalQuestion() {
        return question;
    }

    /**
     * return StatisticsMode by given {@link IStatisticalQuestion}-Class.
     *
     * @return {@link StatisticsMode}
     */
    public static StatisticsMode getByClassName(Class<? extends IStatisticalQuestion> inQuestion) {
        for (StatisticsMode sm : values()) {
            if (sm.getStatisticalQuestion() != null
                    && sm.getStatisticalQuestion().getClass().getName().equals(inQuestion.getName())) {
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
