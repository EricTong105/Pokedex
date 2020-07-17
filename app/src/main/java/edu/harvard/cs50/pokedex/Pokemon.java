package edu.harvard.cs50.pokedex;

public class Pokemon {
    private String name;
    private String url;
//    private boolean caught = false;

    Pokemon(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

//    public boolean getCaught() {
//        return caught;
//    }
//
//    public boolean changeCaught() {
//        caught = !caught;
//        return caught;
//    }

    public boolean nameContains(CharSequence string) {
        int nameLength = name.length();
        int stringLength = string.length();
        int startIndex = 0;
        int endIndex = stringLength;
        while (!(endIndex > nameLength)) {
            if ((name.substring(startIndex, endIndex)).equalsIgnoreCase(string.toString())) {
                return true;
            }
            startIndex++;
            endIndex++;
        }
        return false;
    }
}
