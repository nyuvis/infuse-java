package infuse;

import infuse.easy.DataInfo;
import infuse.easy.FeatureSelectionNode;
import infuse.easy.NodeFeature;
import infuse.glyphs.Glyph;
import infuse.glyphs.PieGlyph;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedPainter;
import jkanvas.painter.CachedRenderpass;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.groups.RenderGroup;
import jkanvas.painter.pod.AbstractTitleRenderpass.Position;
import jkanvas.painter.pod.CardGroupLayout;
import jkanvas.painter.pod.CardPod;
import jkanvas.painter.pod.Renderpod;
import jkanvas.painter.pod.TitleRenderpass;

public class NodeRenderpass extends CachedRenderpass {

  public static final Glyph INITIAL = new PieGlyph(true);

  private final NodeFeature f;
  private final DataInfo di;
  private final FeatureList list;
  private Glyph glyph;

  public NodeRenderpass() {
    list = null;
    di = null;
    f = null;
    glyph = null;
  }

  public NodeRenderpass(final FeatureList list, final DataInfo di, final NodeFeature f) {
    this.list = Objects.requireNonNull(list);
    this.di = Objects.requireNonNull(di);
    this.f = Objects.requireNonNull(f);
    list.addFeature(f);
    glyph = INITIAL;
  }

  public NodeFeature getFeature() {
    return f;
  }

  public boolean isSelected() {
    if(f == null) return false;
    return list.isSelected(f);
  }

  public void setSelected(final boolean selected) {
    if(f == null) return;
    change();
    list.setSelected(f, selected);
  }

  public double importance() {
    if(f == null) return -1;
    return f.importance(di);
  }

  public void setGlyph(final Glyph glyph) {
    if(f == null) return;
    change();
    this.glyph = Objects.requireNonNull(glyph);
  }

  public Glyph getGlyph() {
    return glyph;
  }

  public Shape getSelectionShape(final double offX, final double offY) {
    final Rectangle2D bbox = new Rectangle2D.Double();
    glyph.getBoundingBox(bbox, di);
    return glyph.selectionShape(bbox, offX, offY);
  }

  /**
   * Picks the feature selection node at the given position.
   * 
   * @param pos The position.
   * @return The feature selection node at the given position or
   *         <code>null</code>.
   */
  public FeatureSelectionNode pick(final Point2D pos) {
    if(f == null) return null;
    final Rectangle2D bbox = new Rectangle2D.Double();
    getBoundingBox(bbox);
    return glyph.pick(pos, bbox, di, f);
  }

  @Override
  public String getTooltip(final Point2D p) {
    if(f == null) return null;
    final StringBuilder sb = new StringBuilder();
    sb.append("<html><b>");
    sb.append(f.combinedType());
    sb.append(" - ");
    sb.append(f.readableName());
    sb.append("</b><br>");
    sb.append("average rank: ");
    sb.append(f.meanRank(di));
    sb.append("<br>");
    sb.append("pick count: ");
    sb.append(f.appearsIn(di));
    sb.append("<br>");
    sb.append("importance: ");
    sb.append(f.importance(di));
    sb.append("<br>");
    sb.append("best rank: ");
    sb.append(f.bestRank(di));
    sb.append("<br>");
    sb.append("median rank: ");
    sb.append(f.medianRank(di));
    sb.append("<br>");
    sb.append("rank standard devitaon: ");
    sb.append(f.stdDevRank(di));
    sb.append("<br>");
    final FeatureSelectionNode fsn = pick(p);
    if(fsn != null) {
      sb.append("<b>model: ");
      sb.append(fsn.name() + " " + fsn.getFold().name());
      sb.append("<br>");
      if(fsn.getRank(f) != NodeFeature.UNRANKED) {
        sb.append("rank: ");
        sb.append(fsn.getRank(f));
      } else {
        sb.append("unranked");
      }
      sb.append("</b>");
    }
    return sb.toString();
  }

  private boolean lastSelected;

  @Override
  public void beforeDraw() {
    final boolean sel = isSelected();
    if(lastSelected != sel) {
      change();
      lastSelected = sel;
    }
  }

  @Override
  public void doDraw(final Graphics2D g, final KanvasContext ctx) {
    if(f == null) return;
    final Rectangle2D rect = new Rectangle2D.Double();
    getBoundingBox(rect);
    glyph.paint(g, ctx.toComponentLength(1), rect, di, f, isSelected());
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    if(f == null) {
      bbox.setFrame(0, 0, 100, 0);
      return;
    }
    glyph.getBoundingBox(bbox, di);
  }

