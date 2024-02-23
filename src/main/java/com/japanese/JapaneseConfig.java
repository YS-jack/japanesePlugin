package com.japanese;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import java.awt.image.BufferedImage;
@ConfigGroup("japanese")
public interface JapaneseConfig extends Config {
    @ConfigSection(
            name = "翻訳設定",
            description = "翻訳方法に関する設定をする",
            position = 1
    )
    String translateMethodSection = "翻訳方法の設定";
    enum TranslatorConfig
    {
        簡易翻訳,
        DeepL翻訳
    }
    @ConfigItem(
            position = 1,
            keyName = "translatorChoice",
            name = "翻訳方法",
            description = "簡易的な翻訳機は即使用可能だが低質。" +
                    "DeepLを使用する場合は各々のサイトでアカウント作成・APIキー取得が必要。",
            section = translateMethodSection
    )
    default TranslatorConfig translatorOption() {return TranslatorConfig.簡易翻訳;}

    @ConfigSection(
            name = "DeepLを使う場合の設定",
            description = "DeepLを使う場合の設定",
            position = 2
    )
    String DeepLOptions = "DeepLの設定";

    enum NoDeepLConfig
    {
        簡易翻訳,
        翻訳しない
    }
    @ConfigItem(
            position = 2,
            keyName = "NoneDeepLOption",
            name = "APIキー入力（共有厳禁！）",
            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
            section = DeepLOptions
    )
    default String ApiKeyString() {return "DeepLアカウント作成後、キーを取得";}
    @ConfigItem(
            position = 3,
            keyName = "APIkey",
            name = "APIキー無効時",
            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
            section = DeepLOptions
    )
    default NoDeepLConfig noDeepLConfig() {return NoDeepLConfig.簡易翻訳;}
    @ConfigItem(
            position = 4,
            keyName = "DeepLCount",
            name = "DeepLの翻訳文字数を表示",
            description = "DeepLで何文字翻訳したかオーバーレイで表示",
            section = DeepLOptions
    )
    default boolean deeplCount() {return true;}


    //////////////
    @ConfigSection(
            name = "人の発言",
            description = "プレイヤーの発言を翻訳するか、ローマ字変換するか、何もしないか",
            position = 10
    )
    String chatTransformChoice = "発言の処理オプション";
    enum ChatTextProcessChoice
    {
        そのまま表示,
        簡易翻訳,
        DeepL翻訳,
        ローマ字変換
    }
    @ConfigItem(
            position = 10,
            keyName = "SelfConfig",
            name = "自分",
            description = "自分の発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice selfConfig() {return ChatTextProcessChoice.ローマ字変換;}
    @ConfigItem(
            position = 11,
            keyName = "FriendConfig",
            name = "友達全員",
            description = "友達の発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice friendConfig() {return ChatTextProcessChoice.そのまま表示;}
    @ConfigItem(
            position = 12,
            keyName = "PublicConfig",
            name = "全チャ",
            description = "公開チャットの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice publicConfig() {return ChatTextProcessChoice.簡易翻訳;}

    @ConfigItem(
            position = 13,
            keyName = "ClanConfig",
            name = "クラン",
            description = "クランメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice clanConfig() {return ChatTextProcessChoice.ローマ字変換;}
    @ConfigItem(
            position = 14,
            keyName = "ClanGuestConfig",
            name = "見学クラン",
            description = "見学中のクランメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice clanGuestConfig() {return ChatTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 15,
            keyName = "FriendChatConfig",
            name = "フレチャ",
            description = "フレンドチャットにいるメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice friendChatConfig() {return ChatTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 16,
            keyName = "GIMConfig",
            name = "GIMチャ",
            description = "GIMメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatTextProcessChoice gimConfig() {return ChatTextProcessChoice.ローマ字変換;}
    
    ///////////////
    @ConfigSection(
            name = "ゲーム内テキスト",
            description = "ゲーム内で表示されるテキストをを翻訳するか、何もしないか",
            position = 40
    )

    String gameTextTransformChoice = "ゲーム内テキストの処理オプション";
    enum GameTextProcessChoice
    {
        そのまま,
        簡易翻訳,
        DeepL翻訳,
    }
    @ConfigItem(
            position = 40,
            keyName = "DialogConfig",
            name = "NPCとの会話",
            description = "NPCとの会話の翻訳設定",
            section = gameTextTransformChoice
    )
    default GameTextProcessChoice npcDialogueConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 41,
            keyName = "GameMessageConfig",
            name = "ゲーム情報",
            description = "NPCとの会話の翻訳設定",
            section = gameTextTransformChoice
    )
    default GameTextProcessChoice gameMessageConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 42,
            keyName = "OtherMessageConfig",
            name = "上記以外",
            description = "上記以外の様々なテキストの翻訳設定",
            section = gameTextTransformChoice
    )
    default GameTextProcessChoice otherMessageConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 43,
            keyName = "NpcNameOVRConfig",
            name = "NPC名オーバーレイ",
            description = "マウスの横にNPC名を表示するか否か",
            section = gameTextTransformChoice
    )
    default boolean npcNameOVRConfig() {return true;}
    @ConfigItem(
            position = 44,
            keyName = "ItemNameOVRConfig",
            name = "アイテム名オーバーレイ",
            description = "マウスの横にアイテム名を表示するか否か",
            section = gameTextTransformChoice
    )
    default boolean itemNameOVRConfig() {return true;}
//////////////////
    @ConfigItem(
            position = 99,
            keyName = "greeting",
            name = "name of config item 2",
            description = "The message to show to the user when they login"
    )
    default String greeting() {return "hello";}
}
