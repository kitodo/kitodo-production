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

import org.kitodo.data.interfaces.DataFactoryInterface;

/**
 * A factory object for data transfer objects.
 */
public class DTOFactory implements DataFactoryInterface {

    private static final DTOFactory instance = new DTOFactory();

    /**
     * Returns the instance of the factory object. The factory object implements
     * the {@link DataFactoryInterface}. Since interfaces cannot have static
     * methods, it must be accessed here via an instance of the class, even
     * though the class has no state (no fields).
     * 
     * @return the factory instance
     */
    public static DTOFactory instance() {
        return instance;
    }

    @Override
    public BatchInterface newBatch() {
        return new BatchDTO();
    }

    @Override
    public ClientInterface newClient() {
        return new ClientDTO();
    }

    @Override
    public DocketInterface newDocket() {
        return new DocketDTO();
    }

    @Override
    public FilterInterface newFilter() {
        return new FilterDTO();
    }

    @Override
    public ProcessInterface newProcess() {
        return new ProcessDTO();
    }

    @Override
    public ProjectInterface newProject() {
        return new ProjectDTO();
    }

    @Override
    public PropertyInterface newProperty() {
        return new PropertyDTO();
    }

    @Override
    public RulesetInterface newRuleset() {
        return new RulesetDTO();
    }

    @Override
    public TaskInterface newTask() {
        return new TaskDTO();
    }

    @Override
    public TemplateInterface newTemplate() {
        return new TemplateDTO();
    }

    @Override
    public UserInterface newUser() {
        return new UserDTO();
    }

    @Override
    public WorkflowInterface newWorkflow() {
        return new WorkflowDTO();
    }
}
