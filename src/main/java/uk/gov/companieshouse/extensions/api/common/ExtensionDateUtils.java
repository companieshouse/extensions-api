package uk.gov.companieshouse.extensions.api.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class ExtensionDateUtils {

    private static final String TIMEZONE_ID = "Europe/London";

    public static LocalDateTime handleDSTOffsets(LocalDateTime date) {
        if (date != null) {
            ZoneId zone = ZoneId.of(TIMEZONE_ID);
            ZoneOffset zoneOffSet = zone.getRules().getOffset(date);
            LocalDateTime newDate = date.plusSeconds(zoneOffSet.getTotalSeconds());
            return newDate;
        }
        return date;
    }
}
