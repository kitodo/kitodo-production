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

import java.util.ArrayList;
import java.util.List;


/**
 * User DTO object.
 */
public class UserDTO extends BaseDTO implements UserInterface {

    private String login;
    private String name;
    private String surname;
    private String fullName;
    private String location;
    private String ldapLogin;
    private boolean active = true;
    private List<? extends FilterInterface> filters = new ArrayList<>();
    private Integer filtersSize;
    private List<? extends RoleInterface> roles = new ArrayList<>();
    private Integer rolesSize;
    private List<? extends ClientInterface> clients = new ArrayList<>();
    private int clientsSize;
    private List<? extends ProjectInterface> projects = new ArrayList<>();
    private Integer projectsSize;
    private List<? extends TaskInterface> processingTasks = new ArrayList<>();

    /**
     * Get login.
     *
     * @return login as String
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set login.
     *
     * @param login
     *            as String
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Get name.
     *
     * @return first name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Set name.
     *
     * @param name
     *            as String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get surname.
     *
     * @return surname as String
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Set surname.
     *
     * @param surname
     *            as String
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Get full name of user.
     *
     * @return full name of user as String
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Set full name.
     *
     * @param fullName
     *            as String
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Get location.
     *
     * @return location as String
     */
    public String getLocation() {
        return location;
    }

    /**
     * Set location.
     *
     * @param location
     *            as String
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get LDAP login.
     *
     * @return LDAP login as String
     */
    public String getLdapLogin() {
        return ldapLogin;
    }

    /**
     * Set LDAP login.
     *
     * @param ldapLogin
     *            as String
     */
    public void setLdapLogin(String ldapLogin) {
        this.ldapLogin = ldapLogin;
    }

    /**
     * Get information if user is active.
     *
     * @return true or false
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Set information if user is active.
     *
     * @param active
     *            as boolean
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get list of filters.
     *
     * @return list of filters as FilterInterface
     */
    public List<? extends FilterInterface> getFilters() {
        return filters;
    }

    /**
     * Set list of filters.
     *
     * @param filters
     *            list of filters as FilterInterface
     */
    public void setFilters(List<? extends FilterInterface> filters) {
        this.filters = filters;
    }

    /**
     * Get size of filters.
     *
     * @return size
     */
    public Integer getFiltersSize() {
        return filtersSize;
    }

    /**
     * Set size of filters.
     *
     * @param filtersSize
     *            as Integer
     */
    public void setFiltersSize(Integer filtersSize) {
        this.filtersSize = filtersSize;
    }

    /**
     * Get list of roles.
     *
     * @return list of roles as RoleInterface
     */
    public List<? extends RoleInterface> getRoles() {
        return roles;
    }

    /**
     * Set list of roles.
     *
     * @param roles
     *            list of roles as RoleInterface
     */
    public void setRoles(List<? extends RoleInterface> roles) {
        this.roles = roles;
    }

    /**
     * Get size of roles.
     *
     * @return size
     */
    public int getRolesSize() {
        return rolesSize;
    }

    /**
     * Set size of roles.
     *
     * @param rolesSize
     *            as Integer
     */
    public void setRolesSize(Integer rolesSize) {
        this.rolesSize = rolesSize;
    }

    /**
     * Get list of clients.
     *
     * @return The clients.
     */
    public List<? extends ClientInterface> getClients() {
        return clients;
    }

    /**
     * Set list of clients.
     *
     * @param clients The clients.
     */
    public void setClients(List<? extends ClientInterface> clients) {
        this.clients = clients;
    }

    /**
     * Get size of clients result list.
     *
     * @return The clientsSize.
     */
    public int getClientsSize() {
        return clientsSize;
    }

    /**
     * Sets size of clients result list.
     *
     * @param clientsSize The clientsSize.
     */
    public void setClientsSize(Integer clientsSize) {
        this.clientsSize = clientsSize;
    }

    /**
     * Get list of projects.
     *
     * @return list of projects as ProjectInterface
     */
    public List<? extends ProjectInterface> getProjects() {
        return projects;
    }

    /**
     * Set list of projects.
     *
     * @param projects
     *            list of projects as ProjectInterface
     */
    public void setProjects(List<? extends ProjectInterface> projects) {
        this.projects = projects;
    }

    /**
     * Get size of projects result list.
     *
     * @return result size of projects
     *
     */
    public int getProjectsSize() {
        return projectsSize;
    }

    /**
     * Set size of project list.
     *
     * @param projectsSize
     *            size of project list as Integer
     */
    public void setProjectsSize(Integer projectsSize) {
        this.projectsSize = projectsSize;
    }

    /**
     * Get list of processing tasks.
     *
     * @return list of processing tasks as TaskInterface
     */
    public List<? extends TaskInterface> getProcessingTasks() {
        return processingTasks;
    }

    /**
     * Set list of processing tasks.
     *
     * @param processingTasks
     *            list of processing tasks as TaskInterface
     */
    public void setProcessingTasks(List<? extends TaskInterface> processingTasks) {
        this.processingTasks = processingTasks;
    }
}
