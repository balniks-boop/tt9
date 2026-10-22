package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.sspanak.tt9.commands.CmdAddWord;
import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.screens.deleteWords.DeleteWordsScreen;
import io.github.sspanak.tt9.ui.UI;

/**
 * Toolbar button: add a word the dictionary does not know (for example "tou").
 * - It is shown only when the word at the cursor is NOT in the dictionary yet.
 * - Holding it opens the list of added words, where they can be searched and deleted.
 */
public class SoftKeyToolbarAddWord extends BaseSoftKeyCustomizable {
	private static final ExecutorService checker = Executors.newSingleThreadExecutor();

	private final CmdAddWord command = new CmdAddWord();
	private boolean openedByHold = false;
	private String lastCheckedWord = null;
	private boolean lastWordIsNew = false;

	public SoftKeyToolbarAddWord(Context context) { super(context); }
	public SoftKeyToolbarAddWord(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarAddWord(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	protected boolean handlePress() {
		openedByHold = false;
		return super.handlePress();
	}


	@Override
	protected void handleHold() {
		preventRepeat();
		if (tt9 == null) {
			return;
		}
		openedByHold = true;
		tt9.getSuggestionOps().cancelDelayedAccept();
		UI.showSettingsScreen(tt9, DeleteWordsScreen.NAME);
	}


	@Override
	protected boolean handleRelease() {
		if (openedByHold) {
			openedByHold = false;
			return true;
		}
		lastCheckedWord = null; // check again after the dialog, the word may have been added
		return command.run(tt9);
	}


	@Override protected String getAccessibilityText() { return tt9 != null ? command.getName(tt9) : null; }
	@Override protected String getTitle() { return "+ palavra"; }
	@Override protected float getTitleScale() { return 0.7f; }
	@Override public boolean isDynamic() { return true; }


	/**
	 * Looks up the word at the cursor in the background, and shows the button only if it is new.
	 */
	private void checkWordAtCursor() {
		if (tt9 == null || tt9.getLanguage() == null) {
			setShown(false);
			return;
		}

		final Language language = tt9.getLanguage();
		final String word = tt9.getTextField().getSurroundingWord(language).trim();

		if (word.isEmpty() || word.equals(lastCheckedWord)) {
			setShown(!word.isEmpty() && lastWordIsNew);
			return;
		}

		checker.execute(() -> {
			boolean isNew;
			try {
				final String sequence = language.getDigitSequenceForWord(word);
				isNew = !sequence.isEmpty() && DataStore.getWord(language, word, sequence) == null;
			} catch (Exception e) {
				isNew = true; // when in doubt, offer to add it
			}

			final boolean result = isNew;
			post(() -> {
				lastCheckedWord = word;
				lastWordIsNew = result;
				setShown(result && command.isAvailable(tt9));
			});
		});
	}


	private void setShown(boolean shown) {
		setVisibility(shown ? VISIBLE : INVISIBLE);
		setEnabled(shown);
	}


	@Override
	public void render() {
		if (command.isAvailable(tt9)) {
			checkWordAtCursor();
		} else {
			setShown(false);
		}
		super.render();
	}
}
