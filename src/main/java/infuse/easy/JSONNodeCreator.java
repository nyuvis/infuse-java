package infuse.easy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import jkanvas.io.json.JSONElement;

public final class JSONNodeCreator implements NodeBuilder {

  private static final class ClassificationEntry {

    private final JSONElement e;

    public ClassificationEntry(final JSONElement e) {
      this.e = e;
    }

    public double getAUC() {
      return e.getDouble("auc");
    }

    public String getFeatureSelection() {
      return e.getString("fs");
    }

    public String getClassifier() {
      return e.getString("classifier");
    }

    public int getFold() {
      return e.getInt("fold");
    }

  } // ClassificationEntry

  private static class FeatureFeatureSelectionFold {

    public final String fs;
    public final int fold;
    public final String f;

    public FeatureFeatureSelectionFold(final String f, final String fs, final int fold) {
      this.f = f;
      this.fs = fs;
      this.fold = fold;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + fold;
      result = prime * result + fs.hashCode();
      result = prime * result + f.hashCode();
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if(this == obj) return true;
      if(!(obj instanceof FeatureFeatureSelectionFold)) return false;
      final FeatureFeatureSelectionFold other = (FeatureFeatureSelectionFold) obj;
      if(fold != other.fold) return false;
      if(!f.equals(other.f)) return false;
      return fs.equals(other.fs);
    }

  } // FeatureSelectionFold

  private final List<NodeFeature> features;
  private final Map<String, Integer> featureIds;
  private final Map<String, Integer> featureSelectionIndex;
  private final Map<String, FeatureSelectionNode> featureSelection;
  private final Map<FeatureFeatureSelectionFold, Integer> ranks;
  private final Map<Integer, FoldNode> foldMap;
  private final Map<Integer, List<FeatureSelectionNode>> foldBackMap;

  private final List<ClassificatorNode> classification;

  public JSONNodeCreator(final JSONElement json) {
    featureIds = new HashMap<>();
    featureSelectionIndex = new HashMap<>();
    featureSelection = new HashMap<>();
    foldBackMap = new HashMap<>();
    features = new ArrayList<>();
    ranks = new HashMap<>();
    foldMap = new HashMap<>();
    for(final JSONElement e : json.getValue("features")) {
      final NodeFeature feature = getFeature(e);
      features.add(feature);
      for(final JSONElement r : e.getValue("ranks")) {
        final FeatureFeatureSelectionFold ffsf = new FeatureFeatureSelectionFold(
            feature.name(), r.getString("fs"), r.getInt("fold"));
        final JSONElement rank = r.getValue("rank");
        if(rank.isNull()) {
          ranks.put(ffsf, NodeFeature.UNRANKED);
        } else {
          ranks.put(ffsf, r.getInt("rank"));
        }
        if(!foldMap.containsKey(ffsf.fold)) {
          final int foldId = ffsf.fold;
          final List<FeatureSelectionNode> fsns = new ArrayList<>();
          foldBackMap.put(foldId, fsns);
          foldMap.put(ffsf.fold, new FoldNode() {

            @Override
            public String id() {
              return "" + foldId;
            }

            @Override
            public String name() {
              return "Fold " + foldId;
            }

            @Override
            public List<FeatureSelectionNode> featureSelections() {
              return fsns;
            }

          });
        }
      }
    }
    classification = new ArrayList<>();
    for(final JSONElement e : json.getValue("classification")) {
      classification.add(createClassificatorNode(new ClassificationEntry(e)));
    }
    int ix = 0;
    for(final JSONElement v : json.getValue("featureSelection")) {
      featureSelectionIndex.put(v.string(), ix);
      ++ix;
    }
    for(final Entry<String, Integer> e : featureSelectionIndex.entrySet()) {
      final String name = e.getKey();
      final String firstAbbr = SimpleFeatureSelectionNode.abbreviate(name);
      final String abbr = firstAbbr.startsWith("FS") ? firstAbbr.substring(2) : firstAbbr;
      for(final Entry<Integer, FoldNode> fold : foldMap.entrySet()) {
        final int foldN = fold.getKey();
        final String id = name + "_" + foldN;
        final FoldNode foldNode = fold.getValue();
        final Map<Integer, Integer> rank = new HashMap<>();
        for(final Entry<FeatureFeatureSelectionFold, Integer> r : ranks.entrySet()) {
          final FeatureFeatureSelectionFold ffsf = r.getKey();
          if(ffsf.fold != foldN) {
            continue;
          }
          if(!ffsf.fs.equals(name)) {
            continue;
          }
          rank.put(getFeatureID(ffsf), r.getValue());
        }
        final List<ClassificatorNode> classes = new ArrayList<>();
        final FeatureSelectionNode fsn = new FeatureSelectionNode() {

          @Override
          public String name() {
            return name;
          }

          @Override
          public String id() {
            return id;
          }

          @Override
          public int getRank(final NodeFeature f) {
            return rank.get(f.id());
          }

          @Override
          public FoldNode getFold() {
            return foldNode;
          }

          private final Map<String, Double> auc = new HashMap<>();

          @Override
          public double getAUC(final String classification) {
            if(auc.isEmpty()) {
              for(final ClassificatorNode cn : classificators()) {
                auc.put(cn.id(), cn.getAUC());
              }
            }
            return auc.get(classification);
          }

          @Override
          public Collection<ClassificatorNode> classificators() {
            return classes;
          }

          @Override
          public String abbreviation() {
            return abbr;
          }

        };
        foldBackMap.get(foldN).add(fsn);
        featureSelection.put(id, fsn);
        // fill classificator list after adding feature selection node
        for(final ClassificatorNode cn : classification) {
          final FeatureSelectionNode ofsn = cn.getFeatureSelectionMethod();
          if(ofsn == fsn) {
            classes.add(cn);
          }
        }
      }
    }
  }

