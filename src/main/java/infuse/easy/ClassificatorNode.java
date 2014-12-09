package infuse.easy;

public interface ClassificatorNode {

  String id();

  String name();

  String abbreviation();

  double getAUC();

  FeatureSelectionNode getFeatureSelectionMethod();

}
