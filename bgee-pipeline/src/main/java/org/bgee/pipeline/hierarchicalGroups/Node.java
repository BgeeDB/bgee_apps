package org.bgee.pipeline.hierarchicalGroups;

import java.util.ArrayList;
import java.util.List;

/**
 * This class basically defines a tree data structure. This data structure
 * projects the complete data of an orthologus group in the form of a tree.
 * Every {@code Node} object contains information about each node of the
 * tree. The data of each orthologus group is stored as a nested set model
 * 
 * @author Komal Sanjeev
 * @version
 * @since March 2013
 */
public class Node {

	/**
	 * An {@code long} representing a unique ID for every entry node.
	 */
	private int hierarchicalGroupId;

	/**
	 * A {@code List} of {@code Node} objects representing the all the
	 * children {@code Node}s of the current {@code Node}.
	 */
	private List<Node> childNodes;

	/**
	 * An {@code long} representing a unique Hierarchical Left Bound ID for
	 * every {@code Node} in a particular group of orthologus genes, when
	 * ordered according to the nested set model.
	 */
	private int hierarchicalLeftBound;

	/**
	 * A {@code long} representing a unique Hierarchical Right Bound ID for
	 * every {@code Node} in a particular group of orthologus genes, when
	 * ordered according to the nested set model.
	 */
	private int hierarchicalRightBound;

	/**
	 * A {@code String} representing the ENSEMBL gene ID of the gene.
	 */
	private String geneID;

	/**
	 * An {@code long} representing a unique ID for every entry node.
	 */
	private String ncbiTaxonomyId;

	/**
	 * Default constructor
	 */
	public Node() {
		this.setHierarchicalGroupId(0);
		this.setChildNodes(new ArrayList<Node>());
		this.setHierarchicalLeftBound(0);
		this.setHierarchicalRightBound(0);
		this.setNcbiTaxonomyRange(null);
		this.setGeneID(null);
	}

	public Node(int data) {
		setHierarchicalGroupId(data);
		this.setChildNodes(new ArrayList<Node>());
		this.setHierarchicalLeftBound(0);
		this.setHierarchicalRightBound(0);
		this.setNcbiTaxonomyRange(null);
		this.setGeneID(null);
	}

	/**
	 * @return the nodeId
	 */
	public long getHierarchicalGroupId() {
		return this.hierarchicalGroupId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setHierarchicalGroupId(int hierarchicalGroupId) {
		this.hierarchicalGroupId = hierarchicalGroupId;
	}

	/**
	 * @return the childNodes
	 */
	public List<Node> getChildNodes() {
		if (this.childNodes == null) {
			return new ArrayList<Node>();
		}
		return this.childNodes;
	}

	/**
	 * @param childNodes
	 *            the childNodes to set
	 */
	public void setChildNodes(List<Node> children) {
		this.childNodes = children;
	}

	/**
	 * @param childNode
	 *            the childNode to add
	 */
	public void addChild(Node child) {
		if (childNodes == null) {
			childNodes = new ArrayList<Node>();
		}
		childNodes.add(child);
	}

	/**
	 * @return the hierarchicalLeftBound
	 */
	public long getHierarchicalLeftBound() {
		return this.hierarchicalLeftBound;
	}

	/**
	 * @param hierarchicalLeftBound
	 *            the hierarchicalLeftBound to set
	 */
	public void setHierarchicalLeftBound(int hierarchicalLeftBound) {
		this.hierarchicalLeftBound = hierarchicalLeftBound;
	}

	/**
	 * @return the hierarchicalRightBound
	 */
	public long getHierarchicalRightBound() {
		return this.hierarchicalRightBound;
	}

	/**
	 * @param hierarchicalRightBound
	 *            the hierarchicalRightBound to set
	 */
	public void setHierarchicalRightBound(int hierarchicalRightBound) {
		this.hierarchicalRightBound = hierarchicalRightBound;
	}

	/**
	 * @return the ncbiTaxonomyId
	 */
	public String getNcbiTaxonomyRange() {
		return this.ncbiTaxonomyId;
	}

	/**
	 * @param ncbiTaxonID
	 *            the ncbiTaxonID to set
	 */
	public void setNcbiTaxonomyRange(String ncbiTaxonomyId) {
		this.ncbiTaxonomyId = ncbiTaxonomyId;
	}

	/**
	 * @return the geneID
	 */
	public String getGeneID() {
		return this.geneID;
	}

	/**
	 * @param geneID
	 *            the geneID to set
	 */
	public void setGeneID(String geneID) {
		this.geneID = geneID;
	}
}