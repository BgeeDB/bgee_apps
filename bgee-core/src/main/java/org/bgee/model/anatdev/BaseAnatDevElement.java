package org.bgee.model.anatdev;

/**
 * This class implements the methods specified by the interface {@link AnatDevElement}, 
 * as well as other common methods not part of the interface. 
 * <p>
 * This class is developed to be able to use the Composition design pattern, 
 * and is meant to be used only by classes implementing the {@link AnatDevElement} interface, 
 * or one of its sub-interface, to delegate method calls to it. So this class is not meant 
 * to be part of the public API. 
 * <p>
 * This class also provides methods not part of the interface, but that are nevertheless 
 * needed by all classes implementing the interface; this is because these methods 
 * perform a task that is equivalent programmatically, but very different conceptually 
 * at the class level. For instance, an {@link AnatElement} can hold several 
 * {@link DevElement}s, that could represent stages during which an anatomical 
 * entity exists. In the same way, a {@link DevElement} can hold several 
 * {@link AnatElement}s, that could represent anatomical entities existing 
 * during a specific stage. Programatically, this is easily achieved by maintaining 
 * a {@code Collection} of a generic type extending {@link AnatDevElement}; 
 * But still, we need distinct method names, as these two situations are conceptually 
 * very different, and as a result, we do not want a common obscure method name 
 * such as "getEncapsulatedAnatDevElements". Such obscure method names will exist 
 * in this class, with nicely named exposed methods delegating calls to it. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class BaseAnatDevElement implements AnatDevElement {

    @Override
    public void registerWithId(String id) {
        // TODO Auto-generated method stub
        
    }

}
