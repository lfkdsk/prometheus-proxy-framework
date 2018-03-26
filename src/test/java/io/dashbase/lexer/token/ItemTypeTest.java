package io.dashbase.lexer.token;

import org.junit.jupiter.api.Test;

import static io.dashbase.lexer.token.ItemType.itemADD;
import static org.junit.jupiter.api.Assertions.*;

class ItemTypeTest {

    @Test
    void testIsOp() {
        assertTrue(itemADD.isOperator());
    }
}