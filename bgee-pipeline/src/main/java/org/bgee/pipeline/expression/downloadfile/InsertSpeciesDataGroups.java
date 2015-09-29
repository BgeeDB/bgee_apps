package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
     * file paths belonging to the group.
     */
    private Map<String, Set<String>> groupToFilePaths;

    /**
     * A {@code Map} where keys are {@code String}s that are file paths, the associated values 
     * being {@code String}s corresponding to file categories.
     */
    private Map<String, String> filePathToCategory;

    /**
     * A {@code String} that is the directory to be used to deduce relative paths.
     */
    private String directory;

    /**
     * Default constructor. 
     */
    public InsertSpeciesDataGroups() {
        this(null, null, null, null);
    }
    
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param groupToSpecies    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param groupToFilePaths  A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param fileToCategory    A {@code Map} where keys are {@code String}s that are file paths, 
     *                          the associated values being {@code String}s corresponding to 
     *                          file categories.
     * @param directory         A {@String} that is the directory to be used to deduce relative paths.
     */
    public InsertSpeciesDataGroups(
            Map<String, Set<String>> groupToSpecies, Map<String, Set<String>> groupToFilePaths,
            Map<String, String> fileToCategory, String directory) {
        this(null, groupToSpecies, groupToFilePaths, fileToCategory, directory);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager           the {@code MySQLDAOManager} to use.
     * @param groupToSpecies    A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the IDs 
     *                          of the species composing the group.
     * @param groupToFiles      A {@code Map} where keys are {@code String}s that are names 
     *                          given to groups of species, the associated value being 
     *                          a {@code Set} of {@code String}s that are the file paths 
     *                          of the group.
     * @param fileToCategory    A {@code Map} where keys are {@code String}s that are file paths, 
     *                          the associated values being {@code String}s corresponding to 
     *                          file categories.
     * @param directory         A {@String} that is the directory to be used to deduce relative paths.
     */
    public InsertSpeciesDataGroups(MySQLDAOManager manager, Map<String, Set<String>> groupToSpecies,
            Map<String, Set<String>> groupToFiles, Map<String, String> fileToCategory, String directory) {
        super(manager);
        if (groupToSpecies == null || groupToSpecies.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-species mapping is provided"));
        }
        this.groupToSpecies = groupToSpecies;
        if (groupToFiles == null || groupToFiles.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-paths mapping is provided"));
        }
        this.groupToFilePaths = groupToFiles;
        if (fileToCategory == null || fileToCategory.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No path-category mapping is provided"));
        }
        this.filePathToCategory = fileToCategory;
        if (StringUtils.isEmpty(directory)) {
            throw log.throwing(new IllegalArgumentException("No directory is provided"));
        }
        this.directory = directory;
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
     *     the paths with file name composing the group. Entries of the {@code Map} must 
     *     be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be separated 
     *     from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
     *     values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, see 
     *     {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a {@code Map} where keys are {@code String}s that are file paths, the associated value
     *     being a {@code Set} of {@code String}s containing the category of file only. Entries of  
     *     the {@code Map} must be separated by {@link CommandRunner#LIST_SEPARATOR}, keys must be 
     *     separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR},
     *     see {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
     * <li>a {@code String} that is directory to be used to deduce relative paths.
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     */
    public static void main(String[] args) {
        log.entry((Object[]) args);
        
        int expectedArgLength = 4;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, " +
                    "expected " + expectedArgLength + " arguments, " + args.length + " provided."));
        }
        
        // From arg[2], we generate the mapping file path-category where category is a String 
        // and not a Set<String>
        Map<String, String> pathToCategory = new HashMap<String, String>();
        CommandRunner.parseMapArgument(args[2]).entrySet().stream()
            // TODO : throw an IllegalArgumentException when empty 
            // msg: "No category or several categories provided for the path " + entry.getKey()
            .filter(entry -> entry.getValue().size() != 1)
            .forEach(entry -> {
                String value = entry.getValue().stream().collect(Collectors.joining(""));
                pathToCategory.put(entry.getKey(), value);
            });
        
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(
                null,
                CommandRunner.parseMapArgument(args[0]),
                CommandRunner.parseMapArgument(args[1]),
                pathToCategory,
                CommandRunner.parseArgument(args[3]));
        insert.insert();
        
        log.exit();
    }
    
    /**
     * Insert the species data groups, species data groups to species mappings, 
     * and download files into the Bgee database.
     */
    public void insert() {
        log.entry();

        if (!this.groupToSpecies.keySet().equals(this.groupToFilePaths.keySet())) {
            throw log.throwing(
                    new IllegalArgumentException("Both maps doesn't have the same group names"));
        }
        
        // We create mapping group-groupID before to keep the same order
        Map<String, String> groupNameToGroupId = new HashMap<String, String>();
        List<String> orderedGroupName = new ArrayList<String>(this.groupToSpecies.keySet());
        Collections.sort(orderedGroupName);
        AtomicInteger groupId = new AtomicInteger();
        orderedGroupName.stream()
            .filter(name -> !this.groupToSpecies.get(name).isEmpty())
            .forEach(groupName -> {
                groupId.incrementAndGet();
                groupNameToGroupId.put(groupName, String.valueOf(groupId));
            });

        // First, we create SpeciesDataGroupTOs and SpeciesToDataGroupTOs
        Set<String> speciesIDs = new HashSet<String>(); 
        Set<SpeciesDataGroupTO> speciesDataGroupTOs = new HashSet<SpeciesDataGroupTO>();  
        Set<SpeciesToDataGroupTO> speciesToDataGroupTOs = new HashSet<SpeciesToDataGroupTO>();  
        this.groupToSpecies.entrySet().stream()
            // TODO : throw an IllegalArgumentException when empty 
            .filter(entry -> !entry.getValue().isEmpty())
            .forEach(entry -> {
                speciesIDs.addAll(entry.getValue());
                String groupIdAsString = groupNameToGroupId.get(entry.getKey());

                // Add a SpeciesDataGroupTO for each group. Currently, the group description
                // is not use, so for the moment, we set it at null.
                speciesDataGroupTOs.add(new SpeciesDataGroupTO(groupIdAsString, entry.getKey(), null));
                // Add a SpeciesToDataGroupTO for each species of the group
                entry.getValue().stream().forEach(species -> 
                    speciesToDataGroupTOs.add(new SpeciesToDataGroupTO(species, groupIdAsString)));
            });
        // Sanity check on species IDs
        BgeeDBUtils.checkAndGetSpeciesIds(new ArrayList<String>(speciesIDs), this.getSpeciesDAO());

        // Then, we create DownloadFileTOs
        Set<DownloadFileTO> downloadFileTOs = new HashSet<DownloadFileTO>();
        Set<String> filePaths = new HashSet<String>(); 
        //TODO: fix bugs to use stream instead of 'java<8' for-loops.
//        AtomicInteger downloadFileId = new AtomicInteger();
//        this.groupToFilePaths.entrySet().stream()
//            .forEach(entry -> {
//                filePaths.addAll(entry.getValue());
//                entry.getValue().stream()
//                    .map(File::new)
//                    // TODO : throw an IllegalArgumentException if the file doesn't exist
//                    // msg: "The file " + path + "doesn't exists"
//                    .filter(t -> t.exists())
//                    .forEach(file -> {
//                        downloadFileId.incrementAndGet();
//                        //TODO fix category that is null
//                        // Currently, the file description is not use, so for the moment, we set it at null.
//                        downloadFileTOs.add(new DownloadFileTO(
//                                String.valueOf(downloadFileId), file.getName(), null, 
//                                file.getAbsolutePath().replace(this.directory + "/", ""), file.length(), 
//                                CategoryEnum.convertToCategoryEnum(
//                                        this.filePathToCategory.get(file.getAbsoluteFile())),
//                                groupNameToGroupId.get(entry.getKey())));
//                  });
//            });
            
        
        int downloadFileId = 1;
        for (Entry<String, Set<String>> groupAndPaths : this.groupToFilePaths.entrySet()) {
            filePaths.addAll(groupAndPaths.getValue());
            String groupIdAsString = groupNameToGroupId.get(groupAndPaths.getKey());
            for (String path: groupAndPaths.getValue()) {
                String fileCategory = filePathToCategory.get(path);
                File file = new File(path);
                if (!file.exists()) {
                    throw log.throwing(
                            new IllegalArgumentException("The file " + path + "doesn't exists"));
                }
                // Currently, the file description is not use, so for the moment, we set it at null.
                downloadFileTOs.add(new DownloadFileTO(
                        String.valueOf(downloadFileId), file.getName(), null, 
                        file.getAbsolutePath().replace(this.directory + "/", ""), file.length(),
                        CategoryEnum.convertToCategoryEnum(fileCategory), groupIdAsString));
                downloadFileId++;
            }
        }
        // Sanity check on file paths
        if(!filePaths.equals(filePathToCategory.keySet())) {
            throw log.throwing(new IllegalArgumentException(
                    "Different file paths between mapping filePath-category [" + filePaths + 
                    "] and groupName-filePaths [" + filePathToCategory.keySet() + "]"));
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
            log.info("Done inserting download files, {} files inserted.", downloadFileTOs.size());
            
            this.commit();
        } finally {
            this.closeDAO();
        }

        log.info("Done inserting species data groups.");

        log.exit();
    }
}
