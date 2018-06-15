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

package de.sub.goobi.helper.tasks;

import de.sub.goobi.helper.Helper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.Duration;

/**
 * The class EmptyTask is the base class for worker threads that operate
 * independently to do the work in the background. The name empty task points
 * out that the task doesn’t do anything sensible yet. It is here to be
 * extended.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class EmptyTask extends Thread implements INameableTask {
    /**
     * The enum Actions lists the available instructions to the housekeeper what
     * to do with a terminated thread. These are:
     *
     * <dl>
     * <dt><code>DELETE_IMMEDIATELY</code></dt>
     * <dd>The thread shall be disposed of as soon as is has gracefully stopped.
     * </dd>
     * <dt><code>KEEP_FOR_A_WHILE</code></dt>
     * <dd>The default behaviour: A thread that terminated either normally or
     * abnormally is kept around in memory for a while and then removed
     * automatically. Numeric and temporary limits can be configured.</dd>
     * <dt><code>PREPARE_FOR_RESTART</code></dt>
     * <dd>If the thread was interrupted by a user, replace it by a new one,
     * passing in the state of the old one to be able to continue work.</dd>
     * </dl>
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
    public enum Behaviour {
        DELETE_IMMEDIATELY,
        KEEP_FOR_A_WHILE,
        PREPARE_FOR_RESTART
    }

    /**
     * The constant CATCH_ALL holds an UncaughtExceptionHandler implementation
     * which will automatically be attached to all task threads. Otherwise
     * exceptions might get lost or even bring the runtime to crash.
     */
    public static final Thread.UncaughtExceptionHandler CATCH_ALL = new Thread.UncaughtExceptionHandler() {
        /**
         * The function uncaughtException() will catch any uncaught exception
         * that might occur in the task and will store it in the
         *
         * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
         *      java.lang.Throwable)
         */
        @Override
        public void uncaughtException(Thread origin, Throwable exception) {
            if (origin instanceof EmptyTask) {
                EmptyTask task = (EmptyTask) origin;
                task.setException(exception);
            }
        }
    };

    /**
     * The constant DEFAULT_BEHAVIOUR defines the default behaviour of the
     * TaskKeeper towards a task that terminated. The default behaviour is that
     * it will be kept in the front end as configured in the global
     * configuration file and then will be deleted.
     */
    private static final Behaviour DEFAULT_BEHAVIOUR = Behaviour.KEEP_FOR_A_WHILE;

    /**
     * The field behaviour defines the behaviour of the TaskKeeper towards the
     * task if it has terminated. Setting this field to DELETE_IMMEDIATELY will
     * also result in the desired behaviour if the task has not yet been started
     * at all.
     */
    private Behaviour behaviour;

    /**
     * The field detail holds a string giving some details about what the thread
     * is doing that do not require translation, i.e. which file is currently
     * processed.
     */
    private String detail = null;

    /**
     * The field exception is designated to take an exception if one occurred.
     */
    private Exception exception = null;

    /**
     * The field passedAway will be initialised with a time stamp as the thread
     * dies to be able to remove it a defined timespan after it died.
     */
    private Long passedAway = null;

    /**
     * The field progress holds one out of 101 values, ranging from 0 to 100 to
     * indicate the progress of the work. This will be shown as a progress bar
     * in the front end.
     */
    private int progress = 0;

    /**
     * Default constructor. Creates an empty thread.
     *
     * @param nameDetail
     *            a detail that is helpful when being shown, may be null
     */
    public EmptyTask(String nameDetail) {
        setDaemon(true);
        setNameDetail(nameDetail);
    }

    /**
     * Copy constructor. Required for cloning tasks. Cloning is required to be
     * able to restart a task.
     *
     * @param master
     *            instance to make a copy from
     */
    protected EmptyTask(EmptyTask master) {
        setDaemon(true);
        setName(master.getName());
        this.behaviour = master.behaviour;
        this.detail = master.detail;
        this.exception = master.exception;
        this.passedAway = master.passedAway;
        this.progress = master.progress;
    }

    /**
     * Calls the copy constructor to create a not-yet-executed replacement copy
     * of that thread object. Every subclass must provide its own copy
     * constructor—which must call super(objectToCopy)—and overload this method
     * to call its own copy constructor.
     *
     * @return a not-yet-executed replacement of this thread
     */
    public EmptyTask replace() {
        return new EmptyTask(this);
    }

    /**
     * The function getBehaviour() returns the instruction how the TaskSitter
     * shall behave towards this task. Usually, the behaviour isn’t set while
     * the task is under normal execution. It can be set by calling
     * {@link #interrupt(Behaviour)}. It may also be set this way if the task is
     * still new and wasn’t even started. The following instructions are
     * available:
     *
     * <dl>
     * <dt><code>DELETE_IMMEDIATELY</code></dt>
     * <dd>The thread shall be disposed of as soon as is has gracefully stopped.
     * </dd>
     * <dt><code>KEEP_FOR_A_WHILE</code></dt>
     * <dd>The default behaviour: A thread that terminated either normally or
     * abnormally is kept around in memory for a while and then removed
     * automatically. Numeric and temporary limits can be configured.</dd>
     * <dt><code>PREPARE_FOR_RESTART</code></dt>
     * <dd>If the thread was interrupted by a user, replace it by a new one,
     * passing in the state of the old one to be able to continue work.</dd>
     * </dl>
     *
     * @return how the TaskSitter shall behave towards this task
     */
    Behaviour getBehaviour() {
        return behaviour;
    }

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("EmptyTask");
    }

    /**
     * The function getDurationDead() returns the duration the task is dead. If
     * a time of death has not yet been recorded, null is returned.
     *
     * @return the duration since the task died
     */
    Duration getDurationDead() {
        if (passedAway == null) {
            return null;
        }
        long elapsed = System.nanoTime() - passedAway;
        return new Duration(TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
    }

    /**
     * The function getException() provides access to the exception that
     * occurred if the thread died abnormally. If no exception has occurred yet
     * or it wasn’t properly recorded, null is returned.
     *
     * @return the exception occurred, or null if no exception occurred yet
     */
    public Exception getException() {
        return exception;
    }

    /**
     * The function getProgress() returns the progress of the task in percent,
     * i.e. in a range from 0 to 100.
     *
     * @return the progress of the task
     */
    public int getProgress() {
        return progress;
    }

    /**
     * The function getStateDescription() returns a text string representing the
     * state of the current task as read-only property "stateDescription".
     *
     * @return a string representing the state of the task
     */
    public String getStateDescription() {
        TaskState state = getTaskState();
        String label = Helper.getTranslation(state.toString().toLowerCase());
        switch (state) {
            case WORKING:
                if (detail != null) {
                    return label + " (" + detail + ")";
                } else {
                    return label;
                }
            case CRASHED:
                if (exception.getMessage() != null) {
                    return label + " (" + exception.getMessage() + ")";
                } else if (detail != null) {
                    return label + " (" + detail + ")";
                } else {
                    return label + " (" + exception.getClass().getSimpleName() + ")";
                }
            default:
                return label;
        }
    }

    /**
     * The function getTaskState() returns the task state, which can be one of
     * the followings:
     *
     * <dl>
     * <dt><code>CRASHED</code></dt>
     * <dd>The thread has terminated abnormally. The field “exception” is
     * holding the exception that has occurred.</dd>
     * <dt><code>FINISHED</code></dt>
     * <dd>The thread has finished its work without errors and is available for
     * clean-up.</dd>
     * <dt><code>NEW</code></dt>
     * <dd>The thread has not yet been started.</dd>
     * <dt><code>STOPPED</code></dt>
     * <dd>The thread was stopped by a front end user—resulting in a call to its
     * {@link #interrupt(Behaviour)} method with {@link Behaviour}
     * .PREPARE_FOR_RESTART— and is able to restart after cloning and replacing
     * it.</dd>
     * <dt><code>STOPPING</code></dt>
     * <dd>The thread has received a request to interrupt but didn’t stop
     * yet.</dd>
     * <dt><code>WORKING</code></dt>
     * <dd>The thread is in operation.</dd>
     * </dl>
     *
     * @return the task state
     */
    TaskState getTaskState() {
        switch (getState()) {
            case NEW:
                return TaskState.NEW;
            case TERMINATED:
                if (behaviour == null) {
                    behaviour = DEFAULT_BEHAVIOUR;
                }
                if (exception != null) {
                    return TaskState.CRASHED;
                }
                if (Behaviour.PREPARE_FOR_RESTART.equals(behaviour)) {
                    return TaskState.STOPPED;
                } else {
                    return TaskState.FINISHED;
                }
            default:
                if (isInterrupted()) {
                    return TaskState.STOPPING;
                } else {
                    return TaskState.WORKING;
                }
        }
    }

    /**
     * The function getLongMessage() returns the read-only field "longMessage"
     * which will be shown in a pop-up window.
     *
     * @return the stack trace of the exception, if any
     */
    public String getLongMessage() {
        if (exception == null) {
            return null;
        }
        return ExceptionUtils.getStackTrace(exception);
    }

    /**
     * The function interrupt() interrupts this thread and allows to set a
     * behaviour after interruption.
     *
     * @param mode
     *            how to behave after interruption
     */
    public void interrupt(Behaviour mode) {
        behaviour = mode;
        interrupt();
    }

    /**
     * The function isStartable() returns wether the start button shall be shown
     * as read-only property "startable". A thread can be started as long as it
     * has not yet been started.
     *
     * @return whether the start button shall show
     */
    public boolean isStartable() {
        return getState().equals(State.NEW);
    }

    /**
     * The function isStopable() returns wether the stop button shall be shown
     * as read-only property "stopable". A thread can be stopped if it is
     * working.
     *
     * @return whether the stop button shall show
     */
    public boolean isStopable() {
        return getTaskState().equals(TaskState.WORKING);
    }

    /**
     * The function isDeleteable() returns whether the delete button shall be
     * shown as read-only property "deleteable". In our interpretation, a thread
     * is deleteable if it is either new or has terminated and is still lounging
     * around.
     *
     * @return whether the delete button shall show
     */
    public boolean isDeleteable() {
        switch (getState()) {
            case NEW:
            case TERMINATED:
                return !Behaviour.DELETE_IMMEDIATELY.equals(behaviour);
            default:
                return false;
        }
    }

    /**
     * This is a sample implementation of run() which simulates a “long running
     * task” but does nothing and just fills up the percentage gauge. It isn’t
     * useful for anything but testing or demonstration purposes.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        for (int i = progress + 1; i <= 100; i++) {

            // tell user some details what you are currently working on
            setWorkDetail(Integer.toString(i));

            // do something …
            try {
                sleep(1024);
            } catch (InterruptedException e) {
                this.interrupt();
            }

            // set progress
            setProgress(i);

            // The thread may have been signaled to stop. If so, leave
            if (isInterrupted()) {
                return;
            }
        }
        // We’re done. There is nothing more to do.
    }

    /**
     * The procedure setException can be used to save an exception that occurred
     * and show it in the front end. It will only record the first exception
     * (which is likely to be the source of all the misery) and it will not
     * record an InterruptedException if the thread has already been
     * interrupted.
     *
     * @param exception
     *            exception to save
     */
    public void setException(Throwable exception) {
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if (this.exception == null && (!isInterrupted() || !(exception instanceof InterruptedException))) {
            if (exception instanceof Exception) {
                this.exception = (Exception) exception;
            } else {
                this.exception = new ExecutionException(exception.getMessage(), exception);
            }
        }
    }

    /**
     * The procedure setNameDetail() may be used to set the task’s name along
     * with a detail that doesn’t require translation and is helpful when being
     * shown in the front end (such as the name of the entity the task is based
     * on). The name detail should be set once (in the constructor). You may
     * pass in null to reset the name and remove the detail.
     *
     * <p>
     * I.e., if your task is about creation of OCR for a process, the detail
     * here could be the process title.
     * </p>
     *
     * @param detail
     *            a name detail, may be null
     */
    protected void setNameDetail(String detail) {
        StringBuilder composer = new StringBuilder(119);
        composer.append(this.getDisplayName());
        if (detail != null) {
            composer.append(": ");
            composer.append(detail);
        }
        super.setName(composer.toString());
    }

    /**
     * The procedure setProgress() may be used to set the task’s progress in
     * percent (i.e., from 0 to 100).
     *
     * @param progress
     *            the tasks progress
     */
    public void setProgress(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress out of range: " + progress);
        }
        this.progress = progress;
    }

    /**
     * The procedure setProgress() may be used to set the task’s progress in
     * percent (i.e., from 0 to 100).
     *
     * @param statusProgress
     *            the tasks progress
     */
    protected void setProgress(double statusProgress) {
        setProgress((int) Math.ceil(statusProgress));
    }

    /**
     * Sets the time of death of the task now.
     */
    void setTimeOfDeath() {
        passedAway = System.nanoTime();
    }

    /**
     * The procedure setWorkDetail() may be used to set some detail information
     * that don’t require translation and are helpful when being shown in the
     * front end (such as the name of the entity that is currently being
     * processed by the task). The name detail should be set every time the
     * progress is determined. You may pass in null to remove the detail.
     *
     * <p>
     * I.e., if your task is about creation of OCR for a process, the detail
     * here could be the image file being processed right now.
     * </p>
     *
     * @param detail
     *            a work detail, may be null
     */
    public void setWorkDetail(String detail) {
        this.detail = detail;
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine will
     * create a standalone thread and call the run method of this class. The
     * result is that two threads are running concurrently: the current thread
     * which returns from the call to the start method, and the other thread
     * which executes its run method. In addition, this method override ensures
     * that the thread is properly registered in the task manager and that its
     * uncaught exception handler has been properly set.
     *
     * @see java.lang.Thread#start()
     */
    @Override
    public void start() {
        TaskManager.addTaskIfMissing(this);
        setUncaughtExceptionHandler(CATCH_ALL);
        super.start();
    }
}
