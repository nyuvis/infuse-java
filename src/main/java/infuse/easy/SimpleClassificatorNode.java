package infuse.easy;

import java.util.Map;

public final class SimpleClassificatorNode implements ClassificatorNode {
  private final String abbr;
  private final String name;
  private final String id;
  private final double auc;
  private final String par;
  private final Map<String, FeatureSelectionNode> fs;
  private FeatureSelectionNode fsn;

  SimpleClassificatorNode(final String name, final double auc,
      final String par, final Map<String, FeatureSelectionNode> fs) {
    String n = name;
    if(n.contains("Baysian")) {
      n = n.replace("Baysian", "Bayesian");
    }
    this.name = n.substring(0, n.indexOf("_N"));
    abbr = this.name.substring("Classification".length());
    this.auc = auc;
    this.par = par;
    this.fs = fs;
    id = n;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String abbreviation() {
    return abbr;
  }

  @Override
  public FeatureSelectionNode getFeatureSelectionMethod() {
    if(fsn == null) {
      fsn = fs.get(par);
    }
    return fsn;
  }

  @Override
  public double getAUC() {
    return auc;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof ClassificatorNode)) return false;
    return name().equals(((ClassificatorNode) obj).name());
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }

}
