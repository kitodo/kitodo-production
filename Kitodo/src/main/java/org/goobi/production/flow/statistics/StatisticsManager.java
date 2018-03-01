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

package org.goobi.production.flow.statistics;

import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.statistik.StatistikLaufzeitSchritte;
import de.sub.goobi.statistik.StatistikStatus;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.ResultOutput;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultValueDataset;

/**
 * The Class StatisticsManager organizes all statistical questions by choosing
 * the right implementation depening on {@link StatisticsMode}. For old
 * statistical question there will be generated jfreechart-datasets
 * 
 * @author Steffen Hankiewicz
 * @version 20.05.2009
 */
public class StatisticsManager implements Serializable {

    private static final long serialVersionUID = -1070332559779545423L;
    private static final Logger logger = LogManager.getLogger(StatisticsManager.class);
    /* simple JFreeChart Dataset for the old simple statistics */
    private Dataset jfreeDataset;
    /* internal StatisticsMode */
    private StatisticsMode statisticMode;
    private List dataSource;
    private Date sourceDateFrom;
    private Date sourceDateTo = new Date();
    private int sourceNumberOfTimeUnits;
    private TimeUnit sourceTimeUnit;
    private TimeUnit targetTimeUnit;
    private ResultOutput targetResultOutput;
    private CalculationUnit targetCalculationUnit;
    private boolean showAverage;
    private Date calculatedStartDate = new Date();
    private Date calculatedEndDate = new Date();
    private List<StatisticsRenderingElement> renderingElements;
    private static Locale myLocale = null;
    private Boolean includeLoops = false;

    private StatisticsManager() {
        super();
    }

    /**
     * Public constructor for StatisticsManager.
     *
     * @param inMode
     *            as {@link StatisticsMode}
     * @param dataSource
     *            as {@link List}
     * @param locale
     *            as {@link Locale}
     */
    public StatisticsManager(StatisticsMode inMode, List dataSource, Locale locale) {
        this();
        statisticMode = inMode;
        this.dataSource = dataSource;
        targetResultOutput = ResultOutput.chartAndTable;
        targetTimeUnit = TimeUnit.months;
        sourceTimeUnit = TimeUnit.months;
        myLocale = locale;

        /* for backward compatibility create old jfreechart datasets */
        if (inMode.getIsSimple()) {
            switch (inMode) {

                case SIMPLE_RUNTIME_STEPS:
                    jfreeDataset = StatistikLaufzeitSchritte.getDiagramm(dataSource);
                    break;

                default:
                    jfreeDataset = StatistikStatus.getDiagramm(dataSource);
                    break;
            }
        }
        if (myLocale == null) {
            myLocale = new Locale("de");
        }
    }

    /**
     * Retrieve the jfreechart Dataset for old statistical questions.
     *
     * @return {@link Dataset} for charting
     */
    public Dataset getJfreeDataset() {
        if (statisticMode.getIsSimple()) {
            return jfreeDataset;
        } else {
            return new DefaultValueDataset();
        }
    }

    /**
     * Retrieve current {@link StatisticsMode}.
     *
     * @return {@link StatisticsMode} for current statistical question
     */
    public StatisticsMode getStatisticMode() {
        return statisticMode;
    }

    /**
     * Calculate statistics and retrieve List off {@link DataRow} from
     * {@link StatisticsMode} for presentation and rendering.
     *
     */
    public void calculate() {
        /*
         * if to-date is before from-date, show error message
         */
        if (sourceDateFrom != null && sourceDateTo != null && sourceDateFrom.after(sourceDateTo)) {
            Helper.setMeldung("myStatisticButton", "selectedDatesNotValide", "selectedDatesNotValide");
            return;
        }

        /*
         * some debugging here
         */
        if (logger.isDebugEnabled()) {
            logger.debug(sourceDateFrom + " - " + sourceDateTo + " - " + sourceNumberOfTimeUnits + " - "
                    + sourceTimeUnit + "\n" + targetTimeUnit + " - " + targetCalculationUnit + " - "
                    + targetResultOutput + " - " + showAverage);
        }

        /*
         * calculate the statistical results and save it as List of DataTables (because
         * some statistical questions allow multiple tables and charts)
         */
        IStatisticalQuestion question = statisticMode.getStatisticalQuestion();
        try {
            setTimeFrameToStatisticalQuestion(question);

            if (targetTimeUnit != null) {
                question.setTimeUnit(targetTimeUnit);
            }
            if (targetCalculationUnit != null) {
                question.setCalculationUnit(targetCalculationUnit);
            }
            renderingElements = new ArrayList<>();
            List<DataTable> myDataTables = question.getDataTables(dataSource);

            // if DataTables exist analyze them
            if (myDataTables != null) {

                // localize time frame for gui
                StringBuilder subname = new StringBuilder();
                if (calculatedStartDate != null) {
                    subname.append(DateFormat.getDateInstance(DateFormat.SHORT, myLocale).format(calculatedStartDate));
                }
                subname.append(" - ");
                if (calculatedEndDate != null) {
                    subname.append(DateFormat.getDateInstance(DateFormat.SHORT, myLocale).format(calculatedEndDate));
                }
                if (calculatedStartDate == null && calculatedEndDate == null) {
                    subname = new StringBuilder();
                }

                // run through all DataTables
                for (DataTable dt : myDataTables) {
                    dt.setSubname(subname.toString());
                    StatisticsRenderingElement sre = new StatisticsRenderingElement(dt, question);
                    sre.createRenderer(showAverage);
                    renderingElements.add(sre);
                }
            }
        } catch (UnsupportedOperationException e) {
            Helper.setFehlerMeldung("StatisticButton", "errorOccuredWhileCalculatingStatistics", e);
        }
    }

