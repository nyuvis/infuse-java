package infuse.glyphs;

import infuse.easy.DataInfo;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.NodeFeature;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

import jkanvas.util.PaintUtil;

/**
 * A star glyph.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class StarGlyph implements Glyph {

  @Override
  public boolean showOutside() {
    return false;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox, final DataInfo di) {
    bbox.setFrame(0, 0, 80, 80);
  }

  @Override
  public Shape selectionShape(final RectangularShape bbox,
      final double offX, final double offY) {
    return new Ellipse2D.Double(bbox.getX() + offX, bbox.getY() + offY,
        bbox.getWidth(), bbox.getHeight());
  }

  /**
   * The radius for the given rank.
   * 
   * @param rank The rank.
   * @param maxRank The maximum rank.
   * @return The radius.
   */
  public double radius(final int rank, final int maxRank) {
    if(rank == NodeFeature.UNRANKED) return 1.0;
    return (rank - 1.0) / maxRank;
  }

  @Override
  public void paint(final Graphics2D g, final double zoom,
      final RectangularShape bbox, final DataInfo di,
      final NodeFeature feature, final boolean isSelected) {
    PaintUtil.addPaddingInplace(bbox, -1);
    final double maxRad = bbox.getWidth() * 0.5;
    final double extend = 360.0 / di.getSortedFeaturesSelections().size();
    double angle = 90;
    g.setColor(isSelected ? Color.RED : Color.BLACK);
    for(final FeatureSelectionNode fs : di.getSortedFeaturesSelections()) {
      final double rad = maxRad - maxRad * radius(fs.getRank(feature), di.getMaxRank());
      final double x = bbox.getCenterX() + Math.sin(Math.toRadians(angle)) * rad;
      final double y = bbox.getCenterY() + Math.cos(Math.toRadians(angle)) * rad;
      final Line2D line = new Line2D.Double(bbox.getCenterX(), bbox.getCenterY(), x, y);
      g.draw(line);
      angle += extend;
    }
  }

  @Override
  public void paintExample(final Graphics2D g,
      final RectangularShape bbox, final DataInfo di) {
    // TODO Auto-generated method stub

  }

  @Override
  public FeatureSelectionNode pick(final Point2D pos, final RectangularShape bbox,
      final DataInfo di, final NodeFeature feature) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return "Star Glyph";
  }

}
