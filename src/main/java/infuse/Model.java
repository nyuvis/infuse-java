package infuse;

import java.util.Objects;

import jkanvas.io.json.JSONElement;

public class Model {

  private final String id;

  private final double auc;

  public Model(final String id, final double auc) {
    this.id = Objects.requireNonNull(id);
    this.auc = auc;
  }

  public String getId() {
    return id;
  }

  public double getAreaUnderCurve() {
    return auc;
  }

  @Override
  public String toString() {
    return id + " (" + auc + ")";
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof Model)) return false;
    return getId().equals(((Model) obj).getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  public static final Model fromJSON(final JSONElement el) {
    return new Model(el.getString("id"), el.getDouble("auc"));
  }

}
