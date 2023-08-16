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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.exceptions.Guard;
import org.kitodo.production.helper.Helper;

/**
 * The class EmptyTask is the base class for worker threads that operate
 * independently to do the work in the background. The name empty task points
 * out that the task doesn’t do anything sensible yet. It is here to be
 * extended.
 */
public class EmptyTask extends Thread {

    private static final Logger logger = LogManager.getLogger(EmptyTask.class);

    /**
     * The enum Actions lists the available instructions to the housekeeper what
     * to do with a terminated thread. These are:
     *
     * <dl>
     * <dt>{@code DELETE_IMMEDIATELY}</dt>
     * <dd>The thread shall be disposed of as soon as is has gracefully stopped.
     * </dd>
     * <dt>{@code KEEP_FOR_A_WHILE}</dt>
     * <dd>The default behavior: A thread that terminated either normally or
     * abnormally is kept around in memory for a while and then removed
     * automatically. Numeric and temporary limits can be configured.</dd>
     * <dt>{@code PREPARE_FOR_RESTART}</dt>
     * <dd>If the thread was interrupted by a user, replace it by a new one,
     * passing in the state of the old one to be able to continue work.</dd>
     * </dl>
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
    public static final Thread.UncaughtExceptionHandler CATCH_ALL = (origin, exception) -> {
        if (origin instanceof EmptyTask) {
            EmptyTask task = (EmptyTask) origin;
            task.setException(exception);
        }
    };

    /**
     * The constant DEFAULT_BEHAVIOUR defines the default behavior of the
     * TaskKeeper towards a task that terminated. The default behavior is that
     * it will be kept in the front end as configured in the global
     * configuration file and then will be deleted.
     */
    private static final Behaviour DEFAULT_BEHAVIOUR = Behaviour.KEEP_FOR_A_WHILE;

    /**
     * The field behavior defines the behavior of the TaskKeeper towards the
     * task if it has terminated. Setting this field to DELETE_IMMEDIATELY will
     * also result in the desired behavior if the task has not yet been started
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
     * Returns the instruction how the TaskSitter
     * shall behave towards this task. Usually, the behavior isn’t set while
     * the task is under normal execution. It can be set by calling
     * {@link #interrupt(Behaviour)}. It may also be set this way if the task is
     * still new and wasn’t even started. The following instructions are
     * available:
     *
     * <dl>
     * <dt>{@code DELETE_IMMEDIATELY}</dt>
     * <dd>The thread shall be disposed of as soon as is has gracefully stopped.
     * </dd>
     * <dt>{@code KEEP_FOR_A_WHILE}</dt>
     * <dd>The default behavior: A thread that terminated either normally or
     * abnormally is kept around in memory for a while and then removed
     * automatically. Numeric and temporary limits can be configured.</dd>
     * <dt>{@code PREPARE_FOR_RESTART}</dt>
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
     */
    public String getDisplayName() {
        StringBuilder title = new StringBuilder(getClass().getSimpleName());
        title.setCharAt(0, (char) (title.charAt(0) | 32));
        return Helper.getTranslation(title.toString());
    }

    /**
     * Returns the duration the task is dead. If
     * a time of death has not yet been recorded, null is returned.
     *
     * @return the duration since the task died
     */
    Duration getDurationDead() {
        if (Objects.isNull(passedAway)) {
            return null;
        }
        long elapsed = System.nanoTime() - passedAway;
        return Duration.of(elapsed, ChronoUnit.NANOS);
    }

    /**
     * Provides access to the exception that
     * occurred if the thread died abnormally. If no exception has occurred yet
     * or it wasn’t properly recorded, null is returned.
     *
     * @return the exception occurred, or null if no exception occurred yet
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Returns the progress of the task in percent,
     * i.e. in a range from 0 to 100.
     *
     * @return the progress of the task
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Returns a text string representing the
     * state of the current task as read-only property "stateDescription".
     *
     * @return a string representing the state of the task
     */
    public String getStateDescription() {
        TaskState state = getTaskState();
        String label = Helper.getTranslation(state.toString().toLowerCase());
        switch (state) {
            case WORKING:
                if (Objects.nonNull(detail)) {
                    return label + " (" + detail + ")";
                } else {
                    return label;
                }
            case CRASHED:
                Throwable rootCause = exception;
                while (Objects.nonNull(rootCause.getCause())) {
                    rootCause = rootCause.getCause();
                }
                StringBuilder stateDescription = new StringBuilder(255);
                stateDescription.append(label);
                stateDescription.append(" (");
                if (Objects.nonNull(detail)) {
                    stateDescription.append(detail);
                    stateDescription.append(": ");
                }
                stateDescription.append(rootCause.getClass().getSimpleName());
                if (Objects.nonNull(rootCause.getLocalizedMessage())) {
                    stateDescription.append(": ");
                    stateDescription.append(rootCause.getLocalizedMessage());
                }
                stateDescription.append(')');
                return stateDescription.toString();
            default:
                return label;
        }
    }

