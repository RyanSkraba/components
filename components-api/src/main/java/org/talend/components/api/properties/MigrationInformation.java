package org.talend.components.api.properties;

/**
 * Returns information about a migration that was done when converting an object from its serialized form into an
 * object. If the serialized form was created from a previous version of the implementation, the {@link #isMigrated()}
 * property will be set, which can be used to trigger actions on the part of the client.
 */
// FIXME - this class is temporarily here - needs to move to Daikon.
public interface MigrationInformation {

    /**
     * Returns true if this object was created from a serialized form used by a previous version of the implementation.
     * 
     * This can be used by the caller to notify the user that the object has been migrated and also, if desired, it can
     * be re-saved in its current serialized form (which would be different than the serialized form provided initially).
     * 
     * @return true if the object was migrated
     */
    public boolean isMigrated();

    /**
     * Returns the version identifier of the version of the serialized form.
     * 
     * Could be used to display the version from which this object was migrated.
     * 
     * @return the version identifier.
     */
    public String getVersion();

}
