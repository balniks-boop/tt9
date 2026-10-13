package io.github.sspanak.tt9.preferences.screens.keypad;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownVibrationStrength extends EnhancedDropDownPreference {
	public static final String NAME = "pref_vibration_level";
	public static final int DEFAULT = 6;

	public DropDownVibrationStrength(@NonNull Context context) { super(context); init(); }
	public DropDownVibrationStrength(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
	public DropDownVibrationStrength(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }
	public DropDownVibrationStrength(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }

	private void init() {
		setTitle("Intensidade da vibração");
	}

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		add(0, "0 — vibração normal do sistema");
		for (int level = 1; level <= 10; level++) {
			String label = String.valueOf(level);
			if (level == 1) label += " — mais fraca";
			if (level == 10) label += " — mais forte";
			add(level, label);
		}
		commitOptions();

		setDefaultValue(String.valueOf(settings.getVibrationStrength()));
		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
