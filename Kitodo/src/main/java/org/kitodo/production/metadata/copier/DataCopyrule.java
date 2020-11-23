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

package org.kitodo.production.metadata.copier;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.MetadataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.primefaces.PrimeFaces;

public class DataCopyrule {

    List<String> command;

    public DataCopyrule(List<String> command) {
        this.command = command;
    }

    /**
     * When called, the rule must be applied to the given fileformat.
     *
     * @param data
     *            data to apply yourself on
     */
    public void apply(CopierData data){
        Workpiece workpiece = data.getDigitalDocument().getWorkpiece();
        List<IncludedStructuralElement> allIncludedStructuralElements = workpiece.getAllIncludedStructuralElements();

        System.out.println("allChildrenSize" + allIncludedStructuralElements.size());
        for (IncludedStructuralElement child : allIncludedStructuralElements) {
            System.out.println(child.getLabel());
            Collection<Metadata> metadata = child.getMetadata();
            System.out.println("metadatasize" + metadata.size());
            MdSec domain = null;
            for (Metadata metadatum : metadata) {
                domain = metadatum.getDomain();
                System.out.println("key: " + metadatum.getKey());

                if(metadatum instanceof MetadataEntry) {
                    if (metadatum.getKey().equals(this.command.get(0))) {
                        ((MetadataEntry) metadatum).setValue(this.command.get(2));
                        break;
                    }
                    System.out.println("value: " + ((MetadataEntry) metadatum).getValue());
                }
                if (metadatum instanceof MetadataGroup){
                    Collection<Metadata> group = ((MetadataGroup) metadatum).getGroup();
                    if (metadatum.getKey().equals(this.command.get(0))) {
                        
                    }
                    for (Metadata groupelement : group) {
                        System.out.println("value: " + ((MetadataEntry) groupelement).getValue());
                    }

                }

            }
            MetadataEntry metadataEntry = new MetadataEntry();
            metadataEntry.setKey(command.get(0));
            metadataEntry.setValue(command.get(2));
            metadataEntry.setDomain(domain);
            child.getMetadata().add(metadataEntry);

            try (OutputStream out = ServiceManager.getFileService().write(ServiceManager.getFileService().getMetadataFilePath(data.getProcess()))) {
                ServiceManager.getMetsService().save(workpiece, out);
                ServiceManager.getProcessService().saveToIndex(data.getProcess(),false);
            } catch (IOException | CustomResponseException | DataException e) {
                System.out.println("log me");
            }
        }
    }

}
