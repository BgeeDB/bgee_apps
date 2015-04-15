package org.bgee.pipeline.annotations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.mapping.RawSimilarityAnnotationDAO.RawSimilarityAnnotationTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.RawAnnotationBean;
import org.bgee.pipeline.annotations.SimilarityAnnotationUtils.SummaryAnnotationBean;

/**
 * Class responsible for inserting the similarity annotations into the database.
 * Note that the CIO and ECO ontologies and the NCBI taxonomy ontology must have been 
 * inserted prior to inserting these annotations, see 
 * {@link org.bgee.pipeline.ontologycommon.InsertECO InsertECO},  
 * {@link org.bgee.pipeline.ontologycommon.InsertCIO InsertCIO}, and 
 * {@link org.bgee.pipeline.species.InsertTaxa InsertTaxa}.
 * <p>
 * As of Bgee 13, only annotations to concept of historical homology are considered.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Apr. 2015
 * @since Bgee 13
 */
public class InsertSimilarityAnnotation extends MySQLDAOUser {
    
    private final static Logger log = LogManager.getLogger(
            InsertSimilarityAnnotation.class.getName());
    

    /**
     * Main method to trigger the insertion of the similarity annotations into the Bgee 
     * database. Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the TSV file storing the raw similarity annotations.
     * <li>path to the TSV file storing the summary similarity annotations.
     * </ol>
     * 
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException        If some files could not be found.
     * @throws IOException                  If some files could not be used.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     * @throws DAOException                 If an error occurred while inserting 
     *                                      the data into the Bgee database.
     */
    public static void main(String[] args) throws FileNotFoundException, 
        IllegalArgumentException, DAOException, IOException {
        log.entry((Object[]) args);
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                    "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                    " provided."));
        }
        
        InsertSimilarityAnnotation insert = new InsertSimilarityAnnotation();
        insert.insert(args[0], args[1]);
        
