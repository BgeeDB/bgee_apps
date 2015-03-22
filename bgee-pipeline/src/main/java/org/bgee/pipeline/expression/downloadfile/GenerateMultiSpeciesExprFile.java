package org.bgee.pipeline.expression.downloadfile;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;


/**
 * Class used to generate multi-species expression download files (simple and advanced files) 
 * from the Bgee database.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesExprFile extends GenerateMultiSpeciesDownloadFile {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesExprFile.class.getName());
    
    /**
     * An {@code Enum} used to define the possible multi-species expression file types 
     * to be generated.
     * <ul>
     * <li>{@code MULTI_EXPR_SIMPLE}:   presence/absence of expression in multi-species 
     *                                  in a simple download file.
     * <li>{@code MULTI_EXPR_COMPLETE}: presence/absence of expression in multi-species
     *                                  in an advanced download file.
     * </ul>
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum MultiSpeciesExprFileType implements FileType {
        MULTI_EXPR_SIMPLE("multi-expr-simple", true), 
        MULTI_EXPR_COMPLETE("multi-expr-complete", false);

        /**
         * A {@code String} that can be used to generate names of files of this type.
         */
        private final String stringRepresentation;
        
        /**
         * A {@code boolean} defining whether this {@code MultiSpeciesExprFileType} is a simple 
         * file type
         */
        private final boolean simpleFileType;

        /**
         * Constructor providing the {@code String} representation of this 
         * {@code MultiSpeciesExprFileType}, and a {@code boolean} defining whether this 
         * {@code MultiSpeciesExprFileType} is a simple file type.
         */
        private MultiSpeciesExprFileType(String stringRepresentation, boolean simpleFileType) {
            this.stringRepresentation = stringRepresentation;
            this.simpleFileType = simpleFileType;
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
        public String toString() {
            return this.getStringRepresentation();
        }
    }

    /**
     * Default constructor. 
     */
    //suppress warning as this default constructor should not be used.
    @SuppressWarnings("unused")
    private GenerateMultiSpeciesExprFile() {
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
     * @param fileTypes     A {@code Set} of {@code MultiSpeciesExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpeciesExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesExprFile(List<String> speciesIds, String taxonId, 
            Set<MultiSpeciesExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
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
     * @param fileTypes     A {@code Set} of {@code MultiSpeciesExprFileType}s that are the types
     *                      of files we want to generate. If {@code null} or empty, 
     *                      all {@code MultiSpeciesExprFileType}s are generated.
     * @param directory     A {@code String} that is the directory where to store files.
     * @throws IllegalArgumentException If {@code directory} is {@code null} or blank.
     */
    public GenerateMultiSpeciesExprFile(MySQLDAOManager manager, List<String> speciesIds, 
            String taxonId, Set<MultiSpeciesExprFileType> fileTypes, String directory) 
                    throws IllegalArgumentException {
        super(manager, speciesIds, taxonId, fileTypes, directory);
    }
}
