package org.bgee.model.dao.api.species;

import org.bgee.model.dao.api.DAO;

/**
 * DAO defining queries using or retrieving {@link SpeciesTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @see SpeciesTO
 * @since Bgee 01
 */
public interface SpeciesDAO extends DAO<SpeciesDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code SpeciesTO}s 
     * obtained from this {@code SpeciesDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link SpeciesTO#getId()}.
     * <li>{@code COMMONNAME}: corresponds to {@link SpeciesTO#getName()}.
     * <li>{@code GENUS}: corresponds to {@link SpeciesTO#getGenus()}.
     * <li>{@code SPECIES}: corresponds to {@link SpeciesTO#getSpecies()}.
     * <li>{@code PARENTTAXONID}: corresponds to {@link SpeciesTO#getParentTaxonId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, COMMONNAME, GENUS, SPECIES, PARENTTAXONID;
    }
}
