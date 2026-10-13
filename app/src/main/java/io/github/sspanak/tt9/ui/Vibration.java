package io.github.sspanak.tt9.ui;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.screens.keypad.DropDownVibrationType;
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

		final boolean isClick = settings.getVibrationType() == DropDownVibrationType.TYPE_CLICK;

		// Dry click: use the phone's own built-in click effects, which are short and strong by design.
		if (isClick && vibrateClick(vibrator, level, isHold)) {
			return true;
		}

		// soft: a longer, gentler buzz (21..75 ms); click: a short, strong, dry tap (12..30 ms)
		long duration = isClick ? 10 + level * 2L : 15 + level * 6L;
		if (isHold) {
			duration = duration * 3 / 2;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int amplitude = isClick ? Math.min(255, 130 + level * 13) : Math.min(255, 5 + level * 25);
			if (!vibrator.hasAmplitudeControl()) {
				amplitude = VibrationEffect.DEFAULT_AMPLITUDE;
			}
			vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
		} else {
			vibrator.vibrate(duration);
		}
		return true;
	}


	private boolean vibrateClick(@NonNull Vibrator vibrator, int level, boolean isHold) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
			final float scale = Math.min(1f, 0.1f * level + (isHold ? 0.2f : 0f));
			vibrator.vibrate(VibrationEffect.startComposition().addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale).compose());
			return true;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			final int effect = (level >= 6 || isHold) ? VibrationEffect.EFFECT_HEAVY_CLICK : (level <= 3 ? VibrationEffect.EFFECT_TICK : VibrationEffect.EFFECT_CLICK);
			vibrator.vibrate(VibrationEffect.createPredefined(effect));
			return true;
		}

		return false;
	}


	public void vibrate() {
		vibrate(getPressVibration(null));
	}
}
