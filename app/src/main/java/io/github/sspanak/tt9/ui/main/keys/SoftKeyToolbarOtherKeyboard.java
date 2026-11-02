package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdSelectKeyboard;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

/**
 * Toolbar button: goes to the full (QWERTY) keyboard used before, for example Gboard.
 * - Tap: back to the previous keyboard. If Android does not know which one it was, shows the list.
 * - Hold: always shows the list of keyboards, to choose one.
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

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.stopVoiceInput();

		boolean switched = false;
		if (DeviceInfo.AT_LEAST_ANDROID_9) {
			try {
				switched = tt9.switchToPreviousInputMethod();
			} catch (Exception ignored) {}
		}
		if (!switched) {
			UI.showChangeKeyboardDialog(tt9);
		}
		return true;
	}

	@Override protected String getTitle() { return "⌨︎"; }
	@Override protected float getTitleScale() { return 1.1f; }
	@Override protected String getAccessibilityText() { return "Outro teclado"; }
}
