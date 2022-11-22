package org.bgee.model.expressiondata.rawdata;

import org.bgee.model.NamedEntity;

/**
 * Class created solely for returning consistent search results for experiments or assays,
 * with common attributes id, name, description.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 */
public class ExperimentAssay extends NamedEntity<String> {

    public ExperimentAssay(String id, String name, String description) {
        super(id, name, description);
    }
}
