package infuse.easy;

import infuse.ClinicalFeature;
import infuse.Model;
import infuse.io.Relations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NodeCreator implements NodeBuilder {

  private final Relations rel;
  private final List<ClinicalFeature> features;

  public NodeCreator(final Relations rel, final List<ClinicalFeature> features) {
    this.rel = Objects.requireNonNull(rel);
    this.features = Objects.requireNonNull(features);
  }

  private static NodeFeature getFeature(final ClinicalFeature f) {
    final String name = f.getRealName();
    final String type = f.getType();
    final String subtype = f.getSubtype();
    final int id = f.getId();
    return new SimpleNodeFeature(type, id, name, subtype);
  }

  private Collection<Model> getFoldModels() {
    final Set<Model> folds = new HashSet<>();
    for(final ClinicalFeature f : features) {
      for(final Model m : f.models()) {
        for(final Model p : rel.getParents(m)) {
          if(p.getId().startsWith("CrossValidation")) {
            folds.add(p);
          }
        }
      }
    }
    return folds;
  }

  private Collection<Model> getFeatureSelectionModels() {
    final Set<Model> fs = new HashSet<>();
    for(final ClinicalFeature f : features) {
      for(final Model m : f.models()) {
        if(!m.getId().startsWith("FeatureSelection")) throw new IllegalStateException(
            "" + m);
        fs.add(m);
      }
    }
    return fs;
  }

  private Collection<Model> getClassificationModels() {
    final Set<Model> classificators = new HashSet<>();
    for(final ClinicalFeature f : features) {
      getClassificationModels(classificators, f);
    }
    return classificators;
  }

  private void getClassificationModels(final Set<Model> res, final ClinicalFeature f) {
    for(final Model m : f.models()) {
      for(final Model c : rel.getChilds(m)) {
        if(c.getId().startsWith("Classification")) {
          res.add(c);
        }
      }
    }
  }

  private Collection<FoldNode> createFolds() {
    final Collection<Model> fsm = getFeatureSelectionModels();
    final Map<String, FeatureSelectionNode> fs = new HashMap<>(fsm.size());
    final Collection<Model> cm = getClassificationModels();
    final List<ClassificatorNode> cns = new ArrayList<>(cm.size());
    for(final Model c : cm) {
      final String name = c.getId();
      String par = null;
      for(final Model p : rel.getParents(c)) {
        if(fsm.contains(p)) {
          if(par != null) throw new IllegalStateException(p + " " + par);
          par = p.getId();
        }
      }
      Objects.requireNonNull(par);
      final double auc = c.getAreaUnderCurve();
      final ClassificatorNode cn = new SimpleClassificatorNode(name, auc, par, fs);
      cns.add(cn);
    }
    final Collection<Model> fm = getFoldModels();
    final List<FoldNode> res = new ArrayList<>();
    final Map<String, FoldNode> folds = new HashMap<>();
    for(final Model m : fsm) {
      String cv = null;
      for(final Model p : rel.getParents(m)) {
        if(fm.contains(p)) {
          final String label = rel.getLabel(p, m);
          if(cv != null) throw new IllegalStateException(label + " " + cv);
          cv = label;
        }
      }
      Objects.requireNonNull(cv);
      if(!folds.containsKey(cv)) {
        final FoldNode fn = new SimpleFoldNode(fs, cv, "Fold #" + folds.size());
        folds.put(cv, fn);
        res.add(fn);
      }
      final FoldNode foldNode = folds.get(cv);
      final String name = m.getId();
      final Map<Integer, Integer> ranks = new HashMap<>();
      for(final ClinicalFeature f : features) {
        if(f.hasRank(m)) {
          ranks.put(f.getId(), f.getRank(m));
        } else {
          ranks.put(f.getId(), NodeFeature.UNRANKED);
        }
      }
      final FeatureSelectionNode fsn = new SimpleFeatureSelectionNode(
          ranks, cns, name, foldNode);
      fs.put(name, fsn);
    }
    return res;
  }

  private List<NodeFeature> createFeatures() {
    final List<NodeFeature> res = new ArrayList<>(features.size());
    for(final ClinicalFeature cf : features) {
      res.add(getFeature(cf));
    }
    return res;
  }

  private List<NodeFeature> fs;

  @Override
  public List<NodeFeature> getFeatures() {
    if(fs == null) {
      fs = Collections.unmodifiableList(createFeatures());
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
