package org.bgee.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

import java.util.*;
import java.util.function.Function;

/**
 * Helper methods to work with {@code DAOResultSet}.
 * @author Philippe Moret
 */
public class DAOResultSetHelper {


    private static final Logger log = LogManager.getLogger(DAOResultSetHelper.class.getName());

    /**
     * Helper method to map all results into a {@code List}. The mapping is done on-the-fly by iterating on the
     * {@code DAOResultSet} thus avoiding excessive memory allocation.
     * Typical use includes mapping of {@code TransferObject} to their equivalent types in Bgee-core module.
     * @param resultSet The input {@code DAOResultSet}
     * @param mapper A function to transform the {@code TransferObject}
     * @param <T> The target type of the mapping
     * @param <E> The input type of the {@code DAOResultSet}, (i.e., extends {@code TransferObject}
     * @return The mapped {@code List}
     */
    public static <T,E extends TransferObject> List<T> mapToList(DAOResultSet<E> resultSet, Function<E,T> mapper)
        throws DAOException {
        log.entry(resultSet, mapper);
        List<T> result = mapToCollection(resultSet, mapper, new ArrayList<T>());
        return log.exit(result);
    }

    /**
     * Helper method to map all results into a {@code Set}. The mapping is done on-the-fly by iterating on the
     * {@code DAOResultSet} thus avoiding excessive memory allocation.
     * Typical use includes mapping of {@code TransferObject} to their equivalent types in Bgee-core module.
     * @param resultSet The input {@code DAOResultSet}
     * @param mapper A function to transform the {@code TransferObject}
     * @param <T> The target type of the mapping
     * @param <E> The input type of the {@code DAOResultSet}, (i.e., extends {@code TransferObject}
     * @return The mapped {@code Set}
     */
    public static <T,E extends  TransferObject> Set<T> mapToSet(DAOResultSet<E> resultSet, Function<E,T> mapper) {
        log.entry(resultSet, mapper);
        HashSet<T> result = mapToCollection(resultSet, mapper, new HashSet<T>());
        return log.exit(result);

    }

    /**
     * Helper method to map all results into a {@code Collection}. The mapping is done on-the-fly by iterating on the
     * {@code DAOResultSet} thus avoiding excessive memory allocation. The Collection passed in arguments
     * is filled with the mapped objects.
     * Typical use includes mapping of {@code TransferObject} to their equivalent types in Bgee-core module.
     * @param resultSet The input {@code DAOResultSet}
     * @param mapper A function to transform the {@code TransferObject}
     * @param <T> The target type of the mapping
     * @param <E> The input type of the {@code DAOResultSet}, (i.e., extends {@code TransferObject}
     * @param result The instantiated collection where the results will be added.
     * @return The mapped {@code List}
     */
    public static <T, E extends TransferObject, C extends Collection<T>> C mapToCollection(DAOResultSet<E> resultSet,
                                                                               Function<E,T> mapper,
                                                                               C result) {
        log.entry(result, mapper, result);
        while (resultSet.next()) {
            result.add(mapper.apply(resultSet.getTO()));
        }
        return log.exit(result);
    }



}
