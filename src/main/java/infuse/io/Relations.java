package infuse.io;

import infuse.Model;

import java.util.Set;

/**
 * Shows relations between models.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Relations {

  /**
   * Children of the model.
   * 
   * @param m The model.
   * @return The children.
   */
  Set<Model> getChilds(Model m);

  /**
   * Parents of the model.
   * 
   * @param m The model.
   * @return The parents.
   */
  Set<Model> getParents(Model m);

  /**
   * Getter.
   * 
   * @param from The parent model.
   * @param to The child model.
   * @return The label of the edge.
   */
  String getLabel(Model from, Model to);

}
