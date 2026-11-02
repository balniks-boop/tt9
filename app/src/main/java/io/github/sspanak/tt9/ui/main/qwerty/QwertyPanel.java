package io.github.sspanak.tt9.ui.main.qwerty;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;

/**
 * A full keyboard, with one key per letter (QWERTY), inside this same keyboard. It is shown in
 * place of the T9 keys, with the same colors. Letters are typed directly, without predictions.
 * - Hold a letter (a, e, i, o, u, c, n, s): its accented versions appear on the top row.
 * - ⇧: next letter in capitals; tap twice quickly for all capitals.
 * - "T9" goes back to the T9 keys.
 */
public class QwertyPanel {
	private static final String[][] ROWS = {
		{"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"},
		{"a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"},
	};
	private static final String[] ROW3 = {"z", "x", "c", "v", "b", "n", "m"};
	private static final HashMap<String, String[]> ACCENTS = new HashMap<>();
	static {
		ACCENTS.put("a", new String[]{"á", "à", "â", "ã", "ä"});
		ACCENTS.put("e", new String[]{"é", "ê", "è", "ë"});
		ACCENTS.put("i", new String[]{"í", "ì", "î", "ï"});
		ACCENTS.put("o", new String[]{"ó", "ô", "õ", "ò", "ö"});
		ACCENTS.put("u", new String[]{"ú", "ù", "û", "ü"});
		ACCENTS.put("c", new String[]{"ç", "ć"});
		ACCENTS.put("n", new String[]{"ñ"});
		ACCENTS.put("s", new String[]{"ß"});
	}

	private static final int SHIFT_OFF = 0;
	private static final int SHIFT_ONCE = 1;
	private static final int SHIFT_LOCK = 2;

	private static int shift = SHIFT_OFF;
	private static long lastShiftTap = 0;
	private static final ArrayList<TextView> letterKeys = new ArrayList<>();
	@Nullable private static TextView shiftKey;
	@Nullable private static LinearLayout accentRow;
	private static final Handler repeat = new Handler(Looper.getMainLooper());


	@Nullable
	private static View getRoot(@Nullable TraditionalT9 tt9) {
		return tt9 != null && tt9.getMainView() != null ? tt9.getMainView().getView() : null;
	}


	public static boolean isShown(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		final View panel = root != null ? root.findViewById(R.id.qwerty_panel) : null;
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
		final LinearLayout panel = root.findViewById(R.id.qwerty_panel);
		if (keys == null || panel == null) {
			return;
		}

		io.github.sspanak.tt9.ui.main.emoji.EmojiPanel.hide(tt9);
		io.github.sspanak.tt9.ui.main.clipboard.ClipboardPanel.hide(tt9);
		finishT9Word(tt9);

		final int height = keys.getHeight();
		if (height > 0) {
			ViewGroup.LayoutParams params = panel.getLayoutParams();
			params.height = height;
			panel.setLayoutParams(params);
		}

		build(tt9, panel);
		autoCapitalize(tt9);

		keys.setVisibility(View.GONE);
		panel.setVisibility(View.VISIBLE);
		tt9.getMainView().renderDynamicKeys();
	}


	public static void hide(@Nullable TraditionalT9 tt9) {
		final View root = getRoot(tt9);
		if (root == null) {
			return;
		}

		repeat.removeCallbacksAndMessages(null);
		final View keys = root.findViewById(R.id.main_soft_keys);
		final View panel = root.findViewById(R.id.qwerty_panel);
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


	/** Accepts the word that T9 may still be composing, so nothing is lost or doubled. */
	private static void finishT9Word(@NonNull TraditionalT9 tt9) {
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
		} catch (Exception ignored) {
		}
	}


	// ---------- building ----------

	private static GradientDrawable keyShape(@NonNull SettingsStore settings, boolean highlighted) {
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(14);
		final int fill = settings.getKeyBackgroundColor().getDefaultColor();
		shape.setColor(highlighted ? ColorUtils.blendARGB(fill, settings.getKeyTextColor(), 0.3f) : fill);
		final int border = settings.getKeyBorderColor().getDefaultColor();
		if (android.graphics.Color.alpha(border) > 0) {
			shape.setStroke(3, border);
		}
		return shape;
	}


