package infuse.io;

import infuse.Model;

import java.util.Map;
import java.util.TreeMap;

import jkanvas.io.json.JSONElement;

/**
 * A model pool stores all models.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class ModelPool {

  /** The model map. */
  private final Map<String, Model> models;

  /** Creates an empty model pool. */
  public ModelPool() {
    models = new TreeMap<>();
  }

  /**
   * Adds all models from the given element.
   *
   * @param el The root element. A child of it must be <code>data</code> having
   *          a child <code>results</code> being a list containing the models.
   */
  public void addAll(final JSONElement el) {
    for(final JSONElement e : el.getValue("data").getValue("results")) {
      add(Model.fromJSON(e));
    }
  }

  /**
   * Adds a model.
   *
   * @param model The model.
   */
  public void add(final Model model) {
    if(models.containsKey(model.getId())) throw new IllegalArgumentException(
        model + " already added");
    models.put(model.getId(), model);
  }

  /**
   * Getter.
   *
   * @param name The name of the model.
   * @return The corresponding model.
   */
  public Model get(final String name) {
    return models.get(name);
  }

}
