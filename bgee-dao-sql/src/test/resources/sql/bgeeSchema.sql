-- SQL file to create the Bgee database. Primary keys and other constraints 
-- such as unique indexes are defined in bgeeConstraint.sql. Indexes defined solely 
-- for performance issues are defined in bgeeIndexes. Foreign key constraints 
-- are defined in bgeeForeignKey.sql.
-- 
-- To load a dump into the database, you should typically do: 
-- mysql -u root -p -e "create database bgee_vXX"
-- mysql -u root -p bgee_vXX < bgeeSchema.sql
-- mysql -u root -p bgee_vXX < myDumpFile.sql
-- mysql -u root -p bgee_vXX < bgeeConstraint.sql
-- mysql -u root -p bgee_vXX < bgeeIndex.sql
-- mysql -u root -p bgee_vXX < bgeeForeignKey.sql
--
-- Altering a table after data insertion, to add indexes and foreign key constraints, 
-- can fail if the table is very large, with the error 1206: "ERROR 1206 (HY000): 
-- The total number of locks exceeds the lock table size". To solve this problem, 
-- you have to increase the buffer pool size, or you have to insert the data AFTER 
-- indexes and foreign key constraints generation.
-- The foreign key insertion should be done after the indexes creation to avoid 
-- generating redundant indexes (as foreign key constraints require indexes and 
-- create them if needed). 

ALTER DATABASE CHARACTER SET utf8 COLLATE utf8_general_ci;

--  ****************************************************
--  GENERAL
--  ****************************************************
create table author (
    authorId smallInt unsigned not null, 
    authorName varchar(255) not null
) engine = innodb;

create table dataSource (
    dataSourceId smallInt unsigned not null, 
    dataSourceName varchar(255) not null, 
    XRefUrl varchar(255) not null default '', 
-- path to experiment for expression data sources (ArrayExpress, GEO, NCBI, in situ databases, ...)
-- parameters such as experimentId are defined by the syntax [experimentId] for instance
    experimentUrl varchar(255) not null default '',
-- path to in situ evidence for in situ databases, 
-- to Affymetrix chips for affymetrix data
-- parameters such as experimentId are defined by the syntax [experimentId] for instance
    evidenceUrl varchar(255) not null default '',
-- url to the home page of the ressource
    baseUrl varchar(255) not null default '', 
    releaseDate timestamp null, 
-- e.g.: Ensembl 67, cvs version xxx
    releaseVersion varchar(255) not null default '', 
    dataSourceDescription TEXT, 
--  to define if this dataSource should be displayed on the page listing data sources
    toDisplay boolean not null default 0, 
-- a cat to organize the display 
    category enum('', 'Genomics database', 'Proteomics database', 
        'In situ data source', 'Affymetrix data source', 'EST data source', 'RNA-Seq data source', 
        'Ontology'), 
-- to organize the display. Default value is the highest value, so that this field is the last to be displayed
    displayOrder tinyint unsigned not null default 255
) engine = innodb;

create table keyword (
    keywordId int unsigned not null, 
    keyword varchar(255) not null
) engine = innodb;

--  ****************************************************
--  TAXONOMY
--  ****************************************************

-- The NCBI taxonomy, stored as a nested set model. This does not include species, 
-- that are stored in a different table. This is because, while a species is a taxon, 
-- we store some additional information for them. 
-- 
-- Only taxa that are ancestors of a species included in Bgee are stored. The column 
-- "bgeeSpeciesMostCA" specifies if they are moreover a most common ancestor of at least 
-- two species used in Bgee. For instance: if Bgee was using zebrafish, mouse and human, 
-- "Euarchontoglires" would be the most common ancestor of human and mouse, 
-- and "Euteleostomi" the most common ancestor of human, mouse and zebrafish. 
-- This allows to provide a simplified display to the users, where only these relevant 
-- branchings are used. 
-- We neverthless also store all the ancestors of the species used in Bgee 
-- (for instance, we would still store "Eutheria", "Theria", etc), as they are used 
-- for the gene hierarchical groups, in case users want to have a finer control 
-- on the paralogous/orthologous genes to retrieve and compare, and also for 
-- the transitive evolutionary relations, in case users want to specify in which 
-- common ancestor a structure should have existed. 
create table taxon (
    taxonId mediumint unsigned not null, 
    taxonScientificName varchar(255) not null, 
    taxonCommonName varchar(255), 
    taxonLeftBound int unsigned not null, 
    taxonRightBound int unsigned not null, 
    taxonLevel mediumint unsigned not null, 
-- bgeeSpeciesLCA defines whether this taxon is the Least Common Ancestor of at least 
-- two species used in Bgee. This allows to easily identify important branching.
    bgeeSpeciesLCA boolean not null
) engine = innodb;

create table species (
    speciesId mediumint unsigned not null,
-- example: homo
    genus varchar(70) not null, 
-- example: sapiens
    species varchar(70) not null, 
-- exemple: human
    speciesCommonName varchar(70) not null, 
-- ID of the taxon which this species belongs to, present in the table `taxonomy`.
-- For instance, if this species is `human`, it belongs to the taxon `homo` (taxon ID 9605). 
    taxonId mediumint unsigned not null, 
-- Path to retrieve the genome file we use for this species, from the GTF directory 
-- of the Ensembl FTP, without the Ensembl version suffix, nor the file type suffixes. 
-- For instance, for human, the GTF file in Ensembl 75 is stored at: 
-- ftp://ftp.ensembl.org/pub/release-75/gtf/homo_sapiens/Homo_sapiens.GRCh37.75.gtf.gz
-- This field would then contain: homo_sapiens/Homo_sapiens.GRCh37
-- This field is needed because we use for some species the genome of another species 
-- (for instance, chimp genome for bonobo species).
    genomeFilePath varchar(100) not null, 
-- ID of the species whose the genome was used for this species. This is used 
-- when a genome is not in Ensembl. For instance, for bonobo (ID 9597), we use the chimp 
-- genome (ID 9598), because bonobo is not in Ensembl. 
-- We don't use a foreign key constraint here, because maybe the species whose the genome 
-- was used does not have any data in Bgee, and thus is not in the taxon table.
-- If the correct genome of the species was used, the value of this field is 0.
    genomeSpeciesId mediumint unsigned not null default 0,
-- When another genome is used for this species, we change the gene ID prefix 
-- (for instance, the chimp gene IDs, starting with 'ENSPTRG', will be changed to 'PPAG'
-- when used for the bonobo)
    fakeGeneIdPrefix varchar(10) not null default ''
) engine = innodb;

--  ****************************************************
--  CONFIDENCE AND EVIDENCE ONTOLOGIES
--  ****************************************************

