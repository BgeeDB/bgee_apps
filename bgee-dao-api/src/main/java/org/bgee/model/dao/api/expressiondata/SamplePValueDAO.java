package org.bgee.model.dao.api.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SamplePValueTO}s. 
 * 
 * @author  Frederic Bastian
 * @version Bgee 15.0, Mar 2021
 * @since   Bgee 15.0, Mar 2021
 */
public interface SamplePValueDAO extends DAO<SamplePValueDAO.Attribute> {
    /**
     * The attributes available for {@code ExperimentExpressionTO}
     * <ul>
     *   <li>@{code EXPRESSION_ID} corresponds to {@link SamplePValueTO#getExpressionId()}
     *   <li>@{code EXPERIMENT_ID} corresponds to {@link SamplePValueTO#getExperimentId()}
     *   <li>@{code SAMPLE_ID} corresponds to {@link SamplePValueTO#getSampleId()}}
     *   <li>@{code P_VALUE} corresponds to {@link SamplePValueTO#getPValue()}
     * </ul>
     */
    enum Attribute implements DAO.Attribute {
        EXPRESSION_ID, EXPERIMENT_ID, SAMPLE_ID, P_VALUE;
    }

    /**
     * Retrieve affymetrix p-values from the data source, linked to expression IDs
     * of the raw expression table, and bgee internal affymetrix chip ID,
     * and ordered by gene ID and expression ID of raw expression table.
     * <p>
     * The data are retrieved and returned as an {@code SamplePValueTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
     *                              of the genes to retrieve Affymetrix p-values for.
     * @return                      An {@code SamplePValueTOResultSet} allowing to obtain 
     *                              the requested {@code SamplePValueTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    //If we retrieve the experiment ID, the public chip ID is a String and is unique in an experiment.
    //If we don't retrieve the experiment ID, the Bgee internal chip ID is an int and is unique
    //over the whole Bgee database, it's why we define the sample ID type as Integer.
    public SamplePValueTOResultSet<String, Integer> getAffymetrixPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException;

    /**
     * Retrieve RNA-Seq p-values from the data source, linked to expression IDs
     * of the raw expression table, and RNA-Seq experiment and library IDs,
     * and ordered by gene ID and expression ID of raw expression table.
     * <p>
     * The data are retrieved and returned as an {@code SamplePValueTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
     *                              of the genes to retrieve RNA-Seq p-values for.
     * @return                      An {@code SamplePValueTOResultSet} allowing to obtain 
     *                              the requested {@code SamplePValueTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    public SamplePValueTOResultSet<String, String> getRNASeqPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException;

    /**
     * Retrieve <i>in situ</i> hybridization p-values from the data source, linked to expression IDs
     * of the raw expression table, and <i>in situ</i> evidence and spot IDs,
     * and ordered by gene ID and expression ID of raw expression table.
     * <p>
     * The data are retrieved and returned as an {@code SamplePValueTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
     *                              of the genes to retrieve <i>in situ</i> hybridization p-values for.
     * @return                      An {@code SamplePValueTOResultSet} allowing to obtain 
     *                              the requested {@code SamplePValueTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    public SamplePValueTOResultSet<String, String> getInSituPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException;

    /**
     * Retrieve EST p-values from the data source, linked to expression IDs
     * of the raw expression table, and EST library IDs,
     * and ordered by gene ID and expression ID of raw expression table.
     * <p>
     * The data are retrieved and returned as an {@code SamplePValueTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
     *                              of the genes to retrieve EST p-values for.
     * @return                      An {@code SamplePValueTOResultSet} allowing to obtain 
     *                              the requested {@code SamplePValueTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    //There is no experiment ID for EST data, only library IDs, that will be populated
    //in the returned SamplePValueTOs as 'sampleId'.
    public SamplePValueTOResultSet<String, String> getESTPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException;

    /**
     * Retrieve single-cell RNA-Seq full lenth p-values from the data source, linked to expression IDs
     * of the raw expression table, and experiment and library IDs,
     * and ordered by gene ID and expression ID of raw expression table.
     * <p>
     * The data are retrieved and returned as an {@code SamplePValueTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param geneIds               A {@code Collection of {@code Integer}s that are the Bgee IDs 
     *                              of the genes to retrieve scRNA-Seq full lenth p-values for.
     * @return                      An {@code SamplePValueTOResultSet} allowing to obtain 
     *                              the requested {@code SamplePValueTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    public SamplePValueTOResultSet<String, String> getscRNASeqFullLengthPValuesOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code SamplePValueTO}s
     *
     * @param <T>   The type of experiment ID in the returned {@code SamplePValueTO}s.
     * @param <U>   The type of sample ID in the returned {@code SamplePValueTO}s.
     * @author  Frederic Bastian
     * @version Bgee 15.0, Mar. 2021
     * @since   Bgee 15.0, Mar. 2021
     */
    public interface SamplePValueTOResultSet<T, U> extends DAOResultSet<SamplePValueTO<T, U>> {
    }

    /**
     * {@code TransferObject} representing the pvalue of a test to determine active signal of expression
     * from a sample for a gene in the Bgee database.
     *
     * @param <T>   The type of experiment ID
     * @param <U>   The type of sample ID
     * @author  Frederic Bastian
     * @version Bgee 15.0, Mar 2021
     * @since   Bgee 15.0, Mar 2021
     */
    public class SamplePValueTO<T, U> extends TransferObject {
        private static final long serialVersionUID = -84474173001782925L;

        private final Integer expressionId;
        private final T experimentId;
        private final U sampleId;
        private final BigDecimal pValue;

        /**
         * @param expressionId  An {@code Integer} that is the expression ID associated to this pvalue.
         * @param experimentId  A {@code T} that is the experiment ID of the sample used to determine
         *                      active signal of expression of genes.
         * @param sampleId      A {@code U} that is the ID of the sample used to determine active signal
         *                      of expression of genes.
         * @param pValue        A {@code BigDecimal} representing the pvalue of the test to determine
         *                      active signal of expression in the sample, for a gene in a condition
         *                      (represented by {@code expressionId}).
         */
        public SamplePValueTO(Integer expressionId, T experimentId, U sampleId, BigDecimal pValue) {
            super();
            this.expressionId = expressionId;
            this.experimentId = experimentId;
            this.sampleId = sampleId;
            this.pValue = pValue;
        }

        /**
         * @return  An {@code Integer} that is the expression ID associated to this pvalue.
         *          In Bgee, expression IDs associate a gene with a condition,
         *          for which expression tests were performed.
         */
        public Integer getExpressionId() {
            return expressionId;
        }
        /**
         * @return  A {@code T} that is the experiment ID of the sample used to determine active signal
         *          of expression of genes.
         */
        public T getExperimentId() {
            return experimentId;
        }
        /**
         * @return  A {@code U} that is the ID of the sample used to determine active signal
         *          of expression of genes.
         */
        public U getSampleId() {
            return sampleId;
        }
        /**
         * @return  A {@code BigDecimal} representing the pvalue of the test to determine active signal
         *          of expression in the sample, for a gene in a condition (represented by
         *          {@code expressionId}).
         */
        public BigDecimal getpValue() {
            return pValue;
        }
    }
}