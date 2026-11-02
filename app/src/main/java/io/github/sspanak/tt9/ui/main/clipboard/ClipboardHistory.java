package io.github.sspanak.tt9.ui.main.clipboard;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Keeps the last copied texts, newest first, so they can be pasted again from the clipboard panel.
 * The list is saved, so it survives restarts. Texts marked as sensitive (e.g. passwords copied
 * from a password manager) are never kept.
 */
public class ClipboardHistory {
	public static final int MAX_ITEMS = 20;
	private static final int MAX_LENGTH = 5000;
	private static final String PREFS = "tt9_clipboard_history";
	private static final String KEY = "items";

	@Nullable private static ClipboardManager.OnPrimaryClipChangedListener listener;

	public static class Item {
		@NonNull public final String text;
		public final long time;
		Item(@NonNull String text, long time) { this.text = text; this.time = time; }
	}


	private static SharedPreferences prefs(@NonNull Context context) {
		return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}


	/** Start watching the clipboard. Safe to call many times. */
	public static void startListening(@NonNull Context context) {
		if (listener != null) {
			return;
		}

		final Context app = context.getApplicationContext();
		final ClipboardManager manager = (ClipboardManager) app.getSystemService(Context.CLIPBOARD_SERVICE);
		if (manager == null) {
			return;
		}

		listener = () -> captureCurrent(app);
		try {
			manager.addPrimaryClipChangedListener(listener);
		} catch (Exception ignored) {
			listener = null;
		}
	}


	/** Adds whatever is in the clipboard right now (if anything). */
	public static void captureCurrent(@NonNull Context context) {
		try {
			final ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			final ClipData clip = manager != null ? manager.getPrimaryClip() : null;
			if (clip == null || clip.getItemCount() == 0 || isSensitive(clip.getDescription())) {
				return;
			}
			final CharSequence text = clip.getItemAt(0).coerceToText(context);
			if (text != null) {
				add(context, text.toString());
			}
		} catch (Exception ignored) {
			// some phones do not allow reading the clipboard at certain moments
		}
	}


	private static boolean isSensitive(@Nullable ClipDescription description) {
		if (description == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			return false;
		}
		final PersistableBundle extras = description.getExtras();
		return extras != null && (extras.getBoolean("android.content.extra.IS_SENSITIVE", false) || extras.getBoolean("android.content.extra.IS_REMOTE_DEVICE", false));
	}


	public static void add(@NonNull Context context, @Nullable String text) {
		if (text == null || text.trim().isEmpty()) {
			return;
		}
		if (text.length() > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH);
		}

		ArrayList<Item> items = getAll(context);
		for (int i = items.size() - 1; i >= 0; i--) {
			if (items.get(i).text.equals(text)) {
				items.remove(i);
			}
		}
		items.add(0, new Item(text, System.currentTimeMillis()));
		while (items.size() > MAX_ITEMS) {
			items.remove(items.size() - 1);
		}
		save(context, items);
	}


	public static void remove(@NonNull Context context, @NonNull String text) {
		ArrayList<Item> items = getAll(context);
		for (int i = items.size() - 1; i >= 0; i--) {
			if (items.get(i).text.equals(text)) {
				items.remove(i);
			}
		}
		save(context, items);
	}


	public static void clear(@NonNull Context context) {
		save(context, new ArrayList<>());
	}


	@NonNull
	public static ArrayList<Item> getAll(@NonNull Context context) {
		ArrayList<Item> items = new ArrayList<>();
		try {
			JSONArray array = new JSONArray(prefs(context).getString(KEY, "[]"));
			for (int i = 0; i < array.length(); i++) {
				JSONObject o = array.getJSONObject(i);
				items.add(new Item(o.getString("t"), o.optLong("d", 0)));
			}
		} catch (Exception ignored) {
			// a broken list is simply started over
		}
		return items;
	}


	private static void save(@NonNull Context context, @NonNull ArrayList<Item> items) {
		try {
			JSONArray array = new JSONArray();
			for (Item item : items) {
				JSONObject o = new JSONObject();
				o.put("t", item.text);
				o.put("d", item.time);
				array.put(o);
			}
			prefs(context).edit().putString(KEY, array.toString()).apply();
		} catch (Exception ignored) {
		}
	}
}
