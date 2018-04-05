package io.dashbase.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void testDateTimestamp() {
        String timestamp = "2018-04-03T15:44:43.000Z";
        Assert.assertEquals(1522770283L, DateUtils.timeNum(timestamp));
    }
}