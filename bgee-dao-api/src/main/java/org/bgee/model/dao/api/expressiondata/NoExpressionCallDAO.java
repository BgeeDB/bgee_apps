package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;

/**
 * DAO defining queries using or retrieving {@link NoExpressionCallDAO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version 
 * @since 
 */
public interface NoExpressionCallDAO extends DAO<NoExpressionCallDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code NoExpressionCallTO}s 
     * obtained from this {@code NoExpressionCallDAO}.
     * <ul>
     * <li>{@code ID: corresponds to {@link CallTO#getId()}.
     * <li>{@code GENEID: corresponds to {@link CallTO#getGeneId()}.
     * <li>{@code DEVSTAGEID: corresponds to {@link CallTO#getDevStageId()}.
     * <li>{@code ANATENTITYID: corresponds to {@link CallTO#getAnatEntityId()}.
     * <li>{@code AFFYMETRIXDATA: corresponds to {@link CallTO#getAffymetrixData()}.
     * <li>{@code INSITUDATA: corresponds to {@link CallTO#getInSituData()}.
     * <li>{@code RNASEQDATA;: corresponds to {@link CallTO#getRNASeqData()}.
     * <li>{@code INCLUDEPARENTSTRUCTURES}: corresponds to 
     * {@link NoExpressionCallTO#isIncludeParentStructures()}.
     * <li>{@code ORIGINOFLINE}: corresponds to {@link NoExpressionCallTO#getOriginOfLine()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENEID, DEVSTAGEID, ANATENTITYID, AFFYMETRIXDATA, INSITUDATA, RNASEQDATA,
        INCLUDEPARENTSTRUCTURES, ORIGINOFLINE;
    }

    /**
     * Retrieve all no-expression calls from data source according {@code NoExpressionCallParams}.
     * <p>
     * The no-expression calls are retrieved and returned as a {@code NoExpressionCallTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param params  An {@code NoExpressionCallParams} that provide the parameters specific 
     *                to no-expression calls
     * @return        An {@code NoExpressionCallTOResultSet} containing all no-expression calls 
     *                from data source.
     */
    public NoExpressionCallTOResultSet getAllNoExpressionCalls(NoExpressionCallParams params);
    
    /**
     * Inserts the provided no-expression calls into the Bgee database, 
     * represented as a {@code Collection} of {@code NoExpressionCallTO}s. 
     * 
     * @param noExpressionCalls A {@code Collection} of {@code NoExpressionCallTO}s 
     *                          to be inserted into the database.
     * @return                  An {@code int} that is the number of inserted no-expression calls.
     */
    public int insertNoExpressionCalls(Collection<NoExpressionCallTO> noExpressionCalls);

    /**
     * Inserts the provided correspondence between global no-expression and no-expression IDs into 
     * the Bgee database, represented as a {@code Collection} of 
     * {@code GlobalNoExpressionToNoExpressionTO}s. 
     * 
     * @param globalNoExpressionToNoExpression  A {@code Collection} of 
     *                                          {@code globalNoExpressionToNoExpressionTO}s to be 
     *                                          inserted into the database.
     * @return                                  An {@code int} that is the number of inserted 
     *                                          correspondences.
     */
    public int insertGlobalNoExpressionToNoExpression(
            Collection<GlobalNoExpressionToNoExpressionTO> globalNoExpressionToNoExpression);

    /**
     * {@code DAOResultSet} specifics to {@code NoExpressionCallTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface NoExpressionCallTOResultSet extends DAOResultSet<NoExpressionCallTO> {
        
    }

    /**
     * A {@code CallTO} specific to no-expression calls (explicit report of absence 
     * of expression). Their specificity is that they can be produced using data propagation 
     * from parent anatomical entities by <em>is_a</em> or <em>part_of</em> relations. 
     * See {@link #isIncludeParentStructures()} for more details.
     * <p>
     * Of note, there is no data propagation from developmental stages for no-expression 
     * calls.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class NoExpressionCallTO extends CallTO {
        // TODO modify the class to be immutable.
        private static final long serialVersionUID = 5793434647776540L;
        
        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(NoExpressionCallTO.class.getName());

        /**
         * A {@code boolean} defining whether this no-expression call was generated 
         * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
         * relations, even indirect. If {@code true}, all its parents were considered. 
         * So for instance, if B is_a A, and absence of expression has been reported in A, 
         * then B could benefit from this information. In other words, when a gene 
         * is not expressed in a structure, it is expressed nowhere in that structure.
         */
        private boolean includeParentStructures;

        /**
         * An {@code Enum} used to define the origin of line.
         * <ul>
         * <li>{@code SELF}: this no-expression call exists in itself.
         * <li>{@code PARENT}: this no-expression call exists in one of its parents.
         * <li>{@code BOTH}: this no-expression call exists in itself, AND in one parent at the 
         * same time.
         * </ul>
         */
        public enum OriginOfLineType {
            SELF, PARENT, BOTH;
        }
        
        /**
         * An {@code OriginOfLineType} used to define the origin of line: either {@code SELF},
         * {@code PARENT} or {@code BOTH}.
         * 
         * @see OriginOfLineType
         */
        private OriginOfLineType originOfLine; 

        /**
         * Default constructor.
         */
        NoExpressionCallTO() {
            super();
            this.includeParentStructures = false;
            originOfLine = OriginOfLineType.SELF;
        }

        /**
         * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
         * the contribution of Affymetrix, <em>in situ</em> and, RNA-Seq data to the generation of 
         * this call, whether this no-expression call was generated using data from the anatomical 
         * entity with the ID alone, or by also considering all parents by is_a or part_of 
         * relations, even indirect.
         * 
         * @param geneId               A {@code String} that is the ID of the gene associated to 
         *                             this call.
         * @param anatEntityId         A {@code String} that is the ID of the anatomical entity
         *                             associated to this call. 
         * @param devStageId           A {@code String} that is the ID of the developmental stage 
         *                             associated to this call. 
         * @param affymetrixData       A {@code DataSate} that is the contribution of Affymetrix  
         *                             data to the generation of this call.
         * @param inSituData           A {@code DataSate} that is the contribution of 
         *                             <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData           A {@code DataSate} that is the contribution of RNA-Seq data
         *                             to the generation of this call.
         * @param includeParentStructures
         *                             A {@code boolean} defining whether this no-expression call 
         *                             was generated using data from the anatomical entity with the 
         *                             ID alone, or by also considering all parents by is_a or 
         *                             part_of relations, even indirect.
         */
        public NoExpressionCallTO(String id, String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState inSituData, DataState rnaSeqData, 
                boolean includeParentStructures) {
            super(id, geneId, anatEntityId, devStageId, affymetrixData, DataState.NODATA, inSituData, 
                    DataState.NODATA, rnaSeqData);
            this.includeParentStructures = includeParentStructures;
            originOfLine = OriginOfLineType.SELF;
        }

        /**
         * Constructor providing the gene ID, the anatomical entity ID, the developmental stage ID,  
         * the contribution of Affymetrix, <em>in situ</em> and, RNA-Seq data to the generation of 
         * this call, whether this no-expression call was generated using data from the anatomical 
         * entity with the ID alone, or by also considering all parents by is_a or part_of 
         * relations, even indirect, and, the origin of line
         * 
         * @param geneId               A {@code String} that is the ID of the gene associated to 
         *                             this call.
         * @param anatEntityId         A {@code String} that is the ID of the anatomical entity
         *                             associated to this call. 
         * @param devStageId           A {@code String} that is the ID of the developmental stage 
         *                             associated to this call. 
         * @param affymetrixData       A {@code DataSate} that is the contribution of Affymetrix  
         *                             data to the generation of this call.
         * @param inSituData           A {@code DataSate} that is the contribution of 
         *                             <em>in situ</em> data to the generation of this call.
         * @param rnaSeqData           A {@code DataSate} that is the contribution of RNA-Seq data
         *                             to the generation of this call.
         * @param includeParentStructures
         *                             A {@code boolean} defining whether this no-expression call 
         *                             was generated using data from the anatomical entity with the 
         *                             ID alone, or by also considering all parents by is_a or 
         *                             part_of relations, even indirect.
         * @param originOfLine         A {@code OriginOfLineType} defining the origin of line.
         */
        public NoExpressionCallTO(String id, String geneId, String anatEntityId, String devStageId,
                DataState affymetrixData, DataState inSituData, DataState rnaSeqData, 
                boolean includeParentStructures, OriginOfLineType originOfLine) {
            this(id, geneId, anatEntityId, devStageId, affymetrixData, inSituData, rnaSeqData, 
                    includeParentStructures);
            this.originOfLine = originOfLine;
        }

        /**
         * Returns the {@code boolean} defining whether this no-expression call was generated 
         * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
         * relations, even indirect. If {@code true}, all its parents were considered. 
         * So for instance, if B is_a A, and absence of expression has been reported in A, 
         * then B could benefit from this information. In other words, when a gene 
         * is not expressed in a structure, it is expressed nowhere in that structure.
         * 
         * @return  If {@code true}, all parents of the anatomical entity were considered.
         */
        public boolean isIncludeParentStructures() {
            return includeParentStructures;
        }

        /**
         * Sets the {@code boolean} defining whether this no-expression call was generated 
         * using the data from the anatomical entity with the ID {@link CallTO#getAnatEntityId()} 
         * alone, or by also considering all its parents by <em>is_a</em> or <em>part_of</em> 
         * relations, even indirect. If {@code true}, all its parents were considered. 
         * So for instance, if B is_a A, and absence of expression has been reported in A, 
         * then B could benefit from this information. In other words, when a gene 
         * is not expressed in a structure, it is expressed nowhere in that structure.
         * 
         * @param includeParentStructures   A {@code boolean} defining whether parents 
         *                                  of the anatomical entity were considered.
         */
        void setIncludeParentStructures(boolean includeParentStructures) {
            this.includeParentStructures = includeParentStructures;
        }

        /**
         * @return  the {@code OriginOfLineType} representing the origin of the call.
         */
        public OriginOfLineType getOriginOfLine() {
            return originOfLine;
        }
        
        /**
         * @param originOfLine  the {@code OriginOfLineType} representing the origin of the call.
         */
        void setOriginOfLine(OriginOfLineType originOfLine) {
            this.originOfLine = originOfLine;
        }


        @Override
        public String toString() {
            return super.toString() +  
                    " - Include Parent Structures: " + this.isIncludeParentStructures() +
                    " - Origin Of Line: " + this.getOriginOfLine();
        }
        
        /**
         * Convert the origin of call from the data source into an {@code OriginOfLineType}.
         * 
         * @param databaseEnum  A {@code String} that is origin of call from the data source.
         * @return              An {@code OriginOfLineType} representing the given {@code String}. 
         */
        public static OriginOfLineType convertDatasourceEnumToOriginOfLineType(String databaseEnum) {
            log.entry(databaseEnum);
            
            OriginOfLineType originType = null;
            if (databaseEnum.equals("self")) {
                originType = OriginOfLineType.SELF;
            } else if (databaseEnum.equals("parent")) {
                originType = OriginOfLineType.PARENT;
            } else if (databaseEnum.equals("both")) {
                originType = OriginOfLineType.BOTH;
            }
            
            return log.exit(originType);
        }

        /**
         * Convert the origin of call from the data source into an {@code OriginOfLineType}.
         * 
         * @param databaseEnum  A {@code String} that is origin of call from the data source.
         * @return              An {@code OriginOfLineType} representing the given {@code String}. 
         */
        public static String convertOriginOfLineTypeToDatasourceEnum(OriginOfLineType dataType) {
            log.entry(dataType);
            
            String databaseEnum = null;
            if (dataType == OriginOfLineType.SELF) {
                databaseEnum = "self";
            } else if (dataType == OriginOfLineType.PARENT) {
                databaseEnum = "parent";
            } else if (dataType == OriginOfLineType.BOTH) {
                databaseEnum = "both";
            }
            
            return log.exit(databaseEnum);
        }
    }
    
    /**
     * {@code DAOResultSet} specifics to {@code GlobalNoExpressionToNoExpressionTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface GlobalNoExpressionToNoExpressionTOResultSet 
                        extends DAOResultSet<GlobalNoExpressionToNoExpressionTO> {
    }

    /**
     * A {@code TransferObject} representing relation between no-expression table and 
     * globalNoExpression table in the Bgee data source.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class GlobalNoExpressionToNoExpressionTO implements TransferObject {
        // TODO modify the class to be immutable.
        private static final long serialVersionUID = -5283534395161770005L;

        /**
         * A {@code String} representing the ID of the no-expression call.
         */
        private String noExpressionId;

        /**
         * A {@code String} representing the ID of the global no-expression call.
         */
        private String globalNoExpressionId;

        /**
         * Constructor providing the no-expression call ID and the global no-expression call ID.  
         **/
        public GlobalNoExpressionToNoExpressionTO(String noExpressionId, String globalNoExpressionId) {
            this.setNoExpressionId(noExpressionId);
            this.setGlobalNoExpressionId(globalNoExpressionId);
        }

        /**
         * @return  the {@code String} representing the ID of the no-expression call.
         */
        public String getNoExpressionId() {
            return noExpressionId;
        }

        /**
         * @param noExpressionId  the {@code String} representing the ID of the no-expression call.
         */
        void setNoExpressionId(String noExpressionId) {
            this.noExpressionId = noExpressionId;
        }

        /**
         * @return  the {@code String} representing the ID of the global no-expression call.
         */
        public String getGlobalNoExpressionId() {
            return globalNoExpressionId;
        }

        /**
         * @param globalNoExpressionId  the {@code String} representing the ID of the global 
         *                              no-expression call.
         */
        void setGlobalNoExpressionId(String globalNoExpressionId) {
            this.globalNoExpressionId = globalNoExpressionId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * 
                    result + ((globalNoExpressionId == null) ? 0 : globalNoExpressionId.hashCode());
            result = prime * result + ((noExpressionId == null) ? 0 : noExpressionId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            GlobalNoExpressionToNoExpressionTO other = (GlobalNoExpressionToNoExpressionTO) obj;
            if (globalNoExpressionId == null) {
                if (other.globalNoExpressionId != null) {
                    return false;
                }
            } else if (!globalNoExpressionId.equals(other.globalNoExpressionId)) {
                return false;
            }
            if (noExpressionId == null) {
                if (other.noExpressionId != null) {
                    return false;
                }
            } else if (!noExpressionId.equals(other.noExpressionId)) {
                return false;
            }
            return true;
        }
    }
}
