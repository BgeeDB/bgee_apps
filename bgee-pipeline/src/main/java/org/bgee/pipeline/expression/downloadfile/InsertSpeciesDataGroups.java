package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class responsible for inserting the species data groups, species data groups to species mappings,
 * and download files into the Bgee database.
 * 
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13
 */
public class InsertSpeciesDataGroups extends MySQLDAOUser {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(InsertSpeciesDataGroups.class.getName());
    
    /**
     * Main method to trigger the insertion of the species data groups, species data groups 
     * to species mappings, and download files into the Bgee database.
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>a {@code Map} where keys are {@code String}s that are names given to the species data groups,   
     *     the associated value being a {@code Set} of {@code String}s that are the  
     *     IDs of the species composing the group. The order of the entries is important, 
     *     it is used to specify the preferred display order of the data groups. 
     *     Entries of the {@code Map} must be separated by {@link CommandRunner#LIST_SEPARATOR}, 
     *     keys must be separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, 
     *     see {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a {@code Map} where keys are {@code String}s that are names given to species data groups,  
     *     the associated value being a {@code Set} of {@code String}s that are 
     *     the paths with file name composing the group. The order of the entries is important, 
     *     it is used to specify the preferred display order of the data groups. 
     *     The entry set should be identical to the entry set in the {@code Map} described above. 
     *     The paths provided in values should be relative to the directory provided 
     *     as last argument. They are the paths that will be inserted into the database, 
     *     not the absolute ones using the provided directory. They should all be present in the 
     *     key set of the {@code Map} descibed below. 
     *     Entries of the {@code Map} must be separated by {@link CommandRunner#LIST_SEPARATOR}, 
     *     keys must be separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, 
     *     see {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a {@code Map} where keys are {@code String}s that are file paths, the associated value
     *     being a {@code String} containing the category of the file. Entries of  
     *     the {@code Map} must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be 
     *     separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     see {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a {@code String} that is directory to be used to retrieve files using 
     *     the provided related paths.
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If the provided {@code args} do not contain 
     *                                  requested information.
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);
        
        int expectedArgLength = 4;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, " +
                    "expected " + expectedArgLength + " arguments, " + args.length + " provided."));
        }
        
        //For args[0] and args[1], transforms the LinkedHashMap<String, List<String>> 
        //into LinkedHashMap<String, Set<String>>
        // From args[2], transforms the Map<String, List<String>> into Map<String, String>. 
        // We could use the flatMap function, but we want to perform sanity checks.
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(
                
                //LinkedHashMap<String, Set<String>> groupToSpecies
                CommandRunner.parseMapArgument(args[0]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<String>(e.getValue()), 
                    (k, v) -> {throw log.throwing(
                            new IllegalArgumentException("Key used more than once: " + k));}, 
                    LinkedHashMap::new)),
                
                //LinkedHashMap<String, Set<String>> groupToFilePaths
                CommandRunner.parseMapArgument(args[1]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<String>(e.getValue()), 
                        (k, v) -> {throw log.throwing(
                                new IllegalArgumentException("Key used more than once: " + k));}, 
                        LinkedHashMap::new)),
                
                //Map<String, String> file to category
                CommandRunner.parseMapArgument(args[2]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    if (e.getValue().size() != 1) {
                        throw log.throwing(new IllegalArgumentException("One and only one category "
                                + "can be associated to a file."));
                    }
                    return e.getValue().iterator().next();
                })),
                
                CommandRunner.parseArgument(args[3]));
        insert.insert();
        
        log.exit();
    }

    /**
     * A {@code LinkedHashMap} where keys are {@code String}s that are names given to a group of species, 
     * the associated values being {@code Set}s of {@code String}s corresponding to 
     * species IDs belonging to the group. The order of the entries is important, 
     * it is used to specify the preferred display order of the data groups. 
     */
    private final LinkedHashMap<String, Set<String>> groupToSpecies;
    
    /**
     * A {@code LinkedHashMap} where keys are {@code String}s that are names given to a group of species, 
     * the associated values being {@code Set}s of {@code String}s corresponding to 
     * file paths belonging to the group. The order of the entries is important, 
     * it is used to specify the preferred display order of the data groups. 
     * The entry set should be identical to the entry set in {@link #groupToSpecies}.
     * The paths provided in values should be relative to {@link #directory}. They are the paths 
     * that will be inserted into the database, not the absolute ones using {@link #directory}. 
     * They should all be present in the key set of {@link filePathToCategory}.
     */
    private final LinkedHashMap<String, Set<String>> groupToFilePaths;

