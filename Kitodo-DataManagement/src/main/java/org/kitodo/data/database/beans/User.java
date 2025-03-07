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

import org.kitodo.data.database.persistence.UserDAO;

@Entity
@Table(name = "user")
public class User extends BaseBean {

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

    @Column(name = "shortcuts", columnDefinition = "longtext")
    private String shortcuts;

    @ManyToOne
    @JoinColumn(name = "ldapGroup_id", foreignKey = @ForeignKey(name = "FK_user_ldapGroup_id"))
    private LdapGroup ldapGroup;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_x_role", joinColumns = {@JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "FK_user_x_role_user_id")) }, inverseJoinColumns = {@JoinColumn(name = "role_id",
                    foreignKey = @ForeignKey(name = "FK_user_x_role_role_id")) })
    private List<Role> roles;

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

    @Column(name = "default_gallery_view_mode")
    private String defaultGalleryViewMode;

    @Column(name = "show_comments_by_default")
    private boolean showCommentsByDefault;

    @Column(name = "show_pagination_by_default")
    private boolean showPaginationByDefault;

    @Column(name = "paginate_from_first_page_by_default")
    private boolean paginateFromFirstPageByDefault;

    @Column(name = "show_physical_page_number_below_thumbnail")
    private boolean showPhysicalPageNumberBelowThumbnail;

    @ManyToOne
    @JoinColumn(name = "default_client_id", foreignKey = @ForeignKey(name = "FK_user_default_client_id"))
    private Client defaultClient;

    /**
     * Constructor for User Entity.
     */
    public User() {
        this.roles = new ArrayList<>();
        this.projects = new ArrayList<>();
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
        this.shortcuts = user.shortcuts;
        this.showCommentsByDefault = user.showCommentsByDefault;
        this.showPaginationByDefault = user.showPaginationByDefault;
        this.paginateFromFirstPageByDefault = user.paginateFromFirstPageByDefault;
        this.defaultGalleryViewMode = user.defaultGalleryViewMode;
        this.showPhysicalPageNumberBelowThumbnail = user.showPhysicalPageNumberBelowThumbnail;

        this.roles = Objects.isNull(user.roles) ? new ArrayList<>() : user.roles;
        this.projects = Objects.isNull(user.projects) ? new ArrayList<>() : user.projects;
        this.clients = Objects.isNull(user.clients) ? new ArrayList<>() : user.clients;
        this.filters = Objects.isNull(user.filters) ? new ArrayList<>() : user.filters;
        this.defaultClient = Objects.isNull(user.defaultClient) ? null : user.defaultClient;

        if (Objects.nonNull(user.tableSize)) {
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

    /**
     * Get shortcuts.
     *
     * @return value of shortcuts
     */
    public String getShortcuts() {
        return shortcuts;
    }

    /**
     * Set shortcuts.
     *
     * @param shortcuts as java.lang.String
     */
    public void setShortcuts(String shortcuts) {
        this.shortcuts = shortcuts;
    }

    public LdapGroup getLdapGroup() {
        return this.ldapGroup;
    }

    public void setLdapGroup(LdapGroup ldapGroup) {
        this.ldapGroup = ldapGroup;
    }

    /**
     * Get roles.
     *
     * @return list of Role objects
     */
    public List<Role> getRoles() {
        initialize(new UserDAO(), this.roles);
        if (Objects.isNull(this.roles)) {
            this.roles = new ArrayList<>();
        }
        return this.roles;
    }

    /**
     * Set roles.
     *
     * @param roles
     *            list of Role objects
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Get tasks processed by this user.
     *
     * @return tasks processed by this user
     */
    public List<Task> getProcessingTasks() {
        initialize(new UserDAO(), this.processingTasks);
        if (Objects.isNull(this.processingTasks)) {
            this.processingTasks = new ArrayList<>();
        }
        return this.processingTasks;
    }

    public void setProcessingTasks(List<Task> processingTasks) {
        this.processingTasks = processingTasks;
    }

    /**
     * Get projects to which user is assigned.
     *
     * @return projects to which user is assigned
     */
    public List<Project> getProjects() {
        initialize(new UserDAO(), this.projects);
        if (Objects.isNull(this.projects)) {
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
        initialize(new UserDAO(), this.clients);
        if (Objects.isNull(this.clients)) {
            this.clients = new ArrayList<>();
        }
        return this.clients;
    }

    /**
     * Sets clients.
     *
     * @param clients
     *            The clients.
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

    /**
     * Get Metadata language.
     *
     * @return metadata language as String
     */
    public String getMetadataLanguage() {
        if (Objects.isNull(this.metadataLanguage)) {
            return "";
        } else {
            return this.metadataLanguage;
        }
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
        initialize(new UserDAO(), this.filters);
        if (Objects.isNull(this.filters)) {
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
     * Get defaultGalleryViewMode.
     *
     * @return value of defaultGalleryViewMode
     */
    public String getDefaultGalleryViewMode() {
        return defaultGalleryViewMode;
    }

    /**
     * Set defaultGalleryViewMode.
     *
     * @param defaultGalleryViewMode as java.lang.String
     */
    public void setDefaultGalleryViewMode(String defaultGalleryViewMode) {
        this.defaultGalleryViewMode = defaultGalleryViewMode;
    }

    /**
     * Get showCommentsByDefault.
     *
     * @return value of showCommentsByDefault
     */
    public boolean isShowCommentsByDefault() {
        return showCommentsByDefault;
    }

    /**
     * Set showCommentsByDefault.
     *
     * @param showCommentsByDefault as boolean
     */
    public void setShowCommentsByDefault(boolean showCommentsByDefault) {
        this.showCommentsByDefault = showCommentsByDefault;
    }

    /**
     * Get showPaginationByDefault.
     *
     * @return value of showPaginationByDefault
     */
    public boolean isShowPaginationByDefault() {
        return showPaginationByDefault;
    }

    /**
     * Set showPaginationByDefault.
     *
     * @param showPaginationByDefault as boolean
     */
    public void setShowPaginationByDefault(boolean showPaginationByDefault) {
        this.showPaginationByDefault = showPaginationByDefault;
    }

    /**
     * Get paginateFromFirstPageByDefault.
     * 
     * @return value of paginateFromFirstPageByDefault
     */
    public boolean isPaginateFromFirstPageByDefault() {
        return paginateFromFirstPageByDefault;
    }
    
    /**
     * Set paginateFromFirstPageByDefault.
     * 
     * @param paginateFromFirstPageByDefault as boolean
     */
    public void setPaginateFromFirstPageByDefault(boolean paginateFromFirstPageByDefault) {
        this.paginateFromFirstPageByDefault = paginateFromFirstPageByDefault;
    }

    /**
     * Get showPhysicalPageNumberBelowThumbnail.
     * 
     * @return value of showPhysicalPageNumberBelowThumbnail
     */
    public boolean isShowPhysicalPageNumberBelowThumbnail() {
        return showPhysicalPageNumberBelowThumbnail;
    }

    /**
     * Set showPhysicalPageNumberBelowThumbnail.
     * 
     * @param showPhysicalPageNumberBelowThumbnail as boolean
     */
    public void setShowPhysicalPageNumberBelowThumbnail(boolean showPhysicalPageNumberBelowThumbnail) {
        this.showPhysicalPageNumberBelowThumbnail = showPhysicalPageNumberBelowThumbnail;
    }

    /**
     * Get default client.
     *
     * @return default client
     */
    public Client getDefaultClient() {
        return defaultClient;
    }

    /**
     * Set default client.
     *
     * @param defaultClient default client
     */
    public void setDefaultClient(Client defaultClient) {
        this.defaultClient = defaultClient;
    }

    /**
     * Removes a user from the environment. Since the
     * user ID may still be referenced somewhere, the user is not hard deleted from
     * the database, instead the account is set inactive and invisible.
     *
     * <p>
     * To allow recreation of an account with the same login the login is cleaned -
     * otherwise it would be blocked eternally by the login existence test performed
     * in the UserForm.save() function. In addition, all personally identifiable
     * information is removed from the database as well.
     */
    public void selfDestruct() {
        this.deleted = true;
        this.login = null;
        this.ldapLogin = null;
        this.active = false;
        this.name = null;
        this.surname = null;
        this.location = null;
        this.roles = new ArrayList<>();
        this.projects = new ArrayList<>();
    }

    // Here will be methods which should be in UserService but are used by jsp
    // files

    public String getFullName() {
        return this.getSurname() + ", " + this.getName();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof User) {
            User user = (User) object;
            return Objects.equals(this.getId(), user.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, name, surname);
    }

}
