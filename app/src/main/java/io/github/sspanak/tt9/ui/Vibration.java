package io.github.sspanak.tt9.ui;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.BaseClickableKey;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public record Vibration(@NonNull SettingsStore settings, @Nullable View view) {
	public static int getNoVibration() {
		return -1;
	}

	public static int getPressVibration(BaseClickableKey key) {
		// same short tap for every key; on many phones VIRTUAL_KEY is too weak or silent
		return HapticFeedbackConstants.KEYBOARD_TAP;
	}

	public static int getHoldVibration() {
		if (DeviceInfo.AT_LEAST_ANDROID_11) {
			return HapticFeedbackConstants.CONFIRM;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}

	public static int getReleaseVibration() {
		if (DeviceInfo.AT_LEAST_ANDROID_8_1) {
			return HapticFeedbackConstants.KEYBOARD_RELEASE;
		} else {
			return HapticFeedbackConstants.VIRTUAL_KEY;
		}
	}

	public void vibrate(int vibrationType) {
		if (view == null || !settings.getHapticFeedback()) {
			return;
		}

		try {
			final int strength = settings.getVibrationStrength();
			if (vibrationType == getNoVibration()) {
				return;
			}

			if (strength > 0 && vibrationType != getReleaseVibration() && vibrateCustom(strength, vibrationType == getHoldVibration())) {
				return;
			}

			view.performHapticFeedback(vibrationType, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		} catch (Exception e) {
			// Some Xiaomi and Poco devices with Android 16 crash when trying to vibrate.
			// This is a workaround to prevent the app from crashing on such devices.
			settings.setHapticFeedbackProblematic(true);

			Logger.e(getClass().getSimpleName(), "Failed to vibrate, disabling haptic feedback. " + e);
		}
	}

	/**
	 * Vibrate with the motor directly, so the strength can be adjusted in the settings.
	 * Level 1..10 -> 21..75 ms, and the amplitude grows with the level when the phone supports it.
	 */
	private boolean vibrateCustom(int level, boolean isHold) {
		if (view == null) {
			return false;
		}

		final Vibrator vibrator = view.getContext().getSystemService(Vibrator.class);
		if (vibrator == null || !vibrator.hasVibrator()) {
			return false;
		}


		long duration = 15 + level * 6L; // 21..75 ms
		if (isHold) {
			duration = duration * 3 / 2;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int amplitude = Math.min(255, 5 + level * 25);
			if (!vibrator.hasAmplitudeControl()) {
				amplitude = VibrationEffect.DEFAULT_AMPLITUDE;
			}
			vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
		} else {
			vibrator.vibrate(duration);
		}
		return true;
	}


	public void vibrate() {
		vibrate(getPressVibration(null));
	}
}
