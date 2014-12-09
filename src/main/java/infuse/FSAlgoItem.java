package infuse;

import infuse.easy.ClassificatorNode;
import infuse.easy.DataInfo;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.FoldNode;
import infuse.easy.NodeFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FSAlgoItem {

  private final List<FeatureSelectionItem> items;
  private final String fs;
  private final String c;
  private final String sfs;
  private final String sc;
  private final double auc;
  private final double stddev;
  private final boolean manual;

  public FSAlgoItem(final DataInfo di, final List<NodeFeature> f,
      final String fs, final String c) {
    this.fs = fs;
    this.c = c;
    manual = false;
    double auc = 0;
    String sfs = null;
    String sc = null;
    final List<FeatureSelectionItem> init = new ArrayList<>();
    for(final FoldNode fold : di.getFolds()) {
      for(final FeatureSelectionNode fn : fold.featureSelections()) {
        if(!fn.name().equals(fs)) {
          continue;
        }
        if(sfs == null) {
          sfs = fn.abbreviation();
        }
        for(final ClassificatorNode cn : fn.classificators()) {
          if(!cn.name().equals(c)) {
            continue;
          }
          if(sc == null) {
            sc = cn.abbreviation();
          }
          auc += cn.getAUC();
          init.add(new FeatureSelectionItem(fn, f, cn));
        }
      }
    }
    this.auc = auc / init.size();
    double stddev = 0;
    for(final FeatureSelectionItem i : init) {
      stddev += (i.getAUC() - this.auc) * (i.getAUC() - this.auc);
    }
    this.stddev = Math.sqrt(stddev / init.size());
    this.sfs = sfs;
    this.sc = sc;
    items = Collections.unmodifiableList(init);
  }

  public FSAlgoItem(final DataInfo di, final List<NodeFeature> f,
      final String fs, final String c, final List<Double> aucs) {
    this.fs = fs;
    this.c = c;
    manual = true;
    double auc = 0;
    final String sfs = fs;
    final String sc = c;
    final List<ClassificatorNode> cnodes = new ArrayList<>();
    final List<FeatureSelectionItem> init = new ArrayList<>();
    for(final double d : aucs) {
      auc += d;
      final int id = cnodes.size();
      final FeatureSelectionNode fnode = new FeatureSelectionNode() {

        @Override
        public String name() {
          return fs;
        }

        @Override
        public String id() {
          return fs + id;
        }

        @Override
        public int getRank(final NodeFeature nf) {
          if(!f.contains(nf)) return NodeFeature.UNRANKED;
          return f.indexOf(nf);
        }

        @Override
        public FoldNode getFold() {
          return di.getFolds().get(id);
        }

        @Override
        public double getAUC(final String classification) {
          return d;
        }

        @Override
        public Collection<ClassificatorNode> classificators() {
          return cnodes;
        }

        @Override
        public String abbreviation() {
          return fs;
        }

      };
      final ClassificatorNode cnode = new ClassificatorNode() {

        @Override
        public String name() {
          return c;
        }

        @Override
        public String id() {
          return c + id;
        }

        @Override
        public FeatureSelectionNode getFeatureSelectionMethod() {
          return fnode;
        }

        @Override
        public double getAUC() {
          return d;
        }

        @Override
        public String abbreviation() {
          return c;
        }

      };
      cnodes.add(cnode);
      init.add(new FeatureSelectionItem(fnode, f, cnode));
    }
    this.auc = auc / init.size();
    double stddev = 0;
    for(final FeatureSelectionItem i : init) {
      stddev += (i.getAUC() - this.auc) * (i.getAUC() - this.auc);
    }
    this.stddev = Math.sqrt(stddev / init.size());
    this.sfs = sfs;
    this.sc = sc;
    items = Collections.unmodifiableList(init);
  }

  public List<FeatureSelectionItem> items() {
    return items;
  }

  public String getFSShortcut() {
    return sfs;
  }

  public String getFeatureSelection() {
    return fs;
  }

  public String getCShortcut() {
    return sc;
  }

  public String getClassification() {
    return c;
  }

  public double getAUC() {
    return auc;
  }

  public double getAUCStdDev() {
    return stddev;
  }

  public boolean isManual() {
    return manual;
  }

}
