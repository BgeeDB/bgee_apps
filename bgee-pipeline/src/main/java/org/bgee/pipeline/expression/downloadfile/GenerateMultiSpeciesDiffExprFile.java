package org.bgee.pipeline.expression.downloadfile;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;


/**
 * TODO Javadoc
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesDiffExprFile extends GenerateMultiSpeciesDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesDiffExprFile.class.getName());
    
    /**
     * An {@code Enum} used to define the possible differential expression in multi-species file
     * types to be generated.
     * <ul>
     * <li>{@code MULTI_EXPR_ANATOMY_SIMPLE}:       differential expression in multi-species based 
     *                                              on comparison of several anatomical entities at 
     *                                              a same (broad) developmental stage, 
     *                                              in a simple download file.
     * <li>{@code MULTI_EXPR_ANATOMY_COMPLETE}:     differential expression in multi-species based 
     *                                              on comparison of several anatomical entities at 
     *                                              a same (broad) developmental stage, 
     *                                              in a complete download file.
     * <li>{@code MULTI_EXPR_DEVELOPMENT_SIMPLE}:   differential expression in multi-species based 
     *                                              on comparison of a same anatomical entity at 
     *                                              different developmental stages, 
     *                                              in a simple download file.
     * <li>{@code MULTI_EXPR_DEVELOPMENT_COMPLETE}: differential expression in multi-species based 
     *                                              on comparison of a same anatomical entity at 
     *                                              different developmental stages, 
     *                                              in a complete download file
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum MultiSpeciesDiffExprFileType implements DiffExprFileType {
        MULTI_EXPR_ANATOMY_SIMPLE(
                "multi-expr-anatomy-simple", true, ComparisonFactor.ANATOMY), 
        MULTI_EXPR_ANATOMY_COMPLETE(
                "multi-expr-anatomy-complete", false, ComparisonFactor.ANATOMY),
        MULTI_EXPR_DEVELOPMENT_SIMPLE(
                "multi-expr-anatomy-simple", true, ComparisonFactor.DEVELOPMENT), 
        MULTI_EXPR_DEVELOPMENT_COMPLETE(
                "multi-expr-anatomy-complete", false, ComparisonFactor.DEVELOPMENT);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;
        
        /**
         * A {@code boolean} defining whether this {@code MultiSpeciesDiffExprFileType} is a simple 
         * file type
         */
        private final boolean simpleFileType;

        /**
         * A {@code ComparisonFactor} defining what is the compared experimental factor that 
         * generated the differential expression calls.
         */
        //XXX: I find it a bit weird to use the ComparisonFactor of DiffExpressionCallTO at this point, 
        //because it is not a class related to a DAO...
        private final ComparisonFactor comparisonFactor;

        /**
         * Constructor providing the {@code String} representation of this 
         * {@code MultiSpeciesDiffExprFileType}, a {@code boolean} defining whether this 
         * {@code MultiSpeciesDiffExprFileType} is a simple file type, and a 
         * {@code ComparisonFactor} defining what is the experimental factor compared 
         * that generated the differential expression calls.
         */
        private MultiSpeciesDiffExprFileType(String stringRepresentation, boolean simpleFileType,
                ComparisonFactor comparisonFactor) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = simpleFileType;
            this.comparisonFactor = comparisonFactor;
        }

        @Override
        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        @Override
        public boolean isSimpleFileType() {
            return this.simpleFileType;
        }
        
        @Override
        public ComparisonFactor getComparisonFactor() {
            return this.comparisonFactor;
        }

        @Override
        public String toString() {
            return this.getStringRepresentation();
        }
    }
    
    /**
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesDiffExprFile() {
        this(null, null, null, null, null);
    }

    /**
     * Constructor providing parameters to generate files, using the default {@code DAOManager}.
     * 
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO .
     * @param fileTypes     A {@code Set} of {@code ExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpeciesDiffExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(List<String> speciesIds, String taxonId, 
            Set<MultiSpeciesDiffExprFileType> fileTypes, String directory) throws IllegalArgumentException {
        this(null, speciesIds, taxonId, fileTypes, directory);
    }

    /**
     * Constructor providing parameters to generate files, and the {@code MySQLDAOManager} that will  
     * be used by this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager       the {@code MySQLDAOManager} to use.
     * @param speciesIds    A {@code List} of {@code String}s that are the IDs of species 
     *                      we want to generate data for. If {@code null} or empty, all species 
     *                      are used.
     * @param taxonId       A {@code String} that is the ID of the common ancestor taxon
     *                      we want to into account. If {@code null} or empty, TODO .
     * @param fileTypes     A {@code Set} of {@code ExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpeciesDiffExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesDiffExprFile(MySQLDAOManager manager, List<String> speciesIds, 
            String taxonId, Set<MultiSpeciesDiffExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        super(manager, speciesIds, taxonId, fileTypes, directory);
    }

}
