package infuse;

import infuse.easy.NodeFeature;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FilterBox extends JTextField {

  private final FeatureList list;

  private final List<NodeFeature> allFeatures;

  private boolean needToRefill = true;

  public FilterBox(final FeatureList list) {
    this.list = list;
    allFeatures = new ArrayList<>();
    getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(final DocumentEvent e) {
        changedUpdate(e);
      }

      @Override
      public void insertUpdate(final DocumentEvent e) {
        changedUpdate(e);
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        search(getText());
      }

    });
    setMaximumSize(new Dimension(Integer.MAX_VALUE, getSize().height));
  }

  private void refill() {
    if(!needToRefill) return;
    allFeatures.addAll(list.getFeatures());
    needToRefill = false;
  }

  private void addAllBack() {
    if(needToRefill) throw new IllegalStateException("features are empty");
    for(final NodeFeature nf : allFeatures) {
      list.addFeature(nf);
    }
    allFeatures.clear();
    needToRefill = true;
  }

  private void doFilter(final String[] terms) {
    refill();
    for(final NodeFeature nf : allFeatures) {
      if(accept(nf, terms)) {
        list.addFeature(nf);
      } else {
        list.removeFeature(nf);
      }
    }
  }

  private static boolean accept(final NodeFeature nf, final String[] terms) {
    final String text = nf.searchName().toLowerCase();
    for(final String t : terms) {
      if(!text.contains(t)) return false;
    }
    return true;
  }

  private static String[] getTerms(final String query) {
    final List<String> allTerms = new ArrayList<>();
    for(final String q : query.split(" ")) {
      if(q.isEmpty()) {
        continue;
      }
      allTerms.add(q.toLowerCase());
    }
    return allTerms.toArray(new String[0]);
  }

  public void search(final String query) {
    try (final AutoCloseable bulk = list.startBulkOperation()) {
      final String[] terms = getTerms(query);
      if(terms.length == 0) {
        if(needToRefill) return;
        addAllBack();
        return;
      }
      doFilter(terms);
      list.sortBySelection(terms);
    } catch(final Exception e) {
      throw new RuntimeException(e);
    }
  }

}
