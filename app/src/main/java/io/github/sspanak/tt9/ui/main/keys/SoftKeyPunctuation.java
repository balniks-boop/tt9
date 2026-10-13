package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.chars.Characters;

/**
 * A function-column key that types a single fixed character, like "!", "?" or "@".
 * Added to mimic the TyNi T9 layout, which has these keys on the left column.
 */
public class SoftKeyPunctuation extends BaseSoftKeyCustomizable {
	public SoftKeyPunctuation(Context context) { super(context); }
	public SoftKeyPunctuation(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyPunctuation(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	private String getRawChar() {
		final int id = getId();
		if (id == R.id.soft_key_extra_exclamation) return "!";
		if (id == R.id.soft_key_extra_question) return "?";
		if (id == R.id.soft_key_extra_at) return "@";
		return "";
	}


	private String getKeyChar() {
		final String chr = getRawChar();
		return tt9 != null ? Characters.getChar(tt9.getLanguage(), chr) : chr;
	}


	@Override
	protected boolean handleRelease() {
		final String chr = getKeyChar();
		return tt9 != null && !chr.isEmpty() && tt9.onText(chr, false);
	}


	@Override
	protected String getTitle() {
		return getKeyChar();
	}


	@Override
	protected String getAccessibilityText() {
		return Characters.getCharReadable(getKeyChar());
	}
}
