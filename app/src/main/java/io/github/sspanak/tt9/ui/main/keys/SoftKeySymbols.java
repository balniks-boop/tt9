package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.commands.CmdShowEmojis;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;

/**
 * "SYM" key, inspired by the TyNi T9 layout.
 * - Tap: cycles "symbols" -> "more symbols" -> emojis -> "symbols"...
 *   Backspace closes the list without typing anything.
 * - Hold: opens the emoji list.
 */
public class SoftKeySymbols extends BaseSoftKeyCustomizable {
	private static final int MAX_BACKSPACES = 20;
	private static final int STAGE_SYMBOLS = 1;
	private static final int STAGE_MORE_SYMBOLS = 2;
	private static final int STAGE_EMOJIS = 3;
	private int stage = 0;
	private boolean returnToAbc = false;
	private final CmdShowEmojis showEmojis = new CmdShowEmojis();

	public SoftKeySymbols(Context context) { super(context); }
	public SoftKeySymbols(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeySymbols(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected void handleHold() {
		preventRepeat();
		showEmojis.run(tt9);
	}


	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		// In ABC mode, the key 1 list disappears automatically after a moment, so we briefly switch
		// to T9 to show the full list, and return to ABC once a symbol has been chosen.
		if (InputModeKind.isABC(tt9.getInputMode()) && tt9.getAllowedInputModes().contains(InputMode.MODE_PREDICTIVE)) {
			tt9.setInputMode(InputMode.MODE_PREDICTIVE);
			returnToAbc = true;
		}

		final InputMode mode = tt9.getInputMode();
		final boolean isSymbolSequence = mode != null && InputModeKind.isPredictive(mode) && mode.getFirstKey() == 1;

		// nothing open yet -> "symbols"
		if (!isSymbolSequence) {
			stage = STAGE_SYMBOLS;
			return pressKey1();
		}

		// "symbols" -> "more symbols", but only if there are more symbols. Otherwise, the list would be
		// empty and only a dark, empty bar would show up, so we go straight to the emojis.
		final boolean hasMoreSymbols = tt9.getLanguage() != null
			&& !tt9.getSettings().getCharsExtra(tt9.getLanguage(), io.github.sspanak.tt9.preferences.settings.SettingsStore.CHARS_GROUP_1).isEmpty();
		if (mode.getSequenceLength() == 1 && hasMoreSymbols) {
			stage = STAGE_MORE_SYMBOLS;
			return mode.onReplaceSuggestion(SuggestionsBar.SHOW_GROUP_1_SUGGESTION);
		}

		// "more symbols" (or "symbols") -> emojis
		if (stage == STAGE_MORE_SYMBOLS || (mode.getSequenceLength() == 1 && !hasMoreSymbols)) {
			backToSymbols(mode);
			stage = STAGE_EMOJIS;
			final boolean result = pressKey1();
			post(() -> { if (tt9 != null && tt9.getMainView() != null) tt9.getMainView().renderDynamicKeys(); });
			return result;
		}

		// emojis -> back to "symbols"
		backToSymbols(mode);
		stage = STAGE_SYMBOLS;
		return true;
	}


	private void backToSymbols(@NonNull InputMode mode) {
		for (int i = 0; i < MAX_BACKSPACES && mode.getFirstKey() == 1 && mode.getSequenceLength() > 1; i++) {
			tt9.onBackspace(0);
		}
	}


	private boolean pressKey1() {
		int keyCode = Key.numberToCode(tt9.getSettings(), 1);
		if (keyCode < 0) {
			return false;
		}

		tt9.onKeyDown(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		tt9.onKeyUp(keyCode, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
		return true;
	}


	@Override
	protected String getTitle() {
		return "SYM";
	}


	@Override
	public boolean isDynamic() {
		return true;
	}


	@Override
	public void render() {
		super.render();

		// a symbol was chosen (or the list was closed) -> back to ABC
		if (returnToAbc && tt9 != null) {
			final InputMode mode = tt9.getInputMode();
			if (mode == null || !InputModeKind.isPredictive(mode)) {
				returnToAbc = false;
			} else if (mode.getFirstKey() != 1) {
				// check again a moment later: right after opening, the list may not be loaded yet
				post(() -> {
					if (!returnToAbc || tt9 == null) return;
					final InputMode current = tt9.getInputMode();
					if (current != null && InputModeKind.isPredictive(current) && current.getFirstKey() != 1) {
						returnToAbc = false;
						tt9.setInputMode(InputMode.MODE_ABC);
					}
				});
			}
		}
	}


	@Override
	protected float getTitleScale() {
		return super.getTitleScale() * 0.8f;
	}


	@Override
	protected String getAccessibilityText() {
		return "Símbolos";
	}
}