    /**
     * Depending on selected Dates oder time range, set the dates for the
     * statistical question here.
     *
     * @param question
     *            the {@link IStatisticalQuestion} where the dates should be set, if
     *            it is an implementation of
     *            {@link IStatisticalQuestionLimitedTimeframe}
     */
    @SuppressWarnings("incomplete-switch")
    private void setTimeFrameToStatisticalQuestion(IStatisticalQuestion question) {
        /* only add a date, if correct interface is implemented here */
        if (question instanceof IStatisticalQuestionLimitedTimeframe) {
            /* if a timeunit is selected calculate the dates */
            Calendar cl = Calendar.getInstance();

            if (sourceNumberOfTimeUnits > 0) {

                cl.setFirstDayOfWeek(Calendar.MONDAY);
                cl.setTime(new Date());

                switch (sourceTimeUnit) {
                    case days:
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.DATE, -1);
                        break;

                    case weeks:
                        cl.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.DATE, -7);
                        break;

                    case months:
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.MONTH, -1);
                        break;

                    case quarters:
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        setCalendarToMidnight(cl);
                        while ((cl.get(Calendar.MONTH)) % 3 > 0) {
                            cl.add(Calendar.MONTH, -1);
                        }
                        cl.add(Calendar.MILLISECOND, -1);
                        calculatedEndDate = cl.getTime();
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.MONTH, -3);
                        break;