    /**
     * A {@code Map} where keys are {@code String}s that are file paths, the associated values 
     * being {@code String}s corresponding to file categories. All paths defined in the key set 
     * should be present in the values of {@link #groupToFilePaths}.
     */
    private final Map<String, String> filePathToCategory;

    /**
     * A {@code String} that is the directory to be used to deduce relative paths.
     */
    private final String directory;
    
    /**
     * Constructor requesting all mandatory parameters.
     * 
     * @param groupToSpecies    A {@code LinkedHashMap} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group. The order of the entries 
     *                          is important, it is used to specify the preferred display order 
     *                          of the data groups. 
     * @param groupToFilePaths  A {@code LinkedHashMap} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the file paths 
     *                          of the group. The order of the entries 
     *                          is important, it is used to specify the preferred display order 
     *                          of the data groups. The entry set should be identical to the entry set 
     *                          in {@code groupToSpecies}. The paths provided in values 
     *                          should be relative to {@code directory}. They are the paths 
     *                          that will be inserted into the database, not the absolute ones 
     *                          using {@code directory}. They should all be present in the 
     *                          key set of {@code fileToCategory}.
     * @param fileToCategory    A {@code Map} where keys are {@code String}s that are file paths, 
     *                          the associated values being {@code String}s corresponding to 
     *                          file categories. All paths defined in the key set should be present 
     *                          in the values of {@code groupToFiles}.
     * @param directory         A {@code String} that is the directory to be used to deduce relative paths.
     * @throws IllegalArgumentException If some mandatory parameters are incorrectly provided.
     */
    public InsertSpeciesDataGroups(LinkedHashMap<String, Set<String>> groupToSpecies, 
            LinkedHashMap<String, Set<String>> groupToFilePaths,
            Map<String, String> fileToCategory, String directory) {
        this(null, groupToSpecies, groupToFilePaths, fileToCategory, directory);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager           the {@code MySQLDAOManager} to use.
     * @param groupToSpecies    A {@code LinkedHashMap} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group. The order of the entries 
     *                          is important, it is used to specify the preferred display order 
     *                          of the data groups. 
     * @param groupToFiles      A {@code LinkedHashMap} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the file paths 
     *                          of the group. The order of the entries 
     *                          is important, it is used to specify the preferred display order 
     *                          of the data groups. The entry set should be identical to the entry set 
     *                          in {@code groupToSpecies}. The paths provided in values 
     *                          should be relative to {@code directory}. They are the paths 
     *                          that will be inserted into the database, not the absolute ones 
     *                          using {@code directory}. They should all be present in the 
     *                          key set of {@code fileToCategory}.
     * @param fileToCategory    A {@code Map} where keys are {@code String}s that are file paths, 
     *                          the associated values being {@code String}s corresponding to 
     *                          file categories. All paths defined in the key set should be present 
     *                          in the values of {@code groupToFiles}.
     * @param directory         A {@code String} that is the directory to be used to deduce relative paths.
     * @throws IllegalArgumentException If some mandatory parameters are incorrectly provided.
     */
    public InsertSpeciesDataGroups(MySQLDAOManager manager, LinkedHashMap<String, Set<String>> groupToSpecies,
            LinkedHashMap<String, Set<String>> groupToFiles, Map<String, String> fileToCategory, String directory) {
        super(manager);
        log.entry(groupToSpecies, groupToFiles, fileToCategory, directory);
        if (groupToSpecies == null || groupToSpecies.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-species mapping is provided"));
        }
        this.groupToSpecies = new LinkedHashMap<>(groupToSpecies);
        if (groupToFiles == null || groupToFiles.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-paths mapping is provided"));
        }
        this.groupToFilePaths = new LinkedHashMap<>(groupToFiles);
        if (fileToCategory == null || fileToCategory.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No path-category mapping is provided"));
        }
        this.filePathToCategory = new HashMap<>(fileToCategory);
        if (StringUtils.isEmpty(directory)) {
            throw log.throwing(new IllegalArgumentException("No directory is provided"));
        }
        this.directory = directory;
        
        //Checks coherence of data in the provided maps
        if (!this.groupToSpecies.keySet().equals(this.groupToFilePaths.keySet())) {
            throw log.throwing(new IllegalArgumentException("Both maps don't have "
                    + "the same group names and/or store them in different orders."));
        }
        if (!this.filePathToCategory.keySet().equals(
                this.groupToFilePaths.values().stream().flatMap(e -> e.stream())
                .collect(Collectors.toSet()))) {
            throw log.throwing(new IllegalArgumentException("Different file paths "
                    + "between mapping filePath-category [" + filePathToCategory.keySet() 
                    + "] and groupName-filePaths [" 
                    + groupToFilePaths.values().stream().flatMap(e -> e.stream())
                    .collect(Collectors.toSet()) + "]"));
        }
        
        //sanity check on species IDs
        BgeeDBUtils.checkAndGetSpeciesIds(
                new ArrayList<String>(this.groupToSpecies.values().stream()
                .flatMap(e -> e.stream()).collect(Collectors.toSet())), this.getSpeciesDAO());
        
        log.exit();
    }
    
    /**
     * Insert the species data groups, species data groups to species mappings, 
     * and download files into the Bgee database, using the information provided at instantiation.
     * Note that this method should be used to populate empty tables.
     */
    public void insert() {
        log.entry();

        // First, we create SpeciesDataGroupTOs and SpeciesToDataGroupTOs
        Set<SpeciesToDataGroupTO> speciesToDataGroupTOs = new HashSet<SpeciesToDataGroupTO>();
        //we store the SpeciesDataGroups associated to their name, to be able 
        //to easily retrieve the generated ID of a SpeciesDataGroup from its name 
        //(and also to insert the SpeciesDataGroups). 
        Map<String, SpeciesDataGroupTO> speciesDataGroupTOs = new HashMap<>(); 
        int i = 1;
        for (Entry<String, Set<String>> groupToSpecies: this.groupToSpecies.entrySet()) {
            if (StringUtils.isBlank(groupToSpecies.getKey())) {
                throw log.throwing(new IllegalStateException("No group name can be blank."));
            }
            final String groupName = groupToSpecies.getKey().trim();
            final String groupId = String.valueOf(i);
            speciesDataGroupTOs.put(groupName, 
                    //we use the dataGroupId also as preferred order
                    new SpeciesDataGroupTO(groupId, groupName, null, i));
            speciesToDataGroupTOs.addAll(groupToSpecies.getValue().stream()
                .map(e -> new SpeciesToDataGroupTO(e, groupId))
                .collect(Collectors.toSet()));
            i++;
        }

        // Then, we create DownloadFileTOs
        Set<DownloadFileTO> downloadFileTOs = new HashSet<DownloadFileTO>();
        int downloadFileId = 1;
        for (Entry<String, Set<String>> groupAndPaths : this.groupToFilePaths.entrySet()) {
            String groupId = speciesDataGroupTOs.get(groupAndPaths.getKey()).getId();
            for (String path: groupAndPaths.getValue()) {
                String fileCategory = filePathToCategory.get(path);
                File file = new File(this.directory, path);
                if (!file.exists() || file.isDirectory()) {
                    throw log.throwing(new IllegalArgumentException(
                            "The file " + file.getAbsolutePath() + " doesn't exist or is a directory."));
                }
                // Currently, the file description is not use, so for the moment, we set it at null.
                downloadFileTOs.add(new DownloadFileTO(
                        String.valueOf(downloadFileId), file.getName(), null, path.trim(), file.length(),
                        CategoryEnum.convertToCategoryEnum(fileCategory), groupId));
                downloadFileId++;
            }
        }

        log.info("Start inserting species data groups...");

        try {
            this.startTransaction();
            
            // Insertion of SpeciesDataGroupTOs
            log.debug("Start inserting species data groups...");
            this.getSpeciesDataGroupDAO().insertSpeciesDataGroups(speciesDataGroupTOs.values());
            log.debug("Done inserting species data groups, {} groups inserted", speciesDataGroupTOs.size());

            // Insertion of SpeciesToDataGroupTOs
            log.debug("Start inserting species data groups to species mappings...");
            this.getSpeciesDataGroupDAO().insertSpeciesToDataGroup(speciesToDataGroupTOs);
            log.debug("Done inserting species data groups to species mappings, {} mappings inserted", 
                    speciesToDataGroupTOs.size());
            
            // Insertion of DownloadFileTOs
            log.debug("Start inserting download files...");
            this.getDownloadFileDAO().insertDownloadFiles(downloadFileTOs);
            log.debug("Done inserting download files, {} files inserted.", downloadFileTOs.size());
            
            this.commit();
        } finally {
            this.closeDAO();
        }

        log.info("Done inserting species data groups.");
        log.exit();
    }
}
