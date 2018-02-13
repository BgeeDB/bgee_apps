package org.bgee.pipeline.expression.downloadfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO;
import org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesDataGroupTO;
import org.bgee.model.dao.api.file.SpeciesDataGroupDAO.SpeciesToDataGroupTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class responsible for inserting the species data groups, species data groups to species mappings,
 * and download files into the Bgee database.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, May 2017
 * @since   Bgee 13, Sept 2015
 */
//FIXME: use pipeline/download_files/insert_download_file_info.sql in bgee_pipeline instead
//public class InsertSpeciesDataGroups extends MySQLDAOUser {
//    
//    /**
//     * {@code Logger} of the class. 
//     */
//    private final static Logger log = LogManager.getLogger(InsertSpeciesDataGroups.class.getName());
//    
//    /**
//     * A {@code String} that is the pattern to be replace in provided file path patterns.
//     */
//    public final static String STRING_TO_REPLACE = "\\{REPLACE\\}";
//    
//    /**
//     * An {@code int} that is the minimal number of lines in a file to not be considered as empty.
//     */
//    private final static int MIN_LINE_NUMBER = 3;
//    
//    /**
//     * Main method to trigger the insertion of the species data groups, species data groups 
//     * to species mappings, and download files into the Bgee database. 
//     * Entries of the provided {@code Map}s must be separated by {@link CommandRunner#LIST_SEPARATOR}, 
//     * keys must be separated from their associated value by {@link CommandRunner#KEY_VALUE_SEPARATOR}, 
//     * values must be separated using {@link CommandRunner#VALUE_SEPARATOR}, 
//     * see {@link org.bgee.pipeline.CommandRunner#parseMapArgument(String)}
//     * <p>
//     * Parameters that must be provided in order in {@code args} are: 
//     * <ol>
//     * <li>groupsToSpecies: a {@code LinkedHashMap} where keys are {@code String}s that are names given 
//     *     to the species data groups,  the associated value being a {@code Set} of {@code String}s 
//     *     that are the IDs of the species composing the group. The order of the entries is important, 
//     *     it is used to specify the preferred display order of the data groups. 
//     * <li>groupsToCategories: A {@code LinkedHashMap} where keys are {@code String}s that are names given 
//     *     to the species data groups, the associated value being {@code Set}s of {@code String}s 
//     *     corresponding to categories for which download files are available for the group. 
//     *     The order of the entries is important, it is used to specify the preferred 
//     *     display order of the data groups. The key set should be identical to the key set 
//     *     in the {@code Map}s defined as first and third arguments. Categories should all be present
//     *     in the key set of the {@code Map} provided either as fourth or fifth argument. 
//     *     The {@code String}s provided as categories must be mappable using 
//     *     {@link CategoryEnum#convertToCategoryEnum(String)}.
//     * <li>groupsToReplacement: A {@code LinkedHashMap} where keys are {@code String}s
//     *     that are names given to the species data groups, the associated value being 
//     *     a {@code String} to be used to replace {@link #STRING_TO_REPLACE} in the patterns provided 
//     *     as values of the {@code Map}s provided as fourth and fifth arguments. The key set 
//     *     should be identical to the key set in the two previous {@code Map}s.
//     * <li>singleSpCatToFilePattern: A {@code Map} where keys are {@code String}s that are
//     *     categories found in single species groups, the associated value being a {@code String} 
//     *     that is a pattern to get the path to the corresponding download file, 
//     *     after having replaced {@link #STRING_TO_REPLACE} with the appropriate value provided 
//     *     in the previous {@code Map}. All categories defined in the key set should be present
//     *     in the values of the previous {@code Map}.
//     * <li>multiSpCatToFilePattern: A {@code Map} where keys are {@code String}s that are
//     *     categories found in multi species groups, the associated value being a {@code String} 
//     *     that is a pattern to get the path to the corresponding download file, 
//     *     after having replaced {@link #STRING_TO_REPLACE} with the appropriate value provided 
//     *     in the {@code Map}. provided as third argument. All categories defined in the key set 
//     *     should be present in the values of this {@code Map}.
//     * <li>a {@code String} that is directory to be used to retrieve files using 
//     *     the provided related paths provided as values of the {@code Map}s provided 
//     *     as fourth and fifth argument.
//     * </ol>
//     * 
//     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
//     * @throws IOException              If a file could not be read.
//     * @throws IllegalArgumentException If the provided {@code args} do not contain 
//     *                                  requested information.
//     */
//    public static void main(String[] args) throws IOException {
//        log.entry((Object[]) args);
//        
//        int expectedArgLength = 6;
//        if (args.length != expectedArgLength) {
//            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments provided, " +
//                    "expected " + expectedArgLength + " arguments, " + args.length + " provided."));
//        }
//        
//        // For args[0] and args[1], transforms the LinkedHashMap<String, List<String>>
//        // into LinkedHashMap<String, Set<String>>.
//        // For args[2], transforms the LinkedHashMap<String, List<String>>
//        // into LinkedHashMap<String, String>.
//        // From args[3], and args[4], transforms the Map<String, List<String>>
//        // into Map<String, String>.
//        // We could use the flatMap function, but we want to perform sanity checks.
//        InsertSpeciesDataGroups insert = new InsertSpeciesDataGroups(
//                
//                //LinkedHashMap<String, Set<Integer>> groupToSpecies
//                CommandRunner.parseMapArgument(args[0]).entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<Integer>(e.getValue()
//                        .stream().map(v -> Integer.parseInt(v)).collect(Collectors.toSet())), 
//                    (k, v) -> {throw log.throwing(
//                            new IllegalArgumentException("Key used more than once: " + k));},
//                    LinkedHashMap::new)),
//                
//                //LinkedHashMap<String, Set<String>> groupToCategories
//                CommandRunner.parseMapArgument(args[1]).entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> new HashSet<String>(e.getValue()), 
//                        (k, v) -> {throw log.throwing(
//                                new IllegalArgumentException("Key used more than once: " + k));},
//                        LinkedHashMap::new)),
//                
//                //LinkedHashMap<String, String> groupToReplacements
//                CommandRunner.parseMapArgument(args[2]).entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> {
//                    if (e.getValue().size() != 1) {
//                        throw log.throwing(new IllegalArgumentException("One and only one pattern "
//                                + "can be associated to a simple category."));
//                    }
//                    return e.getValue().iterator().next();},
//                    (k, v) -> {throw log.throwing(
//                            new IllegalArgumentException("Key used more than once: " + k));},
//                    LinkedHashMap::new)),
//                
//                //Map<String, String> single sp. category to pattern
//                CommandRunner.parseMapArgument(args[3]).entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> {
//                    if (e.getValue().size() != 1) {
//                        throw log.throwing(new IllegalArgumentException("One and only one pattern "
//                                + "can be associated to a simple category."));
//                    }
//                    return e.getValue().iterator().next();
//                })),
//                
//                //Map<String, String> multi sp. category to pattern
//                CommandRunner.parseMapArgument(args[4]).entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey(), e -> {
//                    if (e.getValue().size() != 1) {
//                        throw log.throwing(new IllegalArgumentException("One and only one pattern "
//                                + "can be associated to a multi category."));
//                    }
//                    return e.getValue().iterator().next();
//                })),
//                
//                // Directory
//                CommandRunner.parseArgument(args[5]));
//        insert.insert();
//        
//        log.exit();
//    }
//
//    /**
//     * A {@code LinkedHashMap} where keys are {@code String}s that are names given to a group of species,
//     * the associated values being {@code Set}s of {@code String}s corresponding to
//     * species IDs belonging to the group. The order of the entries is important,
//     * it is used to specify the preferred display order of the data groups.
//     */
//    private final LinkedHashMap<String, Set<Integer>> groupToSpecies;
//    
//    /**
//     * A {@code LinkedHashMap} where keys are {@code String}s that are names given to a group of species,
//     * the associated values being {@code Set}s of {@code String}s corresponding to categories belonging
//     * to the group. The order of the entries is important, it is used to specify the preferred
//     * display order of the data groups. The key set should be identical to the key set in
//     * {@link #groupToSpecies} and {@link groupToReplacement}. Categories should all be present in
//     * the key set of {@link singleSpCatToFilePattern} or of {@link multiSpCatToFilePattern}.
//     */
//    private final LinkedHashMap<String, Set<String>> groupToCaterories;
//
//    /**
//     * A {@code LinkedHashMap} where keys are {@code String}s that are names given to a group of species,
//     * the associated values being {@code String}s corresponding to the string to be substituted in
//     * patterns belonging to the group. The order of the entries is important, it is used to specify
//     * the preferred display order of the data groups. The key set should be identical to the key set in
//     * {@link #groupToSpecies} and {@link groupToCategories}.
//     */
//    private final LinkedHashMap<String, String> groupToReplacement;
//
//    /**
//     * A {@code Map} where keys are {@code String}s that are categories found in single species groups,
//     * the associated values being {@code String}s corresponding to file pattern.
//     * All categories defined in the key set should be present in the values of {@link #groupToCategories}.
//     */
//    private final Map<String, String> singleSpCatToFilePattern;
//
//    /**
//     * A {@code Map} where keys are {@code String}s that are categories found in multi species groups,
//     * the associated values being {@code String}s corresponding to file pattern.
//     * All categories defined in the key set should be present in the values of {@link #groupToCategories}.
//     */
//    private final Map<String, String> multiSpCatToFilePattern;
//
//    /**
//     * A {@code String} that is the directory to be used to deduce relative paths.
//     */
//    private final String directory;
//    
//    /**
//     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
//     */
//    private final Supplier<ServiceFactory> serviceFactorySupplier;
//
//    /**
//     * Constructor requesting parameters related to download files, and using 
//     * the default {@code DAOManager}, see {@link #InsertSpeciesDataGroups(MySQLDAOManager, 
//     * LinkedHashMap, LinkedHashMap, LinkedHashMap, Map, Map, String) main constructor}.
//     * 
//     * @param groupToSpecies            See main constructor.  
//     * @param groupToCaterories         See main constructor. 
//     * @param groupToReplacement        See main constructor. 
//     * @param singleSpCatToFilePattern  See main constructor. 
//     * @param multiSpCatToFilePattern   See main constructor. 
//     * @param directory                 See main constructor. 
//     * @throws IllegalArgumentException If some mandatory parameters are incorrectly provided.
//     * @see #InsertSpeciesDataGroups(MySQLDAOManager, LinkedHashMap, LinkedHashMap, LinkedHashMap, 
//     *      Map, Map, String) main constructor
//     */
//    public InsertSpeciesDataGroups(LinkedHashMap<String, Set<Integer>> groupToSpecies,
//            LinkedHashMap<String, Set<String>> groupToCaterories,
//            LinkedHashMap<String, String> groupToReplacement,
//            Map<String, String> singleSpCatToFilePattern,
//            Map<String, String> multiSpCatToFilePattern,
//            String directory) {
//        this(null, ServiceFactory::new, groupToSpecies, groupToCaterories, groupToReplacement, 
//                singleSpCatToFilePattern, multiSpCatToFilePattern, directory);
//    }
//
//    /**
//     * Constructor providing the {@code MySQLDAOManager} that will be used by 
//     * this object to perform queries to the database, and all parameters related to download files.
//     * 
//     * @param manager                       The {@code MySQLDAOManager} to use.
//     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
//     *                                      to be able to provide one to each thread.
//     * @param groupToSpecies                A {@code LinkedHashMap} where keys are {@code String}s
//     *                                      that are names given to groups of species, the associated
//     *                                      value being a {@code Set} of {@code String}s that are
//     *                                      the IDs of the species composing the group. The order of 
//     *                                      the entries is important, it is used to specify the
//     *                                      preferred display order of the data groups.
//     * @param groupToCaterories             A {@code LinkedHashMap} where keys are {@code String}s
//     *                                      that are names given to a group of species, the associated
//     *                                      values being {@code Set}s of {@code String}s corresponding
//     *                                      to categories for which download files are available for the group. 
//     *                                      The order of the entries is important, it is used to specify 
//     *                                      the preferred display order of the data groups. The key set 
//     *                                      should be identical to the key set in {@code groupToSpecies} and
//     *                                      {@code groupToReplacement}. Categories should all be present
//     *                                      either in the key set of {@code singleSpCatToFilePattern}, or
//     *                                      in the key set of {@code multiSpCatToFilePattern}. 
//     *                                      The {@code String}s provided as categories must be mappable using 
//     *                                      {@link org.bgee.model.dao.api.file.DownloadFileDAO.DownloadFileTO.CategoryEnum#convertToCategoryEnum(String)}.
//     * @param groupToReplacement            A {@code LinkedHashMap} where keys are {@code String}s
//     *                                      that are names given to a group of species, the associated
//     *                                      values being a {@code String} to be used to replace 
//     *                                      {@link #STRING_TO_REPLACE} in the patterns provided 
//     *                                      as values of {@code singleSpCatToFilePattern} and 
//     *                                      {@code multiSpCatToFilePattern}.
//     *                                      The key set should be identical to the key set in 
//     *                                      {@code groupToSpecies} and {@code groupToCategories}.
//     * @param singleSpCatToFilePattern      A {@code Map} where keys are {@code String}s that are
//     *                                      categories found in single species groups, the associated
//     *                                      value being a {@code String} that is a pattern 
//     *                                      to get the path to the corresponding download file, 
//     *                                      after having replaced {@link #STRING_TO_REPLACE} with 
//     *                                      the appropriate value provided in {@code groupToReplacement}.
//     *                                      All categories defined in the key set should be present
//     *                                      in the values of {@code groupToCategories}.
//     * @param multiSpCatToFilePattern       A {@code Map} where keys are {@code String}s that are
//     *                                      categories found in multi species groups, the associated
//     *                                      value being a {@code String} that is a pattern 
//     *                                      to get the path to the corresponding download file, 
//     *                                      after having replaced {@link #STRING_TO_REPLACE} with 
//     *                                      the appropriate value provided in {@code groupToReplacement}.
//     *                                      All categories defined in the key set should be present
//     *                                      in the values of {@code groupToCategories}.
//     * @param directory                     A {@code String} that is the directory to be used to
//     *                                      deduce relative paths.
//     * @throws IllegalArgumentException If some mandatory parameters are incorrectly provided.
//     */
//    public InsertSpeciesDataGroups(MySQLDAOManager manager, Supplier<ServiceFactory> serviceFactorySupplier,
//            LinkedHashMap<String, Set<Integer>> groupToSpecies,
//            LinkedHashMap<String, Set<String>> groupToCaterories,
//            LinkedHashMap<String, String> groupToReplacement,
//            Map<String, String> singleSpCatToFilePattern,
//            Map<String, String> multiSpCatToFilePattern,
//            String directory) {
//        super(manager);
//        this.serviceFactorySupplier = serviceFactorySupplier;
//        log.entry(groupToSpecies, groupToCaterories, groupToReplacement, singleSpCatToFilePattern,
//                multiSpCatToFilePattern, directory);
//        if (groupToSpecies == null || groupToSpecies.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("No group-species mapping is provided"));
//        }
//        this.groupToSpecies = new LinkedHashMap<>(groupToSpecies);
//
//        if (groupToCaterories == null || groupToCaterories.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("No group-categories mapping is provided"));
//        }
//        this.groupToCaterories = new LinkedHashMap<>(groupToCaterories);
//
//        if (groupToReplacement == null || groupToReplacement.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("No group-replacement mapping is provided"));
//        }
//        this.groupToReplacement = new LinkedHashMap<>(groupToReplacement);
//        
//        if (singleSpCatToFilePattern == null || singleSpCatToFilePattern.isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("No simpleCategory-pattern mapping is provided"));
//        }
//        this.singleSpCatToFilePattern = new HashMap<>(singleSpCatToFilePattern);
//
//        // FIXME enable sanity checks on multi-species files
//        if (multiSpCatToFilePattern != null && !multiSpCatToFilePattern.isEmpty()) {
//            this.multiSpCatToFilePattern = new HashMap<>(multiSpCatToFilePattern);
//        } else {
//            this.multiSpCatToFilePattern = new HashMap<>();
//        }
//
//        if (StringUtils.isEmpty(directory)) {
//            throw log.throwing(new IllegalArgumentException("No directory is provided"));
//        }
//        this.directory = directory;
//        
//        //Checks coherence of data in the provided maps
//        if (!this.groupToSpecies.keySet().equals(this.groupToCaterories.keySet())) {
//            throw log.throwing(new IllegalArgumentException("Both maps group-speciesIds and " +
//                    "group-caterories don't have the same group names and/or store them in different orders."));
//        }
//        if (!this.groupToSpecies.keySet().equals(this.groupToReplacement.keySet())) {
//            throw log.throwing(new IllegalArgumentException("Both maps group-speciesIds and " +
//                    "group-replacement don't have the same group names and/or store them in different orders."));
//        }
//        
//        if (!this.singleSpCatToFilePattern.keySet().equals(
//                this.groupToCaterories.entrySet().stream()
//                .filter(e -> this.groupToSpecies.get(e.getKey()).size() == 1)
//                .flatMap(e -> e.getValue().stream())
//                .collect(Collectors.toSet()))) {
//            throw log.throwing(new IllegalArgumentException("Different categories between "
//                    + "mappings singleSpCategory-filePattern [" + this.singleSpCatToFilePattern.keySet()
//                    + "] and group-category with one species ["
//                    + this.groupToCaterories.values().stream().filter(e -> e.size() == 1)
//                    .flatMap(e -> e.stream())
//                    .collect(Collectors.toSet()) + "]"));
//        }
//
//        // FIXME enable sanity checks on multi-species files
////        if (!this.multiSpCatToFilePattern.keySet().equals(
////                this.groupToCaterories.entrySet().stream()
////                .filter(e -> this.groupToSpecies.get(e.getKey()).size() > 1)
////                .flatMap(e -> e.getValue().stream())
////                .collect(Collectors.toSet()))) {
////            throw log.throwing(new IllegalArgumentException("Different categories between "
////                    + "mappings multiSpCategory-filePattern [" + this.multiSpCatToFilePattern.keySet()
////                    + "] and group-category with several species ["
////                    + this.groupToCaterories.values().stream().filter(e -> e.size() > 1)
////                    .flatMap(e -> e.stream())
////                    .collect(Collectors.toSet()) + "]"));
////        }
//        //sanity check on species IDs
//        BgeeDBUtils.checkAndGetSpeciesIds(
//                new ArrayList<Integer>(this.groupToSpecies.values().stream()
//                .flatMap(e -> e.stream()).collect(Collectors.toSet())), this.getSpeciesDAO());
//        
//        log.exit();
//    }
//    
//    /**
//     * Insert the species data groups, species data groups to species mappings, 
//     * and download files into the Bgee database, using the information provided at instantiation.
//     * Note that this method should be used to populate empty tables.
//     * 
//     * @throws IOException  If a file could not be read.
//     */
//    public void insert() throws IOException {
//        log.entry();
//        
//        // FIXME enable creation of SpeciesDataGroupTOs and SpeciesToDataGroupTOs
////        // First, we create SpeciesDataGroupTOs and SpeciesToDataGroupTOs
////        Set<SpeciesToDataGroupTO> speciesToDataGroupTOs = new HashSet<SpeciesToDataGroupTO>();
////        //we store the SpeciesDataGroups associated to their name, to be able 
////        //to easily retrieve the generated ID of a SpeciesDataGroup from its name 
////        //(and also to insert the SpeciesDataGroups). 
////        Map<String, SpeciesDataGroupTO> speciesDataGroupTOs = new HashMap<>(); 
////        int i = 1;
////        for (Entry<String, Set<Integer>> groupToSpecies: this.groupToSpecies.entrySet()) {
////            if (StringUtils.isBlank(groupToSpecies.getKey())) {
////                throw log.throwing(new IllegalStateException("No group name can be blank."));
////            }
////            if (groupToSpecies.getValue().isEmpty()) {
////                throw log.throwing(new IllegalStateException(
////                        "No species in group " + groupToSpecies.getKey()));
////            }
////            final String groupName = groupToSpecies.getKey().trim();
////            final Integer groupId = i;
////            speciesDataGroupTOs.put(groupName, 
////                    //we also use i to generate the preferred order
////                    new SpeciesDataGroupTO(groupId, groupName, null, i));
////            speciesToDataGroupTOs.addAll(groupToSpecies.getValue().stream()
////                    .map(e -> new SpeciesToDataGroupTO(e, groupId))
////                .collect(Collectors.toSet()));
////            i++;
////        }
//
////        List<SpeciesDataGroup> groups = serviceFactorySupplier.get().getSpeciesDataGroupService()
////                .loadAllSpeciesDataGroup();
//        Set<Species> list = serviceFactorySupplier.get().getSpeciesService().loadSpeciesByIds(null, false);
//        List<SpeciesDataGroup> groups = list.stream()
//                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder))
//                .map(sp -> new SpeciesDataGroup(sp.getId(), sp.getName(), sp.getDescription(),
//                        Arrays.asList(sp), sp.getId() == 9606?
//                                Collections.singleton(
//                                        new DownloadFile("path", "name", DownloadFile.CategoryEnum.RNASEQ_DATA, 30L, sp.getId())): 
//                                    new HashSet<>(Arrays.asList(
//                                new DownloadFile("SIMPLE/ANAT_ENTITY", "ANAT_ENTITY",
//                                        DownloadFile.CategoryEnum.EXPR_CALLS_SIMPLE, 10L, sp.getId(),
//                                        Arrays.asList(DownloadFile.ConditionParameter.ANAT_ENTITY)),
//                                new DownloadFile("COMPLETE/ANAT_ENTITY", "ANAT_ENTITY",
//                                        DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE, 10L, sp.getId(),
//                                        Arrays.asList(DownloadFile.ConditionParameter.ANAT_ENTITY)),
//                                new DownloadFile("SIMPLE/ANAT_ENTITYandDEV_STAGE", "ANAT_ENTITYandDEV_STAGE",
//                                        DownloadFile.CategoryEnum.EXPR_CALLS_SIMPLE, 10L, sp.getId(),
//                                        Arrays.asList(DownloadFile.ConditionParameter.ANAT_ENTITY, DownloadFile.ConditionParameter.DEV_STAGE)),
//                                new DownloadFile("COMPLETE/ANAT_ENTITYandDEV_STAGE", "ANAT_ENTITYandDEV_STAGE",
//                                        DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE, 10L, sp.getId(),
//                                        Arrays.asList(DownloadFile.ConditionParameter.ANAT_ENTITY, DownloadFile.ConditionParameter.DEV_STAGE)),
//                                new DownloadFile("path", "name", DownloadFile.CategoryEnum.RNASEQ_DATA, 30L, sp.getId()),
//                                new DownloadFile("path", "name", DownloadFile.CategoryEnum.AFFY_DATA, 40L, sp.getId())))))
//                .filter(group -> group.getId().equals(9606) || group.getId().equals(10090))
//                .collect(Collectors.toList());
//
//        // Then, we create DownloadFileTOs
//        Set<DownloadFileTO> downloadFileTOs = new HashSet<>();
//        int downloadFileId = 1;
//        //We store the IDs of groups that actually have valid download files existing
//        Set<Integer> groupIdsWithData = new HashSet<>();
//        for (Entry<String, Set<String>> groupAndCategories : this.groupToCaterories.entrySet()) {
//
//            String groupName = groupAndCategories.getKey();
//            Integer groupId = groups.stream()
//                    .filter(g -> groupName.equals(g.getName()))
//                    .map(g -> g.getId())
//                    .findFirst().get();
//            int speciesCount = this.groupToSpecies.get(groupName).size();
//            
//            for (String category: groupAndCategories.getValue()) {
//                String pattern = null;
//                if (speciesCount > 1) {
//                    pattern = this.multiSpCatToFilePattern.get(category);
//                } else if (speciesCount == 1) {
//                    pattern = this.singleSpCatToFilePattern.get(category);
//                } else {
//                    throw log.throwing(new IllegalArgumentException(
//                            "The group [" + groupName + "] have no species"));
//                }
//                String path = pattern.replaceAll(STRING_TO_REPLACE, 
//                        this.groupToReplacement.get(groupName)).trim();
//                File file = new File(this.directory, path);
//                //if the file doesn't exist or is empty, we just skip it, this is useful
//                //if there were no data for a file type in a group, we don't have to change 
//                //the arguments
////                if (!file.exists()) {
////                    log.warn("File not existing, skipping: {}", file);
////                    continue;
////                } else if (file.isDirectory()) {
////                    throw log.throwing(new IllegalArgumentException(
////                            "The file " + file.getAbsolutePath() + " is a directory."));
////                } else if (!this.hasEnoughLines(file)) {
////                    log.warn("File is empty, skipping: {}", file);
////                    continue;
////                }
//                List<ConditionDAO.Attribute> conditionParams = null;
//                // We need to define which the condition parameters was used to generate the file
//                if (category.contains(DownloadFile.CategoryEnum.EXPR_CALLS_SIMPLE.getStringRepresentation())
//                        || category.contains(DownloadFile.CategoryEnum.EXPR_CALLS_COMPLETE.getStringRepresentation())) {
//                    conditionParams = new ArrayList<>();
//                    conditionParams.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
//                    if (file.getName().contains("development")) {
//                        conditionParams.add(ConditionDAO.Attribute.STAGE_ID);
//                    }
//                }
//                Long fileLength = 10L;
//                // Currently, the file description is not use, so for the moment, we set it to null.
//                downloadFileTOs.add(new DownloadFileTO(
//                        downloadFileId, file.getName(), null, path, fileLength,
//                        this.getCategoryEnum(category), groupId,
//                        conditionParams));
//                groupIdsWithData.add(groupId);
//                downloadFileId++;
//            }
//        }
//
//        log.info("Start inserting species data groups...");
//
//        try {
//            this.startTransaction();
//            
//            //we only insert groups with actual download files existing
//            
//            // FIXME enable insertion of SpeciesDataGroupTOs and SpeciesToDataGroupTOs
////            // Insertion of SpeciesDataGroupTOs
////            log.debug("Start inserting species data groups...");
////            Set<SpeciesDataGroupTO> filteredSpeciesDataGroupTOs = speciesDataGroupTOs.values()
////                    .stream().filter(e -> groupIdsWithData.contains(e.getId()))
////                    .collect(Collectors.toSet());
////            this.getSpeciesDataGroupDAO().insertSpeciesDataGroups(filteredSpeciesDataGroupTOs);
////            log.debug("Done inserting species data groups, {} groups inserted", 
////                    filteredSpeciesDataGroupTOs.size());
////
////            // Insertion of SpeciesToDataGroupTOs
////            log.debug("Start inserting species data groups to species mappings...");
////            Set<SpeciesToDataGroupTO> filteredSpeciesToDataGroupTOs = speciesToDataGroupTOs
////                    .stream().filter(e -> groupIdsWithData.contains(e.getGroupId()))
////                    .collect(Collectors.toSet());
////            this.getSpeciesDataGroupDAO().insertSpeciesToDataGroup(filteredSpeciesToDataGroupTOs);
////            log.debug("Done inserting species data groups to species mappings, {} mappings inserted", 
////                    filteredSpeciesToDataGroupTOs.size());
//            
//            // Insertion of DownloadFileTOs
//            log.debug("Start inserting download files...");
//
//            log.info("Download files: {}", downloadFileTOs);
//
////            this.getDownloadFileDAO().insertDownloadFiles(downloadFileTOs);
//            log.debug("Done inserting download files, {} files inserted.", downloadFileTOs.size());
//            
//            this.commit();
//        } finally {
//            this.closeDAO();
//        }
//
//        log.info("Done inserting species data groups.");
//        log.exit();
//    }
//
//    private CategoryEnum getCategoryEnum(String category) {
//        log.entry(category);
//
//        DownloadFile.CategoryEnum serviceCategory =
//                DownloadFile.CategoryEnum.convertToCategoryEnum(category.replace("_dev", ""));
//        switch (serviceCategory) {
//                case EXPR_CALLS_SIMPLE:
//                    return log.exit(DownloadFileTO.CategoryEnum.EXPR_CALLS_SIMPLE);
//                case EXPR_CALLS_COMPLETE:
//                    return log.exit(DownloadFileTO.CategoryEnum.EXPR_CALLS_COMPLETE);
//                case DIFF_EXPR_ANAT_SIMPLE:
//                case DIFF_EXPR_ANAT_COMPLETE:
//                case DIFF_EXPR_DEV_COMPLETE:
//                case DIFF_EXPR_DEV_SIMPLE:
//                case ORTHOLOG:
//                case AFFY_ANNOT:
//                case AFFY_DATA:
//                case RNASEQ_ANNOT:
//                case RNASEQ_DATA:
//                    return log.exit(DownloadFileTO.CategoryEnum.convertToCategoryEnum(
//                            serviceCategory.getStringRepresentation()));
//                default:
//                    throw new IllegalArgumentException("Category not supported: " + serviceCategory);
//            }
//    }
//
//    /**
//     * Determines whether the provided files contains enough lines to be considered valid.
//     * 
//     * @param file          A {@code File} that is the file to check.
//     * @return              A {@code boolean} that is {@code true} if the provided file is valid, 
//     *                      {@code false} otherwise.
//     * @throws IOException  If {@code filePath} could not be read.
//     * @see #MIN_LINE_NUMBER
//     */
//    private boolean hasEnoughLines(File file) throws IOException {
//        log.entry(file);
//        
//        //FIXME: disabling this feature until we actually use uncompressed files 
//        //to get more info about them see issue #69
//        //In the meantime, we define a threshold based on the size
////        try (Stream<String> lines = Files.lines(Paths.get(filePath)).limit(MIN_LINE_NUMBER)) {
////            return log.exit(lines.count() >= MIN_LINE_NUMBER);
////        }
//        return log.exit(file.length() >= 1024L);
//    }
//}
