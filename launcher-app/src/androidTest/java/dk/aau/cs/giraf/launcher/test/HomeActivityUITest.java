package dk.aau.cs.giraf.launcher.test;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import dk.aau.cs.giraf.dblib.Helper;
import dk.aau.cs.giraf.launcher.activities.HomeActivity;
import dk.aau.cs.giraf.launcher.helper.Constants;
import org.junit.After;
import org.junit.Before;

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
        //onView(withId(R.id.settings_button)).perform(click());
        //onView(withId(R.id.settingsListFragment)).check(ViewAssertions.matches(isDisplayed()));
    }
}