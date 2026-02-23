import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LibraryBookTracker {

    static int validRecords = 0;
    static int results = 0;
    static int added = 0;
    static int errors = 0;

    static ArrayList<Book> catalog = new ArrayList<>();

    public static void main(String[] args) {

        String catalogFileName = "";
        String operation = "";

        try {

            // Check arguments
            if (args.length < 2)
                throw new InsufficientArgumentsException(
                        "Not enough arguments");

            catalogFileName = args[0];
            operation = args[1];

            // Validate file name
            if (!catalogFileName.endsWith(".txt"))
                throw new InvalidFileNameException(
                        "File must end with .txt");

            File catalogFile =
                    new File(catalogFileName);

            // Create directories
            if (catalogFile.getParentFile() != null)
                catalogFile.getParentFile().mkdirs();

            // Create file
            catalogFile.createNewFile();

            // Read catalog
            readCatalog(catalogFile);

            // Decide operation
            if (operation.matches("\\d{13}"))
                searchISBN(operation, catalogFile);

            else if (operation.contains(":"))
                addBook(operation, catalogFile);

            else
                searchTitle(operation);

        }

        catch (BookCatalogException e) {

            errors++;

            if (!catalogFileName.isEmpty())
                logError(catalogFileName,
                        operation,
                        e);

            System.out.println(
                    "Error: " + e.getMessage());

        }

        catch (IOException e) {

            System.out.println(
                    "File error");

        }

        finally {

            System.out.println();
            System.out.println(
                    "Valid records: " +
                            validRecords);

            System.out.println(
                    "Search results: " +
                            results);

            System.out.println(
                    "Books added: " +
                            added);

            System.out.println(
                    "Errors: " +
                            errors);

            System.out.println();
            System.out.println(
                    "Thank you for using the Library Book Tracker.");

        }}
    static void readCatalog(File file)
            throws IOException {

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = br.readLine())
                    != null) {

                try {

                    Book b =
                            parseBook(line);

                    catalog.add(b);

                    validRecords++;

                }

                catch (
                        BookCatalogException e) {

                    errors++;

                    logError(
                            file.getPath(),
                            line,
                            e);

                }  } } }

    static Book parseBook(String line)
            throws BookCatalogException {

        String[] parts =
                line.split(":");

        if (parts.length != 4)

            throw new MalformedBookEntryException(
                    "Wrong number of fields");


        String title =
                parts[0].trim();

        String author =
                parts[1].trim();

        String isbn =
                parts[2].trim();

        String copiesStr =
                parts[3].trim();


        if (title.isEmpty())

            throw new MalformedBookEntryException(
                    "Title empty");


        if (author.isEmpty())

            throw new MalformedBookEntryException(
                    "Author empty");


        if (!isbn.matches("\\d{13}"))

            throw new InvalidISBNException(
                    "ISBN must be 13 digits");


        int copies;

        try {

            copies =
                    Integer.parseInt(
                            copiesStr);

            if (copies <= 0)

                throw new MalformedBookEntryException(
                        "Copies must be positive");

        }

        catch (NumberFormatException e) {

            throw new MalformedBookEntryException(
                    "Copies not integer");

        }
        return new Book(
                title,
                author,
                isbn,
                copies);

    }
    static void searchTitle(String keyword) {

        printHeader();

        for (Book b : catalog) {

            if (b.title
                    .toLowerCase()
                    .contains(
                            keyword.toLowerCase())) {

                printBook(b);

                results++;

            }  } }
    static void searchISBN(
            String isbn,
            File file)

            throws BookCatalogException {

        int count = 0;

        Book found = null;

        for (Book b : catalog) {

            if (b.isbn.equals(isbn)) {

                found = b;

                count++;

            }

        }

        if (count > 1)

            throw new DuplicateISBNException(
                    "Duplicate ISBN found");


        printHeader();

        if (found != null) {

            printBook(found);

            results = 1;

        } }
    static void addBook(
            String record,
            File file)

            throws BookCatalogException,
            IOException {

        Book newBook =
                parseBook(record);

        catalog.add(newBook);


        Collections.sort(
                catalog,
                Comparator.comparing(
                        b -> b.title));

        saveCatalog(file);

        printHeader();
        printBook(newBook);
        added = 1;

    }

    

   static void saveCatalog(File file) throws IOException {
    // This syntax automatically closes the PrintWriter
    try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
        for (Book b : catalog) {
            pw.println(b.title + ":" + b.author + ":" + b.isbn + ":" + b.copies);
        }
    } 
}
    static void printHeader() {

        System.out.printf(
                "%-30s %-20s %-15s %5s%n",
                "Title",
                "Author",
                "ISBN",
                "Copies");

        System.out.println(
                "--------------------------------------------------------------");

    }
    static void printBook(Book b) {

        System.out.printf(
                "%-30s %-20s %-15s %5d%n",
                b.title,
                b.author,
                b.isbn,
                b.copies);

    }

    static void logError(String catalogFile, String text, Exception e) {
    try {
        File file = new File(catalogFile);
        // Use the parent of the catalog file or default to current directory
        File logFile = new File(file.getParent() == null ? "." : file.getParent(), "errors.log");

        // Try-with-resources for the PrintWriter
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            pw.printf("[%s] %s - %s: %s%n", 
                      time, text, e.getClass().getSimpleName(), e.getMessage());
        }
    } catch (IOException ioEx) { // Use a specific name like ioEx instead of generic ex
        System.err.println("Logging failed: " + ioEx.getMessage());
    }
}

}