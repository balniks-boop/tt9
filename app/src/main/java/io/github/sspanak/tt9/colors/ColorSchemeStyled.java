package io.github.sspanak.tt9.colors;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;

/**
 * Complete keyboard styles: colors plus the roundness and border of the keys.
 */
public class ColorSchemeStyled extends AbstractColorScheme {
	private static final int ID_BASE = 200;

	private static final class Style {
		final String name;
		final int background, key, fn, border, text, altText, ok, okText;
		final float radiusDp, borderDp;

		Style(String name, String background, String key, String fn, String border, String text, String altText, String ok, String okText, float radiusDp, float borderDp) {
			this.name = name;
			this.background = Color.parseColor(background);
			this.key = Color.parseColor(key);
			this.fn = Color.parseColor(fn);
			this.border = border == null ? Color.TRANSPARENT : Color.parseColor(border);
			this.text = Color.parseColor(text);
			this.altText = Color.parseColor(altText);
			this.ok = Color.parseColor(ok);
			this.okText = Color.parseColor(okText);
			this.radiusDp = radiusDp;
			this.borderDp = borderDp;
		}
	}

	private static final Style[] STYLES = {
		new Style("Estilo 1 · Contorno azul", "#121826", "#121826", "#121826", "#4A6FA5", "#D5E1F5", "#8FA3C4", "#2F5A8F", "#EEF3FB", 12, 2),
		new Style("Estilo 2 · Grafite", "#1C1C1E", "#3A3A3C", "#2A2A2C", null, "#F2F2F2", "#A1A1A6", "#FF8A1F", "#1C1C1E", 8, 0),
		new Style("Estilo 3 · Claro", "#E4E6EA", "#FFFFFF", "#CFD3DA", null, "#1D2330", "#5B6270", "#2563EB", "#FFFFFF", 8, 0),
		new Style("Estilo 4 · Preto AMOLED", "#000000", "#000000", "#000000", "#3A3A3A", "#ECECEC", "#8A8A8A", "#22C55E", "#000000", 10, 1),
		new Style("Estilo 5 · Menta", "#EAF4EF", "#FFFFFF", "#D4EBE0", "#A9CFBD", "#1F3B30", "#4F6F62", "#2F8F6A", "#FFFFFF", 16, 1.5f),
		new Style("Estilo 6 · Vinho e dourado", "#1E1014", "#3A1D25", "#2C151B", null, "#F3E6D0", "#CFA86F", "#C99A4E", "#1E1014", 10, 0),
	};

	private final int id;


	private ColorSchemeStyled(@NonNull Context context, int index) {
		super(context);
		id = ID_BASE + index;
		final Style s = STYLES[index];
		displayName = s.name;

		final boolean light = ColorUtils.calculateLuminance(s.background) > 0.5;
		final int ripple = light ? ColorUtils.setAlphaComponent(Color.BLACK, 40) : ColorUtils.setAlphaComponent(Color.WHITE, 50);

		keyboardBackground = s.background;
		keyboardText = s.text;

		suggestionSelectedBackground = ColorUtils.blendARGB(s.key, s.ok, 0.25f);
		suggestionSelectedColor = s.text;
		suggestionSeparatorColor = ColorUtils.blendARGB(s.background, s.altText, 0.35f);

		keyBackground = s.key;
		keyBorder = s.border;
		keyRipple = ripple;
		keyText = s.text;
		keyAlternativeText = s.altText;

		keyFnBackground = s.fn;
		keyFnBorder = s.border;
		keyFnRipple = ripple;
		keyFnText = s.text;
		keyFnAlternativeText = s.altText;

		keyLf4Background = s.fn;
		keyLf4Border = s.border;
		keyLf4Ripple = ripple;
		keyLf4Text = s.text;
		keyLf4AlternativeText = s.altText;

		keyOkBackground = s.ok;
		keyOkBorder = s.border == Color.TRANSPARENT ? Color.TRANSPARENT : s.ok;
		keyOkRipple = ColorUtils.setAlphaComponent(Color.WHITE, 60);
		keyOkText = s.okText;

		keyCornerRadiusDp = s.radiusDp;
		keyBorderWidthDp = s.borderDp;
	}


	@NonNull
	public static ArrayList<AbstractColorScheme> getAll(@NonNull Context context) {
		ArrayList<AbstractColorScheme> all = new ArrayList<>();
		for (int i = 0; i < STYLES.length; i++) {
			all.add(new ColorSchemeStyled(context, i));
		}
		return all;
	}


	@Override public int getId() { return id; }
	@Override public int getName() { return R.string.pref_color_scheme; }
}
