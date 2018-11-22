package com.ekylibre.android;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DatabaseWriteReadTest.class,
        APITest.class,
})

public class TestSuite {
    // the class remains empty,
    // used only as a holder for the above annotations
}