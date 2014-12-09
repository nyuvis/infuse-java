package infuse.easy;

import java.util.List;

public interface NodeBuilder {

  List<NodeFeature> getFeatures();

  List<FoldNode> getFolds();

  Statistic getStatistics();

}
