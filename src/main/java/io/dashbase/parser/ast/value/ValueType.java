package io.dashbase.parser.ast.value;

import lombok.Getter;

public enum ValueType {
    ValueTypeNone("none"),
    ValueTypeVector("vector"),
    ValueTypeScalar("scalar"),
    ValueTypeMatrix("matrix"),
    ValueTypeString("string");

    @Getter
    private String text;

    ValueType(String text) {
        this.text = text;
    }

    // documentedType returns the internal type to the equivalent
    // user facing terminology as defined in the documentation.
    public String documentedType() {
        switch (this.text) {
            case "vector":
                return "instant vector";
            case "matrix":
                return "range vector";
            default:
                return text;
        }
    }
}
