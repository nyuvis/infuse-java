package infuse;

import infuse.easy.DataInfo;
import infuse.easy.NodeFeature;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.groups.RenderGroup.RenderpassPosition;
import jkanvas.painter.groups.RenderpassLayout;
import jkanvas.painter.pod.AxisTitleRenderpass;
import jkanvas.painter.pod.CardPod;
import jkanvas.table.ColumnAggregation;
import jkanvas.table.DataTable;
import jkanvas.table.Feature;
import jkanvas.table.FeatureTable;

public class ScatterplotLayout extends RenderpassLayout<CardPod<NodeRenderpass>> {

  public static enum Axis {

    AVG_RANK("Average Rank") {

      @Override
      public double getValue(final NodeFeature f, final DataInfo di) {
        return f.meanRank(di) - 1;
      }

      @Override
      public double getMax(final Feature f) {
        return 1;
      }

      @Override
      public double getMin(final Feature f) {
        return f.aggregate(ColumnAggregation.MAXIMUM) + 1;
      }

    },

    FS_COUNT("Pick Count") {

      @Override
      public double getValue(final NodeFeature f, final DataInfo di) {
        return di.getModelCount() - f.appearsIn(di);
      }

      @Override
      public double getMax(final Feature f) {
        return f.aggregate(ColumnAggregation.MAXIMUM) + 1;
      }

      @Override
      public double getMin(final Feature f) {
        return 1;
      }

    },

    IMPORTANCE("Importance") {

      @Override
      public double getValue(final NodeFeature f, final DataInfo di) {
        return f.importance(di);
      }

      @Override
      public double getMax(final Feature f) {
        return 1;
      }

      @Override
      public double getMin(final Feature f) {
        return f.aggregate(ColumnAggregation.MAXIMUM);
      }

    },

    MAX_RANK("Best Rank") {

      @Override
      public double getMax(final Feature f) {
        return 1;
      }

      @Override
      public double getMin(final Feature f) {
        return f.aggregate(ColumnAggregation.MAXIMUM) + 1;
      }

      @Override
      public double getValue(final NodeFeature f, final DataInfo di) {
        return f.bestRank(di) - 1;
      }

    },

    MED_RANK("Median Rank") {

      @Override
      public double getValue(final NodeFeature f, final DataInfo di) {
        return f.medianRank(di) - 1;
      }

      @Override
      public double getMax(final Feature f) {
        return 1;
      }

      @Override
      public double getMin(final Feature f) {
        return f.aggregate(ColumnAggregation.MAXIMUM) + 1;
      }

    },

    DEV_RANK("Rank Std-Dev") {

      @Override
      public double getValue(final NodeFeature f, final DataInfo di) {
        return f.stdDevRank(di);
      }

      @Override
      public double getMax(final Feature f) {
        return 0;
      }

      @Override
      public double getMin(final Feature f) {
        return f.aggregate(ColumnAggregation.MAXIMUM);
      }

    },

    ; // EOD

    private final String name;

    private Axis(final String name) {
      this.name = name;
    }

    public abstract double getMax(final Feature f);

    public abstract double getMin(final Feature f);

    public abstract double getValue(NodeFeature f, DataInfo di);

    @Override
    public String toString() {
      return name;
    }

  } // Axis

  private final DataInfo di;

  private final AxisTitleRenderpass<?> xAxisKey;

  private final AxisTitleRenderpass<?> yAxisKey;

  public ScatterplotLayout(final DataInfo di, final AxisTitleRenderpass<?> xAxis,
      final AxisTitleRenderpass<?> yAxis) {
    xAxisKey = Objects.requireNonNull(xAxis);
    yAxisKey = Objects.requireNonNull(yAxis);
    this.di = Objects.requireNonNull(di);
  }

  private Axis xAxis = Axis.AVG_RANK;

  private Axis yAxis = Axis.FS_COUNT;

  public Axis getXAxis() {
    return xAxis;
  }

  public Axis getYAxis() {
    return yAxis;
  }

  public void setXAxis(final Axis xAxis) {
    this.xAxis = Objects.requireNonNull(xAxis);
  }

