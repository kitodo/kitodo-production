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

package org.kitodo.production.forms.user;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("UserEditViewClientsTab")
@ViewScoped
public class UserEditViewClientsTab extends BaseForm {

    private static final Logger logger = LogManager.getLogger(UserEditViewClientsTab.class);

    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;

    private List<Client> clients;


    /**
     * Initialize UserEditViewClientsTab.
     */
    @PostConstruct
    public void init() {
        sortBy = SortMeta.builder().field("name").order(SortOrder.ASCENDING).build();
    }

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Return list of clients available for assignment to the user.
     *
     * @return list of clients available for assignment to the user
     */
    public List<Client> getClients() {
        return clients;
    }       

    /**
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    public void load(User userObject) {
        // reset when user is loaded
        this.userObject = userObject;

        try {
            this.clients = ServiceManager.getClientService().getAllAvailableForAssignToUser(this.userObject)
                    .stream().sorted(Comparator.comparing(Client::getName)).collect(Collectors.toList());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.CLIENT.getTranslationPlural() }, logger,
                    e);
            this.clients = new LinkedList<>();
        }
    }

    /**
     * Save clients tab.
     *
     * @return true if user clients data was saved
     */
    public boolean save() {
        return true;
    }

    /**
     * Remove user from client.
     *
     * @return null (to stay one the same page)
     */
    public String deleteFromClient() {
        String idParameter = Helper.getRequestParameter(ID_PARAMETER);
        if (Objects.nonNull(idParameter)) {
            try {
                int clientId = Integer.parseInt(idParameter);
                for (Client client : this.userObject.getClients()) {
                    if (client.getId().equals(clientId)) {
                        this.userObject.getClients().remove(client);
                        if (client.equals(this.userObject.getDefaultClient())) {
                            this.userObject.setDefaultClient(null);
                        }
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add client to user.
     *
     * @return null (to stay one the same page)
     */
    public String addToClient() {
        String idParameter = Helper.getRequestParameter("ID");
        if (Objects.nonNull(idParameter)) {
            int clientId = 0;
            try {
                clientId = Integer.parseInt(idParameter);
                Client client = ServiceManager.getClientService().getById(clientId);

                if (!this.userObject.getClients().contains(client)) {
                    this.userObject.getClients().add(client);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.CLIENT.getTranslationSingular(), clientId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

}
