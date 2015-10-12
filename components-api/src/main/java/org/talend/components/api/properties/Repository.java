package org.talend.components.api.properties;

import org.talend.components.api.schema.Schema;

/**
 * A design-time interface to the repository to allow {@link ComponentProperties} to be stored.
 */
public interface Repository {

    /**
     * Adds the specified {@link ComponentProperties} into the design environment.
     *
     * @param properties the {@code ComponentProperties} object to add.
     * @param name the name of the collection of properties
     * @param repositoryLocation the repositoryLocation under which this item should be stored (using the name
     * parameter).
     * @param schema an optional schema to be added at this location.
     * @return repositoryLocation, a String containing the location where this object was stored.
     */
    public String storeComponentProperties(ComponentProperties properties, String name, String repositoryLocation,
            Schema schema);

}
