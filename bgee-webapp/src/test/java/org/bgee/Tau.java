package org.bgee;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.supercsv.cellprocessor.ParseBigDecimal;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

public class Tau extends TestAncestor{

	private final static Logger log = LogManager.getLogger(TestTissueEnhanced.class.getName());
	private final static String inputFile = "src/test/resources/export.tsv";
	@Override
	protected Logger getLogger() {
		return log;
	}
	
	@Test
	public void tauValuesForAllGenesOfOneSpecies(){
		Integer speciesId = 7227;
		try (TestTempDAO dao = new TestTempDAO("bgee", "bgee", "altbioinfo","bgee_v14")){
			Map<String, Set<Row>>  geneIdsToAllExpression = dao.getRanksAndAnatEntitiesExpressionForOneSpecies(speciesId);
			log.debug("FINISH QUERY");
			geneIdsToAllExpression.keySet().parallelStream().forEach(gene ->{
				Set<Row> rows = geneIdsToAllExpression.get(gene);
				try{
					System.out.println("tau value for "+gene+" : "+tauValue(rows));
				}catch (Exception e) {
					log.error(e.getMessage());
				}
			});
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void tauValueForOneGene(){
		String geneId = "ENSG00000213144";
		try (TestTempDAO dao = new TestTempDAO("bgee", "bgee", "altbioinfo","bgee_v14")){
			Set<Row> rows = dao.getRanksAndAnatEntitiesExpressionForOneGene(geneId);
			System.out.println("tau value for "+geneId+" : "+tauValue(rows));
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public Double tauValue(Set<Row> rows) throws Exception{
		//XXX It's maybe wrong to use the max rank value for one gene. We must probably use the max rank of the whole database
		double max = rows.stream().map(i -> i.getRank().doubleValue()).reduce(0.0, (a, b) -> Double.max(a, b));
		double min = rows.stream().map(i -> i.getRank().doubleValue()).reduce(0.0, (a, b) -> Double.min(a, b));
		int rowNumber = rows.size();
		Set<Double> tauValues = new HashSet<Double>();
		//in bgee min rank value corresponds to the most expressed gene.
		//as tau used formula where the best rank is the biggest one, each rank value is defined as (max-current_rank_value)
		//and best ranked value is defined as (max-min)
		rows.stream().forEach( row -> {
			BigDecimal rankValue = row.getRank();
			tauValues.add(1-((max-rankValue.doubleValue())/(max-min)));
		});
		Double tau = tauValues.stream().mapToDouble(i -> i.doubleValue()).sum()/(rowNumber-1);
		return tau;	
	}

	public static class Row{

		private int bgeeGeneId;
		private String anatEntityName;
		private BigDecimal rank;

		public Row(){}

		public Row(final int bgeeGeneId, final String anatEntityId, final BigDecimal rank){
			this.bgeeGeneId = bgeeGeneId;
			this.anatEntityName = anatEntityId;
			this.rank = rank;
		}

		public int getBgeeGeneId() {
			return bgeeGeneId;
		}

		public void setBgeeGeneId(int bgeeGeneId) {
			this.bgeeGeneId = bgeeGeneId;
		}

		public String getAnatEntityName() {
			return anatEntityName;
		}

		public void setAnatEntityName(String anatEntityName) {
			this.anatEntityName = anatEntityName;
		}

		public BigDecimal getRank() {
			return rank;
		}

		public void setRank(BigDecimal rank) {
			this.rank = rank;
		}

		@Override
		public String toString(){
			return this.bgeeGeneId+" "+this.anatEntityName+" "+this.rank;
		}


	}


	public static Set<Row> readWithCsvBeanReader() throws Exception {
		Set<Row> rows = new HashSet<Row>();
		log.debug(new File(inputFile).getAbsolutePath());
		try (ICsvBeanReader beanReader= new CsvBeanReader(new FileReader(inputFile), CsvPreference.TAB_PREFERENCE)) {
			// the header elements are used to map the values to the bean (names must match)
			final String[] header = beanReader.getHeader(true);
			final CellProcessor[] processors = new CellProcessor[] { 
					new ParseInt(), // geneId
					new NotNull(), // anatEntityName
					new ParseBigDecimal(), // rank
			};

			Row row;
			while( (row = beanReader.read(Row.class, header, processors)) != null ) {
				rows.add(row);
			}

		}
		return rows;
	}
	
	

}
