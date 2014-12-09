package infuse;

import infuse.easy.ClassificatorNode;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.FoldNode;
import infuse.easy.NodeFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureSelectionItem {

  private final List<NodeFeature> features;

  private final ClassificatorNode c;

  public FeatureSelectionItem(final FeatureSelectionNode fs,
      final List<NodeFeature> f, final ClassificatorNode c) {
    features = new ArrayList<>();
    for(final NodeFeature nf : f) {
      if(nf.hasRank(fs)) {
        features.add(nf);
      }
    }
    this.c = c;
  }

  protected ClassificatorNode getClassificator() {
    return c;
  }

  public FoldNode getFold() {
    return c.getFeatureSelectionMethod().getFold();
  }

  public FeatureSelectionNode getFeatureSelection() {
    return c.getFeatureSelectionMethod();
  }

  public double getAUC() {
    return c.getAUC();
  }

  public List<NodeFeature> getFeatures() {
    return Collections.unmodifiableList(features);
  }

  public boolean sameFold(final FeatureSelectionItem item) {
    if(item == null) return false;
    return getFold().equals(item.getFold());
  }

  public boolean exactSameFeatureSelection(final FeatureSelectionItem item) {
    if(item == null) return false;
    return getFeatureSelection().id().equals(item.getFeatureSelection().id());
  }

}
