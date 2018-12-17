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

package org.kitodo.production.helper.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;

/**
 * Deprecated class.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @deprecated New task implementations should directly implement EmptyTask.
 */
@Deprecated
public abstract class LongRunningTask extends EmptyTask {
    /**
     * No-argument constructor. Creates an empty long running task. Must be made
     * explicit because a constructor taking an argument is present.
     *
     * @deprecated New task implementations should directly implement EmptyTask.
     */
    @Deprecated
    public LongRunningTask() {
        super((String) null);
    }

    /**
     * The clone constructor creates a new instance of this object. This is
     * necessary for Threads that have terminated in order to render to run them
     * again possible.
     *
     * @param master
     *            copy master to create a clone of
     */
    public LongRunningTask(LongRunningTask master) {
        super(master);
        initialize(master.process);
    }

    protected static final Logger logger = LogManager.getLogger(LongRunningTask.class);

    private Process process;
    private boolean isSingleThread = true;

    public void initialize(Process inputProcess) {
        this.process = inputProcess;
    }

    /**
     * The method setShowMessages() can be used to set a flag whether this long
     * running task is executing asynchronously or not, in the latter case it
     * shall show messages to the user using
     * {@link org.kitodo.production.helper.Helper#setMessage(String)}, otherwise not.
     *
     * @param show
     *            whether to show messages to the user
     */
    public void setShowMessages(boolean show) {
        isSingleThread = !show;
    }

    /**
     * Cancel method.
     *
     * @deprecated Replaced by {@link Thread#interrupt()}.
     */
    @Deprecated
    public void cancel() {
        this.interrupt();
    }

    /**
     * Calls the clone constructor to create a not yet executed instance of this
     * thread object. This is necessary for threads that have terminated in
     * order to render possible to restart them.
     *
     * @return a not-yet-executed replacement of this thread
     * @see org.kitodo.production.helper.tasks.EmptyTask#replace()
     */
    @Override
    public abstract EmptyTask replace();

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see org.kitodo.production.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public abstract String getDisplayName();

    /**
     * Process-Getter.
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Status des Tasks in Angabe von Prozent.
     *
     * @deprecated Replaced by {@link EmptyTask#getProgress()}.
     */
    @Deprecated
    public int getStatusProgress() {
        if (super.getException() != null) {
            return -1;
        }
        return super.getProgress();
    }

    /**
     * Meldung über den aktuellen Task.
     *
     * @deprecated Replaced by {@link EmptyTask#getTaskState()}.
     */
    @Deprecated
    public String getStatusMessage() {
        return super.getTaskState().toString().toLowerCase();
    }

    /**
     * Titel des aktuellen Task.
     *
     * @deprecated Replaced by {@link Thread#getName()}.
     */
    @Deprecated
    public String getTitle() {
        return super.getName();
    }

    /**
     * Setter für Fortschritt nur für vererbte Klassen.
     *
     * @deprecated Replaced by {@link EmptyTask#setProgress(int)}.
     */
    @Deprecated
    protected void setStatusProgress(int statusProgress) {
        super.setProgress(statusProgress);
    }

    /**
     * Set status progress.
     *
     * @deprecated Replaced by {@link EmptyTask#setProgress(double)}.
     */
    @Deprecated
    protected void setStatusProgress(double statusProgress) {
        super.setProgress(statusProgress);
    }

    /**
     * Setter für Statusmeldung nur für vererbte Klassen.
     *
     * @deprecated Replaced by {@link EmptyTask#setWorkDetail(String)}.
     */
    @Deprecated
    protected void setStatusMessage(String statusMessage) {
        super.setWorkDetail(statusMessage);
        if (!this.isSingleThread) {
            Helper.setMessage(statusMessage);
            logger.debug(statusMessage);
        }
    }

    /**
     * Setter für Titel nur für vererbte Klassen.
     *
     * @deprecated Replaced by {@link EmptyTask#EmptyTask(String)}.
     */
    @Deprecated
    protected void setTitle(String title) {
        super.setNameDetail(title);
    }

    /**
     * Setter für Prozess nur für vererbte Klassen.
     */
    protected void setProcess(Process process) {
        this.process = process;
        setNameDetail(process.getTitle());
    }

    /**
     * Set long message.
     *
     * @deprecated Replaced by {@link EmptyTask#setWorkDetail(String)}.
     */
    @Deprecated
    public void setLongMessage(String inlongMessage) {
        super.setWorkDetail(inlongMessage);
    }

}
