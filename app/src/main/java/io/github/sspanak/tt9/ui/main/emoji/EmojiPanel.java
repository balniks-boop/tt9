package io.github.sspanak.tt9.ui.main.emoji;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

/**
 * The full emoji picker. It is shown in place of the keys, with category tabs at the top,
 * the emoji grid in the middle, and "ABC", space and backspace at the bottom.
 */
public class EmojiPanel {
	private static final int CATEGORY_RECENT = 0;

	@Nullable private static ArrayList<String> currentList = null;


	@Nullable
	private static View getRoot(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.getMainView() != null ? tt9.getMainView().getView() : null;
	}


	public static boolean isShown(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		final View panel = root != null ? root.findViewById(R.id.emoji_panel) : null;
		return panel != null && panel.getVisibility() == View.VISIBLE;
	}


	public static void show(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		if (tt9 == null || root == null) {
			return;
		}

		final View keys = root.findViewById(R.id.main_soft_keys);
		final View panel = root.findViewById(R.id.emoji_panel);
		if (keys == null || panel == null) {
			return;
		}

		// forget the emoji list that is open on the suggestions bar, without typing anything
		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.getInputMode().reset();
		tt9.getSuggestionOps().clear();

		// take exactly the space of the keys, so the keyboard does not jump
		final int height = keys.getHeight();
		if (height > 0) {
			ViewGroup.LayoutParams params = panel.getLayoutParams();
			params.height = height;
			panel.setLayoutParams(params);
		}

		final SettingsStore settings = tt9.getSettings();
		setup(tt9, root, settings);

		keys.setVisibility(View.GONE);
		panel.setVisibility(View.VISIBLE);

		final ArrayList<String> recent = settings.getRecentEmojis();
		selectCategory(tt9, root, recent.isEmpty() ? 1 : CATEGORY_RECENT);

		// hide the "+" button, it is not needed while the panel is open
		tt9.getMainView().renderDynamicKeys();
	}


	public static void hide(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		if (root == null) {
			return;
		}

		final View keys = root.findViewById(R.id.main_soft_keys);
		final View panel = root.findViewById(R.id.emoji_panel);
		if (panel != null && panel.getVisibility() == View.VISIBLE) {
			panel.setVisibility(View.GONE);
			if (keys != null) {
				keys.setVisibility(View.VISIBLE);
			}
			if (tt9.getMainView() != null) {
				tt9.getMainView().renderDynamicKeys();
			}
		}
	}


	private static GradientDrawable keyShape(@NonNull SettingsStore settings, boolean selected) {
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(18);
		final int fill = settings.getKeyBackgroundColor().getDefaultColor();
		shape.setColor(selected ? ColorUtils.blendARGB(fill, settings.getKeyTextColor(), 0.25f) : fill);
		final int border = settings.getKeyBorderColor().getDefaultColor();
		if (ColorUtils.calculateLuminance(border) > 0 && android.graphics.Color.alpha(border) > 0) {
			shape.setStroke(3, border);
		}
		return shape;
	}


	private static void styleButton(@NonNull TextView button, @NonNull SettingsStore settings) {
		button.setTextColor(settings.getKeyTextColor());
		button.setBackground(keyShape(settings, false));
	}


	private static void setup(@NonNull TraditionalT9 tt9, @NonNull View root, @NonNull SettingsStore settings) {
		final TextView close = root.findViewById(R.id.emoji_close);
		final TextView space = root.findViewById(R.id.emoji_space);
		final TextView backspace = root.findViewById(R.id.emoji_backspace);
		final TextView title = root.findViewById(R.id.emoji_category_name);
		final LinearLayout tabs = root.findViewById(R.id.emoji_tabs);

		if (title != null) {
			title.setTextColor(settings.getKeyCornerElementColor());
		}

		if (close != null) {
			styleButton(close, settings);
			close.setOnClickListener(v -> { vibrate(tt9, v); hide(tt9); });
		}
		if (space != null) {
			styleButton(space, settings);
			space.setOnClickListener(v -> { vibrate(tt9, v); tt9.onText(" ", false); });
		}
		if (backspace != null) {
			styleButton(backspace, settings);
			backspace.setOnClickListener(v -> { vibrate(tt9, v); tt9.onBackspace(0); });
		}

		if (tabs != null) {
			tabs.removeAllViews();
			for (int i = 0; i < EmojiCatalog.ICONS.length; i++) {
				final int category = i;
				TextView tab = new TextView(tabs.getContext());
				tab.setText(EmojiCatalog.ICONS[i]);
				tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				tab.setGravity(Gravity.CENTER);
				tab.setTag("emoji_tab_" + i);
				final int width = Math.round(48 * tabs.getResources().getDisplayMetrics().density);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
				lp.setMargins(3, 3, 3, 3);
				tab.setLayoutParams(lp);
				tab.setOnClickListener(v -> { vibrate(tt9, v); selectCategory(tt9, root, category); });
				tabs.addView(tab);
			}
		}
	}


	private static void selectCategory(@NonNull TraditionalT9 tt9, @NonNull View root, int category) {
		final SettingsStore settings = tt9.getSettings();
		final LinearLayout tabs = root.findViewById(R.id.emoji_tabs);
		final TextView title = root.findViewById(R.id.emoji_category_name);
		final GridView grid = root.findViewById(R.id.emoji_grid);

		if (tabs != null) {
			for (int i = 0; i < tabs.getChildCount(); i++) {
				tabs.getChildAt(i).setBackground(keyShape(settings, i == category));
			}
		}

		if (title != null) {
			title.setText(EmojiCatalog.NAMES[category]);
		}

		currentList = category == CATEGORY_RECENT ? settings.getRecentEmojis() : EmojiCatalog.getCategory(category);

		if (grid != null) {
			grid.setAdapter(new EmojiAdapter(currentList, settings));
			grid.setOnItemClickListener((parent, view, position, id) -> {
				if (currentList == null || position >= currentList.size()) return;
				final String emoji = currentList.get(position);
				vibrate(tt9, view);
				tt9.onText(emoji, false);
				// the recent list is reordered only the next time it is opened, to avoid jumping around
				settings.addRecentEmoji(emoji);
			});
		}
	}


	private static void vibrate(@NonNull TraditionalT9 tt9, @NonNull View view) {
		new Vibration(tt9.getSettings(), view).vibrate();
	}


	private static class EmojiAdapter extends BaseAdapter {
		@NonNull private final ArrayList<String> emojis;
		private final int textColor;

		EmojiAdapter(@NonNull ArrayList<String> emojis, @NonNull SettingsStore settings) {
			this.emojis = new ArrayList<>(emojis);
			this.textColor = settings.getKeyTextColor();
		}

		@Override public int getCount() { return emojis.size(); }
		@Override public Object getItem(int position) { return emojis.get(position); }
		@Override public long getItemId(int position) { return position; }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView cell = convertView instanceof TextView ? (TextView) convertView : new TextView(parent.getContext());
			cell.setText(emojis.get(position));
			cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
			cell.setTextColor(textColor);
			cell.setGravity(Gravity.CENTER);
			final int size = Math.round(46 * parent.getResources().getDisplayMetrics().density);
			cell.setLayoutParams(new GridView.LayoutParams(size, size));
			cell.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
			return cell;
		}
	}
}
