package infuse;

import static infuse.io.ClinicalLoader.*;
import infuse.ScatterplotLayout.Axis;
import infuse.easy.DataInfo;
import infuse.easy.NodeBuilder;
import infuse.easy.NodeFeature;
import infuse.easy.NodeToJSON;
import infuse.easy.SimpleAnonymizer;
import infuse.easy.Statistic;
import infuse.glyphs.BarChartGlyph;
import infuse.glyphs.Glyph;
import infuse.glyphs.MatrixGlyph;
import infuse.glyphs.PieGlyph;
import infuse.glyphs.StarGlyph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.CanvasSetup;
import jkanvas.DefaultMessageHandler;
import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.SimpleRefreshManager;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.io.json.JSONElement;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.painter.CachedRenderpass;
import jkanvas.painter.HUDRenderpass;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.groups.RenderGroup;
import jkanvas.painter.groups.RenderpassLayout;
import jkanvas.painter.pod.AbstractTitleRenderpass.Position;
import jkanvas.painter.pod.AxisTitleRenderpass;
import jkanvas.painter.pod.CardGroupLayout;
import jkanvas.painter.pod.CardPod;
import jkanvas.painter.pod.Renderpod;
import jkanvas.selection.AbstractSelector;
import jkanvas.selection.LassoSelection;
import jkanvas.selection.Selectable;
import jkanvas.util.PaintUtil;
import jkanvas.util.Resource;
import jkanvas.util.Stopwatch;
import jkanvas.util.StringDrawer.Orientation;

public class PlaygroundMain {

  public static boolean SHOW_GLYPH_SELECTION = true;

  public static boolean SHOW_GLYPH_KEY = false;

  public static boolean WIDE_GRID = false;

  public static int COLUMN_COUNT = 55;

  public static int SQUARE_COLUMN_COUNT = 47;

  public static boolean NEVER_SHOW_BORDER = false;

  public static boolean VIDEO_SIZE = false;

  public static final double AXIS_SPACE = 10;

  public static final double AXIS_TEXT_HEIGHT = 200;

  public static boolean SHOW_EVALUATE_BUTTON = false;

  protected static Canvas cbr;

