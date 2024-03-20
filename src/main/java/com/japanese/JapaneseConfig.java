package com.japanese;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("japanese")
public interface JapaneseConfig extends Config {
    @ConfigSection(
            name = "API無し・キー欄未記入時",
            description = "APIキーが使えない場合の設定",
            position = 1
    )
    String translateMethodSection = "翻訳方法の設定";
    enum TranslatorConfig
    {
        簡易翻訳,
        翻訳しない
    }
    @ConfigItem(
            position = 1,
            keyName = "translatorChoice",
            name = "API無使用時",
            description = "簡易的な翻訳機は即使用可能だが低質。" +
                    "API翻訳を使用する場合は各々のサイトでアカウント作成・APIキー取得が必要。",
            section = translateMethodSection
    )
    default TranslatorConfig translatorOption() {return TranslatorConfig.簡易翻訳;}

    @ConfigSection(
            name = "APIキー入力欄",
            description = "APIを使う場合の設定",
            position = 2
    )
    String APIOptions = "APIキーの設定欄";

//    enum NoAPIConfig
//    {
//        簡易翻訳,
//        翻訳しない,
//        課金して続行
//    }
//    @ConfigItem(
//            position = 1,
//            keyName = "APIkey",
//            name = "無料分不足時",
//            description = "各翻訳サービスの無料使用には文字制限があります。それを使い切りそうな場合の翻訳方法を選択",
//            section = APIOptions
//    )
//    default NoAPIConfig NoAPIConfig() {return NoAPIConfig.簡易翻訳;}
    @ConfigItem(
            position = 2,
            keyName = "DeeplAPIOption",
            name = "DeepLのAPIキー",
            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
            section = APIOptions
    )
    default String DeeplApiKey() {return "DeepLのアカウント作成後、キーを取得";}

    @ConfigItem(
            position = 4,
            keyName = "AzureAPIOption",
            name = "Azure AIのAPIキー",
            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
            section = APIOptions
    )
    default String AzureApiKey() {return "Azure AI登録後、キーを取得";}
    @ConfigItem(
            position = 6,
            keyName = "GoogleAPIOption",
            name = "Google CloudのAPIキー",
            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
            section = APIOptions
    )
    default String GoogleCloudApiKey() {return "Google Cloud 登録後、キーを取得";}
    @ConfigItem(
            position = 3,
            keyName = "DeeplTranslateCount",
            name = "DeepLの翻訳文字数を表示",
            description = "API翻訳で何文字翻訳したかオーバーレイで表示",
            section = APIOptions
    )
    default boolean DeeplAPICount() {return true;}
    @ConfigItem(
            position = 5,
            keyName = "AzureTranslateCount",
            name = "Azureの翻訳文字数を表示",
            description = "API翻訳で何文字翻訳したかオーバーレイで表示",
            section = APIOptions
    )
    default boolean AzureAPICount() {return true;}
    @ConfigItem(
            position = 7,
            keyName = "GoogleTranslateCount",
            name = "Google Cloudの翻訳文字数を表示",
            description = "API翻訳で何文字翻訳したかオーバーレイで表示",
            section = APIOptions
    )
    default boolean GoogleAPICount() {return true;}
/////////////////
    @ConfigSection(
            name = "ゲーム内テキスト",
            description = "ゲーム内で表示されるテキストをを翻訳するか、何もしないか",
            position = 40
    )

    String gameTransformChoice = "ゲーム内テキストの処理オプション";
    enum GameTextProcessChoice
    {
        そのまま,
        簡易翻訳,
        API翻訳,
    }
    @ConfigItem(
            position = 40,
            keyName = "DialogConfig",
            name = "NPCとの会話",
            description = "NPCとの会話の翻訳設定",
            section = gameTransformChoice
    )
    default GameTextProcessChoice npcDialogueConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 41,
            keyName = "InterfaceConfig",
            name = "各画面",
            description = "画面（インターフェース）の翻訳設定",
            section = gameTransformChoice
    )
    default GameTextProcessChoice interfaceConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 42,
            keyName = "GameMessageConfig",
            name = "ゲーム情報",
            description = "チャットボックス内に表示される、発言以外の文章の翻訳設定",
            section = gameTransformChoice
    )
    default GameTextProcessChoice gameMessageConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 43,
            keyName = "OtherMessageConfig",
            name = "その他",
            description = "ボタン、クエストダイアログ等のテキストの翻訳設定",
            section = gameTransformChoice
    )
    default GameTextProcessChoice widgetTextConfig() {return GameTextProcessChoice.簡易翻訳;}
    //    @ConfigItem(
