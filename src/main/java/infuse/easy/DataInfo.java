package infuse.easy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jkanvas.painter.ColorKeyHUD;
import jkanvas.painter.TextHUD;
import jkanvas.util.PaintUtil;

public class DataInfo {

  private final List<FoldNode> folds;
  private final int maxRank;
  private final int modelCount;
  private final int fsCount;
  private final ColorKeyHUD ckHUD;
  private final Map<String, Color> subtype;

  public DataInfo(final List<FoldNode> folds, final Statistic s) {
    this.folds = Collections.unmodifiableList(folds);
    maxRank = s.maxRank();
    modelCount = s.countFeatureSelections() * s.countFolds();
    fsCount = s.countFeatureSelections();
    final int ctc = s.countCombinedTypes();
    subtype = new HashMap<>(ctc + 2);
    final Color[] p = {
        new Color(166, 206, 227), new Color(31, 120, 180), new Color(178, 223, 138),
        new Color(51, 160, 44), new Color(251, 154, 153), new Color(227, 26, 28),
        new Color(253, 191, 111), new Color(255, 127, 0), new Color(202, 178, 214),
        new Color(106, 61, 154), new Color(255, 255, 153)
    };
    final List<Color> colors = new ArrayList<>(p.length / 2);
    loop: for(int i = 0; i < p.length; ++i) {
      switch(i) {
        case 0:
        case 2:
        case 4:
        case 6:
        case 8:
          continue loop;
      }
      colors.add(p[i]);
    }
    ckHUD = new ColorKeyHUD(TextHUD.LEFT, TextHUD.BOTTOM);
    ckHUD.setIds("ckhud");
    subtype.put("###SELECT###", p[6]);
    subtype.put("###SECONDARY###", p[0]);
    final int ip = (int) Math.ceil((double) ctc / colors.size());
    int k = 0;
    int rounds = 0;
    for(final String type : s.getCombinedTypes()) {
      final Color color;
      if(k >= colors.size()) {
        k = 0;
        ++rounds;
      }
      final double r = (double) rounds / ip;
      if(rounds == 0) {
        color = colors.get(k);
      } else {
        color = PaintUtil.interpolate(colors.get(k),
            colors.get((k + 1) % colors.size()), r);
      }
      subtype.put(type, color);
      ckHUD.addKey(type, color);
      ++k;
    }
  }

  public List<FoldNode> getFolds() {
    return folds;
  }

  private List<FeatureSelectionNode> sorted;

  public List<FeatureSelectionNode> getSortedFeaturesSelections() {
    if(sorted == null) {
      final List<FeatureSelectionNode> fss = new ArrayList<>();
      for(final FoldNode fold : getFolds()) {
        fss.addAll(fold.featureSelections());
      }
      Collections.sort(fss, new Comparator<FeatureSelectionNode>() {

        @Override
        public int compare(final FeatureSelectionNode o1, final FeatureSelectionNode o2) {
          final int cmp = o1.name().compareTo(o2.name());
          if(cmp != 0) return cmp;
          return o1.getFold().name().compareTo(o2.getFold().name());
        }

      });
      sorted = Collections.unmodifiableList(fss);
    }
    return sorted;
  }

  private List<String> fsNames;

  private List<String> fsAbbr;

  public List<String> getFSNames() {
    if(fsNames == null) {
      final List<String> fss = new ArrayList<>();
      final List<String> fab = new ArrayList<>();
      for(final FoldNode fold : getFolds()) {
        for(final FeatureSelectionNode fs : fold.featureSelections()) {
          if(!fss.contains(fs.name())) {
            fss.add(fs.name());
            fab.add(fs.abbreviation());
          }
        }
      }
      fsNames = Collections.unmodifiableList(fss);
      fsAbbr = Collections.unmodifiableList(fab);
    }
    return fsNames;
  }

  public List<String> getFSAbbr() {
    if(fsAbbr == null) {
      getFSNames();
    }
    return fsAbbr;
  }

  private List<String> cNames;

  public List<String> getCNames() {
    if(cNames == null) {
      final List<String> css = new ArrayList<>();
      for(final FoldNode fold : getFolds()) {
        for(final FeatureSelectionNode fs : fold.featureSelections()) {
          for(final ClassificatorNode cs : fs.classificators()) {
            if(!css.contains(cs.name())) {
              css.add(cs.name());
            }
          }
        }
      }
      cNames = Collections.unmodifiableList(css);
    }
    return cNames;
  }

  public int getMaxRank() {
    return maxRank;
  }

  public int getModelCount() {
    return modelCount;
  }

  public int getFeatureSelectionCount() {
    return fsCount;
  }

  public Color getSelectionColor() {
    final String st = "###SELECT###";
    return subtype.get(st);
  }

  public Color getSecondaryColor() {
    final String st = "###SECONDARY###";
    return subtype.get(st);
  }

  public Color getColor(final NodeFeature nf) {
    return getColor(nf.combinedType());
  }

  public Color getColor(final String st) {
    return subtype.get(st);
  }

  public ColorKeyHUD getColorKeyHUD() {
    return ckHUD;
  }

}
