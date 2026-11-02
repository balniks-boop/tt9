package io.github.sspanak.tt9.ui.main.emoji;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.ui.main.clipboard.ClipboardPanel;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

/**
 * The recently used symbols and emojis, always ready on the suggestions bar while nothing is
 * being typed, so the bar is never just empty. Tapping one types it.
 */
public class RecentCharsBar {
	@NonNull private static WeakReference<TraditionalT9> ref = new WeakReference<>(null);
	private static boolean wasShown = false;


	public static void attach(@Nullable TraditionalT9 tt9) {
		if (tt9 != null && ref.get() != tt9) {
			ref = new WeakReference<>(tt9);
			wasShown = false;
		}
	}


	/** Called whenever the suggestions change. */
	public static void onSuggestionsChanged() {
		update(ref.get());
	}


	public static boolean shouldShow(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.getMainView() == null || tt9.isInputModeNumeric()) {
			return false;
		}
		final InputMode mode = tt9.getInputMode();
		if (mode == null || mode.getSequenceLength() > 0) {
			return false;
		}
		return tt9.getSuggestionOps().isEmpty()
			&& !EmojiPanel.isShown(tt9)
			&& !ClipboardPanel.isShown(tt9)
			&& !tt9.getSettings().getRecentChars().isEmpty();
	}


	public static void update(@Nullable TraditionalT9 tt9) {
		final View root = tt9 != null && tt9.getMainView() != null ? tt9.getMainView().getView() : null;
		final View bar = root != null ? root.findViewById(R.id.recent_chars_bar) : null;
		if (bar == null) {
			return;
		}

		final boolean show = shouldShow(tt9);
		if (show && !wasShown) {
			// rebuilt only when it appears, so it does not jump around while picking several symbols
			fill(tt9, root);
			if (bar instanceof HorizontalScrollView) {
				bar.scrollTo(0, 0);
			}
		}
		wasShown = show;
		if (show) {
			bar.setBackgroundColor(tt9.getSettings().getKeyboardBackground());
		}
		bar.setVisibility(show ? View.VISIBLE : View.GONE);

		// the "+" at the right of the bar follows the recent list
		final View plus = root.findViewById(R.id.soft_key_emoji_more);
		if (plus instanceof SoftKey) {
			((SoftKey) plus).render();
		}
	}


	private static void fill(@NonNull TraditionalT9 tt9, @NonNull View root) {
		final LinearLayout list = root.findViewById(R.id.recent_chars_list);
		if (list == null) {
			return;
		}
		list.removeAllViews();

		final SettingsStore settings = tt9.getSettings();
		final ArrayList<String> chars = settings.getRecentChars();
		final float density = list.getResources().getDisplayMetrics().density;
		final int width = Math.round(44 * density);

		for (String ch : chars) {
			TextView cell = new TextView(list.getContext());
			cell.setText(ch);
			cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
			cell.setTextColor(settings.getKeyboardTextColor());
			cell.setGravity(Gravity.CENTER);
			cell.setBackgroundColor(Color.TRANSPARENT);
			cell.setOnClickListener(v -> {
				new Vibration(settings, v).vibrate();
				tt9.onText(ch, false);
				settings.addRecentChar(ch);
				if (EmojiCatalog.isEmoji(ch)) {
					settings.addRecentEmoji(ch);
				}
			});
			list.addView(cell, new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
		}
	}
}
