package io.github.sspanak.tt9.preferences.screens.keypad;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownVibrationType extends EnhancedDropDownPreference {
	public static final String NAME = "pref_vibration_type";
	public static final int TYPE_SOFT = 0;  // longer, gentler buzz
	public static final int TYPE_CLICK = 1; // short, strong, dry tap
	public static final int DEFAULT = TYPE_SOFT;

	public DropDownVibrationType(@NonNull Context context) { super(context); init(); }
	public DropDownVibrationType(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
	public DropDownVibrationType(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }
	public DropDownVibrationType(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }

	private void init() {
		setTitle("Tipo de vibração");
	}

	@Override
	public EnhancedDropDownPreference populate(@NonNull SettingsStore settings) {
		add(TYPE_SOFT, "Suave (zumbido)");
		add(TYPE_CLICK, "Seco e forte (clique)");
		commitOptions();

		setDefaultValue(String.valueOf(settings.getVibrationType()));
		return this;
	}

	@Override
	protected String getName() {
		return NAME;
	}
}
