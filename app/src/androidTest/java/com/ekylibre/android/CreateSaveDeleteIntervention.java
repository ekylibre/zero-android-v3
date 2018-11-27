package com.ekylibre.android;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.android.material.textfield.TextInputEditText;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CreateSaveDeleteIntervention {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void createSaveDeleteIntervention() {

        assertTrue("Data from online account are not present. Sync not working.",
                MainActivity.interventionsList.size() > 0);

//        ViewInteraction textInputEditText = onView(
//                allOf(childAtPosition(
//                        childAtPosition(
//                                withId(R.id.login_email),
//                                0),
//                        0),
//                        isDisplayed()));
//        textInputEditText.perform(replaceText(BuildConfig.TEST_LOGIN), closeSoftKeyboard());
//
//        ViewInteraction textInputEditText4 = onView(
//                allOf(withText(BuildConfig.TEST_LOGIN),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.login_email),
//                                        0),
//                                0),
//                        isDisplayed()));
//        textInputEditText4.perform(pressImeActionButton());
//
//        ViewInteraction textInputEditText6 = onView(
//                allOf(childAtPosition(
//                        childAtPosition(
//                                withId(R.id.login_password),
//                                0),
//                        0),
//                        isDisplayed()));
//        textInputEditText6.perform(replaceText(BuildConfig.TEST_PASSWORD), closeSoftKeyboard());
//
//        ViewInteraction textInputEditText7 = onView(
//                allOf(withText(BuildConfig.TEST_PASSWORD),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.login_password),
//                                        0),
//                                0),
//                        isDisplayed()));
//        textInputEditText7.perform(pressImeActionButton());
//
//        ViewInteraction appCompatButton = onView(
//                allOf(withId(R.id.sign_in_button), withText("Connexion"),
//                        childAtPosition(
//                                allOf(withId(R.id.login_layout),
//                                        childAtPosition(
//                                                withId(android.R.id.content),
//                                                0)),
//                                5),
//                        isDisplayed()));
//        appCompatButton.perform(click());
//
//        // Added a sleep statement to match the app's execution delay.
//        // The recommended way to handle such scenarios is to use Espresso idling resources:
//        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
//        try {
//            Thread.sleep(700);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        onView(withId(R.id.button_finishing)).perform(click());
        onView(withId(R.id.button_ground_work)).perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction constraintLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.crop_dialog_recycler),
                                childAtPosition(
                                        withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        1)),
                        5),
                        isDisplayed()));
        constraintLayout.perform(click());

        ViewInteraction constraintLayout2 = onView(
                allOf(withId(R.id.item_crop_layout),
                        childAtPosition(
                                allOf(withId(R.id.crop_container),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                0),
                        isDisplayed()));
        constraintLayout2.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.button_validate), withText("Valider"),
                        childAtPosition(
                                allOf(withId(R.id.nav_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                2)),
                                1),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction constraintLayout3 = onView(
                allOf(withId(R.id.working_period_layout),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.scroll),
                                        0),
                                2)));
        constraintLayout3.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.working_period_edit_date),
                        childAtPosition(
                                allOf(withId(R.id.working_period_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.appcompat.widget.LinearLayoutCompat")),
                                                2)),
                                5)));
        appCompatEditText.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Mois précédent"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Mois précédent"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withClassName(is("androidx.appcompat.widget.AppCompatImageButton")), withContentDescription("Mois précédent"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.DayPickerView")),
                                        childAtPosition(
                                                withClassName(is("com.android.internal.widget.DialogViewAnimator")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton4.perform(click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        appCompatButton4.perform(scrollTo(), click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.working_period_edit_duration), withText("7"),
                        childAtPosition(
                                allOf(withId(R.id.working_period_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.appcompat.widget.LinearLayoutCompat")),
                                                2)),
                                7)));
        appCompatEditText2.perform(scrollTo(), replaceText("5"));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.working_period_edit_duration), withText("5"),
                        childAtPosition(
                                allOf(withId(R.id.working_period_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.appcompat.widget.LinearLayoutCompat")),
                                                2)),
                                7),
                        isDisplayed()));
        appCompatEditText3.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.working_period_edit_duration), withText("5"),
                        childAtPosition(
                                allOf(withId(R.id.working_period_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.appcompat.widget.LinearLayoutCompat")),
                                                2)),
                                7)));
        appCompatEditText4.perform(pressImeActionButton());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.equipment_add_label), withText("+ Ajouter"),
                        childAtPosition(
                                allOf(withId(R.id.equipment_zone),
                                        childAtPosition(
                                                withId(R.id.equipment_layout),
                                                0)),
                                2)));
        appCompatTextView.perform(scrollTo(), click());

        ViewInteraction constraintLayout4 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.equipment_dialog_recycler),
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3)),
                        3),
                        isDisplayed()));
        constraintLayout4.perform(click());

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.person_add_label), withText("+ Ajouter"),
                        childAtPosition(
                                allOf(withId(R.id.person_zone),
                                        childAtPosition(
                                                withId(R.id.person_layout),
                                                0)),
                                2)));
        appCompatTextView2.perform(scrollTo(), click());

        ViewInteraction constraintLayout5 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.person_dialog_recycler),
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3)),
                        5),
                        isDisplayed()));
        constraintLayout5.perform(click());

        onView(withId(R.id.weather_layout)).perform(scrollTo(), click());

        onView(withId(R.id.weather_edit_temp)).perform(scrollTo(), replaceText("7"), closeSoftKeyboard(), pressImeActionButton());

