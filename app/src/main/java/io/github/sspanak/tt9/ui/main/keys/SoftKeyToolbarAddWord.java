package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdAddWord;

/**
 * Toolbar button: add a word the dictionary does not know (for example "tou").
 * If the cursor is on or right after a word, that word is suggested in the dialog.
 */
public class SoftKeyToolbarAddWord extends BaseSoftKeyCustomizable {
	private final CmdAddWord command = new CmdAddWord();

	public SoftKeyToolbarAddWord(Context context) { super(context); }
	public SoftKeyToolbarAddWord(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarAddWord(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected boolean handleRelease() { return command.run(tt9); }
	@Override protected String getAccessibilityText() { return tt9 != null ? command.getName(tt9) : null; }
	@Override protected String getTitle() { return "+ palavra"; }
	@Override protected float getTitleScale() { return 0.7f; }
	@Override public boolean isDynamic() { return true; }

	@Override
	public void render() {
		final boolean available = command.isAvailable(tt9);
		setVisibility(available ? VISIBLE : INVISIBLE);
		setEnabled(available);
		super.render();
	}
}
