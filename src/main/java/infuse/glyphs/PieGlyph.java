package infuse.glyphs;

import infuse.easy.DataInfo;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.NodeFeature;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Map;
import java.util.WeakHashMap;

import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;

/**
 * Two types of pie glyphs.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class PieGlyph implements Glyph {

  /** Whether the bars are growing inward. */
  private final boolean inward;

  /**
   * Creates a pie glyph.
   *
   * @param inward Whether the bars grow inward.
   */
  public PieGlyph(final boolean inward) {
    this.inward = inward;
  }

  @Override
  public boolean showOutside() {
    return false;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox, final DataInfo di) {
    bbox.setFrame(0, 0, 88, 88);
  }

  @Override
  public void paintExample(final Graphics2D g,
      final RectangularShape bbox, final DataInfo di) {
    final Rectangle2D full = PaintUtil.addPadding(bbox, -4);
    final Ellipse2D circle = new Ellipse2D.Double(
        full.getX(), full.getY(), full.getWidth(), full.getHeight());
    g.setColor(Color.WHITE);
    g.fill(circle);
    g.setColor(Color.BLACK);
    final double in = full.getWidth() * 0.25 / Math.sqrt(2.0);
    final double extend = 360.0 / di.getFSAbbr().size();
    double angle = 0;
    for(final String fs : di.getFSAbbr()) {
      final Shape a = new Arc2D.Double(full, angle - EPS, extend + 2 * EPS, Arc2D.PIE);
      g.draw(a);
      StringDrawer.drawInto(g, fs, PaintUtil.addPadding(a.getBounds2D(), -in));
      angle += extend;
    }
    g.draw(circle);
  }

  /**
   * The radius for the given rank.
   *
   * @param rank The rank.
   * @param maxRank The maximum rank.
   * @param rough Whether to only compute the rough radius.
   * @return The radius.
   */
  public double radius(final int rank, final int maxRank, final boolean rough) {
    if(rank == NodeFeature.UNRANKED) return 1.0;
    final double res = (rank - 1.0) / maxRank;
    if(rough) return Math.round(res * 20.0) / 20.0;
    return res;
  }

  /** A small number larger zero. */
  private static final double EPS = 1e-4;
  /** The shape cache. */
  private final Map<NodeFeature, Shape> shapeCache = new WeakHashMap<>();
  /** The shape cache for rough glyphs. */
  private final Map<NodeFeature, Shape> roughCache = new WeakHashMap<>();

  /**
   * Create area for the given segment.
   * 
   * @param bbox The bounding box of the glyph.
   * @param rad The radius of the segment.
   * @param maxRad The maximal radius.
   * @param angle The angle of the segment.
   * @param extend The extend of the angle.
   * @return The area.
   */
  private Area createAreaFor(final Rectangle2D bbox, final double rad,
      final double maxRad, final double angle, final double extend) {
    if(inward) {
      if(-maxRad + rad >= 0) return null;
      final Area a = new Area(new Arc2D.Double(
          bbox, angle - EPS, extend + 2 * EPS, Arc2D.PIE));
      if(rad > 0) {
        a.subtract(new Area(new Arc2D.Double(
            PaintUtil.addPadding(bbox, -maxRad + rad),
            angle - 2 * EPS, extend + 4 * EPS, Arc2D.PIE)));
      }
      return a;
    } else if(maxRad - rad <= 0) return null;
    final Arc2D a = new Arc2D.Double(PaintUtil.addPadding(bbox, -rad),
        angle - EPS, extend + 2 * EPS, Arc2D.PIE);
    return new Area(a);
  }

  @Override
  public void paint(final Graphics2D g, final double zoom,
      final RectangularShape bbox, final DataInfo di,
      final NodeFeature feature, final boolean isSelected) {
    final Rectangle2D full = PaintUtil.addPadding(bbox, -4);
    final boolean rough = zoom < 1;
    final Map<NodeFeature, Shape> cache = rough ? roughCache : shapeCache;
    Shape s = cache.get(feature);
    if(s == null) {
      final double maxRad = full.getWidth() * 0.5;
      final double extend = 360.0 / di.getSortedFeaturesSelections().size();
      double angle = 0;
      final Area all = new Area();
      for(final FeatureSelectionNode fs : di.getSortedFeaturesSelections()) {
        final double rad = maxRad * radius(fs.getRank(feature), di.getMaxRank(), rough);
        final Area a = createAreaFor(full, rad, maxRad, angle, extend);
        if(a != null) {
          all.add(a);
        }
        angle += extend;
      }
      cache.put(feature, all);
      s = all;
    }
    final Ellipse2D circle = new Ellipse2D.Double(
        full.getX(), full.getY(), full.getWidth(), full.getHeight());
    final Graphics2D g2 = (Graphics2D) g.create();
    if(!isSelected) {
      PaintUtil.setAlpha(g2, 0.3);
    }
    g2.setColor(di.getColor(feature));
    g2.fill(circle);
    g2.dispose();
    g.setColor(Color.BLACK);
    g.fill(s);
    if(isSelected) {
      g.setStroke(new BasicStroke(4));
    }
    g.draw(circle);
  }

  @Override
  public Shape selectionShape(final RectangularShape bbox,
      final double offX, final double offY) {
    final Rectangle2D full = PaintUtil.addPadding(bbox, -4);
    return new Ellipse2D.Double(
        full.getX() + offX, full.getY() + offY, full.getWidth(), full.getHeight());
  }

  @Override
  public FeatureSelectionNode pick(final Point2D pos, final RectangularShape bbox,
      final DataInfo di, final NodeFeature feature) {
    final Rectangle2D full = PaintUtil.addPadding(bbox, -4);
    final double maxRad = full.getWidth() * 0.5;
    final double extend = 360.0 / di.getSortedFeaturesSelections().size();
    double angle = 0;
    for(final FeatureSelectionNode fs : di.getSortedFeaturesSelections()) {
      final Area a = createAreaFor(full, 0, maxRad, angle, extend);
      if(a != null && a.contains(pos)) return fs;
      angle += extend;
    }
    return null;
  }

  @Override
  public String toString() {
    return (inward ? "Inward " : "") + "Pie Glyph";
  }

}
