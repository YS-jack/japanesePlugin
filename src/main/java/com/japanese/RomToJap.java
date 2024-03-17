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
    @Inject
    private JapanesePlugin japanesePlugin;
    public int inputCount = 0; //for counting number of words in chat input
    public String chatJpMsg = "";//whats written for chat input overlay
    public List<String> kanjKatCandidates = new ArrayList<>();//candidates of kanji or katakana from input, also used for candidates overlay
    public int instCandidateSelection = -1;
    private List<FourValues> japCharDS = new ArrayList<>();//store all japanese words's written form, how its read, type, rank
    private List<String> prevHiraList = new ArrayList<>();//stores the last updated words that are displayed
    private List<String> prevJPList = new ArrayList<>();
    private HashMap<String,String> char2char = new HashMap<>();
    private String notAvailable = "nan";

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
    public class FourValues {
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
        String newMsg = romJpTransform(inputmsg, true);
        if (!newMsg.isEmpty())
            chatJpMsg = newMsg;
    }
    public String romJpTransform(String romMsg, boolean chatInput) {
        String hiraMsg = rom2Kat(romMsg);
        List<String> ret = hira2Jp(hiraMsg, chatInput);
        if (ret == null)
            return "";
        else {
            StringBuilder inputBuilder = new StringBuilder();
            for (String written : ret)
                inputBuilder.append(written);
            return inputBuilder.toString();
        }
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
                        katBuilder.append("ん");
                    else
                        katBuilder.append(secToLast);//append q
                    katBuilder.append(ch); // append エ
                    romBuilder.setLength(0);
                    continue;
                }
                if (Pattern.matches(pattern, romBuffer)){//when n comes before a symbol or space, change it to ン
                    katBuilder.append("ん");
                    katBuilder.append(romBuffer.charAt(1));
                    romBuilder.setLength(0);
                    continue;
                }
            } else {//rombuffer size > 2
                if (Pattern.matches(pattern2, romBuffer)){//when n comes before a symbol or space, change it to ン
                    katBuilder.append(romBuffer, 0, romBufferSize-2);
                    katBuilder.append("ん");
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
                            katBuilder.append("っ");//append ッ
                            katBuilder.append(ch); // append ウェ
                            romBuilder.setLength(0);
                            continue;
                        }
                        if(romBuffer.charAt(romBufferSize - 4) == 'n'){// eg: xnwhe > xンウェ
                            katBuilder.append(romBuffer,0,romBufferSize-4);//append x
                            katBuilder.append("ん");//append ン
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
                        katBuilder.append("っ");//append ッ
                        katBuilder.append(ch); // append ウェ
                        romBuilder.setLength(0);
                        continue;
                    }
                    if(romBuffer.charAt(romBufferSize - 3) == 'n'){// eg: xnka > xンカ
                        katBuilder.append(romBuffer,0,romBufferSize-3);//append x
                        katBuilder.append("ん");//append ン
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
    private List<String> hira2Jp(String hiraMsg, boolean chatInput) {//hiragana list to kanji sentence
        String[] wordList = getWakatiGaki(hiraMsg);//get katakana text split with symbols, space attached at the end of each string if its katakana or numbers
        //eg of wordList : "強烈はぴはぴ閾値" "、," "凄く"  "!/" "だよ" "、" "kaka" "クェスト00" "やり" "33" "," "ましょう"
        List<String> wordListList = Arrays.asList(getWakatiGaki(hiraMsg));
        if (compareLists(wordListList, prevHiraList) && chatInput)
            return null;
        int startIndex = searchFirstDif(wordList);
        if (startIndex < 1)
            startIndex = 0;
        List<String> changedList = new ArrayList<>();
        boolean last;
        for (int i = startIndex; i < wordList.length; i++) {
            String word = wordList[i];
            last = i == wordList.length - 1;
            FourValues FVword = getMatch(word, last, chatInput);
            changedList.add(FVword.written);
        }
        List<String> sublistPrevJPList = prevJPList.subList(0, startIndex);
        String[] notChanged = sublistPrevJPList.toArray(new String[0]);
        ArrayList<String> combindedList = new ArrayList<>(Arrays.asList(notChanged));
        combindedList.addAll(changedList);

        prevHiraList = wordListList;
        prevJPList = combindedList;
        return combindedList;
    }

    private FourValues getMatch(String word, boolean last, boolean chatInput){//the last word in wordList
        FourValues FVofWord = new FourValues(word.trim().replaceAll("\\d",""),word.trim().replaceAll("\\d",""),notAvailable,-1);
        List<FourValues> newCandidates = new ArrayList<>();
        newCandidates.add(FVofWord);
        if (word.matches("[\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+\\d+$") ||//if theres a number at the end of strings, set candidate to that
                word.matches("[\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+ +$")){// if theres 1 or more space, #space - 1 = candidateN
            //show no candidates
            int candidateSelectionN;
            String wordPart;
            if (word.matches("[\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+\\d+$")) {
                String intPart = word.split("\\D+")[1].trim();
                candidateSelectionN = Integer.parseInt(intPart);
                wordPart = word.split("\\d+\\s*$")[0];
            } else {
                String spacePart = word.split("[\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+")[1];
                candidateSelectionN = spacePart.length() - 1;
                wordPart = word.split(" +$")[0];
            }
            if (chatInput)
                kanjKatCandidates.clear();
            newCandidates.addAll(getCandidates(wordPart));

            if (newCandidates.size() -1< candidateSelectionN) //if the selection is too large, return the last cand
                candidateSelectionN = newCandidates.size() - 1;

            if (last) {
                if (chatInput)
                    instCandidateSelection = candidateSelectionN;
                for (FourValues fv : newCandidates)
                    if (chatInput)
                        kanjKatCandidates.add(fv.written);
            } else if (chatInput)
                instCandidateSelection = -1;
            return newCandidates.get(candidateSelectionN);

        } else { //no japanese + number nor space at the end, add it as hiragana if its not the last word, if last word then look for candidates
            if (last) {//if its the last word on wordList, update candidates shown by overlay
                if (chatInput)
                    instCandidateSelection = -1;
                newCandidates.addAll(getCandidates(word));
                if (chatInput)
                    kanjKatCandidates.clear();
                for (FourValues fv : newCandidates)
                    if (chatInput)
                        kanjKatCandidates.add(fv.written);
                if (chatInput)
                    instCandidateSelection = -1;
            }
            return FVofWord;
        }
    }
    private List<FourValues> getCandidates(String word){
        List<FourValues> matches;
        List<FourValues> newCandidates = new ArrayList<>();

        matches = getAllMatches(word, 999);//get all exact matches
        newCandidates.addAll(matches);

        if (newCandidates.size() < 6) {//if not many candidates, get matches that begin with the last wordPart
            // (the last wordPart might be in the middle of being typed
            matches = getAllBeginningWith(word,5);
            newCandidates.addAll(matches);
        }
        newCandidates.sort(new compareFV());
        if (newCandidates.size() < 15) {//if still not many candidates
            // (the last wordPart might be in the middle of being typed
            int nWordsToAdd = 15 - newCandidates.size();
            List<FourValues> containedAndExtra;
            containedAndExtra = getAllContaining(word, nWordsToAdd); // get all words that is contained within "wordPart"
            newCandidates.addAll(containedAndExtra);
        }
        //newCandidates.sort(new compareFV());
        return  newCandidates;
    }
    private int searchFirstDif(String[] wordList){
        int upTo = 0;
        if (wordList.length > prevHiraList.size())
            upTo = prevHiraList.size();
        else
            upTo = wordList.length;
        for (int i = 0; i < upTo; i++)
            if (!Objects.equals(wordList[i], prevHiraList.get(i)))
                return i;
        return (upTo == 0 ? 0 : upTo-1);
    }
    public static <T> boolean compareLists(List<T> list1, List<T> list2) {
        // If lists have different sizes, they are not equal
        if (list1.size() != list2.size()) {
            return false;
        }

        // Compare elements of the lists
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }

        // If all elements match, lists are equal
        return true;
    }
    private List<FourValues> getAllMatches(String kata, int nToAdd) {
        List<FourValues> matches = new ArrayList<>();
        int count = 0;
        for (FourValues entry: japCharDS) {
            if (count > nToAdd)
                break;
            if (entry.read.equals(kata)){
                if (matches.isEmpty()) {
                    count++;
                    matches.add(entry);
                }
                else if (matches.get(matches.size() - 1).rank + 9 < entry.rank) {//dont add if the word is only different tense of the previous
                    count++;
                    matches.add(entry);
                }
            }
        }
        return matches;
    }
    private List<FourValues> getAllBeginningWith(String kata, int nToAdd) {
        List<FourValues> matches = new ArrayList<>();
        int count = 0;
        for (FourValues entry: japCharDS) {
            if (count > nToAdd)
                break;
            if (entry.read.startsWith(kata) && !entry.read.equals(kata)) {
                count++;
                matches.add(entry);
            }
        }
        return matches;
    }
    private List<FourValues> getAllContaining(String kata, int nToAdd) {//returns a substring 0:x that is in japCharDS, and add it to substring x:
        List<FourValues> matches = new ArrayList<>();
        int count = 0;
        for (int i = kata.length() - 1; i >= 0 && count < nToAdd; i--) {
            String substring = kata.substring(0,i);
            for (FourValues entry: japCharDS)
                if (entry.read.equals(substring)) {
                    FourValues addingFV = new FourValues(entry.written + kata.substring(i),
                            entry.written + kata.substring(i), notAvailable, entry.rank);
                    if (matches.isEmpty()) {
                        matches.add(addingFV);
                        count++;
                    }
                    else if (matches.get(matches.size() - 1).rank + 3 < addingFV.rank) {//only add few of the word is only different tense of the previous
                        count++;
                        if(count > nToAdd)
                            break;
                        matches.add(addingFV);
                    }
                }
        }
        return matches;
    }
    private String[] getWakatiGaki (String text) {
        // Regular expression pattern for splitting
        String regexPattern =
                "([\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]*\\d+)|" +
                        "([\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+ +)|" +
                        "([\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}]+)|"+
                        "([^\\p{IsAlphabetic}\\p{IsHiragana}\\p{IsKatakana}\\d ]+)";

        Pattern patternCompiled = Pattern.compile(regexPattern);
        Matcher matcher = patternCompiled.matcher(text);
        List<String> segmentsAndPunctuation = new ArrayList<>();

        // Find and collect all matches
        while (matcher.find()) {
            String segment = matcher.group(0);
            if (segment != null) {
                segmentsAndPunctuation.add(segment);
            }
        }

        // Convert the list to an array
        return segmentsAndPunctuation.toArray(new String[0]);
    }
}