  private Collection<FoldNode> createFolds() {
    final Set<FoldNode> res = new TreeSet<>(new Comparator<FoldNode>() {

      @Override
      public int compare(final FoldNode fn1, final FoldNode fn2) {
        return fn1.id().compareTo(fn2.id());
      }

    });
    for(final FeatureSelectionNode fs : featureSelection.values()) {
      res.add(fs.getFold());
    }
    return res;
  }

  private NodeFeature getFeature(final JSONElement f) {
    return new SimpleNodeFeature(f.getString("type"), getFeatureID(f),
        f.getString("name"), f.getString("subtype"));
  }

  private ClassificatorNode createClassificatorNode(final ClassificationEntry ce) {
    final String name = ce.getClassifier();
    final int fold = ce.getFold();
    final String abbr = name.substring("Classification".length());
    final String id = name + "_" + ce.getFeatureSelection() + "_" + ce.getFold();
    final String fs = ce.getFeatureSelection() + "_" + fold;
    final Map<String, FeatureSelectionNode> fsn = featureSelection;
    final double auc = ce.getAUC();
    return new ClassificatorNode() {

      @Override
      public String name() {
        return name;
      }

      @Override
      public String id() {
        return id;
      }

      @Override
      public FeatureSelectionNode getFeatureSelectionMethod() {
        return fsn.get(fs);
      }

      @Override
      public double getAUC() {
        return auc;
      }

      @Override
      public String abbreviation() {
        return abbr;
      }

    };
  }

  private int getFeatureID(final JSONElement f) {
    final String name = f.getString("name");
    if(!featureIds.containsKey(name)) {
      featureIds.put(name, featureIds.size());
    }
    return featureIds.get(name);
  }

  private int getFeatureID(final FeatureFeatureSelectionFold ffsf) {
    final String name = ffsf.f;
    if(!featureIds.containsKey(name)) {
      featureIds.put(name, featureIds.size());
    }
    return featureIds.get(name);
  }

  private List<NodeFeature> fs;

  @Override
  public List<NodeFeature> getFeatures() {
    if(fs == null) {
      fs = Collections.unmodifiableList(features);
    }
    return fs;
  }

  private List<FoldNode> folds;

  @Override
  public List<FoldNode> getFolds() {
    if(folds == null) {
      final List<FoldNode> list = new ArrayList<>(createFolds());
      Collections.sort(list, new Comparator<FoldNode>() {

        @Override
        public int compare(final FoldNode o1, final FoldNode o2) {
          return o1.name().compareTo(o2.name());
        }

      });
      folds = Collections.unmodifiableList(list);
    }
    return folds;
  }

  private Statistic statistics;

  @Override
  public Statistic getStatistics() {
    if(statistics == null) {
      final List<NodeFeature> fs = getFeatures();
      final List<FoldNode> folds = getFolds();
      statistics = new SimpleStatistic(fs, folds);
    }
    return statistics;
  }

}
