package io.github.sspanak.tt9.db.words;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Counts how many times each word was chosen, so the word written most often comes first,
 * instead of the one chosen last. Using "dê" once no longer pushes "de" down.
 */
public class UsageCounter extends SQLiteOpenHelper {
	private static final String DB_NAME = "word_usage.db";
	private static final String TABLE = "usage";

	/** A word must be chosen at least this many times before it can take the first place. */
	public static final int MIN_USES_TO_BE_FIRST = 2;

	public UsageCounter(@NonNull Context context) {
		super(context.getApplicationContext(), DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE + " (lang INTEGER NOT NULL, word TEXT NOT NULL, uses INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (lang, word))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


	private static String key(@NonNull String word, @NonNull Locale locale) {
		return word.toLowerCase(locale);
	}


	public synchronized void increment(int langId, @NonNull String word, @NonNull Locale locale) {
		try {
			SQLiteDatabase db = getWritableDatabase();
			final String k = key(word, locale);
			db.execSQL("INSERT OR IGNORE INTO " + TABLE + " (lang, word, uses) VALUES (?, ?, 0)", new Object[]{langId, k});
			db.execSQL("UPDATE " + TABLE + " SET uses = uses + 1 WHERE lang = ? AND word = ?", new Object[]{langId, k});
		} catch (Exception ignored) {
		}
	}


	public synchronized int get(int langId, @NonNull String word, @NonNull Locale locale) {
		try (Cursor c = getReadableDatabase().rawQuery(
			"SELECT uses FROM " + TABLE + " WHERE lang = ? AND word = ?",
			new String[]{String.valueOf(langId), key(word, locale)}
		)) {
			return c.moveToFirst() ? c.getInt(0) : 0;
		} catch (Exception e) {
			return 0;
		}
	}
}
