package org.bgee.model.dao.api.gene;

import org.bgee.model.dao.api.DAO;

/**
 * DAO defining queries using or retrieving {@link GOTermTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see GOTermTO
 * @since Bgee 13
 */
public interface GeneOntologyDAO extends DAO<GeneOntologyDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GOTermTO}s 
     * obtained from this {@code GeneOntologyDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link GOTermTO#getId()}.
     * <li>{@code LABEL}: corresponds to {@link GOTermTO#getName()}.
     * <li>{@code DOMAIN}: corresponds to {@link GOTermTO#getDomain()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, LABEL, DOMAIN;
    }
}
