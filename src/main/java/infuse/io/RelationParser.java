package infuse.io;

import infuse.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jkanvas.util.Resource;

/**
 * Parses a dot file into relations.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class RelationParser implements Relations {

  /** Regex for splitting a link. */
  private static final String ARROW = "([a-zA-Z0-9_]+)\\s->\\s([a-zA-Z0-9_]+)\\s+\\[.+";
  /** The pattern for splitting a link. */
  private static final Pattern ARROW_PAT = Pattern.compile(ARROW);

  /** Forward rules. */
  private final Map<Model, Set<Model>> rules;
  /** Backward rules. */
  private final Map<Model, Set<Model>> reverseRules;
  /** The label of a rule. */
  private final Map<Model, Map<Model, String>> labels;

  /**
   * Creates a relation parser.
   * 
   * @param pool The model pool.
   * @param dot The dot file.
   * @throws IOException I/O Exception.
   */
  public RelationParser(final ModelPool pool, final Resource dot) throws IOException {
    rules = new HashMap<>();
    reverseRules = new HashMap<>();
    labels = new HashMap<>();
    final BufferedReader in = dot.reader();
    String line;
    while((line = in.readLine()) != null) {
      try {
        final Matcher m = ARROW_PAT.matcher(line);
        if(!m.matches()) {
          continue;
        }
        final String l = "label=";
        final int labelStart = line.indexOf(l) + l.length();
        if(labelStart < l.length()) throw new IllegalArgumentException(
            "could not find label");
        final int labelEnd = line.indexOf("]", labelStart);
        if(labelEnd < 0) throw new IllegalArgumentException("could not find end of label");
        final String label = line.charAt(labelStart) == '"' ? line.substring(
            labelStart + 1, labelEnd - 1) : line.substring(labelStart, labelEnd);
        final Model from = Objects.requireNonNull(pool.get(m.group(1)));
        final Model to = pool.get(m.group(2));
        if(to == null) {
          System.err.println("warning: " + m.group(2) + " not in model pool");
          continue;
        }
        if(!labels.containsKey(from)) {
          labels.put(from, new HashMap<Model, String>());
        }
        final Map<Model, String> lFrom = labels.get(from);
        if(lFrom.containsKey(to)) throw new IllegalArgumentException("multiple labels");
        lFrom.put(to, label);
        if(!rules.containsKey(from)) {
          rules.put(from, new HashSet<Model>());
        }
        rules.get(from).add(to);
        if(!reverseRules.containsKey(to)) {
          reverseRules.put(to, new HashSet<Model>());
        }
        reverseRules.get(to).add(from);
      } catch(final Exception e) {
        throw new IOException("error in line: " + line, e);
      }
    }
    in.close();
  }

  @Override
  public Set<Model> getChilds(final Model node) {
    final Set<Model> set = rules.get(node);
    if(set == null) return Collections.emptySet();
    return Collections.unmodifiableSet(set);
  }

  @Override
  public Set<Model> getParents(final Model node) {
    final Set<Model> set = reverseRules.get(node);
    if(set == null) return Collections.emptySet();
    return Collections.unmodifiableSet(set);
  }

  @Override
  public String getLabel(final Model from, final Model to) {
    return labels.get(from).get(to);
  }

}
