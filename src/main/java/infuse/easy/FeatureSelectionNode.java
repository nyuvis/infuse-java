package infuse.easy;

import java.util.Collection;

public interface FeatureSelectionNode {

  String id();

  String name();

  String abbreviation();

  Collection<ClassificatorNode> classificators();

  double getAUC(String classification);

  FoldNode getFold();

  int getRank(NodeFeature f);

}
