package dk.aau.cs.giraf.launcher.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class MyEspressoTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;

    public MyEspressoTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    public void someTest() {
        onView(withId(R.id.settings_button)).perform(click());
        onView(withId(R.id.settingsListFragment)).check(ViewAssertions.matches(isDisplayed()));
    }
}