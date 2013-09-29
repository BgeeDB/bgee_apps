package org.bgee.model.dao.api;

/**
 * Parent class of all {@code TransferObject}s that are "Entities" in Bgee.
 * <p>
 * {@code TransferObject}s are immutable, but for the sake of simplicity, 
 * we do not use a {@code Builder} pattern as there is only 3 attributes 
 * in this class, and 2 constructors. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class EntityTO extends TransferObject {
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
     * @throws IllegalArgumentException IF {@code id} is null or empty.
     */
    protected EntityTO(String id) {
        this(id, null, null);
    }
    /**
     * Constructor providing the ID, name and description of this {@code Entity}.
     * @param id            A {@code String} that is the ID.
     * @param name          A {@code String} that is the name. Can be {@code null}
     *                      or empty.
     * @param description   A {@code String} that is the description. Can be {@code null}
     *                      or empty.
     */
    protected EntityTO(String id, String name, String description) {
        if (id == null || id.trim().length() == 0) {
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
}
