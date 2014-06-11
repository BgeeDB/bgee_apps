package org.bgee.pipeline.annotations;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.OntologyUtils;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.uberon.Uberon;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class to perform tasks common to all annotations used in Bgee (similarity annotations, 
 * expression data annotations, ...).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnnotationCommon {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = 
            LogManager.getLogger(AnnotationCommon.class.getName());
    
    /**
     * An unmodifiable {@code List} of {@code String}s that are the potential names 
     * of columns containing anatomical entity IDs, in annotation files (for instance, 
     * expression data annotation files assign samples to one anatomical entity).
     * We allow multiple values because maybe this is inconsistent in the various 
     * annotation files. These values are ordered by order of preference of use.
     */
    public static final List<String> ANAT_ENTITY_COL_NAMES = Collections.unmodifiableList(
            Arrays.asList(Uberon.UBERON_ENTITY_ID_COL, "UberonId", "anatEntityId"));
    /**
     * An unmodifiable {@code List} of {@code String}s that are the potential names 
     * of columns containing multiple anatomical entity IDs, in annotation files 
     * (for instance, in the similarity annotation file, to describe homology between 
     * "lung" and "swim bladder", we would use the syntax "UBERON:0002048|UBERON:0006860").
     * We allow multiple values because maybe this is inconsistent in the various 
     * annotation files. These values are ordered by order of preference of use. 
     * Allowed separators between entities are listed in {@link #ENTITY_SEPARATORS}.
     */
    public static final List<String> MULTIPLE_ANAT_ENTITY_COL_NAMES = 
            Collections.unmodifiableList(
                    Arrays.asList(SimilarityAnnotation.ENTITY_COL_NAME));
    
    /**
     * A {@code String} that is the default separator between entities, in columns 
     * containing multiple entities, in annotation files. See {@link #ENTITY_SEPARATORS}.
     * @see #ENTITY_SEPARATORS
     */
    public final static String DEFAULT_ENTITY_SEPARATOR = "|";
    
    /**
     * An unmodifiable {@code Set} of {@code String}s that are the allowed separators 
     * between entities in columns containing multiple entities, in annotation files. 
     * See {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES} for an example of such columns.
     * 
     * @see #DEFAULT_ENTITY_SEPARATOR
     */
    public final static Set<String> ENTITY_SEPARATORS = 
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList(DEFAULT_ENTITY_SEPARATOR, ",")));
    
    /**
     * Actions that can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "filterInfoFiles", the action 
     * will be to filter the information obtained following simplification of Uberon, 
     * to contain only anatomical entities used in our annotations. See {@link 
     * #filterUberonSimplificationInfo(OWLOntology, Set, Set, Set, String, boolean)} 
     * for more details. 
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>The path to the original ontology, as before the simplification process
     *   <li>A list of paths to information files generated from Uberon simplification, 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>A list of paths to annotation files using single anatomical entities 
     *   for annotations (see {@link #ANAT_ENTITY_COL_NAMES}), 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>A list of paths to annotation files using multiple anatomical entities 
     *   for annotations (see {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES}), 
     *   separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     *   <li>The path to the directory where to store filtered info files. 
     *   <li>A boolean value defining whether a term should be filtered 
     *   if one of its parent by an is_a/part_of relation is also present in the same info file, 
     *   and this parent is not a member of a non-informative subset. If {@code true}, 
     *   such children will be filtered and will not appear in the filtered file.
     *   </ol>
     * </ul>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException             
     * @throws FileNotFoundException 
     * @throws OBOFormatParserException 
     * @throws OWLOntologyCreationException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, 
        OWLOntologyCreationException, OBOFormatParserException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("filterInfoFiles")) {
            if (args.length != 7) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "7 arguments, " + args.length + " provided."));
            }
            
            AnnotationCommon.filterUberonSimplificationInfo(
                    OntologyUtils.loadOntology(args[1]), 
                    new HashSet<String>(CommandRunner.parseListArgument(args[2])), 
                    new HashSet<String>(CommandRunner.parseListArgument(args[3])), 
                    new HashSet<String>(CommandRunner.parseListArgument(args[4])), 
                    args[5], Boolean.valueOf(args[6]));
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
            		"is not recognized: " + args[0]));
        }
        
        log.exit();
    }
    
    /**
     * Filter the anatomical entities reported from {@link 
     * org.bgee.pipeline.uberon.Uberon#saveSimplificationInfo(OWLOntology, String, Collection) 
     * Uberon#saveSimplificationInfo} method, to keep only those used in our annotations. 
     * This method will generate new files, corresponding to those listed in {@code infoFiles}, 
     * in the directory {@code filteredFileDirectory} (so make sure you provide a different 
     * directory from where original info files are stored). 
     * <p>
     * The {@code OWLOntology} {@code originalOnt} will be used to retrieve 
     * the ancestors of terms, to retrieve mappings from XRef IDs to {@code OWLClass} IDs 
     * (in case an annotation used a xRef), and mapping from obsolete IDs to replacement IDs 
     * (in case an annotation used an obsolete ID). It must be the original 
     * {@code OWLOntology}, as before simplification, that must be provided.
     * <p>
     * It is possible, using the {@code filterUsingParents} argument, to define whether 
     * a term should be filtered if one of its parent by an is_a/part_of relation 
     * is also present in the same info file, and this parent is not a member of 
     * a non-informative subset. If {@code true}, such children will be filtered 
     * and will not appear in the filtered file.
     * <p>
     * This method must be provided with the list of information files generated by 
     * {@code saveSimplificationInfo}, with the list of annotation files using 
     * single anatomical entity for annotations (see {@link #ANAT_ENTITY_COL_NAMES} 
     * for details), and with the list of annotation files using multiple anatomical entities 
     * for annotations (see {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES} for details).
     * <p>
     * Files generated by {@code saveSimplificationInfo} are expected to have a header, 
     * and a column whose name is listed in {@link #ANAT_ENTITY_COL_NAMES}. 
     * <p>
     * Annotation files using single anatomical entity for annotations are expected 
     * to have a header, and a column whose name is listed in {@link #ANAT_ENTITY_COL_NAMES}.
     * <p>
     * Annotation files using multiple anatomical entities for annotations are expected 
     * to have a header, a column whose name is listed in 
     * {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES}, with entities separated by one of the separators 
     * listed in {@link #ENTITY_SEPARATORS}. 
     * 
     * @param originalOnt               The {@code OWLOntology} that was used as source for 
     *                                  the simplification process. 
     * @param infoFiles                 A {@code Set} of {@code String}s that are the paths 
     *                                  to the files generated by the method 
     *                                  {@code Uberon#saveSimplificationInfo}.
     * @param singleAnatEntityFiles     A {@code Set} of {@code String}s that are the paths 
     *                                  to the annotation files using single anatomical entity.
     * @param multipleAnatEntitieFiles  A {@code Set} of {@code String}s that are the paths 
     *                                  to the annotation files using multiple anatomical 
     *                                  entities.
     * @param filteredFileDirectory     A {@code String} that is the directory where to store 
     *                                  generated filtered info files.
     * @param filterUsingParents        A {@code boolean} defining whether terms that have 
     *                                  a parent in the info list by a is_a or part_of related 
     *                                  relation should be discarded. If {@code true}, 
     *                                  they will be discarded.
     * @throws IOException              If an error occurred while reading or writing a file.
     * @throws FileNotFoundException    If a provided file could not be found.
     */
    public static void filterUberonSimplificationInfo(OWLOntology originalOnt, 
            Set<String> infoFiles, 
            Set<String> singleAnatEntityFiles, Set<String> multipleAnatEntitieFiles, 
            String filteredFileDirectory, boolean filterUsingParents) 
                    throws FileNotFoundException, IOException {
        
        log.entry(originalOnt, infoFiles, singleAnatEntityFiles, multipleAnatEntitieFiles, 
                filteredFileDirectory, filterUsingParents);
        log.info("Start filtering Uberon simplification info...");
        
        //first, read all annotation files to get all anatomical entity IDs used
        Set<String> annotatedAnatEntityIds = new HashSet<String>();
        for (String singleAnatEntityFile: singleAnatEntityFiles) {
            annotatedAnatEntityIds.addAll(AnnotationCommon.extractAnatEntityIdsFromFile(
                    singleAnatEntityFile, true));
        }
        for (String multipleAnatEntitiesFile: multipleAnatEntitieFiles) {
            annotatedAnatEntityIds.addAll(AnnotationCommon.extractAnatEntityIdsFromFile(
                    multipleAnatEntitiesFile, false));
        }
        
        OWLGraphWrapper wrapper = new OWLGraphWrapper(originalOnt);
        OntologyUtils utils = new OntologyUtils(wrapper);
        
        //now, we get from the original ontology the association from XRefs to Uberon IDs, 
        //to be able to recognize a term even if the annotation was using a xref
        for (Entry<String, Set<String>> xRefEntry: utils.getXRefMappings().entrySet()) {
            if (annotatedAnatEntityIds.contains(xRefEntry.getKey())) {
                annotatedAnatEntityIds.addAll(xRefEntry.getValue());
            }
        }
        
        //now we add the IDs to use to replace an obsolete ID, in the case an obsolete ID 
        //was used in an annotation
        for (Entry<String, Set<String>> considerEntry: 
            utils.getConsiderMappings().entrySet()) {
            if (annotatedAnatEntityIds.contains(considerEntry.getKey())) {
                annotatedAnatEntityIds.addAll(considerEntry.getValue());
            }
        }
        for (Entry<String, Set<String>> replacedByEntry: 
            utils.getReplacedByMappings().entrySet()) {
            if (annotatedAnatEntityIds.contains(replacedByEntry.getKey())) {
                annotatedAnatEntityIds.addAll(replacedByEntry.getValue());
            }
        }
        
        //now, we add all the ancestors of the allowed terms, so that we display terms 
        //if one of their children is used in an annotation
        Set<String> allParentIds = new HashSet<String>();
        //NOTE 2014-06-04: Anne asked to not display these parents: these parent terms 
        //were used so that annotators could easily find the parent that is the cause 
        //of a term filtering. But Anne says it is easy to spot the responsible parent, 
        //and it is easier to have a shorter list of terms to review. 
        //So, the following code is disabled: 
//        for (String annotatedId: annotatedAnatEntityIds) {
//            OWLClass cls = wrapper.getOWLClassByIdentifier(annotatedId);
//            if (cls == null) {
//                //maybe was not an OBO-like ID but an IRI
//                cls = wrapper.getOWLClass(annotatedId);
//            }
//            //OK, maybe it was an xref or an obsolete ID, continue
//            if (cls == null) {
//                continue;
//            }
//            //get all ancestors, even indirect
//            for (OWLObject ancestor: wrapper.getAncestors(cls)) {
//                //consider only named ancestors
//                if (ancestor instanceof OWLClass) {
//                    allParentIds.add(wrapper.getIdentifier(ancestor));
//                }
//            }
//        }
        
        Set<String> allowedAnatEntityIds = new HashSet<String>(annotatedAnatEntityIds);
        allowedAnatEntityIds.addAll(allParentIds);
        
        //now, for each file infoFile, we generate a corresponding file with IDs filtered
        for (String infoFile: infoFiles) {
            log.trace("Filtering info file {}...", infoFile);
            
            //we need to first read all the file to get all IDs, if filtering using 
            //the parents is requested. 
            Set<String> allInfoAnatEntityIds = new HashSet<String>();
            //we open the file anyway once, at least to localize the proper column
            String entityColName = null;
            try (ICsvMapReader infoReader = 
                    new CsvMapReader(new FileReader(infoFile), Utils.TSVCOMMENTED)) {
                
                String[] header = infoReader.getHeader(true);
                int entityColIndex = localizeColumn(header, ANAT_ENTITY_COL_NAMES);
                if (entityColIndex == -1) {
                    throw log.throwing(new IllegalArgumentException("The info file " + 
                        infoFile + " does not contain any column to retrieve anatomical " +
                                "entities from. Offending header: " + header));
                }
                entityColName = header[entityColIndex];
                
                //now we read all the file if filtering on parents is requested
                if (filterUsingParents) {
                    Map<String, String> row;
                    while( (row = infoReader.read(header)) != null ) {
                        allInfoAnatEntityIds.add(row.get(entityColName));
                    }
                }
            }
            
            //now, read the info file to apply the filtering 
            try (ICsvMapReader infoReader = 
                    new CsvMapReader(new FileReader(infoFile), Utils.TSVCOMMENTED)) {
                
                String[] header = infoReader.getHeader(true);

                //create new filtered file 
                Path infoFilePath = Paths.get(infoFile);
                Path filteredFilePath = Paths.get(filteredFileDirectory, 
                        infoFilePath.getFileName().toString());
                if (Files.exists(filteredFilePath)) {
                    if (Files.isSameFile(infoFilePath, filteredFilePath)) {
                        throw log.throwing(new IllegalArgumentException("The directory " +
                    		"provided to store filtered files (" + 
                            Paths.get(filteredFileDirectory).toAbsolutePath().toString() + 
                            ") contains also the original files."));
                    }
                    Files.delete(filteredFilePath);
                }
                
                //write to new filtered file
                try (ICsvMapWriter filteredInfoWriter = new CsvMapWriter(
                        new FileWriter(filteredFilePath.toFile()), Utils.TSVCOMMENTED)) {
                    
                    filteredInfoWriter.writeHeader(header);
                    
                    //read each line of source info file to apply filtering
                    Map<String, String> row;
                    rowLoop: while( (row = infoReader.read(header)) != null ) {
                        String entityId = row.get(entityColName);
                        OWLClass cls = wrapper.getOWLClassByIdentifier(entityId);
                        if (cls == null) {
                            //maybe was not an OBO-like ID but an IRI
                            cls = wrapper.getOWLClass(entityId);
                        }
                        
                        //check if it is an allowed ID or IRI
                        if (!allowedAnatEntityIds.contains(entityId) && 
                                (cls == null || !allowedAnatEntityIds.contains(
                                        wrapper.getIdentifier(cls)))) {
                            if (log.isTraceEnabled()) {
                                log.trace("Discarding row because anatomical entity " +
                                        "not used in annotations: " + 
                                        infoReader.getLineNumber() + " - " + 
                                        infoReader.getUntokenizedRow());
                            }
                            continue rowLoop;
                        }
                        
                        //if this ID is allowed not because used in our annotations directly, 
                        //but because it is a parent of a term used in an annotation, 
                        //we discard it if it is member of a non-informative subset
                        if (!annotatedAnatEntityIds.contains(entityId) && 
                                (cls == null || !annotatedAnatEntityIds.contains(
                                        wrapper.getIdentifier(cls)))) {
                            if (cls != null && utils.isNonInformativeSubsetMember(cls)) {
                                if (log.isTraceEnabled()) {
                                    log.trace("Discarding row because parent " +
                                            "of a term used in annotations, but member of " +
                                            "a non-informative subset: " + 
                                            infoReader.getLineNumber() + " - " + 
                                            infoReader.getUntokenizedRow());
                                }
                                continue rowLoop;
                            }
                        }
                        
                        //filter terms that have a parent by is_a or part_of relation 
                        //in the list, if this parent is not member of 
                        //a non informative subset
                        if (filterUsingParents) {
                            //if this class does not exist in the ontology, well...
                            //we will keep it anyway
                            if (cls != null) {
                                edgeLoop: for (OWLGraphEdge edge: 
                                    utils.getIsAPartOfOutgoingEdges(cls)) {
                                    //if the parent is member of a non-informative subset, 
                                    //we will not discard the term anyway
                                    if (utils.isNonInformativeSubsetMember(edge.getTarget())) {
                                        continue edgeLoop;
                                    }
                                    //check if the parent is itself in the info file
                                    if (allInfoAnatEntityIds.contains(
                                            wrapper.getIdentifier(edge.getTarget())) || 
                                            allInfoAnatEntityIds.contains(edge.getTargetId())) {
                                        
                                        if (log.isTraceEnabled()) {
                                            log.trace("Discarding row because a parent " +
                                            		"of the term is also in the info file: " + 
                                                    infoReader.getLineNumber() + " - " + 
                                                    infoReader.getUntokenizedRow());
                                        }
                                        continue rowLoop;
                                    }
                                }
                            }
                        }
                        
                        //OK, valid term, write into filtered file
                        log.trace("Writing row: {}", row);
                        filteredInfoWriter.write(row, header);
                    }
                }
                
            }
            log.trace("Done filtering {}", infoFile);
        }
        
        log.info("Done filtering Uberon simplification info.");
        log.exit();
    }
    
    /**
     * Extract from the annotation TSV file {@code pathToTSVFile} the anatomical entity IDs used. 
     * This file must have a header, and can either have a column whose name is listed 
     * in {@link #ANAT_ENTITY_COL_NAMES}, containing single entity values, or a column 
     * whose name is listed in {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES}, containing multiple 
     * entity values, separated by one of the separator listed in {@link #ENTITY_SEPARATORS}.
     * 
     * @param pathToTSVFile     A {@code String} that is the path to the TSV file to be read.
     * @param singleAnatEntity  A {@code boolean} defining whether the provided file contains 
     *                          a column with single entity values, or a column with 
     *                          multiple entities values. 
     * @return                  A {@code Set} of {@code String}s that are the IDs 
     *                          of the anatomical entities used in the provided TSV file.
     * @throws FileNotFoundException    If {@code pathToTSVFile} could not be found.
     * @throws IOException              If {@code pathToTSVFile} could not be read.
     * @throws IllegalArgumentException If {@code pathToTSVFile} did not allow to retrieve 
     *                                  anatomical entity IDs.
     */
    public static Set<String> extractAnatEntityIdsFromFile(String pathToTSVFile, 
            boolean singleAnatEntity) throws FileNotFoundException, IOException {
        log.entry(pathToTSVFile, singleAnatEntity);
        
        Set<String> anatEntityIds = new HashSet<String>();
        try (ICsvListReader listReader = new CsvListReader(
                new FileReader(pathToTSVFile), Utils.TSVCOMMENTED)) {

            //localize the proper column. We choose the column by order of preference 
            //(some annotation files contain several columns 
            //from potential column names)
            String[] header = listReader.getHeader(true);
            List<String> colNamesToUse;
            if (singleAnatEntity) {
                colNamesToUse = ANAT_ENTITY_COL_NAMES;
            } else {
                colNamesToUse = MULTIPLE_ANAT_ENTITY_COL_NAMES;
            }
            //column indexes start at 1 in super csv, so we add 1
            int columnIndex = localizeColumn(header, colNamesToUse) + 1;
            if (columnIndex == 0) {
                throw log.throwing(new IllegalArgumentException("The file " + pathToTSVFile + 
                        " does not contain any column to retrieve " +
                        "anatomical entity IDs from."));
            }
            
            //read file
            while( (listReader.read()) != null ) {
                String columnValue = listReader.get(columnIndex);
                if (StringUtils.isBlank(columnValue)) {
                    throw log.throwing(new IllegalArgumentException("The file " + pathToTSVFile + 
                            " contains a row with no anatomical entities. Offending row: " +
                            listReader.getLineNumber() + " - " + 
                            listReader.getUntokenizedRow()));
                }
                if (singleAnatEntity) {
                    anatEntityIds.add(columnValue.trim());
                } else {
                    anatEntityIds.addAll(
                            AnnotationCommon.parseMultipleEntitiesColumn(columnValue));
                }
            }
        }
        
        if (anatEntityIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("The file " + pathToTSVFile + 
                    " did not allow to retrieve any anatomical entity IDs."));
        }
        
        return log.exit(anatEntityIds);
    }
    
    
    /**
     * Parses a value extracted from a column accepting multiple entities 
     * for annotations (see {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES} for an example). 
     * These multiple entities must be separated by one of the separators listed in 
     * {@link #ENTITY_SEPARATORS}.
     * <p>
     * The {@code String}s in the returned {@code List} are ordered using 
     * their natural ordering, to allow easier diff on generated file between releases.
     * 
     * @param columnContent     A {@code String} extracted from a column accepting multiple 
     *                          entities.
     * @return                  A {@code List} of {@code String}s that are the values 
     *                          corresponding to each individual entity,  
     *                          ordered by alphabetical order.
     */
    public static List<String> parseMultipleEntitiesColumn(String columnContent) {
        log.entry(columnContent);
        
        if (columnContent == null) {
            return log.exit(null);
        }
        String splitPattern = "";
        for (String separator: ENTITY_SEPARATORS) {
            if (!splitPattern.equals("")) {
                splitPattern += "|";
            }
            splitPattern += Pattern.quote(separator);
        }
        String[] values = columnContent.split(splitPattern);
        List<String> orderedValues = new ArrayList<String>();
        for (String uberonId: values) {
            orderedValues.add(uberonId.trim());
        }
        //perform the alphabetical ordering
        Collections.sort(orderedValues);
        
        return log.exit(orderedValues);
    }
    
    /**
     * Generates a {@code String} based on {@code terms}, that can be used as value 
     * in a column accepting multiple terms, in annotation files 
     * (see {@link #MULTIPLE_ANAT_ENTITY_COL_NAMES} for an example). 
     * The order of the {@code String}s in {@code terms} are preserved, the separator used 
     * is {@link #DEFAULT_ENTITY_SEPARATOR}. 
     * 
     * @param terms     A {@code List} of {@code String}s that are the terms used 
     *                  in a same column.
     * @return          A {@code String} that is the formatting of {@code terms} 
     *                  to be used in a multiple entities column 
     *                  of an annotation file.
     */
    public static String getTermsToColumnValue(List<String> terms) {
        log.entry(terms);
        String colValue = "";
        for (String term: terms) {
            if (!colValue.equals("")) {
                colValue += DEFAULT_ENTITY_SEPARATOR;
            }
            colValue += term.trim();
        }
        return log.exit(colValue);
    }
    
    /**
     * Determine, among {@code header}, the index of the element having an equal value 
     * in {@code allowedColumnNames}. If several elements in {@code header} have 
     * an equal value in {@code allowedColumnNames}, then the element whose matching value 
     * has the lowest index in {@code allowedColumnNames} is considered. Index returned 
     * starts from 0. If no element could be found in {@code header} with a matching value 
     * in {@code allowedColumnNames}, then -1 is returned. 
     * <p>
     * The aim is to localize, in the header of a TSV file, the column with the preferred 
     * name, when several column names are allowed to describe a same type of data, 
     * and a header includes several of these allowed column names. For instance, 
     * in some annotation files, the column name "anatEntityId" is sometimes used 
     * to refer to Uberon anatomical entities. But, in other files, "anatEntityId" 
     * represents IDs from anatomical ontologies used before we moved to Uberon, 
     * and they also include a column named "UberonId", referring to Uberon anatomical 
     * entities. In that case, the allowed names to refer to Uberon entities will be, 
     * in order of preference, "UberonId", then "anatEntityId". This will allow 
     * to correctly localize the column "UberonId" in files also including a column 
     * "anatEntityId", and to correctly localize the column "anatEntityId" in files 
     * where it is the only valid column. 
     * 
     * @param header                An {@code Array} of {@code String}s representing 
     *                              the tokenized header of a TSV file. This is how 
     *                              headers are returned by the {@code Super CSV} library 
     *                              that we use to read/write TSV files.
     * @param allowedColumnNames    A {@code List} of {@code String}s representing 
     *                              the potential column names allowed for the data 
     *                              we are looking for, in order of preference.  
     * @return                      an {@code int} that is the index of the preferred 
     *                              matching element in {@code header} (index starts from 0). 
     *                              If no elements in {@code header} match an element 
     *                              in {@code allowedColumnNames}, then -1 is returned. 
     */
    public static int localizeColumn(String[] header, List<String> allowedColumnNames) {
        log.entry(header, allowedColumnNames);
        
        int columnIndex = -1;
        //iterate potential column names in order of preference
        columnLoop: for (String columnName: allowedColumnNames) {
            for (int i = 0; i < header.length; i++) {
                if (columnName.equals(header[i])) {
                    columnIndex = i;
                    break columnLoop;
                } 
            }
        }
         
         return log.exit(columnIndex);
    }
    
}
