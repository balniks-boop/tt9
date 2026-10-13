package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Toolbar button: goes to the calculator-style 123 mode. Tapping again returns to text.
 */
public class SoftKeyToolbarNumeric extends BaseSoftKeyCustomizable {
	public SoftKeyToolbarNumeric(Context context) { super(context); }
	public SoftKeyToolbarNumeric(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarNumeric(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return ModeSwitcher.toggleNumeric(tt9);
	}

	@Override
	protected String getTitle() {
		return ModeSwitcher.isNumeric(tt9) ? "abc" : "123";
	}

	@Override
	protected float getTitleScale() {
		return 0.8f;
	}

	@Override
	protected String getAccessibilityText() {
		return ModeSwitcher.isNumeric(tt9) ? "Letras" : "Números";
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void render() {
		setEnabled(ModeSwitcher.canToggleNumeric(tt9));
		super.render();
	}
}
