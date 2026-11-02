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
 * - The recent symbols and emojis are always on the bar while nothing is typed.
 * - Tap: symbols -> emojis -> back to the recent ones. Nothing is typed until picked.
 *   Each list has a "+" at the right of the bar, which opens the full panel with tabs.
 * - Hold: opens the full panel on the symbols.
 */
public class SoftKeySymbols extends BaseSoftKeyCustomizable {
	private static final int MAX_BACKSPACES = 20;
	private static final int STAGE_SYMBOLS = 1;
	private static final int STAGE_MORE_SYMBOLS = 2;
	private static final int STAGE_EMOJIS = 3;
	private int stage = 0;
	private boolean returnToAbc = false;
	private boolean held = false;
	private final CmdShowEmojis showEmojis = new CmdShowEmojis();

	public SoftKeySymbols(Context context) { super(context); }
	public SoftKeySymbols(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeySymbols(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected boolean handlePress() {
		held = false;
		return super.handlePress();
	}


	@Override
	protected void handleHold() {
		preventRepeat();
		held = true;
		io.github.sspanak.tt9.ui.main.emoji.EmojiPanel.show(tt9, io.github.sspanak.tt9.ui.main.emoji.EmojiCatalog.TAB_SYMBOLS);
	}


	@Override
	protected boolean handleRelease() {
		if (!validateTT9Handler()) {
			return false;
		}

		// a long press already opened the panel
		if (held) {
			held = false;
			return true;
		}

		// In ABC mode, the key 1 list disappears automatically after a moment, so we briefly switch
		// to T9 to show the full list, and return to ABC once a symbol has been chosen.
		if (InputModeKind.isABC(tt9.getInputMode()) && tt9.getAllowedInputModes().contains(InputMode.MODE_PREDICTIVE)) {
			tt9.setInputMode(InputMode.MODE_PREDICTIVE);
			returnToAbc = true;
		}

		// show the lists without writing anything, until a symbol or an emoji is picked
		io.github.sspanak.tt9.ime.SuggestionHandler.hideSymbolPreview = true;

		final InputMode mode = tt9.getInputMode();
		final boolean isOpen = mode != null && InputModeKind.isPredictive(mode) && mode.getFirstKey() == 1;

		// The recent symbols and emojis are always on the bar while nothing is typed.
		// 1st tap: symbols
		if (!isOpen) {
			final boolean result = pressKey1();
			refreshPlusButton();
			return result;
		}

		// (older list of recent ones on key 1) -> symbols
		if (mode.isRecentCharsSequence()) {
			tt9.onBackspace(0);
			refreshPlusButton();
			return true;
		}

		// 2nd tap: symbols -> emojis
		if (!mode.isEmojiSequence()) {
			final boolean result = pressKey1();
			refreshPlusButton();
			return result;
		}

		// 3rd tap: close the lists, without typing anything, so the recent ones are back
		for (int i = 0; i < MAX_BACKSPACES; i++) {
			final InputMode m = tt9.getInputMode();
			if (m == null || m.getFirstKey() != 1) break;
			tt9.onBackspace(0);
		}
		refreshPlusButton();
		return true;
	}


	private void refreshPlusButton() {
		post(() -> { if (tt9 != null && tt9.getMainView() != null) tt9.getMainView().renderDynamicKeys(); });
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
		io.github.sspanak.tt9.ui.main.emoji.RecentCharsBar.attach(tt9);
		io.github.sspanak.tt9.ui.main.emoji.RecentCharsBar.update(tt9);

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
