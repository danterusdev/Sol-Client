package me.mcblueparrot.client.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

/**
 * Utility to track config version.
 */
public enum ConfigVersion {
	/**
	 * Original version without a number, which still dwells in
	 * sol_client_settings.json, because it is moved elsewhere in the code.
	 */
	ORIGINAL {

		@Override
		public boolean check(JsonObject object) {
			return !object.has("version");
		}

		@Override
		protected JsonObject transformToNext(JsonObject object) {
			JsonObject newObject = object.deepCopy();

			// Shuffle the names around.
			newObject.add("coordinates", newObject.remove("position"));
			newObject.add("potion_effects", newObject.remove("statuseffects"));
			newObject.add("speedometer", newObject.remove("speed"));
			newObject.add("chunk_animator", newObject.remove("chunk_animation"));
			newObject.add("freelook", newObject.remove("perspective"));
			newObject.add("fullbright", newObject.remove("nightVision"));

			// For whatever reason, I forgot to change the name of ShowOwnTagMod when
			// copying and pasting like all the best programmers.
			newObject.add("show_own_tag", newObject.get("arabic_numerals"));

			return newObject;
		}

	},
	/**
	 * First version to have a number.
	 * Created to migrate mod names.
	 * Added in 1.5.8.
	 */
	V1;

	private static final Logger LOGGER = LogManager.getLogger();

	public static JsonObject migrate(JsonObject object) {
		for(int i = 0; i < values().length - 1; i++) {
			ConfigVersion current = values()[i];
			ConfigVersion ahead = values()[i + 1];

			object = current.attemptTransform(object, ahead.name());
		}

		return object;
	}

	public boolean check(JsonObject object) {
		return object.get("version").getAsString().equals(name());
	}

	/**
	 * Gets the next version if this is the current version.
	 *
	 * @param object The config object.
	 * @return The migrated config.
	 */
	public JsonObject attemptTransform(JsonObject object, String nextVersion) {
		if(!check(object)) {
			return object; // Skip to next version.
		}

		LOGGER.info("Migrating config from {} to {}...", name(), nextVersion);

		object = transformToNext(object);
		object.addProperty("version", nextVersion);

		return object;
	}

	/**
	 * Transform a config to the next version (below the current one).
	 *
	 * @param object The config object.
	 * @return The migrated config.
	 */
	protected JsonObject transformToNext(JsonObject object) {
		throw new IllegalStateException("Could not identify config version");
	}

}
