package com.vbay.server.service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    void formatRemainingDuration_variousIntervals_formatsCorrectly() throws Exception {
        AuthService authService = new AuthService(null, null, null, null);
        Method formatMethod = AuthService.class.getDeclaredMethod("formatRemainingDuration", LocalDateTime.class, LocalDateTime.class);
        formatMethod.setAccessible(true);

        LocalDateTime now = LocalDateTime.of(2026, 5, 26, 12, 0, 0);

        // Test years, days, hours, minutes, seconds
        LocalDateTime target1 = now.plusYears(2).plusDays(5).plusHours(3).plusMinutes(15).plusSeconds(45);
        String res1 = (String) formatMethod.invoke(authService, now, target1);
        assertEquals("2 years 5 days 3 hours 15 minutes 45 seconds", res1);

        // Test singular values
        LocalDateTime target2 = now.plusYears(1).plusDays(1).plusHours(1).plusMinutes(1).plusSeconds(1);
        String res2 = (String) formatMethod.invoke(authService, now, target2);
        assertEquals("1 year 1 day 1 hour 1 minute 1 second", res2);

        // Test no seconds
        LocalDateTime target3 = now.plusHours(1).plusMinutes(30);
        String res3 = (String) formatMethod.invoke(authService, now, target3);
        assertEquals("1 hour 30 minutes", res3);

        // Test only seconds
        LocalDateTime target4 = now.plusSeconds(30);
        String res4 = (String) formatMethod.invoke(authService, now, target4);
        assertEquals("30 seconds", res4);

        // Test expired / start after end
        String res5 = (String) formatMethod.invoke(authService, now.plusSeconds(1), now);
        assertEquals("0 seconds", res5);
    }
}
