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

import org.kitodo.data.encryption.DesEncrypter;

@Entity
@Table(name = "user")
public class User extends BaseIndexedBean {
    private static final long serialVersionUID = -7482853955996650586L;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "login")
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

    @Column(name = "sessionTimeout")
    private Integer sessionTimeout = 7200;

    @Column(name = "configProductionDateShow")
    private boolean configProductionDateShow = false;

    @Column(name = "metadataLanguage")
    private String metadataLanguage;

    @Column(name = "withMassDownload")
    private boolean withMassDownload = false;

    @Column(name = "css")
    private String css;

    @ManyToOne
    @JoinColumn(name = "ldapGroup_id", foreignKey = @ForeignKey(name = "FK_user_ldapGroup_id"))
    private LdapGroup ldapGroup;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_x_userGroup", joinColumns = {
            @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_user_x_userGroup_user_id")) }, inverseJoinColumns = {
                    @JoinColumn(name = "userGroup_id", foreignKey = @ForeignKey(name = "FK_user_x_userGroup_userGroup_id")) })
    private List<UserGroup> userGroups;

    @ManyToMany(mappedBy = "users", cascade = CascadeType.PERSIST)
    private List<Task> tasks;

    @OneToMany(mappedBy = "processingUser", cascade = CascadeType.PERSIST)
    private List<Task> processingTasks;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "project_x_user", joinColumns = {
            @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_project_x_user_user_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_project_x_user_project_id")) })
    private List<Project> projects;

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

    public String getPasswordDecrypted() {
        DesEncrypter encrypter = new DesEncrypter();
        return encrypter.decrypt(this.password);
    }

    public void setPasswordDecrypted(String inputPassword) {
        DesEncrypter encrypter = new DesEncrypter();
        this.password = encrypter.encrypt(inputPassword);
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

    public Integer getTableSize() {
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
        return this.projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
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

    public String getLdapLogin() {
        return this.ldapLogin;
    }

    public void setLdapLogin(String ldapLogin) {
        this.ldapLogin = ldapLogin;
    }

    public Integer getSessionTimeout() {
        return this.sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getCss() {
        if (this.css == null || this.css.length() == 0) {
            this.css = "defaultOld.css";
        }
        return this.css;
    }

    public void setCss(String css) {
        this.css = css;
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
     * The function selfDestruct() removes a user from the environment. Since
     * the user ID may still be referenced somewhere, the user is not hard
     * deleted from the database, instead the account is set inactive and
     * invisible.
     *
     * <p>
     * To allow recreation of an account with the same login the login is
     * cleaned - otherwise it would be blocked eternally by the login existence
     * test performed in the BenutzerverwaltungForm.Speichern() function. In
     * addition, all personally identifiable information is removed from the
     * database as well.
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

    /**
     * Get size of user group result.
     *
     * @return size
     */
    public int getUserGroupSize() {
        if (this.getUserGroups() == null) {
            return 0;
        } else {
            return this.getUserGroups().size();
        }
    }

    public String getFullName() {
        return this.getSurname() + ", " + this.getName();
    }

    public Integer getSessionTimeoutInMinutes() {
        return this.getSessionTimeout() / 60;
    }

    public void setSessionTimeoutInMinutes(Integer sessionTimeout) {
        if (sessionTimeout < 5) {
            this.setSessionTimeout(5 * 60);
        } else {
            this.setSessionTimeout(sessionTimeout * 60);
        }
    }
}