//            position = 41,
//            keyName = "DialogOvlConfig",
//            name = "ダイアログオーバーレイ",
//            description = "会話する際に表示されるダイアログにオーバーレイを表示するか否か",
//            section = gameTransformChoice
//    )
//    default boolean npcNameOVRConfig() {return true;}
//    @ConfigItem(
//            position = 44,
//            keyName = "ItemNameOVRConfig",
//            name = "アイテム名オーバーレイ",
//            description = "マウスの横にアイテム名を表示するか否か",
//            section = gameTransformChoice
//    )
//    default boolean itemNameOVRConfig() {return true;}
//////////////////
    //////////////
    @ConfigSection(
            name = "発言の翻訳設定",
            description = "プレイヤーの発言の翻訳設定",
            position = 60
    )
    String chatTransformChoice = "発言の処理オプション";
    enum ChatCoonfig
    {
        そのまま表示,
        簡易翻訳,
        API翻訳,
        ローマ字変換
    }
    @ConfigItem(
            position = 60,
            keyName = "SelfConfig",
            name = "自分",
            description = "自分の発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig selfConfig() {return ChatCoonfig.ローマ字変換;}
    @ConfigItem(
            position = 61,
            keyName = "FriendConfig",
            name = "友達全員",
            description = "友達の発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig friendConfig() {return ChatCoonfig.そのまま表示;}
    @ConfigItem(
            position = 62,
            keyName = "PublicConfig",
            name = "公開",
            description = "公開チャットの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig publicConfig() {return ChatCoonfig.簡易翻訳;}

    @ConfigItem(
            position = 63,
            keyName = "ClanConfig",
            name = "クラン",
            description = "クランメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig clanConfig() {return ChatCoonfig.ローマ字変換;}
    @ConfigItem(
            position = 64,
            keyName = "ClanGuestConfig",
            name = "見学クラン",
            description = "見学中のクランメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig clanGuestConfig() {return ChatCoonfig.簡易翻訳;}
    @ConfigItem(
            position = 65,
            keyName = "FriendChatConfig",
            name = "フレチャ",
            description = "フレンドチャットにいるメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig friendChatConfig() {return ChatCoonfig.簡易翻訳;}
    @ConfigItem(
            position = 66,
            keyName = "GIMConfig",
            name = "GIMチャ",
            description = "GIMメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatCoonfig gimConfig() {return ChatCoonfig.ローマ字変換;}
//////

    @ConfigSection(
            name = "特定プレイヤーの翻訳設定",
            description = "プレイヤーの発言の翻訳設定",
            position = 70
    )
    String playerSpecific = "特定プレイヤーの発言の処理オプション";

    @ConfigItem(
            position = 71,
            keyName = "ForceDoNothing",
            name = "そのまま表示する人:強制",
            description = "上記に該当する人であってもこっちの設定が適用されます",
            section = playerSpecific
    )
    default String playerListDoNothing() {return "プレイヤー名を入力、カンマで区切れば複数名指定可";}
    @ConfigItem(
            position = 72,
            keyName = "ForceWord2Word",
            name = "簡易翻訳する人:強制",
            description = "上記に該当する人であってもこっちの設定が適用されます",
            section = playerSpecific
    )
    default String playerListWord2Word() {return "プレイヤー名を入力。カンマで区切れば複数名指定可";}
    @ConfigItem(
            position = 73,
            keyName = "ForceAPI",
            name = "API翻訳する人:強制",
            description = "上記に該当する人であってもこっちの設定が適用されます",
            section = playerSpecific
    )
    default String playerListAPI() {return "プレイヤー名を入力。カンマで区切れば複数名指定可";}
    @ConfigItem(
            position = 74,
            keyName = "ForceRom2Jap",
            name = "ローマ字変換する人:強制",
            description = "上記に該当する人であってもこっちの設定が適用されます",
            section = playerSpecific
    )
    default String playerListRom2Jap() {return "プレイヤー名を入力。カンマで区切れば複数名指定可";}
    ///////////////

    @ConfigItem(
            position = 99,
            keyName = "greeting",
            name = "name of config item 2",
            description = "The message to show to the user when they login"
            //section = playerSpecific
    )
    default String greeting() {return "hello";}
}
