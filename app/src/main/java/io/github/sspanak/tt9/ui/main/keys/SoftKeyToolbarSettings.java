package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdShowSettings;

/**
 * Toolbar button: opens the keyboard settings directly. Drawn as a text gear, so it fits in
 * the thin toolbar without needing an icon overlay.
 */
public class SoftKeyToolbarSettings extends BaseSoftKeyCustomizable {
	private final CmdShowSettings command = new CmdShowSettings();

	public SoftKeyToolbarSettings(Context context) { super(context); }
	public SoftKeyToolbarSettings(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarSettings(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected boolean handleRelease() { return command.run(tt9); }
	@Override protected String getAccessibilityText() { return tt9 != null ? command.getName(tt9) : null; }
	@Override protected String getTitle() { return "⚙︎"; } // text-style gear, not the colored emoji
	@Override protected float getTitleScale() { return 1.0f; }
}
