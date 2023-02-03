package com.afjltd.tracking.ota;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ForceUpdateChecker {

    private static final String TAG = ForceUpdateChecker.class.getSimpleName();

    public static final String KEY_UPDATE_REQUIRED = "force_update_required";
    public static final String KEY_CURRENT_VERSION = "force_update_current_version";
    public static final String KEY_UPDATE_URL = "force_update_store_url";


    public static final String KEY_APP_UPDATE_JSON = "app_update_data";

    public static final String APP_NAME = "TRACKING";


    private final OnUpdateNeededListener onUpdateNeededListener;
    private final Context context;

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String updateUrl);
        void onAppUptoDate();
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public ForceUpdateChecker(@NonNull Context context,
                              OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public void check() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        String appData = remoteConfig.getString(KEY_APP_UPDATE_JSON);
        Gson gson = new GsonBuilder().create();
        FirebaseAppUpdate appUpdate = gson.fromJson(appData, new TypeToken<FirebaseAppUpdate>(){}.getType());
        if(appUpdate != null) {
            List<FirebaseAppUpdate.AppDetails> appDataList = appUpdate.getAppData();
            for (int i = 0; i <= appDataList.size(); i++) {
                FirebaseAppUpdate.AppDetails details = appDataList.get(i);
                if (details.getAppName().equals(APP_NAME)) {
                    if (!details.getVersion().equals(getAppVersion(context))
                            && onUpdateNeededListener != null) {
                        onUpdateNeededListener.onUpdateNeeded(details.getDownloadUrl());
                        break;
                    } else {
                        onUpdateNeededListener.onAppUptoDate();
                        break;
                    }
                }
                else {
                    onUpdateNeededListener.onAppUptoDate();
                }

            }
        }
      //  onUpdateNeededListener.onAppUptoDate();
//
      /*  if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
            String currentVersion = remoteConfig.getString(KEY_CURRENT_VERSION);
            String appVersion = getAppVersion(context);
            String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);
            Log.e("app-version", appVersion);
            if (!TextUtils.equals(currentVersion, appVersion)
                    && onUpdateNeededListener != null) {
                onUpdateNeededListener.onUpdateNeeded(updateUrl);
            } else {
                onUpdateNeededListener.onAppUptoDate();
            }
        } else {
            onUpdateNeededListener.onAppUptoDate();
        }*/
    }

    private String getAppVersion(Context context) {
        String result = "";

        try {
            result = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return result;
    }

    public static class Builder {

        private final Context context;
        private OnUpdateNeededListener onUpdateNeededListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener);
        }

        public ForceUpdateChecker check() {
            ForceUpdateChecker forceUpdateChecker = build();
            forceUpdateChecker.check();

            return forceUpdateChecker;
        }
    }
}

class FirebaseAppUpdate{


    @SerializedName("app_data")
    @Expose
    private List<AppDetails> appData = null;

    public List<AppDetails> getAppData() {
        return appData;
    }

    public void setAppData(List<AppDetails> appData) {
        this.appData = appData;
    }

    public class AppDetails {

        @SerializedName("app_name")
        @Expose
        private String appName;
        @SerializedName("version")
        @Expose
        private String version;
        @SerializedName("download_url")
        @Expose
        private String downloadUrl;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

    }

}
