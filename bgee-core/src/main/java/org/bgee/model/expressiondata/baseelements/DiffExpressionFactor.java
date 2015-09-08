package org.bgee.model.expressiondata.baseelements;

/**
 * An {@code enum} defining the types of differential expression analyses, 
 * based on the experimental factor studied: 
 * <ul>
 * <li>ANATOMY: analyses comparing different anatomical structures at a same 
 * (broad) developmental stage. The experimental factor is the anatomy, 
 * these analyses try to identify in which anatomical structures genes are 
 * differentially expressed. 
 * <li>DEVELOPMENT: analyses comparing for a same anatomical structure 
 * different developmental stages. The experimental factor is the developmental time, 
 * these analyses try to identify for a given anatomical structures at which 
 * developmental stages genes are differentially expressed. 
 * </ul>
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public enum DiffExpressionFactor {
    ANATOMY, DEVELOPMENT;
}
