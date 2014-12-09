package infuse.easy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class SimpleFoldNode implements FoldNode {
  private final Map<String, FeatureSelectionNode> fs;
  private final String cv;
  private final String name;
  private List<FeatureSelectionNode> cache;

  SimpleFoldNode(final Map<String, FeatureSelectionNode> fs,
      final String cv, final String name) {
    this.fs = fs;
    this.cv = cv;
    this.name = name;
  }

  @Override
  public String id() {
    return cv;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<FeatureSelectionNode> featureSelections() {
    if(cache == null) {
      final List<FeatureSelectionNode> c = new ArrayList<>();
      for(final FeatureSelectionNode n : fs.values()) {
        if(n.getFold().equals(this)) {
          c.add(n);
        }
      }
      Collections.sort(c, new Comparator<FeatureSelectionNode>() {

        @Override
        public int compare(final FeatureSelectionNode f1, final FeatureSelectionNode f2) {
          return f1.name().compareTo(f2.name());
        }

      });
      cache = Collections.unmodifiableList(c);
    }
    return cache;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof FoldNode)) return false;
    return id().equals(((FoldNode) obj).id());
  }

  @Override
  public int hashCode() {
    return id().hashCode();
  }

}
