package org.bgee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.bgee.Tau.Row;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

//quickly developed class allowing to load data from the database
public class TestTempDAO implements AutoCloseable{
	
	Connection conn;
	Context context;
	MysqlDataSource dataSource;
	
	public TestTempDAO(MysqlDataSource datasource) throws NamingException {
		context = new InitialContext();
		this.dataSource = datasource;
	}
	
	public TestTempDAO(String user, String pwd, String server, String dbName) throws NamingException {
		context = new InitialContext();
		dataSource = new MysqlDataSource();
		dataSource.setUser(user);
		dataSource.setPassword(pwd);
		dataSource.setServerName(server);
		dataSource.setDatabaseName(dbName);
	}
	
	//load expressions for one geneId
	public Set<Row> getRanksAndAnatEntitiesExpressionForOneGene(String geneId) throws SQLException{
		Set<Row> rows = new HashSet<Row>();
		String sql = "select t1.bgeeGeneId, t3.anatEntityName, "
				+ "(IF(affymetrixMeanRankNorm IS NULL, 0, affymetrixDistinctRankSum * affymetrixMeanRankNorm) "
				+ "+ IF(rnaSeqMeanRankNorm IS NULL, 0, rnaSeqDistinctRankSum * rnaSeqMeanRankNorm)"
				+ "+ IF(estRankNorm IS NULL, 0, estMaxRank * estRankNorm) "
				+ "+ IF(InSituRankNorm IS NULL, 0, inSituMaxRank * inSituRankNorm))"
				+ "/(IF(affymetrixMeanRankNorm IS NULL, 0, affymetrixDistinctRankSum) "
				+ "+ IF(rnaSeqMeanRankNorm IS NULL, 0, rnaSeqDistinctRankSum) "
				+ "+ IF(estRankNorm IS NULL, 0, estMaxRank) "
				+ "+ IF(InSituRankNorm IS NULL, 0, inSituMaxRank)) "
				+ "AS rank from globalExpression AS t1 inner join globalCond "
				+ "AS t2 ON t1.globalConditionId = t2.globalConditionId "
				+ "INNER JOIN anatEntity AS t3 ON t2.anatEntityId = t3.anatEntityId "
				+ "where bgeeGeneId IN (SELECT bgeeGeneId FROM gene WHERE geneId IN (?)) "
				+ "ORDER BY rank DESC";
		conn = dataSource.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql);){
			pstmt.setString(1, geneId);
			System.out.println(pstmt);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				rows.add(new Row(rs.getInt("bgeeGeneId"), rs.getString("anatEntityName"), rs.getBigDecimal("rank")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  rows;
	}
	
	//load all expressions for one species ordered by ensembl geneId
	public Map<String, Set<Row>> getRanksAndAnatEntitiesExpressionForOneSpecies(Integer SpeciesId) throws SQLException{
		Map<String,Set<Row>> geneIdToExpressions = new HashMap<>();
		String sql = "select t1.bgeeGeneId, t3.anatEntityName, "
				+ "(IF(affymetrixMeanRankNorm IS NULL, 0, affymetrixDistinctRankSum * affymetrixMeanRankNorm) "
				+ "+ IF(rnaSeqMeanRankNorm IS NULL, 0, rnaSeqDistinctRankSum * rnaSeqMeanRankNorm)"
				+ "+ IF(estRankNorm IS NULL, 0, estMaxRank * estRankNorm) "
				+ "+ IF(InSituRankNorm IS NULL, 0, inSituMaxRank * inSituRankNorm))"
				+ "/(IF(affymetrixMeanRankNorm IS NULL, 0, affymetrixDistinctRankSum) "
				+ "+ IF(rnaSeqMeanRankNorm IS NULL, 0, rnaSeqDistinctRankSum) "
				+ "+ IF(estRankNorm IS NULL, 0, estMaxRank) "
				+ "+ IF(InSituRankNorm IS NULL, 0, inSituMaxRank)) "
				+ "AS rank from globalExpression AS t1 inner join globalCond "
				+ "AS t2 ON t1.globalConditionId = t2.globalConditionId "
				+ "INNER JOIN anatEntity AS t3 ON t2.anatEntityId = t3.anatEntityId "
				+ "where bgeeGeneId IN (SELECT bgeeGeneId FROM gene WHERE speciesId = ?) "
				+ "ORDER BY rank DESC";
		conn = dataSource.getConnection();
		try (PreparedStatement pstmt = conn.prepareStatement(sql);){
			pstmt.setInt(1, SpeciesId);
			System.out.println(pstmt);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				String geneId= rs.getString("bgeeGeneId");
				if(geneIdToExpressions.containsKey(geneId)){
							geneIdToExpressions.get(geneId).add(new Row(rs.getInt("bgeeGeneId"), rs.getString("anatEntityName"), rs.getBigDecimal("rank")));
				}else{
					Set<Row> rowSet = new HashSet<Row>();
					rowSet.add(new Row(rs.getInt("bgeeGeneId"), rs.getString("anatEntityName"), rs.getBigDecimal("rank")));
					geneIdToExpressions.put(geneId, rowSet);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return  geneIdToExpressions;
	}

	@Override
	public void close() throws Exception {
		conn.close();
		
	}

}
