package infuse.easy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SimpleNodeFeature implements NodeFeature {
  private final String type;
  private final int id;
  private final String name;
  private final String subtype;
  private final String read;

  SimpleNodeFeature(final String type, final int id,
      final String name, final String subtype) {
    this.type = type;
    this.id = id;
    this.name = name;
    this.subtype = subtype;
    final int begin = name.indexOf('[');
    final int end = name.indexOf(']');
    read = begin >= 0 && end >= 0 ? name.substring(begin + 1, end) : name;
  }

  @Override
  public double importance(final DataInfo di) {
    double score = 0;
    int nonRanked = 0;
    int count = 0;
    for(final FoldNode fn : di.getFolds()) {
      for(final FeatureSelectionNode fsn : fn.featureSelections()) {
        if(!hasRank(fsn)) {
          ++nonRanked;
        } else {
          score += getRank(fsn);
          ++count;
        }
      }
    }
    return (score + nonRanked * di.getMaxRank() * 2.0) / (nonRanked + count);
  }

  @Override
  public double meanRank(final DataInfo di) {
    double score = 0;
    int count = 0;
    for(final FoldNode fn : di.getFolds()) {
      for(final FeatureSelectionNode fsn : fn.featureSelections()) {
        if(hasRank(fsn)) {
          score += getRank(fsn);
          ++count;
        }
      }
    }
    return score / count;
  }

  @Override
  public double stdDevRank(final DataInfo di) {
    final double mean = meanRank(di);
    double score = 0;
    int count = 0;
    for(final FoldNode fn : di.getFolds()) {
      for(final FeatureSelectionNode fsn : fn.featureSelections()) {
        if(hasRank(fsn)) {
          final double v = getRank(fsn);
          score += (v - mean) * (v - mean);
          ++count;
        }
      }
    }
    return Math.sqrt(score / count);
  }

  @Override
  public int appearsIn(final DataInfo di) {
    int ranked = 0;
    for(final FoldNode fn : di.getFolds()) {
      for(final FeatureSelectionNode fsn : fn.featureSelections()) {
        if(hasRank(fsn)) {
          ++ranked;
        }
      }
    }
    return ranked;
  }

  @Override
  public int bestRank(final DataInfo di) {
    int bestRank = UNRANKED;
    for(final FoldNode fn : di.getFolds()) {
      for(final FeatureSelectionNode fsn : fn.featureSelections()) {
        if(hasRank(fsn)) {
          final int rank = getRank(fsn);
          if(bestRank == UNRANKED || rank < bestRank) {
            bestRank = rank;
          }
        }
      }
    }
    return bestRank;
  }

  @Override
  public double medianRank(final DataInfo di) {
    final List<Integer> ranks = new ArrayList<>();
    for(final FoldNode fn : di.getFolds()) {
      for(final FeatureSelectionNode fsn : fn.featureSelections()) {
        if(hasRank(fsn)) {
          ranks.add(getRank(fsn));
        }
      }
    }
    if(ranks.isEmpty()) return Double.POSITIVE_INFINITY;
    Collections.sort(ranks);
    final int s = ranks.size();
    if(ranks.size() == 1) return ranks.get(0);
    final int p = s / 2;
    return (s % 2 == 0) ? (ranks.get(p) + ranks.get(p - 1)) * 0.5 : ranks.get(p);
  }

  @Override
  public int getRank(final FeatureSelectionNode fs) {
    return fs.getRank(this);
  }

  @Override
  public boolean hasRank(final FeatureSelectionNode fs) {
    return getRank(fs) != NodeFeature.UNRANKED;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public String subtype() {
    return subtype;
  }

  @Override
  public String combinedType() {
    return type + " - " + subtype;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String readableName() {
    return read;
  }

  @Override
  public String searchName() {
    return name + "     " + subtype + "     " + type;
  }

  @Override
  public int id() {
    return id;
  }

  @Override
  public String toString() {
    return readableName();
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof NodeFeature)) return false;
    return id() == ((NodeFeature) obj).id();
  }

  @Override
  public int hashCode() {
    return id();
  }

}