	private static void build(@NonNull TraditionalT9 tt9, @NonNull LinearLayout panel) {
		panel.removeAllViews();
		letterKeys.clear();
		final SettingsStore settings = tt9.getSettings();

		// top row: accents, after holding a letter
		accentRow = new LinearLayout(panel.getContext());
		accentRow.setOrientation(LinearLayout.HORIZONTAL);
		accentRow.setGravity(Gravity.CENTER_VERTICAL);
		panel.addView(accentRow, rowParams(0.75f));
		showAccentHint(tt9);

		for (String[] row : ROWS) {
			LinearLayout line = newRow(panel);
			for (String letter : row) {
				addLetter(tt9, line, letter);
			}
		}

		LinearLayout line3 = newRow(panel);
		shiftKey = addKey(tt9, line3, "⇧", 1.5f, true, v -> onShift(tt9));
		for (String letter : ROW3) {
			addLetter(tt9, line3, letter);
		}
		TextView backspace = addKey(tt9, line3, "⌫", 1.5f, true, null);
		setRepeating(tt9, backspace, () -> tt9.onBackspace(0));

		LinearLayout line4 = newRow(panel);
		addKey(tt9, line4, "T9", 1.5f, true, v -> hide(tt9));
		addKey(tt9, line4, ",", 1f, true, v -> type(tt9, ","));
		TextView space = addKey(tt9, line4, "␣", 5f, false, v -> type(tt9, " "));
		space.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		addKey(tt9, line4, ".", 1f, true, v -> type(tt9, "."));
		TextView ok = addKey(tt9, line4, "OK", 1.5f, true, v -> {
			if (!tt9.onOK()) {
				tt9.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			}
			autoCapitalize(tt9);
		});
		ok.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		updateLetters(settings);
	}