                    case years:
                        cl.set(Calendar.DAY_OF_YEAR, 1);
                        calculatedEndDate = calulateStartDateForTimeFrame(cl);
                        calculatedStartDate = calculateEndDateForTimeFrame(cl, Calendar.YEAR, -1);
                        break;
                }

            } else {
                /* take start and end date */
                cl.setTime(sourceDateTo);
                cl.add(Calendar.DATE, 1);
                setCalendarToMidnight(cl);
                cl.add(Calendar.MILLISECOND, -1);
                calculatedStartDate = sourceDateFrom;
                calculatedEndDate = cl.getTime();
            }
            ((IStatisticalQuestionLimitedTimeframe) question).setTimeFrame(calculatedStartDate, calculatedEndDate);
        } else {
            calculatedStartDate = null;
            calculatedEndDate = null;
        }
    }

    private void setCalendarToMidnight(Calendar cl) {
        cl.set(Calendar.HOUR_OF_DAY, 0);
        cl.set(Calendar.MINUTE, 0);
        cl.set(Calendar.SECOND, 0);
        cl.set(Calendar.MILLISECOND, 0);
    }

    private Date calculateEndDateForTimeFrame(Calendar cl, int calenderUnit, int faktor) {
        cl.add(Calendar.MILLISECOND, 1);
        cl.add(calenderUnit, sourceNumberOfTimeUnits * faktor);
        return cl.getTime();
    }

    private Date calulateStartDateForTimeFrame(Calendar cl) {
        setCalendarToMidnight(cl);
        cl.add(Calendar.MILLISECOND, -1);
        return cl.getTime();
    }

    /**
     * Get all {@link TimeUnit} from enum.
     *
     * @return all timeUnits
     */
    public List<TimeUnit> getAllTimeUnits() {
        return Arrays.asList(TimeUnit.values());
    }

    /**
     * Get all {@link CalculationUnit} from enum.
     *
     * @return all calculationUnit
     */
    public List<CalculationUnit> getAllCalculationUnits() {
        return Arrays.asList(CalculationUnit.values());
    }

    /**
     * Get all {@link ResultOutput} from enum.
     *
     * @return all resultOutput
     */
    public List<ResultOutput> getAllResultOutputs() {
        return Arrays.asList(ResultOutput.values());
    }

    /**
     * Get source date from.
     * 
     * @return the sourceDateFrom
     */
    public Date getSourceDateFrom() {
        return sourceDateFrom;
    }

    /**
     * Set source date from.
     * 
     * @param sourceDateFrom
     *            the sourceDateFrom to set
     */
    public void setSourceDateFrom(Date sourceDateFrom) {
        this.sourceDateFrom = sourceDateFrom;
    }

    /**
     * Get source date to.
     * 
     * @return the sourceDateTo
     */
    public Date getSourceDateTo() {
        return sourceDateTo;
    }

    /**
     * Set source date to.
     * 
     * @param sourceDateTo
     *            the sourceDateTo to set
     */
    public void setSourceDateTo(Date sourceDateTo) {
        this.sourceDateTo = sourceDateTo;
    }

    /**
     * Get source number if time units as String.
     * 
     * @return the sourceNumberOfTimeUnitsAsString
     */
    public String getSourceNumberOfTimeUnitsAsString() {
        if (sourceNumberOfTimeUnits == 0) {
            return "";
        } else {
            return String.valueOf(sourceNumberOfTimeUnits);
        }
    }

    /**
     * Set source number if time units as String.
     * 
     * @param inUnits
     *            the sourceNumberOfTimeUnits to set given as String to show an
     *            empty text field in gui
     */
    public void setSourceNumberOfTimeUnitsAsString(String inUnits) {
        if (StringUtils.isNotBlank(inUnits) && StringUtils.isNumericSpace(inUnits)) {
            sourceNumberOfTimeUnits = Integer.parseInt(inUnits);
        } else {
            sourceNumberOfTimeUnits = 0;
        }
    }

    /**
     * Get source time unit.
     * 
     * @return the sourceTimeUnit
     */
    public TimeUnit getSourceTimeUnit() {
        return sourceTimeUnit;
    }

    /**
     * Set source time unit.
     * 
     * @param sourceTimeUnit
     *            the sourceTimeUnit to set
     */
    public void setSourceTimeUnit(TimeUnit sourceTimeUnit) {
        this.sourceTimeUnit = sourceTimeUnit;
    }

    /**
     * Get target time unit.
     * 
     * @return the targetTimeUnit
     */
    public TimeUnit getTargetTimeUnit() {
        return targetTimeUnit;
    }

    /**
     * Set target time unit.
     * 
     * @param targetTimeUnit
     *            the targetTimeUnit to set
     */
    public void setTargetTimeUnit(TimeUnit targetTimeUnit) {
        this.targetTimeUnit = targetTimeUnit;
    }

    /**
     * Get target result output.
     * 
     * @return the targetResultOutput
     */
    public ResultOutput getTargetResultOutput() {
        return targetResultOutput;
    }

    /**
     * Set target result output.
     * 
     * @param targetResultOutput
     *            the targetResultOutput to set
     */
    public void setTargetResultOutput(ResultOutput targetResultOutput) {
        this.targetResultOutput = targetResultOutput;
    }

    /**
     * Get target calculation unit.
     * 
     * @return the targetCalculationUnit
     */
    public CalculationUnit getTargetCalculationUnit() {
        return targetCalculationUnit;
    }

    /**
     * Set target calculation unit.
     * 
     * @param targetCalculationUnit
     *            the targetCalculationUnit to set
     */
    public void setTargetCalculationUnit(CalculationUnit targetCalculationUnit) {
        this.targetCalculationUnit = targetCalculationUnit;
    }

    /**
     * Check if is show average.
     * 
     * @return the showAverage
     */
    public boolean isShowAverage() {
        return showAverage;
    }

    /**
     * Set ShowAverage.
     * 
     * @param showAverage
     *            the showAverage to set
     */
    public void setShowAverage(boolean showAverage) {
        this.showAverage = showAverage;
    }

    /**
     * isIncludeLoops.
     * 
     * @return includeLoops flag
     */
    public boolean isIncludeLoops() {
        if (includeLoops == null) {
            includeLoops = false;
        }
        return includeLoops;
    }

    public boolean isRenderLoopOption() {
        return statisticMode.isRenderIncludeLoops();
    }

    /**
     * Set value of includeLoops.
     * 
     * @param includeLoops
     *            as boolean
     */
    public void setIncludeLoops(boolean includeLoops) {
        this.includeLoops = includeLoops;
    }

    /**
     * get List of RenderingElements.
     *
     * @return List of {@link StatisticsRenderingElement} of calculated results
     */
    public List<StatisticsRenderingElement> getRenderingElements() {
        return renderingElements;
    }

    /**
     * Get locale.
     * 
     * @return Locale
     */
    public static Locale getLocale() {
        if (myLocale != null) {
            return myLocale;
        } else {
            return new Locale("de");
        }
    }
}
