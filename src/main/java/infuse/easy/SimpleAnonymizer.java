package infuse.easy;

import java.util.HashMap;
import java.util.Map;

public class SimpleAnonymizer implements Anonymizer {

  // private final Map<String, String> fsMap = new HashMap<>();

  @Override
  public String fsName(final String fs) {
    // if(!fsMap.containsKey(fs)) {
    // fsMap.put(fs, "fs#" + fsMap.size());
    // }
    // return fsMap.get(fs);
    return fs;
  }

  // private final Map<ClassificatorNode, String> cMap = new HashMap<>();

  @Override
  public String cName(final ClassificatorNode c) {
    // if(!cMap.containsKey(c)) {
    // cMap.put(c, "c#" + cMap.size());
    // }
    // return cMap.get(c);
    return c.name();
  }

  private final Map<FoldNode, String> foldMap = new HashMap<>();

  @Override
  public String fold(final FoldNode f) {
    if(!foldMap.containsKey(f)) {
      foldMap.put(f, "" + foldMap.size());
    }
    return foldMap.get(f);
  }

  // private final Map<String, String> typeMap = new HashMap<>();

  @Override
  public String type(final String type) {
    // if(!typeMap.containsKey(type)) {
    // typeMap.put(type, "type#" + typeMap.size());
    // }
    // return typeMap.get(type);
    return type;
  }

  // private final Map<String, String> subtypeMap = new HashMap<>();

  @Override
  public String subtype(final String subtype) {
    // if(!subtypeMap.containsKey(subtype)) {
    // subtypeMap.put(subtype, "subtype#" + subtypeMap.size());
    // }
    // return subtypeMap.get(subtype);
    return subtype;
  }

  private final Map<NodeFeature, String> featureMap = new HashMap<>();

  @Override
  public String name(final NodeFeature f) {
    if(!featureMap.containsKey(f)) {
      featureMap.put(f, "feature#" + featureMap.size());
    }
    return featureMap.get(f);
  }

  @Override
  public String auc(final double auc) {
    return String.format("%.4f", auc).trim();
  }

  @Override
  public String rank(final int rank) {
    return "" + rank;
  }

}