	private static LinearLayout.LayoutParams rowParams(float weight) {
		return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, weight);
	}


	@NonNull
	private static LinearLayout newRow(@NonNull LinearLayout panel) {
		LinearLayout row = new LinearLayout(panel.getContext());
		row.setOrientation(LinearLayout.HORIZONTAL);
		panel.addView(row, rowParams(1f));
		return row;
	}


	@NonNull
	private static TextView addKey(@NonNull TraditionalT9 tt9, @NonNull LinearLayout row, @NonNull String label, float weight, boolean fn, @Nullable View.OnClickListener onClick) {
		final SettingsStore settings = tt9.getSettings();
		TextView key = new TextView(row.getContext());
		key.setText(label);
		key.setGravity(Gravity.CENTER);
		key.setTextColor(settings.getKeyTextColor());
		key.setTextSize(TypedValue.COMPLEX_UNIT_SP, fn ? 18 : 20);
		key.setBackground(keyShape(settings, false));
		key.setSingleLine(true);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
		final int m = Math.round(2 * row.getResources().getDisplayMetrics().density);
		lp.setMargins(m, m + 1, m, m + 1);
		row.addView(key, lp);
		if (onClick != null) {
			key.setOnClickListener(v -> { vibrate(tt9, v); onClick.onClick(v); });
		}
		return key;
	}


	private static void addLetter(@NonNull TraditionalT9 tt9, @NonNull LinearLayout row, @NonNull String letter) {
		TextView key = addKey(tt9, row, letter, 1f, false, v -> {
			type(tt9, ((TextView) v).getText().toString());
			if (shift == SHIFT_ONCE) {
				shift = SHIFT_OFF;
				updateLetters(tt9.getSettings());
			}
		});
		key.setTag(letter);
		letterKeys.add(key);

		if (ACCENTS.containsKey(letter)) {
			key.setOnLongClickListener(v -> {
				vibrate(tt9, v);
				showAccents(tt9, letter);
				return true;
			});
		}
	}


	// ---------- behaviour ----------

	private static void type(@NonNull TraditionalT9 tt9, @NonNull String text) {
		tt9.getTextField().setText(text);
		if (text.equals(".") || text.equals(" ")) {
			autoCapitalize(tt9);
		}
	}


	/** Capital letter at the start of the text and after ". ", "! " or "? ". */
	private static void autoCapitalize(@NonNull TraditionalT9 tt9) {
		if (shift == SHIFT_LOCK) {
			return;
		}
		String before = "";
		try {
			before = tt9.getTextField().getStringBeforeCursor(3);
		} catch (Exception ignored) {}
		final String trimmed = before.replaceAll("\\s+$", "");
		final boolean afterSentence = before.endsWith(" ") && trimmed.matches(".*[.!?]$");
		final boolean sentenceStart = trimmed.isEmpty() || afterSentence || before.endsWith("\n");
		shift = sentenceStart ? SHIFT_ONCE : SHIFT_OFF;
		updateLetters(tt9.getSettings());
	}


	private static void onShift(@NonNull TraditionalT9 tt9) {
		final long now = System.currentTimeMillis();
		if (shift == SHIFT_LOCK) {
			shift = SHIFT_OFF;
		} else if (shift == SHIFT_ONCE && now - lastShiftTap < 400) {
			shift = SHIFT_LOCK;
		} else {
			shift = shift == SHIFT_OFF ? SHIFT_ONCE : SHIFT_OFF;
		}
		lastShiftTap = now;
		updateLetters(tt9.getSettings());
	}


	private static void updateLetters(@NonNull SettingsStore settings) {
		final boolean upper = shift != SHIFT_OFF;
		for (TextView key : letterKeys) {
			final String letter = String.valueOf(key.getTag());
			key.setText(upper ? letter.toUpperCase() : letter);
		}
		if (shiftKey != null) {
			shiftKey.setText(shift == SHIFT_LOCK ? "⇪" : "⇧");
			shiftKey.setBackground(keyShape(settings, shift != SHIFT_OFF));
		}
	}


	private static void showAccentHint(@NonNull TraditionalT9 tt9) {
		if (accentRow == null) return;
		accentRow.removeAllViews();
		TextView hint = new TextView(accentRow.getContext());
		hint.setText("Mantenha uma vogal (ou c, n) para acentos");
		hint.setTextColor(tt9.getSettings().getKeyCornerElementColor());
		hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		hint.setGravity(Gravity.CENTER);
		accentRow.addView(hint, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}


	private static void showAccents(@NonNull TraditionalT9 tt9, @NonNull String letter) {
		final String[] options = ACCENTS.get(letter);
		if (accentRow == null || options == null) return;
		accentRow.removeAllViews();
		final boolean upper = shift != SHIFT_OFF;
		for (String accented : options) {
			final String text = upper ? accented.toUpperCase() : accented;
			addKey(tt9, accentRow, text, 1f, false, v -> {
				type(tt9, text);
				if (shift == SHIFT_ONCE) {
					shift = SHIFT_OFF;
					updateLetters(tt9.getSettings());
				}
				showAccentHint(tt9);
			});
		}
		addKey(tt9, accentRow, "✕", 1f, true, v -> showAccentHint(tt9));
	}


	@SuppressLint("ClickableViewAccessibility")
	private static void setRepeating(@NonNull TraditionalT9 tt9, @NonNull TextView key, @NonNull Runnable action) {
		final Runnable[] loop = new Runnable[1];
		loop[0] = () -> { action.run(); repeat.postDelayed(loop[0], 60); };
		key.setOnTouchListener((v, event) -> {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					vibrate(tt9, v);
					v.setPressed(true);
					action.run();
					repeat.postDelayed(loop[0], 450);
					return true;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					v.setPressed(false);
					repeat.removeCallbacksAndMessages(null);
					autoCapitalize(tt9);
					return true;
				default:
					return true;
			}
		});
	}


	private static void vibrate(@NonNull TraditionalT9 tt9, @NonNull View view) {
		new Vibration(tt9.getSettings(), view).vibrate();
	}
}
