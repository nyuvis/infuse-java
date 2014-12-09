package infuse;

import infuse.easy.DataInfo;
import infuse.easy.NodeFeature;
import infuse.glyphs.Glyph;
import infuse.glyphs.MatrixGlyph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jkanvas.util.PaintUtil;

public class RankCellRenderer extends JLabel implements ListCellRenderer<NodeFeature> {

  private final DataInfo di;
  private String space;
  private Glyph glyph;

  public RankCellRenderer(final DataInfo di, final Glyph glyph) {
    this.glyph = Objects.requireNonNull(glyph);
    this.di = Objects.requireNonNull(di);
  }

  public void setGlyph(final Glyph glyph) {
    this.glyph = Objects.requireNonNull(glyph);
    space = null;
  }

  public Glyph getGlyph() {
    return glyph;
  }

  @Override
  public Component getListCellRendererComponent(
      final JList<? extends NodeFeature> list,
      final NodeFeature value, final int index, final boolean isSelected,
      final boolean cellHasFocus) {
    this.isSelected = isSelected;
    f = value;
    if(space == null) {
      final Rectangle2D bbox = new Rectangle2D.Double();
      glyph.getBoundingBox(bbox, di);
      final char[] c = new char[(int) Math.ceil(bbox.getWidth() / bbox.getHeight() * 4.0) + 1];
      Arrays.fill(c, ' ');
      space = String.valueOf(c);
    }
    setText(space + value.toString());
    return this;
  }

  private boolean isSelected;

  private NodeFeature f;

  @Override
  public void paint(final Graphics gfx) {
    final Rectangle bbox = getBounds();
    setOpaque(true);
    setBackground(isSelected ? di.getSelectionColor() : Color.WHITE);
    setForeground(PaintUtil.getFontColor(getBackground()));
    final Graphics2D g2 = (Graphics2D) gfx.create();
    super.paint(g2);
    g2.dispose();
    final Graphics2D g = (Graphics2D) gfx.create();
    final Rectangle2D gb = new Rectangle2D.Double();
    glyph.getBoundingBox(gb, di);
    g.translate(g.getFontMetrics().charWidth(' ') * 0.5, 0);
    final double scale = bbox.getHeight() / gb.getHeight();
    g.scale(scale, scale);
    g.clip(gb);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    glyph.paint(g, 1, gb, di, f, isSelected);
    if(glyph == MatrixGlyph.INSTANCE) {
      g.setColor(getBackground());
      g.setStroke(new BasicStroke(2));
      g.draw(gb);
    }
    g.dispose();
  }

}
