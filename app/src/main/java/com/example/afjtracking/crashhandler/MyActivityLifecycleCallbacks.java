package com.example.afjtracking.crashhandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import java.util.LinkedList;
import java.util.List;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

	public List<Activity> activities = new LinkedList<>();
	public static int sAnimationId = 0;

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		addActivity(activity);
	}

	@Override
	public void onActivityStarted(Activity activity) {

	}

	@Override
	public void onActivityResumed(Activity activity) {

	}

	@Override
	public void onActivityPaused(Activity activity) {

	}

	@Override
	public void onActivityStopped(Activity activity) {

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		removeActivity(activity);
	}

	public void addActivity(Activity activity) {
		if (activities == null) {
			activities = new LinkedList<>();
		}

		if (!activities.contains(activity)) {
			activities.add(activity);
		}
	}


	public void removeActivity(Activity activity) {
        activities.remove(activity);

		if (activities.size() == 0) {
			activities = null;
		}
	}

	public void removeAllActivities() {
		for (Activity activity : activities) {
			if (null != activity) {
				activity.finish();
				activity.overridePendingTransition(0, sAnimationId);
			}
		}
	}
}
