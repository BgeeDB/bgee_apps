package org.bgee.model.dao.api.anatdev;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO.DAORawDataSex;

/**
 * A {@code DAO} for retrieving information related to sex.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Oct. 2022
 * @since Bgee 15.0, Oct. 2022
 */
//How to deal with Attributes here?
public interface SexDAO extends DAO {

    /**
     * Retrieve the mappings between sexes, and the species they are valid in.
     * <p>
     * Note that using the {@code setAttributes} methods (see {@link DAO}) has no effect 
     * on attributes retrieved in {@code SpeciesToSexTO}s.
     * <p>
     * The associations are returned as a {@code SpeciesToSexTOResultSet}. It is 
     * the responsibility of the caller to close this {@code DAOResultSet} once results are 
     * retrieved.
     *
     * @param speciesIds        A {@code Set} of {@code Integer}s that are the IDs 
     *                          of the species for which the associations to sex should be retrieved.
     * @return                  A {@code SpeciesToSexTOResultSet} allowing 
     *                          to retrieve the requested {@code SpeciesToSexTO}s.
     * @throws DAOException     If an error occurred when accessing the data source. 
     */
    public SpeciesToSexTOResultSet getSpeciesToSex(Collection<Integer> speciesIds);

    /**
     * {@code DAOResultSet} specifics to {@code SpeciesToSexTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 15.0, Oct. 2022
     * @since Bgee 15.0, Oct. 2022
     */
    public interface SpeciesToSexTOResultSet 
                    extends DAOResultSet<SpeciesToSexTO> {
    }
    /**
     * A {@code TransferObject} representing relation between a sex 
     * and a species it is valid in.
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 15.0, Oct. 2022
     * @since Bgee 15.0, Oct. 2022
     */
    public final class SpeciesToSexTO extends TransferObject {
        private static final long serialVersionUID = 2531269241324072504L;

        private final DAORawDataSex sex;
        private final Integer speciesId;

        public SpeciesToSexTO(DAORawDataSex sex, Integer speciesId) {
            this.sex = sex;
            this.speciesId = speciesId;
        }

        public DAORawDataSex getSex() {
            return sex;
        }
        public Integer getSpeciesId() {
            return speciesId;
        }
    }
}