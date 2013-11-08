-- Procedure to drop all databases used for integration tests. 
-- This procedure should be called after the integration tests, in case some tests failed 
-- and the databases were not dropped properly. This file is notably used by 
-- the sql-maven-plugin, during the post-integration-test of the maven-failsafe-plugin 

-- a proceure need to be attqached to a database, so we use the test database
use test;

DROP PROCEDURE IF EXISTS dropBgeeIntegrationTestDBs;

DELIMITER $$

CREATE PROCEDURE dropBgeeIntegrationTestDBs()
BEGIN

DECLARE finished INTEGER DEFAULT 0;
DECLARE dbname VARCHAR(255);
DECLARE cur CURSOR FOR SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME like 'bgeeIntegrationTest_%';
DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

OPEN cur;

read_loop: LOOP
    FETCH cur INTO dbname;
    IF finished = 1 THEN
      LEAVE read_loop;
    END IF;
    SET @dropcmd = CONCAT('DROP DATABASE ', dbname);
    PREPARE stmt FROM @dropcmd;
    EXECUTE stmt; 
    DEALLOCATE PREPARE stmt;
    
END LOOP;

CLOSE cur;

END$$

DELIMITER ;

CALL dropBgeeIntegrationTestDBs;
DROP PROCEDURE dropBgeeIntegrationTestDBs;
