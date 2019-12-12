import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Formatter {

    public Boolean isNotBlankLineOrComment(String line) {
        return line.length() >= 2 && !line.substring(0, 2).equals("//");
    }

    public String removeWhitespaceAndComments(String line) {
        int counter = 0;
        String formatted = "";
        char[] chars = line.toCharArray();
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