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

package org.kitodo.production.forms;

import java.text.MessageFormat;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;

@Named("ClientListView")
@ViewScoped
public class ClientListView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(ClientEditView.class);

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "users") + "#usersTabView:clientsTab";
    
    /**
     * Empty default constructor that also sets the LazyBeanModel instance of
     * this bean.
     */
    public ClientListView() {
        super();
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getClientService()));
    }

    /**
     * Create new client.
     *
     * @return page address
     */
    public String newClient() {
        return ClientEditView.VIEW_PATH;
    }

    /**
     * View action to remove a client.
     * 
     * @param client the client to be removed
     * @return page
     */
    public String delete(Client client) {
        if (deleteClient(client)) {
            return VIEW_PATH;
        }
        return stayOnCurrentPage;
    }

    /**
     * Deletes a client.
     * 
     * @param client the client to be removed
     * @return true if the client was deleted
     */
    public static boolean deleteClient(Client client) {
        try {
            client.getListColumns().clear();
            ServiceManager.getClientService().remove(client);
            return true;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.CLIENT.getTranslationSingular() }, logger, e);
        }
        return false;
    }

}
