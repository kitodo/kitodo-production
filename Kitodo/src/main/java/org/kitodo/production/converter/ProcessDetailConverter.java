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

package org.kitodo.production.converter;

import java.util.List;
import java.util.Optional;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;

import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;

@Named
public class ProcessDetailConverter implements Converter<ProcessDetail> {

    @Override
    public ProcessDetail getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        List<ProcessDetail> list = (List<ProcessDetail>) component.getAttributes().get("allMetadataTypes");
        Optional<ProcessDetail> processDetail = list.parallelStream()
                .filter(metadata -> metadata.getMetadataID().equals(value)).findFirst();
        return processDetail.orElseGet(ProcessFieldedMetadata::new);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ProcessDetail value) throws ConverterException {
        if (value != null) {
            return value.getMetadataID();
        }
        return "";
    }
}
