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
    public BatchDTO newBatch() {
        return new BatchDTO();
    }

    @Override
    public ClientDTO newClient() {
        return new ClientDTO();
    }

    @Override
    public DocketDTO newDocket() {
        return new DocketDTO();
    }

    @Override
    public FilterDTO newFilter() {
        return new FilterDTO();
    }

    @Override
    public ProcessDTO newProcess() {
        return new ProcessDTO();
    }

    @Override
    public ProjectDTO newProject() {
        return new ProjectDTO();
    }

    @Override
    public PropertyDTO newProperty() {
        return new PropertyDTO();
    }

    @Override
    public RulesetDTO newRuleset() {
        return new RulesetDTO();
    }

    @Override
    public TaskDTO newTask() {
        return new TaskDTO();
    }

    @Override
    public TemplateDTO newTemplate() {
        return new TemplateDTO();
    }

    @Override
    public UserDTO newUser() {
        return new UserDTO();
    }

    @Override
    public WorkflowDTO newWorkflow() {
        return new WorkflowDTO();
    }
}
