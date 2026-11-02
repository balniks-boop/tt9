package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.github.sspanak.tt9.commands.CmdShowSettings;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

/**
 * Toolbar button, drawn as a text gear so it fits in the thin toolbar.
 * - Tap: opens the keyboard settings.
 * - Hold, then drag up or down: makes the keyboard taller or shorter. The size is saved
 *   (it is the same as "Key height" in the settings).
 */
public class SoftKeyToolbarSettings extends BaseSoftKeyCustomizable {
	private static final int MIN_PERCENT = 70;
	private static final int MAX_PERCENT = 150;
	private static final long RENDER_INTERVAL = 40;

	private final CmdShowSettings command = new CmdShowSettings();

	private boolean resizing = false;
	private boolean resized = false;
	private float lastY = 0;
	private float startY = 0;
	private int startHeight = 0;
	private int lastHeight = 0;
	private long lastRender = 0;

	public SoftKeyToolbarSettings(Context context) { super(context); }
	public SoftKeyToolbarSettings(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyToolbarSettings(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }


	@Override
	public boolean onTouch(View view, MotionEvent event) {
		final int action = event.getActionMasked();
		lastY = event.getRawY();

		if (action == MotionEvent.ACTION_DOWN) {
			resizing = false;
			resized = false;
		} else if (action == MotionEvent.ACTION_MOVE && resizing) {
			resize(lastY, false);
			return true;
		} else if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && resizing) {
			resize(lastY, true);
			resizing = false;
			resized = true;
			render();
		}

		return super.onTouch(view, event);
	}


	@Override
	protected void handleHold() {
		preventRepeat();
		if (tt9 == null) {
			return;
		}
		resizing = true;
		startY = lastY;
		startHeight = tt9.getSettings().getNumpadKeyHeight(null);
		lastHeight = startHeight;
		vibrate(io.github.sspanak.tt9.ui.Vibration.getHoldVibration());
		render();
	}


	private void resize(float y, boolean force) {
		if (tt9 == null || tt9.getMainView() == null) {
			return;
		}

		final SettingsStore settings = tt9.getSettings();
		final int base = settings.getNumpadKeyDefaultHeight();
		final int min = Math.round(base * MIN_PERCENT / 100f);
		final int max = Math.round(base * MAX_PERCENT / 100f);

		// there are 4 rows of keys, so each key grows by a quarter of the finger movement
		int height = startHeight + Math.round((startY - y) / 4f);
		height = Math.max(min, Math.min(max, height));

		final long now = System.currentTimeMillis();
		if (height == lastHeight && !force) {
			return;
		}
		if (!force && now - lastRender < RENDER_INTERVAL) {
			return;
		}

		lastHeight = height;
		lastRender = now;
		settings.setNumpadKeyHeight(height);
		tt9.resetNormalizedDimensions();
		tt9.getMainView().render();
	}


	@Override
	protected boolean handleRelease() {
		if (resized) {
			resized = false;
			return true;
		}
		return command.run(tt9);
	}


	@Override protected String getAccessibilityText() { return tt9 != null ? command.getName(tt9) : null; }
	@Override protected String getTitle() { return resizing ? "↕" : "⚙︎"; } // text-style gear, not the colored emoji
	@Override protected float getTitleScale() { return 1.0f; }
}
