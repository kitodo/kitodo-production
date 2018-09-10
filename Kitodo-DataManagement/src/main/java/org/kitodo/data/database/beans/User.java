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

package org.kitodo.data.database.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User extends BaseIndexedBean {
    private static final long serialVersionUID = -7482853955996650586L;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "ldapLogin")
    private String ldapLogin;

    @Column(name = "password")
    private String password;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "location")
    private String location;

    @Column(name = "tableSize")
    private Integer tableSize = 10;

    @Column(name = "configProductionDateShow")
    private boolean configProductionDateShow = false;

    @Column(name = "metadataLanguage")
    private String metadataLanguage;

    @Column(name = "language")
    private String language;

    @Column(name = "withMassDownload")
    private boolean withMassDownload = false;

    @ManyToOne
    @JoinColumn(name = "ldapGroup_id", foreignKey = @ForeignKey(name = "FK_user_ldapGroup_id"))
    private LdapGroup ldapGroup;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_x_userGroup", joinColumns = {@JoinColumn(name = "user_id",
        foreignKey = @ForeignKey(name = "FK_user_x_userGroup_user_id")) }, inverseJoinColumns = {@JoinColumn(name = "userGroup_id",
            foreignKey = @ForeignKey(name = "FK_user_x_userGroup_userGroup_id")) })
    private List<UserGroup> userGroups;

    @ManyToMany(mappedBy = "users", cascade = CascadeType.PERSIST)
    private List<Task> tasks;

    @OneToMany(mappedBy = "processingUser", cascade = CascadeType.PERSIST)
    private List<Task> processingTasks;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "project_x_user", joinColumns = {@JoinColumn(name = "user_id",
        foreignKey = @ForeignKey(name = "FK_project_x_user_user_id")) }, inverseJoinColumns = {@JoinColumn(name = "project_id",
            foreignKey = @ForeignKey(name = "FK_project_x_user_project_id")) })
    private List<Project> projects;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "client_x_user", joinColumns = {@JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "FK_client_x_user_user_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_client_x_user_client_id")) })
    private List<Client> clients;

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Filter> filters;

    /**
     * Constructor for User Entity.
     */
    public User() {
        this.userGroups = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.setLanguage("de");
    }

    /**
     * Copy Constructor.
     *
     * @param user
     *            The user.
     */
    public User(User user) {

        this.setId(user.getId());
        this.setIndexAction(user.getIndexAction());
        this.setLanguage(user.getLanguage());
        this.active = user.active;
        this.configProductionDateShow = user.configProductionDateShow;
        this.deleted = user.deleted;
        this.ldapGroup = user.ldapGroup;
        this.ldapLogin = user.ldapLogin;
        this.location = user.location;
        this.login = user.login;
        this.metadataLanguage = user.metadataLanguage;
        this.name = user.name;
        this.password = user.password;
        this.processingTasks = user.processingTasks;
        this.surname = user.surname;
        this.withMassDownload = user.withMassDownload;

        if (user.userGroups != null) {
            this.userGroups = user.userGroups;
        } else {
            this.userGroups = new ArrayList<>();
        }

        if (user.projects != null) {
            this.projects = user.projects;
        } else {
            this.projects = new ArrayList<>();
        }

        if (user.clients != null) {
            this.clients = user.clients;
        } else {
            this.clients = new ArrayList<>();
        }

        if (user.tasks != null) {
            this.tasks = user.tasks;
        } else {
            this.tasks = new ArrayList<>();
        }

        if (user.filters != null) {
            this.filters = user.filters;
        } else {
            this.filters = new ArrayList<>();
        }

        // default values
        if (user.tableSize != null) {
            this.tableSize = user.tableSize;
        }
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String inputPassword) {
        this.password = inputPassword;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get table size or 10 if table size is null.
     *
     * @return table size or 10 if table size is null
     */
    public Integer getTableSize() {
        if (Objects.isNull(this.tableSize)) {
            return 10;
        }
        return this.tableSize;
    }

    public void setTableSize(Integer tableSize) {
        this.tableSize = tableSize;
    }

    public boolean isWithMassDownload() {
        return this.withMassDownload;
    }

    public void setWithMassDownload(boolean withMassDownload) {
        this.withMassDownload = withMassDownload;
    }

    public LdapGroup getLdapGroup() {
        return this.ldapGroup;
    }

    public void setLdapGroup(LdapGroup ldapGroup) {
        this.ldapGroup = ldapGroup;
    }

    public List<UserGroup> getUserGroups() {
        return this.userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getProcessingTasks() {
        if (this.processingTasks == null) {
            this.processingTasks = new ArrayList<>();
        }
        return this.processingTasks;
    }

    public void setProcessingTasks(List<Task> processingTasks) {
        this.processingTasks = processingTasks;
    }

    public List<Project> getProjects() {
        if (this.projects == null) {
            this.projects = new ArrayList<>();
        }
        return this.projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    /**
     * Gets clients.
     *
     * @return The clients.
     */
    public List<Client> getClients() {
        if (this.clients == null) {
            this.clients = new ArrayList<>();
        }
        return clients;
    }

    /**
     * Sets clients.
     *
     * @param clients The clients.
     */
    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public boolean isConfigProductionDateShow() {
        return this.configProductionDateShow;
    }

    public void setConfigProductionDateShow(boolean configProductionDateShow) {
        this.configProductionDateShow = configProductionDateShow;
    }

    public String getMetadataLanguage() {
        return this.metadataLanguage;
    }

    public void setMetadataLanguage(String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    /**
     * Get user language.
     *
     * @return user language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set user language.
     *
     * @param language
     *            String
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLdapLogin() {
        return this.ldapLogin;
    }

    public void setLdapLogin(String ldapLogin) {
        this.ldapLogin = ldapLogin;
    }

    /**
     * Get user filters.
     *
     * @return list of user filters
     */
    public List<Filter> getFilters() {
        if (this.filters == null) {
            this.filters = new ArrayList<>();
        }
        return this.filters;
    }

    /**
     * Set user filters.
     *
     * @param filters
     *            list of user filters
     */
    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    /**
     * The function selfDestruct() removes a user from the environment. Since the
     * user ID may still be referenced somewhere, the user is not hard deleted from
     * the database, instead the account is set inactive and invisible.
     *
     * <p>
     * To allow recreation of an account with the same login the login is cleaned -
     * otherwise it would be blocked eternally by the login existence test performed
     * in the BenutzerverwaltungForm.Speichern() function. In addition, all
     * personally identifiable information is removed from the database as well.
     * </p>
     */

    public User selfDestruct() {
        this.deleted = true;
        this.login = null;
        this.active = false;
        this.name = null;
        this.surname = null;
        this.location = null;
        this.userGroups = new ArrayList<>();
        this.projects = new ArrayList<>();
        return this;
    }

    // Here will be methods which should be in UserService but are used by jsp
    // files

    public String getFullName() {
        return this.getSurname() + ", " + this.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (name != null ? !name.equals(user.name) : user.name != null) {
            return false;
        }
        if (surname != null ? !surname.equals(user.surname) : user.surname != null) {
            return false;
        }
        return login != null ? login.equals(user.login) : user.login == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (login != null ? login.hashCode() : 0);
        return result;
    }

}
