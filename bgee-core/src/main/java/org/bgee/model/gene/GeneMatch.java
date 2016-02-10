package org.bgee.model.gene;

/**
 * This class encapsulates a result to a gene search. A match can either be on the gene name
 * or a synonym. 
 * @author Philippe Moret
 *
 */
public class GeneMatch {

	protected GeneMatch(Gene gene, String synonym) {
		this.gene = gene;
		this.synonym = synonym;
	}
	
	private final Gene gene;
	
	private final String synonym;
	
	/**
	 * @return A {boolean}, true if the match was on a synonym of the gene, false otherwise.
	 */
	public boolean isSynonymMatch(){
		return synonym != null;
	}
	
	/**
	 * @return A {@code Gene} that was matched by the search.
	 */
	public Gene getGene() {
		return gene;
	}
	
	/**
	 * @return A {@code String} representing the matched synonym, null when there is no synonym match
	 */
	public String getMatchedSynonym() {
		return synonym;
	}
}
