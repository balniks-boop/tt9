package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.ui.main.emoji.EmojiPanel;

/**
 * "+" button at the right of the suggestions bar, shown while a SYM list (recent, symbols or emojis) is there.
 * It opens the full emoji picker.
 */
public class SoftKeyEmojiMore extends BaseSoftKeyCustomizable {
	public SoftKeyEmojiMore(Context context) { super(context); }
	public SoftKeyEmojiMore(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyEmojiMore(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		final io.github.sspanak.tt9.ime.modes.InputMode mode = tt9 != null ? tt9.getInputMode() : null;
		int tab = io.github.sspanak.tt9.ui.main.emoji.EmojiCatalog.TAB_RECENT;
		if (mode != null && mode.isEmojiSequence()) {
			tab = io.github.sspanak.tt9.ui.main.emoji.EmojiCatalog.TAB_EMOJIS;
		} else if (mode != null && !mode.isRecentCharsSequence()) {
			tab = io.github.sspanak.tt9.ui.main.emoji.EmojiCatalog.TAB_SYMBOLS;
		}
		EmojiPanel.show(tt9, tab);
		return true;
	}

	@Override protected String getTitle() { return "+"; }
	@Override protected float getTitleScale() { return 1.2f; }
	@Override protected String getAccessibilityText() { return "Mais emojis"; }
	@Override public boolean isDynamic() { return true; }

	@Override
	public void render() {
		final boolean show = tt9 != null
			&& tt9.getInputMode() != null
			&& io.github.sspanak.tt9.ime.modes.InputModeKind.isPredictive(tt9.getInputMode())
			&& tt9.getInputMode().getFirstKey() == 1
			&& !EmojiPanel.isShown(tt9);
		setVisibility(show ? VISIBLE : GONE);
		super.render();
	}
}
