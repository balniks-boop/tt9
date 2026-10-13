package io.github.sspanak.tt9.ui.main.keys;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;

/**
 * Shared logic for the "T9/ABC" key and the "123" toolbar button.
 * - The T9 key switches between T9 and ABC. From 123, it returns to the last text mode.
 * - The 123 button goes to numbers, and a second tap returns to the last text mode.
 */
class ModeSwitcher {
	private static int lastTextMode = InputMode.MODE_PREDICTIVE;


	static boolean isNumeric(@Nullable TraditionalT9 tt9) {
		return tt9 != null && InputModeKind.is123(tt9.getInputMode());
	}


	private static boolean isTextMode(@Nullable TraditionalT9 tt9) {
		return tt9 != null && (InputModeKind.isPredictive(tt9.getInputMode()) || InputModeKind.isABC(tt9.getInputMode()));
	}


	static boolean canToggleText(@Nullable TraditionalT9 tt9) {
		if (tt9 == null || tt9.isVoiceInputActive()) {
			return false;
		}
		ArrayList<Integer> allowed = tt9.getAllowedInputModes();
		return allowed.contains(InputMode.MODE_PREDICTIVE) || allowed.contains(InputMode.MODE_ABC);
	}


	static boolean canToggleNumeric(@Nullable TraditionalT9 tt9) {
		return tt9 != null && !tt9.isVoiceInputActive() && tt9.getAllowedInputModes().contains(InputMode.MODE_123) && canToggleText(tt9);
	}


	private static boolean setMode(@Nullable TraditionalT9 tt9, int modeId) {
		if (tt9 == null || !tt9.getAllowedInputModes().contains(modeId)) {
			return false;
		}
		tt9.setInputMode(modeId);
		tt9.forceShowWindow();
		return true;
	}


	private static boolean backToText(@Nullable TraditionalT9 tt9) {
		return setMode(tt9, lastTextMode) || setMode(tt9, InputMode.MODE_PREDICTIVE) || setMode(tt9, InputMode.MODE_ABC);
	}


	/** T9 <-> ABC. From 123, go back to the last text mode. */
	static boolean toggleText(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		if (!isTextMode(tt9)) {
			return backToText(tt9);
		}

		final int next = InputModeKind.isPredictive(tt9.getInputMode()) ? InputMode.MODE_ABC : InputMode.MODE_PREDICTIVE;
		if (setMode(tt9, next)) {
			lastTextMode = next;
			return true;
		}
		return false;
	}


	/** Text -> 123 -> back to the previous text mode. */
	static boolean toggleNumeric(@Nullable TraditionalT9 tt9) {
		if (tt9 == null) {
			return false;
		}

		if (isNumeric(tt9)) {
			return backToText(tt9);
		}

		if (isTextMode(tt9)) {
			lastTextMode = tt9.getInputMode().getId();
		}
		return setMode(tt9, InputMode.MODE_123);
	}
}
