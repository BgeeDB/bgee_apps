package org.bgee.model.function;

/**
 * Represents a function that accepts four arguments and produces a result. 
 * This is a functional interface whose functional method is 
 * {@link #apply(Object, Object, Object, Object)}.
 * 
 * @param T the type of the first argument to the function
 * @param U the type of the second argument to the function
 * @param V the type of the third argument to the function
 * @param W the type of the fourth argument to the function
 * @param R the type of the result of the function
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Oct. 2016
 * @since   Bgee 13, Oct. 2016
 */
@FunctionalInterface
public interface QuadriFunction<T, U, V, W, R> {
    public R apply(T t, U u, V v, W w);
}
