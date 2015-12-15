package org.bgee.model.ontology;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Entity;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

/**
 * Class allowing to describe ontology. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Dec. 2013
 * @since   Bgee 13, Dec. 2013
 * @param <T>
 */
public class Ontology<T extends Entity & OntologyElement<T>> {

    private static final Logger log = LogManager.getLogger(Ontology.class.getName());

    /**
     * A {@code Collection} of {@code T}s that are the elements of the ontology.
     */
    private Set<T> entities;

    /**
     * A {@code Collection} of {@code RelationTO}s that are the relations
     * between members of the ontology.
     */
    private Set<RelationTO> relations;

    /**
     * Constructor providing the {@code entities} and the {relations} of this {@code Ontology}.
     * 
     * @param entities  A {@code Collection} of {@code T}s that are the elements of the ontology. 
     * @param relations A {@code Collection} of {@code RelationTO}s that are the relations
     *                  between members of the ontology.
     */
    protected Ontology(Collection<T> entities, Collection<RelationTO> relations) {
        // TODO add sanity checks? entities and relations can be null?
        this.entities = Collections.unmodifiableSet(
                entities == null? new HashSet<>(): new HashSet<>(entities));
        this.relations = Collections.unmodifiableSet(
                relations == null? new HashSet<>(): new HashSet<>(relations));
    }

    /**
     * TODO
     * 
     * @param childrenFromParents   A {@code boolean} defining whether the returned {@code Map} 
     *                              will associate a source to its targets, or a target 
     *                              to its sources. If {@code true}, it will associate 
     *                              a source to its targets.
     * @return                      A {@code Map} where keys are {@code String}s representing the  
     *                              IDs of either the sources or the targets of relations, the  
     *                              associated value being {@code Set} of {@code String}s that are  
     *                              the IDs of either the associated targets, or sources,  
     *                              respectively. If {@code targetsBySource} is {@code true}, 
     *                              it will associate sources to their targets. 
     */
    // TODO DRY in BgeeUtils.getIsAPartOfRelativesFromDb()
    private Map<T, Set<T>> getRelatives(boolean childrenFromParents, Set<RelationType> relationType) {
        log.entry(childrenFromParents, relationType);

        Map<T, Set<T>> relativesMap = new HashMap<>();

        for (RelationTO relTO: this.relations) {
            // TODO add sanity checks relationType && relTO.getRelationType()
            if (!relationType.contains(relTO.getRelationType())) {
                continue;
            }
            T key = null;
            T value = null;
            if (childrenFromParents) {
                key = this.getEntity(relTO.getTargetId());
                value = this.getEntity(relTO.getSourceId());
            } else {
                key = this.getEntity(relTO.getSourceId());
                value = this.getEntity(relTO.getTargetId());
            }
            // TODO add sanity checks (if key and/or value are null)
            Set<T> relatives = relativesMap.get(key);
            if (relatives == null) {
                relatives = new HashSet<>();
                relativesMap.put(key, relatives);
            }
            relatives.add(value);
        }
        return log.exit(relativesMap);
    }

    private T getEntity(String id) {
        for (T t : entities) {
            if (t.getId().equals(id))
                return t;
        }
        return null;
    }

    public Set<T> getAncestors(T child, Set<RelationType> relationType) throws DAOException {
        log.entry(child);
        // TODO add sanity checks
        return log.exit(getRelatives(false, relationType).get(child));
    }

    public Set<T> getDescendants(T parent, Set<RelationType> relationType) throws DAOException {
        log.entry(parent);
        // TODO add sanity checks
        return log.exit(getRelatives(true, relationType).get(parent));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entities == null) ? 0 : entities.hashCode());
        result = prime * result + ((relations == null) ? 0 : relations.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ontology<?> other = (Ontology<?>) obj;
        if (entities == null) {
            if (other.entities != null)
                return false;
        } else if (!entities.equals(other.entities))
            return false;
        if (relations == null) {
            if (other.relations != null)
                return false;
        } else if (!relations.equals(other.relations))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Entities: " + entities + " - Relations: " + relations;
    } 
}
