package org.bgee.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * A class allowing for entities to be post-composed, for instance, for describing
 * the post-composition of a cell type and of an anatomical structure.
 * When there is only one {@code Entity} in an object of this class,
 * it simply means that the term is not post-composed.
 * <p>
 * For convenience, this class is itself an {@code Entity}.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Dec. 2022
 * @since Bgee 15.0, Dec. 2022
 *
 * @param <T>   The type of {@code Entity} used in this post-composition.
 */
public abstract class ComposedEntity<T extends Entity<?>> extends Entity<String> {
    /**
     * A {@code String} used a separator between the IDs of the {@code Entity}s part of
     * a {@code ComposedEntity}. Used to generate a sort of ID for {@code ComposedEntity}s.
     * As a result, this ID is always a {@code String}, and the IDs of the individual
     * {@code Entity}s are converted using their {@code toString} method.
     */
    public static final String ENTITY_ID_SEPARATOR = "_";

    private final LinkedHashSet<T> entities;
    private final Class<T> entityType;

    protected ComposedEntity(LinkedHashSet<T> entities, Class<T> entityType) {
        //create an "ID"
        super(entities == null? "": entities.stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.joining(ENTITY_ID_SEPARATOR)));

        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("The provided entities cannot be null nor empty");
        }
        if (StringUtils.isBlank(this.getId())) {
            throw new IllegalArgumentException("The provided entities do not allow to produce an ID");
        }
        if (entityType == null) {
            throw new IllegalArgumentException("The entity type cannot be null");
        }
        //We will use defensive copying, there is no unmodifiableLinkedHashSet
        this.entities = new LinkedHashSet<>(entities);
        this.entityType = entityType;
    }

    /**
     * @return  A {@code LinkedHashSet} of the {@code Entity}s that are post-composed
     *          to create this {@code ComposedEntity}. The order is important,
     *          as in the case of the post-composition of a cell type and an anat. entity,
     *          to describe a condition as "cell type <i>in</i> anat. entity".
     *          The returned {@code LinkedHashSet} can be safely modified
     *          (defensive copying).
     */
    public LinkedHashSet<T> getEntities() {
        //defensive copying, there is no unmodifiableLinkedHashSet
        return new LinkedHashSet<>(entities);
    }
    public T getFirstEntity() {
        return entities.iterator().next();
    }
    public boolean isComposed() {
        return entities.size() > 1;
    }
    public Class<T> getEntityType() {
        return entityType;
    }

    //We reimplement hashCode(equals even though this class is also an Entity,
    //I don't like the idea of making the comparisons on a generated String ID.
    @Override
    public int hashCode() {
        return Objects.hash(entities);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComposedEntity<?> other = (ComposedEntity<?>) obj;
        return Objects.equals(entities, other.entities);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ComposedEntity [")
               .append("entityType=").append(entityType)
               .append(", entities=").append(entities)
               .append("]");
        return builder.toString();
    }
}
