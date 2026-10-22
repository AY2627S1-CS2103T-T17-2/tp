package trackflow.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static trackflow.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

    @Test
    public void constructor_nullEventOrResult_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Performance(null, "12.34", null));
        assertThrows(NullPointerException.class, () -> new Performance("100m", null, null));
    }

    @Test
    public void constructor_invalidEventOrResult_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Performance("  ", "12.34", null));
        assertThrows(IllegalArgumentException.class, () -> new Performance("100m", "abc", null));
    }

    @Test
    public void isValidEvent() {
        assertTrue(Performance.isValidEvent("100m"));
        assertTrue(Performance.isValidEvent("Long Jump"));
        assertFalse(Performance.isValidEvent(""));
        assertFalse(Performance.isValidEvent("   "));
    }

    @Test
    public void isValidResult_validTimes_returnsTrue() {
        for (String valid : new String[] {"9", "12.34", "58.70", "12.34s", "12.34S", "0",
            "2:05.4", "1:23", "10:59.99", "90:00", "1:02:03.4", "1:2:3"}) {
            assertTrue(Performance.isValidResult(valid), valid);
        }
    }

    @Test
    public void isValidResult_validDistances_returnsTrue() {
        for (String valid : new String[] {"6.21m", "1.85m", "18.44m", "0m", "6.21M", "100m"}) {
            assertTrue(Performance.isValidResult(valid), valid);
        }
    }

    @Test
    public void isValidResult_invalid_returnsFalse() {
        for (String invalid : new String[] {"", "   ", "abc", "-5", "12.34.5", "1:2:3:4", ":30", "1:",
            "1::30", "1:70", "1:60:00", "s", "m", "12 34", "1.", ".5", "6.21km", "-6.21m", "1:2:3:4:5"}) {
            assertFalse(Performance.isValidResult(invalid), invalid);
        }
    }

    @Test
    public void isValidDate() {
        assertTrue(Performance.isValidDate("2026-10-22"));
        assertTrue(Performance.isValidDate("2024-02-29")); // leap year
        assertFalse(Performance.isValidDate("2026-13-40"));
        assertFalse(Performance.isValidDate("2026-02-29")); // not a leap year
        assertFalse(Performance.isValidDate("22-10-2026"));
        assertFalse(Performance.isValidDate("today"));
        assertFalse(Performance.isValidDate(""));
    }

    @Test
    public void getDisplayResult_appendsUnit() {
        assertEquals("12.34 s", new Performance("100m", "12.34", null).getDisplayResult());
        assertEquals("58.7 s", new Performance("100m", "58.7s", null).getDisplayResult());
        assertEquals("2:05.4", new Performance("800m", "2:05.4", null).getDisplayResult());
        assertEquals("6.21 m", new Performance("Long Jump", "6.21m", null).getDisplayResult());
    }

    @Test
    public void getKind_detectsTimeVersusDistance() {
        assertEquals(Performance.Kind.TIME, new Performance("100m", "12.34", null).getKind());
        assertEquals(Performance.Kind.TIME, new Performance("800m", "2:05.4", null).getKind());
        assertEquals(Performance.Kind.DISTANCE, new Performance("Long Jump", "6.21m", null).getKind());
    }

    @Test
    public void isBetterThan_time_lowerIsBetter() {
        Performance faster = new Performance("100m", "12.10", null);
        Performance slower = new Performance("100m", "12.34", null);
        assertTrue(faster.isBetterThan(slower));
        assertFalse(slower.isBetterThan(faster));
    }

    @Test
    public void isBetterThan_distance_higherIsBetter() {
        Performance longer = new Performance("Long Jump", "6.50m", null);
        Performance shorter = new Performance("Long Jump", "6.21m", null);
        assertTrue(longer.isBetterThan(shorter));
        assertFalse(shorter.isBetterThan(longer));
    }

    @Test
    public void isBetterThan_equalResults_returnsFalseBothWays() {
        // 2:05.4 equals 125.4 seconds equals 125.4s; neither is strictly better.
        Performance colon = new Performance("800m", "2:05.4", null);
        Performance seconds = new Performance("800m", "125.4", null);
        Performance withSuffix = new Performance("800m", "125.4s", null);
        assertFalse(colon.isBetterThan(seconds));
        assertFalse(seconds.isBetterThan(colon));
        assertFalse(seconds.isBetterThan(withSuffix));
    }

    @Test
    public void isComparableResult_timeVersusDistance_returnsFalse() {
        Performance time = new Performance("100m", "12.34", null);
        Performance distance = new Performance("100m", "6.21m", null);
        assertFalse(time.isComparableResult(distance));
        assertThrows(IllegalArgumentException.class, () -> time.isBetterThan(distance));
    }

    @Test
    public void isSameEvent_ignoresCase() {
        Performance a = new Performance("100m", "12.34", null);
        Performance b = new Performance("100M", "12.10", null);
        Performance c = new Performance("200m", "24.00", null);
        assertTrue(a.isSameEvent(b));
        assertFalse(a.isSameEvent(c));
    }

    @Test
    public void equalsHashCodeAndToString() {
        LocalDate date = LocalDate.parse("2026-10-22");
        Performance a = new Performance("100m", "12.34", date);
        Performance sameValues = new Performance("100m", "12.34", date);
        Performance differentDate = new Performance("100m", "12.34", null);

        assertEquals(a, sameValues);
        assertEquals(a.hashCode(), sameValues.hashCode());
        assertNotEquals(a, differentDate);
        assertNotEquals(a, null);
        assertNotEquals(a, "100m 12.34");

        assertEquals("100m 12.34 (2026-10-22)", a.toString());
        assertEquals("100m 12.34", differentDate.toString());
    }
}