        log.exit();
    }
    
    /**
     * A {@code Set} of {@code SummarySimilarityAnnotationTO}s that will be generated 
     * by the {@link #generateTOs(String, String)} method, that will be used 
     * for insertion into the database.
     * @see #generateTOs(String, String)
     */
    private Set<SummarySimilarityAnnotationTO> summaryAnnotTOs;
    /**
     * A {@code Set} of {@code SimAnnotToAnatEntityTO}s that will be generated 
     * by the {@link #generateTOs(String, String)} method, that will be used 
     * for insertion into the database.
     * @see #generateTOs(String, String)
     */
    private Set<SimAnnotToAnatEntityTO> simAnnotToAnatEntityTOs;
    /**
     * A {@code Set} of {@code RawSimilarityAnnotationTO}s that will be generated 
     * by the {@link #generateTOs(String, String)} method, that will be used 
     * for insertion into the database.
     * @see #generateTOs(String, String)
     */
    private Set<RawSimilarityAnnotationTO> rawAnnotTOs;
    
    /**
     * An {@code int} that is used to generate IDs of {@code SummarySimilarityAnnotationTO}s 
     * created by this class. This {@code int} is set to 0 when initialized, so it must 
     * be incremented <strong>before</strong> setting an ID.
     */
    private int similarityAnnotIdGenerator;
    
    /**
     * Default constructor. 
     */
    public InsertSimilarityAnnotation() {
        this(null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     * 
     * @param manager   The {@code MySQLDAOManager} to use.
     */
    public InsertSimilarityAnnotation(MySQLDAOManager manager) {
        super(manager);
        this.initTOs();
    }
    
    /**
     * Reinit the {@code Set}s storing the {@code TransferObject}s used for insertion, 
     * and set to 0 the {@code int} used to generate IDs of {@code SummarySimilarityAnnotationTO}s 
     * created by this class.
     * 
     * @see #similarityAnnotIdGenerator
     * @see #summaryAnnotTOs
     * @see #simAnnotToAnatEntityTOs
     * @see #rawAnnotTOs
     */
    private void initTOs() {
        log.entry();
        this.similarityAnnotIdGenerator = 0;
        this.summaryAnnotTOs = new HashSet<SummarySimilarityAnnotationTO>();
        this.simAnnotToAnatEntityTOs = new HashSet<SimAnnotToAnatEntityTO>();
        this.rawAnnotTOs = new HashSet<RawSimilarityAnnotationTO>();
        log.exit();
    }
    
    /**
     * Insert the similarity annotations retrieved from the provided raw annotation file 
     * and summary annotation file into the database. 
     * <p>
     * As of Bgee 13, only annotations to concept of historical homology are considered.
     * 
     * @param rawAnnotFile      A {@code String} that is the path to a file storing 
     *                          the raw similarity annotations.
     * @param summaryAnnotFile  A {@code String} that is the path to a file storing 
     *                          the summary similarity annotations, corresponding to 
     *                          the raw annotations in {@code rawAnnotFile}.
     * @throws DAOException             If an error occurred while inserting the annotations 
     *                                  into the database.
     * @throws FileNotFoundException    If a file could not be found.
     * @throws IllegalArgumentException If the provided annotation files contains errors 
     *                                  or does not allow to retrieve proper annotations.
     * @throws IOException              If a file could not be read. 
     */
    public void insert(String rawAnnotFile, String summaryAnnotFile) 
            throws DAOException, FileNotFoundException, IllegalArgumentException, IOException {
        log.entry(rawAnnotFile, summaryAnnotFile);
        
        try {
            log.info("Start inserting similarity annotations into database...");
            this.generateTOs(rawAnnotFile, summaryAnnotFile);
            
            this.startTransaction();
            
            this.getSummarySimilarityAnnotationDAO().insertSummarySimilarityAnnotations(
                    this.summaryAnnotTOs);
            this.getSummarySimilarityAnnotationDAO().insertSimilarityAnnotationsToAnatEntityIds(
                    this.simAnnotToAnatEntityTOs);
            this.getRawSimilarityAnnotationDAO().insertRawSimilarityAnnotations(this.rawAnnotTOs);
            
            this.commit();
            log.info("Done inserting similarity annotations into database, inserted {} summary annotations, {} mappings to anat entities, {} raw annotations.", 
                    this.summaryAnnotTOs.size(), this.simAnnotToAnatEntityTOs.size(), 
                    this.rawAnnotTOs.size());
        } finally {
            this.closeDAO();
        }
        
        log.exit();
    }
    
    /**
     * Generate all necessary {@code transferObject}s used to insert the similarity annotations 
     * into the database. The annotations are retrieved from {@code rawAnnotFile} and 
     * {@code summaryAnnotFile}, and will be stored as TOs in {@link #summaryAnnotTOs}, 
     * {@link #simAnnotToAnatEntityTOs}, and {@link #rawAnnotTOs}. 
     * <p>
     * As of Bgee 13 we only use annotations to concept of historical homology.
     *  
     * @param rawAnnotFile      A {@code String} that is the path to a file storing 
     *                          the raw similarity annotations.
     * @param summaryAnnotFile  A {@code String} that is the path to a file storing 
     *                          the summary similarity annotations, corresponding to 
     *                          the raw annotations in {@code rawAnnotFile}.
     * @throws FileNotFoundException    If a file could not be found.
     * @throws IllegalArgumentException If the annotations retrieved were incorrect.
     * @throws IOException              If a file could not be read. 
     * 
     * @see SimilarityAnnotationUtils#extractRawAnnotations(String)
     * @see SimilarityAnnotationUtils#extractSummaryAnnotations(String)
     * @see SimilarityAnnotationUtils#groupRawPerSummaryAnnots(Collection, Collection)
     * @see #getSummaryTO(SummaryAnnotationBean)
     * @see #getSummaryAnnotToAnatEntitiesTOs(String, Collection)
     * @see #getRawTOs(String, Collection)
     */
    private void generateTOs(String rawAnnotFile, String summaryAnnotFile) 
            throws FileNotFoundException, IllegalArgumentException, IOException {
        log.entry(rawAnnotFile, summaryAnnotFile);
        
        this.initTOs();
        
        for (Entry<SummaryAnnotationBean, Set<RawAnnotationBean>> groupedAnnot: 
            SimilarityAnnotationUtils.groupRawPerSummaryAnnots(
                    SimilarityAnnotationUtils.extractSummaryAnnotations(summaryAnnotFile), 
                    SimilarityAnnotationUtils.extractRawAnnotations(rawAnnotFile)).entrySet()) {
            //for now, we only use annotations to concept of historical homology
            if (!SimilarityAnnotation.HISTORICAL_HOMOLOGY_ID.equals(
                    groupedAnnot.getKey().getHomId())) {
                throw log.throwing(new IllegalArgumentException("Annotations using "
                        + "other concept other than historical homology are not "
                        + "currently supported. Offending annotation: " 
                        + groupedAnnot.getKey()));
            }
            SummarySimilarityAnnotationTO summaryAnnotTO = this.getSummaryTO(
                    groupedAnnot.getKey());
            this.summaryAnnotTOs.add(summaryAnnotTO);
            
            this.simAnnotToAnatEntityTOs.addAll(this.getSummaryAnnotToAnatEntitiesTOs(
                    summaryAnnotTO.getId(), groupedAnnot.getKey().getEntityIds()));
            
            this.rawAnnotTOs.addAll(this.getRawTOs(summaryAnnotTO.getId(), 
                    groupedAnnot.getValue()));
        }
        
        log.entry();
    }
    
    /**
     * Generate a {@code SummarySimilarityAnnotationTO} based on {@code summaryAnnot}. 
     * The ID will be defined by incrementing {@link #similarityAnnotIdGenerator}.
     * 
     * @param summaryAnnot  A {@code SummaryAnnotationBean} to transform into a 
     *                      {@code SummarySimilarityAnnotationTO}, for insertion into 
     *                      the database.
     * @return              A {@code SummarySimilarityAnnotationTO} corresponding to 
     *                      {@code summaryAnnot}.
     * @throws IllegalArgumentException If {@code summaryAnnot} provides invalid information.
     */
    private SummarySimilarityAnnotationTO getSummaryTO(SummaryAnnotationBean summaryAnnot) 
        throws IllegalArgumentException {
        log.entry(summaryAnnot);
        
        if (StringUtils.isBlank(summaryAnnot.getHomId()) || 
                summaryAnnot.getEntityIds() == null || summaryAnnot.getEntityIds().isEmpty() || 
                summaryAnnot.getNcbiTaxonId() <= 0 || 
                StringUtils.isBlank(summaryAnnot.getCioId())) {
            throw log.throwing(new IllegalArgumentException("Invalid summary annotation: "
                    + summaryAnnot));
        }
        this.similarityAnnotIdGenerator++;
        return log.exit(new SummarySimilarityAnnotationTO(
                Integer.toString(this.similarityAnnotIdGenerator), 
                Integer.toString(summaryAnnot.getNcbiTaxonId()), summaryAnnot.isNegated(), 
                summaryAnnot.getCioId()));
    }
    
    /**
     * Generate a {@code Set} of {@code SimAnnotToAnatEntityTO}s for 
     * the similarity annotation ID {@code annotId}, one for each entity ID in
     * {@code entityIds}.
     * 
     * @param annotId       A {@code String} that is the ID of a summary similarity annotation.
     * @param entityIds     A {@code Collection} of {@code String}s that are the IDs 
     *                      of the anatomical entities associated in the similarity annotation 
     *                      with ID {@code annotId}.
     * @return              A {@code Set} of {@code SimAnnotToAnatEntityTO}s, where each 
     *                      {@code SimAnnotToAnatEntityTO} represents a mapping from 
     *                      {@code annotId} to an entity ID in {@code entityIds}.
     * @throws IllegalArgumentException If {@code annotId} is blank, or {@code entityIds} 
     *                                  is {@code null} or empty, or an element in 
     *                                  {@code entityIds} is blank.
     */
    private Set<SimAnnotToAnatEntityTO> getSummaryAnnotToAnatEntitiesTOs(String annotId, 
            Collection<String> entityIds) throws IllegalArgumentException {
        log.entry(annotId, entityIds);
        
        if (StringUtils.isBlank(annotId)) {
            throw log.throwing(new IllegalArgumentException("Incorrect annot ID provided."));
        }
        if (entityIds == null || entityIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Invalid entity IDs provided."));
        }
        
        Set<SimAnnotToAnatEntityTO> tos = new HashSet<SimAnnotToAnatEntityTO>();
        for (String entityId: entityIds) {
            if (StringUtils.isBlank(entityId)) {
                throw log.throwing(new IllegalArgumentException("Blank uberon Id "
                        + "in annotation with ID " + annotId));
            }
            tos.add(new SimAnnotToAnatEntityTO(annotId, entityId));
        }
        return log.exit(tos);
    }
    
    /**
     * Generate a {@code Set} of {@code RawSimilarityAnnotationTO}s for 
     * the similarity annotation ID {@code annotId}, one for each {@code RawAnnotationBean} 
     * in {@code rawAnnots}.
     * 
     * @param annotId       A {@code String} that is the ID of a summary similarity annotation.
     * @param rawAnnots     A {@code Collection} of {@code RawAnnotationBean}s that are 
     *                      the annotations used to produce the summary annotation with ID 
     *                      {@code annotId}.
     * @return              A {@code Set} of {@code RawSimilarityAnnotationTO}s, where each 
     *                      {@code RawSimilarityAnnotationTO} represents an annotation used 
     *                      to produce the summary annotation, with corresponding ID defined.
     * @throws IllegalArgumentException If {@code annotId} is blank, or {@code rawAnnots} 
     *                                  is {@code null} or empty, or an element in 
     *                                  {@code rawAnnots} is {@code null} or invalid.
     */
    private Collection<RawSimilarityAnnotationTO> getRawTOs(String annotId, 
            Collection<RawAnnotationBean> rawAnnots) throws IllegalArgumentException {
        log.entry(annotId, rawAnnots);
        
        if (StringUtils.isBlank(annotId)) {
            throw log.throwing(new IllegalArgumentException("Incorrect annot ID provided."));
        }
        if (rawAnnots == null || rawAnnots.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Invalid raw annotations provided."));
        }
        
        Set<RawSimilarityAnnotationTO> tos = new HashSet<RawSimilarityAnnotationTO>();
        for (RawAnnotationBean annot: rawAnnots) {
            if (annot == null || 
                    StringUtils.isBlank(annot.getCioId()) || 
                    StringUtils.isBlank(annot.getEcoId())) {
                throw log.throwing(new IllegalArgumentException("Invalid raw annotation: "
                        + annot));
            }
            tos.add(new RawSimilarityAnnotationTO(annotId, annot.isNegated(), 
                    annot.getEcoId(), annot.getCioId(), annot.getRefId(), annot.getRefTitle(), 
                    annot.getSupportingText(), annot.getAssignedBy(), annot.getCurator(), 
                    annot.getCurationDate()));
        }
        return log.exit(tos);
    }
}
