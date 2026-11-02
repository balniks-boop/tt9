package io.github.sspanak.tt9.preferences.screens.appearance;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.colors.AbstractColorScheme;
import io.github.sspanak.tt9.colors.CollectionColorScheme;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.ScreenWithPreviewKeyboardHeaderFragment;
import io.github.sspanak.tt9.ui.UI;

/**
 * Themes, with the keyboard always visible below the list. Tapping a theme only shows it on the
 * keyboard; nothing changes until "Usar este tema" is tapped. Leaving without confirming keeps
 * the theme that was in use.
 */
public class ThemesScreen extends ScreenWithPreviewKeyboardHeaderFragment {
	public static final String NAME = "Themes";

	@Nullable private AbstractColorScheme selected;
	@Nullable private String savedId;

	public ThemesScreen() { super(); }
	public ThemesScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_themes; }
	@Override protected int getXml() { return R.xml.prefs_screen_themes; }


	@Override
	protected void onCreate() {
		super.onCreate();
		if (activity == null) {
			return;
		}

		savedId = activity.getSettings().getColorSchemeId();
		selected = CollectionColorScheme.get(activity, savedId);
		buildList();
		resetFontSize(true);
	}


	@Override
	public void onResume() {
		super.onResume();
		// show the keyboard straight away
		final View view = getView();
		if (view != null) {
			view.postDelayed(this::startPreview, 400);
		}
	}


	@Nullable
	@Override
	protected AbstractColorScheme getInitialPreviewScheme() {
		return selected;
	}


	private void buildList() {
		PreferenceCategory list = findPreference("pref_themes_list");
		if (list == null || activity == null) {
			return;
		}

		list.removeAll();
		for (AbstractColorScheme scheme : CollectionColorScheme.getAll(activity)) {
			Preference item = new Preference(activity);
			item.setKey("theme_" + scheme.getId());
			item.setIconSpaceReserved(false);
			item.setPersistent(false);
			item.setOnPreferenceClickListener(p -> {
				selected = scheme;
				refreshMarks();
				previewKeyboard();
				return true;
			});
			list.addPreference(item);
		}

		Preference confirm = findPreference("pref_themes_confirm");
		if (confirm != null) {
			confirm.setPersistent(false);
			confirm.setOnPreferenceClickListener(p -> {
				confirmSelection();
				return true;
			});
		}

		refreshMarks();
	}


	private void refreshMarks() {
		if (activity == null || selected == null) {
			return;
		}

		for (AbstractColorScheme scheme : CollectionColorScheme.getAll(activity)) {
			Preference item = findPreference("theme_" + scheme.getId());
			if (item == null) continue;

			final boolean isSelected = scheme.getId() == selected.getId();
			final boolean isSaved = String.valueOf(scheme.getId()).equals(savedId);
			item.setTitle((isSelected ? "●  " : "○  ") + scheme.getDisplayName());
			item.setSummary(isSaved ? "Em uso" : null);
		}

		Preference confirm = findPreference("pref_themes_confirm");
		if (confirm != null) {
			final boolean changed = !String.valueOf(selected.getId()).equals(savedId);
			confirm.setTitle(changed ? "✔  Usar este tema" : "✔  Tema em uso");
			confirm.setSummary(selected.getDisplayName());
			confirm.setEnabled(changed);
		}
	}


	private void confirmSelection() {
		if (activity == null || selected == null) {
			return;
		}

		activity.getSettings().setColorScheme(selected);
		savedId = String.valueOf(selected.getId());
		refreshMarks();
		UI.toast(activity, "Tema guardado: " + selected.getDisplayName());
	}


	@Override
	public void onPause() {
		super.onPause(); // stops the preview and goes back to the saved theme, if not confirmed
	}
}
