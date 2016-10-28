package com.siemens.citypulse.androidapp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.siemens.citypulse.androidapp.common.DefaultValues;

/**
 * This is the main activity of the application. It contains three tab
 * fragments: route, parking and setings.
 * 
 * @author dan.puiu, cosmin marin
 *
 */

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	private ViewPager viewPager;
	private TabPagerAdapter mAdapter;
	private ActionBar actionBar;
	private String[] tabs = { "Parking place", "Route", "Settings" };
	private final int ROUTE_TAB_NUMBER = 1;
	private final int TRAVEL_TAB_NUMBER = 0;

	private BroadcastReceiver broadcastReceiverRecommendParking;
	private BroadcastReceiver broadcastReceiverRestart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
				getServerLocation();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		broadcastReceiverRecommendParking = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				viewPager.setCurrentItem(ROUTE_TAB_NUMBER);
			}
		};

		IntentFilter intentFilterRecommendParking = new IntentFilter();
		intentFilterRecommendParking
				.addAction(ParkingPlaceSelectionActivity.GO_TO_ROUTE_SELECTION);
		registerReceiver(broadcastReceiverRecommendParking,
				intentFilterRecommendParking);

		broadcastReceiverRestart = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() == DefaultValues.COMMAND_GO_TO_TRAVEL_RECOMANDATION) {
					viewPager.setCurrentItem(ROUTE_TAB_NUMBER);
				} else {
					viewPager.setCurrentItem(TRAVEL_TAB_NUMBER);
				}

			}
		};

		IntentFilter intentFilterReceiverRestart = new IntentFilter();
		intentFilterReceiverRestart
				.addAction(DefaultValues.COMMAND_GO_TO_PARKING_RECOMANDATION);
		intentFilterReceiverRestart
				.addAction(DefaultValues.COMMAND_GO_TO_TRAVEL_RECOMANDATION);
		registerReceiver(broadcastReceiverRestart, intentFilterReceiverRestart);

	}

	@Override
	protected void onDestroy() {

		unregisterReceiver(broadcastReceiverRestart);
		unregisterReceiver(broadcastReceiverRecommendParking);

		super.onDestroy();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected
		// show respected fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	private String getServerLocation() {

		SharedPreferences settingsPreferences = getSharedPreferences(
				"SettingsPreferences", Context.MODE_PRIVATE);

		String serverIP = settingsPreferences.getString("serverLocation",
				DefaultValues.WEB_SOCKET_SERVER_IP);

		System.out.println(serverIP);

		return serverIP;
	}

}