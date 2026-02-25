import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OperationAnalyzerRunnable implements Runnable {
    private List<Book> catalog;
    private String operation;
    private File file;

    public OperationAnalyzerRunnable(List<Book> catalog, String operation, File file) {
        this.catalog = catalog;
        this.operation = operation;
        this.file = file;
    }

   @Override
public void run() {
    try {
        if (operation.matches("\\d{13}")) {
            searchISBN(operation);
        } else if (operation.contains(":")) {
            addBook(operation);
        } else {
            searchTitle(operation);
        }
    } catch (BookCatalogException e) {
        LibraryBookTracker.errors++;
        System.out.println("Operation Error: " + e.getMessage());
    } 
}

    private void searchISBN(String isbn) throws BookCatalogException {
    int count = 0;
    Book found = null;

for (Book b : catalog) {
    // trim() removes hidden spaces that cause "Search results: 0"
    if (b.isbn.trim().equals(isbn.trim())) { 
        found = b;
        count++;
    }
}

    // Check for duplicates as required
    if (count > 1) {
        throw new DuplicateISBNException("Duplicate ISBN found");
    }

    if (found != null) {
        LibraryBookTracker.printHeader(); 
        LibraryBookTracker.printBook(found); 
        LibraryBookTracker.results = 1; 
    } else {
        System.out.println("No book found with ISBN: " + isbn);
    }
}
    private void searchTitle(String keyword) {
        LibraryBookTracker.printHeader();
        for (Book b : catalog) {
            if (b.title.toLowerCase().contains(keyword.toLowerCase())) {
                LibraryBookTracker.printBook(b);
                LibraryBookTracker.results++;
            }
        }
    }

    private void addBook(String record) {
    try {
        // 1. Parse and add to the shared list
        Book newBook = FileReaderTask.parseBook(record); 
        catalog.add(newBook);

        // 2. Sort the catalog by title
        Collections.sort(catalog, Comparator.comparing(b -> b.title));

        // 3. WRITE TO FILE (This was the missing step!)
        LibraryBookTracker.saveCatalog(file); 

        // 4. Feedback for the user
        LibraryBookTracker.printHeader();
        LibraryBookTracker.printBook(newBook);
        LibraryBookTracker.added = 1; 
        
    } catch (Exception e) {
        LibraryBookTracker.errors++;
        System.out.println("Error adding book: " + e.getMessage());
    }
}
}