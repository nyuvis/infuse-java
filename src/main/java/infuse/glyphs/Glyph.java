package infuse.glyphs;

import infuse.easy.DataInfo;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.NodeFeature;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

/**
 * A glyph.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Glyph {

  /**
   * Getter.
   *
   * @return Whether to show the outside pods of the glyph.
   */
  boolean showOutside();

  /**
   * The shape that can be selected.
   *
   * @param bbox The bounding box of the glyph.
   * @param offX The x offset.
   * @param offY The y offset.
   * @return The shape that can be selected.
   */
  Shape selectionShape(RectangularShape bbox, double offX, double offY);

  /**
   * Computes the bounding box of the glyph.
   *
   * @param bbox The rectangle to store the bounding box.
   * @param di The data info.
   */
  void getBoundingBox(RectangularShape bbox, DataInfo di);

  /**
   * Paints the example glyph.
   *
   * @param g The graphics context.
   * @param bbox The bounding box of the glyph.
   * @param di The data info.
   */
  void paintExample(Graphics2D g, RectangularShape bbox, DataInfo di);

  /**
   * Paints the glyph.
   *
   * @param g The graphics context.
   * @param zoom The zoom factor.
   * @param bbox The bounding box.
   * @param di The data info.
   * @param feature The feature node represented by the glyph.
   * @param isSelected Whether the feature is selected.
   */
  void paint(Graphics2D g, double zoom, RectangularShape bbox,
      DataInfo di, NodeFeature feature, boolean isSelected);

  /**
   * Picks a feature selection node.
   * 
   * @param pos The position.
   * @param bbox The glyph bounding box.
   * @param di The data info.
   * @param feature The feature.
   * @return The feature selection node at the given position or
   *         <code>null</code>.
   */
  FeatureSelectionNode pick(Point2D pos, RectangularShape bbox,
      DataInfo di, NodeFeature feature);

}
