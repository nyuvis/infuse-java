package infuse.easy;

import java.util.List;

public interface FoldNode {

  String id();

  String name();

  List<FeatureSelectionNode> featureSelections();

}
