package org.bgee.controller.user;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Convenient class to store information about user UUID. Package-private to not expose it to users for now.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Nov 2016
 * @since Bgee 13 Nov 2016
 */
class BgeeUUID {
    private final static Logger log = LogManager.getLogger(BgeeUUID.class.getName());
    
    /**
     * A {@code String} representing the charset to use to convert strings to bytes.
     */
    public static final String CHARSET = "UTF-8";
    
    //*****************************
    // attributes
    //*****************************
    /**
     * @see #getUUID()
     */
    private final UUID uuid;
    /**
     * @see #getFromString()
     */
    private final String fromString;
    /**
     * @see #getUUIDSource()
     */
    private final UUIDSource source;
    /**
     * @see #getLastUpdateTimestamp()
     */
    private final long lastUpdateTimestamp;
    

    //*****************************
    // constructors
    //*****************************
    /**
     * Construct a new {@code BgeeUUID} by providing a {@code String} 
     * and the source where the UUID was retrieved from.
     * 
     * @param fromString    The {@code String} to use to produce the UUID. Does not need to be 
     *                      a UUID-formatted string, this method will automatically detect if it does.
     * @param source        The {@code UUIDSource} where the UUID was retrieved from.
     * @param lastUpdate    A {@code long} that is the timestamp of creation date or last update 
     *                      of this {@code UUID}. In that case, most likely the creation date.
     *                      If less than or equal to 0, means that this information 
     *                      is irrelevant (e.g., for API keys, they are currently auto-generated 
     *                      by the user so that we don't know when it was created).
     */
    protected BgeeUUID(String fromString, UUIDSource source, long lastUpdate) {
        //cannot use this(UUID, String, UUIDSource) directly, to get rid of the UnsupportedEncodingException 
        //and IllegalArgumentException.
        //OK, first, try to see if fromString is a real UUID
        UUID tmpUUID = null;
        try {
            tmpUUID = UUID.fromString(fromString);
        } catch (IllegalArgumentException e1) {
            log.catching(Level.TRACE, e1);
            
            //Not a real UUID
            try {
                tmpUUID = UUID.nameUUIDFromBytes(fromString.getBytes(CHARSET));
            } catch (UnsupportedEncodingException e2) {
                //we get this exception solely because we enforce UTF-8 character encoding, 
                //should never happen, so it's safe to rethrow an IllegalArgumentException I think.
                log.catching(e2);
                throw log.throwing(new IllegalArgumentException(e2));
            }
        }
        
        this.uuid = tmpUUID;
        this.fromString = fromString;
        this.source = source;
        this.lastUpdateTimestamp = lastUpdate;
    }
    /**
     * Construct a new {@code BgeeUUID} by providing the UUID and the {@code String} used to produce it 
     * and the source where the UUID was retrieved from.
     * 
     * @param uuid          The {@code UUID} of this {@code BgeeUUID}.
     * @param fromString    The {@code String} that was used to produce the UUID.
     * @param source        The {@code UUIDSource} where the UUID was retrieved from.
     * @param lastUpdate    A {@code long} that is the timestamp of creation date or last update 
     *                      of this {@code UUID}.
     *                      If less than or equal to 0, means that this information 
     *                      is irrelevant (e.g., for API keys, they are currently auto-generated 
     *                      by the user so that we don't know when it was created).
     */
    protected BgeeUUID(UUID uuid, String fromString, UUIDSource source, long lastUpdate) {
        this.uuid = uuid;
        this.fromString = fromString;
        this.source = source;
        this.lastUpdateTimestamp = lastUpdate;
    }

    
    //*****************************
    // getters
    //*****************************
    /**
     * @return  The {@code UUID} held by this {@code BgeeUUID}.
     */
    public UUID getUUID() {
        return uuid;
    }
    /**
     * @return  The {@code String} that was used to produce the UUID.
     */
    public String getFromString() {
        return fromString;
    }
    /**
     * @return  The {@code UUIDSource} where the UUID was retrieved from.
     */
    public UUIDSource getUUIDSource() {
        return source;
    }
    /**
     * @return  A {@code long} corresponding to the timestamp of creation date or last update 
     *          of this {@code UUID}. If less than or equal to 0, means that this information 
     *          is irrelevant (e.g., for API keys, they are currently auto-generated by the user 
     *          so that we don't know when it was created).
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    //*****************************
    // equals/hashCode/toString
    //*****************************
    @Override
    public int hashCode() {
        //solely based on UUID
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        //solely based on UUID
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BgeeUUID other = (BgeeUUID) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BgeeUUID [uuid=").append(uuid)
               .append(", source=").append(source)
               .append(", lastUpdate=").append(lastUpdateTimestamp)
               .append("]");
        return builder.toString();
    }
}
