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

		final InputMode mode = tt9.getInputMode();
		final boolean isSymbolSequence = mode != null && InputModeKind.isPredictive(mode) && mode.getFirstKey() == 1;

		// nothing open yet -> "symbols"
		if (!isSymbolSequence) {
			stage = STAGE_SYMBOLS;
			return pressKey1();
		}

		// "symbols" -> "more symbols"
		if (mode.getSequenceLength() == 1) {
			stage = STAGE_MORE_SYMBOLS;
			return mode.onReplaceSuggestion(SuggestionsBar.SHOW_GROUP_1_SUGGESTION);
		}

		// "more symbols" -> emojis
		if (stage == STAGE_MORE_SYMBOLS) {
			backToSymbols(mode);
			stage = STAGE_EMOJIS;
			return pressKey1();
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
	protected float getTitleScale() {
		return super.getTitleScale() * 0.8f;
	}


	@Override
	protected String getAccessibilityText() {
		return "Símbolos";
	}
}
