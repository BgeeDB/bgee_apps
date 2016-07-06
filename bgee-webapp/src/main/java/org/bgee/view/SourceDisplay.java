package org.bgee.view;

import java.util.Collection;

import org.bgee.model.source.Source;

/**
 * Interface defining methods to be implemented by views related to {@code Source}s.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Mar. 2016
 * @since   Bgee 13, Mar. 2016
 */
public interface SourceDisplay {

    /**
     * Displays the source page.
     */
    void displaySources(Collection<Source> sources);
    
}
