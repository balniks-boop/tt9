package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdNextInputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;

/**
 * Mode key. Switches T9 (predictive) <-> ABC (letter by letter). In 123 mode, it returns to text.
 * Numbers are reached with the "123" button on the toolbar.
 */
public class SoftKeyTogglePredictiveMode extends BaseSoftKeyCustomizable {
	private final CmdNextInputMode nextMode = new CmdNextInputMode();

	public SoftKeyTogglePredictiveMode(Context context) { super(context); }
	public SoftKeyTogglePredictiveMode(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyTogglePredictiveMode(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		return ModeSwitcher.toggleText(tt9);
	}

	@Override
	protected String getAccessibilityText() {
		return nextMode.getName(tt9);
	}

	@Override
	protected String getTitle() {
		if (tt9 == null) {
			return "";
		}

		if (InputModeKind.isPredictive(tt9.getInputMode())) {
			return "T9";
		} else if (tt9.isInputModeNumeric()) {
			return "T9";
		} else if (tt9.isInputModeABC()) {
			return "ABC";
		} else {
			return tt9.getInputModeName();
		}
	}

	@Override
	protected float getTitleScale() {
		return super.getTitleScale() * 0.9f;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void render() {
		setEnabled(ModeSwitcher.canToggleText(tt9));
		super.render();
	}
}
