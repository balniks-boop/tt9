package io.github.sspanak.tt9.ui.main.clipboard;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

/**
 * The clipboard history: the last copied texts, newest at the top. Tapping one pastes it.
 * Each row shows what kind of text it is (link, e-mail, phone, number, text), the beginning
 * of the text, and when it was copied. The ✕ removes a single item.
 */
public class ClipboardPanel {
	private static final Pattern LINK = Pattern.compile("^(https?://|www\\.)\\S+$", Pattern.CASE_INSENSITIVE);
	private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
	private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 ()\\-]{6,}[0-9]$");
	private static final Pattern NUMBER = Pattern.compile("^[0-9 .,\\-+/€$%]+$");

	private static boolean confirmClear = false;


	@Nullable
	private static View getRoot(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.getMainView() != null ? tt9.getMainView().getView() : null;
	}


	public static boolean isShown(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		final View panel = root != null ? root.findViewById(R.id.clipboard_panel) : null;
		return panel != null && panel.getVisibility() == View.VISIBLE;
	}


	public static void toggle(@Nullable TraditionalT9 tt9) {
		if (isShown(tt9)) {
			hide(tt9);
		} else {
			show(tt9);
		}
	}


	public static void show(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		if (tt9 == null || root == null) {
			return;
		}

		final View keys = root.findViewById(R.id.main_soft_keys);
		final View panel = root.findViewById(R.id.clipboard_panel);
		if (keys == null || panel == null) {
			return;
		}

		io.github.sspanak.tt9.ui.main.emoji.EmojiPanel.hide(tt9);
		io.github.sspanak.tt9.ui.main.qwerty.QwertyPanel.hide(tt9);

		// what is in the clipboard right now goes to the top, even if it was copied before the keyboard started
		ClipboardHistory.captureCurrent(tt9);

		final int height = keys.getHeight();
		if (height > 0) {
			ViewGroup.LayoutParams params = panel.getLayoutParams();
			params.height = height;
			panel.setLayoutParams(params);
		}

		confirmClear = false;
		setup(tt9, root);
		fillList(tt9, root);

		keys.setVisibility(View.GONE);
		panel.setVisibility(View.VISIBLE);

		final ScrollView scroll = root.findViewById(R.id.clipboard_scroll);
		if (scroll != null) {
			scroll.scrollTo(0, 0);
		}

		tt9.getMainView().renderDynamicKeys();
	}


	public static void hide(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		if (root == null) {
			return;
		}

		final View keys = root.findViewById(R.id.main_soft_keys);
		final View panel = root.findViewById(R.id.clipboard_panel);
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


	private static GradientDrawable keyShape(@NonNull SettingsStore settings, boolean highlighted) {
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(18);
		final int fill = settings.getKeyBackgroundColor().getDefaultColor();
		shape.setColor(highlighted ? ColorUtils.blendARGB(fill, settings.getKeyTextColor(), 0.25f) : fill);
		final int border = settings.getKeyBorderColor().getDefaultColor();
		if (android.graphics.Color.alpha(border) > 0) {
			shape.setStroke(3, border);
		}
		return shape;
	}


	private static void styleButton(@NonNull TextView button, @NonNull SettingsStore settings) {
		button.setTextColor(settings.getKeyTextColor());
		button.setBackground(keyShape(settings, false));
	}


	private static void setup(@NonNull TraditionalT9 tt9, @NonNull View root) {
		final SettingsStore settings = tt9.getSettings();
		final TextView title = root.findViewById(R.id.clipboard_title);
		final TextView close = root.findViewById(R.id.clipboard_close);
		final TextView clear = root.findViewById(R.id.clipboard_clear);
		final TextView backspace = root.findViewById(R.id.clipboard_backspace);

		if (title != null) {
			title.setTextColor(settings.getKeyCornerElementColor());
		}
		if (close != null) {
			styleButton(close, settings);
			close.setOnClickListener(v -> { vibrate(tt9, v); hide(tt9); });
		}
		if (backspace != null) {
			styleButton(backspace, settings);
			backspace.setOnClickListener(v -> { vibrate(tt9, v); tt9.onBackspace(0); });
		}
		if (clear != null) {
			styleButton(clear, settings);
			clear.setText("Limpar tudo");
			clear.setOnClickListener(v -> {
				vibrate(tt9, v);
				if (ClipboardHistory.getAll(tt9).isEmpty()) {
					return;
				}
				if (!confirmClear) {
					// ask once more, so nothing is lost by accident
					confirmClear = true;
					clear.setText("Tocar de novo p/ apagar");
					clear.setBackground(keyShape(settings, true));
					return;
				}
				confirmClear = false;
				ClipboardHistory.clear(tt9);
				clear.setText("Limpar tudo");
				clear.setBackground(keyShape(settings, false));
				fillList(tt9, root);
			});
		}
	}


	private static void fillList(@NonNull TraditionalT9 tt9, @NonNull View root) {
		final LinearLayout list = root.findViewById(R.id.clipboard_list);
		if (list == null) {
			return;
		}
		list.removeAllViews();

		final SettingsStore settings = tt9.getSettings();
		final ArrayList<ClipboardHistory.Item> items = ClipboardHistory.getAll(tt9);
		final float density = list.getResources().getDisplayMetrics().density;

		if (items.isEmpty()) {
			TextView empty = new TextView(list.getContext());
			empty.setText("Ainda não há nada copiado.\n\nQuando copiar um texto (em qualquer aplicação), ele aparece aqui para colar mais tarde.");
			empty.setTextColor(settings.getKeyCornerElementColor());
			empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			empty.setGravity(Gravity.CENTER);
			final int pad = Math.round(16 * density);
			empty.setPadding(pad, pad, pad, pad);
			list.addView(empty, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			return;
		}

		for (ClipboardHistory.Item item : items) {
			list.addView(buildRow(tt9, root, item, density));
		}
	}


	@NonNull
	private static View buildRow(@NonNull TraditionalT9 tt9, @NonNull View root, @NonNull ClipboardHistory.Item item, float density) {
		final SettingsStore settings = tt9.getSettings();
		final android.content.Context context = root.getContext();

		LinearLayout row = new LinearLayout(context);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setGravity(Gravity.CENTER_VERTICAL);
		row.setBackground(keyShape(settings, false));
		final int pad = Math.round(8 * density);
		row.setPadding(pad, Math.round(5 * density), 0, Math.round(5 * density));
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rowParams.setMargins(Math.round(4 * density), Math.round(3 * density), Math.round(4 * density), Math.round(3 * density));
		row.setLayoutParams(rowParams);

		// kind icon
		TextView icon = new TextView(context);
		icon.setText(kindIcon(item.text));
		icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		icon.setGravity(Gravity.CENTER);
		row.addView(icon, new LinearLayout.LayoutParams(Math.round(34 * density), ViewGroup.LayoutParams.WRAP_CONTENT));

		// text + details
		LinearLayout texts = new LinearLayout(context);
		texts.setOrientation(LinearLayout.VERTICAL);
		texts.setPadding(Math.round(4 * density), 0, Math.round(4 * density), 0);

		TextView preview = new TextView(context);
		preview.setText(item.text.trim().replaceAll("\\s*\\n\\s*", " ⏎ "));
		preview.setTextColor(settings.getKeyTextColor());
		preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		preview.setMaxLines(2);
		preview.setEllipsize(TextUtils.TruncateAt.END);
		texts.addView(preview);

		TextView details = new TextView(context);
		details.setText(kindName(item.text) + " · " + describeTime(item.time) + (item.text.length() > 80 ? " · " + item.text.length() + " caracteres" : ""));
		details.setTextColor(settings.getKeyCornerElementColor());
		details.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
		details.setSingleLine(true);
		texts.addView(details);

		row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

		// remove one
		TextView remove = new TextView(context);
		remove.setText("✕");
		remove.setTextColor(settings.getKeyCornerElementColor());
		remove.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		remove.setGravity(Gravity.CENTER);
		row.addView(remove, new LinearLayout.LayoutParams(Math.round(44 * density), Math.round(44 * density)));
		remove.setOnClickListener(v -> {
			vibrate(tt9, v);
			ClipboardHistory.remove(tt9, item.text);
			fillList(tt9, root);
		});

		// tap = paste
		row.setOnClickListener(v -> {
			vibrate(tt9, v);
			paste(tt9, item.text);
		});

		return row;
	}


	private static void paste(@NonNull TraditionalT9 tt9, @NonNull String text) {
		try {
			tt9.getSuggestionOps().cancelDelayedAccept();
			final String lastWord = tt9.getSuggestionOps().acceptIncomplete();
			if (tt9.getInputMode() != null) {
				if (!lastWord.isEmpty()) {
					tt9.getInputMode().onAcceptSuggestion(lastWord);
				}
				tt9.getInputMode().reset();
			}
			tt9.getSuggestionOps().clear();
			tt9.getTextField().setText(text);
		} catch (Exception ignored) {
		}

		// move it to the top, it is the most recently used now
		ClipboardHistory.add(tt9, text);
		hide(tt9);
	}


	@NonNull
	private static String kindIcon(@NonNull String text) {
		final String t = text.trim();
		if (LINK.matcher(t).matches()) return "🔗";
		if (EMAIL.matcher(t).matches()) return "✉️";
		if (PHONE.matcher(t).matches()) return "📞";
		if (NUMBER.matcher(t).matches()) return "🔢";
		return "📝";
	}


	@NonNull
	private static String kindName(@NonNull String text) {
		final String t = text.trim();
		if (LINK.matcher(t).matches()) return "Link";
		if (EMAIL.matcher(t).matches()) return "E-mail";
		if (PHONE.matcher(t).matches()) return "Telefone";
		if (NUMBER.matcher(t).matches()) return "Número";
		return "Texto";
	}


	@NonNull
	private static String describeTime(long time) {
		if (time <= 0) return "";
		final long diff = System.currentTimeMillis() - time;
		final long minutes = diff / 60000;
		if (minutes < 1) return "agora";
		if (minutes < 60) return "há " + minutes + " min";

		Calendar then = Calendar.getInstance();
		then.setTimeInMillis(time);
		Calendar today = Calendar.getInstance();
		final boolean sameDay = then.get(Calendar.YEAR) == today.get(Calendar.YEAR) && then.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
		final String hour = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(time));
		if (sameDay) return "hoje, " + hour;

		today.add(Calendar.DAY_OF_YEAR, -1);
		final boolean yesterday = then.get(Calendar.YEAR) == today.get(Calendar.YEAR) && then.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
		if (yesterday) return "ontem, " + hour;

		return new SimpleDateFormat("d MMM, HH:mm", new Locale("pt", "PT")).format(new Date(time));
	}


	private static void vibrate(@NonNull TraditionalT9 tt9, @NonNull View view) {
		new Vibration(tt9.getSettings(), view).vibrate();
	}
}
