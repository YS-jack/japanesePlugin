package com.japanese;

import java.util.Comparator;

public class compareFV implements Comparator<RomToJap.FourValues> {
    public int compare(RomToJap.FourValues fv1, RomToJap.FourValues fv2) {
        return Integer.compare(fv1.getRank(), fv2.getRank());
    }
}
