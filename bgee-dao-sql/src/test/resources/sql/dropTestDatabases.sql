-- Procedure to drop all databases used for integration tests. 
-- This procedure should be called after the integration tests, in case some tests failed 
-- and the databases were not dropped properly. This file is notably used by 
-- the sql-maven-plugin, during the post-integration-test of the maven-failsafe-plugin 

-- Thie file is tuned to be functional for the sql-maven-plugin, this explains why we need 
-- this weird semicolons alone on some lines.


-- If you need to run this file yourself in standalone, uncomment the following line:
-- use test;
 
-- the variable @bgeeTestDBPrefix will allow maven to provide a user-defined value.
SET @bgeeTestDBPrefix = IF (@bgeeTestDBPrefix is null OR @bgeeTestDBPrefix like '% %' OR 
                        @bgeeTestDBPrefix like '%\%%', 'bgeeIntegrationTest_', @bgeeTestDBPrefix)
;
 
DROP PROCEDURE IF EXISTS dropBgeeIntegrationTestDBs
;

-- If you need to run this file yourself in standalone, uncomment the following line:
-- DELIMITER //

CREATE PROCEDURE dropBgeeIntegrationTestDBs()
BEGIN
	

DECLARE finished INTEGER DEFAULT 0;
DECLARE dbname VARCHAR(255);
DECLARE cur CURSOR FOR SELECT SCHEMA_NAME FROM information_schema.SCHEMATA 
                       WHERE SCHEMA_NAME like CONCAT(@bgeeTestDBPrefix, '%');
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
