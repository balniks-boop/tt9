package io.github.sspanak.tt9.colors;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;

/**
 * Dark themes with a subtle colored outline around each key and softly tinted text,
 * in several colors and three intensities.
 */
public class ColorSchemeOutline extends AbstractColorScheme {
	private static final int ID_BASE = 100;

	private static final String[] COLOR_NAMES = { "Azul", "Vermelho", "Verde", "Roxo", "Laranja", "Cinzento" };
	private static final int[] COLORS = {
		Color.rgb(90, 180, 240),  // blue
		Color.rgb(240, 110, 110), // red
		Color.rgb(110, 205, 125), // green
		Color.rgb(170, 140, 240), // purple
		Color.rgb(240, 165, 90),  // orange
		Color.rgb(175, 185, 200)  // gray
	};
	private static final String[] INTENSITY_NAMES = { "suave", "médio", "intenso" };

	private final int id;


	private ColorSchemeOutline(@NonNull Context context, int colorIndex, int intensity) {
		super(context);
		id = ID_BASE + colorIndex * 10 + intensity;
		displayName = "Contorno " + COLOR_NAMES[colorIndex] + " (" + INTENSITY_NAMES[intensity] + ")";
		buildColors(COLORS[colorIndex], intensity);
	}


	@NonNull
	public static ArrayList<AbstractColorScheme> getAll(@NonNull Context context) {
		ArrayList<AbstractColorScheme> all = new ArrayList<>();
		for (int c = 0; c < COLORS.length; c++) {
			for (int i = 0; i < INTENSITY_NAMES.length; i++) {
				all.add(new ColorSchemeOutline(context, c, i));
			}
		}
		return all;
	}


	private static int mix(int color, int base, float amountOfColor) {
		return ColorUtils.blendARGB(base, color, amountOfColor);
	}


	private void buildColors(int accent, int intensity) {
		final float[] borderStrength = { 0.45f, 0.70f, 0.95f };
		final float[] textStrength = { 0.30f, 0.55f, 0.85f };

		final int white = Color.rgb(235, 238, 242);
		final int background = Color.rgb(17, 20, 26);
		final int keyFill = Color.rgb(27, 31, 39);
		final int fnFill = Color.rgb(23, 27, 34);

		final int border = mix(accent, keyFill, borderStrength[intensity]);
		final int text = mix(accent, white, textStrength[intensity]);
		final int altText = mix(accent, Color.rgb(140, 148, 160), textStrength[intensity]);
		final int ripple = ColorUtils.setAlphaComponent(accent, 70);

		keyboardBackground = background;
		keyboardText = text;

		suggestionSelectedBackground = mix(accent, keyFill, 0.35f);
		suggestionSelectedColor = white;
		suggestionSeparatorColor = mix(accent, background, 0.35f);

		keyBackground = keyFill;
		keyBorder = border;
		keyRipple = ripple;
		keyText = text;
		keyAlternativeText = altText;

		keyFnBackground = fnFill;
		keyFnBorder = border;
		keyFnRipple = ripple;
		keyFnText = text;
		keyFnAlternativeText = altText;

		keyLf4Background = fnFill;
		keyLf4Border = border;
		keyLf4Ripple = ripple;
		keyLf4Text = text;
		keyLf4AlternativeText = altText;

		// the OK key is filled with the accent, like the "Guardar" button in the example
		keyOkBackground = mix(accent, keyFill, 0.55f + 0.2f * intensity);
		keyOkBorder = border;
		keyOkRipple = ColorUtils.setAlphaComponent(Color.WHITE, 60);
		keyOkText = background;
	}


	@Override public int getId() { return id; }
	@Override public int getName() { return R.string.pref_color_scheme; }
}
