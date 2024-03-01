package com.japanese;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Getter
@Slf4j
enum Colors {
    black("000000","black"),
    black2("0","black"),
    blue("0000ff","blue"),
    blue2("ff","blue"),
    green("00ff00","green"),
    green2("ff00","green"),
    lightblue("00ffff","lightblue"),
    lightblue2("ffff", "lightblue"),
    orange("ff7000","orange"),
    orange2("ff9040","orange"),
    orange3("ff981f","orange"),
    red("ff0000","red"),
    white("ffffff","white"),
    yellow("ffff00", "yellow");

    private String name;
    private String hex;

    private Colors(String hex, String name) {
        this.hex = hex;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getHex() {
        return hex;
    }

//    public static Colors fromName(String name) {
//        for (Colors Colors : values()) {
//            if (Colors.getName().equalsIgnoreCase(name)) {
//                return Colors;
//            }
//        }
//        return black; // or throw an exception
//    }

    public static Colors fromHex(String hex) {
        int[] colorInts = new int[Colors.values().length];//number of colors

        for (int i = 0; i < Colors.values().length;i++) {
            colorInts[i] = hexToInt(Colors.values()[i].getHex());
        }
        int j = findClosest(hexToInt(hex),colorInts);
        //log.info("color = " + Colors.values()[j]);
        return Colors.values()[j]; // or throw an exception
    }
    private static int findClosest(int target, int[] numbers) {
        int smallestDifference = Math.abs(numbers[0] - target);
        int closestI = 0;
        for (int i = 1; i < numbers.length; i++) {
            int currentDifference = Math.abs(numbers[i] - target);
            if (currentDifference < smallestDifference) {
                smallestDifference = currentDifference;
                closestI = i;
            }
        }
        return closestI;
    }
    public static String IntToHex(int intColor) {
        String hexString = String.format("%06x",intColor);
        for (Colors Colors : values()) {
            if (Colors.getHex().equalsIgnoreCase(hexString)) {
                return Colors.getHex();
            }
        }
        return black.getHex(); // or throw an exception
    }

    public static int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }
}