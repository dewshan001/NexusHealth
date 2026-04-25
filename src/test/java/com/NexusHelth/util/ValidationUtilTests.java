package com.NexusHelth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTests {

    @Test
    void normalizeGender_shouldLowercaseAndTrim() {
        assertEquals("male", ValidationUtil.normalizeGender("Male"));
        assertEquals("female", ValidationUtil.normalizeGender("  FEMALE "));
        assertEquals("other", ValidationUtil.normalizeGender("other"));
    }

    @Test
    void normalizeGender_shouldMapShortForms() {
        assertEquals("male", ValidationUtil.normalizeGender("m"));
        assertEquals("female", ValidationUtil.normalizeGender("F"));
    }

    @Test
    void normalizeGender_blankShouldReturnNull() {
        assertNull(ValidationUtil.normalizeGender(null));
        assertNull(ValidationUtil.normalizeGender(""));
        assertNull(ValidationUtil.normalizeGender("   "));
    }

    @Test
    void isValidGender_shouldUseNormalizedValues() {
        assertTrue(ValidationUtil.isValidGender("Male"));
        assertTrue(ValidationUtil.isValidGender("female"));
        assertTrue(ValidationUtil.isValidGender(" OTHER "));
        assertFalse(ValidationUtil.isValidGender("unknown"));
        assertFalse(ValidationUtil.isValidGender(""));
    }
}

