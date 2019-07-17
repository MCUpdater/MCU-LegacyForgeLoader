package org.mcupdater.forgeloader;

import com.google.common.base.Predicate;
import net.minecraftforge.installer.*;

import java.io.*;
import java.nio.file.Files;

public class ForgeLoader {

	public static void main(String[] args) {
		File installPath = new File(args[0]);
		String side = args[1];

		File profiles = new File(installPath, "launcher_profiles.json");
		if (!profiles.exists()) {
			InputStream inStream = ForgeLoader.class.getResourceAsStream("/launcher_profiles.json");
			try {
				Files.copy(inStream, profiles.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		OptionalListEntry[] optionals = new OptionalListEntry[0];
		if (VersionInfo.hasOptionals()) {
			optionals = new OptionalListEntry[VersionInfo.getOptionals().size()];
			int x = 0;

			for(OptionalLibrary opt : VersionInfo.getOptionals()){
				optionals[x++] = new OptionalListEntry(opt);
			}
		}
		ActionType action;
		switch(side.toLowerCase()) {
			case "client":
				action = new ClientInstall();
				break;
			case "server":
				action = new ServerInstall();
				break;
			default:
				action = new ExtractAction();
		}
		final OptionalListEntry[] entries = optionals;
		Predicate<String> optPred = new Predicate<String>() {
			public boolean apply(String input) {
				if (entries == null) {
					return true;
				} else {
					OptionalListEntry[] entryArray = entries;
					int arrSize = entryArray.length;

					for (int y = 0; y < arrSize; ++y) {
						OptionalListEntry ent = entryArray[y];
						if (ent.lib.getArtifact().equals(input)) {
							return ent.isEnabled();
						}
					}

					return false;
				}
			}
		};
		action.run(installPath, optPred);
	}

	private static class OptionalListEntry {
		OptionalLibrary lib;
		private boolean enabled = false;

		OptionalListEntry(OptionalLibrary lib) {
			this.lib = lib;
			this.enabled = lib.getDefault();
		}

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean v) {
			this.enabled = v;
		}
	}
}
