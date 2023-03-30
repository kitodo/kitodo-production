package org.kitodo.dataeditor.ruleset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.MutableTriple;
import org.kitodo.api.Metadata;
import org.kitodo.dataeditor.ruleset.xml.Reimport;

/**
 * Performs an update merge of metadata.
 */
class MetadataUpdater extends MutableTriple<Collection<Metadata>, Reimport, Collection<Metadata>> {

    /**
     * The metadata during the union.
     */
    private HashMap<String, MetadataUpdater> unifying;

    /**
     * Performs the update. @param currentMetadata the collection being updated
     * 
     * @param updateMetadata
     *            the new metadata that is conditionally inserted
     * @param settings
     *            how to replace the metadata
     */
    void update(Collection<Metadata> currentMetadata, Collection<Metadata> updateMetadata,
            Settings settings) {

        unifying = new HashMap<>();
        for (Metadata metadata : currentMetadata) {
            unifying.computeIfAbsent(metadata.getKey(), MetadataUpdater::create).getLeft().add(metadata);
        }
        for (Metadata metadata : updateMetadata) {
            unifying.computeIfAbsent(metadata.getKey(), MetadataUpdater::create).getRight().add(metadata);
        }
        for (Entry<String, MetadataUpdater> entry:unifying.entrySet()) {
            entry.getValue().setMiddle(settings.getReimport(entry.getKey()));
        }

        currentMetadata.clear();
        for (Entry<String, MetadataUpdater> entry:unifying.entrySet()) {
            MetadataUpdater metadata = entry.getValue();
            switch(metadata.getMiddle()) {
                case ADD:
                    currentMetadata.addAll(metadata.getLeft());
                    currentMetadata.addAll(metadata.getRight());
                break;
                case KEEP:
                    currentMetadata.addAll(metadata.getLeft());
                break;
                case REPLACE:
                    currentMetadata.addAll(metadata.getRight());
                break;
                default: throw new IllegalStateException("complete switch");
            }
        }

        unifying = null;
    }

    /**
     * Makeshift constructor function for {@code Map.computeIfAbsent()}.
     * @see  Map#computeIfAbsent(Object, java.util.function.Function)
     * @param key key passed by the caller, to fulfill the method signature
     * @return created storage object
     */
    private static MetadataUpdater create(String key) {
        MetadataUpdater created = new MetadataUpdater();
        created.setLeft(new ArrayList<Metadata>());
        created.setRight(new ArrayList<Metadata>());
        return created;
    }
}
