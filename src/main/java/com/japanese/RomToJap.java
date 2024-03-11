package com.japanese;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RomToJap {
    @Inject
    private JapWidgets japWidgets = new JapWidgets();
    public int inputCount = 0; //for counting number of words in chat input
    public String chatJpMsg = "";
    public String[] kanjKatCandidates = {};//candidates of kanji or katakana from input
    private List<FourValues> japCharDS = new ArrayList<>();
    private HashMap<String,String> char2char = new HashMap<>();

    public void initRom2JpHash() throws Exception {
        String wordsDir = "src/main/resources/com/japanese/romToJap/romJap.csv";
        putWordToHash(wordsDir);

        String charDir = "src/main/resources/com/japanese/romToJap/romJapChar.csv";
        char2char = new HashMap<>();
        putCharToHash(char2char, charDir);

        log.info("end of init Rom2JP hash");
    }
    private void putWordToHash(String dirName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(dirName), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    FourValues fourValues = new FourValues(parts[0],parts[1],parts[2],Integer.parseInt(parts[3]));
                    japCharDS.add(fourValues);
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
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    hash.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            log.info("error creating hashmap for transform dict, for type : " + dirName);
            e.printStackTrace();
        }
    }
    class FourValues {
        @Getter@Setter
        private String written;
        @Getter@Setter
        private String read;
        @Getter@Setter
        private String type;
        @Getter@Setter
        private int rank;

        public FourValues(String value1, String value2, String value3, int value4) {
            this.written = value1;
            this.read = value2;
            this.type = value3;
            this.rank = value4;
        }
    }
///<img=43>sand in sand: <col=0000ff> hi hi </col><col=0000ff>*</col>
    public void drawOverlay(Widget widget){
        String playerName = japWidgets.removeTag(widget.getText().split(": <col=0000ff>")[0]);
        String inputmsg = japWidgets.removeTag(widget.getText().split("<col=0000ff>")[1]);
        inputCount = inputmsg.length();
        if (inputmsg.isEmpty()){
            return;
        }
        chatJpMsg = romJpTransform(inputmsg);
    }
    public String romJpTransform(String romMsg) {
        String katMsg = rom2Kat(romMsg);
        return rom2Jp(katMsg);
    }

    public String rom2Kat(String romMsg) {
        StringBuilder katBuilder = new StringBuilder();
        StringBuilder romBuilder = new StringBuilder();
        String pattern = "n[,.!?;:#$%&()'\\s]$";
        String pattern2 = ".+n[,.!?;:#$%&()'\\s]$";
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
                    Character secToLast = romBuffer.charAt(0);
                    if (Pattern.matches(pattern, romBuffer)) //when n comes before a symbol or space, change it to ン
                        katBuilder.append("ン");
                    else
                        katBuilder.append(secToLast);//append q
                    katBuilder.append(ch); // append エ
                    romBuilder.setLength(0);
                    continue;
                }
                if (Pattern.matches(pattern, romBuffer)){//when n comes before a symbol or space, change it to ン
                    katBuilder.append("ン");
                    katBuilder.append(romBuffer.charAt(1));
                    romBuilder.setLength(0);
                    continue;
                }
            } else {//rombuffer size > 2
                if (Pattern.matches(pattern2, romBuffer)){//when n comes before a symbol or space, change it to ン
                    katBuilder.append(romBuffer, 0, romBufferSize-2);
                    katBuilder.append("ン");
                    String lastChar = Character.toString(romBuffer.charAt(romBufferSize-1));
                    katBuilder.append(char2char.getOrDefault(lastChar, lastChar));
                    romBuilder.setLength(0);
                    continue;
                }
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
        katBuilder.append(romBuilder.toString());
        return katBuilder.toString();
    }
    private String rom2Jp(String katMsg) {//katakana list to kanji sentence
        String[] wordList = getWakatiGaki(katMsg);//get katakana text split with symbols, space attached at the end of each string if its katakana or numbers
        //eg of wordList : "強烈はぴはぴ閾値" "、," "凄く"  "!/" "だよ" "、" "kaka" "クェスト00" "やり" "33" "," "ましょう"
        StringBuilder sentenceBuilder = new StringBuilder();
        String symbolPart = "([^\\p{IsAlphabetic}\\d\\p{IsHiragana}\\p{IsKatakana}\\p{Nd}]+[\\s\u3000]*)";
        FourValues prevKata = null;
        for (int i = 0; i < wordList.length; i++) {
            String word  = wordList[i];
            if (Pattern.matches(symbolPart, word)) {//if its symbol, dont change
                sentenceBuilder.append(word);
                prevKata = null;
                continue;
            }
            FourValues newKata = getKatKanWord(word, prevKata);//turn word into kanji+hiragana+katakana
            //todo; check if the new kata(prevkata) is valid grammar wise
            sentenceBuilder.append(newKata.written);

            prevKata = newKata;
        }
        //kanjKatCandidates = wordList;
        return sentenceBuilder.toString();
        //return katMsg;
    }

    private FourValues getKatKanWord(String word, FourValues prevKata) {
        int candidateSelectionN = 0;
        String wordPart = word;
        List<FourValues> transformCandidates = new ArrayList<>();
        if (word.matches(".+\\d+\\s*$")){
            String intPart = word.split("\\D+")[1].trim();
            if (!intPart.isEmpty()) {
                candidateSelectionN = Integer.parseInt(intPart);
                wordPart = word.split("\\d+\\s*$")[0];
            }
        }
        for (int i = 0; i < wordPart.length(); i++){// i words shorter
            transformCandidates.addAll(getAllMatches(wordPart.substring(0,wordPart.length()-i)));
            if (!transformCandidates.isEmpty()){
                break;
            }
        }

        String[] writtenArray = new String[transformCandidates.size()];
        for (int i = 0; i < transformCandidates.size(); i++) {
            writtenArray[i] = transformCandidates.get(i).getWritten();
        }
        kanjKatCandidates = writtenArray;

        return transformCandidates.get(candidateSelectionN);
    }
    private List<FourValues> getAllMatches(String kata) {
        List<FourValues> matches = new ArrayList<>();
        for (FourValues entry: japCharDS) {
            if (entry.read.equals(kata)){
                if (matches.isEmpty())
                    matches.add(entry);
                else if (matches.get(matches.size() - 1).rank + 9 < entry.rank)//dont add if the word is only different tense of the previous
                    matches.add(entry);
            }
        }
        return matches;
    }
    private String[] getWakatiGaki (String text) {
        // Regular expression pattern for splitting
        String regexPattern = "([\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}\\p{Nd}]+\\d+)|" +
                "([\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}\\p{Nd}]+)|" +
                "([\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}\\p{Nd}]+[^\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}\\p{Nd}\\d\\s\u3000]+)|"+
                "([^\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}\\p{Nd}\\d\\s\u3000]+)";

        Pattern patternCompiled = Pattern.compile(regexPattern);
        Matcher matcher = patternCompiled.matcher(text);
        List<String> segmentsAndPunctuation = new ArrayList<>();

        // Find and collect all matches
        while (matcher.find()) {
            String segment = matcher.group(0);
            if (segment != null) segmentsAndPunctuation.add(segment);
        }

        // Convert the list to an array
        return segmentsAndPunctuation.toArray(new String[0]);
    }
}
