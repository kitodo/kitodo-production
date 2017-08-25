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
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.ldap.Ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
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
import org.kitodo.dto.UserDTO;
import org.kitodo.services.ServiceManager;

@Named("BenutzerverwaltungForm")
@SessionScoped
public class BenutzerverwaltungForm extends BasisForm {
    private static final long serialVersionUID = -3635859455444639614L;
    private User myClass = new User();
    private boolean hideInactiveUsers = true;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(BenutzerverwaltungForm.class);
    private int userId;

    /**
     * New user.
     *
     * @return page
     */
    public String newUser() {
        this.myClass = new User();
        this.myClass.setName("");
        this.myClass.setSurname("");
        this.myClass.setLogin("");
        this.myClass.setLdapLogin("");
        this.myClass.setPasswordDecrypted("Passwort");
        this.userId = 0;
        return "/pages/BenutzerBearbeiten?faces-redirect=true";
    }

    /**
     * display all users without filtering.
     *
     * @return page or empty String
     */
    public String filterKein() {
        this.filter = null;
        List<UserDTO> users = new ArrayList<>();
        try {
            users = getUsers();
        } catch (DataException e) {
            logger.error(e);
        }
        this.page = new Page(0, users);
        return "/pages/BenutzerAlle";
    }

    /**
     * This method initializes the user list without any filters whenever the
     * bean is constructed.
     */
    @PostConstruct
    public void initializeUserList() {
        filterKein();
    }

    public String filterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /**
     * Anzeige der gefilterten Nutzer.
     */
    public String filterAlleStart() {
        List<UserDTO> users = new ArrayList<>();
        try {
            if (this.filter != null && this.filter.length() != 0) {
                users = serviceManager.getUserService().findActiveUsersByName(this.filter);
            } else {
                users = getUsers();
            }
        } catch (DataException e) {
            logger.error(e);
        }
        this.page = new Page(0, users);
        return "/pages/BenutzerAlle";
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
        session.evict(this.myClass);
        String login = this.myClass.getLogin();

        if (!isLoginValid(login)) {
            return null;
        }

        String id = this.myClass.getId().toString();

        try {
            if (this.serviceManager.getUserService().getAmountOfUsersWithExactlyTheSameLogin(id, login) == 0) {
                this.serviceManager.getUserService().save(this.myClass);
                return "/pages/BenutzerAlle";
            } else {
                Helper.setFehlerMeldung("", Helper.getTranslation("loginBereitsVergeben"));
                return null;
            }
        } catch (DataException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
            logger.error(e);
            return null;
        }
    }

    private boolean isLoginValid(String inLogin) {
        boolean valide = true;
        String patternStr = "[A-Za-z0-9@_\\-.]*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inLogin);
        valide = matcher.matches();
        if (!valide) {
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
                    valide = false;
                    Helper.setFehlerMeldung("", "Login " + str + Helper.getTranslation("loginNotValid"));
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
        return valide;
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
            serviceManager.getUserService().remove(myClass);
        } catch (DataException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
            logger.error(e);
            return null;
        }
        return "/pages/BenutzerAlle";
    }

    /**
     * Remove from user group.
     *
     * @return empty String
     */
    public String deleteFromGroup() {
        int gruppenID = Integer.parseInt(Helper.getRequestParameter("ID"));

        List<UserGroup> neu = new ArrayList<>();
        for (UserGroup userGroup : this.myClass.getUserGroups()) {
            if (userGroup.getId() != gruppenID) {
                neu.add(userGroup);
            }
        }
        this.myClass.setUserGroups(neu);
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
            UserGroup usergroup = serviceManager.getUserGroupService().find(gruppenID);
            for (UserGroup b : this.myClass.getUserGroups()) {
                if (b.equals(usergroup)) {
                    return null;
                }
            }
            this.myClass.getUserGroups().add(usergroup);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error on reading database", e.getMessage());
            return null;
        }
        return null;
    }

    /**
     * Remove user from project.
     *
     * @return empty String
     */
    public String ausProjektLoeschen() {
        int projektID = Integer.parseInt(Helper.getRequestParameter("ID"));
        List<Project> neu = new ArrayList<>();
        for (Project project : this.myClass.getProjects()) {
            if (project.getId() != projektID) {
                neu.add(project);
            }
        }
        this.myClass.setProjects(neu);
        return null;
    }

    /**
     * Add user to project.
     *
     * @return empty String or null
     */
    public String zuProjektHinzufuegen() {
        Integer projektID = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            Project project = serviceManager.getProjectService().find(projektID);
            for (Project p : this.myClass.getProjects()) {
                if (p.equals(project)) {
                    return null;
                }
            }
            this.myClass.getProjects().add(project);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error on reading database", e.getMessage());
            return null;
        }
        return null;
    }

    /*
     * Getter und Setter
     */

    public User getMyClass() {
        return this.myClass;
    }

    /**
     * Set class.
     *
     * @param inMyClass
     *            user object
     */
    public void setMyClass(User inMyClass) {
        Helper.getHibernateSession().flush();
        Helper.getHibernateSession().clear();
        try {
            this.myClass = serviceManager.getUserService().find(inMyClass.getId());
        } catch (DAOException e) {
            this.myClass = inMyClass;
        }
    }

    /**
     * Ldap-Konfiguration - choose LDAP group.
     */
    public Integer getLdapGruppeAuswahl() {
        if (this.myClass.getLdapGroup() != null) {
            return this.myClass.getLdapGroup().getId();
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
                this.myClass.setLdapGroup(serviceManager.getLdapGroupService().find(inAuswahl));
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Error on writing to database", "");
                logger.error(e);
            }
        }
    }

    /**
     * Ldap-Konfiguration - get LDAP group choice list.
     */
    public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
        List<SelectItem> myLdapGruppen = new ArrayList<>();
        List<LdapGroup> temp = serviceManager.getLdapGroupService().search("from LdapGroup ORDER BY title");
        for (LdapGroup gru : temp) {
            myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitle(), null));
        }
        return myLdapGruppen;
    }

    /**
     * Ldap-Konfiguration für den Benutzer schreiben.
     */
    public String ldapKonfigurationSchreiben() {
        Ldap myLdap = new Ldap();
        try {
            myLdap.createNewUser(this.myClass, this.myClass.getPasswordDecrypted());
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not generate ldap entry: " + e.getMessage());
            }
            Helper.setFehlerMeldung(e.getMessage());
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
     * Method being used as viewAction for user edit form. If 'userId' is '0',
     * the form for creating a new user will be displayed.
     */
    public void loadMyClass() {
        try {
            if (!Objects.equals(this.userId, 0)) {
                setMyClass(this.serviceManager.getUserService().find(this.userId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving user with ID '" + this.userId + "'; ", e.getMessage());
        }
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

}
