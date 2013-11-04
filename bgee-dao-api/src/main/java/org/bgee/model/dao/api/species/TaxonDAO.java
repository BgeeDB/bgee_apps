package org.bgee.model.dao.api.species;

import org.bgee.model.dao.api.DAO;

/**
 * DAO defining queries using or retrieving {@link TaxonTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see TaxonTO
 * @since Bgee 13
 */
public interface TaxonDAO extends DAO<TaxonDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code TaxonTO}s 
     * obtained from this {@code TaxonDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link TaxonTO#getId()}.
     * <li>{@code COMMONNAME}: corresponds to {@link TaxonTO#getName()}.
     * <li>{@code SCIENTIFICNAME}: corresponds to {@link TaxonTO#getScientificName()}.
     * <li>{@code LEFTBOUND}: corresponds to {@link TaxonTO#getLeftBound()}.
     * <li>{@code RIGHTBOUND}: corresponds to {@link TaxonTO#getRightBound()}.
     * <li>{@code LEVEL}: corresponds to {@link TaxonTO#getLevel()}.
     * <li>{@code LCA}: corresponds to {@link TaxonTO#isLca()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, COMMONNAME, SCIENTIFICNAME, LEFTBOUND, RIGHTBOUND, LEVEL, LCA;
    }
}
