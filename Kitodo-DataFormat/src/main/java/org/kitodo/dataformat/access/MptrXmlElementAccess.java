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

package org.kitodo.dataformat.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.DataBindingException;

import org.kitodo.api.dataformat.LinkedStructure;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.mets.InputStreamProviderInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MptrXmlElementAccess {

    LinkedStructure linkedStructure = new LinkedStructure();

    /**
     * Constructor to read a linked structure from METS.
     *
     * @param div
     *            METS {@code <div>} element from which the structure is to be
     *            built
     * @param mets
     *            METS data structure of the current workpiece
     * @param inputStreamProvider
     *            a function that opens an input stream
     * @throws IllegalStateException
     *             if the child does not have a link to the parent, or the
     *             parent link of the child returns METS data different from the
     *             parent’s METS
     */
    MptrXmlElementAccess(DivType div, Mets parent, InputStreamProviderInterface inputStreamProvider) {
        try {
            linkedStructure.setOrder(div.getORDER());
            URI uri = new URI(div.getMptr().stream().findFirst().get().getHref());
            linkedStructure.setUri(uri);
            Mets child;
            try (InputStream in = inputStreamProvider.getInputStream(uri, true)) {
                child = MetsXmlElementAccess.readMets(in);
            }
            ensureParenthood(parent, child, inputStreamProvider);
            Structure linked = MetsXmlElementAccess.toWorkpiece(child, inputStreamProvider).getStructure();
            linkedStructure.setLabel(linked.getLabel());
            linkedStructure.setType(linked.getType());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new DataBindingException(e.getMessage(), e);
        }
    }

    /**
     * Checks if the child METS file is a child of this METS file. If the child
     * being found for some reason is not actually the child of this process,
     * editing can corrupt the other process that was erroneously assumed as a
     * child. Therefore, it is checked at this point whether the assumed child
     * is actually a child. For this, the link is opened and the METS data
     * compared. If the link is missing or the METS data is not equal, the child
     * is a mistake, and an exception is throne.
     *
     * @param current
     *            METS data of the current process
     * @param child
     *            METS data of the process linked as child
     * @throws IOException
     *             if file system I/O fails
     * @throws IllegalStateException
     *             if the child does not have a link to the parent, or the
     *             parent link of the child returns METS data different from the
     *             parent’s METS
     */
    private void ensureParenthood(Mets current, Mets child,
            InputStreamProviderInterface inputStreamProvider) throws IOException {

        Optional<String> optionalParentLink = child.getStructMap().parallelStream()
                .filter(structMap -> "LOGICAL".equals(structMap.getTYPE())).map(structMap -> structMap.getDiv())
                .filter(Objects::nonNull).flatMap(div -> div.getMptr().parallelStream()).map(mptr -> mptr.getHref())
                .reduce((one, another) -> {
                    if (!one.equals(another)) {
                        throw new IllegalStateException("Parent link is ambiguous");
                    } else {
                        return one;
                    }
                });

        if (!optionalParentLink.isPresent()) {
            throw new IllegalStateException("METS file linked as child misses parent link");
        }

        Mets linked;
        try (InputStream in = inputStreamProvider
                .getInputStream(MetsXmlElementAccess.hrefToUri(optionalParentLink.get()), false)) {
            linked = MetsXmlElementAccess.readMets(in);
        }
        if (!Objects.deepEquals(linked, current)) {
            throw new IllegalStateException("METS file linked as child points to different parent METS");
        }
    }
}
