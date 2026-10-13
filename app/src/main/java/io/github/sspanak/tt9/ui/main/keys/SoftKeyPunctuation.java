package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.chars.Characters;

/**
 * A function-column key that types a single fixed character. Used for the TyNi-style "!", "?", "@"
 * keys in text mode, and for the calculator keys (+ − × ÷ =) in number mode. Some keys type an
 * alternative character when held, which is shown in the corner.
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
		if (id == R.id.soft_key_extra_plus) return "+";
		if (id == R.id.soft_key_extra_minus) return "-";
		if (id == R.id.soft_key_extra_times) return "×";
		if (id == R.id.soft_key_extra_divide) return "÷";
		if (id == R.id.soft_key_extra_equals) return "=";
		return "";
	}


	private String getHoldChar() {
		final int id = getId();
		if (id == R.id.soft_key_extra_plus) return "(";
		if (id == R.id.soft_key_extra_minus) return ")";
		if (id == R.id.soft_key_extra_times) return "*";
		if (id == R.id.soft_key_extra_divide) return "/";
		if (id == R.id.soft_key_extra_equals) return "%";
		return null;
	}


	private String getKeyChar() {
		final String chr = getRawChar();
		return tt9 != null ? Characters.getChar(tt9.getLanguage(), chr) : chr;
	}


	@Override
	public boolean isHoldEnabled() {
		return getHoldChar() != null;
	}


	@Override
	protected void handleHold() {
		preventRepeat();
		final String chr = getHoldChar();
		if (tt9 != null && chr != null) {
			tt9.onText(chr, false);
		}
	}


	@Override
	protected boolean handleRelease() {
		final String chr = getKeyChar();
		return tt9 != null && !chr.isEmpty() && tt9.onText(chr, false);
	}


	@Override
	protected String getTitle() {
		final String chr = getKeyChar();
		return "-".equals(chr) ? "−" : chr; // display a proper minus sign, but type the standard "-"
	}


	@Override
	protected String getHoldText() {
		return getHoldChar();
	}


	@Override
	protected String getAccessibilityText() {
		return Characters.getCharReadable(getKeyChar());
	}
}
