package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

/**
 * € and $ buttons. In 123 mode they are on the suggestions bar; in T9 and ABC, on the toolbar.
 */
public class SoftKeyCurrency extends BaseSoftKeyCustomizable {
	public SoftKeyCurrency(Context context) { super(context); }
	public SoftKeyCurrency(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyCurrency(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	private String getChar() {
		final int id = getId();
		return id == R.id.soft_key_currency_dollar || id == R.id.soft_key_toolbar_dollar ? "$" : "€";
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
		final boolean isToolbar = getId() == R.id.soft_key_toolbar_euro || getId() == R.id.soft_key_toolbar_dollar;
		final boolean isNumeric = ModeSwitcher.isNumeric(tt9);
		final boolean show = (isToolbar != isNumeric) && !(tt9 != null && tt9.isInputTypePhone());
		setVisibility(show ? VISIBLE : GONE);
		super.render();
	}
}
