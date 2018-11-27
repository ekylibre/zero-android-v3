package com.ekylibre.android;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LoginActivityTest.class,
        InterventionTest.class,
//        CreateSaveDeleteIntervention.class,
})

public class UITestSuite {
    // the class remains empty,
    // used only as a holder for the above annotations
}