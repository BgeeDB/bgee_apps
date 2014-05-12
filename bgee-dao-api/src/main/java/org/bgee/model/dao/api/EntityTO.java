package org.bgee.model.dao.api;

import org.apache.commons.lang3.StringUtils;

/**
 * Parent class of all {@code TransferObject}s that are "Entities" in Bgee.
 * <p>
 * {@code TransferObject}s should be immutable. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class EntityTO implements TransferObject {
	private static final long serialVersionUID = 9170289303150839721L;
	private final String id;
    private final String name;
    private final String description;
    
    /**
     * Default constructor private, an {@code EntityTO} must be provided with 
     * an ID, see {@link #EntityTO(String)}.
     */
    @SuppressWarnings("unused")
    private EntityTO() {
        this(null);
    }
    /**
     * Constructor providing the ID of this {@code Entity}.
     * @param id    A {@code String} that is the ID. Cannot be null nor empty, 
     *              otherwise an {@code IllegalArgumentException} is thrown.
     * @throws IllegalArgumentException If {@code id} is null or empty.
     */
    protected EntityTO(String id) throws IllegalArgumentException {
        this(id, null, null);
    }
    /**
     * Constructor providing the ID and name of this {@code Entity}.
     * @param id            A {@code String} that is the ID.
     * @param name          A {@code String} that is the name. Can be {@code null}
     *                      or empty.
     * @throws IllegalArgumentException If {@code id} is null or empty.
     */
    protected EntityTO(String id, String name) throws IllegalArgumentException {
        this(id, name, null);
    }
    /**
     * Constructor providing the ID, name and description of this {@code Entity}.
     * @param id            A {@code String} that is the ID.
     * @param name          A {@code String} that is the name. Can be {@code null}
     *                      or empty.
     * @param description   A {@code String} that is the description. Can be {@code null}
     *                      or empty.
     * @throws IllegalArgumentException If {@code id} is null or empty.
     */
    protected EntityTO(String id, String name, String description) 
            throws IllegalArgumentException {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("ID cannot be null nor empty.");
        }
        this.id          = id;
        this.name        = name;
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityTO other = (EntityTO) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
