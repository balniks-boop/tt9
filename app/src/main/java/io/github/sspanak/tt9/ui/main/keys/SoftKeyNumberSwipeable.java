package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.commands.CmdFilterSuggestions;
import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.commands.CmdNextInputMode;
import io.github.sspanak.tt9.commands.CmdTogglePredictiveMode;
import io.github.sspanak.tt9.commands.CmdVoiceInput;
import io.github.sspanak.tt9.commands.Command;
import io.github.sspanak.tt9.commands.CommandCollection;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;

public class SoftKeyNumberSwipeable extends SoftKeyNumber {
	private float previousX;
	private String lastSwipeCommand = null;
	private final Command[] swipeCommand = { null, null };
	private boolean swipeCommandRan = false;

	public SoftKeyNumberSwipeable(Context context) { super(context); }
	public SoftKeyNumberSwipeable(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNumberSwipeable(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	public void setTT9(TraditionalT9 tt9) {
		super.setTT9(tt9);
		isSwipeable = false;

		if (tt9 != null) {
			final int number = getNumber();
			final String actionKey = ActionKey.numberToActionKey(number);

			isSwipeable =
				number == 0 // the spacebar always swipes: to switch languages or to move the cursor
				|| (getNumber() > 0 && !(tt9.getSettings().getSwipeLeftCommand(actionKey).isEmpty() && tt9.getSettings().getSwipeRightCommand(actionKey).isEmpty()));
		}
	}


	@Override
	protected String getRightText() { return is0Swipeable() && !isLanguageSwipe() && getNumber() == 0 ? getContext().getString(R.string.key_dpad_right) : super.getRightText(); }


	@Override
	protected String getLeftText() { return is0Swipeable() && !isLanguageSwipe() && getNumber() == 0 ? getContext().getString(R.string.key_dpad_left) : super.getLeftText(); }


	@Override
	protected int getCornerIcon(int position) {
		final Command cmd = switch (position) {
			case ICON_POSITION_BOTTOM_LEFT -> getSwipeCommand(true);
			case ICON_POSITION_BOTTOM_RIGHT -> getSwipeCommand(false);
			default -> null;
		};

		if (cmd instanceof CmdNextInputMode) {
			((CmdNextInputMode) cmd).invalidateIcon(tt9);
		}
		if (cmd instanceof CmdTogglePredictiveMode) {
			((CmdTogglePredictiveMode) cmd).invalidateIcon(tt9);
		}

		return cmd != null ? cmd.getIcon() : super.getCornerIcon(position);
	}


	@Override
	protected float getCornerElementScale(int position) {
		Command cornerCommand = getSwipeCommand(position == ICON_POSITION_BOTTOM_LEFT);

		if ((position == ICON_POSITION_BOTTOM_LEFT || position == ICON_POSITION_BOTTOM_RIGHT)
			&& !(cornerCommand instanceof CmdNextInputMode)
			&& !(cornerCommand instanceof CmdTogglePredictiveMode)
		) {
			return super.getCornerElementScale(position) * 0.75f;
		}

		return super.getCornerElementScale(position);
	}


	@Override
	protected boolean handlePress() {
		swipeCommandRan = false;
		return super.handlePress();
	}


	@Override
	protected void handleStartSwipeX(float screenX, float d) {
		previousX = screenX;
	}


	/**
	 * 0-key swipe function. Move the text cursor left or right.
	 */
	@Override
	protected void handleSwipeX(float screenX, float delta) {
		if (is0Swipeable() && !isLanguageSwipe()) {
			moveCursor(screenX);
		}
	}


	/**
	 * All other number keys swipe function. Execute the assigned command, and set the swipeCommandRan
	 * flag to avoid triggering the default press-release method. Or keep the flag off, if no action.
	 */
	@Override
	protected void handleEndSwipeX(float position, float delta) {
		if (is0Swipeable() && isLanguageSwipe()) {
			swipeCommandRan = switchLanguage(delta > 0);
			return;
		}

		Command cmd = getSwipeCommand(delta < 0);

		// avoid executing commands during voice input, to prevent undesired actions
		if (tt9 != null && tt9.isVoiceInputActive() && !(cmd instanceof CmdVoiceInput)) {
			return;
		}

		if (cmd instanceof CmdFilterSuggestions) {
			swipeCommandRan = ((CmdFilterSuggestions) cmd).run(tt9, cmd.getId().equals(lastSwipeCommand));
		} else if (cmd != null) {
			swipeCommandRan = cmd.run(tt9);
		}

		lastSwipeCommand = cmd != null && swipeCommandRan ? cmd.getId() : null;
	}


	@Override
	protected boolean handleRelease() {
		if (!swipeCommandRan) {
			lastSwipeCommand = null;
		}

		return swipeCommandRan || super.handleRelease();
	}


	@Nullable
	private Command getSwipeCommand(boolean left) {
		if (tt9 == null || getNumber() == 0 || isFnPanelOn()) {
			return null;
		}

		final String aKey = ActionKey.numberToActionKey(getNumber());

		String currentCommandId = left ? tt9.getSettings().getSwipeLeftCommand(aKey) : tt9.getSettings().getSwipeRightCommand(aKey);
		Command cachedCommand = left ? swipeCommand[0] : swipeCommand[1];

		if (cachedCommand == null || !cachedCommand.getId().equals(currentCommandId)) {
			cachedCommand = CommandCollection.getById(CommandCollection.COLLECTION_SWIPE, currentCommandId);
			swipeCommand[left ? 0 : 1] = cachedCommand;
			resetIconCache();
		}

		return cachedCommand != null && cachedCommand.isAvailable(tt9) ? cachedCommand : null;
	}


	@Override
	public boolean isDynamic() {
		return
			getSwipeCommand(false) instanceof CmdNextInputMode || getSwipeCommand(true) instanceof CmdNextInputMode
			|| getSwipeCommand(false) instanceof CmdTogglePredictiveMode || getSwipeCommand(true) instanceof CmdTogglePredictiveMode;
	}


	/**
	 * TouchPal-style spacebar: when more than one language is enabled, swiping the spacebar
	 * left-to-right goes to the next language, and right-to-left goes to the previous one.
	 */
	private boolean isLanguageSwipe() {
		return
			tt9 != null
			&& getNumber() == 0
			&& !tt9.isInputModeNumeric()
			&& !tt9.isTextEditingActive()
			&& tt9.getSettings().areEnabledLanguagesMoreThanN(1);
	}


	private boolean switchLanguage(boolean next) {
		if (tt9 == null) {
			return false;
		}

		ArrayList<Integer> languages = tt9.getSettings().getEnabledLanguageIds();
		if (languages.size() < 2) {
			return false;
		}

		final int currentId = tt9.getLanguage() != null ? tt9.getLanguage().getId() : -1;
		final int current = Math.max(0, languages.indexOf(currentId));
		final int target = (current + (next ? 1 : -1) + languages.size()) % languages.size();

		tt9.getSuggestionOps().cancelDelayedAccept();
		tt9.stopVoiceInput();
		tt9.setLang(languages.get(target));
		return true;
	}


	private boolean is0Swipeable() {
		boolean fnPanelOn = isFnPanelOn();

		return
			isSwipeable
			&& getNumber() == 0
			&& !LanguageKind.isKorean(tt9 != null ? tt9.getLanguage() : null)
			&& (!fnPanelOn || (tt9 != null && tt9.isTextEditingActive()));
	}


	/**
	 * Swipe function for the 0-key. Move the text cursor left or right, and set the swipeCommandRan
	 * flag to avoid triggering the default press-release method. Or keep the flag off, if no action.
	 */
	private void moveCursor(float screenX) {
		float delta = previousX - screenX;
		if (tt9 == null || !tt9.getSettings().getMoveCursorWithSpace() || Math.abs(delta) <= tt9.getSettings().getMoveCursorWithSpaceThreshold()) {
			return;
		}

		previousX = screenX;
		new CmdMoveCursor().run(tt9, delta > 0 ? CmdMoveCursor.CURSOR_MOVE_LEFT : CmdMoveCursor.CURSOR_MOVE_RIGHT);
		swipeCommandRan = true;
	}
}
