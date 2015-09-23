package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class responsible for inserting the species data groups, species data groups to species mappings,
 * and download files into the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public class InsertSpeciesDataGroups extends MySQLDAOUser {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(InsertSpeciesDataGroups.class.getName());

    /**
     * A {@code Map} where keys are {@code String}s that are names given to a group of species, 
     * the associated values being {@code Set}s of {@code String}s corresponding to 
     * species IDs belonging to the group.
     */
    private Map<String, Set<String>> groupToSpecies;
    
    /**
     * A {@code Map} where keys are {@code String}s that are names given to a group of species, 
     * the associated values being {@code Set}s of {@code String}s corresponding to 
     * file path belonging to the group.
     */
    private Map<String, Set<String>> groupToPath;

    /**
     * Default constructor. 
     */
    public InsertSpeciesDataGroups() {
        this(null, null, null);
    }
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param groupToSpecies    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param groupToPath       A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     */
    public InsertSpeciesDataGroups(
            Map<String, Set<String>> groupToSpecies, Map<String, Set<String>> groupToPath) {
        this(null, groupToSpecies, groupToPath);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param groupToSpecies    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param groupToPath       A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertSpeciesDataGroups(MySQLDAOManager manager, Map<String, Set<String>> groupToSpecies,
            Map<String, Set<String>> groupToPath) {
        super(manager);
        if (groupToSpecies == null || groupToSpecies.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-species mapping is provided"));
        }
        this.groupToSpecies = groupToSpecies;
        if (groupToPath == null || groupToPath.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-path mapping is provided"));
        }
        this.groupToPath = groupToPath;
    }
    
    /**
     * Main method to trigger the insertion of the species data groups, species data groups 
     * to species mappings, and download files into the Bgee database.
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>a {@code Map} where keys are {@code String}s that are names given to groups of  
     *     species, the associated value being a {@code Set} of {@code String}s that are the  
     *     IDs of the species composing the group. Entries of the {@code Map} must be separated 
     *     by {@link CommandRunner#LIST_SEPARATOR}, keys must be separated from their associated 
     *     value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, values must be separated using 
     *     {@link CommandRunner#VALUE_SEPARATOR}, 
     *     see {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a {@code Map} where keys are {@code String}s that are names given to groups of 
     *     species, the associated value being a {@code Set} of {@code String}s that are 
     *     the path with file name composing the group. Entries of the {@code Map} must 
     *     be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be separated 
     *     from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, see 
     *     {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);
        
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, " +
                    "expected " + expectedArgLength + " arguments, " + args.length + " provided."));
        }
        
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(
                null,
                CommandRunner.parseMapArgument(args[0]),
                CommandRunner.parseMapArgument(args[1]));
        insert.insert();
        
        log.exit();
    }
    
    /**
     * Insert the species data groups, species data groups to species mappings, 
     * and download files into the Bgee database.
     */
    private void insert() {
        log.entry();

        if (!this.groupToSpecies.keySet().equals(this.groupToPath.keySet())) {
            throw log.throwing(new IllegalArgumentException("Both maps doesn't have the same group names"));
        }
        
        // First, we create SpeciesDataGroupTOs and SpeciesToDataGroupTOs
        int groupId = 1;
        Set<SpeciesDataGroupTO> speciesDataGroupTOs = new HashSet<SpeciesDataGroupTO>();  
        Set<SpeciesToDataGroupTO> speciesToDataGroupTOs = new HashSet<SpeciesToDataGroupTO>();  
        for (Entry<String, Set<String>> groupAndSpecies : this.groupToSpecies.entrySet()) {
            String groupIdAsString = String.valueOf(groupId);
            //TODO how to define description?
            speciesDataGroupTOs.add(
                    new SpeciesDataGroupTO(groupIdAsString, groupAndSpecies.getKey(), null));

            for (String speciesId : groupAndSpecies.getValue()) {
                //TODO should we check species ID?
                speciesToDataGroupTOs.add(new SpeciesToDataGroupTO(speciesId, groupIdAsString));       
            }
            groupId++;
        }

        // THen we create DownloadFileTOs
        int downloadFileId = 1;
        Set<DownloadFileTO> downloadFileTOs = new HashSet<DownloadFileTO>();  
        for (Entry<String, Set<String>> groupAndPath : this.groupToPath.entrySet()) {
            String groupIdAsString = String.valueOf(groupId);
            Set<String> filePaths = groupAndPath.getValue();
            
            for (String path: filePaths) {
                // Get file from file name
                File file = new File(path);
                if (!file.exists()) {
                    throw log.throwing(
                            new IllegalArgumentException("The file " + path + "doesn't exists"));
                }
                //TODO how to define description?
                downloadFileTOs.add(new DownloadFileTO(
                        String.valueOf(downloadFileId), file.getName(), "description", 
                        file.getParent(), file.length(), this.getCategoryEnum(file.getName()), groupIdAsString));
            }
            downloadFileId++;
        }

        log.info("Start inserting species data groups...");

        try {
            this.startTransaction();
            
            // Insertion of SpeciesDataGroupTOs
            log.info("Start inserting species data groups...");
            this.getSpeciesDataGroupDAO().insertSpeciesDataGroups(speciesDataGroupTOs);
            log.info("Done inserting species data groups, {} groups inserted", speciesDataGroupTOs.size());

            // Insertion of SpeciesToDataGroupTOs
            log.info("Start inserting species data groups to species mappings...");
            this.getSpeciesDataGroupDAO().insertSpeciesToDataGroup(speciesToDataGroupTOs);
            log.info("Done inserting species data groups to species mappings, {} mappings inserted", 
                    speciesDataGroupTOs.size());
            
            // Insertion of DownloadFileTOs
            log.info("Start inserting download files...");
            this.getDownloadFileDAO().insertDownloadFiles(downloadFileTOs);
            log.info("Done inserting download files, {} files inserted.");
            
            this.commit();
        } finally {
            this.closeDAO();
        }

        log.exit();
    }


    /**
     * Define the {@code CategoryEnum} of a file according to the provided file name.
     * 
     * @param filename  A {@code String} that is the name of the file to be used.
     * @return          A {@code CategoryEnum} that is the category of the provided file name.
     */
    private CategoryEnum getCategoryEnum(String filename) {
        log.entry(filename);
        //TODO define category by parsing file name using CategoryEnum and FileTypes

//      CategoryEnum  
//          EXPR_CALLS_SIMPLE("expr_simple"),
//          EXPR_CALLS_COMPLETE("expr_complete"),
//          DIFF_EXPR_ANAT_SIMPLE("diff_expr_anatomy_simple"),
//          DIFF_EXPR_ANAT_COMPLETE("diff_expr_anatomy_complete"),
//          DIFF_EXPR_DEV_COMPLETE("diff_expr_dev_complete"),
//          DIFF_EXPR_DEV_SIMPLE("diff_expr_dev_simple"),
//          ORTHOLOG("ortholog"),
//          AFFY_ANNOT("affy_annot"),
//          AFFY_DATA("affy_data"),
//          AFFY_ROOT("affy_root"),
//          RNASEQ_ANNOT("rnaseq_annot"),
//          RNASEQ_DATA("rnaseq_data"),
//          RNASEQ_ROOT("rnaseq_root");
        
//      SingleSpDiffExprFileType
//          DIFF_EXPR_ANATOMY_SIMPLE("diffexpr-anatomy-simple", true, ComparisonFactor.ANATOMY), 
//          DIFF_EXPR_ANATOMY_COMPLETE("diffexpr-anatomy-complete", false, ComparisonFactor.ANATOMY),
//          DIFF_EXPR_DEVELOPMENT_SIMPLE("diffexpr-development-simple", true, ComparisonFactor.DEVELOPMENT), 
//          DIFF_EXPR_DEVELOPMENT_COMPLETE("diffexpr-development-complete", false, ComparisonFactor.DEVELOPMENT);

//      SingleSpExprFileType
//          EXPR_SIMPLE("expr-simple", true), 
//          EXPR_COMPLETE("expr-complete", false);
        
//      MultiSpeciesDiffExprFileType
//          MULTI_DIFF_EXPR_ANATOMY_SIMPLE("multi-diffexpr-anatomy-simple", true, ComparisonFactor.ANATOMY), 
//          MULTI_DIFF_EXPR_ANATOMY_COMPLETE("multi-diffexpr-anatomy-complete", false, ComparisonFactor.ANATOMY),
//          MULTI_DIFF_EXPR_DEVELOPMENT_SIMPLE("multi-diffexpr-development-simple", true, ComparisonFactor.DEVELOPMENT), 
//          MULTI_DIFF_EXPR_DEVELOPMENT_COMPLETE("multi-diffexpr-development-complete", false, ComparisonFactor.DEVELOPMENT);

        return null;
    }
}