  public static void main(final String[] args) throws IOException {
    // Canvas.DISABLE_CACHING = true;
    CachedRenderpass.CACHE_SIZE = 128;
    CachedRenderpass.CACHE_VISIBLE = 64;
    final Stopwatch total = new Stopwatch();
    final Stopwatch stop = new Stopwatch();
    final NodeBuilder nc;
    int pos = 0;
    if(args.length == 0) {
      nc = loadCombined(Resource.getFor("data/example.json"));
    } else if(args.length == 1) {
      usageAndExit();
      return;
    } else {
      switch(args[pos++]) {
        case "-json": {
          nc = loadCombined(Resource.getFor(args[pos++]));
          break;
        }
        case "-old": {
          if(args.length <= 3) {
            usageAndExit();
            return;
          }
          final File folder = new File(args[pos++]);
          if(!folder.exists() || !folder.isDirectory()) {
            System.err.println(folder + " does not exist or is not a directory");
            usageAndExit();
          }
          final String prefix = args[pos++] + "-";
          final String pf = prefix + "features";
          final File models = new File(folder, prefix + "models.json");
          System.out.println("model file: \"" + models + "\"");
          final File graph = new File(folder, prefix + "graph.dot");
          System.out.println("graph file: \"" + graph + "\"");
          final File[] files = folder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
              return name.startsWith(pf) && name.endsWith(".json");
            }

          });
          System.out.println("input files: [");
          final Resource[] input = new Resource[files.length];
          for(int i = 0; i < files.length; ++i) {
            System.out.println("  \"" + files[i] + "\",");
            input[i] = Resource.getFor(files[i]);
          }
          System.out.println("]");
          nc = loadAll(Resource.getFor(models), Resource.getFor(graph), input);
          break;
        }
        default:
          usageAndExit();
          return;
      }
    }
    final List<NodeFeature> fs = nc.getFeatures();
    stop.status("loading all: ", System.out);
    final Statistic s = nc.getStatistics();
    final DataInfo di = new DataInfo(nc.getFolds(), s);
    stop.status("creating simple nodes: ", System.out);
    if(args.length - pos >= 2) {
      if(args[pos++].equals("-anon")) {
        final String out = args[pos++];
        final JSONElement outJSON = new NodeToJSON(di, fs, new SimpleAnonymizer()).createJSON();
        try (final Writer w = new OutputStreamWriter(new FileOutputStream(out), "UTF-8")) {
          w.append(outJSON.toString());
          System.out.println("successfully written to \"" + out + "\"");
        }
        stop.status("dumping: ", System.out);
      } else {
        usageAndExit();
        return;
      }
    }
    if(pos < args.length) {
      usageAndExit();
      return;
    }
    final RefreshManager mng = new SimpleRefreshManager();
    final AtomicBoolean unwrap = new AtomicBoolean(true);
    final AnimatedPainter ap = new AnimatedPainter() {

      @Override
      public void getBoundingBox(final RectangularShape bbox) {
        super.getBoundingBox(bbox);
        if(unwrap.get()) {
          final double m = AXIS_SPACE + AXIS_TEXT_HEIGHT;
          bbox.setFrame(bbox.getX() + m, bbox.getY(),
              bbox.getWidth() - m, bbox.getHeight() - m);
        }
      }

    };
    mng.addRefreshable(ap);
    ap.addHUDPass(di.getColorKeyHUD());
    final FeatureList list = new FeatureList(mng);
    final RenderGroup<CardPod<NodeRenderpass>> cp =
        NodeRenderpass.createPlayground(ap, list, fs, di);
    final RankCellRenderer rcr = new RankCellRenderer(di, NodeRenderpass.INITIAL);
    list.setCellRenderer(rcr);
    stop.status("creating playground: ", System.out);
    final Canvas c = new Canvas(ap, true, 700, 700);
    final AbstractSelector lasso = new LassoSelection(
        c, di.getSelectionColor(), 0.6, di.getSelectionColor().darker(), 0.3) {

      @Override
      public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
        return e.isShiftDown() && SwingUtilities.isLeftMouseButton(e);
      }

    };
    lasso.addSelectable(new Selectable() {

      @Override
      public void select(final Shape selection, final boolean preview) {
        list.clearSelection();
        final Area sel = new Area(selection);
        final Rectangle2D rect = new Rectangle2D.Double();
        for(int i = 0; i < cp.renderpassCount(); ++i) {
          final CardPod<NodeRenderpass> pod = cp.getRenderpass(i);
          final NodeRenderpass nr = pod.unwrap();
          RenderpassPainter.getPassBoundingBox(rect, pod);
          if(!selection.intersects(rect)) {
            continue;
          }
          pod.getInnerBoundingBox(rect);
          rect.setFrame(rect.getX() + pod.getOffsetX(), rect.getY() + pod.getOffsetY(),
              rect.getWidth(), rect.getHeight());
          if(!selection.intersects(rect)) {
            continue;
          }
          final Area a = new Area(
              nr.getSelectionShape(pod.unwrapOffsetX(), pod.unwrapOffsetY()));
          a.intersect(sel);
          nr.setSelected(!a.isEmpty());
        }
      }

      @Override
      public Renderpass getRenderpass() {
        return cp;
      }

    });
    ap.addHUDPass(lasso);
    final JFrame f = new JFrame("INFUSE");
    final SimpleTextHUD info = CanvasSetup.setupCanvas(
        f, c, ap, true, true, false, true, false);
    c.getCamera().move(AXIS_TEXT_HEIGHT + AXIS_SPACE, 0);
    final JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    top.add(new JButton(new AbstractAction("Sort by name") {

      @Override
      public void actionPerformed(final ActionEvent e) {
        list.sortByName();
        list.scrollToTop();
        c.grabFocus();
      }

    }));
    top.add(new JButton(new AbstractAction("Sort by selection") {

      @Override
      public void actionPerformed(final ActionEvent e) {
        list.sortBySelection(di);
        list.scrollToTop();
        c.grabFocus();
      }

    }));
    final AxisTitleRenderpass<RenderGroup<CardPod<NodeRenderpass>>> xAxisKey = new AxisTitleRenderpass<>(
        cp, AXIS_TEXT_HEIGHT, AXIS_SPACE, "", "", "");
    xAxisKey.setActive(false);
    xAxisKey.setPosition(Position.BELOW);
    final AxisTitleRenderpass<RenderGroup<CardPod<NodeRenderpass>>> yAxisKey = new AxisTitleRenderpass<>(
        xAxisKey, AXIS_TEXT_HEIGHT, AXIS_SPACE, "", "", "");
    yAxisKey.setPosition(Position.LEFT);
    yAxisKey.setOrientation(Orientation.VERTICAL);
    yAxisKey.setActive(false);
    ap.addPass(yAxisKey);
    final ScatterplotLayout spl = new ScatterplotLayout(di, xAxisKey, yAxisKey);
    final JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(new JButton(new AbstractAction("Switch Layout") {

      private RenderpassLayout<CardPod<NodeRenderpass>> oldLayout;

      @Override
      public void actionPerformed(final ActionEvent e) {
        if(oldLayout != null) {
          cp.setLayout(oldLayout);
          oldLayout = null;
          xAxisKey.setActive(false);
          yAxisKey.setActive(false);
          unwrap.set(true);
        } else {
          oldLayout = cp.getLayout();
          if(oldLayout instanceof CardGroupLayout) {
            ((CardGroupLayout<NodeRenderpass>) oldLayout).setTiming(AnimationTiming.SMOOTH);
          }
          unwrap.set(false);
          xAxisKey.setActive(true);
          yAxisKey.setActive(true);
          cp.setLayout(spl);
        }
        c.reset(AnimationTiming.SMOOTH, new AnimationAction() {

          @Override
          public void animationFinished() {
            c.reset(AnimationTiming.SMOOTH, null);
          }

        });
        c.grabFocus();
      }

    }));
    final JComboBox<Axis> xAxis = new JComboBox<>(Axis.values());
    xAxis.setSelectedItem(spl.getXAxis());
    bottom.add(xAxis);
    final JComboBox<Axis> yAxis = new JComboBox<>(Axis.values());
    yAxis.setSelectedItem(spl.getYAxis());
    bottom.add(yAxis);
    final ItemListener il = new ItemListener() {

      @Override
      public void itemStateChanged(final ItemEvent e) {
        spl.setXAxis(xAxis.getItemAt(xAxis.getSelectedIndex()));
        spl.setYAxis(yAxis.getItemAt(yAxis.getSelectedIndex()));
        cp.invalidate();
        c.refresh();
        c.grabFocus();
      }

    };
    xAxis.addItemListener(il);
    yAxis.addItemListener(il);
    if(SHOW_GLYPH_SELECTION) {
      final JComboBox<Glyph> cb = new JComboBox<>(new Glyph[] {
          MatrixGlyph.INSTANCE, new PieGlyph(false),
          NodeRenderpass.INITIAL, new StarGlyph(), BarChartGlyph.INSTANCE});
      cb.setSelectedItem(NodeRenderpass.INITIAL);
      cb.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(final ItemEvent e) {
          final Glyph glyph = cb.getItemAt(cb.getSelectedIndex());
          NodeRenderpass.setGlyph(cp, glyph);
          rcr.setGlyph(glyph);
          f.repaint();
          c.grabFocus();
        }

      });
      bottom.add(cb);
    }
    final JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.add(c);
    left.add(bottom);
    final JPanel upper = new JPanel();
    upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
    upper.add(top);
    upper.add(new FilterBox(list));
    upper.add(list.getListComponent(VIDEO_SIZE ? 400 : 400, VIDEO_SIZE ? 500 : 500));
    final AnimatedPainter brp = new AnimatedPainter() {

      @Override
      public boolean doubleClick(final Camera cam, final Point2D p, final MouseEvent e) {
        cbr.resetToWidth();
        return true;
      }

    };
    mng.addRefreshable(brp);
    final Renderpod<MatrixRenderpass<AUCMatrix>> br = AUCMatrix.create(
        di, fs, brp, 40, list);
    cbr = new Canvas(brp, VIDEO_SIZE ? 400 : 250, VIDEO_SIZE ? 150 : 100);
    cbr.setAnimator(brp);
    brp.addRefreshable(cbr);
    cbr.setMargin(1.5);
    cbr.setBackground(Color.WHITE);
    brp.addPass(br);
    cbr.setUserZoomable(false);
    cbr.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(final ComponentEvent e) {
        cbr.resetToWidth();
      }

    });
    c.setMessageHandler(new DefaultMessageHandler(f) {

      @Override
      protected List<JComponent> photoComponents(
          final JFrame frame, final Canvas canvas, final boolean window) {
        return Arrays.asList(frame.getRootPane(), c, cbr);
      }

      @Override
      protected String photoPrefix(final int i) {
        return super.photoPrefix(i) + (i == 0 ? "-all" : i == 1 ? "-main" : "-class");
      }

    });
    cbr.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        br.unwrap().getMatrix().sortRows();
        cbr.resetToWidth();
      }

    }, 0);
    bottom.add(new JButton(new AbstractAction("Reset View") {

      @Override
      public void actionPerformed(final ActionEvent e) {
        c.reset(AnimationTiming.SMOOTH, null);
      }

    }));
    if(SHOW_EVALUATE_BUTTON) {
      bottom.add(new JButton(new AbstractAction("Evaluate Model") {

        @Override
        public void actionPerformed(final ActionEvent e) {
          aucForSelection(fs, di, list, br);
        }

      }));
    }
    c.addAction(KeyEvent.VK_S, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        aucForSelection(fs, di, list, br);
      }

    });
    info.addLine("S: Save current feature set");
    c.addAction(KeyEvent.VK_B, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        Canvas.DEBUG_BBOX = !Canvas.DEBUG_BBOX;
        Canvas.DISABLE_CACHING = true;
        mng.refreshAll();
        c.scheduleAction(new AnimationAction() {

          @Override
          public void animationFinished() {
            Canvas.DISABLE_CACHING = false;
          }

        }, 0);
      }

    });
    info.addLine("B: Toggle bounding box debug mode");
    if(SHOW_GLYPH_KEY) {
      ap.addHUDPass(new HUDRenderpass() {

        {
          setIds("ckhud");
        }

        @Override
        public void drawHUD(final Graphics2D g, final KanvasContext ctx) {
          final Glyph glyph = rcr.getGlyph();
          final Rectangle2D bbox = new Rectangle2D.Double();
          glyph.getBoundingBox(bbox, di);
          final double pad = 2.5;
          final RoundRectangle2D rect = PaintUtil.toRoundRectangle(bbox, pad);
          g.scale(2, 2);
          g.translate(pad, pad);
          g.setColor(Color.BLACK);
          final Graphics2D g2 = (Graphics2D) g.create();
          PaintUtil.setAlpha(g2, 0.5);
          g2.fill(rect);
          g2.dispose();
          g.clip(bbox);
          glyph.paintExample(g, bbox, di);
        }

      });
    }
    c.addMessageAction(KeyEvent.VK_K, "ckhud#visible:toggle");
    info.addLine("K: Toggle color key");
    final JSplitPane vert = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, cbr);
    final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, vert);
    f.setLayout(new BorderLayout());
    f.add(split, BorderLayout.CENTER);
    f.pack();
    bottom.setMaximumSize(bottom.getSize());
    f.setLocationRelativeTo(null);
    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    f.setVisible(true);
    c.postMessage("info#visible:false");
    c.postMessage("ckhud#visible:false");
    c.grabFocus();
    total.status("total time: ", System.out);
    System.out.println("folds: " + s.countFolds());
    System.out.println("feature selection models: " + s.countFeatureSelections());
    System.out.println("classification models: " + s.countClassificators());
    System.out.println("features: " + s.countFeatures());
    System.out.println("types: " + s.countTypes());
    System.out.println("subtypes: " + s.countCombinedTypes());
    System.out.println("max rank: " + s.maxRank());
  }

  private static void usageAndExit() {
    System.err.println("Usage: [(-json <file> | -old <path> <prefix>)] [-anon <outputJSON>]");
    System.exit(1);
  }

  private static int count = 0;

  static void aucForSelection(final List<NodeFeature> fs, final DataInfo di,
      final FeatureList list, final Renderpod<MatrixRenderpass<AUCMatrix>> br) {
    final List<NodeFeature> sel = new ArrayList<>();
    for(final NodeFeature f : fs) {
      if(list.isSelected(f)) {
        sel.add(f);
      }
    }
    final String setName = "S" + (++count);
    final AUCMatrix m = br.unwrap().getMatrix();
    final Random r = new Random();
    int col = 0;
    for(final String c : di.getCNames()) {
      final double stddev = m.getTotalStdDev(col);
      final double mean = m.getTotalAUC(col) + stddev * 1.1;
      final Double[] aucs = new Double[di.getFolds().size()];
      for(int f = 0; f < aucs.length; ++f) {
        aucs[f] = mean + r.nextGaussian() * stddev * 0.9;
      }
      m.setAlgo(new FSAlgoItem(di, sel, setName, c, Arrays.asList(aucs)));
      ++col;
    }
    m.sortRows();
    cbr.resetToWidth();
  }

}
