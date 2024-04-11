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

package org.kitodo.data.interfaces;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

/**
 * An interface to manage digitization projects.
 */
public interface ProjectInterface extends DataInterface {

    /**
     * Returns the name of the project.
     *
     * @return the name of the project
     */
    String getTitle();

    /**
     * Sets the name of the project.
     *
     * @param title
     *            the name of the project
     */
    void setTitle(String title);

    /**
     * Returns the start time of the project. The value is a {@link Date} (a
     * specific instant in time, with millisecond precision). It is a freely
     * configurable value, not the date the project object was created in the
     * database. This can be, for example, the start of the funding period. The
     * string is formatted according to
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @return the start time
     */
    String getStartDate();

    /**
     * Sets the start time of the project. The string must be parsable with
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param startDate
     *            the start time
     * @throws ParseException
     *             if the time cannot be converted
     */
    void setStartDate(String startDate);

    /**
     * Returns the project end time. The value is a {@link Date} (a specific
     * instant in time, with millisecond precision). This is a freely
     * configurable value, regardless of when the project was last actually
     * worked on. For example, this can be the time at which the project must be
     * completed in order to be billed. The timing can be used to monitor that
     * the project is on time.
     * 
     * <p>
     * The string is formatted according to
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @return the end time
     */
    String getEndDate();

    /**
     * Sets the project end time. The string must be parsable with
     * {@link SimpleDateFormat}{@code ("yyyy-MM-dd HH:mm:ss")}.
     *
     * @param endDate
     *            the end time
     * @throws ParseException
     *             if the time cannot be converted
     */
    void setEndDate(String endDate);

    /**
     * Returned the file format for exporting the project's business objects. In
     * earlier times, the business objects were converted into the target format
     * using native Java code. The desired target format was returned here.
     * Today the export is done using XSLT, which is not covered by this.
     *
     * @return always empty
     * @deprecated Today the export is done using XSLT, which is configured
     *             elsewhere.
     */
    @Deprecated
    default String getFileFormatDmsExport() {
        return "";
    }

    /**
     * Formerly to set the file format for exporting business objects.
     *
     * @param fileFormatDmsExport
     *            unused
     * @deprecated Functionless today.
     */
    @Deprecated
    default void setFileFormatDmsExport(String fileFormatDmsExport) {
    }

    /**
     * Returned the file format used to store business objects internally. In
     * earlier times, several more bad than good file formats were used to store
     * the business objects internally.
     *
     * @return always empty
     * @deprecated The less suitable formats are no longer supported since
     *             version 3.
     */
    default String getFileFormatInternal() {
        return "";
    }

    /**
     * Set the file format used to store business objects internally.
     *
     * @param fileFormatInternal
     *            unused
     * @deprecated Functionless today.
     */
    void setFileFormatInternal(String fileFormatInternal);

    /**
     * Returns the name of the copyright holder of the business objects. These
     * are often the digitizing institution and also the sponsor.
     *
     * @return metsRightsOwner the name of the copyright holder
     */
    String getMetsRightsOwner();

    /**
     * Sets the name of the copyright owner of the business objects. The
     * effective maximum length of this VARCHAR field is subject to the maximum
     * row size of 64k shared by all columns, and the charset.
     *
     * @param metsRightsOwner
     *            the name of the copyright holder
     */
    void setMetsRightsOwner(String metsRightsOwner);

    /**
     * Returns the number of media objects to produce. This is a freely
     * configurable value, not a determined statistic. The value can be used to
     * monitor whether the project is on time.
     *
     * @return the number of media objects to produce
     */
    Integer getNumberOfPages();

    /**
     * Sets the number of media objects to produce. This is usually roughly
     * determined as part of project planning and can be stored here as a
     * reminder.
     *
     * @param numberOfPages
     *            the number of media objects to produce
     */
    void setNumberOfPages(Integer numberOfPages);

    /**
     * Returns the number of physical archival items to be digitized. This is a
     * freely configurable value, not a collected statistical value. The value
     * can be used to monitor whether the project is on time.
     *
     * @return number of volumes as Integer
     */
    Integer getNumberOfVolumes();

    /**
     * Sets the number of physical archival materials to be digitized. This is
     * usually roughly determined as part of project planning and can be stored
     * here as a reminder.
     *
     * @param numberOfVolumes
     *            as Integer
     */
    void setNumberOfVolumes(Integer numberOfVolumes);

    /**
     * Returns whether the project is active. Completed projects are typically
     * not hard deleted, but are simply marked as completed. This means that
     * even in the event of complaints, the old stocks can still be accessed and
     * corrected.
     *
     * @return whether the project is active
     */
    Boolean isActive();

    /**
     * Sets whether the project is active. Deactivated projects are hidden from
     * general operation.
     *
     * @param active
     *            whether project is active
     */
    void setActive(boolean active);

    /**
     * Returns the client running this project.
     *
     * @return the client
     */
    ClientInterface getClient();

    /**
     * Specifies the tenant that is executing this project.
     *
     * @param client
     *            the client
     */
    void setClient(ClientInterface client);

    /**
     * Returns the non-deactivated production templates associated with the
     * project.
     *
     * @return the active production templates
     */
    List<? extends TemplateInterface> getActiveTemplates();

    /**
     * Sets the active production templates associated with the project. This
     * list is not guaranteed to be in reliable order.
     *
     * @param templates
     *            the active production templates
     */
    void setActiveTemplates(List<? extends TemplateInterface> templates);

    /**
     * Returns the users contributing to this project.
     *
     * @return the users contributing to this project
     */
    List<? extends UserInterface> getUsers();

    /**
     * Specifies the users who will contribute to this project.
     *
     * @param users
     *            the users contributing to this project
     */
    void setUsers(List<? extends UserInterface> users);

    /**
     * Returns whether processes exist in the project. A project that contains
     * processes cannot be deleted.
     *
     * @return whether processes exist in the project
     */
    boolean hasProcesses();

    /**
     * Set whether project has processes. The setter can be used when
     * representing data from a third-party source. Internally it depends on,
     * whether there are process objects in the database for a project. Setting
     * this to true cannot insert processes into the database.
     *
     * @param hasProcesses
     *            as boolean
     * @throws UnsupportedOperationException
     *             when trying to set this to true for a project without
     *             processes
     */
    void setHasProcesses(boolean hasProcesses);

}