  public void setYAxis(final Axis yAxis) {
    this.yAxis = Objects.requireNonNull(yAxis);
  }

  private final double width = 6000.0;

  private final double height = 6000.0;

  @Override
  public void doLayout(
      final List<RenderpassPosition<CardPod<NodeRenderpass>>> members) {
    final double[] xAxis = new double[members.size()];
    final double[] yAxis = new double[members.size()];
    int i = 0;
    for(final RenderpassPosition<CardPod<NodeRenderpass>> m : members) {
      final NodeRenderpass nrp = m.pass.unwrap();
      final NodeFeature f = nrp.getFeature();
      if(f == null) {
        m.pass.setVisible(false);
        continue;
      }
      xAxis[i] = this.xAxis.getValue(f, di);
      yAxis[i] = this.yAxis.getValue(f, di);
      ++i;
    }
    final DataTable pos = new FeatureTable(new Feature[] {
        DataTable.fromArray(Arrays.copyOf(xAxis, i), "xAxis").getFeature(0),
        DataTable.fromArray(Arrays.copyOf(yAxis, i), "yAxis").getFeature(0)
    }).cached();
    xAxisKey.setValues(
        String.format("%4.0f", this.xAxis.getMin(pos.getFeature(0))).trim(),
        String.format("%4.0f", this.xAxis.getMax(pos.getFeature(0))).trim(),
        this.xAxis.toString().trim());
    yAxisKey.setValues(
        String.format("%4.0f", this.yAxis.getMin(pos.getFeature(1))).trim(),
        String.format("%4.0f", this.yAxis.getMax(pos.getFeature(1))).trim(),
        this.yAxis.toString().trim());
    int p = 0;
    for(final RenderpassPosition<CardPod<NodeRenderpass>> m : members) {
      final NodeRenderpass nrp = m.pass.unwrap();
      final NodeFeature f = nrp.getFeature();
      if(f == null) {
        continue;
      }
      m.startAnimationTo(new Point2D.Double(width - pos.getZeroMaxScaled(p, 0) * width,
          pos.getZeroMaxScaled(p, 1) * height), AnimationTiming.SMOOTH);
      ++p;
    }
  }

  @Override
  public void drawBackground(
      final Graphics2D g, final KanvasContext ctx, final Rectangle2D bbox,
      final List<RenderpassPosition<CardPod<NodeRenderpass>>> members) {
    final Rectangle2D maxBox = new Rectangle2D.Double();
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final RenderpassPosition<CardPod<NodeRenderpass>> m : members) {
      cur.setFrame(0, 0, 0, 0);
      m.pass.getBoundingBox(cur);
      RenderpassPainter.addToRect(maxBox, cur);
    }
    g.setColor(Color.LIGHT_GRAY);
    final int num = 20;
    for(int i = 0; i <= num; ++i) {
      final double x = i * (bbox.getWidth() - maxBox.getWidth()) / num
          + maxBox.getWidth() * 0.5;
      final double y = i * (bbox.getHeight() - maxBox.getHeight()) / num
          + maxBox.getHeight() * 0.5;
      g.setStroke(new BasicStroke((i % 5) == 0 ? (i % 10) == 0 ? 8 : 4 : 2));
      g.draw(new Line2D.Double(x, 0, x, bbox.getHeight()));
      g.draw(new Line2D.Double(0, y, bbox.getWidth(), y));
    }
  }

  @Override
  public boolean addBoundingBox(final RectangularShape bbox,
      final List<RenderpassPosition<CardPod<NodeRenderpass>>> members) {
    final Rectangle2D maxBox = new Rectangle2D.Double();
    final Rectangle2D cur = new Rectangle2D.Double();
    for(final RenderpassPosition<CardPod<NodeRenderpass>> m : members) {
      cur.setFrame(0, 0, 0, 0);
      m.pass.getBoundingBox(cur);
      RenderpassPainter.addToRect(maxBox, cur);
    }
    RenderpassPainter.addToRect(bbox, new Rectangle2D.Double(
        0, 0, width + maxBox.getWidth(), height + maxBox.getHeight()));
    return false;
  }

}
