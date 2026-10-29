package io.github.sspanak.tt9.ui.main.emoji;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * The full emoji list used by the emoji panel, grouped in categories.
 */
public class EmojiCatalog {
	public static final int TAB_RECENT = 0;
	public static final int TAB_SYMBOLS = 1;
	public static final int TAB_EMOJIS = 5;

	public static final String[] ICONS = { "🕘", ".,?", "±", "/", "€", "😀", "👋", "❤️", "🐶", "🍔", "⚽", "🚗", "💡", "🏳️" };
	public static final String[] NAMES = { "Recentes", "Pontuação", "Matemática", "Barras e sinais", "Moedas", "Caras", "Pessoas e gestos", "Corações e símbolos", "Animais e natureza", "Comida e bebida", "Atividades", "Viagens e lugares", "Objetos", "Bandeiras" };

	private static final String[] DATA = {
		"", // recent symbols and emojis come from the settings
		". , ; : ! ? ¡ ¿ ' \" « » “ ” ‘ ’ ( ) [ ] { } … - – — _ · •",
		"+ − × ÷ = ≠ ≈ ± < > ≤ ≥ % ‰ ^ ~ √ ∞ ° ¹ ² ³ ½ ⅓ ¼ ¾ π µ",
		"/ \\ | @ # & * § ¶ © ® ™ ✓ ✗ ★ ☆ ← → ↑ ↓ ↔ ♀ ♂ † ¬",
		"€ $ £ ¥ ¢ ₿ ₹ ₽ ₩ ₺ ¤",
		"😀 😃 😄 😁 😆 😅 🤣 😂 🙂 🙃 😉 😊 😇 🥰 😍 🤩 😘 😗 ☺️ 😚 😙 🥲 😋 😛 😜 🤪 😝 🤑 🤗 🤭 🤫 🤔 🤐 🤨 😐 😑 😶 😏 😒 🙄 😬 🤥 😌 😔 😪 🤤 😴 😷 🤒 🤕 🤢 🤮 🤧 🥵 🥶 🥴 😵 🤯 🤠 🥳 😎 🤓 🧐 😕 😟 🙁 ☹️ 😮 😯 😲 😳 🥺 😦 😧 😨 😰 😥 😢 😭 😱 😖 😣 😞 😓 😩 😫 🥱 😤 😡 😠 🤬 😈 👿 💀 💩 🤡 👻 👽 🤖 😺 😸 😹 😻 😼 😽 🙀 😿 😾 🙈 🙉 🙊",
		"👋 🤚 🖐️ ✋ 🖖 👌 🤌 🤏 ✌️ 🤞 🤟 🤘 🤙 👈 👉 👆 👇 ☝️ 👍 👎 ✊ 👊 🤛 🤜 👏 🙌 👐 🤲 🤝 🙏 ✍️ 💅 💪 🦵 🦶 👂 👃 🧠 👀 👁️ 👅 👄 💋 👶 🧒 👦 👧 🧑 👱 👨 🧔 👩 🧓 👴 👵 🙍 🙎 🙅 🙆 💁 🙋 🙇 🤦 🤷 👮 👷 💂 🕵️ 👩‍⚕️ 👨‍⚕️ 👩‍🍳 👨‍🍳 👩‍🔧 👨‍🔧 👩‍💻 👨‍💻 🧑‍✈️ 🤴 👸 🤵 👰 🤰 🎅 🤶 🦸 🦹 🧙 🧚 🧛 🧜 🧝 🚶 🧍 🏃 💃 🕺 👯 🧖 🧗 🏇 ⛷️ 🏂 🏌️ 🏄 🚣 🏊 🚴 👫 👬 👭 💏 💑 👪",
		"❤️ 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 ❣️ 💕 💞 💓 💗 💖 💘 💝 💟 ☮️ ✝️ ☪️ 🕉️ ☯️ ♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓ ⚛️ ✅ ☑️ ✔️ ❌ ❎ ➕ ➖ ➗ ✖️ ♾️ ‼️ ⁉️ ❓ ❔ ❕ ❗ 〰️ 💯 🔥 ✨ ⭐ 🌟 💫 💥 💢 💦 💨 🕳️ 💬 💭 💤 🔴 🟠 🟡 🟢 🔵 🟣 ⚫ ⚪ 🟤 🔺 🔻 🔸 🔹 ▶️ ⏸️ ⏹️ ⏺️ ⏭️ ⏮️ 🔀 🔁 🔂 🆗 🆕 🆓 🆒 🆙 🔝 🔜 ⚠️ 🚫 ⛔ 📵 🔞 ♻️ 🔰 ©️ ®️ ™️",
		"🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🐔 🐧 🐦 🐤 🦆 🦅 🦉 🦇 🐺 🐗 🐴 🦄 🐝 🐛 🦋 🐌 🐞 🐜 🕷️ 🦂 🐢 🐍 🦎 🐙 🦑 🦐 🦀 🐡 🐠 🐟 🐬 🐳 🐋 🦈 🐊 🐅 🐆 🦓 🦍 🐘 🦛 🦏 🐪 🦒 🦘 🐃 🐂 🐄 🐎 🐖 🐏 🐑 🐐 🦌 🐕 🐩 🐈 🐓 🦃 🦚 🦜 🦢 🕊️ 🐇 🐿️ 🦔 🐾 🌵 🎄 🌲 🌳 🌴 🌱 🌿 ☘️ 🍀 🍁 🍂 🍃 🍄 🌾 💐 🌷 🌹 🥀 🌺 🌸 🌼 🌻 🌞 🌝 🌚 🌙 ⭐ ☀️ ⛅ ☁️ 🌧️ ⛈️ 🌩️ ❄️ ☃️ ⛄ 🌈 💧 🌊",
		"🍏 🍎 🍐 🍊 🍋 🍌 🍉 🍇 🍓 🫐 🍈 🍒 🍑 🥭 🍍 🥥 🥝 🍅 🍆 🥑 🥦 🥬 🥒 🌶️ 🌽 🥕 🧄 🧅 🥔 🍠 🥐 🍞 🥖 🥨 🧀 🥚 🍳 🧈 🥞 🧇 🥓 🥩 🍗 🍖 🌭 🍔 🍟 🍕 🥪 🌮 🌯 🥗 🥘 🍝 🍜 🍲 🍛 🍣 🍱 🥟 🍤 🍙 🍚 🍘 🍥 🍢 🍡 🍧 🍨 🍦 🥧 🧁 🍰 🎂 🍮 🍭 🍬 🍫 🍿 🍩 🍪 🥜 🍯 🥛 🍼 ☕ 🍵 🧃 🥤 🍶 🍺 🍻 🥂 🍷 🥃 🍸 🍹 🍾 🧊 🥄 🍴 🍽️",
		"⚽ 🏀 🏈 ⚾ 🥎 🎾 🏐 🏉 🥏 🎱 🏓 🏸 🏒 🏑 🥍 🏏 🥅 ⛳ 🏹 🎣 🥊 🥋 🎽 🛹 🛼 ⛸️ 🥌 🎿 🏆 🥇 🥈 🥉 🏅 🎖️ 🎗️ 🎫 🎟️ 🎪 🎭 🎨 🎬 🎤 🎧 🎼 🎹 🥁 🎷 🎺 🎸 🎻 🎲 ♟️ 🎯 🎳 🎮 🎰 🧩 🎉 🎊 🎈 🎁 🎀 🎃 🎆 🎇 🧨",
		"🚗 🚕 🚙 🚌 🚎 🏎️ 🚓 🚑 🚒 🚐 🛻 🚚 🚛 🚜 🏍️ 🛵 🚲 🛴 🚨 🚔 🚍 🚘 🚖 🚡 🚠 🚟 🚃 🚋 🚞 🚝 🚄 🚅 🚈 🚂 🚆 🚇 🚊 🚉 ✈️ 🛫 🛬 🛩️ 💺 🚀 🛸 🚁 🛶 ⛵ 🚤 🛥️ 🛳️ ⛴️ 🚢 ⚓ ⛽ 🚧 🚦 🚥 🚏 🗺️ 🗿 🗽 🗼 🏰 🏯 🏟️ 🎡 🎢 🎠 ⛲ ⛱️ 🏖️ 🏝️ 🏜️ 🌋 ⛰️ 🏔️ 🗻 🏕️ ⛺ 🏠 🏡 🏘️ 🏚️ 🏗️ 🏭 🏢 🏬 🏣 🏤 🏥 🏦 🏨 🏪 🏫 🏩 💒 🏛️ ⛪ 🕌 🕍 🛕 🌅 🌄 🌠 🎇 🌉 🌃 🏙️ 🌆 🌇",
		"⌚ 📱 📲 💻 ⌨️ 🖥️ 🖨️ 🖱️ 💽 💾 💿 📀 📷 📸 📹 🎥 📞 ☎️ 📟 📠 📺 📻 🎙️ ⏱️ ⏰ ⌛ ⏳ 📡 🔋 🔌 💡 🔦 🕯️ 🧯 💸 💵 💴 💶 💷 💰 💳 💎 ⚖️ 🧰 🔧 🔨 ⚒️ 🛠️ ⛏️ 🔩 ⚙️ 🧱 ⛓️ 🧲 🔫 💣 🔪 🗡️ ⚔️ 🛡️ 🚬 ⚰️ 🔮 📿 💈 ⚗️ 🔭 🔬 🩹 🩺 💊 💉 🧬 🦠 🧫 🧪 🌡️ 🧹 🧺 🧻 🚽 🚰 🚿 🛁 🧼 🧽 🛎️ 🔑 🗝️ 🚪 🛋️ 🛏️ 🧸 🖼️ 🛍️ 🛒 🎁 ✉️ 📩 📨 📧 💌 📦 🏷️ 📪 📬 📮 📜 📃 📄 📑 📊 📈 📉 📆 📅 🗓️ 📇 🗃️ 🗳️ 🗄️ 📋 📁 📂 🗞️ 📰 📓 📔 📒 📕 📗 📘 📙 📚 📖 🔖 🔗 📎 🖇️ 📐 📏 📌 📍 ✂️ 🖊️ 🖋️ ✒️ 🖌️ 🖍️ 📝 ✏️ 🔍 🔎 🔏 🔐 🔒 🔓",
		"🏳️ 🏴 🏁 🚩 🏳️‍🌈 🇵🇹 🇧🇷 🇦🇴 🇲🇿 🇨🇻 🇬🇼 🇸🇹 🇹🇱 🇪🇸 🇫🇷 🇬🇧 🇮🇪 🇩🇪 🇮🇹 🇳🇱 🇧🇪 🇨🇭 🇱🇺 🇦🇹 🇵🇱 🇺🇦 🇷🇴 🇬🇷 🇸🇪 🇳🇴 🇩🇰 🇫🇮 🇺🇸 🇨🇦 🇲🇽 🇦🇷 🇨🇱 🇨🇴 🇻🇪 🇺🇾 🇵🇾 🇨🇳 🇯🇵 🇰🇷 🇮🇳 🇦🇺 🇿🇦 🇲🇦 🇪🇺 🇺🇳"
	};

