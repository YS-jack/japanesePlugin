package com.japanese;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(JapaneseConfig.GROUP)
public interface JapaneseConfig extends Config {
    String GROUP = "日本語翻訳";
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
            name = "DeepL無使用時",
            description = "簡易的な翻訳機は即使用可能だが低質。" +
                    "DeepL翻訳を使用する場合はアカウント作成・APIキー取得が必要。",
            section = translateMethodSection
    )
    default TranslatorConfig translatorOption() {return TranslatorConfig.簡易翻訳;}

    @ConfigSection(
            name = "DeepLのAPI設定ラン",
            description = "DeepLのAPIをの設定",
            position = 2
    )
    String APIOptions = "APIキーの設定欄";

//    enum NoAPIConfig
//    {
//        簡易翻訳,
//        翻訳しない,
//        課金して続行
//    }
    @ConfigItem(
            position = 1,
            keyName = "useDeepl",
            name = "DeepLを使用",
            description = "各翻訳サービスの無料使用には文字制限があります。それを使い切りそうな場合の翻訳方法を選択",
            section = APIOptions
    )
    default boolean useDeepl() {return false;}

    @ConfigItem(
            secret = true,
            position = 2,
            keyName = "DeeplAPIOption",
            name = "DeepLのAPIキー",
            description = "簡易翻訳以外を使用する場合、DeepLのアカウント作成後、APIキーを取得してここに入力してください。",
            section = APIOptions
    )

    default String DeeplApiKey() {return "DeepLのアカウント作成後、キーを取得";}

//    @ConfigItem(
//            position = 4,
//            keyName = "AzureAPIOption",
//            name = "Azure AIのAPIキー",
//            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
//            section = APIOptions
//    )
//    default String AzureApiKey() {return "Azure AI登録後、キーを取得";}
//    @ConfigItem(
//            position = 6,
//            keyName = "GoogleAPIOption",
//            name = "Google CloudのAPIキー",
//            description = "簡易翻訳以外を使用する場合、各サービスのサイトでアカウントを作成し、APIキーを取得してここに入力してください。",
//            section = APIOptions
//    )
//    default String GoogleCloudApiKey() {return "Google Cloud 登録後、キーを取得";}
    @ConfigItem(
            position = 3,
            keyName = "DeeplTranslateCount",
            name = "DeepLの翻訳文字数を表示",
            description = "DeepL翻訳で何文字翻訳したかオーバーレイで表示",
            section = APIOptions
    )
    default boolean DeeplAPICount() {return true;}
