-- Procedure to drop all databases used for integration tests. These databases must start 
-- with the prefix 'bgeeIntegrationTest_' to be dropped. This prefix is hardcoded 
-- on purpose, to be sure there could be no unexpected database deletions (a former version 
-- of this script provided the possibility to set this prefix using a variable).
-- This procedure should be called after the integration tests. This file is notably used by 
-- the sql-maven-plugin, during the post-integration-test of the maven-failsafe-plugin. 

-- Thie file is tuned to be functional for the sql-maven-plugin, this explains why we need 
-- these weird semicolons alone on some lines.


-- If you need to run this file yourself in standalone, uncomment the following line:
-- use test;
 
DROP PROCEDURE IF EXISTS dropBgeeIntegrationTestDBs
;

-- If you need to run this file yourself in standalone, uncomment the following line:
-- DELIMITER //

CREATE PROCEDURE dropBgeeIntegrationTestDBs()
BEGIN
	

DECLARE finished INTEGER DEFAULT 0;
DECLARE dbname VARCHAR(255);
DECLARE cur CURSOR FOR SELECT SCHEMA_NAME FROM information_schema.SCHEMATA 
                       WHERE SCHEMA_NAME like 'bgeeIntegrationTest_%';
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

END
-- If you need to run this file yourself in standalone, uncomment the following lines:
-- //
-- DELIMITER ;

;

CALL dropBgeeIntegrationTestDBs
;
DROP PROCEDURE dropBgeeIntegrationTestDBs
;