    /**
     * Returns the task state. It can be one of
     * the following:
     *
     * <dl>
     * <dt>{@code CRASHED}</dt>
     * <dd>The thread has terminated abnormally. The field “exception” is
     * holding the exception that has occurred.</dd>
     * <dt>{@code FINISHED}</dt>
     * <dd>The thread has finished its work without errors and is available for
     * clean-up.</dd>
     * <dt>{@code NEW}</dt>
     * <dd>The thread has not yet been started.</dd>
     * <dt>{@code STOPPED}</dt>
     * <dd>The thread was stopped by a front end user—resulting in a call to its
     * {@link #interrupt(Behaviour)} method with {@link Behaviour}
     * .PREPARE_FOR_RESTART— and is able to restart after cloning and replacing
     * it.</dd>
     * <dt>{@code STOPPING}</dt>
     * <dd>The thread has received a request to interrupt but did not stop
     * yet.</dd>
     * <dt>{@code WORKING}</dt>
     * <dd>The thread is in operation.</dd>
     * </dl>
     *
     * @return the task state
     */
    public TaskState getTaskState() {
        switch (getState()) {
            case NEW:
                return TaskState.NEW;
            case TERMINATED:
                if (Objects.isNull(behaviour)) {
                    behaviour = DEFAULT_BEHAVIOUR;
                }
                if (Objects.nonNull(exception)) {
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
     * Returns the read-only field "longMessage"
     * which will be shown in a pop-up window.
     *
     * @return the stack trace of the exception, if any
     */
    public String getLongMessage() {
        if (Objects.isNull(exception)) {
            return null;
        }
        return ExceptionUtils.getStackTrace(exception);
    }

    /**
     * Interrupts this thread and allows to set a
     * behavior after interruption.
     *
     * @param mode
     *            how to behave after interruption
     */
    public void interrupt(Behaviour mode) {
        behaviour = mode;
        interrupt();
    }

    /**
     * Returns wether the start button shall be shown
     * as read-only property "startable". A thread can be started as long as it
     * has not yet been started.
     *
     * @return whether the start button shall show
     */
    public boolean isStartable() {
        return getState().equals(State.NEW);
    }

    /**
     * Returns wether the stop button shall be shown
     * as read-only property "stopable". A thread can be stopped if it is
     * working.
     *
     * @return whether the stop button shall show
     */
    public boolean isStoppable() {
        return getTaskState().equals(TaskState.WORKING);
    }

    /**
     * Returns whether the delete button shall be
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
        logger.error(exception.getLocalizedMessage(), exception);
        if (Objects.isNull(this.exception) && (!isInterrupted() || !(exception instanceof InterruptedException))) {
            if (exception instanceof Exception) {
                this.exception = (Exception) exception;
            } else {
                this.exception = new ExecutionException(exception.getMessage(), exception);
            }
        }
    }

    /**
     * May be used to set the task’s name along
     * with a detail that doesn’t require translation and is helpful when being
     * shown in the front end (such as the name of the entity the task is based
     * on). The name detail should be set once (in the constructor). You may
     * pass in null to reset the name and remove the detail.
     *
     * <p>
     * I.e., if your task is about creation of OCR for a process, the detail
     * here could be the process title.
     *
     * @param detail
     *            a name detail, may be null
     */
    protected void setNameDetail(String detail) {
        StringBuilder composer = new StringBuilder(119);
        composer.append(this.getDisplayName());
        if (Objects.nonNull(detail)) {
            composer.append(": ");
            composer.append(detail);
        }
        super.setName(composer.toString());
    }

    /**
     * May be used to set the task’s progress in
     * percent (i.e., from 0 to 100).
     *
     * @param progress
     *            the tasks progress
     */
    public void setProgress(int progress) {
        Guard.isInRange("progress", progress, 0, 100);
        this.progress = progress;
    }

    /**
     * May be used to set the task’s progress in
     * percent (i.e., from 0 to 100).
     *
     * @param statusProgress
     *            the tasks progress
     */
    public void setProgress(double statusProgress) {
        setProgress((int) Math.ceil(statusProgress));
    }

    /**
     * Sets the time of death of the task now.
     */
    void setTimeOfDeath() {
        passedAway = System.nanoTime();
    }

    /**
     * May be used to set some detail information
     * that don’t require translation and are helpful when being shown in the
     * front end (such as the name of the entity that is currently being
     * processed by the task). The name detail should be set every time the
     * progress is determined. You may pass in null to remove the detail.
     *
     * <p>
     * I.e., if your task is about creation of OCR for a process, the detail
     * here could be the image file being processed right now.
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
