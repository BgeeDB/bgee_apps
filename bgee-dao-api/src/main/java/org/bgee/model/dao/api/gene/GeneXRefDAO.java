package org.bgee.model.dao.api.gene;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link GeneXRefTO}s. 
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Apr. 2016
 * @see GeneXRefTO
 */
public interface GeneXRefDAO extends DAO<GeneXRefDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code GeneXRefTO}s 
     * obtained from this {@code GeneXRefDAO}.
     * <ul>
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link GeneXRefTO#getBgeeGeneId()}.
     * <li>{@code XREF_ID}: corresponds to {@link GeneXRefTO#getXRefId()}.
     * <li>{@code XREF_NAME}: corresponds to {@link GeneXRefTO#getXRefName()}.
     * <li>{@code DATA_SOURCE_ID}: corresponds to {@link GeneXRefTO#getDataSourceId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        BGEE_GENE_ID, XREF_ID, XREF_NAME, DATA_SOURCE_ID;
    }

    /**
     * Retrieve cross-references of all genes from data source.
     * <p>
     * The cross-references are retrieved and returned as a {@code GeneXRefTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} 
     * once results are retrieved.
     * 
     * @param attributes    A {@code Collection} of {@code GeneXRefDAO.Attribute}s defining 
     *                      the attributes to populate in the returned {@code GeneXRefTO}s.
     *                      If {@code null} or empty, all attributes are populated.
     * @return              A {@code GeneXRefTOResultSet} containing all gene cross-references
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneXRefTOResultSet getAllGeneXRefs(Collection<GeneXRefDAO.Attribute> attributes)
            throws DAOException;
    
    /**
     * Retrieve cross-references from Bgee gene IDs.
     * <p>
     * The cross-references are retrieved and returned as a {@code GeneXRefTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} 
     * once results are retrieved.
     *
     * @param bgeeGeneIds   A {@code Collection} of {@code Integer}s that are the Bgee IDs 
     *                      of the genes to retrieve cross-references for.
     * @param attributes    A {@code Collection} of {@code GeneXRefDAO.Attribute}s defining 
     *                      the attributes to populate in the returned {@code GeneXRefTO}s.
     *                      If {@code null} or empty, all attributes are populated.
     * @return              A {@code GeneXRefTOResultSet} containing gene cross-references
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneXRefTOResultSet getGeneXRefsByBgeeGeneIds(Collection<Integer> bgeeGeneIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException;

    /**
     * Retrieve cross-references from cross-reference IDs.
     * <p>
     * The cross-references are retrieved and returned as a {@code GeneXRefTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} 
     * once results are retrieved.
     * 
     * @param xRefIds       A {@code Collection} of {@code String}s that are the IDs of 
     *                      cross-references allowing to filter the cross-references to retrieve.
     * @param attributes    A {@code Collection} of {@code GeneXRefDAO.Attribute}s defining 
     *                      the attributes to populate in the returned {@code GeneXRefTO}s.
     *                      If {@code null} or empty, all attributes are populated.
     * @return              A {@code GeneXRefTOResultSet} containing gene cross-references
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneXRefTOResultSet getGeneXRefsByXRefIds(Collection<String> xRefIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException;
    
    /**
     * Retrieve cross-references from data source.
     * <p>
     * The cross-references are retrieved and returned as a {@code GeneXRefTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} 
     * once results are retrieved.
     *
     * @param bgeeGeneIds   A {@code Collection} of {@code Integer}s that are the Bgee IDs 
     *                      of the genes to retrieve cross-references for.
     * @param xRefIds       A {@code Collection} of {@code String}s that are the IDs of 
     *                      cross-references allowing to filter the cross-references to retrieve.
     * @param dataSourceIds A {@code Collection} of {@code Integer}s that are the IDs of 
     *                      the data source the cross-reference comes from allowing to filter
     *                      the cross-references to retrieve.
     * @param attributes    A {@code Collection} of {@code GeneXRefDAO.Attribute}s defining 
     *                      the attributes to populate in the returned {@code GeneXRefTO}s.
     *                      If {@code null} or empty, all attributes are populated.
     * @return              A {@code GeneXRefTOResultSet} containing gene cross-references
     *                      from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public GeneXRefTOResultSet getGeneXRefs(Collection<Integer> bgeeGeneIds,
            Collection<String> xRefIds, Collection<Integer> dataSourceIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException;

    public GeneXRefTOResultSet getGeneXRefs(Collection<String> geneIds, Collection<Integer> speciesIds,
            Collection<String> xRefIds, Collection<Integer> dataSourceIds,
            Collection<GeneXRefDAO.Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} specifics to {@code GeneXRefTO}s
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13, Apr. 2016
     * @since   Bgee 13, Apr. 2016
     */
    public interface GeneXRefTOResultSet extends DAOResultSet<GeneXRefTO> {
        
    }

    /**
     * {@code EntityTO} representing a cross-reference of a gene in the Bgee database.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Apr. 2019
     * @since   Bgee 13, Apr. 2016
     */
    public class GeneXRefTO extends TransferObject {

        private static final long serialVersionUID = -6883965009968486597L;
        
        /**
         * An {@code Integer} that is the Bgee ID of the gene.
         */
        private final Integer bgeeGeneId;

        /**
         * A {@code String} that is the ID of the cross-reference.
         */
        private final String xRefId;

        /**
         * A {@code String} that is the name of the cross-reference.
         */
        private final String xRefName;

        /**
         * An {@code Integer} that is the ID of the data source the cross-reference comes from.
         */
        private final Integer dataSourceId;

        /**
         * Constructor providing the ID of the gene (for instance, {@code ENSMUSG00000038253}), 
         * the ID of the cross-references (for instance, {@code P09021}), the name of the 
         * cross-references, and the ID of the data source the cross-reference comes from.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * Other attributes are set to {@code null}.
         *
         * @param bgeeGeneId    An {@code Integer} that is the Bgee ID of the gene.
         * @param xRefId        A {@code String} that is the ID of the cross-reference.
         * @param xRefName      A {@code String} that is the name of the cross-reference.
         * @param dataSourceId  An {@code Integer} that is the ID of the data source
         *                      the cross-reference comes from.
         */
        public GeneXRefTO(Integer bgeeGeneId, String xRefId, String xRefName, Integer dataSourceId) {
            this.bgeeGeneId = bgeeGeneId;
            this.xRefId = xRefId;
            this.xRefName = xRefName;
            this.dataSourceId = dataSourceId;

        }

        /**
         * @return  The {@code Integer} that is the Bgee ID of the gene.
         */
        public Integer getBgeeGeneId() {
            return bgeeGeneId;
        }

        /**
         * @return  The {@code String} that is the ID of the cross-reference.
         */
        public String getXRefId() {
            return xRefId;
        }

        /**
         * @return  The {@code String} that is the name of the cross-reference.
         */
        public String getXRefName() {
            return xRefName;
        }

        /**
         * @return  The {@code Integer} that is the ID of the data source 
         *          the cross-reference comes from.
         */
        public Integer getDataSourceId() {
            return dataSourceId;
        }

        @Override
        public String toString() {
            return "Bgee gene ID: " + this.getBgeeGeneId() + " - X-Ref ID: " + this.getXRefId() +
                    " - X-Ref name: " + this.getXRefName() + 
                    " - Data source ID: " + this.getDataSourceId();
        }
    }
}
