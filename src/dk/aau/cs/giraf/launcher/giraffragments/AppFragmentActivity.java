package dk.aau.cs.giraf.launcher.giraffragments;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import dk.aau.cs.giraf.launcher.R;

public class AppFragmentActivity extends FragmentActivity {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar actionBar = getActionBar();
        final Activity activity = this;

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        mViewPager = (ViewPager) findViewById(R.id.pager);


        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the
                // corresponding page in the ViewPager.
                mViewPager.setCurrentItem(tab.getPosition());

                //If we got to the Play Store tab, so we're opening the play store.
                if(tab.getText().equals("Play Store"))
                {
                    final String appPackageName = activity.getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        // This TaskStackBuilder makes sure that the Play Store returns to this activity after having been closed.
                        TaskStackBuilder.create(activity)
                                .addParentStack(activity)
                                .addNextIntentWithParentStack(new Intent(activity.getApplicationContext(), activity.getClass()))
                                .addNextIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + appPackageName)))
                                .startActivities();
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
                //if(tab.getPosition() == 0)
                //    getActionBar().setSelectedNavigationItem(actionBar.getTabCount()-1);
            }
        };

        // Add tabs, specifying the tab's text and TabListener
        actionBar.addTab(actionBar.newTab().setText("Giraf").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Android").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Play Store").setTabListener(tabListener));

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        if (position <= actionBar.getTabCount() - 1)
                            getActionBar().setSelectedNavigationItem(position);

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        super.onPageScrollStateChanged(state);
                    }

                });

        if( savedInstanceState != null ){
            getActionBar().selectTab(actionBar.getTabAt(savedInstanceState.getInt("tabState")));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("tabState", getActionBar().getSelectedTab().getPosition());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
