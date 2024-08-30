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

/**
 * Factory interface for the objects of the interface. An instance of the
 * interface can be used to obtain objects of the interface. This allows new
 * objects of the interface to be obtained without being aware of the
 * implementation.
 */
public interface DataFactoryInterface {

    /**
     * Returns a new batch. The batch has not yet been persisted and does not
     * yet have a database ID.
     * 
     * @return a new batch
     */
    Batch newBatch();

    /**
     * Returns a new client. The client has not yet been persisted and does not
     * yet have a database ID.
     * 
     * @return a new client
     */
    Client newClient();

    /**
     * Returns a new docket generation statement. The client has not yet been
     * persisted and does not yet have a database ID.
     * 
     * @return a new docket generation
     */
    Docket newDocket();

    /**
     * Returns a new store for a search string. The filter string has not yet
     * been persisted and does not yet have a database ID.
     * 
     * @return a new search string
     */
    Filter newFilter();

    /**
     * Returns a new process. The process has not yet been persisted and does
     * not yet have a database ID.
     * 
     * @return a new process
     */
    Process newProcess();

    /**
     * Returns a new project. The project has not yet been persisted and does
     * not yet have a database ID.
     * 
     * @return a new project
     */
    Project newProject();

    /**
     * Returns a new container for a property key-value pair. The property has
     * not yet been persisted and does not yet have a database ID.
     * 
     * @return a new property
     */
    Property newProperty();

    /**
     * Returns a new storage for the business domain specification. The ruleset
     * has not yet been persisted and does not yet have a database ID.
     * 
     * @return a new business specification
     */
    Ruleset newRuleset();

    /**
     * Returns a new task. The task has not yet been persisted and does not yet
     * have a database ID.
     * 
     * @return a new task
     */
    Task newTask();

    /**
     * Returns a new production template. The production template has not yet
     * been persisted and does not yet have a database ID.
     * 
     * @return a new template
     */
    Template newTemplate();

    /**
     * Returns a new user. The user has not yet been persisted and does not yet
     * have a database ID.
     * 
     * @return a new user
     */
    User newUser();

    /**
     * Returns a new workflow. Returns a new workflow reference. The workflow
     * has not yet been persisted in the database and does not yet have a record
     * number.
     * 
     * @return a new batch
     */
    Workflow newWorkflow();
}
