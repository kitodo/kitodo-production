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

/**
 * Backing bean for the title record link tab.
 */
package org.kitodo.production.forms.copyprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

public class TitleRecordLinkTab {
    private static final Logger logger = LogManager.getLogger(TitleRecordLinkTab.class);

    /**
     * Maximum number of search hits to show.
     */
    private static final int MAXIMUM_NUMBER_OF_HITS = 10;

    /**
     * Process creation dialog to which this tab belongs.
     */
    private final ProzesskopieForm copyProcessForm;

    /**
     * The user-selected parent process.
     */
    private String chosenParentProcess = null;

    /**
     * Specifies whether an indication of further hits is visible.
     */
    private boolean indicationOfMoreHitsVisible = false;

    /**
     * Elements from which a parent process can be selected.
     */
    private List<SelectItem> possibleParentProcesses = Collections.emptyList();

    /**
     * The user’s search for parent processes.
     */
    private String searchQuery = "";

    /**
     * Process selected as parent.
     */
    private Process titleRecordProcess = null;

    /**
     * Creates a new data object underlying the title record link tab.
     *
     * @param copyProcessForm
     *            process copy form containing the object
     */
    public TitleRecordLinkTab(ProzesskopieForm copyProcessForm) {
        this.copyProcessForm = copyProcessForm;
    }

    /**
     * Selects a parent process and builds the tree with the root element of the
     * selected process and the possible insertion positions.
     */
    public void chooseParentProcess() {
        try {
            titleRecordProcess = ServiceManager.getProcessService().getById(Integer.valueOf(chosenParentProcess));
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne",
                new Object[] {chosenParentProcess,
                              possibleParentProcesses.parallelStream()
                                      .filter(selectItem -> selectItem.getValue().equals(chosenParentProcess)).findAny()
                                      .orElse(new SelectItem(null, null)).getLabel() },
                logger, e);
        }
    }

    /**
     * Returns the HTML identifier of the selected parent process.
     *
     * @return the identifier of the selected parent process
     */
    public String getChosenParentProcess() {
        return chosenParentProcess;
    }

    /**
     * Sets the HTML identifier of the selected parent process.
     *
     * @param chosenParentProcess
     *            identifier to set
     */
    public void setChosenParentProcess(String chosenParentProcess) {
        this.chosenParentProcess = chosenParentProcess;
    }

    /**
     * Search for possible parent processes and fill with the result the hit
     * selection. If there are more than the maximum number of hits defined as a
     * constant above, the corresponding message is displayed.
     */
    public void searchForParentProcesses() {
        if (searchQuery.trim().isEmpty()) {
            Helper.setMessage("prozesskopieForm.titleRecordLinkTab.searchButtonClick.empty");
            return;
        }
        try {
            List<ProcessDTO> processes = ServiceManager.getProcessService().findLinkableParentProcesses(searchQuery,
                copyProcessForm.getProject().getId(), copyProcessForm.getTemplate().getRuleset().getId());
            if (processes.isEmpty()) {
                Helper.setMessage("prozesskopieForm.titleRecordLinkTab.searchButtonClick.noHits");
            }
            indicationOfMoreHitsVisible = processes.size() > MAXIMUM_NUMBER_OF_HITS;
            possibleParentProcesses = new ArrayList<>();
            for (ProcessDTO process : processes.subList(0, Math.min(processes.size(), MAXIMUM_NUMBER_OF_HITS))) {
                possibleParentProcesses.add(new SelectItem(process.getId().toString(), process.getTitle()));
            }
        } catch (DataException e) {
            Helper.setErrorMessage("prozesskopieForm.titleRecordLinkTab.searchButtonClick.error", e.getMessage(),
                logger, e);
            indicationOfMoreHitsVisible = false;
            possibleParentProcesses = Collections.emptyList();
        }
    }

    /**
     * Returns the search query typed by the user.
     *
     * @return the search query
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Sets the search query typed by the user.
     *
     * @param searchQuery
     *            search query to set
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * Returns whether the hint that there were more hits is visible.
     *
     * @return whether the hint is visible
     */
    public boolean isIndicationOfMoreHitsVisible() {
        return indicationOfMoreHitsVisible;
    }

    /**
     * Returns the list of selectors for selecting a parent process.
     *
     * @return the list of selectors
     */
    public List<SelectItem> getPossibleParentProcesses() {
        return possibleParentProcesses;
    }

    /**
     * Returns the process of the selected title record.
     *
     * @return the process of the selected title record
     */
    public Process getTitleRecordProcess() {
        return titleRecordProcess;
    }
}
