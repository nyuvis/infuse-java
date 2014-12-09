package infuse;

import infuse.io.ModelPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jkanvas.io.json.JSONElement;

/**
 * A feature from a clinical data-set.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ClinicalFeature {

  /** The subtype. */
  private final String subtype;
  /** The type. */
  private final String type;
  /** The name. */
  private final String name;
  /** Known models that include this feature. */
  private final Map<Model, Integer> models;
  /** The id. */
  private final int id;

  /**
   * Creates a new clinical feature.
   * 
   * @param type The type.
   * @param name The name.
   * @param subtype The subtype.
   * @param id The id.
   */
  public ClinicalFeature(final String type, final String name,
      final String subtype, final int id) {
    this.subtype = Objects.requireNonNull(subtype);
    this.type = Objects.requireNonNull(type);
    this.name = Objects.requireNonNull(name);
    this.id = id;
    models = new HashMap<>();
  }

  /**
   * Adds a model to the feature.
   * 
   * @param model The model to add.
   * @param rank The rank of the feature in the model.
   */
  public void addModel(final Model model, final int rank) {
    if(models.containsKey(model)) throw new IllegalStateException(
        "model " + model + " already added to feature");
    models.put(model, rank);
  }

  public boolean hasRank(final Model model) {
    return models.containsKey(model);
  }

  public int getRank(final Model model) {
    return models.get(model);
  }

  public String getSubtype() {
    return subtype;
  }

  public int getId() {
    return id;
  }

  public Collection<Model> models() {
    return models.keySet();
  }

  /**
   * Getter.
   * 
   * @return The type.
   */
  public String getType() {
    return type;
  }

  /**
   * Getter.
   * 
   * @return The name.
   */
  public String getRealName() {
    return name;
  }

  @Override
  public String toString() {
    return getRealName();
  }

  /**
   * Reads a feature from JSON input.
   * 
   * @param features The feature pool. The same features may appear in multiple
   *          files.
   * @param el The JSON element.
   * @param pool The known models.
   */
  public static final void fromJSON(
      final Map<Integer, ClinicalFeature> features,
      final JSONElement el, final ModelPool pool) {
    final int id = el.getInt("id");
    final ClinicalFeature feature;
    if(features.containsKey(id)) {
      feature = features.get(id);
    } else {
      final String type = el.getString("type");
      final String name = el.getString("name");
      final String subtype = el.getString("subtype");
      feature = new ClinicalFeature(type, name, subtype, id);
      features.put(id, feature);
    }
    final JSONElement models = el.getValue("models");
    for(final JSONElement cur : models) {
      final String name = Objects.requireNonNull(cur.name());
      if(!name.startsWith("FeatureSelection")) {
        continue;
      }
      final int rank = cur.getInt("N");
      feature.addModel(Objects.requireNonNull(pool.get(name)), rank);
    }
  }

}
