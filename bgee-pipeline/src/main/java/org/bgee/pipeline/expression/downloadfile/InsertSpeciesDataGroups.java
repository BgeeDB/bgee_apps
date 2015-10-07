package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * A {@code String} that is the pattern to be replace in provided file path patterns.
     */
    private final static String STRING_TO_REPLACE = "\\{REPLACE\\}";
    
    /**
     * A {@code long} that is the minimal number of lines in a file to not be considered as empty.
     */
    private final static long MIN_LINE_NUMBER = 3L;
    
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
     * @throws IOException              If a file could not be read.
     * @throws IllegalArgumentException If the provided {@code args} do not contain 
     *                                  requested information.
     */
    public static void main(String[] args) throws IOException {
        log.entry((Object[]) args);
        
        int expectedArgLength = 6;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, " +
                    "expected " + expectedArgLength + " arguments, " + args.length + " provided."));
        }
        
        // For args[0] and args[1], transforms the LinkedHashMap<String, List<String>>
        // into LinkedHashMap<String, Set<String>>.
        // For args[2], transforms the LinkedHashMap<String, List<String>>
        // into LinkedHashMap<String, String>.
        // From args[3], and args[4], transforms the Map<String, List<String>>
        // into Map<String, String>.
        // We could use the flatMap function, but we want to perform sanity checks.
        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(
                
                //LinkedHashMap<String, Set<String>> groupToSpecies
                CommandRunner.parseMapArgument(args[0]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<String>(e.getValue()), 
                    (k, v) -> {throw log.throwing(
                            new IllegalArgumentException("Key used more than once: " + k));},
                    LinkedHashMap::new)),
                
                //LinkedHashMap<String, Set<String>> groupToCategories
                CommandRunner.parseMapArgument(args[1]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<String>(e.getValue()), 
                        (k, v) -> {throw log.throwing(
                                new IllegalArgumentException("Key used more than once: " + k));},
                        LinkedHashMap::new)),
                
                //LinkedHashMap<String, Set<String>> groupToSpecies
                CommandRunner.parseMapArgument(args[2]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    if (e.getValue().size() != 1) {
                        throw log.throwing(new IllegalArgumentException("One and only one pattern "
                                + "can be associated to a simple category."));
                    }
                    return e.getValue().iterator().next();},
                    (k, v) -> {throw log.throwing(
                            new IllegalArgumentException("Key used more than once: " + k));},
                    LinkedHashMap::new)),
                
                //Map<String, String> single sp. category to pattern
                CommandRunner.parseMapArgument(args[3]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    if (e.getValue().size() != 1) {
                        throw log.throwing(new IllegalArgumentException("One and only one pattern "
                                + "can be associated to a simple category."));
                    }
                    return e.getValue().iterator().next();
                })),
                
                //Map<String, String> multi sp. category to pattern
                CommandRunner.parseMapArgument(args[4]).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    if (e.getValue().size() != 1) {
                        throw log.throwing(new IllegalArgumentException("One and only one pattern "
                                + "can be associated to a multi category."));
                    }
                    return e.getValue().iterator().next();
                })),
                
                // Directory
                CommandRunner.parseArgument(args[5]));
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
     * the associated values being {@code Set}s of {@code String}s corresponding to categories belonging
     * to the group. The order of the entries is important, it is used to specify the preferred
     * display order of the data groups. The key set should be identical to the key set in
     * {@link #groupToSpecies} and {@link groupToReplacement}. Categories should all be present in
     * the key set of {@link singleSpCategoryToFilePattern} or of {@link multiSpCategoryToFilePattern}.
     */
    private final LinkedHashMap<String, Set<String>> groupToCaterories;

    /**
     * A {@code LinkedHashMap} where keys are {@code String}s that are names given to a group of species,
     * the associated values being {@code String}s corresponding to the string to be substituted in
     * patterns belonging to the group. The order of the entries is important, it is used to specify
     * the preferred display order of the data groups. The key set should be identical to the key set in
     * {@link #groupToSpecies} and {@link groupToCategories}.
     */
    private final LinkedHashMap<String, String> groupToReplacement;

    /**
     * A {@code Map} where keys are {@code String}s that are categories found in single species groups,
     * the associated values being {@code String}s corresponding to file pattern.
     * All categories defined in the key set should be present in the values of {@link #groupToCategories}.
     */
    private final Map<String, String> singleSpCategoryToFilePattern;

    /**
     * A {@code Map} where keys are {@code String}s that are categories found in multi species groups,
     * the associated values being {@code String}s corresponding to file pattern.
     * All categories defined in the key set should be present in the values of {@link #groupToCategories}.
     */
    private final Map<String, String> multiSpCategoryToFilePattern;

    /**
     * A {@code String} that is the directory to be used to deduce relative paths.
     */
    private final String directory;
    
    /**
     * Constructor requesting all mandatory parameters.
     * 
     * @param groupToSpecies                A {@code LinkedHashMap} where keys are {@code String}s
     *                                      that are names given to groups of species, the associated
     *                                      value being a {@code Set} of {@code String}s that are
     *                                      the IDs of the species composing the group. The order of 
     *                                      the entries is important, it is used to specify the
     *                                      preferred display order of the data groups.
     * @param groupToCaterories             A {@code LinkedHashMap} where keys are {@code String}s
     *                                      that are names given to a group of species, the associated
     *                                      values being {@code Set}s of {@code String}s corresponding
     *                                      to categories belonging to the group. The order of the
     *                                      entries is important, it is used to specify the preferred
     *                                      display order of the data groups. The key set should be
     *                                      identical to the key set in {@link #groupToSpecies} and
     *                                      {@link groupToReplacement}. Categories should all be present
     *                                      in the key set of {@link singleSpCategoryToFilePattern} or
     *                                      of {@link multiSpCategoryToFilePattern}.
     * @param groupToReplacement            A {@code LinkedHashMap} where keys are {@code String}s
     *                                      that are names given to a group of species, the associated
     *                                      values being {@code String}s corresponding to the string
     *                                      to be substituted in patterns belonging to the group.
     *                                      The order of the entries is important, it is used to
     *                                      specify the preferred display order of the data groups.
     *                                      The key set should be identical to the key set in 
     *                                      {@link #groupToSpecies} and {@link groupToCategories}.
     * @param singleSpCategoryToFilePattern A {@code Map} where keys are {@code String}s that are
     *                                      categories found in single species groups, the associated
     *                                      values being {@code String}s corresponding to file pattern.
     *                                      All categories defined in the key set should be present
     *                                      in the values of {@link #groupToCategories}.
     * @param multiSpCategoryToFilePattern  A {@code Map} where keys are {@code String}s that are
     *                                      categories found in multi species groups, the associated
     *                                      values being {@code String}s corresponding to file pattern.
     *                                      All categories defined in the key set should be present
     *                                      in the values of {@link #groupToCategories}.
     * @param directory                     A {@code String} that is the directory to be used to
     *                                      deduce relative paths.
     * @throws IllegalArgumentException If some mandatory parameters are incorrectly provided.
     */
    public InsertSpeciesDataGroups(LinkedHashMap<String, Set<String>> groupToSpecies,
            LinkedHashMap<String, Set<String>> groupToCaterories,
            LinkedHashMap<String, String> groupToReplacement,
            Map<String, String> singleSpCategoryToFilePattern,
            Map<String, String> multiSpCategoryToFilePattern,
            String directory) {
        this(null, groupToSpecies, groupToCaterories, groupToReplacement, 
                singleSpCategoryToFilePattern, multiSpCategoryToFilePattern, directory);
    }

    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @param groupToSpecies                A {@code LinkedHashMap} where keys are {@code String}s
     *                                      that are names given to groups of species, the associated
     *                                      value being a {@code Set} of {@code String}s that are
     *                                      the IDs of the species composing the group. The order of 
     *                                      the entries is important, it is used to specify the
     *                                      preferred display order of the data groups.
     * @param groupToCaterories             A {@code LinkedHashMap} where keys are {@code String}s
     *                                      that are names given to a group of species, the associated
     *                                      values being {@code Set}s of {@code String}s corresponding
     *                                      to categories belonging to the group. The order of the
     *                                      entries is important, it is used to specify the preferred
     *                                      display order of the data groups. The key set should be
     *                                      identical to the key set in {@link #groupToSpecies} and
     *                                      {@link groupToReplacement}. Categories should all be present
     *                                      in the key set of {@link singleSpCategoryToFilePattern} or
     *                                      of {@link multiSpCategoryToFilePattern}.
     * @param groupToReplacement            A {@code LinkedHashMap} where keys are {@code String}s
     *                                      that are names given to a group of species, the associated
     *                                      values being {@code String}s corresponding to the string
     *                                      to be substituted in patterns belonging to the group.
     *                                      The order of the entries is important, it is used to
     *                                      specify the preferred display order of the data groups.
     *                                      The key set should be identical to the key set in 
     *                                      {@link #groupToSpecies} and {@link groupToCategories}.
     * @param singleSpCategoryToFilePattern A {@code Map} where keys are {@code String}s that are
     *                                      categories found in single species groups, the associated
     *                                      values being {@code String}s corresponding to file pattern.
     *                                      All categories defined in the key set should be present
     *                                      in the values of {@link #groupToCategories}.
     * @param multiSpCategoryToFilePattern  A {@code Map} where keys are {@code String}s that are
     *                                      categories found in multi species groups, the associated
     *                                      values being {@code String}s corresponding to file pattern.
     *                                      All categories defined in the key set should be present
     *                                      in the values of {@link #groupToCategories}.
     * @param directory                     A {@code String} that is the directory to be used to 
     *                                      deduce relative paths.
     * @throws IllegalArgumentException If some mandatory parameters are incorrectly provided.
     */
    public InsertSpeciesDataGroups(MySQLDAOManager manager,
            LinkedHashMap<String, Set<String>> groupToSpecies,
            LinkedHashMap<String, Set<String>> groupToCaterories,
            LinkedHashMap<String, String> groupToReplacement,
            Map<String, String> singleSpCategoryToFilePattern,
            Map<String, String> multiSpCategoryToFilePattern,
            String directory) {
        super(manager);
        log.entry(groupToSpecies, groupToCaterories, groupToReplacement, singleSpCategoryToFilePattern,
                multiSpCategoryToFilePattern, directory);
        if (groupToSpecies == null || groupToSpecies.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-species mapping is provided"));
        }
        this.groupToSpecies = new LinkedHashMap<>(groupToSpecies);

        if (groupToCaterories == null || groupToCaterories.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-categories mapping is provided"));
        }
        this.groupToCaterories = new LinkedHashMap<>(groupToCaterories);

        if (groupToReplacement == null || groupToReplacement.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No group-replacement mapping is provided"));
        }
        this.groupToReplacement = new LinkedHashMap<>(groupToReplacement);
        
        if (singleSpCategoryToFilePattern == null || singleSpCategoryToFilePattern.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No simpleCategory-pattern mapping is provided"));
        }
        this.singleSpCategoryToFilePattern = new HashMap<>(singleSpCategoryToFilePattern);

        if (multiSpCategoryToFilePattern == null || multiSpCategoryToFilePattern.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No multiCategory-pattern mapping is provided"));
        }
        this.multiSpCategoryToFilePattern = new HashMap<>(multiSpCategoryToFilePattern);

        if (StringUtils.isEmpty(directory)) {
            throw log.throwing(new IllegalArgumentException("No directory is provided"));
        }
        this.directory = directory;
        
        //Checks coherence of data in the provided maps
        if (!this.groupToSpecies.keySet().equals(this.groupToCaterories.keySet())) {
            throw log.throwing(new IllegalArgumentException("Both maps group-speciesIds and " +
                    "group-caterories don't have the same group names and/or store them in different orders."));
        }
        if (!this.groupToSpecies.keySet().equals(this.groupToReplacement.keySet())) {
            throw log.throwing(new IllegalArgumentException("Both maps group-speciesIds and " +
                    "group-replacement don't have the same group names and/or store them in different orders."));
        }
        
        if (!this.singleSpCategoryToFilePattern.keySet().equals(
                this.groupToCaterories.entrySet().stream()
                .filter(e -> this.getSingleSpeciesGroups(this.groupToSpecies).contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet()))) {
            throw log.throwing(new IllegalArgumentException("Different categories between "
                    + "mappings singleSpCategory-filePattern [" + this.singleSpCategoryToFilePattern.keySet()
                    + "] and group-category with one species ["
                    + this.groupToCaterories.values().stream().filter(e -> e.size() == 1)
                    .flatMap(e -> e.stream())
                    .collect(Collectors.toSet()) + "]"));
        }

        if (!this.multiSpCategoryToFilePattern.keySet().equals(
                this.groupToCaterories.entrySet().stream()
                .filter(e -> this.getMultiSpeciesGroups(this.groupToSpecies).contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet()))) {
            throw log.throwing(new IllegalArgumentException("Different categories between "
                    + "mappings multiSpCategory-filePattern [" + this.multiSpCategoryToFilePattern.keySet()
                    + "] and group-category with several species ["
                    + this.groupToCaterories.values().stream().filter(e -> e.size() > 1)
                    .flatMap(e -> e.stream())
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
     * 
     * @throws IOException  If a file could not be read.
     */
    public void insert() throws IOException {
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
            if (groupToSpecies.getValue().isEmpty()) {
                throw log.throwing(new IllegalStateException(
                        "No species in group " + groupToSpecies.getKey()));
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
        Set<String> singleSpGroups = this.getSingleSpeciesGroups(this.groupToSpecies);
        Set<String> multiSpGroups = this.getMultiSpeciesGroups(this.groupToSpecies);
        int downloadFileId = 1;
        //We store the IDs of groups that actually have download files existing
        Set<String> groupIdsWithData = new HashSet<String>();
        for (Entry<String, Set<String>> groupAndCategories : this.groupToCaterories.entrySet()) {
            String groupId = speciesDataGroupTOs.get(groupAndCategories.getKey()).getId();
            for (String category: groupAndCategories.getValue()) {
                String pattern = null;
                if (multiSpGroups.contains(groupAndCategories.getKey())) {
                    pattern = this.multiSpCategoryToFilePattern.get(category);
                } else if (singleSpGroups.contains(groupAndCategories.getKey())) {
                    pattern = this.singleSpCategoryToFilePattern.get(category);
                } else {
                    throw log.throwing(new IllegalArgumentException(
                            "The group [" + groupAndCategories.getKey() + "] have no species"));
                }
                String path = pattern.replaceAll(STRING_TO_REPLACE, this.groupToReplacement.get(groupAndCategories.getKey()));
                File file = new File(this.directory, path);
                //if the file doesn't exist or is empty, we just skip it, this is useful
                //if there were no data for a file type in a group, we don't have to change 
                //the arguments
                if (!file.exists()) {
                    log.warn("File not existing, skipping: {}", file);
                    continue;
                }
                
                if (this.countFileLines(file.getAbsolutePath()) < MIN_LINE_NUMBER) {
                    log.warn("File is empty, skipping: {}", file);
                    continue;
                }

                if (file.isDirectory()) {
                    throw log.throwing(new IllegalArgumentException(
                            "The file " + file.getAbsolutePath() + " is a directory."));
                }
                // Currently, the file description is not use, so for the moment, we set it at null.
                downloadFileTOs.add(new DownloadFileTO(
                        String.valueOf(downloadFileId), file.getName(), null, path.trim(), file.length(),
                        CategoryEnum.convertToCategoryEnum(category), groupId));
                groupIdsWithData.add(groupId);
                downloadFileId++;
            }

        }

        log.info("Start inserting species data groups...");

        try {
            this.startTransaction();
            
            //we only insert groups with actual download files existing
            
            // Insertion of SpeciesDataGroupTOs
            log.debug("Start inserting species data groups...");
            Set<SpeciesDataGroupTO> filteredSpeciesDataGroupTOs = speciesDataGroupTOs.values()
                    .stream().filter(e -> groupIdsWithData.contains(e.getId()))
                    .collect(Collectors.toSet());
            this.getSpeciesDataGroupDAO().insertSpeciesDataGroups(filteredSpeciesDataGroupTOs);
            log.debug("Done inserting species data groups, {} groups inserted", 
                    filteredSpeciesDataGroupTOs.size());

            // Insertion of SpeciesToDataGroupTOs
            log.debug("Start inserting species data groups to species mappings...");
            Set<SpeciesToDataGroupTO> filteredSpeciesToDataGroupTOs = speciesToDataGroupTOs
                    .stream().filter(e -> groupIdsWithData.contains(e.getGroupId()))
                    .collect(Collectors.toSet());
            this.getSpeciesDataGroupDAO().insertSpeciesToDataGroup(filteredSpeciesToDataGroupTOs);
            log.debug("Done inserting species data groups to species mappings, {} mappings inserted", 
                    filteredSpeciesToDataGroupTOs.size());
            
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
    
    /**
     * Retrieve group names composed by only one species.
     * 
     * @param groupToSpecies    A {@code LinkedHashMap} where keys are {@code String}s that are names
     *                          given to groups of species, the associated value being a {@code Set}
     *                          of {@code String}s that are the IDs of the species composing the group.
     * @return                  The {@code Set} of {@code String}s that are the group names
     *                          composed by one species.
     */
    private Set<String> getMultiSpeciesGroups(LinkedHashMap<String, Set<String>> groupToSpecies) {
        log.entry(groupToSpecies);
        return log.exit(groupToSpecies.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey())
                .collect(Collectors.toSet()));
    }

    /**
     * Retrieve group names composed by more than one species.
     * 
     * @param groupToSpecies    A {@code LinkedHashMap} where keys are {@code String}s that are names
     *                          given to groups of species, the associated value being a {@code Set}
     *                          of {@code String}s that are the IDs of the species composing the group.
     * @return                  The {@code Set} of {@code String}s that are the group names
     *                          composed by more than one species.
     */
    private Set<String> getSingleSpeciesGroups(LinkedHashMap<String, Set<String>> groupToSpecies) {
        log.entry(groupToSpecies);
        return log.exit(groupToSpecies.entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .map(e -> e.getKey())
                .collect(Collectors.toSet()));
    }

    /**
     * Count the number of lines in the provided file.
     * 
     * @param filePath      A {@code String} that is the file path to be used.
     * @return              The {@code long} that is the number of lines in the provided file.
     * @throws IOException  If {@code filePath} could not be read.
     */
    private long countFileLines(String filePath) throws IOException {
        log.entry(filePath);
        
        try (Stream<String> lines = Files.lines(Paths.get(filePath)).limit(MIN_LINE_NUMBER)) {
            return log.exit(lines.count());
        }
    }
}