//        onView(withId(R.id.weather_edit_wind)).perform(scrollTo(), replaceText("5"), closeSoftKeyboard(), pressImeActionButton());

//        onView(withId(R.id.weather_few_clouds)).perform(scrollTo(), click());

        onView(allOf(isDescendantOfA(withId(R.id.comment)), isAssignableFrom(TextInputEditText.class)))
                .perform(scrollTo(), replaceText("Tout va bien..."), closeSoftKeyboard(), pressImeActionButton());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.button_save), withText("Enregistrer"),
                        childAtPosition(
                                allOf(withId(R.id.nav_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        appCompatButton5.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction constraintLayout7 = onView(
                allOf(withId(R.id.intervention_item_layout),
                        childAtPosition(
                                allOf(withId(R.id.intervention_recycler),
                                        childAtPosition(
                                                withId(R.id.swipeRefreshLayout),
                                                0)),
                                3),
                        isDisplayed()));
        constraintLayout7.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(R.id.person_summary), withText("1 opérateur"),
                        childAtPosition(
                                allOf(withId(R.id.person_zone),
                                        childAtPosition(
                                                withId(R.id.person_layout),
                                                0)),
                                3)));
        appCompatTextView3.perform(scrollTo(), click());

        ViewInteraction switchCompat = onView(
                allOf(withId(R.id.person_is_driver),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.person_recycler),
                                        0),
                                3),
                        isDisplayed()));
        switchCompat.perform(click());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.button_save), withText("Enregistrer"),
                        childAtPosition(
                                allOf(withId(R.id.nav_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        appCompatButton6.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction constraintLayout8 = onView(
                allOf(withId(R.id.intervention_item_layout),
                        childAtPosition(
                                allOf(withId(R.id.intervention_recycler),
                                        childAtPosition(
                                                withId(R.id.swipeRefreshLayout),
                                                0)),
                                4),
                        isDisplayed()));
        constraintLayout8.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.button_cancel), withText("Annuler"),
                        childAtPosition(
                                allOf(withId(R.id.nav_layout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        appCompatButton7.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction constraintLayout9 = onView(
                allOf(withId(R.id.intervention_item_layout),
                        childAtPosition(
                                allOf(withId(R.id.intervention_recycler),
                                        childAtPosition(
                                                withId(R.id.swipeRefreshLayout),
                                                0)),
                                5),
                        isDisplayed()));
        constraintLayout9.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction overflowMenuButton = onView(
                allOf(withContentDescription("Plus d'options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        2),
                                0),
                        isDisplayed()));
        overflowMenuButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatTextView4 = onView(
                allOf(withId(R.id.title), withText("Supprimer"),
                        childAtPosition(childAtPosition(withId(R.id.content), 0), 0), isDisplayed()));
        appCompatTextView4.perform(click());

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(android.R.id.button1), withText("oui"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton8.perform(scrollTo(), click());
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
