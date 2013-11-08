-- This is a dump file containing test data, that are used for the integration tests 
-- of SELECT statements.

INSERT INTO dataSource (dataSourceId, dataSourceName, XRefUrl, experimentUrl, 
    evidenceUrl, baseUrl, releaseDate, releaseVersion, dataSourceDescription, 
    toDisplay, category, displayOrder) VALUES 
    (1, 'First DataSource', 'XRefUrl', 'experimentUrl', 'evidenceUrl', 'baseUrl', 
    NOW(), '1.0', 'My custom data source', 1, 'Genomics database', 1);