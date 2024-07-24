/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Named;

import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.production.services.ServiceManager;

@Named
public class ImportConfigurationConverter extends BeanConverter implements Converter<ImportConfiguration> {
    @Override
    public ImportConfiguration getAsObject(FacesContext facesContext, UIComponent uiComponent, String value)
            throws ConverterException {
        return (ImportConfiguration) getAsObject(ServiceManager.getImportConfigurationService(), value);
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, ImportConfiguration value)
            throws ConverterException {
        return getAsString(value, "importConfig.configuration");
    }
}
