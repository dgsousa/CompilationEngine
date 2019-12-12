import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileParser {
    String path;
    String inputExt = "jack";
    String outputExt = "xml";

    public Boolean validateFileType(String path) {
        String[] fileNameArr = path.split("\\.");
        String ext = fileNameArr[fileNameArr.length - 1];
        return ext.equals(this.inputExt);
    }

    public String getOutputFilePath(String path) {
        String[] fileNameArr = path.split("\\/");
        String outputFile = fileNameArr[fileNameArr.length - 1].split("\\.")[0] + "." + this.outputExt;
        fileNameArr[fileNameArr.length - 1] = outputFile;
        return String.join("/", fileNameArr);
    }

    public String getShortFileName(String path) {
        String[] fileNameArr = path.split("\\/");
        String[] fileNameArr2 = fileNameArr[fileNameArr.length - 1].split("\\.");
        return fileNameArr2[0];
    }

    public BufferedReader getBufferedReader(String path) throws Exception {
        File file = new File(path);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        return br;
    }

    public BufferedWriter getBufferedWriter(String path) throws Exception {
        File file = new File(path);
        FileWriter fr = new FileWriter(file);
        BufferedWriter br = new BufferedWriter(fr);
        return br;
    }

    public void translateFile(BufferedReader br, BufferedWriter bw, String fileName) throws Exception {
        Formatter formatter = new Formatter();
        // Translator translator = new Translator(fileName);
        String st;
        ArrayList<String> contents = new ArrayList<String>();
        
        while((st = br.readLine()) != null) {
            contents.add(st);
        }
        
        List<String> formattedContents = formatter.format(contents);
        // List<String> jackCode = translator.translate(formattedContents);
        formattedContents.forEach(line -> {
            try {
                bw.write(line + "\n");
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
        br.close();
        bw.close();
    }

    public void recurseThroughFiles(String args[]) throws Exception {
        for (String path: args) {
            File filePath = new File(path);
            if(filePath.isDirectory()) {
                File[] listOfFiles = filePath.listFiles();
                String[] fileNames = new String[listOfFiles.length];
                for(int i = 0; i < listOfFiles.length; i++) {
                    fileNames[i] = path + "/" + listOfFiles[i].getName();
                }
                recurseThroughFiles(fileNames);
            } else if(filePath.isFile()) {
                Boolean isValidFileType = this.validateFileType(path);
                if(isValidFileType) {
                    String fileName = this.getShortFileName(path);
                    String outputFileName = this.getOutputFilePath(path);
                    BufferedReader br = this.getBufferedReader(path);
                    BufferedWriter bw = this.getBufferedWriter(outputFileName);
                    this.translateFile(br, bw, fileName);
                }
            }
        }
    }

    public FileParser(String args[]) throws Exception {
        this.recurseThroughFiles(args);
    }
}