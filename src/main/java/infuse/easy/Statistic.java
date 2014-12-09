package infuse.easy;

import java.util.List;

public interface Statistic {

  int countFolds();

  int countFeatureSelections();

  int countClassificators();

  int countTypes();

  int countCombinedTypes();

  int countFeatures();

  int maxRank();

  List<String> getCombinedTypes();

}
