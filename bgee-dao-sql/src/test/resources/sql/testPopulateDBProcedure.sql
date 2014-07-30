-- Procedure to insert data in SELECT/UPDATE databases used for integration tests.

DROP PROCEDURE IF EXISTS populateTestDBs
;

CREATE PROCEDURE populateTestDBs()
BEGIN
    

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

INSERT INTO stage (stageId,stageName,stageDescription,stageLeftBound,stageRightBound,stageLevel,tooGranular,groupingStage) 
    VALUES ('Stage_id1','stageN1','stage Desc 1',1,6,1,false,true),
           ('Stage_id2','stageN2','stage Desc 2',2,3,2,true,false),
           ('Stage_id3','stageN3','stage Desc 3',4,5,2,false,false);
        
INSERT INTO gene (geneId,geneName,geneDescription,speciesId,geneBioTypeId,OMAParentNodeId,ensemblGene) 
    VALUES ('ID1','genN1','genDesc1',11,12,2,true),
           ('ID2','genN2','genDesc2',21,null,null,true),
           ('ID3','genN3','genDesc3',31,null,3,false);
           
END
;
