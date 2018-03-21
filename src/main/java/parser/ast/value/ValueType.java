package parser.ast.value;

public enum ValueType {
    ValueTypeNone("none"),
    ValueTypeVector("vector"),
    ValueTypeScalar("scalar"),
    ValueTypeMatrix("matrix"),
    ValueTypeString("string");

    private String text;

    ValueType(String text) {
        this.text = text;
    }
}
