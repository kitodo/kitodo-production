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

import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("AuthorityListView")
@ViewScoped
public class AuthorityListView extends BaseForm {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "users") + "#usersTabView:authoritiesTab";

    /**
     * Default constructor that also sets the LazyBeanModel instance of this
     * bean.
     */
    public AuthorityListView() {
        super();
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getAuthorityService()));
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Create new authority.
     *
     * @return page address
     */
    public String newAuthority() {
        return AuthorityEditView.VIEW_PATH;
    }

}
