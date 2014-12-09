package infuse.easy;

public interface Anonymizer {

  String fsName(String fs);

  String cName(ClassificatorNode c);

  String fold(FoldNode f);

  String type(String type);

  String subtype(String subtype);

  String name(NodeFeature f);

  String auc(double auc);

  String rank(int rank);

}
