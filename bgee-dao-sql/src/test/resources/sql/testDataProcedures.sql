-- Procedure to insert data in SELECT/UPDATE databases used for integration tests.
DROP PROCEDURE IF EXISTS populateTestDBs
;

CREATE PROCEDURE populateTestDBs()
BEGIN
    
    DECLARE CheckExists int;
    SET CheckExists = 0;

    SELECT count(*) INTO CheckExists FROM geneBioType;

    IF (CheckExists = 0) THEN 

        INSERT INTO geneBioType (geneBioTypeId,geneBioTypeName) 
        VALUES (12,'geneBioTypeName12');
        
        INSERT INTO dataSource (dataSourceId,dataSourceName,XRefUrl,experimentUrl,evidenceUrl,baseUrl,releaseDate,releaseVersion,dataSourceDescription,toDisplay,category,displayOrder)
        VALUES (1,'First DataSource','XRefUrl','experimentUrl','evidenceUrl','baseUrl',NOW(),'1.0','My custom data source',1,'Genomics database',1);

        INSERT INTO taxon (taxonId,taxonScientificName,taxonCommonName,taxonLeftBound,taxonRightBound,taxonLevel,bgeeSpeciesLCA) 
        VALUES (111,'taxSName111','taxCName111',1,10,1,1),
               (211,'taxSName211','taxCName211',2,3,2,0),
               (311,'taxSName311','taxCName311',4,9,2,0),
               (411,'taxSName411','taxCName411',5,6,1,1),
               (511,'taxSName511','taxCName511',7,8,1,1);

        INSERT INTO OMAHierarchicalGroup (OMANodeId,OMAGroupId,OMANodeLeftBound,OMANodeRightBound,taxonId) 
        VALUES (1,'HOG:NAILDQY',1,8,111),
               (2,'HOG:NAILDQY',2,3,211),
               (3,'HOG:NAILDQY',4,7,311),
               (4,'HOG:NAILDQY',5,6,411),
               (5,'HOG:VALEWID',9,14,111),
               (6,'HOG:VALEWID',10,13,211),
               (7,'HOG:VALEWID',11,12,511);

        INSERT INTO species (speciesId,genus,species,speciesCommonName,taxonId,genomeFilePath,genomeSpeciesId,fakeGeneIdPrefix) 
        VALUES (11,'gen11','sp11','spCName11',111,'path/genome11',0,''),
               (21,'gen21','sp21','spCName21',211,'path/genome21',52,'FAKEPREFIX'),
               (31,'gen31','sp31','spCName31',311,'path/genome31',0,'');

        INSERT INTO gene (geneId,geneName,geneDescription,speciesId,geneBioTypeId,OMAParentNodeId,ensemblGene) 
        VALUES ('ID1','genN1','genDesc1',11,12,2,true),
               ('ID2','genN2','genDesc2',21,null,null,true),
               ('ID3','genN3','genDesc3',31,null,3,false);
    
        INSERT INTO stage (stageId,stageName,stageDescription,stageLeftBound,stageRightBound,stageLevel) 
        VALUES ('Stage_id1','stageN1','stage Desc 1',1,36,1),
               ('Stage_id2','stageN2','stage Desc 2',2,7,2),
               ('Stage_id3','stageN3','stage Desc 3',3,4,2),
               ('Stage_id4','stageN4','stage Desc 4',5,6,1),
               ('Stage_id5','stageN5','stage Desc 5',8,17,2),
               ('Stage_id6','stageN6','stage Desc 6',9,10,2),
               ('Stage_id7','stageN7','stage Desc 7',11,16,3),
               ('Stage_id8','stageN8','stage Desc 8',12,13,3),
               ('Stage_id9','stageN9','stage Desc 9',14,15,1),
               ('Stage_id10','stageN10','stage Desc 10',18,25,2),
               ('Stage_id11','stageN11','stage Desc 11',19,20,2),
               ('Stage_id12','stageN12','stage Desc 12',21,22,2),
               ('Stage_id13','stageN13','stage Desc 13',23,24,1),
               ('Stage_id14','stageN14','stage Desc 14',26,35,2),
               ('Stage_id15','stageN15','stage Desc 15',27,32,3),
               ('Stage_id16','stageN16','stage Desc 16',28,29,3),
               ('Stage_id17','stageN17','stage Desc 17',30,31,2),
               ('Stage_id18','stageN18','stage Desc 18',33,34,2);
               
        INSERT INTO anatEntity(anatEntityId,anatEntityName,anatEntityDescription,startStageId,endStageId,frequentlyUsedAnatEntity)
        VALUES ('Anat_id1','anatStruct','anatStruct desc','Stage_id1','Stage_id2',1),
               ('Anat_id2','organ','organ desc','Stage_id10','Stage_id18',1),
               ('Anat_id3','heart','heart desc','Stage_id16','Stage_id18',0),
               ('Anat_id4','gill','gill desc','Stage_id12','Stage_id18',1),
               ('Anat_id5','brain','brain desc','Stage_id11','Stage_id17',1),
               ('Anat_id6','embryoStruct','embryoStruct desc','Stage_id2','Stage_id5',0),
               ('Anat_id7','ectoderm','ectoderm desc','Stage_id6','Stage_id13',1),
               ('Anat_id8','neuralTube','neuralTube desc','Stage_id8','Stage_id17',0),
               ('Anat_id9','forebrain','forebrain desc','Stage_id8','Stage_id17',1),
               ('Anat_id10','hindbrain','hindbrain desc','Stage_id8','Stage_id17',1),
               ('Anat_id11','cerebellum','cerebellum desc','Stage_id9','Stage_id13',1);

        INSERT INTO anatEntityRelation(anatEntitySourceId,anatEntityTargetId,relationType,relationStatus)
        VALUES ('Anat_id1','Anat_id1','is_a part_of','reflexive'),
               ('Anat_id2','Anat_id2','is_a part_of','reflexive'),
               ('Anat_id3','Anat_id3','is_a part_of','reflexive'),
               ('Anat_id4','Anat_id4','is_a part_of','reflexive'),
               ('Anat_id5','Anat_id5','is_a part_of','reflexive'),
               ('Anat_id6','Anat_id6','is_a part_of','reflexive'),
               ('Anat_id7','Anat_id7','is_a part_of','reflexive'),
               ('Anat_id8','Anat_id8','is_a part_of','reflexive'),
               ('Anat_id9','Anat_id9','is_a part_of','reflexive'),
               ('Anat_id10','Anat_id10','is_a part_of','reflexive'),
               ('Anat_id11','Anat_id11','is_a part_of','reflexive'),
               ('Anat_id2','Anat_id1','is_a part_of','direct'),
               ('Anat_id3','Anat_id2','is_a part_of','direct'),
               ('Anat_id4','Anat_id2','is_a part_of','direct'),
               ('Anat_id5','Anat_id2','is_a part_of','direct'),
               ('Anat_id5','Anat_id7','develops_from','indirect'),
               ('Anat_id5','Anat_id8','develops_from','direct'),
               ('Anat_id6','Anat_id1','is_a part_of','direct'),
               ('Anat_id7','Anat_id6','develops_from','direct'),
               ('Anat_id8','Anat_id7','is_a part_of','direct'),
               ('Anat_id9','Anat_id5','is_a part_of','direct'),
               ('Anat_id10','Anat_id5','is_a part_of','direct'),
               ('Anat_id11','Anat_id10','is_a part_of','direct');
               
        INSERT INTO expression(expressionId,geneId,anatEntityId,stageId,estData,affymetrixData,inSituData,rnaSeqData)
        VALUES (1,'ID3','Anat_id1','Stage_id1','no data','poor quality','high quality','high quality'),
               (2,'ID1','Anat_id6','Stage_id6','high quality','poor quality','high quality','poor quality'),
               (3,'ID1','Anat_id6','Stage_id7','no data','no data','no data','poor quality'),
               (4,'ID2','Anat_id2','Stage_id18','high quality','high quality','high quality','high quality'),
               (5,'ID1','Anat_id7','Stage_id10','poor quality','poor quality','poor quality','poor quality'),
               (6,'ID2','Anat_id11','Stage_id12','poor quality','high quality','no data','high quality'),
               (7,'ID2','Anat_id11','Stage_id13','high quality','no data','poor quality','no data');

        INSERT INTO globalExpressionToExpression (expressionId,globalExpressionId) 
        VALUES (1,1),
               (1,8),
               (1,9),
               (1,10),
               (1,11),
               (1,12),
               (1,13),
               (1,14),
               (1,15),
               (4,4),
               (4,16),
               (4,17),
               (4,18),
               (4,19),
               (4,20),
               (4,21);

        INSERT INTO globalExpression(globalExpressionId,geneId,anatEntityId,stageId,estData,affymetrixData,inSituData,rnaSeqData,originOfLine)
        VALUES (1,'ID3','Anat_id1','Stage_id1','no data','poor quality','high quality','high quality','self'),
               (2,'ID1','Anat_id6','Stage_id6','high quality','poor quality','high quality','poor quality','self'),
               (3,'ID1','Anat_id6','Stage_id7','no data','no data','no data','poor quality','self'),
               (4,'ID2','Anat_id2','Stage_id18','high quality','high quality','high quality','high quality','self'),
               (5,'ID1','Anat_id7','Stage_id10','poor quality','poor quality','poor quality','poor quality','self'),
               (6,'ID2','Anat_id11','Stage_id12','poor quality','high quality','no data','high quality','self'),
               (7,'ID2','Anat_id11','Stage_id13','high quality','no data','poor quality','no data','self'),
               
               (8,'ID3','Anat_id2','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (9,'ID3','Anat_id3','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (10,'ID3','Anat_id4','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (11,'ID3','Anat_id5','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (12,'ID3','Anat_id6','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (13,'ID3','Anat_id9','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (14,'ID3','Anat_id10','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               (15,'ID3','Anat_id11','Stage_id1','no data','poor quality','high quality','high quality','descent'),
               
               (16,'ID2','Anat_id3','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (17,'ID2','Anat_id4','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (18,'ID2','Anat_id5','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (19,'ID2','Anat_id9','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (20,'ID2','Anat_id10','Stage_id18','high quality','high quality','high quality','high quality','descent'),
               (21,'ID2','Anat_id11','Stage_id18','high quality','high quality','high quality','high quality','descent');

    END IF; 
END
;

-- Procedure to empty table in UPDATE/INSERT databases used for integration tests.
DROP PROCEDURE IF EXISTS emptyTestDBs
;

CREATE PROCEDURE emptyTestDBs()
BEGIN
    
-- SUMMARY NO-EXPRESSION CALLS
DELETE FROM differentialExpression;
DELETE FROM globalNoExpressionToNoExpression;
DELETE FROM globalNoExpression;
DELETE FROM noExpression;

-- SUMMARY EXPRESSION CALLS
DELETE FROM hogExpressionSummary;
DELETE FROM hogExpressionToExpression;
DELETE FROM hogExpression;
DELETE FROM globalExpressionToExpression;
DELETE FROM globalExpression;
DELETE FROM expression;

-- RAW DIFFERENTIAL EXPRESSION ANALYSES
DELETE FROM deaRNASeqSummary;
DELETE FROM deaAffymetrixProbesetSummary;
DELETE FROM deaSampleGroupToRnaSeqLibrary;
DELETE FROM deaSampleGroupToAffymetrixChip;
DELETE FROM deaSampleGroup;
DELETE FROM differentialExpressionAnalysis;

-- RNA-SEQ DATA
DELETE FROM rnaSeqResult;
DELETE FROM rnaSeqRun;
DELETE FROM rnaSeqLibrary;
DELETE FROM rnaSeqPlatform;
DELETE FROM rnaSeqExperimentToKeyword;
DELETE FROM rnaSeqExperiment;

-- IN SITU HYBRIDIZATION DATA
DELETE FROM inSituSpot;
DELETE FROM inSituEvidence;
DELETE FROM inSituExperimentToKeyword;
DELETE FROM inSituExperiment;

-- RAW AFFYMETRIX DATA
DELETE FROM affymetrixProbeset;
DELETE FROM affymetrixChip;
DELETE FROM chipType;
DELETE FROM microarrayExperimentToKeyword;
DELETE FROM microarrayExperiment;

-- RAW EST DATA
DELETE FROM expressedSequenceTag;
DELETE FROM estLibraryToKeyword;
DELETE FROM estLibrary;

-- GENE
DELETE FROM geneToGeneOntologyTerm;
DELETE FROM geneToTerm;
DELETE FROM geneXRef;
DELETE FROM geneNameSynonym;
DELETE FROM gene;
DELETE FROM geneBioType;
DELETE FROM geneOntologyRelation;
DELETE FROM geneOntologyTermAltId;
DELETE FROM geneOntologyTerm;
DELETE FROM OMAHierarchicalGroup;

-- ANATOMY AND DEVELOPMENT
DELETE FROM anatEntityRelation;
DELETE FROM anatEntityNameSynonym;
DELETE FROM anatEntityXRef;
DELETE FROM anatEntity;
DELETE FROM stageXRef;
DELETE FROM stageNameSynonym;
DELETE FROM stage;

-- TAXONOMY
DELETE FROM species;
DELETE FROM taxon;

-- GENERAL
DELETE FROM keyword;
DELETE FROM author;
DELETE FROM dataSource;

END
;
