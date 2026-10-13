package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

/**
 * € and $ buttons, shown on the suggestions bar only in 123 mode.
 */
public class SoftKeyCurrency extends BaseSoftKeyCustomizable {
	public SoftKeyCurrency(Context context) { super(context); }
	public SoftKeyCurrency(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCurrency(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	private String getChar() {
		return getId() == R.id.soft_key_currency_dollar ? "$" : "€";
	}

	@Override
	protected boolean handleRelease() {
		return tt9 != null && tt9.onText(getChar(), false);
	}

	@Override protected String getTitle() { return getChar(); }
	@Override protected float getTitleScale() { return 0.9f; }
	@Override protected String getAccessibilityText() { return getChar(); }
	@Override public boolean isDynamic() { return true; }

	@Override
	public void render() {
		final boolean show = ModeSwitcher.isNumeric(tt9) && !(tt9 != null && tt9.isInputTypePhone());
		setVisibility(show ? VISIBLE : GONE);
		super.render();
	}
}
