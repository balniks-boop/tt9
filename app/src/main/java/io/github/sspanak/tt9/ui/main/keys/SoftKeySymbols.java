package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import io.github.sspanak.tt9.commands.CmdShowEmojis;
import io.github.sspanak.tt9.ime.helpers.Key;

/**
 * "SYM" key, inspired by the TyNi T9 layout. A tap opens the punctuation and symbols list
 * (same as tapping the 1-key). Holding it opens the emoji list.
 */
public class SoftKeySymbols extends BaseSoftKeyCustomizable {
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
		int keyCode = Key.numberToCode(tt9 != null ? tt9.getSettings() : null, 1);
		if (keyCode < 0 || !validateTT9Handler()) {
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
