package uk.gov.companieshouse.extensions.api.common;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.gov.companieshouse.extensions.api.groups.Unit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(Unit.class)
public class ExtensionDateUtilsUnitTest {

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    public void testZeroHourDSTOffsetForWinterDate() {
        LocalDateTime winterDate = LocalDateTime.of(2020,12,21,0,0,0);
        LocalDateTime result = ExtensionDateUtils.handleDSTOffsets(winterDate);
        String resultString = result.format(FORMATTER);
        assertEquals("2020-12-21T00:00:00", resultString);
    }

    @Test
    public void testOneHourDSTOffsetForSummerDate() {
        LocalDateTime summerDate = LocalDateTime.of(2020,6,21,0,0,0);
        LocalDateTime result = ExtensionDateUtils.handleDSTOffsets(summerDate);
        String resultString = result.format(FORMATTER);
        assertEquals("2020-06-21T01:00:00", resultString);
    }

    @Test
    public void testNothingHappensWhenDateIsNull() {
        LocalDateTime summerDate = null;
        LocalDateTime result = ExtensionDateUtils.handleDSTOffsets(summerDate);
        assertNull(result);
    }

}
