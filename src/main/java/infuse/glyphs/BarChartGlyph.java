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

/**
 * Bar chart glyph.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class BarChartGlyph implements Glyph {

  /** The instance. */
  public static final BarChartGlyph INSTANCE = new BarChartGlyph();

  /** Private constructor. */
  private BarChartGlyph() {
    // nothing to do
  }

  @Override
  public boolean showOutside() {
    return false;
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
    int num = 0;
    for(final FoldNode fn : getFolds(di)) {
      num += fn.featureSelections().size();
    }
    bbox.setFrame(0, 0, num * barWidth(), height());
  }

  @Override
  public Shape selectionShape(final RectangularShape bbox,
      final double offX, final double offY) {
    return new Rectangle2D.Double(bbox.getX() + offX, bbox.getY() + offY,
        bbox.getWidth(), bbox.getHeight());
  }

  private static double barWidth() {
    return 5;
  }

  private static double height() {
    return barWidth() * 8;
  }

  @Override
  public void paintExample(final Graphics2D g,
      final RectangularShape bbox, final DataInfo di) {
    // TODO
  }

  @Override
  public void paint(final Graphics2D g, final double zoom, final RectangularShape rect,
      final DataInfo di, final NodeFeature f, final boolean isSelected) {
    final double h = height();
    final double w = barWidth();
    double x = 0;
    final Graphics2D g2 = (Graphics2D) g.create();
    if(!isSelected) {
      PaintUtil.setAlpha(g2, 0.3);
    }
    g2.setColor(di.getColor(f));
    g2.fill(rect);
    g2.dispose();
    g.setColor(Color.BLACK);
    g.draw(rect);
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final FeatureSelectionNode fs : di.getSortedFeaturesSelections()) {
      final double y = h * fs.getRank(f) / di.getMaxRank();
      cur.setFrame(x, y, w, h - y);
      g.fill(cur);
      g.draw(cur);
      x += w;
    }
  }

  @Override
  public FeatureSelectionNode pick(final Point2D pos,
      final RectangularShape bbox, final DataInfo di, final NodeFeature f) {
    final double h = height();
    final double w = barWidth();
    double x = 0;
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final FeatureSelectionNode fs : di.getSortedFeaturesSelections()) {
      cur.setFrame(x, 0, w, h);
      if(cur.contains(pos)) return fs;
      x += w;
    }
    return null;
  }

  @Override
  public String toString() {
    return "Bar Chart Glyph";
  }

}
