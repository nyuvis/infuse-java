package infuse.glyphs;

import infuse.easy.DataInfo;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.FoldNode;
import infuse.easy.NodeFeature;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import jkanvas.util.ArrayUtil;
import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;

/**
 * Matrix glyph.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class MatrixGlyph implements Glyph {

  /** The instance. */
  public static final MatrixGlyph INSTANCE = new MatrixGlyph();

  /** Private constructor. */
  private MatrixGlyph() {
    // nothing to do
  }

  @Override
  public boolean showOutside() {
    return true;
  }

  /**
   * Getter.
   *
   * @param di The data info.
   * @return The folds in reverse order.
   */
  private static Iterable<FoldNode> getFolds(final DataInfo di) {
    return ArrayUtil.reverseList(di.getFolds());
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox, final DataInfo di) {
    final double s = cellSize();
    final double w = di.getFolds().size() * s;
    double h = 0;
    for(final FoldNode fn : getFolds(di)) {
      final double t = fn.featureSelections().size() * s;
      if(h < t) {
        h = t;
      }
    }
    bbox.setFrame(0, 0, w, h);
  }

  @Override
  public Shape selectionShape(final RectangularShape bbox,
      final double offX, final double offY) {
    return new Rectangle2D.Double(bbox.getX() + offX, bbox.getY() + offY,
        bbox.getWidth(), bbox.getHeight());
  }

  /**
   * Getter.
   *
   * @return The size of a cell.
   */
  private static double cellSize() {
    return 10;
  }

  /** The min color. */
  private static final Color FROM = Color.LIGHT_GRAY.brighter();
  /** The max color. */
  private static final Color TO = Color.DARK_GRAY;

  /**
   * Getter.
   *
   * @param rank The rank.
   * @param maxRank The maximum rank.
   * @return The color for the cell.
   */
  private static Color getColorForRank(final int rank, final int maxRank) {
    if(rank == NodeFeature.UNRANKED) return Color.WHITE;
    final double r = (double) rank / maxRank;
    return PaintUtil.interpolate(FROM, TO, 1 - r);
  }

  /**
   * Correctly rotated feature selection names.
   *
   * @param di The data info.
   * @return The correctly rotated feature selection names.
   */
  private static Iterable<String> getFS(final DataInfo di) {
    return ArrayUtil.rotate(ArrayUtil.reverseList(di.getFSAbbr()), 2);
  }

  /**
   * Getter.
   *
   * @param fn The node.
   * @return The correctly rotated feature selection nodes.
   */
  private static Iterable<FeatureSelectionNode> getFS(final FoldNode fn) {
    return ArrayUtil.rotate(ArrayUtil.reverseList(fn.featureSelections()), 2);
  }

  @Override
  public void paintExample(final Graphics2D g,
      final RectangularShape bbox, final DataInfo di) {
    final double s = cellSize();
    double x = 0;
    final Rectangle2D cur = new Rectangle2D.Double();
    for(int k = 0; k < di.getFolds().size(); ++k) {
      double y = 0;
      for(final String n : getFS(di)) {
        cur.setFrame(x, y, s, s);
        g.setColor(Color.WHITE);
        g.fill(cur);
        g.setColor(Color.BLACK);
        g.draw(cur);
        StringDrawer.drawInto(g, n + k, PaintUtil.addPadding(cur, -1));
        y += s;
      }
      if(y + s < bbox.getHeight()) {
        cur.setFrame(x, y + s, s, bbox.getHeight() - y - s);
        g.setColor(Color.LIGHT_GRAY);
        g.fill(cur);
      }
      x += s;
    }
  }

  @Override
  public void paint(final Graphics2D g, final double zoom, final RectangularShape rect,
      final DataInfo di, final NodeFeature f, final boolean isSelected) {
    final double s = cellSize();
    double x = 0;
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final FoldNode fn : getFolds(di)) {
      double y = 0;
      for(final FeatureSelectionNode fsn : getFS(fn)) {
        cur.setFrame(x, y, s, s);
        PaintUtil.drawShape(g, cur, zoom, Color.BLACK,
            getColorForRank(fsn.getRank(f), di.getMaxRank()));
        y += s;
      }
      if(y + s < rect.getHeight()) {
        cur.setFrame(x, y + s, s, rect.getHeight() - y - s);
        g.setColor(Color.LIGHT_GRAY);
        g.fill(cur);
      }
      x += s;
    }
    g.setColor(Color.BLACK);
    g.draw(rect);
  }

  @Override
  public FeatureSelectionNode pick(final Point2D pos,
      final RectangularShape bbox, final DataInfo di, final NodeFeature f) {
    final double s = cellSize();
    double x = 0;
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final FoldNode fn : getFolds(di)) {
      double y = 0;
      for(final FeatureSelectionNode fsn : getFS(fn)) {
        cur.setFrame(x, y, s, s);
        if(cur.contains(pos)) return fsn;
        y += s;
      }
      x += s;
    }
    return null;
  }

  @Override
  public String toString() {
    return "Matrix Glyph";
  }

}
