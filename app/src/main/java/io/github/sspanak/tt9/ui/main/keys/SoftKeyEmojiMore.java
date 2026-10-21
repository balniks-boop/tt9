package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.ui.main.emoji.EmojiPanel;

/**
 * "+" button at the right of the suggestions bar, shown while emojis are listed there.
 * It opens the full emoji picker.
 */
public class SoftKeyEmojiMore extends BaseSoftKeyCustomizable {
	public SoftKeyEmojiMore(Context context) { super(context); }
	public SoftKeyEmojiMore(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyEmojiMore(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override
	protected boolean handleRelease() {
		EmojiPanel.show(tt9);
		return true;
	}

	@Override protected String getTitle() { return "+"; }
	@Override protected float getTitleScale() { return 1.2f; }
	@Override protected String getAccessibilityText() { return "Mais emojis"; }
	@Override public boolean isDynamic() { return true; }

	@Override
	public void render() {
		final boolean show = tt9 != null && tt9.getInputMode() != null && tt9.getInputMode().containsEmojis();
		setVisibility(show ? VISIBLE : GONE);
		super.render();
	}
}
