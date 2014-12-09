package infuse;

import infuse.easy.DataInfo;
import infuse.easy.NodeFeature;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.matrix.AnimatedMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.painter.pod.AbstractTitleRenderpass.Alignment;
import jkanvas.painter.pod.Renderpod;
import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;
import jkanvas.util.StringDrawer.Orientation;

public final class AUCMatrix extends AnimatedMatrix<FSAlgoItem> {

  private static final double AMPLIFY = 5;

  private static final double LEFT_COL = 1.75;

  private static final double MID_COL = 0.25;

  private static final double RIGHT_COL = 1;

  private final Map<String, Integer> colMap;

  private final Map<String, Integer> rowMap;

  private AUCMatrix(final FSAlgoItem m, final double height, final Animator a) {
    super(m, height * (LEFT_COL + MID_COL + RIGHT_COL), height,
        m.getFSShortcut(), m.getCShortcut());
    colMap = new HashMap<>();
    colMap.put(m.getClassification(), 0);
    rowMap = new HashMap<>();
    rowMap.put(m.getFeatureSelection(), 0);
    setAnimator(a);
  }

  private AnimationTiming timing = AnimationTiming.NO_ANIMATION;

  public void setTiming(final AnimationTiming timing) {
    this.timing = Objects.requireNonNull(timing);
  }

  public AnimationTiming getTiming() {
    return timing;
  }

  public void setAlgo(final FSAlgoItem item) {
    final String c = item.getClassification();
    if(!colMap.containsKey(c)) {
      final int col = cols();
      colMap.put(c, col);
      final ArrayList<List<FSAlgoItem>> l = new ArrayList<>();
      l.add(Arrays.asList(new FSAlgoItem[rows()]));
      addColumns(col, l, Arrays.asList(item.getCShortcut()),
          0, Arrays.asList(getWidth(0)), timing);
    }
    final String f = item.getFeatureSelection();
    if(!rowMap.containsKey(f)) {
      final int row = rows();
      rowMap.put(f, row);
      final ArrayList<List<FSAlgoItem>> l = new ArrayList<>();
      l.add(Arrays.asList(new FSAlgoItem[cols()]));
      addRows(row, l, Arrays.asList(item.getFSShortcut()),
          0, Arrays.asList(getHeight(0)), timing);
    }
    set(rowMap.get(f), colMap.get(c), item);
  }

  public void sortRows() {
    sortRows(new Comparator<Integer>() {

      @Override
      public int compare(final Integer r1, final Integer r2) {
        final double a1 = getAUCForRow(r1);
        final double a2 = getAUCForRow(r2);
        return -Double.compare(a1, a2);
      }

    });
  }

  public double getAUCForRow(final int row) {
    double auc = 0;
    int count = 0;
    for(int col = 0; col < cols(); ++col) {
      final FSAlgoItem item = get(row, col);
      if(item == null) {
        continue;
      }
      auc += item.getAUC();
      ++count;
    }
    return auc / count;
  }

  public int rowOf(final String r) {
    final String[] rn = getRowNames();
    for(int i = 0; i < rn.length; ++i) {
      if(r.equals(rn[i])) return i;
    }
    return -1;
  }

  public int columnOf(final String c) {
    final String[] cn = getColumnNames();
    for(int i = 0; i < cn.length; ++i) {
      if(c.equals(cn[i])) return i;
    }
    return -1;
  }

  public double getTotalAUC(final int col) {
    double auc = 0;
    int count = 0;
    for(int row = 0; row < rows(); ++row) {
      final FSAlgoItem item = get(row, col);
      if(item == null) {
        continue;
      }
      auc += item.getAUC();
      ++count;
    }
    return auc / count;
  }

  public double getTotalStdDev(final int col) {
    final double mean = getTotalAUC(col);
    double stddev = 0;
    int count = 0;
    for(int row = 0; row < rows(); ++row) {
      final FSAlgoItem item = get(row, col);
      if(item == null) {
        continue;
      }
      final double cur = item.getAUC() - mean;
      stddev += cur * cur;
      ++count;
    }
    return Math.sqrt(stddev / count);
  }

