package org.bgee.pipeline.hierarchicalGroups;

import java.util.ArrayList;
import java.util.List;

/**
 * This class basically defines a tree data structure. This data structure
 * projects the complete data of an orthologus group in the form of a tree.
 * Every <code>Node</code> object contains information about each node of the
 * tree. The data of each orthologus group is stored as a nested set model
 * 
 * @author Komal Sanjeev
 * @version
 * @since March 2013
 */
public class Node {

	/**
	 * An <code>long</code> representing a unique ID for every entry node.
	 */
	private long hierarchicalGroupId;

	/**
	 * A <code>List</code> of <code>Node</code> objects representing the all the
	 * children <code>Node</code>s of the current <code>Node</code>.
	 */
	private List<Node> childNodes;

	/**
	 * An <code>long</code> representing a unique Hierarchical Left Bound ID for
	 * every <code>Node</code> in a particular group of orthologus genes, when
	 * ordered according to the nested set model.
	 */
	private long hierarchicalLeftBound;

	/**
	 * A <code>long</code> representing a unique Hierarchical Right Bound ID for
	 * every <code>Node</code> in a particular group of orthologus genes, when
	 * ordered according to the nested set model.
	 */
	private long hierarchicalRightBound;

	/**
	 * A <code>String</code> representing the ENSEMBL gene ID of the gene.
	 */
	private String GeneID;

	/**
	 * An <code>long</code> representing a unique ID for every entry node.
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
		this.setNcbiTaxonomyId(null);
		this.setGeneID(null);
	}

	public Node(long data) {
		setHierarchicalGroupId(data);
		this.setChildNodes(new ArrayList<Node>());
		this.setHierarchicalLeftBound(0);
		this.setHierarchicalRightBound(0);
		this.setNcbiTaxonomyId(null);
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
	public void setHierarchicalGroupId(long hierarchicalGroupId) {
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
	public void setHierarchicalLeftBound(long hierarchicalLeftBound) {
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
	public void setHierarchicalRightBound(long hierarchicalRightBound) {
		this.hierarchicalRightBound = hierarchicalRightBound;
	}

	/**
	 * @return the ncbiTaxonomyId
	 */
	public String getNcbiTaxonomyId() {
		return this.ncbiTaxonomyId;
	}

	/**
	 * @param ncbiTaxonID
	 *            the ncbiTaxonID to set
	 */
	public void setNcbiTaxonomyId(String ncbiTaxonomyId) {
		this.ncbiTaxonomyId = ncbiTaxonomyId;
	}

	/**
	 * @return the geneID
	 */
	public String getGeneID() {
		return this.GeneID;
	}

	/**
	 * @param geneID
	 *            the geneID to set
	 */
	public void setGeneID(String geneID) {
		GeneID = geneID;
	}
}