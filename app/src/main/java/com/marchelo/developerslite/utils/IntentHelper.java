package com.marchelo.developerslite.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import androidx.annotation.CheckResult;
import androidx.annotation.StringRes;
import android.util.Log;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.network.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Green
 * @since 30.08.15
 */
public final class IntentHelper {
    private static final String TAG = IntentHelper.class.getSimpleName();

    private IntentHelper() {}

    @CheckResult
    public static boolean sharePostLink(Context context, long postId) {
        String postUrl = Constants.WEB_SERVICE_BASE_URL + "/" + postId;
        return shareLink(context, postUrl);
    }

    @CheckResult
    public static boolean shareImageLink(Context context, String imageUrl) {
        return shareLink(context, imageUrl);
    }

    private static boolean shareLink(Context context, String url) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(sharingIntent);
        } catch(ActivityNotFoundException e) {
            Log.d(TAG, "shareLink(), no activity found to handle intent [" + sharingIntent + "]");
            return false;
        }
        return true;
    }

    @CheckResult
    public static boolean openPostWebLinkExcludeSelf(Context context, long postId) {
        String postUrl = Constants.WEB_SERVICE_BASE_URL + "/" + postId;

        Intent openSite = new Intent();
        openSite.setAction(Intent.ACTION_VIEW);
        openSite.setData(Uri.parse(postUrl));
        openSite.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return openForIntentExcludeSelf(context, openSite, "Open post web link:");
    }

    @CheckResult
    public static boolean openWebLinkExcludeSelf(Context context, Uri uri) {
        Intent openSite = new Intent();
        openSite.setAction(Intent.ACTION_VIEW);
        openSite.setData(uri);

        return openForIntentExcludeSelf(context, openSite, "Open web link:");
    }

    @CheckResult
    public static boolean openForIntentExcludeSelf(Context context, Intent intent, String msg) {
        List<Intent> targetIntents = IntentHelper.excludeSelfFromIntent(context, intent);

        if (targetIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), msg);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(
                    new Parcelable[targetIntents.size()]));
            context.startActivity(chooserIntent);
            return true;

        }
        return false;
    }

    public static List<Intent> excludeSelfFromIntent(Context context, Intent intent) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
        String packageNameOfAppToHide = context.getPackageName();
        List<Intent> targetIntents = new ArrayList<>();
        for (ResolveInfo currentInfo : resInfoList) {
            String packageName = currentInfo.activityInfo.packageName;
            if (!packageNameOfAppToHide.equals(packageName)) {
                Log.d(TAG, "excludeSelfFromIntent(), adding app [" + packageName + "]");
                Intent targetIntent = new Intent(intent);
                targetIntent.setPackage(packageName);
                targetIntents.add(targetIntent);
            } else {
                Log.d(TAG, "excludeSelfFromIntent(), excluding app [" + packageNameOfAppToHide + "]");
            }
        }
        return targetIntents;
    }

    public static void sendIntentExcludingThisApp(Activity activity, Intent sourceIntent, @StringRes int StringId) {
        Log.d(TAG, "sendIntentExcludingThisApp(), source intent = " + sourceIntent);

        Intent intent = new Intent(sourceIntent);
        intent.setComponent(null);
        Log.d(TAG, "sendIntentExcludingThisApp(), new intent = " + intent);

        String appCantShowLink = activity.getString(StringId, activity.getString(R.string.app_name));

        if (IntentHelper.openForIntentExcludeSelf(activity, intent, appCantShowLink)) {
            Log.d(TAG, "sendIntentExcludingThisApp(), found apps to handle intent");
            activity.finish();

        } else {
            Log.d(TAG, "sendIntentExcludingThisApp(), no appropriate app found to handle intent");
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage(appCantShowLink)
                    .setNegativeButton(R.string.dialog_close, (dialog, which) -> {
                        activity.finish();
                    })
                    .create()
                    .show();
        }
    }

    public static void onNoActivityFoundToHandleIntent(Context context, @StringRes int stringId) {
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setMessage(stringId)
                .setPositiveButton(R.string.dialog_ok, null)
                .create()
                .show();
    }
}