//    @ConfigItem(
//            position = 5,
//            keyName = "AzureTranslateCount",
//            name = "Azureの翻訳文字数を表示",
//            description = "API翻訳で何文字翻訳したかオーバーレイで表示",
//            section = APIOptions
//    )
//    default boolean AzureAPICount() {return true;}
//    @ConfigItem(
//            position = 7,
//            keyName = "GoogleTranslateCount",
//            name = "Google Cloudの翻訳文字数を表示",
//            description = "API翻訳で何文字翻訳したかオーバーレイで表示",
//            section = APIOptions
//    )
//    default boolean GoogleAPICount() {return true;}
/////////////////
    @ConfigSection(
            name = "ゲーム内テキストの翻訳設定",
            description = "ゲーム内で表示されるテキストをを翻訳するか、何もしないか",
            position = 40
    )

    String gameTransformChoice = "ゲーム内テキストの処理オプション";
    enum GameTextProcessChoice
    {
        そのまま,
        簡易翻訳,
        DeepL翻訳,
    }
    enum jpEnChoice
    {
        英語,
        日本語
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
            position = 42,
            keyName = "MenuEntryConfig",
            name = "マウス周辺",
            description = "マウスを置いたり右クリックすると表示されるテキストの翻訳設定",
            section = gameTransformChoice
    )
    default jpEnChoice menuEntryConfig() {return jpEnChoice.日本語;}
    @ConfigItem(
            position = 41,
            keyName = "GameMessageConfig",
            name = "ゲーム情報",
            description = "チャットボックス内に表示される、発言以外の文章の翻訳設定",
            section = gameTransformChoice
    )
    default GameTextProcessChoice gameMessageConfig() {return GameTextProcessChoice.簡易翻訳;}
    @ConfigItem(
            position = 43,
            keyName = "OtherMessageConfig",
            name = "画面やボタンなど上記以外全て",
            description = "ボタン、クエストダイアログ等のテキストの翻訳設定",
            section = gameTransformChoice
    )
    default jpEnChoice widgetTextConfig() {return jpEnChoice.日本語;}
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
    enum ChatConfig
    {
        そのまま表示,
        簡易翻訳,
        DeepL翻訳,
        ローマ字変換
    }
    enum ChatConfigSelf
    {
        そのまま表示,
        ローマ字変換
    }
    @ConfigItem(
            position = 60,
            keyName = "SelfConfig",
            name = "自分",
            description = "自分の発言の変換設定",
            section = chatTransformChoice
    )
    default ChatConfigSelf selfConfig() {return ChatConfigSelf.ローマ字変換;}
    @ConfigItem(
            position = 61,
            keyName = "FriendConfig",
            name = "友達全員",
            description = "友達の発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatConfig friendConfig() {return ChatConfig.そのまま表示;}
    @ConfigItem(
            position = 62,
            keyName = "PublicConfig",
            name = "公開",
            description = "公開チャットの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatConfig publicConfig() {return ChatConfig.簡易翻訳;}

    @ConfigItem(
            position = 63,
            keyName = "ClanConfig",
            name = "クラン",
            description = "クランメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatConfig clanConfig() {return ChatConfig.ローマ字変換;}
    @ConfigItem(
            position = 64,
            keyName = "ClanGuestConfig",
            name = "見学クラン",
            description = "見学中のクランメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatConfig clanGuestConfig() {return ChatConfig.簡易翻訳;}
    @ConfigItem(
            position = 65,
            keyName = "FriendChatConfig",
            name = "フレチャ",
            description = "フレンドチャットにいるメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatConfig friendChatConfig() {return ChatConfig.簡易翻訳;}
    @ConfigItem(
            position = 66,
            keyName = "GIMConfig",
            name = "GIMチャ",
            description = "GIMメンバーの発言の翻訳・変換設定",
            section = chatTransformChoice
    )
    default ChatConfig gimConfig() {return ChatConfig.ローマ字変換;}
//////

    @ConfigSection(
            name = "特定プレイヤーの翻訳設定",
            description = "プレイヤーの発言の強制翻訳",
            position = 70
    )
    String playerSpecific = "特定プレイヤーの発言の処理オプション";

    @ConfigItem(
            position = 71,
            keyName = "ForceDoNothing",
            name = "そのまま表示する人の名前",
            description = "「発言の翻訳設定」より優先的に適用されます",
            section = playerSpecific
    )
    default String playerListDoNothing() {return "プレイヤー名を入力。「,」で区切れば複数名指定可";}
    @ConfigItem(
            position = 72,
            keyName = "ForceWord2Word",
            name = "簡易翻訳する人の名前",
            description = "「発言の翻訳設定」より優先的に適用されます",
            section = playerSpecific
    )
    default String playerListWord2Word() {return "プレイヤー名を入力。カンマで区切れば複数名指定可";}
    @ConfigItem(
            position = 73,
            keyName = "ForceAPI",
            name = "DeepL翻訳する人の名前",
            description = "「発言の翻訳設定」より優先的に適用されます",
            section = playerSpecific
    )
    default String playerListAPI() {return "プレイヤー名を入力。カンマで区切れば複数名指定可";}
    @ConfigItem(
            position = 74,
            keyName = "ForceRom2Jap",
            name = "ローマ字変換する人の名前",
            description = "「発言の翻訳設定」より優先的に適用されます",
            section = playerSpecific
    )
    default String playerListRom2Jap() {return "プレイヤー名を入力。カンマで区切れば複数名指定可";}
    @ConfigSection(
            name = "翻訳データ提供",
            description = "翻訳に使うデータを送信します",
            position = 90
    )
    String forDevelopers = "for better translation";
    @ConfigItem(
            position = 90,
            keyName = "webhookUrl",
            name = "webhook URL",
            description = "本プラグインのdiscordサーバーで得られるURLを入力してください  ",
            section = forDevelopers,
            secret = true
    )
    default String webHookUrl() {return "";}

}
