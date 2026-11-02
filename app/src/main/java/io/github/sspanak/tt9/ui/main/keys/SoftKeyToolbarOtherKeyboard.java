package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdSelectKeyboard;

/**
 * Toolbar button: switches between the T9 keys and the full keyboard with all the letters
 * (QWERTY), both inside this same keyboard.
 * - Hold: shows Android's list of keyboards, to change to another app.
 */
public class SoftKeyToolbarOtherKeyboard extends BaseSoftKeyCustomizable {
	private final CmdSelectKeyboard select = new CmdSelectKeyboard();
	private boolean held = false;

	public SoftKeyToolbarOtherKeyboard(Context context) { super(context); }
	public SoftKeyToolbarOtherKeyboard(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarOtherKeyboard(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handlePress() {
		held = false;
		return super.handlePress();
	}

	@Override
	protected void handleHold() {
		preventRepeat();
		held = true;
		select.run(tt9);
	}

	@Override
	protected boolean handleRelease() {
		if (tt9 == null) {
			return false;
		}
		if (held) {
			held = false;
			return true;
		}

		io.github.sspanak.tt9.ui.main.qwerty.QwertyPanel.toggle(tt9);
		return true;
	}

	@Override protected String getTitle() { return "⌨︎"; }
	@Override protected float getTitleScale() { return 1.1f; }
	@Override protected String getAccessibilityText() { return "Teclado completo"; }
}
