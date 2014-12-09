package infuse.easy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jkanvas.io.json.JSONElement;

public class NodeToJSON {

  private final DataInfo di;
  private final List<NodeFeature> features;
  private final Anonymizer anon;

  public NodeToJSON(final DataInfo di, final List<NodeFeature> features,
      final Anonymizer anon) {
    this.anon = Objects.requireNonNull(anon);
    this.di = Objects.requireNonNull(di);
    this.features = Objects.requireNonNull(features);
  }

  public JSONElement createJSON() {
    final JSONElement fs = createFS();
    final JSONElement cs = createCS();
    final JSONElement features = createFeatures();
    return JSONElement.createObject(null, Arrays.asList(fs, cs, features));
  }

  private JSONElement createFS() {
    final List<JSONElement> el = new ArrayList<>();
    for(final String fs : di.getFSNames()) {
      final String fsName = anon.fsName(fs);
      el.add(JSONElement.createString(null, fsName));
    }
    return JSONElement.createArray("featureSelection", el);
  }

  private JSONElement createCS() {
    final List<JSONElement> el = new ArrayList<>();
    for(final FoldNode fold : di.getFolds()) {
      final JSONElement fName = JSONElement.createString("fold", anon.fold(fold));
      for(final FeatureSelectionNode fs : fold.featureSelections()) {
        final JSONElement fsName = JSONElement.createString("fs", anon.fsName(fs.name()));
        for(final ClassificatorNode c : fs.classificators()) {
          final JSONElement cName = JSONElement.createString("classifier", anon.cName(c));
          final JSONElement auc = JSONElement.createString("auc", anon.auc(c.getAUC()));
          el.add(JSONElement.createObject(null, Arrays.asList(fName, fsName, cName, auc)));
        }
      }
    }
    return JSONElement.createArray("classification", el);
  }

  private JSONElement createFeatures() {
    final List<JSONElement> el = new ArrayList<>();
    for(final NodeFeature f : features) {
      final JSONElement name = JSONElement.createString("name", anon.name(f));
      final JSONElement type = JSONElement.createString("type", anon.type(f.type()));
      final JSONElement subtype = JSONElement.createString(
          "subtype", anon.subtype(f.subtype()));
      final List<JSONElement> ranks = new ArrayList<>();
      for(final FoldNode fold : di.getFolds()) {
        final JSONElement fName = JSONElement.createString("fold", anon.fold(fold));
        for(final FeatureSelectionNode fs : fold.featureSelections()) {
          final JSONElement fsName = JSONElement.createString(
              "fs", anon.fsName(fs.name()));
          final int r = fs.getRank(f);
          final JSONElement rank;
          if(r == NodeFeature.UNRANKED) {
            rank = JSONElement.createNull("rank");
          } else {
            rank = JSONElement.createString("rank", anon.rank(r));
          }
          ranks.add(JSONElement.createObject(null, Arrays.asList(fName, fsName, rank)));
        }
      }
      el.add(JSONElement.createObject(null,
          Arrays.asList(name, type, subtype, JSONElement.createArray("ranks", ranks))));
    }
    return JSONElement.createArray("features", el);
  }

}
