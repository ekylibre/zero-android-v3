package com.ekylibre.android;

import android.content.SharedPreferences;

import org.junit.Ignore;
import org.junit.Test;

import static android.content.Context.MODE_PRIVATE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MockitoTests {

    @Ignore
    @Test
    public void testContext() {
        MainActivity mainActivity = mock(MainActivity.class);

        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("prefs", MODE_PRIVATE);
        assertNotNull("Trop null !", sharedPreferences);
    }
}
