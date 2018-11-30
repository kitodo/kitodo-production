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

package org.kitodo.services.dataformat;

import java.util.Objects;

import org.kitodo.api.dataformat.mets.AgentXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.DivXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataGroupXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetadataXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class MetsService {

    private static volatile MetsService instance = null;
    private KitodoServiceLoader<AgentXmlElementAccessInterface> agentServiceLoader;
    private KitodoServiceLoader<AreaXmlElementAccessInterface> areaServiceLoader;
    private KitodoServiceLoader<DivXmlElementAccessInterface> divServiceLoader;
    private KitodoServiceLoader<FileXmlElementAccessInterface> fileServiceLoader;
    private KitodoServiceLoader<FLocatXmlElementAccessInterface> fLocatServiceLoader;
    private KitodoServiceLoader<MetadataGroupXmlElementAccessInterface> metadataGroupServiceLoader;
    private KitodoServiceLoader<MetadataXmlElementAccessInterface> metadataServiceLoader;
    private KitodoServiceLoader<MetsXmlElementAccessInterface> metsServiceLoader;
    private KitodoServiceLoader<UseXmlAttributeAccessInterface> useServiceLoader;

    /**
     * Return singleton variable of type MetsService.
     *
     * @return unique instance of MetsService
     */
    public static MetsService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (MetsService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new MetsService();
                }
            }
        }
        return instance;
    }

    private MetsService() {
        agentServiceLoader = new KitodoServiceLoader<>(AgentXmlElementAccessInterface.class);
        areaServiceLoader = new KitodoServiceLoader<>(AreaXmlElementAccessInterface.class);
        divServiceLoader = new KitodoServiceLoader<>(DivXmlElementAccessInterface.class);
        fileServiceLoader = new KitodoServiceLoader<>(FileXmlElementAccessInterface.class);
        fLocatServiceLoader = new KitodoServiceLoader<>(FLocatXmlElementAccessInterface.class);
        metadataGroupServiceLoader = new KitodoServiceLoader<>(MetadataGroupXmlElementAccessInterface.class);
        metadataServiceLoader = new KitodoServiceLoader<>(MetadataXmlElementAccessInterface.class);
        metsServiceLoader = new KitodoServiceLoader<>(MetsXmlElementAccessInterface.class);
        useServiceLoader = new KitodoServiceLoader<>(UseXmlAttributeAccessInterface.class);
    }

    public AgentXmlElementAccessInterface createAgent() {
        return agentServiceLoader.loadModule();
    }

    public AreaXmlElementAccessInterface createArea() {
        return areaServiceLoader.loadModule();
    }

    public DivXmlElementAccessInterface createDiv() {
        return divServiceLoader.loadModule();
    }

    public FileXmlElementAccessInterface createFile() {
        return fileServiceLoader.loadModule();
    }

    public FLocatXmlElementAccessInterface createFLocat() {
        return fLocatServiceLoader.loadModule();
    }

    public MetadataGroupXmlElementAccessInterface createMetadataGroup() {
        return metadataGroupServiceLoader.loadModule();
    }

    public MetadataXmlElementAccessInterface createMetadata() {
        return metadataServiceLoader.loadModule();
    }

    public MetsXmlElementAccessInterface createMets() {
        return metsServiceLoader.loadModule();
    }

    public UseXmlAttributeAccessInterface createUse() {
        return useServiceLoader.loadModule();
    }
}
