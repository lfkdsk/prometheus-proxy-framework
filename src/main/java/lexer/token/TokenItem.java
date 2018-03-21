package lexer.token;

import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;
import static lexer.state.States.itemTypeStr;

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

//    func (i item) desc() string {
//        if _, ok := itemTypeStr[i.typ]; ok {
//            return i.String()
//        }
//        if i.typ == itemEOF {
//            return i.typ.desc()
//        }
//        return fmt.Sprintf("%s %s", i.typ.desc(), i)
//    }

    public String desc() {
        if (itemTypeStr.containsKey(type)) {
            return toString();
        }

        if (type == ItemType.itemEOF) {
            return type.desc();
        }

        return format("%s %s", type.desc(), this.toString());
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
