package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.ui.main.clipboard.ClipboardPanel;

/**
 * Toolbar button: opens (or closes) the list of recently copied texts.
 */
public class SoftKeyToolbarClipboard extends BaseSoftKeyCustomizable {
	public SoftKeyToolbarClipboard(Context context) { super(context); }
	public SoftKeyToolbarClipboard(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarClipboard(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		if (tt9 == null) {
			return false;
		}
		ClipboardPanel.toggle(tt9);
		return true;
	}

	@Override protected String getTitle() { return "📋"; }
	@Override protected float getTitleScale() { return 0.8f; }
	@Override protected String getAccessibilityText() { return "Área de transferência"; }
}
