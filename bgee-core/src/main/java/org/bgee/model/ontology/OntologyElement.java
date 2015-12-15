package org.bgee.model.ontology;

import java.util.Set;

import org.bgee.model.Entity;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;

public interface OntologyElement<T extends Entity & OntologyElement<T>> {

    default public Set<T> getAncestors(Ontology<T> ontology, T child, Set<RelationType> relationType) {
        return ontology.getAncestors(child, relationType);
    }
    default public Set<T> getDescendants(Ontology<T> ontology, T parent, Set<RelationType> relationType) {
        return ontology.getAncestors(parent, relationType);
    }
}