-- Branch 'confidence information statement' of the CIO 
-- (see https://github.com/BgeeDB/confidence-information-ontology). 
-- We only use CIO statements for annotations, so we do not insert terms from the branch 
-- 'confidence information element'. Moreover, we only insert terms associated to 
-- a 'confidence level' term.
-- Also, we do not use relations between CIO terms yet, so they are not inserted for now;
-- if they were to be inserted, we coud use a nested set model (as for the tables stage, 
-- taxon, etc), as there is a single is_a inheritance between CIO statements. 
-- TODO: all confidence levels used in Bgee should use these CIO statements, 
-- rather than the enum fields 'low quality'/'high quality'.
-- In order to not use terms from the branch 'confidence information element', 
-- this table has 3 columns capturing the 3 different types of CI element a statement 
-- can be associatd to. This is highly dependent on the current state of the ontology, 
-- this table should be changed if the ontology changed.
create table CIOStatement ( 
    CIOId varchar(20) not null, 
    CIOName varchar(255) not null, 
    CIODescription TEXT, 
-- define whether this CIO term is used to capture a trusted evidence line (= 1), or whether 
-- it indicates that the evidence should not be trusted (= 0).
    trusted tinyint unsigned not null default 0, 
-- represents the level of confidence that can be put in a CI statement. 
-- These enum fields correspond exactly to the labels of the relevant classes, 
-- leaves of the branch 'confidence level'.
-- can be null when the evidenceConcordance is 'strongly conflicting'
    confidenceLevel enum('low confidence level', 'medium confidence level', 'high confidence level'), 
-- capture whether there are multiple evidence lines available related to an assertion, 
-- and whether they are congruent or conflicting.
-- These enum fields correspond exactly to the labels of the relevant classes, 
-- leaves of the branch 'evidence concordance'.
    evidenceConcordance enum('single evidence', 'congruent', 'weakly conflicting', 'strongly conflicting') not null, 
-- capture, when there are several evidence lines available related to a same assertion, 
-- whether there are of a same or different experimental or computational types.
-- These enum fields correspond exactly to the labels of the relevant classes, 
-- leaves of the branch 'evidence type concordance'.
-- It is only applicable when a statement doesn't have an evienceConcordance = 'single evidence' 
-- (so this field is null for, and only for, confidence from single evidence)
    evidenceTypeConcordance enum('same type', 'different type')
) engine = innodb;

-- Evidence Ontology (see http://www.evidenceontology.org/).
-- Note that we do not insert pre-composed terms used to distinguish between 
-- evidence based on manual or automatic assertion (such terms have a relation 'used in' 
-- to either 'manual assertion' or 'automatic assertion', and are subclasses of either 
-- 'evidence used in manual assertion' or 'evidence used in automatic assertion'). 
-- So, for instance, we will insert the term 'genetic similarity evidence', not the terms 
-- 'genetic similarity evidence used in automatic assertion' and 
-- 'genetic similarity evidence used in manual assertion'. 
-- Also, we do not use relations between ECO terms yet, so they are not inserted for now;
-- if they were to be inserted, we coud use a nested set model (as for the tables stage, 
-- taxon, etc), as there is a single is_a inheritance between these terms. 
create table evidenceOntology ( 
    ECOId varchar(20) not null, 
    ECOName varchar(255) not null, 
    ECODescription TEXT
) engine = innodb;

--  ****************************************************
--  ANATOMY AND DEVELOPMENT
--  ****************************************************

create table stage ( 
    stageId varchar(20) not null, 
    stageName varchar(255) not null, 
    stageDescription TEXT, 
    stageLeftBound int unsigned not null, 
    stageRightBound int unsigned not null,  
    stageLevel int unsigned not null, 
    tooGranular tinyint unsigned not null default 0,
    groupingStage tinyint unsigned not null default 0
) engine = innodb;

create table stageTaxonConstraint (
    stageId varchar(20) not null, 
-- if speciesId is null, it means that the stage exists in all species.
-- The aim is to have an entry in this table for each stage, 
-- to avoid looking for stages not present, and to avoid creating an entry for each species 
-- when the stage exists in all species.
    speciesId mediumint unsigned 
) engine = innodb;

create table stageNameSynonym (
    stageId varchar(20) not null, 
    stageNameSynonym varchar(255) not null
) engine = innodb;

-- XRefs of developmental terms in the Uberon ontology
create table stageXRef (
    stageId varchar(20) not null, 
    stageXRefId varchar(20) not null
) engine = innodb;

create table anatEntity ( 
    anatEntityId varchar(20) not null, 
    anatEntityName varchar(255) not null, 
    anatEntityDescription TEXT, 
    startStageId varchar(20) not null, 
    endStageId varchar(20) not null, 
-- a boolean defining whether this anatomical entity is part of 
-- a non-informative subset in Uberon, as, for instance, 
-- 'upper_level "abstract upper-level terms not directly useful for analysis"'
    nonInformative boolean not null default 0
) engine = innodb;

create table anatEntityTaxonConstraint (
    anatEntityId varchar(20) not null, 
-- if speciesId is null, it means that the anatEntity exists in all species.
-- The aim is to have an entry in this table for each anatEntity, 
-- to avoid looking for anatEntities not present, and to avoid creating an entry for each species 
-- when the anatEntity exists in all species.
    speciesId mediumint unsigned 
) engine = innodb;

-- XRefs of anatomical terms in the Uberon ontology
create table anatEntityXRef (
    anatEntityId varchar(20) not null, 
    anatEntityXRefId varchar(20) not null
) engine = innodb;

create table anatEntityNameSynonym (
    anatEntityId varchar(20) not null, 
    anatEntityNameSynonym varchar(255) not null
) engine = innodb;

create table anatEntityRelation (
    anatEntityRelationId int unsigned not null, 
    anatEntitySourceId varchar(20) not null, 
    anatEntityTargetId varchar(20) not null, 
--  there is no distinction made in Bgee between is_a and part_of
    relationType enum('is_a part_of', 'develops_from', 'transformation_of'), 
--  relationStatus - direct: the relation is direct between anatEntityParentId and 
--  anatEntityDescentId; indirect: this is an indirect relation between two 
--  anatomical entities, that have been composed (e.g., part_of o is_a -> part_of); 
--  reflexive: a special line added for each anatomical entity, where anatEntityTargetId 
--  is equal to anatEntitySourceId. This is useful to get in one join all the descendants 
--  of an antomical entity, plus itself (otherwise it requires a 'or' in the join clause, 
--  which is non-optimal)
    relationStatus enum('direct', 'indirect', 'reflexive')
) engine = innodb;

create table anatEntityRelationTaxonConstraint (
    anatEntityRelationId int unsigned not null, 
-- if speciesId is null, it means that the anatEntityRelation exists in all species.
-- The aim is to have an entry in this table for each anatEntityRelation, 
-- to avoid looking for anatEntityRelations not present, and to avoid creating an entry for each species 
-- when the anatEntityRelation exists in all species.
    speciesId mediumint unsigned 
) engine = innodb;

--  ****************************************************
--  SIMILARITY ANNOTATIONS
--  (See https://github.com/BgeeDB/anatomical-similarity-annotations/)
--  ****************************************************

-- This table captures 'summary' similarity annotations: when several evidence lines 
-- are available related to an assertion (same HOM ID, taxon ID, anatEntity IDs), 
-- they are summarized into a single summary annotation, providing a global 
-- confidence level, emerging from all evidence lines available.
-- See table 'rawSimilarityAnnotation' to retrieve associated single evidence 
-- with single confidence level.
-- For convenience, all annotations are inserted in this table, even when only 
-- a single evidence is available related to an annotation. 
create table summarySimilarityAnnotation (
    summarySimilarityAnnotationId mediumint unsigned not null, 
-- for now, we only capture annotations of 'historical homology' (HOM:0000007), 
-- so we do not use a field 'HOMId'. We should, if in the future we captured other types 
-- of similarity annotations.
--  HOMId varchar(20) not null,
-- the taxon targeted by the similarity annotation 
-- (note that the similarity annotation file lets open the possibility of capturing 
-- several taxon IDs, for instance to define in which taxa a structure is 
-- functionally equivalent, as this type of relation would not originate from 
-- a common ancestor; but, this is not yet done, so we use this field, and not a link table).
    taxonId mediumint unsigned not null, 
-- define whether this annotation is negated (using the NOT qualifier of the similarity 
-- annotation file); this would mean that there existed only negative evidence lines 
-- related to this annotation (when evidence lines are conflicting, the summary annotation 
-- is considered positive, because we are primarly interested in positive annotations).
    negated boolean not null default 0, 
-- the ID of the confidence statement associated to this summary annotation; 
-- allows to capture the global confidence level, whether evidence lines were congruent 
-- or conflicting, etc.
-- If this summary annotation corresponds to an annotation supported by 
-- a single evidence line, then this CIO term will be the same as the one used in the table 
-- 'rawSimilarityAnnotation' for the related single annotation. Otherwise, 
-- it will be a CIO statement from the 'multiple evidence lines' branch.
    CIOId varchar(20) not null 
) engine = innodb;

-- similarity annotations can target several anatomical entities (e.g., to capture 
-- the homology between 'lung' and 'swim bladder'), and an antomical entity can be targeted 
-- by several annotations (e.g., to capture multiple homology hypotheses); 
-- this is why we need this link table.
create table similarityAnnotationToAnatEntityId (
    summarySimilarityAnnotationId mediumint unsigned not null, 
    anatEntityId varchar(20) not null
) engine = innodb;

-- Represent raw similarity annotations, capturing one single evidence line, 
-- which corresponds to the GO guidelines to capture sources of annotations (see 
-- http://www.geneontology.org/GO.evidence.shtml). When several evidence lines are available 
-- related to a same assertion, they are captured in an summary annotation, summarizing 
-- all evidence lines available, see table 'summarySimilarityAnnotation'; 
-- for convenience, annotations are all present in the table 'summarySimilarityAnnotation' 
-- anyway, even when they capture a single evidence.
-- So, this table provides information about single evidence related to an annotation: 
-- the individual evidence and confidence codes, the reference ID, the supporting text, 
-- the annotator who made the annotation, etc... Other "global" information are present 
-- in the table 'summarySimilarityAnnotation' (e.g., the HOM ID, the taxon ID). 
-- Targeted anatomical entities are stored in the table 'similarityAnnotationToAnatEntityId', 
-- and can be retrieved through the table 'summarySimilarityAnnotation'. 
create table rawSimilarityAnnotation (
-- the associated 'summary' annotation
    summarySimilarityAnnotationId mediumint unsigned not null, 
-- define whether this annotation is negated (using the NOT qualifier of the similarity 
-- annotation file: used to capture an information rejecting a putative relation 
-- between structures, that could otherwise seem plausible). 
    negated boolean not null default 0, 
-- capture how the annotation is supported 
    ECOId varchar(20) not null, 
-- the ID of the confidence statement associated to this annotation; 
-- it can only be a confidence statement from the branch 
-- 'confidence statement from single evidence' 
-- XXX: maybe we could have a trigger to check that it is a term from the correct branch; 
-- it would require to store branch information, or relations between terms, in the table 
-- 'CIOStatement'.
    CIOId varchar(20) not null, 
-- Unique identifier of a single source, cited as an authority for asserting the relation. 
-- Can be a DOI, a Pubmed ID, an ISBN, an URL.
-- XXX: should it be a TEXT field, to store long URL? It does not seem to be necessary for now. 
    referenceId varchar(255) not null, 
-- information provided for convenience, manually captured. 
    referenceTitle TEXT not null, 
-- A quote from the reference, supporting the annotation. If possible, it should 
-- also support the choice of the ECO and CIO IDs. 
    supportingText TEXT not null, 
-- The database which made the annotation. Used for tracking the source of 
-- an individual annotation. Currently, only Bgee is working on this file.
    assignedBy varchar(20) not null, 
-- A code allowing to identify the curator who made the annotation, from the database 
-- defined above. 
-- XXX: if we assume that these annotations are internal to Bgee, then this field 
-- should be named 'authorId', and have a foreign key constraint to the table 'author'; 
-- if we assume these annotations are a community effort, then this simple varchar field 
-- is fine. Let's stick to the community effort. 
    curator varchar(20) not null, 
-- Date when the annotation was made (AAAA-MM-JJ)
-- XXX: other tables store date in different format, which one is better really?
    annotationDate date 
) engine = innodb;

--  ****************************************************
--  GENE
--  ****************************************************
-- Hierarchical Orthologous Groups from OMA.

-- All the nodes of a particular group are stored in a nested set model. 
-- A node in the tree could be a speciation node or a duplication node.
-- The OMANodeLeftBound and OMANodeRightBound correspond to the left and right bound IDs of the nested set model.
-- Note: to use the nested set model, we often need to join this table to itself, 
-- using a range condition on left and right bounds for the join clause; sadly, 
-- there is a performance issue for such queries in MySQL, see 
-- http://www.percona.com/blog/2010/05/17/joining-on-range-wrong/
create table OMAHierarchicalGroup (
    -- A unique ID for each node inside an OMA Hierarchical Orthologous Group.
    -- Auto generated by us, unique over all groups (use as primary key)
    OMANodeId int unsigned not null, 
    -- The ID of Hierarchical Orthologous Group as provided by OMA.
    -- Only for Xref purpose.
    OMAGroupId varchar(255) not null,
    -- Bounds generated over all groups.
    OMANodeLeftBound int unsigned not null, 
    OMANodeRightBound int unsigned not null, 
    -- The ID corresponding to the level of taxonomy as in NCBI. 
    -- Some nodes have no taxonomy ID because they correspond to a duplication node (paralogy group). 
    taxonId mediumint unsigned
) engine = innodb;

create table geneOntologyTerm (
    goId char(10) not null, 
    goTerm varchar(255) not null,
    goDomain enum ('biological process', 'cellular component', 'molecular function')
) engine = innodb;

-- link a GO ID to its alternative IDs
create table geneOntologyTermAltId (
    goId char(10) not null, 
    goAltId char(10) not null
) engine = innodb;

-- list all is_a or part_of relations between GO terms, even indirect.
-- Relations other than is_a or part_of are not considered. 
create table geneOntologyRelation  (
    goAllTargetId char(10) not null,
    goAllSourceId char(10) not null 
) engine = innodb;

create table geneBioType (
    geneBioTypeId smallint unsigned not null, 
    geneBioTypeName varchar(255) not null default ''
) engine = innodb;

create table gene (
    geneId varchar(20) not null, 
    geneName varchar(255) not null default '', 
    geneDescription TEXT, 
    speciesId mediumint unsigned not null, 
    geneBioTypeId smallint unsigned,
-- can be null if the gene does not belong to a hierarchical group
-- a gene can belong to one and only one group
-- OMA parent node ID instead of OMA node ID to avoid create group for all genes  
    OMAParentNodeId int unsigned default null, 
-- defines whether the gene ID is present in Ensembl. For some species, they are not 
-- (for instance, bonobo; we generate our own custom IDs)
    ensemblGene boolean not null default 1
) engine = innodb;

create table geneNameSynonym (
    geneId varchar(20) not null, 
    geneNameSynonym varchar(255) not null
) engine = innodb;

create table geneXRef (
    geneId varchar(20) not null, 
    XRefId varchar(20) not null, 
    XRefName varchar(255) not null default '', 
    dataSourceId smallInt unsigned not null 
) engine = innodb;

create table geneToTerm (
    geneId varchar(20) not null, 
    term varchar(255) not null 
) engine = innodb;

-- TODO: use IDs from the Evidence Ontology, rather than the Evidence Codes used by 
-- the GO consortium. The field 'goEvidenceCode' would be replaced by a field 'ECOId', 
-- with a foreign key constraint to the table 'evidenceOntology'. This would require 
-- for the application inserting data in this table to retrieve the mapping between 
-- ECO IDs and Evidence Codes from the Evidence Ontology. 
create table geneToGeneOntologyTerm (
    geneId varchar(20) not null, 
    goId char(10) not null, 
    goEvidenceCode varchar(20) not null default ''
) engine = innodb;


--  ****************************************************
--  RAW EST DATA
--  ****************************************************
create table estLibrary (
    estLibraryId varchar(50) not null,
    estLibraryName varchar(255) not null,  
    estLibraryDescription text, 
    anatEntityId varchar(20), 
    stageId varchar(20), 
    dataSourceId smallInt unsigned not null 
) engine = innodb;

create table estLibraryToKeyword (
    estLibraryId varchar(50) not null, 
    keywordId int unsigned not null
) engine = innodb;

create table expressedSequenceTag (
    estId varchar(50) not null, 
--  ESTs have two IDs in Unigene
    estId2 varchar(50) not null default '', 
    estLibraryId varchar(50) not null, 
    geneId varchar(20) not null, 
    UniGeneClusterId varchar(70) not null default '', 
    expressionId int unsigned, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    estData enum('no data', 'poor quality', 'high quality') default 'no data'
) engine = innodb;
--  ****************************************************
--  RAW AFFYMETRIX DATA
--  ****************************************************
create table microarrayExperiment (
    microarrayExperimentId varchar(70) not null, 
    microarrayExperimentName varchar(255) not null default '', 
    microarrayExperimentDescription text, 
    dataSourceId smallInt unsigned not null 
) engine = innodb;

create table microarrayExperimentToKeyword (
    microarrayExperimentId varchar(70) not null, 
    keywordId int unsigned not null
) engine = innodb;

create table chipType (
    chipTypeId varchar(70) not null, 
    chipTypeName varchar(255) not null, 
    cdfName varchar(255) not null, 
    isCompatible tinyint(1) not null default 1, 
    qualityScoreThreshold decimal(10, 2) unsigned not null default 0, 
-- percentage of present probesets
-- 100.00
    percentPresentThreshold decimal(5, 2) unsigned not null default 0
) engine = innodb;

--  this table represents mapping of affymetrix probesets in general, 
--  not constrainted by the tables chipType and afymetrixProbeset 
--  (that means for instance that you can insert in this table a mapping 
--  for a probeset not present in the table affymetrixProbeset)
--  => so, NO foreign keys to the tables affymetrixProbeset and chipType.
--  moreover, the probeset mapping can be use for other tables 
--  (deaAffymetrixProbesetGroups, noExpressionAffymetrixProbeset)
-- create table affymetrixProbesetMapping(
--    chipTypeId varchar(70) not null, 
--    affymetrixProbesetId varchar(70) not null, 
--    geneId varchar(20) not null
-- ) engine = innodb;

create table affymetrixChip (
-- affymetrixChipId are not unique (couple affymetrixChipId - microarrayExperimentId is)
-- then we need an internal ID to link to affymetrixProbeset
    bgeeAffymetrixChipId int unsigned not null, 
    affymetrixChipId varchar(255) not null, 
    microarrayExperimentId varchar(70) not null, 
-- define only if CEL file available, normalization gcRMA, detection schuster
    chipTypeId varchar(70), 
    scanDate varchar(70) not null default '', 
-- An <code>enum</code> listing the different methods used ib Bgee 
--  to normalize Affymetrix data: 
-- * MAS5: normalization using the MAS5 software. Using 
--  this naormalization usually means that only the processed MAS5 files 
--  were available, otherwise another method would be used. 
-- * RMA: normalization by RMA method.
-- * gcRMA: normalization by gcRMA method. This is the default 
-- method in Bgee when raw data are available. 
    normalizationType enum('MAS5', 'RMA', 'gcRMA') not null, 
-- An <code>enum</code> listing the different methods to generate expression calls 
-- on Affymetrix chips: 
-- * MAS5: expression calls from the MAS5 software. Such calls 
--  are usually taken from a processed MAS5 file, and imply that the data 
--  were also normalizd using MAS5.
-- * Schuster: Wilcoxon test on the signal of probesets 
--  against a subset of weakly expressed probesets, to generate expression calls 
--  (see http://www.ncbi.nlm.nih.gov/pubmed/17594492). Such calls usually implies 
-- that raw data were available, and were normalized using gcRMA. 
    detectionType enum('MAS5', 'Schuster') not null, 
    anatEntityId varchar(20), 
    stageId varchar(20), 
-- arIQR_score Marta score
-- can be set to 0 if it is a MAS5 file
-- 99999999.99
    qualityScore decimal(10, 2) unsigned not null default 0, 
-- percentage of present probesets
-- 100.00
    percentPresent decimal(5, 2) unsigned not null
) engine = innodb;

create table affymetrixProbeset (
    affymetrixProbesetId varchar(70) not null, 
    bgeeAffymetrixChipId int unsigned not null,
    geneId varchar(20) not null,
    normalizedSignalIntensity decimal(13,5) unsigned not null default 0, 
--  Warning, flags must be ordered, the index in the enum is used in many queries
    detectionFlag enum('undefined', 'absent', 'marginal', 'present') not null default 'undefined', 
--  expressionId and noExpressionId can never be both not null simultaneously (but can be both null simultaneously)
    expressionId int unsigned, 
    noExpressionId int unsigned, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    affymetrixData enum('no data', 'poor quality', 'high quality') not null default 'no data', 
--  When both expressionId and noExpressionId are null, the probeset is not used for the summary of expression.
--  Reasons are: 
--  pre filtering: Probesets always seen as "absent" or "marginal" over the whole dataset are removed
--  bronze quality: for a gene/organ/stage, mix of probesets "absent" and "marginal" (no "present" and inconsistency expression / no expression)
--  absent low quality (MAS5): probesets always "absent" for this gene/organ/stage, but only seen by MAS5 (that we do not trust = "low quality" - "noExpression" should always be "high quality").
--  noExpression conflict: a "noExpression" result has been removed because of expression in some substructures/child stages.
-- undefined: only 'undefined' call have been seen
    reasonForExclusion enum('not excluded', 'pre-filtering', 
        'bronze quality', 'absent low quality', 
        'noExpression conflict', 'undefined') not null default 'not excluded'
) engine = innodb;

--  ****************************************************
--  IN SITU HYBRIDIZATION DATA
--  ****************************************************
create table inSituExperiment (
    inSituExperimentId varchar(70) not null, 
    inSituExperimentName varchar(255) not null default '', 
    inSituExperimentDescription text, 
    dataSourceId smallInt unsigned not null 
) engine = innodb;

create table inSituExperimentToKeyword (
    inSituExperimentId varchar(70) not null, 
    keywordId int unsigned not null
) engine = innodb;

--  evidence: picture, figure, paper, ...
create table inSituEvidence (
    inSituEvidenceId varchar(70) not null, 
    inSituExperimentId varchar(70) not null, 
-- some databases do  not allow to distinguish different samples used in an experiment, 
-- all results are merged into one "fake" sample. In that case, this boolean is false.
    evidenceDistinguishable boolean not null default 1, 
-- an information used to generate URLs to this sample, taht can be used in the evidenceUrl 
-- of the related DataSource. For instance, in MGI this represents 
-- the ID of the image to link to (but as an image is not always available, we cannot 
-- use it as the inSituEvidenceId)
    inSituEvidenceUrlPart varchar(255) not null default '' 
) engine = innodb;

--  Absent spots can be associated to an expressionId, if there is other data 
-- showing expression for the same gene/organ/stage
create table inSituSpot (
    inSituSpotId varchar(70) not null, 
    inSituEvidenceId varchar(70) not null, 
    --  for control purpose only (used in other databases)
    inSituExpressionPatternId varchar(70) not null, 
    anatEntityId varchar(20), 
    stageId varchar(20), 
    geneId varchar(20) not null, 
--  Warning, tags must be ordered, the index in the enum is used in many queries
    detectionFlag enum('undefined', 'absent', 'present') default 'undefined',	
--  expressionId and noExpressionId can never be both not null simultaneously
--  (but can be both null simultaneously: only case = spots absent low quality (not used for the noExpression table, and obviously, neither used for the table expression)
    expressionId int unsigned, 
    noExpressionId int unsigned, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    inSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
--  When both expressionId and noExpressionId are null, the probeset is not used for the summary of expression.
--  Reasons are: 
--  pre filtering: Probesets always seen as "absent" or "marginal" over the whole dataset are removed
--  bronze quality: for a gene/organ/stage, mix of probesets "absent" and "marginal" (no "present" and inconsistency expression / no expression)
--  absent low quality (MAS5): probesets always "absent" for this gene/organ/stage, but only seen by MAS5 (that we do not trust = "low quality" - "noExpression" should always be "high quality").
--  noExpression conflict: a "noExpression" result has been removed because of expression in some substructures/child stages.
-- undefined: only 'undefined' call have been seen
    reasonForExclusion enum('not excluded', 'pre-filtering', 
        'bronze quality', 'absent low quality', 
        'noExpression conflict', 'undefined') not null default 'not excluded'
) engine = innodb;

--  ****************************************************
--  RNA-Seq DATA
--  ****************************************************
create table rnaSeqExperiment (
--  primary exp ID, from GEO, patterns GSExxx
    rnaSeqExperimentId varchar(70) not null, 
    rnaSeqExperimentName varchar(255) not null default '', 
    rnaSeqExperimentDescription text, 
    dataSourceId smallInt unsigned not null 
) engine = innodb;

create table rnaSeqExperimentToKeyword (
    rnaSeqExperimentId varchar(70) not null, 
    keywordId int unsigned not null
) engine = innodb;

create table rnaSeqPlatform (
    rnaSeqPlatformId varchar(255) not null, 
    rnaSeqPlatformDescription text 
) engine = innodb;

--  corresponds to one sample
--  uses to produce several runs
create table rnaSeqLibrary (
--  primary ID, from GEO, pattern GSMxxx
    rnaSeqLibraryId varchar(70) not null, 
--  secondary ID, from SRA, pattern SRXxxx pattern
    rnaSeqSecondaryLibraryId varchar(70) not null,
    rnaSeqExperimentId varchar(70) not null, 
    rnaSeqPlatformId varchar(255) not null, 
    anatEntityId varchar(20), 
    stageId varchar(20), 
    log2RPKMThreshold decimal(8, 6) not null, 
    allGenesPercentPresent decimal(5, 2) unsigned not null default 0, 
    proteinCodingGenesPercentPresent decimal(5, 2) unsigned not null default 0, 
    intergenicRegionsPercentPresent decimal(5, 2) unsigned not null default 0, 
-- total number of reads in library, including those not mapped.
-- In case of paired-end libraries, it's the number of pairs of reads;
-- In case of single read, it's the total number of reads
    allReadsCount int unsigned not null default 0, 
-- total number of reads in library that were mapped to anything. 
-- if it is not a paired-end library, this number is equal to leftMappedReadsCount
    allMappedReadsCount int unsigned not null default 0, 
-- number of pairs of reads that were mapped from the left part, in case of a paired-end library.
-- In that case, this number is not independent from rightMappedReadsCount, a pair can be mapped 
-- from its left read AND its right read, or fron only one of them.
-- if it was not a paired-end library, this field is the total number of reads mapped.
    leftMappedReadsCount int unsigned not null default 0, 
-- number of reads that were mapped from the right part, in case of a paired-end library.
-- In that case, this number is not independent from leftMappedReadsCount, a pair can be mapped 
-- from its left read AND its right read, or from only one of them.
-- if it was not a paired-end library, this field is left to 0.
    rightMappedReadsCount int unsigned not null default 0, 
    minReadLength int unsigned not null default 0, 
    maxReadLength int unsigned not null default 0, 
--  Is the library built using paired end?
    libraryType enum('single', 'paired') not null, 
    libraryOrientation enum('forward', 'reverse', 'unstranded')
) engine = innodb;

--  Store the information of runs used, pool together to generate the results 
-- for a given library. 
create table rnaSeqRun (
--  same ID in GEO and SRA, pattern SRR...
    rnaSeqRunId varchar(70) not null, 
    rnaSeqLibraryId varchar(70) not null
) engine = innodb;

-- We sometimes discard some runs associated to a library, because of low mappability.
-- We keep track of these discarded runs in this table.
create table rnaSeqRunDiscarded (
--  same ID in GEO and SRA, pattern SRR...
    rnaSeqRunId varchar(70) not null, 
    rnaSeqLibraryId varchar(70) not null
) engine = innodb;

--  This table contains RPK values for each Ensembl gene for each library
--  and link them to an expressionId
create table rnaSeqResult (
    rnaSeqLibraryId varchar(70) not null, 
    geneId varchar(20) not null, 
-- why storing the log2 rather than the raw value?
    log2RPKM decimal(8, 6) not null, 
--  for information, measure not normalized for reads or genes lengths
    readsCount int unsigned not null, 
--  expressionId and noExpressionId can never be both not null simultaneously (but can be both null simultaneously)
    expressionId int unsigned, 
    noExpressionId int unsigned, 
    detectionFlag enum('undefined', 'absent', 'present') default 'undefined',	
--  Warning, qualities must be ordered, the index in the enum is used in many queries.
--  We should only see genes with 'high quality' here
    rnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data', 
--  When both expressionId and noExpressionId are null, the probeset is not used for the summary of expression.
--  Reasons are: 
--  pre filtering: Probesets always seen as "absent" or "marginal" over the whole dataset are removed
--  bronze quality: for a gene/organ/stage, mix of probesets "absent" and "marginal" (no "present" and inconsistency expression / no expression)
--  absent low quality (MAS5): probesets always "absent" for this gene/organ/stage, but only seen by MAS5 (that we do not trust = "low quality" - "noExpression" should always be "high quality").
--  noExpression conflict: a "noExpression" result has been removed because of expression in some substructures/child stages.
-- undefined: only 'undefined' call have been seen
    reasonForExclusion enum('not excluded', 'pre-filtering', 
        'bronze quality', 'absent low quality', 
        'noExpression conflict', 'undefined') not null default 'not excluded'
) engine = innodb;


--  ****************************************************
--  RAW DIFFERENTIAL EXPRESSION ANALYSES
--  Note: dea = Differential Expression Analyses ;)
--  ****************************************************

--  several differential expression analyses can be performed 
--  on the same experiment
create table differentialExpressionAnalysis (
    deaId int unsigned not null, 
    detectionType enum('Limma - MCM'),
--  defines whether different organs at a same (broad) developmental stage 
--  were compared ('anatomy'), or a same organ at different developmental stages 
--  ('development')
    comparisonFactor enum('anatomy', 'development'), 
--  microarrayExperimentId and rnaSeqExperimentId cannot be both null, ot both not null 
--  at the same time. We use these fields rather than an association table, 
--  because a DEA can belong to only one experiment, and because this would make 
--  one join less needed.
    microarrayExperimentId varchar(70) default null, 
    rnaSeqExperimentId varchar(70) default null
) engine = innodb;

--  a DEA can only be performed by comparing different conditions 
--  (a condition being an organ at a developmental stage), with each condition 
--  represented by several replicates. Such a group of replicates of a same condition 
--  in a same DEA is a 'deaSampleGroup'.
--  While it would be possible to determine the condition (anatEntityId + stageId) 
--  of a deaSampleGroup by looking at the individual samples (for instance, 
--  looking at the condition of an affymetrixChip member of a deaSampleGroup), 
--  this information is also present in this table (see anatEntityId and stageId fields).
--  This is because, for the sake of performing the analyses, too granular 
--  developmental stages can be mapped to a broader parent stage (for instance, 
--  mapping '24 yo human' to 'young adult'), otherwise the analyses could be 
--  meaningless (e.g., performing a DEA on '24 yo human' vs. '25 yo human').
--  So the anatEntityId and stageId in this table can actually be different than 
--  the annotated anatDevId and stageId of the samples (meaning, different than 
--  in the table affymetrixChip or rnaSeqLibrary).
--  As of Bgee 13, a deaSampleGroup can either be a group of affymetrixChips, 
--  or a group of rnaSeqLibraries. Their related samples will then be find 
--  respectively in deaSampleGroupToAffymetrixChip, or deaSampleGroupToRnaSeqLibrary.
--  this can be determined by checking in the table differentialExpressionAnalysis 
--  the fields microarrayExperimentId and rnaSeqExperimentId, to determine whether 
--  the DEA was using Affymetrix, or RNA-Seq.
create table deaSampleGroup (
    deaSampleGroupId int unsigned not null, 
    deaId int unsigned not null,
    anatEntityId varchar(20), 
    stageId varchar(20)
) engine = innodb;

--  An association table to link an affymetrixChip to the deaSampleGroup it belongs to.
--  A same chip can be part of several groups, for instance if it was use for DEAs 
--  with different comparisonFactors. But all the affymetrixChips inside a deaSampleGroup 
--  are unique
create table deaSampleGroupToAffymetrixChip (
    deaSampleGroupId int unsigned not null, 
    bgeeAffymetrixChipId int unsigned not null
) engine = innodb;

--  An association table to link a rnaSeqLibrary to the deaSampleGroup it belongs to.
--  A same library can be part of several groups, for instance if it was use for DEAs 
--  with different comparisonFactors. But all the rnaSeqLibraries inside a deaSampleGroup 
--  are unique
create table deaSampleGroupToRnaSeqLibrary (
    deaSampleGroupId int unsigned not null, 
    rnaSeqLibraryId varchar(70) not null
) engine = innodb;

--  differentialExpressionAnalysisProbesetsSummary
--  a line in this table is a summary of a set of probesets, used for the 
--  differential expression analysis, belonging to different 
--  affymetrix chips, corresponding to one group of chips
create table deaAffymetrixProbesetSummary (
--  deaAffymetrixProbesetSummaryId corresponds to the IDs of the probesets used for this summary 
--  (all of them have the same of course). These probesets belong to the affymetrix chips, retrieved using the field `deaChipsGroupId` 
--  and the table `deaChipsGroupToAffymetrixChip`
    deaAffymetrixProbesetSummaryId varchar(70) not null,
    deaSampleGroupId  int unsigned not null, 
    geneId varchar(20) not null, 
    foldChange decimal(7,2) not null default 0, 
    differentialExpressionId int unsigned,
--  Warning, qualities must be ordered, the index in the enum is used in many queries
-- 'not expressed' = gene never seen as 'expressed' in the conditions studied ('marginal' is not considered)
-- 'no diff expression' = gene has expression, but no significant fold change observed
    differentialExpressionAffymetrixData enum('no data', 'not expressed', 'no diff expression', 'poor quality', 'high quality') default 'no data', 
-- p-value adjusted by Benjamini-Hochberg procedure
-- "number of digits to the right of the decimal point (the scale). It has a range of 0 to 30"
    deaRawPValue decimal(31, 30) unsigned not null default 1, 
-- excluded if not expressed in ALL samples in a given analysis 
-- (it is not excluded if expressed in at least one condition)
    reasonForExclusion enum('not excluded', 'not expressed') not null default 'not excluded'
) engine = innodb;

--  deaRNASeqSummary
--  a line in this table is a summary of a set of RNA-Seq results, used for the 
--  differential expression analysis, belonging to different runs, corresponding to one group of runs
create table deaRNASeqSummary (
    geneSummaryId varchar(20) not null, 
    deaSampleGroupId  int unsigned not null, 
    foldChange decimal(7,2) not null default 0, 
    differentialExpressionId int unsigned,
--  Warning, qualities must be ordered, the index in the enum is used in many queries
-- 'not expressed' = gene never seen as 'expressed' in the conditions studied ('marginal' is not considered)
-- 'no diff expression' = gene has expression, but no significant fold change observed
    differentialExpressionRNASeqData enum('no data', 'not expressed', 'no diff expression', 'poor quality', 'high quality') default 'no data', 
-- p-value adjusted by Benjamini-Hochberg procedure
-- "number of digits to the right of the decimal point (the scale). It has a range of 0 to 30"
    deaRawPValue decimal(31, 30) unsigned not null default 1, 
-- excluded if not expressed in ALL samples in a given analysis 
-- (it is not excluded if expressed in at least one condition)
    reasonForExclusion enum('not excluded', 'not expressed') not null default 'not excluded'
) engine = innodb;

--  ****************************************************
--  SUMMARY EXPRESSION CALLS
--  ****************************************************

--  This table is a summary of expression calls for a given triplet 
--  gene - anatomical entity - developmental stage, over all the experiments 
--  for all data types.
create table expression (
    expressionId int unsigned not null, 
    geneId varchar(20) not null,
    anatEntityId varchar(20) not null, 
    stageId varchar(20) not null, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    estData enum('no data', 'poor quality', 'high quality') default 'no data', 
    affymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
    inSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
    rnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data'
) engine = innodb;

--  precomputed expression table where the expression of an organ and
--  all its descendants are mapped to the parent organ id
create table globalExpression (
    globalExpressionId int unsigned not null, 
    geneId varchar(20) not null,
    anatEntityId varchar(20) not null, 
    stageId varchar(20) not null, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    estData enum('no data', 'poor quality', 'high quality') default 'no data', 
    affymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
    inSituData enum('no data', 'poor quality', 'high quality') default 'no data',
    rnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data',
    originOfLine enum('self', 'descent', 'both') default 'self'
) engine = innodb;

create table globalExpressionToExpression (
    globalExpressionId int unsigned not null, 
    expressionId int unsigned not null
) engine = innodb;

--  expression table containg all the expression for a hog (including all descent)
create table hogExpression (
    hogExpressionId int unsigned not null, 
    geneId varchar(20) not null,
    hogId varchar(70) not null, 
    metastageId varchar(20) not null, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    estData enum('no data', 'poor quality', 'high quality') default 'no data', 
    affymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
    inSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
    rnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data'
) engine = innodb;

create table hogExpressionToExpression (
    hogExpressionId int unsigned not null, 
    expressionId int unsigned not null
) engine = innodb;

--  precompute whether, in a HOG at a metastage for a species, 
--  expression data exist or not
create table hogExpressionSummary (
    hogId varchar(70) not null, 
    metastageId varchar(20) not null, 
    speciesId mediumint unsigned not null, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    estData enum('no data', 'data') default 'no data', 
    affymetrixData enum('no data', 'data') default 'no data', 
    inSituData enum('no data', 'data') default 'no data', 
    rnaSeqData enum('no data', 'data') default 'no data'
) engine = innodb;


--  ****************************************************
--  SUMMARY NO-EXPRESSION CALLS
--  ****************************************************
--  This table is a summary of no-expression calls for a given triplet 
--  gene - anatomical entity - developmental stage, over all the experiments 
--  for all data types.
create table noExpression (
    noExpressionId int unsigned not null, 
    geneId varchar(20) not null,
    anatEntityId varchar(20) not null, 
    stageId varchar(20) not null, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    noExpressionAffymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
    noExpressionInSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
    noExpressionRelaxedInSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
    noExpressionRnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data'
) engine = innodb;

--  precomputed no-expression table, where a no-expression in an anatomical entity 
--  takes into account report of absence of expression in parent anatomical entities.
create table globalNoExpression (
    globalNoExpressionId int unsigned not null, 
    geneId varchar(20) not null,
    anatEntityId varchar(20) not null, 
    stageId varchar(20) not null, 
--  Warning, qualities must be ordered, the index in the enum is used in many queries
    noExpressionAffymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
    noExpressionInSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
    noExpressionRelaxedInSituData enum('no data', 'poor quality', 'high quality') default 'no data', 
    noExpressionRnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data',
    noExpressionOriginOfLine enum('self', 'parent', 'both') default 'self'
) engine = innodb;

create table globalNoExpressionToNoExpression (
    globalNoExpressionId int unsigned not null, 
    noExpressionId int unsigned not null
) engine = innodb;

--  ****************************************************
--  SUMMARY DIFF EXPRESSION CALLS
--  ****************************************************

create table differentialExpression (
    differentialExpressionId int unsigned not null, 
    geneId varchar(20) not null,
    anatEntityId varchar(20) not null, 
    stageId varchar(20) not null, 
--  defines whether different organs at a same (broad) developmental stage 
--  were compared ('anatomy'), or a same organ at different developmental stages 
--  ('development')
    comparisonFactor enum('anatomy', 'development'), 
-- *** Affymetrix ***
-- the diff expression call generated by Affymetrix
-- 'not expressed' = gene never seen as 'expressed' in the conditions studied ('marginal' is not considered)
-- 'no diff expression' = gene has expression, but no significant fold change observed
    diffExprCallAffymetrix enum('no data', 'not expressed', 'no diff expression', 'under-expression', 'over-expression') not null default 'no data', 
-- confidence in the call generated by Affymetrix data
-- 'no data' is redundant but it is kept to keep the same indexes for all data states (for instance, rnaSeqData in expression table) 
    diffExprAffymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
-- among all the analyses using Affymetrix comparing this condition, best p-value associated to this call
-- "number of digits to the right of the decimal point (the scale). It has a range of 0 to 30"
    bestPValueAffymetrix decimal(31, 30) unsigned not null default 1, 
-- number of analyses using Affymetrix data where the same call is found
    consistentDEACountAffymetrix smallint unsigned not null default 0, 
-- number of analyses using Affymetrix data where a different call is found
    inconsistentDEACountAffymetrix smallint unsigned not null default 0, 
-- *** RNA-Seq ***
-- the diff expression call generated by RNA-Seq
-- 'not expressed' = gene never seen as 'expressed' in the conditions studied ('marginal' is not considered)
-- 'no diff expression' = gene has expression, but no significant fold change observed
    diffExprCallRNASeq enum('no data','not expressed', 'no diff expression', 'under-expression', 'over-expression') not null default 'no data', 
-- confidence in the call generated by RNA-Seq data
-- 'no data' is redundant but it is kept to keep the same indexes for all data states (for instance, rnaSeqData in expression table) 
    diffExprRNASeqData enum('no data', 'poor quality', 'high quality') default 'no data', 
-- among all the analyses using RNA-Seq comparing this condition, best p-value associated to this call
-- "number of digits to the right of the decimal point (the scale). It has a range of 0 to 30"
    bestPValueRNASeq decimal(31, 30) unsigned not null default 1, 
-- number of analyses using RNA-Seq data where the same call is found
    consistentDEACountRNASeq smallint unsigned not null default 0, 
-- number of analyses using RNA-Seq data where a different call is found
    inconsistentDEACountRNASeq smallint unsigned not null default 0
) engine = innodb;

-- this version of the diff expression table is not considered as of Bgee 13
/*create table differentialExpression (
    differentialExpressionId int unsigned not null, 
    geneId varchar(20) not null,
    anatEntityId varchar(20) not null, 
    stageId varchar(20) not null, 
--  defines whether different organs at a same (broad) developmental stage 
--  were compared ('anatomy'), or a same organ at different developmental stages 
--  ('development')
    comparisonFactor enum('anatomy', 'development'), 
--  Warning, differentialExpressionCall must be ordered this way, the index in the enum 
--  is used in many queries
    differentialExpressionCall enum('no diff expression', 'under-expression', 'over-expression'),
-- the maximum number of conditions compared for which this differential expression call 
-- is valid. For instance, if a differential expression analysis comparing 3 conditions 
-- generated a call for a given gene-organ-stage, and another analysis comparing 
-- 6 conditions generated another call for the same gene-organ-stage (with different 
-- direction and/or qualities), then maxNumberOfConditions will be 3 for the call 
-- generated by the first analysis, and 6 for the other call. 
-- 
-- But if the two analyses were generating the same call, then they would be only 
-- one call in this table for the given gene-organ-stade, with a maxNumberOfConditions 
-- equals to 6
-- 
-- default is 3, because as of Bgee 13, this is the minimum number of conditions 
-- to perform a diff expression analysis
-- 
-- Examples of queries: 
-- 
-- query to retrieve diff expression calls for a given Gene with no minimum number 
-- of conditions compared requested: 
-- select * from differentialExpression as t1 inner join 
-- (
--     select geneId, organId, stageId, comparisonFactor, min(maxNumberOfConditions) as min 
--     from differentialExpression where geneId = ? group by geneId, organId, stageId, 
--     comparisonFactor)
-- ) as t2 on t1.geneId = t2.geneId and t1.organId = t2.organId and t1.stageId = t2.stageId 
--         and t1.comparisonFactor = t2.comparisonFactor and t1.maxNumberOfConditions = t2.min 
-- where t1.geneId = ?;
-- 
-- Alternatively: TO TEST, IT SEEMS WRONG
-- 
-- select * from differentialExpression as t1 
-- where t1.geneId = ? and t1.maxNumberOfConditions = 
-- (select min(maxNumberOfConditions) from differentialExpression as t2 where 
--  t2.geneId = t1.geneId and t2.organId = t1.organId and t2.stageId = t1.geneId and 
--  t2.comparisonFactor = t1.comparisonFactor);
-- 
-- Example of query to select the calls with the maximum number of conditions compared 
-- for a given gene-organ-stage, with no minimum defined (select only the "best" calls): 
-- 
-- select * from differentialExpression as t1 inner join 
-- (
--     select geneId, organId, stageId, comparisonFactor, max(maxNumberOfConditions) as max 
--     from differentialExpression where geneId = ? group by geneId, organId, stageId, 
--     comparisonFactor)
-- ) as t2 on t1.geneId = t2.geneId and t1.organId = t2.organId and t1.stageId = t2.stageId 
--         and t1.comparisonFactor = t2.comparisonFactor and t1.maxNumberOfConditions = t2.max 
-- where t1.geneId = ?;
   maxNumberOfConditions smallint unsigned not null default 3, 
--  Warning, qualities must be ordered this way, the index in the enum is used in many queries
    differentialExpressionAffymetrixData enum('no data', 'poor quality', 'high quality') default 'no data', 
    differentialExpressionRnaSeqData enum('no data', 'poor quality', 'high quality') default 'no data'
) engine = innodb;*/


--  select((select count(1) from rnaSeqExperiment) + (select count(1) from rnaSeqLibrary) + (select count(1) from rnaSeqResults) + (select count(1) from rnaSeqExperimentToKeyword) + (select count(1) from affymetrixChip) + (select count(1) from affymetrixProbeset) + (select count(1) from author) + (select count(1) from chipType) + (select count(1) from dataSource) + (select count(1) from dataType) + (select count(1) from deaAffymetrixProbesetSummary) + (select count(1) from deaChipsGroup) + (select count(1) from deaChipsGroupToAffymetrixChip) + (select count(1) from detectionType) + (select count(1) from differentialExpression) + (select count(1) from differentialExpressionAnalysis) + (select count(1) from differentialExpressionAnalysisType) + (select count(1) from estLibrary) + (select count(1) from estLibraryToKeyword) + (select count(1) from expressedSequenceTag) + (select count(1) from expression) + (select count(1) from gene) + (select count(1) from geneBioType) + (select count(1) from geneFamily) + (select count(1) from geneFamilyPredictionMethod) + (select count(1) from geneNameSynonym) + (select count(1) from geneOntologyDescendants) + (select count(1) from geneOntologyTerm) + (select count(1) from geneToTerm) + (select count(1) from geneXRef) + (select count(1) from globalExpression) + (select count(1) from globalExpressionToExpression) + (select count(1) from hogDescendants) + (select count(1) from hogExpression) + (select count(1) from hogExpressionSummary) + (select count(1) from hogExpressionToExpression) + (select count(1) from hogNameSynonym) + (select count(1) from hogRelationship) + (select count(1) from hogXRef) + (select count(1) from homologousOrgansGroup) + (select count(1) from inSituEvidence) + (select count(1) from inSituExperiment) + (select count(1) from inSituExperimentToKeyword) + (select count(1) from inSituSpot) + (select count(1) from keyword) + (select count(1) from metaStage) + (select count(1) from metaStageNameSynonym) + (select count(1) from microarrayExperiment) + (select count(1) from microarrayExperimentToKeyword) + (select count(1) from noExpression) + (select count(1) from normalizationType) + (select count(1) from organ) + (select count(1) from organDescendants) + (select count(1) from organNameSynonym) + (select count(1) from organRelationship) + (select count(1) from species) + (select count(1) from stage) + (select count(1) from stageNameSynonym) + (select count(1) from stageXRef));