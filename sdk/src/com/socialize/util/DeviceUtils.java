/*
 * Copyright (c) 2011 Socialize Inc. 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.util;

import java.util.List;
import java.util.Locale;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;

import com.socialize.Socialize;
import com.socialize.log.SocializeLogger;

/**
 * @author Jason Polites
 */
public class DeviceUtils {

	private SocializeLogger logger;
	private String userAgent;
	private float density = 160.0f;
	private String packageName;
	private boolean hasCamera;

	public void init(Context context) {
		if (context instanceof Activity) {
			DisplayMetrics metrics = new DisplayMetrics();
			Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
			display.getMetrics(metrics);
			density = metrics.density;
			packageName = context.getPackageName();
			hasCamera = isIntentAvailable(context, MediaStore.ACTION_IMAGE_CAPTURE);
		}
		else {
			String errroMsg = "Unable to determine device screen density.  Socialize must be intialized from an Activity";
			if (logger != null) {
				logger.warn(errroMsg);
			}
			else {
				System.err.println(errroMsg);
			}
		}
	}

	public boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public int getDIP(int pixels) {
		if (pixels != 0) {
			return (int) ((float) pixels * density);
		}
		return pixels;
	}

	public String getUDID(Context context) {
		if (hasPermission(context, permission.READ_PHONE_STATE)) {
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			String deviceId = tManager.getDeviceId();

			if (StringUtils.isEmpty(deviceId)) {
				if (logger != null) {
					logger.warn("Unable to determine device UDID, reverting to " + Secure.ANDROID_ID);
				}
				deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			}

			return deviceId;
		}
		else {
			// this is fatal
			if (logger != null) {
				logger.error(SocializeLogger.NO_UDID);
			}

			return null;
		}
	}

	public String getUserAgentString() {
		if (userAgent == null) {
			userAgent = "Android-" + android.os.Build.VERSION.SDK_INT + "/" + android.os.Build.MODEL + " SocializeSDK/v" + Socialize.VERSION + "; " + Locale.getDefault().getLanguage() + "_"
					+ Locale.getDefault().getCountry() + "; BundleID/" + packageName;
		}
		return userAgent;
	}

	public boolean hasPermission(Context context, String permission) {
		return context.getPackageManager().checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}

	public boolean hasCamera() {
		return hasCamera;
	}

	public SocializeLogger getLogger() {
		return logger;
	}

	public void setLogger(SocializeLogger logger) {
		this.logger = logger;
	}
}