	private static final ArrayList<ArrayList<String>> cache = new ArrayList<>();


	@NonNull
	public static ArrayList<String> getCategory(int index) {
		if (cache.isEmpty()) {
			final Paint paint = new Paint();
			for (String row : DATA) {
				ArrayList<String> list = new ArrayList<>();
				if (!row.isEmpty()) {
					for (String emoji : row.split(" ")) {
						if (!emoji.isEmpty() && paint.hasGlyph(emoji)) {
							list.add(emoji);
						}
					}
				}
				cache.add(list);
			}
		}

		return index >= 0 && index < cache.size() ? cache.get(index) : new ArrayList<>();
	}


	/** True for a single symbol or emoji (no letters, digits or spaces), e.g. "/", "€" or "😂". */
	public static boolean isSymbolOrEmoji(@NonNull String text) {
		if (text.isEmpty() || text.length() > 16) {
			return false;
		}
		for (int i = 0; i < text.length(); ) {
			final int cp = text.codePointAt(i);
			if (Character.isLetterOrDigit(cp) || Character.isWhitespace(cp)) {
				return false;
			}
			i += Character.charCount(cp);
		}
		return true;
	}


	/** True for real emoji, not for ordinary punctuation or currency symbols. */
	public static boolean isEmoji(@NonNull String text) {
		if (text.isEmpty()) {
			return false;
		}
		final int cp = text.codePointAt(0);
		return cp >= 0x1F000 || (cp >= 0x2190 && Character.getType(cp) == Character.OTHER_SYMBOL);
	}
}
