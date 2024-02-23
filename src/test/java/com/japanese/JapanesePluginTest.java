package com.japanese;

import com.japanese.JapanesePlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class JapanesePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(JapanesePlugin.class);
		RuneLite.main(args);
	}
}