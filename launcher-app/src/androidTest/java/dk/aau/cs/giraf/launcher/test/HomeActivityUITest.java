package dk.aau.cs.giraf.launcher.test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.test.ActivityInstrumentationTestCase2;
import android.test.IsolatedContext;

import org.junit.After;
import org.junit.Before;

import dk.aau.cs.giraf.launcher.R;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.activities.MainActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import dk.aau.cs.giraf.dblib.Helper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class HomeActivityUITest
        extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mActivity;

    public HomeActivityUITest() {
        super(HomeActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        Helper h = new Helper(getInstrumentation().getTargetContext().getApplicationContext());
        h.CreateDummyData();

        Intent intent = new Intent();
        intent.putExtra(Constants.GUARDIAN_ID, h.profilesHelper.getGuardians().get(0).getId());
        setActivityIntent(intent);

        mActivity = getActivity();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Helper h = new Helper(getInstrumentation().getTargetContext().getApplicationContext());
        h.clearTables();
    }

    public void testSettingsButton() {
        onView(withId(R.id.settings_button)).perform(click());
        onView(withId(R.id.settingsListFragment)).check(ViewAssertions.matches(isDisplayed()));
    }
}