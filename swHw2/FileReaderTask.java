import java.io.*;
import java.util.List;

public class FileReaderTask implements Runnable {
    private String fileName;
    private List<Book> catalog;

    public FileReaderTask(List<Book> catalog, String fileName) {
        this.catalog = catalog;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Book b = parseBook(line);
                    if (b != null){
                        catalog.add(b);
                        LibraryBookTracker.validRecords++; 
                    }
                } catch (Exception e) {
                    LibraryBookTracker.errors++;
                }
            }
        } catch (IOException e) {
            System.err.println("File Error: " + e.getMessage());
        }
    }

    public static Book parseBook(String line) throws Exception {
        String[] parts = line.split(":");
        if (parts.length != 4) throw new Exception("Invalid format");
        return new Book(
            parts[0].trim(),
            parts[1].trim(),
            parts[2].trim(),
            Integer.parseInt(parts[3].trim()));
    }
}