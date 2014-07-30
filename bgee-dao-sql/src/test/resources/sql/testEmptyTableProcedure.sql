-- Procedure to empty table in UPDATE/INSERT databases used for integration tests.

DROP PROCEDURE IF EXISTS emptyTestDBs
;

CREATE PROCEDURE emptyTestDBs(IN tableName VARCHAR(255))
BEGIN
    
	SET @SQL = CONCAT('DELETE FROM ', tableName);
    PREPARE stmt FROM @SQL;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

END
;
