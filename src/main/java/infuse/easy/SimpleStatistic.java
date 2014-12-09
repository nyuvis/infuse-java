package infuse.easy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SimpleStatistic implements Statistic {
  private final List<NodeFeature> fs;
  private final List<FoldNode> folds;
  private int types;
  private int selections;
  private int classificators;

  SimpleStatistic(final List<NodeFeature> fs, final List<FoldNode> folds) {
    this.fs = fs;
    this.folds = folds;
  }

  @Override
  public int countTypes() {
    if(types == 0) {
      final Set<String> t = new HashSet<>();
      for(final NodeFeature f : fs) {
        t.add(f.type());
      }
      types = t.size();
    }
    return types;
  }

  @Override
  public int countCombinedTypes() {
    return getCombinedTypes().size();
  }

  private List<String> ct;

  @Override
  public List<String> getCombinedTypes() {
    if(ct == null) {
      final Set<String> t = new HashSet<>();
      for(final NodeFeature f : fs) {
        t.add(f.combinedType());
      }
      final List<String> list = new ArrayList<>(t);
      Collections.sort(list);
      ct = Collections.unmodifiableList(list);
    }
    return ct;
  }

  @Override
  public int countFolds() {
    return folds.size();
  }

  @Override
  public int countFeatures() {
    return fs.size();
  }

  @Override
  public int countFeatureSelections() {
    if(selections == 0) {
      final Set<FeatureSelectionNode> t = new HashSet<>();
      for(final FoldNode f : folds) {
        for(final FeatureSelectionNode fsn : f.featureSelections()) {
          t.add(fsn);
        }
      }
      selections = t.size();
    }
    return selections;
  }

  @Override
  public int countClassificators() {
    if(classificators == 0) {
      final Set<ClassificatorNode> t = new HashSet<>();
      for(final FoldNode f : folds) {
        for(final FeatureSelectionNode fs : f.featureSelections()) {
          for(final ClassificatorNode c : fs.classificators()) {
            t.add(c);
          }
        }
      }
      classificators = t.size();
    }
    return classificators;
  }

  private int maxRank;

  @Override
  public int maxRank() {
    if(maxRank == 0) {
      int mr = 1;
      for(final FoldNode f : folds) {
        for(final FeatureSelectionNode fsn : f.featureSelections()) {
          for(final NodeFeature n : fs) {
            final int r = fsn.getRank(n);
            if(r < NodeFeature.UNRANKED && r > mr) {
              mr = r;
            }
          }
        }
      }
      maxRank = mr;
    }
    return maxRank;
  }

}
