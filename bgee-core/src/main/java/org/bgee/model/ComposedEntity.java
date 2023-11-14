package org.bgee.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
public class ComposedEntity<T extends NamedEntity<?>> extends Entity<String> {
    /**
     * A {@code String} used a separator between the IDs of the {@code Entity}s part of
     * a {@code ComposedEntity}. Used to generate a sort of ID for {@code ComposedEntity}s.
     * As a result, this ID is always a {@code String}, and the IDs of the individual
     * {@code Entity}s are converted using their {@code toString} method.
     */
    public static final String ENTITY_ID_SEPARATOR = "_";

    private final LinkedHashSet<T> entities;
    private final Class<T> entityType;

    public ComposedEntity(Class<T> entityType) {
        this((T) null, entityType);
    }
    public ComposedEntity(T entity, Class<T> entityType) {
        this(entity == null? null: new LinkedHashSet<>(Set.of(entity)), entityType);
    }
    public ComposedEntity(LinkedHashSet<T> entities, Class<T> entityType) {
        //create an "ID"
        super(entities == null || entities.isEmpty()? "": entities.stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.joining(ENTITY_ID_SEPARATOR)));

        if (entityType == null) {
            throw new IllegalArgumentException("The entity type cannot be null");
        }
        //We will use defensive copying, there is no unmodifiableLinkedHashSet
        this.entities = entities == null? new LinkedHashSet<>(): new LinkedHashSet<>(entities);
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
    /**
     * Returns the {@code T} at the index provided,
     * from the {@code List} returns by {@link #getEntities()}.
     * Unlike the method {@code List.get(int)}, this method returns {@code null}
     * if the index is out of bond, instead of throwing an {@code IndexOutOfBoundsException}.
     *
     * @param index The {@code int} that is the index of the {@code T} to return.
     * @return      The {@code T} at the specified position
     *              in the composed entity list.
     */
    public T getEntity(int index) {
        return entities.stream().skip(index).findFirst().orElse(null);
    }
    public boolean isComposed() {
        return entities.size() > 1;
    }
    public int size() {
        return entities.size();
    }
    public boolean isEmpty() {
        return entities.isEmpty();
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
