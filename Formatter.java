import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Formatter {

    public Boolean isBlankLine(String line) {
        if(line.length() == 0) {
            return true;
        }
        char[] chars = line.toCharArray();
        int counter = 0;
        int length = line.length();
        Boolean hasNonBlank = false;
        while(counter < length && !hasNonBlank) {
            if(chars[counter] != ' '
                && chars[counter] != '\t'
                && chars[counter] != '\r'
                && chars[counter] != '\n'
            ) {
                hasNonBlank = true;
            }
            counter++;
        }
        return !hasNonBlank;
    }

    public Boolean isComment(String line) {
        char[] chars = line.toCharArray();
        int counter = 0;
        int length = line.length();
        String lineString = "";
        while(counter < length && lineString.length() < 2) {
            if(chars[counter] != ' '
                && chars[counter] != '\t'
                && chars[counter] != '\r'
                && chars[counter] != '\n'
            ) {
                lineString += chars[counter];
            }
            counter++;
        }
        if(
            lineString.length() >= 2
            && (lineString.substring(0, 1).equals("*")
            || lineString.substring(0, 2).equals("/*")
            || lineString.substring(0, 2).equals("//")
            || lineString.substring(0, 2).equals("*/")) 
        ) {
            return true;
        }
        return false;
    }

    public Boolean isNotBlankLineOrComment(String line) {
        return !(isBlankLine(line) || isComment(line));
    }

    public String removeWhitespaceAndComments(String line) {
        int counter = 0;
        String formatted = "";
        char[] chars = line.toCharArray();
        System.out.println(line);
        while(chars.length == 0 || chars[counter] == ' ') {
            counter++;
        }
        while(
            counter < chars.length &&
            chars[counter] != '/') {
                formatted = formatted + chars[counter];
                counter++;
            }
        return formatted;
    }

    public List<String> format(ArrayList<String> contents) {
        List<String> formattedContents = contents
            .stream()
            .filter(line -> isNotBlankLineOrComment(line))
            .map(line -> removeWhitespaceAndComments(line))
            .collect(Collectors.toList());
        
        return formattedContents;
    }
}