  public static final void setGlyph(
      final RenderGroup<CardPod<NodeRenderpass>> group, final Glyph g) {
    for(int i = 0; i < group.renderpassCount(); ++i) {
      final CardPod<NodeRenderpass> rp = group.getRenderpass(i);
      rp.unwrap().setGlyph(g);
    }
  }

  public static final RenderGroup<CardPod<NodeRenderpass>> createPlayground(
      final AnimatedPainter ap, final FeatureList list,
      final List<NodeFeature> features, final DataInfo di) {
    final Set<String> knownTypes = new HashSet<>();
    final RenderGroup<CardPod<NodeRenderpass>> res = new RenderGroup<CardPod<NodeRenderpass>>(
        ap) {

      @Override
      public boolean doubleClick(
          final Camera cam, final Point2D position, final MouseEvent e) {
        final Rectangle2D bbox = new Rectangle2D.Double();
        for(int i = renderpassCount() - 1; i >= 0; --i) {
          final Renderpass r = getRenderpass(i);
          if(!r.isVisible()) {
            continue;
          }
          r.getBoundingBox(bbox);
          final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
          if(!bbox.contains(pos)) {
            continue;
          }
          if(r.doubleClick(cam, pos, e)) return true;
        }
        return ap.doubleClick(cam,
            new Point2D.Double(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), e);
      }

    };
    res.setLayout(new CardGroupLayout<NodeRenderpass>() {

      {
        if(PlaygroundMain.WIDE_GRID) {
          setColumnCount(PlaygroundMain.COLUMN_COUNT);
        } else {
          // setColumnCount((int) Math.ceil(Math.sqrt(features.size())) + 4);
          setColumnCount(PlaygroundMain.SQUARE_COLUMN_COUNT);
          // setColumnCount(PlaygroundMain.VIDEO_SIZE ? 36 : 40);
        }
      }

      @Override
      protected Comparator<NodeRenderpass> order() {
        return new Comparator<NodeRenderpass>() {

          @Override
          public int compare(final NodeRenderpass nrp1, final NodeRenderpass nrp2) {
            final double s1 = nrp1.importance();
            final double s2 = nrp2.importance();
            return Double.compare(s1, s2);
          }

        };
      }

    });
    for(final NodeFeature nf : features) {
      final NodeRenderpass npr = new NodeRenderpass(list, di, nf);
      final TitleRenderpass<NodeRenderpass> trp = new TitleRenderpass<>(npr, 20, 3);
      trp.setTitle(nf.name());
      final TitleRenderpass<NodeRenderpass> types = new TitleRenderpass<>(trp, 10, 3);
      types.setPosition(Position.BELOW);
      final String type = nf.type();
      types.setTitle(type + " - " + nf.subtype());
      final CardPod<NodeRenderpass> pod = new CardPod<NodeRenderpass>(types) {

        @Override
        public boolean isSubtle() {
          return !unwrap().isSelected();
        }

        @Override
        public boolean doubleClick(
            final Camera cam, final Point2D position, final MouseEvent e) {
          final NodeRenderpass n = unwrap();
          if(!isActive() && !n.getSelectionShape(
              n.getOffsetX(), n.getOffsetY()).contains(position)) return false;
          return super.doubleClick(cam, position, e);
        }

        @Override
        public boolean click(final Camera cam, final Point2D position, final MouseEvent e) {
          final NodeRenderpass n = unwrap();
          if(!isActive() && !n.getSelectionShape(
              n.getOffsetX(), n.getOffsetY()).contains(position)) return false;
          n.setSelected(!n.isSelected());
          return true;
        }

        @Override
        public boolean isActive() {
          final boolean active = !PlaygroundMain.NEVER_SHOW_BORDER
              && npr.getGlyph().showOutside();
          if(active != super.isActive()) {
            Renderpod<?> pod = this;
            do {
              pod.setActive(active);
              pod = pod.getChildPod();
            } while(pod != null);
          }
          return super.isActive();
        }

      };
      pod.setFadeBorder(true);
      pod.setSubtle(true);
      pod.setColor(di.getColor(nf));
      pod.setGroup(type);
      pod.setHitAll(true);
      if(!knownTypes.contains(type)) {
        final TitleRenderpass<NodeRenderpass> t =
            new TitleRenderpass<>(new NodeRenderpass(), 20, 0);
        t.setTitle(type);
        final CardPod<NodeRenderpass> p = new CardPod<>(t);
        p.setGroup(type);
        res.addRenderpass(p);
        knownTypes.add(type);
      }
      res.addRenderpass(pod);
    }
    list.sortByName();
    return res;
  }

  private boolean changeFlag;

  protected void change() {
    changeFlag = true;
  }

  @Override
  public boolean isChanging() {
    if(!changeFlag) return false;
    changeFlag = false;
    return true;
  }

}
