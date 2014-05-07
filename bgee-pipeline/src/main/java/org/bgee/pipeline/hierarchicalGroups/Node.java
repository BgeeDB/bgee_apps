package org.bgee.pipeline.hierarchicalGroups;

import java.util.ArrayList;
import java.util.List;

import sbc.orthoxml.Group;

/**
 * This class basically defines a tree data structure. This data structure
 * projects the complete data of an orthologus group in the form of a tree.
 * Every {@code Node} object contains information about each node of the
 * tree. The data of each orthologus group is stored as a nested set model
 * 
 * @author Komal Sanjeev
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
public class Node {

	/**
	 * An {@code int} representing a unique ID for every entry node.
	 */
	private int OMANodeId;

	/**
	 * An {@code int} representing OMA group ID.
	 */
	private int OMAGroupId;

	/**
	 * An OMA {@code Group} of the {@code Node}.
	 */
	private Group OMAGroup;

	/**
	 * A {@code List} of {@code Node} objects representing the all the
	 * children {@code Node}s of the current {@code Node}.
	 */
	private List<Node> childNodes;

	/**
	 * An {@code long} representing a unique OMA Node Left Bound ID for
	 * every {@code Node} in a particular group of orthologus genes, when
	 * ordered according to the nested set model.
	 */
	private int OMANodeLeftBound;

	/**
	 * A {@code long} representing a unique OMA Node Right Bound ID for
	 * every {@code Node} in a particular group of orthologus genes, when
	 * ordered according to the nested set model.
	 */
	private int OMANodeRightBound;

	/**
	 * A {@code String} representing the ENSEMBL gene ID of the gene.
	 */
	private List<String> geneIDs;

	/**
	 * An {@code long} representing a unique ID for every entry node.
	 */
	private String ncbiTaxonomyRange;

	/**
	 * Default constructor
	 */
	public Node() {
		this.setOMANodeId(0);
		this.setOMAGroupId(0);
		this.setOMAGroup(null);
		this.setChildNodes(new ArrayList<Node>());
		this.setOMANodeLeftBound(0);
		this.setOMANodeRightBound(0);
		this.setNcbiTaxonomyRange(null);
		this.setGeneIDs(null);
	}

	public Node(int nodeId) {
		setOMANodeId(nodeId);
		this.setOMAGroupId(0);
		this.setOMAGroup(null);
		this.setChildNodes(new ArrayList<Node>());
		this.setOMANodeLeftBound(0);
		this.setOMANodeRightBound(0);
		this.setNcbiTaxonomyRange(null);
		this.setGeneIDs(null);
	}

	/**
	 * @return the nodeId
	 */
	public int getOMANodeId() {
		return this.OMANodeId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setOMANodeId(int OMANodeId) {
		this.OMANodeId = OMANodeId;
	}

	/**
	 * @return the OMA group ID
	 */
	public int getOMAGroupId() {
		return this.OMAGroupId;
	}

	/**
	 * @param OMAGroupId
	 *            the OMA group ID to set
	 */
	public void setOMAGroupId(int OMAGroupId) {
		this.OMAGroupId = OMAGroupId;
	}

	/**
	 * @return the OMA group
	 */
	public Group getOMAGroup() {
		return this.OMAGroup;
	}

	/**
	 * @param OMAGroup
	 *            the OMA group to set
	 */
	public void setOMAGroup(Group OMAGroup) {
		this.OMAGroup = OMAGroup;
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
	 * @return the OMANodeLeftBound
	 */
	public long getOMANodeLeftBound() {
		return this.OMANodeLeftBound;
	}

	/**
	 * @param OMANodeLeftBound
	 *            the OMANodeLeftBound to set
	 */
	public void setOMANodeLeftBound(int OMANodeLeftBound) {
		this.OMANodeLeftBound = OMANodeLeftBound;
	}

	/**
	 * @return the OMANodeRightBound
	 */
	public long getOMANodeRightBound() {
		return this.OMANodeRightBound;
	}

	/**
	 * @param OMANodeRightBound
	 *            the OMANodeRightBound to set
	 */
	public void setOMANodeRightBound(int OMANodeRightBound) {
		this.OMANodeRightBound = OMANodeRightBound;
	}

	/**
	 * @return the ncbiTaxonRange
	 */
	public String getNcbiTaxonomyRange() {
		return this.ncbiTaxonomyRange;
	}

	/**
	 * @param ncbiTaxonRange
	 *            the ncbiTaxonRange to set
	 */
	public void setNcbiTaxonomyRange(String ncbiTaxonomyRange) {
		this.ncbiTaxonomyRange = ncbiTaxonomyRange;
	}

	/**
	 * @return the geneID
	 */
	public List<String> getGeneIDs() {
		return this.geneIDs;
	}

	/**
	 * @param geneID
	 *            the geneID to set
	 */
	public void setGeneIDs(List<String> geneIDs) {
		this.geneIDs = geneIDs;
	}
}