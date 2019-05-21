package org.bgee.model.dao.api;

/**
 * Represents an entity that can be named, and that also often has a description.
 * An example of the difference between a {@code NamedEntityTO} and an {@code EntityTO}  
 * could be for instance between a {@code GeneTO} (genes have an ID, a name, a description) 
 * and a {@code CallTO} (calls only have an ID, used to reference them).
 * 
 * @author Frederic Bastian
 * @version Bgee 14 Feb. 2017
 * @since Bgee 14 Feb. 2017
 * 
 * @param <T> The type of ID of this {@code NamedEntityTO}.
 */
public class NamedEntityTO<T extends Comparable<T>> extends EntityTO<T> {
    private static final long serialVersionUID = 8075426392002069067L;
    
    private final String name;
    private final String description;


    /**
     * Constructor providing the ID and name of this {@code NamedEntity}.
     * 
     * @param id            A {@code T} that is the ID Can be {@code null}
     *                      or empty.
     * @param name          A {@code String} that is the name. Can be {@code null}
     *                      or empty.
     */
    protected NamedEntityTO(T id, String name) {
        this(id, name, null);
    }
    /**
     * Constructor providing the ID, name and description of this {@code NamedEntity}.
     * 
     * @param id            A {@code T} that is the ID. Can be {@code null}
     *                      or empty.
     * @param name          A {@code String} that is the name. Can be {@code null}
     *                      or empty.
     * @param description   A {@code String} that is the description. Can be {@code null}
     *                      or empty.
     */
    protected NamedEntityTO(T id, String name, String description) {
        super(id);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NamedEntityTO [id=").append(getId()).append(", name=").append(name)
               .append(", description=").append(description).append("]");
        return builder.toString();
    }
}
