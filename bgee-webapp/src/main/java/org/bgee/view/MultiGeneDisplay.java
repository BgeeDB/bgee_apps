package org.bgee.view;


import java.util.List;

/**
 * Interface defining methods to be implemented FIXME.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public interface MultiGeneDisplay {

    /**
     * Displays the default multi-gene page (when no arguments are given)
     */
    void displayMultiGeneHomePage();

    /**
     * Displays information about a search of multi-gene expression.
     */
    void displayMultiGene(List<String> geneList);

}
