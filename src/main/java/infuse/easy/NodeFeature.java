package infuse.easy;

public interface NodeFeature {

  static int UNRANKED = Integer.MAX_VALUE;

  String readableName();

  String name();

  String type();

  String subtype();

  String combinedType();

  String searchName();

  int id();

  double importance(DataInfo di);

  double meanRank(DataInfo di);

  double medianRank(DataInfo di);

  double stdDevRank(DataInfo di);

  int bestRank(DataInfo di);

  int appearsIn(DataInfo di);

  int getRank(FeatureSelectionNode fs);

  boolean hasRank(FeatureSelectionNode fs);

}
