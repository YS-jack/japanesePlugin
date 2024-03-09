package com.japanese;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ChatIconManager;

import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import com.japanese.JapaneseConfig.*;
@Slf4j
public class RomToJap {
    private HashMap<String, String> r2Written;//read to written
    private HashMap<String, String> wordType;
    private HashMap<String, String> usageRank;
    private HashMap<String, String> char2char; //rom, kat
    @Inject
    private JapWidgets japWidgets = new JapWidgets();
    public String chatMsg = "";
    public String chatJpMsg = "";

    public void initRom2JpHash() throws Exception {
        String wordsDir = "src/main/resources/com/japanese/romToJap/romJap.csv";
        r2Written = new HashMap<>();
        wordType = new HashMap<>();
        usageRank = new HashMap<>();
        putWordToHash(r2Written, wordType, usageRank, wordsDir);

        String charDir = "src/main/resources/com/japanese/romToJap/romJapChar.csv";
        char2char = new HashMap<>();
        putCharToHash(char2char, charDir);

        log.info("end of init Rom2JP hash");
    }
    private void putWordToHash(HashMap<String, String> written, HashMap<String, String> type,
                               HashMap<String, String> rank, String dirName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(dirName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    written.put(parts[1].trim().toLowerCase(), parts[0].trim());
                    type.put(parts[1].trim().toLowerCase(), parts[2].trim());
                    rank.put(parts[1].trim().toLowerCase(), parts[3].trim());
                } else {
                    log.info("rom to jap.csv : not enough elements : "+parts.length);
                }
            }
        } catch (IOException e) {
            log.info("error creating hashmap for transform dict, for type : " + dirName);
            e.printStackTrace();
        }
    }
    private void putCharToHash(HashMap<String, String> hash, String dirName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(dirName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    hash.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            log.info("error creating hashmap for transform dict, for type : " + dirName);
            e.printStackTrace();
        }
    }
///<img=43>sand in sand: <col=0000ff> hi hi </col><col=0000ff>*</col>
    public void drawOverlay(Widget widget){
        String playerName = japWidgets.removeTag(widget.getText().split(": <col=0000ff>")[0]);
        chatMsg = japWidgets.removeTag(widget.getText().split("<col=0000ff>")[1]);
        if (chatMsg.isEmpty()) return;
        chatJpMsg = romJpTransform(chatMsg);
    }
    public String romJpTransform(String romMsg) {
        String katMsg = rom2Kat(romMsg);
        return katMsg;
    }

    public String rom2Kat(String romMsg) {
        StringBuilder katBuilder = new StringBuilder();
        StringBuilder romBuilder = new StringBuilder();
        for (int i = 0; i < romMsg.length(); i++) {
            romBuilder.append(romMsg.charAt(i));
            String romBuffer = romBuilder.toString();
            int romBufferSize = romBuffer.length();
            String katCandidate;

            if (romBufferSize == 0)//something went wrong
                return "";
            if (romBufferSize == 1) {
                katCandidate = romBuffer;
                if (char2char.containsKey(katCandidate)) {
                    String ch = char2char.get(katCandidate);
                    katBuilder.append(ch);
                    romBuilder.setLength(0);
                    continue;
                }
            } else if (romBufferSize == 2) {
                katCandidate = romBuffer;//eg: ka > カ
                if (char2char.containsKey(katCandidate)) {
                    String ch = char2char.get(katCandidate);
                    katBuilder.append(ch);
                    romBuilder.setLength(0);
                    continue;
                }
                katCandidate = romBuffer.substring(romBufferSize-1);
                if (char2char.containsKey(katCandidate)) {//eg:qe > qエ
                    String ch = char2char.get(katCandidate);
                    katBuilder.append(romBuffer.charAt(0));//append q
                    katBuilder.append(ch); // append エ
                    romBuilder.setLength(0);
                    continue;
                }
            } else{//rombuffer size > 2
                katCandidate = romBuffer.substring(romBufferSize-3);
                if (char2char.containsKey(katCandidate)) {
                    String ch = char2char.get(katCandidate);
                    if (romBufferSize > 3) {
                        if (romBuffer.charAt(romBufferSize - 4)
                                == romBuffer.charAt(romBufferSize - 3)){ // eg: xwwhe > xッウェ
                            katBuilder.append(romBuffer,0,romBufferSize-4);//append x
                            katBuilder.append("ッ");//append ッ
                            katBuilder.append(ch); // append ウェ
                            romBuilder.setLength(0);
                            continue;
                        }
                        if(romBuffer.charAt(romBufferSize - 4) == 'n'){// eg: xnwhe > xンウェ
                            katBuilder.append(romBuffer,0,romBufferSize-4);//append x
                            katBuilder.append("ン");//append ン
                            katBuilder.append(ch); // append ウェ
                            romBuilder.setLength(0);
                            continue;
                        }
                    }//eg:xxxwhe > xxxウェ
                    katBuilder.append(romBuffer, 0, romBufferSize-3);//append xxx
                    katBuilder.append(ch); // append ウェ
                    romBuilder.setLength(0);
                    continue;
                }
                katCandidate = romBuffer.substring(romBufferSize - 2);
                if (char2char.containsKey(katCandidate)) {
                    String ch = char2char.get(katCandidate);
                    if (romBuffer.charAt(romBufferSize - 3)
                            == romBuffer.charAt(romBufferSize - 2)){ // eg: xkka > xッカ
                        katBuilder.append(romBuffer,0,romBufferSize-3);//append x
                        katBuilder.append("ッ");//append ッ
                        katBuilder.append(ch); // append ウェ
                        romBuilder.setLength(0);
                        continue;
                    }
                    if(romBuffer.charAt(romBufferSize - 3) == 'n'){// eg: xnka > xンカ
                        katBuilder.append(romBuffer,0,romBufferSize-3);//append x
                        katBuilder.append("ン");//append ン
                        katBuilder.append(ch); // append カ
                        romBuilder.setLength(0);
                        continue;
                    }
                    //eg: xxka > xxカ
                    katBuilder.append(romBuffer, 0, romBufferSize-2);//append xx
                    katBuilder.append(ch); //append カ
                    romBuilder.setLength(0);
                    continue;
                }
                katCandidate = romBuffer.substring(romBufferSize - 1);
                if (char2char.containsKey(katCandidate)) {
                    String ch = char2char.get(katCandidate);
                    katBuilder.append(romBuffer, 0, romBufferSize-1);//append
                    katBuilder.append(ch);
                    romBuilder.setLength(0);
                }
            }
        }
        return katBuilder.toString();
    }
}