  public static Renderpod<MatrixRenderpass<AUCMatrix>> create(
      final DataInfo di, final List<NodeFeature> features,
      final Animator a, final double size, final FeatureList list) {
    AUCMatrix m = null;
    for(final String fs : di.getFSNames()) {
      for(final String cs : di.getCNames()) {
        final FSAlgoItem i = new FSAlgoItem(di, features, fs, cs);
        if(m == null) {
          m = new AUCMatrix(i, size, a);
        } else {
          m.setAlgo(i);
        }
      }
    }
    final CellRealizer<AUCMatrix> cd = new CellRealizer<AUCMatrix>() {

      @Override
      public void drawCell(final Graphics2D g, final KanvasContext ctx,
          final Rectangle2D rect, final AUCMatrix matrix,
          final int row, final int col, final boolean isSelected,
          final boolean hasSelection) {
        final FSAlgoItem item = matrix.get(row, col);
        final Color back = item == null ? Color.WHITE
            : item.isManual() ? di.getSecondaryColor() : Color.WHITE;
        g.setColor(back);
        g.fill(rect);
        final Color border = Color.BLACK;
        g.setColor(border);
        g.draw(rect);
        if(item == null) return;
        final double h = rect.getHeight();
        final double w = h;
        // leftmost square
        final Rectangle2D cur = new Rectangle2D.Double(
            rect.getX(), rect.getY(), w * LEFT_COL, h);
        final List<FeatureSelectionItem> items = item.items();
        final double fw = cur.getWidth() / items.size();
        int x = 0;
        final Line2D topBar = new Line2D.Double();
        final Rectangle2D bar = new Rectangle2D.Double();
        final FeatureSelectionItem fsi = list.getSelectedGroup();
        for(final FeatureSelectionItem i : items) {
          final double y = i.getAUC() * h;
          bar.setFrame(cur.getX() + x * fw, cur.getY() + h - y, fw, y);
          topBar.setLine(bar.getMinX() + 1, bar.getMinY(),
              bar.getMaxX() - 1, bar.getMinY());
          g.setColor(i.exactSameFeatureSelection(fsi) ? di.getSelectionColor()
              : i.sameFold(fsi) ? di.getSelectionColor().brighter() : Color.GRAY);
          g.fill(PaintUtil.addPadding(bar, -0.5));
          g.setColor(Color.BLACK);
          g.draw(topBar);
          // g.setColor(Color.WHITE);
          // g.draw(bar);
          ++x;
        }
        // middle square
        cur.setFrame(cur.getX() + cur.getWidth(), cur.getY(), w * MID_COL, h);
        final double span = item.getAUC() * h;
        bar.setFrame(cur.getX(), cur.getY() + h - span, cur.getWidth(), span);
        topBar.setLine(bar.getMinX() + 1, bar.getMinY(),
            bar.getMaxX() - 1, bar.getMinY());
        g.setColor(Color.DARK_GRAY);
        g.fill(PaintUtil.addPadding(bar, -0.5));
        g.setColor(Color.BLACK);
        g.draw(topBar);
        // g.setColor(Color.WHITE);
        // g.draw(bar);
        // g.setColor(border);
        // g.draw(cur);
        g.setColor(Color.LIGHT_GRAY);
        final double top = h - span - item.getAUCStdDev() * h * AMPLIFY;
        final double bottom = h - span + item.getAUCStdDev() * h * AMPLIFY;
        final Line2D line = new Line2D.Double(
            cur.getCenterX(), cur.getY() + top, cur.getCenterX(), cur.getY() + bottom);
        g.draw(line);
        final double side = cur.getWidth() / 5.0;
        line.setLine(line.getX1() - side, cur.getY() + top,
            line.getX2() + side, cur.getY() + top);
        g.draw(line);
        line.setLine(line.getX1(), cur.getY() + bottom, line.getX2(), cur.getY() + bottom);
        g.draw(line);
        // line below
        // g.setColor(Color.BLACK);
        // line.setLine(rect.getX(), rect.getY() + h,
        // rect.getX() + w * (LEFT_COL + MID_COL), rect.getY() + h);
        // g.draw(line);
        // right square
        cur.setFrame(cur.getX() + cur.getWidth(), cur.getY(), w * RIGHT_COL - 0.5, h);
        PaintUtil.addPaddingInplace(cur, -h * 0.1);
        g.setColor(PaintUtil.getFontColor(back));
        final String fmt = String.format("%.4f", item.getAUC()).trim();
        StringDrawer.drawInto(g, fmt.charAt(0) == '0' ? fmt.substring(1) : fmt, cur);
        cur.setFrame(rect.getX() + w * (LEFT_COL + MID_COL), rect.getY(),
            w * RIGHT_COL, h);
        g.setColor(border);
        g.draw(cur);
        g.draw(PaintUtil.addPadding(rect, -0.5));
      }

    };
    final MatrixRenderpass<AUCMatrix> mr = new MatrixRenderpass<AUCMatrix>(m, cd, a) {

      @Override
      public boolean click(final Camera cam, final Point2D pos, final MouseEvent e) {
        final MatrixPosition p = pick(pos);
        if(p == null) return false;
        final AUCMatrix auc = getMatrix();
        final FSAlgoItem item = auc.get(p);
        if(item == null) return false;
        final Rectangle2D bbox = new Rectangle2D.Double();
        auc.getBoundingBox(bbox, p.row, p.col);
        final List<FeatureSelectionItem> items = item.items();
        final double w = bbox.getHeight() * LEFT_COL / items.size();
        final int i = (int) Math.floor((pos.getX() - bbox.getX()) / w);
        if(i < 0 || i >= items.size()) return false;
        if(items.get(i).exactSameFeatureSelection(list.getSelectedGroup())) {
          list.clearSelection();
        } else {
          list.select(items.get(i));
        }
        return true;
      }

      @Override
      public String getTooltip(final Point2D pos) {
        final MatrixPosition p = pick(pos);
        if(p == null) return null;
        final AUCMatrix auc = getMatrix();
        final FSAlgoItem item = auc.get(p);
        if(item == null) return null;
        final Rectangle2D bbox = new Rectangle2D.Double();
        auc.getBoundingBox(bbox, p.row, p.col);
        final List<FeatureSelectionItem> items = item.items();
        final double w = bbox.getHeight() * LEFT_COL / items.size();
        final int i = (int) Math.floor((pos.getX() - bbox.getX()) / w);
        final String main = item.getFeatureSelection() + " - " + item.getClassification();
        if(i < 0 || i >= items.size()) return main + " (" + item.getAUC() + ")";
        final FeatureSelectionItem fsi = items.get(i);
        return fsi.getFold().name() + " " + main + " (" + fsi.getAUC() + ")";
      }

    };
    return MatrixRenderpass.createTitledMatrixRenderpass(mr, 10, 5,
        Orientation.HORIZONTAL, Alignment.CENTER,
        Orientation.VERTICAL, Alignment.CENTER);
  }
}
