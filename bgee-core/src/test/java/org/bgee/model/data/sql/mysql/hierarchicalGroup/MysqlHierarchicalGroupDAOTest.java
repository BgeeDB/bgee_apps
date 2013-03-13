package org.bgee.model.data.sql.mysql.hierarchicalGroup;

import java.sql.SQLException;

import org.bgee.model.data.sql.mysql.hierarchicalGroup.MysqlHierarchicalGroupDAO;

import static org.junit.Assert.*;

public class MysqlHierarchicalGroupDAOTest {

	public void testGetHierarchicalOrthologusGenes() throws SQLException {

		String[] requiredOrthologusGenes = new String[] { "ENSG00000165810",
				"ENSG00000186470", "ENSG00000215811", "ENSMUSG00000034359",
				"ENSXETG00000009860", "ENSXETG00000012100",
				"ENSXETG00000013072", "ENSXETG00000025123",
				"ENSXETG00000031931", "ENSXETG00000032302" };

		MysqlHierarchicalGroupDAO query = new MysqlHierarchicalGroupDAO();

		assertArrayEquals(requiredOrthologusGenes, query
				.getHierarchicalOrthologusGenes("ENSG00000186470", "Tetrapoda")
				.toArray());

	}

}
