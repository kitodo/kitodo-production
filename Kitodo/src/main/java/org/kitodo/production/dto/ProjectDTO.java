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

package org.kitodo.production.dto;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.kitodo.data.interfaces.ClientInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.TemplateInterface;
import org.kitodo.data.interfaces.UserInterface;

/**
 * Project DTO object.
 */
public class ProjectDTO extends BaseDTO implements ProjectInterface {

    private String title;
    private String startDate;
    private String endDate;
    private String fileFormatDmsExport;
    private String fileFormatInternal;
    private String metsRightsOwner = "";
    private Integer numberOfPages;
    private Integer numberOfVolumes;
    private Boolean active = true;
    private ClientInterface client;
    private List<? extends TemplateInterface> templates = new ArrayList<>();
    private List<? extends UserInterface> users = new ArrayList<>();
    private boolean hasProcesses;

    /**
     * Get title.
     *
     * @return title as String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get start date.
     *
     * @return start date as String
     */
    public String getStartTime() {
        return startDate;
    }

    /**
     * Set start date.
     *
     * @param startDate
     *            as String
     */
    public void setStartTime(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Get end date.
     *
     * @return end date as String
     */
    public String getEndTime() {
        return endDate;
    }

    /**
     * Set end date.
     *
     * @param endDate
     *            as String
     */
    public void setEndTime(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Get DMS export file format.
     *
     * @return DMS export file format as String
     */
    public String getFileFormatDmsExport() {
        return this.fileFormatDmsExport;
    }

    /**
     * Set DMS export file format.
     *
     * @param fileFormatDmsExport
     *            as String
     */
    public void setFileFormatDmsExport(String fileFormatDmsExport) {
        this.fileFormatDmsExport = fileFormatDmsExport;
    }

    /**
     * Get internal file format.
     *
     * @return internal file format as String
     */
    public String getFileFormatInternal() {
        return this.fileFormatInternal;
    }

    /**
     * Set internal file format.
     *
     * @param fileFormatInternal
     *            as String
     */
    public void setFileFormatInternal(String fileFormatInternal) {
        this.fileFormatInternal = fileFormatInternal;
    }

    /**
     * Get mets rights owner.
     *
     * @return metsRightsOwner as String
     */
    public String getMetsRightsOwner() {
        return metsRightsOwner;
    }

    /**
     * Set mets right owner.
     *
     * @param metsRightsOwner
     *            as String
     */
    public void setMetsRightsOwner(String metsRightsOwner) {
        this.metsRightsOwner = metsRightsOwner;
    }

    /**
     * Get number of pages.
     *
     * @return number of pages as Integer
     */
    public Integer getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Set number of pages.
     *
     * @param numberOfPages
     *            as Integer
     */
    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    /**
     * Get number of volumes.
     *
     * @return number of volumes as Integer
     */
    public Integer getNumberOfVolumes() {
        return numberOfVolumes;
    }

    /**
     * Set number of volumes.
     *
     * @param numberOfVolumes
     *            as Integer
     */
    public void setNumberOfVolumes(Integer numberOfVolumes) {
        this.numberOfVolumes = numberOfVolumes;
    }

    /**
     * Get if project is active.
     *
     * @return whether project is active or not
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Set if project is active.
     *
     * @param active
     *            whether project is active or not
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the client.
     *
     * @return The client.
     */
    public ClientInterface getClient() {
        return client;
    }

    /**
     * Sets the client.
     *
     * @param client The client.
     */
    public void setClient(ClientInterface client) {
        this.client = client;
    }

    /**
     * Get list of active templates.
     *
     * @return list of active templates as TemplateDTO
     */
    public List<? extends TemplateInterface> getActiveTemplates() {
        return templates;
    }

    /**
     * Set list of templates.
     *
     * @param templates
     *            as list of TemplateDTO
     */
    public void setActiveTemplates(List<? extends TemplateInterface> templates) {
        this.templates = templates;
    }

    /**
     * Get list of users.
     *
     * @return list of users as UserDTO
     */
    public List<? extends UserInterface> getUsers() {
        return users;
    }

    /**
     * Set list of users.
     *
     * @param users
     *            as list of UserDTO
     */
    public void setUsers(List<? extends UserInterface> users) {
        this.users = users;
    }

    /**
     * Get whether project has processes.
     *
     * @return value of hasProcesses
     */
    public boolean hasProcesses() {
        return hasProcesses;
    }

    /**
     * Set whether project has processes.
     *
     * @param hasProcesses as boolean
     */
    public void setHasProcesses(boolean hasProcesses) {
        this.hasProcesses = hasProcesses;
    }

    @Override
    public Date getStartDate() {
        try {
            return Strings.isNotEmpty(this.startDate) ? DATE_FORMAT.parse(this.startDate) : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = Objects.nonNull(startDate) ? DATE_FORMAT.format(startDate) : null;
    }

    @Override
    public Date getEndDate() {
        try {
            return Strings.isNotEmpty(this.endDate) ? DATE_FORMAT.parse(this.endDate) : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = Objects.nonNull(endDate) ? DATE_FORMAT.format(endDate) : null;
       
    }
}
