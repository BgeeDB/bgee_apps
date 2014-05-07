package org.bgee.model.dao.api.gene;

import org.bgee.model.dao.api.DAO;

/**
 * DAO defining queries using or retrieving {@link GeneTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see GeneTO
 * @since Bgee 13
 */
public interface GeneDAO extends DAO<GeneDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneTO}s 
     * obtained from this {@code GeneDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link GeneTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link GeneTO#getName()}.
     * <li>{@code DOMAIN}: corresponds to {@link GeneTO#getDomain()}.
     * <li>{@code SPECIESID}: corresponds to {@link GeneTO#getSpeciesId()}.
     * <li>{@code GENEBIOTYPEID}: corresponds to {@link GeneTO#getGeneBioTypeId()}.
     * <li>{@code OMANODEID}: corresponds to {@link GeneTO#getOMANodeId()}.
     * <li>{@code ENSEMBLGENE}: corresponds to {@link GeneTO#isEnsemblGene()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Collection)
     * @see org.bgee.model.dao.api.DAO.setAttributesToGet(Object[])
     * @see org.bgee.model.dao.api.DAO.clearAttributesToGet()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, SPECIESID, GENEBIOTYPEID, OMANODEID, ENSEMBLGENE;
    }
}
