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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.security.SecurityPasswordEncoder;
import org.kitodo.services.ServiceManager;

@Named("BenutzerverwaltungForm")
@SessionScoped
public class BenutzerverwaltungForm extends BasisForm {
    private static final long serialVersionUID = -3635859455444639614L;
    private User userObject = new User();
    private boolean hideInactiveUsers = true;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(BenutzerverwaltungForm.class);
    private SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
    private String password;

    private String userListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String userEditPath = MessageFormat.format(REDIRECT_PATH, "userEdit");

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public BenutzerverwaltungForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getUserService()));
    }

    /**
     * New user.
     *
     * @return page
     */
    public String newUser() {
        this.userObject = new User();
        this.userObject.setName("");
        this.userObject.setSurname("");
        this.userObject.setLogin("");
        this.userObject.setLdapLogin("");
        this.userObject.setPassword("");
        this.password = "";
        return userEditPath;
    }

    /**
     * This method initializes the user list without any filters whenever the
     * bean is constructed.
     */
    @PostConstruct
    public void initializeUserList() {
        filterAll();
    }

    /**
     * Anzeige der gefilterten Nutzer.
     */
    public String filterAll() {
        return userListPath;
    }

    private List<UserDTO> getUsers() throws DataException {
        if (this.hideInactiveUsers) {
            return serviceManager.getUserService().findAllActiveUsers();
        } else {
            return serviceManager.getUserService().findAllVisibleUsers();
        }
    }

    /**
     * Save user if there is not other user with the same login.
     *
     * @return page or empty String
     */
    public String save() {
        Session session = Helper.getHibernateSession();
        session.evict(this.userObject);
        String login = this.userObject.getLogin();

        if (!isLoginValid(login)) {
            return null;
        }

        String id = null;
        if (this.userObject.getId() != null) {
            id = this.userObject.getId().toString();
        }

        try {
            if (this.serviceManager.getUserService().getAmountOfUsersWithExactlyTheSameLogin(id, login) == 0) {
                this.userObject.setPassword(passwordEncoder.encrypt(this.password));
                this.serviceManager.getUserService().save(this.userObject);
                return userListPath;

            } else {
                Helper.setFehlerMeldung("", Helper.getTranslation("loginBereitsVergeben"));
                return null;
            }
        } catch (DataException e) {
            Helper.setErrorMessage("Error, could not save", logger, e);
            return null;
        }
    }

    private boolean isLoginValid(String inLogin) {
        boolean valid;
        String patternStr = "[A-Za-z0-9@_\\-.]*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inLogin);
        valid = matcher.matches();
        if (!valid) {
            Helper.setFehlerMeldung("", Helper.getTranslation("loginNotValid"));
        }

        /* Pfad zur Datei ermitteln */
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes"
                + File.separator + "kitodo_loginBlacklist.txt";
        /*
         * Datei zeilenweise durchlaufen und die auf ungültige Zeichen
         * vergleichen
         */
        try (FileInputStream fis = new FileInputStream(filename);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(isr)) {
            String str;
            while ((str = in.readLine()) != null) {
                if (str.length() > 0 && inLogin.equalsIgnoreCase(str)) {
                    valid = false;
                    Helper.setFehlerMeldung("", "Login " + str + Helper.getTranslation("loginNotValid"));
                }
            }
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return valid;
    }

    /**
     * The function delete() deletes a user account.
     *
     * <p>
     * Please note that deleting a user in goobi.production will not delete the
     * user from a connected LDAP service.
     * </p>
     *
     * @return a string indicating the screen showing up after the command has
     *         been performed.
     */
    public String delete() {
        try {
            serviceManager.getUserService().remove(userObject);
        } catch (DataException e) {
            Helper.setErrorMessage("Error, could not save", logger, e);
            return null;
        }
        return filterAll();
    }

    /**
     * Remove from user group.
     *
     * @return empty String
     */
    public String deleteFromGroup() {
        int gruppenID = Integer.parseInt(Helper.getRequestParameter("ID"));

        List<UserGroup> neu = new ArrayList<>();
        for (UserGroup userGroup : this.userObject.getUserGroups()) {
            if (userGroup.getId() != gruppenID) {
                neu.add(userGroup);
            }
        }
        this.userObject.setUserGroups(neu);
        return null;
    }

    /**
     * Add to user group.
     *
     * @return empty String or null
     */
    public String addToGroup() {
        Integer gruppenID = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            UserGroup usergroup = serviceManager.getUserGroupService().getById(gruppenID);
            for (UserGroup b : this.userObject.getUserGroups()) {
                if (b.equals(usergroup)) {
                    return null;
                }
            }
            this.userObject.getUserGroups().add(usergroup);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
        return null;
    }

    /**
     * Remove user from project.
     *
     * @return empty String
     */
    public String deleteFromProject() {
        int projectId = Integer.parseInt(Helper.getRequestParameter("ID"));
        try {
            Project project = serviceManager.getProjectService().getById(projectId);
            this.userObject.getProjects().remove(project);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
        return null;
    }

    /**
     * Add user to project.
     *
     * @return empty String or null
     */
    public String addToProject() {
        Integer projectId = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            Project project = serviceManager.getProjectService().getById(projectId);
            for (Project p : this.userObject.getProjects()) {
                if (p.equals(project)) {
                    return null;
                }
            }
            this.userObject.getProjects().add(project);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error on reading database", logger, e);
            return null;
        }
        return null;
    }

    /*
     * Getter und Setter
     */

    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Set class.
     *
     * @param userObject
     *            user object
     */
    public void setUserObject(User userObject) {
        try {
            this.userObject = serviceManager.getUserService().getById(userObject.getId());
        } catch (DAOException e) {
            this.userObject = userObject;
        }
    }

    /**
     * Ldap-Konfiguration - choose LDAP group.
     */
    public Integer getLdapGruppeAuswahl() {
        if (this.userObject.getLdapGroup() != null) {
            return this.userObject.getLdapGroup().getId();
        } else {
            return 0;
        }
    }

    /**
     * Ldap-Konfiguration - set LDAP group.
     */
    public void setLdapGruppeAuswahl(Integer inAuswahl) {
        if (inAuswahl != 0) {
            try {
                this.userObject.setLdapGroup(serviceManager.getLdapGroupService().getById(inAuswahl));
            } catch (DAOException e) {
                Helper.setErrorMessage("Error on writing to database", logger, e);
            }
        }
    }

    /**
     * Ldap-Konfiguration - get LDAP group choice list.
     */
    public List<SelectItem> getLdapGruppeAuswahlListe() {
        List<SelectItem> myLdapGruppen = new ArrayList<>();
        List<LdapGroup> temp = serviceManager.getLdapGroupService().getByQuery("from LdapGroup ORDER BY title");
        for (LdapGroup gru : temp) {
            myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitle(), null));
        }
        return myLdapGruppen;
    }

    /**
     * Writes the user at ldap server.
     */
    public String writeUserAtLdapServer() {
        try {
            serviceManager.getLdapServerService().createNewUser(this.userObject,
                passwordEncoder.decrypt(this.userObject.getPassword()));
        } catch (NoSuchAlgorithmException | NamingException | IOException | RuntimeException e) {
            Helper.setErrorMessage("Could not generate ldap entry", logger, e);
        }
        return null;
    }

    public boolean isHideInactiveUsers() {
        return this.hideInactiveUsers;
    }

    public void setHideInactiveUsers(boolean hideInactiveUsers) {
        this.hideInactiveUsers = hideInactiveUsers;
    }

    public boolean getLdapUsage() {
        return ConfigCore.getBooleanParameter("ldap_use");
    }

    /**
     * Method being used as viewAction for user edit form.
     *
     * @param id
     *            ID of the user to load
     */
    public void loadUserObject(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setUserObject(this.serviceManager.getUserService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("user"), id }, logger, e);
        }
    }

    /**
     * Return list of projects.
     *
     * @return list of projects
     */
    public List<ProjectDTO> getProjects() {
        try {
            return serviceManager.getProjectService().findAll(true);
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("projekte") }, logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Return list of user groups.
     *
     * @return list of user groups
     */
    public List<UserGroupDTO> getUserGroups() {
        try {
            return serviceManager.getUserGroupService().findAll();
        } catch (DataException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("benutzergruppen") }, logger,
                e);
            return new LinkedList<>();
        }
    }

    /**
     * Gets password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password.
     *
     * @param password
     *            The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
