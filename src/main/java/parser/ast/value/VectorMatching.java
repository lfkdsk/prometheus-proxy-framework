package parser.ast.value;

import exception.ParserException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public class VectorMatching {
    public enum VectorMatchCardinality {
        CardOneToOne,
        CardManyToOne,
        CardOneToMany,
        CardManyToMany;

        public String text() {
            switch (this) {
                case CardOneToOne:
                    return "one-to-one";
                case CardManyToOne:
                    return "many-to-one";
                case CardOneToMany:
                    return "one-to-many";
                case CardManyToMany:
                    return "many-to-many";
            }

            throw new RuntimeException("promql.VectorMatchCardinality.String: unknown match cardinality");
        }
    }

    public VectorMatchCardinality card;
    public List<String> matchingLabels;
    public boolean on;
    public List<String> include;

    private VectorMatching(VectorMatchCardinality card, List<String> matchingLabels, boolean on, List<String> include) {
        this.card = card;
        this.matchingLabels = matchingLabels;
        this.on = on;
        this.include = include;
    }

    public static VectorMatching of(VectorMatchCardinality card) {
        return new VectorMatching(card, Collections.emptyList(), false, Collections.emptyList());
    }

    @Override
    public String toString() {
        String matchLabels = matchingLabels.stream().collect(joining(","));
        return String.format("VectorMatching<%s,%s,%s>", card.text(), matchLabels, on);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof VectorMatching)) {
            return false;
        }

        VectorMatching other = (VectorMatching) obj;
        return hashCode() == other.hashCode();
    }
}
