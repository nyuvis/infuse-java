package infuse.io;

import infuse.ClinicalFeature;
import infuse.easy.JSONNodeCreator;
import infuse.easy.NodeBuilder;
import infuse.easy.NodeCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jkanvas.io.json.JSONElement;
import jkanvas.io.json.JSONReader;
import jkanvas.util.Resource;

/**
 * Loads clinical data.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public final class ClinicalLoader {

  /** No constructor. */
  private ClinicalLoader() {
    throw new AssertionError();
  }

  /**
   * Loads the data from the given resources.
   *
   * @param pool The pool which must be filled.
   * @param features The list where the features get stored in.
   * @param inputs The input files.
   * @throws IOException I/O Exception.
   */
  public static void loadData(final ModelPool pool,
      final List<ClinicalFeature> features, final Resource... inputs) throws IOException {
    final Map<Integer, ClinicalFeature> map = new HashMap<>();
    for(final Resource input : inputs) {
      final JSONReader in = new JSONReader(input.reader());
      final JSONElement root = in.get().getValue("data");
      for(final JSONElement el : root) {
        ClinicalFeature.fromJSON(map, el, pool);
      }
    }
    features.addAll(map.values());
  }

  /**
   * Fills the model pool.
   *
   * @param pool The pool to fill in.
   * @param in The input resource.
   * @throws IOException I/O Exception.
   */
  public static void fillModelPool(
      final ModelPool pool, final Resource in) throws IOException {
    final JSONElement root = new JSONReader(in.reader()).get();
    pool.addAll(root);
  }

  /**
   * Loads relations.
   *
   * @param pool The filled model pool.
   * @param in The input resource.
   * @return The relations.
   * @throws IOException I/O Exception.
   */
  public static Relations loadRelations(
      final ModelPool pool, final Resource in) throws IOException {
    return new RelationParser(pool, in);
  }

  /**
   * Remove empty features.
   *
   * @param features The feature list to clean up.
   */
  public static void removeEmptyFeatures(final List<ClinicalFeature> features) {
    final Iterator<ClinicalFeature> it = features.iterator();
    int rem = 0;
    while(it.hasNext()) {
      final ClinicalFeature f = it.next();
      if(f.models().isEmpty()) {
        it.remove();
        ++rem;
      }
    }
    if(rem > 0) {
      System.out.println("removed " + rem + " empty features");
    }
  }

  /**
   * Loads everything.
   *
   * @param models The model resource.
   * @param relations The relations resource.
   * @param inputs The input resources.
   * @return The resulting node creator.
   * @throws IOException I/O Exception.
   */
  public static NodeBuilder loadAll(final Resource models,
      final Resource relations, final Resource... inputs) throws IOException {
    final List<ClinicalFeature> features = new ArrayList<>();
    final ModelPool pool = new ModelPool();
    fillModelPool(pool, models);
    loadData(pool, features, inputs);
    removeEmptyFeatures(features);
    final Relations rels = loadRelations(pool, relations);
    return new NodeCreator(rels, features);
  }

  public static NodeBuilder loadCombined(final Resource json) throws IOException {
    return new JSONNodeCreator(new JSONReader(json.reader()).get());
  }

}
