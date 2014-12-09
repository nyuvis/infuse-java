package infuse.easy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SimpleFeatureSelectionNode implements FeatureSelectionNode {
  private final Map<Integer, Integer> ranks;
  private final List<ClassificatorNode> cns;
  private final String id;
  private final String name;
  private final String abbr;
  private final FoldNode foldNode;
  private Map<String, ClassificatorNode> clas;
  private List<ClassificatorNode> cache;

  SimpleFeatureSelectionNode(final Map<Integer, Integer> ranks,
      final List<ClassificatorNode> cns, final String name, final FoldNode foldNode) {
    this.ranks = ranks;
    this.cns = cns;
    this.name = name.substring(0, name.indexOf("_N"));
    abbr = abbreviate(this.name).substring(2);
    id = name;
    this.foldNode = foldNode;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String abbreviation() {
    return abbr;
  }

  @Override
  public int getRank(final NodeFeature f) {
    return ranks.get(f.id());
  }

  @Override
  public FoldNode getFold() {
    return foldNode;
  }

  @Override
  public Collection<ClassificatorNode> classificators() {
    if(cache == null) {
      final List<ClassificatorNode> c = new ArrayList<>();
      for(final ClassificatorNode n : cns) {
        if(n.getFeatureSelectionMethod().id().equals(id())) {
          c.add(n);
        }
      }
      cache = Collections.unmodifiableList(c);
    }
    return cache;
  }

  @Override
  public double getAUC(final String classification) {
    if(clas == null) {
      final Map<String, ClassificatorNode> map = new HashMap<>();
      for(final ClassificatorNode c : classificators()) {
        map.put(c.name(), c);
      }
      clas = map;
    }
    return clas.get(classification).getAUC();
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof FeatureSelectionNode)) return false;
    return name().equals(((FeatureSelectionNode) obj).name());
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }

  public static final String abbreviate(final String name) {
    final StringBuilder sb = new StringBuilder();
    for(final char c : name.toCharArray()) {
      if(Character.toLowerCase(c) != c) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
