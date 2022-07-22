package org.bgee.model.ontology;

import java.util.Collection;

import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;

/**
 * This class is needed to manage discrepancies in upper/lowercase of strain IDs
 * (that are just strain names really, not standardized as well as we would need)
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since Bgee 15.0, Apr. 2021
 */
public class StrainOntology extends Ontology<Strain, String> {

    public StrainOntology(Integer speciesId, Collection<Strain> elements,
            Collection<RelationTO<String>> relations,
            Collection<RelationType> relationTypes, ServiceFactory serviceFactory,
            Class<Strain> type) {
        super(speciesId, elements, relations, relationTypes, serviceFactory, type);
    }

    @Override
    public Strain getElement(String id) {
        if (id == null) {
            return null;
        }
        Strain firstTry = this.elements.get(id);
        if (firstTry != null) {
            return firstTry;
        }
        return this.elements.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(id))
                .findAny()
                .map(e -> e.getValue())
                .orElse(null);
    }
}
