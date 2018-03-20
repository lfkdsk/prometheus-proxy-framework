package model.token;

import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

public class TokenItem {
    public final ItemType type;
    public final int position;
    public final String text;

    private TokenItem(ItemType type, int position, String text) {
        this.type = type;
        this.position = position;
        this.text = text;
    }

    public static TokenItem of(@NotNull ItemType type, int position, @NotNull String text) {
        return new TokenItem(type, position, text);
    }

    @Override
    public String toString() {
        return format("Item<%s,%d,%s>", type.name(), position, text);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TokenItem)) {
            return false;
        }

        TokenItem other = (TokenItem) obj;
        return other.hashCode() == this.hashCode();
    }
}
