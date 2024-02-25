package com.japanese;

enum Colors {
    black("000000","black"),
    blue("0000ff","blue"),
    green("00ff00","green"),
    lightblue("00ffff","lightblue"),
    lightblue2("ffff", "lightblue"),
    orange("ff7000","orange"),
    orange2("ff9040","orange"),
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
        for (Colors Colors : values()) {
            if (Colors.getHex().equalsIgnoreCase(hex)) {
                return Colors;
            }
        }
        return black; // or throw an exception
    }
}