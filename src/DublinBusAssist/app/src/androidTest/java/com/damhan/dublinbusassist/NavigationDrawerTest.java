package com.damhan.dublinbusassist;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.NavigationViewActions.navigateTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class NavigationDrawerTest {
    //Begin our test on main activity
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void clickStopItem_ShowsStopScreen() {
        // Open Drawer, checking if it's closed already
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(open());

        // Try Open search by stop number screen
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.stop_no_menu));

        // Check stop number page is open
        String expectedNoStatisticsText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.stopNoPage);
        onView(withId(R.id.sStopNo)).check(matches(withText(expectedNoStatisticsText)));
    }


    @Test
    public void clickStopItem_ShowsAboutScreen() {
        // Open Drawer, checking if it's closed already
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(open());

        // Try Open About Screen
        onView(withId(R.id.nav_view))
                .perform(navigateTo(R.id.settings_menu));

        // Check About is open
        String expectedAboutText = "About";
        onView(withId(R.id.sbyText)).check(matches(withText(expectedAboutText)));
    }

    

}
