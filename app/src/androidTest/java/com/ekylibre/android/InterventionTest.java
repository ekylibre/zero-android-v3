package com.ekylibre.android;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ekylibre.android.database.AppDatabase;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class InterventionTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void interventionTest() {

        // Verify login previously passed
        assertTrue("Data from online account are not present. Sync not working.",
                MainActivity.interventionsList.size() > 0);

        onView(withId(R.id.button_finishing)).perform(click());
        onView(withId(R.id.button_ground_work)).perform(click());

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(
                childAtPosition(childAtPosition(withId(R.id.crop_dialog_recycler), 0), 0),
                isDisplayed()))
                .perform(click());
        onView(withId(R.id.button_validate)).perform(click());

        onView(withId(R.id.working_period_layout)).perform(scrollTo(), click());
        onView(withId(R.id.working_period_edit_duration)).perform(scrollTo(), replaceText("5"));

        onView(withId(R.id.equipment_add_label)).perform(scrollTo(), click());

//        onView(withId(R.id.equipment_dialog_create_new)).perform(click());
//        onView(withId(R.id.create_equipment_type_spinner)).perform(click());
//
////        DataInteraction appCompatCheckedTextView = onData(anything())
//////                .inAdapterView(childAtPosition(
//////                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
//////                        0))
////                .atPosition(8);
////        appCompatCheckedTextView.perform(click());
//
//        onData(anything()).atPosition(0).perform(click());
//
//        try {
//            Thread.sleep(700);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
////        onView(withId(R.id.create_equipment_type_spinner))
////                .check(matches(withSpinnerText(containsString("Déchaumeur"))));
//
//        onView(allOf(childAtPosition(
//                childAtPosition(withId(R.id.create_equipment_name), 0),
//                0),
//                isDisplayed()))
//                .perform(replaceText("Chaumdu"), closeSoftKeyboard());
//
//        onView(allOf(childAtPosition(
//                        childAtPosition(
//                                withId(R.id.create_equipment_field1),
//                                0),
//                        0),
//                        isDisplayed()))
//                .perform(replaceText("5"), closeSoftKeyboard());
//
//        ViewInteraction appCompatButton4 = onView(
//                allOf(withId(android.R.id.button1), withText("Créer"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)));
//        appCompatButton4.perform(scrollTo(), click());
//
//        ViewInteraction constraintLayout2 = onView(
//                allOf(childAtPosition(
//                        allOf(withId(R.id.equipment_dialog_recycler),
//                                childAtPosition(
//                                        withClassName(is("android.widget.LinearLayout")),
//                                        3)),
//                        0),
//                        isDisplayed()));
//        constraintLayout2.perform(click());

//        onView(withId(R.id.equipment_add_label)).perform(scrollTo(), click());

        ViewInteraction constraintLayout3 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.equipment_dialog_recycler),
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3)),
                        1),
                        isDisplayed()));
        constraintLayout3.perform(click());

        onView(withId(R.id.person_add_label)).perform(scrollTo(), click());

        ViewInteraction constraintLayout4 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.person_dialog_recycler),
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3)),
                        0),
                        isDisplayed()));
        constraintLayout4.perform(click());

        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.person_is_driver),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.person_recycler),
                                        0),
                                3),
                        isDisplayed()));
        switchCompat.perform(click());

        onView(withId(R.id.weather_layout)).perform(scrollTo(), click());
        onView(withId(R.id.weather_edit_temp)).perform(scrollTo(), replaceText("8.5"), closeSoftKeyboard());
        onView(withId(R.id.weather_edit_wind)).perform(scrollTo(), replaceText("5.3"), closeSoftKeyboard(), pressImeActionButton());
        onView(withId(R.id.weather_shower_rain)).perform(scrollTo(), click());

        onView(childAtPosition(childAtPosition(withId(R.id.comment), 0), 0))
                .perform(scrollTo(), replaceText("Il pleut des cordes"), closeSoftKeyboard());

        onView(withId(R.id.button_save)).perform(click());

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Sync intervention

        ViewInteraction constraintLayout6 = onView(
                allOf(withId(R.id.intervention_item_layout),
                        childAtPosition(
                                allOf(withId(R.id.intervention_recycler),
                                        childAtPosition(
                                                withId(R.id.swipeRefreshLayout),
                                                0)),
                                0),
                        isDisplayed()));
        constraintLayout6.perform(click());

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify checkbox is checked
        onView(allOf(withId(R.id.intervention_item_layout),
                        childAtPosition(
                                allOf(withId(R.id.intervention_recycler),
                                        childAtPosition(
                                                withId(R.id.swipeRefreshLayout),
                                                0)),
                                0),
                        isDisplayed())).perform(click());

        onView(withId(R.id.equipment_summary)).perform(scrollTo(), click());


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        onView(allOf(withId(R.id.equipment_delete),
//                        childAtPosition(withId(R.id.equipment_recycler), 3),
//                        isDisplayed()))
//                .perform(click());
//
//        ViewInteraction appCompatButton6 = onView(
//                allOf(withId(android.R.id.button1), withText("oui"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)));
//        appCompatButton6.perform(scrollTo(), click());
//
//        ViewInteraction appCompatButton7 = onView(
//                allOf(withId(R.id.button_save), withText("Enregistrer"),
//                        childAtPosition(
//                                allOf(withId(R.id.nav_layout),
//                                        childAtPosition(
//                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
//                                                1)),
//                                1),
//                        isDisplayed()));
//        appCompatButton7.perform(click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(700);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction constraintLayout7 = onView(
//                allOf(withId(R.id.intervention_item_layout),
//                        childAtPosition(
//                                allOf(withId(R.id.intervention_recycler),
//                                        childAtPosition(
//                                                withId(R.id.swipeRefreshLayout),
//                                                0)),
//                                0),
//                        isDisplayed()));
//        constraintLayout7.perform(click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(700);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction overflowMenuButton = onView(
//                allOf(withContentDescription("Plus d'options"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.action_bar),
//                                        2),
//                                0),
//                        isDisplayed()));
//        overflowMenuButton.perform(click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(250);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        ViewInteraction appCompatTextView6 = onView(
//                allOf(withId(R.id.title), withText("Supprimer"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.content),
//                                        0),
//                                0),
//                        isDisplayed()));
//        appCompatTextView6.perform(click());
//
//        ViewInteraction appCompatButton8 = onView(
//                allOf(withId(android.R.id.button1), withText("oui"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)));
//        appCompatButton8.perform(scrollTo(), click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
