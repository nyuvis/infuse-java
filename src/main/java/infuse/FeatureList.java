package infuse;

import infuse.easy.DataInfo;
import infuse.easy.NodeFeature;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jkanvas.RefreshManager;

public class FeatureList extends AbstractListModel<NodeFeature> implements
ListSelectionModel {

  private JList<NodeFeature> list;

  private JScrollPane scroll;

  private final RefreshManager rm;

  public FeatureList(final RefreshManager rm) {
    this.rm = Objects.requireNonNull(rm);
  }

  public JScrollPane getListComponent(final int width, final int height) {
    if(scroll != null) return scroll;
    list = new JList<>(this);
    list.setSelectionModel(this);
    list.setLayoutOrientation(JList.VERTICAL);
    list.setVisibleRowCount(-1);
    if(lcr != null) {
      list.setCellRenderer(lcr);
    }
    scroll = new JScrollPane(list,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.setPreferredSize(new Dimension(width, height));
    return scroll;
  }

  @Override
  public NodeFeature getElementAt(final int index) {
    return features.get(index);
  }

  @Override
  public int getSize() {
    return features.size();
  }

  private final List<NodeFeature> features = new ArrayList<>();

  public List<NodeFeature> getFeatures() {
    return Collections.unmodifiableList(features);
  }

  public void addFeature(final NodeFeature feature) {
    if(features.contains(Objects.requireNonNull(feature))) return;
    final int index = features.size();
    features.add(feature);
    fireIntervalAdded(this, index, index);
  }

  public void removeFeature(final NodeFeature feature) {
    final int i = features.indexOf(feature);
    if(i < 0) return;
    features.remove(feature);
    selected.remove(feature);
    fireIntervalRemoved(this, i, i);
    fsi = null;
    rm.refreshAll();
  }

  private final Set<NodeFeature> selected = new HashSet<>();

  public int setSelected(final NodeFeature feature, final boolean isSelected) {
    fsi = null;
    if(isSelected) {
      selected.add(feature);
    } else {
      selected.remove(feature);
    }
    rm.refreshAll();
    final int i = features.indexOf(feature);
    if(!rm.inBulkOperation()) {
      for(final ListSelectionListener l : lsls) {
        l.valueChanged(new ListSelectionEvent(this, i, i, isAdjusting));
      }
    }
    return i;
  }

  public boolean isSelected(final NodeFeature feature) {
    return selected.contains(feature);
  }

  private FeatureSelectionItem fsi;

  public void select(final FeatureSelectionItem item) {
    setValueIsAdjusting(true);
    rm.startBulkOperation();
    clearSelection();
    int min = features.size() - 1;
    int max = 0;
    for(final NodeFeature f : item.getFeatures()) {
      final int i = setSelected(f, true);
      if(i < 0) {
        System.err.println(f + " not in list");
      } else {
        if(i < min) {
          min = i;
        }
        if(i > min) {
          max = i;
        }
      }
    }
    setValueIsAdjusting(false);
    for(final ListSelectionListener l : lsls) {
      l.valueChanged(new ListSelectionEvent(this, min, max, isAdjusting));
    }
    rm.endBulkOperation();
    fsi = item;
  }

  public FeatureSelectionItem getSelectedGroup() {
    return fsi;
  }

  private final List<ListSelectionListener> lsls = new ArrayList<>();

  private int anchor;

  private int lead;

  private boolean gestureStarted;

  @Override
  public void addListSelectionListener(final ListSelectionListener lsl) {
    lsls.add(lsl);
  }

  @Override
  public void removeListSelectionListener(final ListSelectionListener lsl) {
    lsls.remove(lsl);
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    rm.startBulkOperation();
    for(int i = index0; i <= index1; ++i) {
      setSelected(getElementAt(i), true);
    }
    rm.endBulkOperation();
    if(index0 <= index1) {
      anchor = index0;
      lead = index1;
      for(final ListSelectionListener l : lsls) {
        l.valueChanged(new ListSelectionEvent(this, index0, index1, isAdjusting));
      }
    }
  }

  @Override
  public void clearSelection() {
    fsi = null;
    final int min = getMinSelectionIndex();
    final int max = getMaxSelectionIndex();
    selected.clear();
    rm.refreshAll();
    for(final ListSelectionListener l : lsls) {
      l.valueChanged(new ListSelectionEvent(this, min, max, isAdjusting));
    }
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
    if(!gestureStarted) {
      rm.startBulkOperation();
      if(isSelectedIndex(index0)) {
        removeSelectionInterval(index0, index1);
      } else {
        addSelectionInterval(index0, index1);
      }
      rm.endBulkOperation();
    }
    gestureStarted = true;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    rm.startBulkOperation();
    for(int i = index0; i <= index1; ++i) {
      setSelected(getElementAt(i), false);
    }
    rm.endBulkOperation();
    if(index0 <= index1) {
      for(final ListSelectionListener l : lsls) {
        l.valueChanged(new ListSelectionEvent(this, index0, index1, isAdjusting));
      }
    }
  }

  public AutoCloseable startBulkOperation() {
    final RefreshManager rm = this.rm;
    rm.startBulkOperation();
    return new AutoCloseable() {

      private RefreshManager prm = rm;

      @Override
      public void close() throws Exception {
        if(prm != null) {
          prm.endBulkOperation();
        }
        prm = null;
      }

    };
  }

  @Override
  public void setAnchorSelectionIndex(final int index) {
    anchor = index;
  }

  @Override
  public void setLeadSelectionIndex(final int index) {
    lead = index;
  }

  @Override
  public void insertIndexInterval(final int index, final int length, final boolean before) {
    // nothing to do
  }

  @Override
  public void removeIndexInterval(final int index0, final int index1) {
    // nothing to do
  }

  @Override
  public boolean isSelectionEmpty() {
    return selected.isEmpty();
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    return selected.contains(getElementAt(index));
  }

  @Override
  public int getAnchorSelectionIndex() {
    return anchor;
  }

  @Override
  public int getLeadSelectionIndex() {
    return lead;
  }

  @Override
  public int getMaxSelectionIndex() {
    if(selected.isEmpty()) return -1;
    for(int i = getSize() - 1; i >= 0; --i) {
      if(isSelectedIndex(i)) return i;
    }
    return -1;
  }

  @Override
  public int getMinSelectionIndex() {
    if(selected.isEmpty()) return -1;
    for(int i = 0; i < getSize(); ++i) {
      if(isSelectedIndex(i)) return i;
    }
    return -1;
  }

  @Override
  public int getSelectionMode() {
    return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
  }

  @Override
  public void setSelectionMode(final int selectionMode) {
    // ignore
  }

  private boolean isAdjusting;

  private ListCellRenderer<NodeFeature> lcr;

  @Override
  public void setValueIsAdjusting(final boolean valueIsAdjusting) {
    isAdjusting = valueIsAdjusting;
    if(!isAdjusting) {
      gestureStarted = false;
    }
  }

  @Override
  public boolean getValueIsAdjusting() {
    return isAdjusting;
  }

  public void setCellRenderer(final ListCellRenderer<NodeFeature> lcr) {
    this.lcr = lcr;
    if(list != null) {
      list.setCellRenderer(lcr);
    }
  }

  public void sortByName() {
    Collections.sort(features, new Comparator<NodeFeature>() {

      @Override
      public int compare(final NodeFeature f1, final NodeFeature f2) {
        final int cmp = f1.combinedType().compareTo(f2.combinedType());
        if(cmp != 0) return cmp;
        return f1.toString().compareTo(f2.toString());
      }

    });
    fireContentsChanged(this, 0, features.size() - 1);
  }

  public void sortBySelection(final DataInfo di) {
    Collections.sort(features, new Comparator<NodeFeature>() {

      @Override
      public int compare(final NodeFeature f1, final NodeFeature f2) {
        if(isSelected(f1) != isSelected(f2)) return isSelected(f2) ? 1 : -1;
        final double d1 = f1.importance(di);
        final double d2 = f2.importance(di);
        return Double.compare(d1, d2);
      }

    });
    fireContentsChanged(this, 0, features.size() - 1);
  }

  public void sortBySelection(final String[] terms) {
    Collections.sort(features, new Comparator<NodeFeature>() {

      private double getScore(final String text) {
        double score = 0.0;
        for(final String t : terms) {
          int pos = 0;
          while((pos = text.indexOf(t, pos)) >= 0) {
            score += 1.0 / (pos + 1.0);
            ++pos;
          }
        }
        return score;
      }

      @Override
      public int compare(final NodeFeature f1, final NodeFeature f2) {
        if(isSelected(f1) != isSelected(f2)) return isSelected(f2) ? 1 : -1;
        final String text1 = f1.searchName().toLowerCase();
        final String text2 = f2.searchName().toLowerCase();
        final double score1 = getScore(text1);
        final double score2 = getScore(text2);
        final int cmp = -Double.compare(score1, score2);
        if(cmp != 0) return cmp;
        return text1.compareTo(text2);
      }

    });
    fireContentsChanged(this, 0, features.size() - 1);
  }

  public void scrollToTop() {
    if(list == null) return;
    list.ensureIndexIsVisible(0);
  }

}
