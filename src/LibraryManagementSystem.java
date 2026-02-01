
import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

// ---------- Abstract base class demonstrating inheritance & polymorphism ----------
abstract class LibraryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final String id; // unique identifier
    protected String title;
    protected boolean available = true;

    public LibraryItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    // polymorphic string describing the item
    public abstract String getDetails();
}

// ---------- Concrete item type ----------
class Book extends LibraryItem {
    private static final long serialVersionUID = 1L;
    private String author;
    private int year;
    private String genre;

    public Book(String id, String title, String author, int year, String genre) {
        super(id, title);
        this.author = author;
        this.year = year;
        this.genre = genre;
    }

    public String getAuthor() { return author; }
    public int getYear() { return year; }
    public String getGenre() { return genre; }

    @Override
    public String getDetails() {
        return String.format("Book[id=%s,title=%s,author=%s,year=%d,genre=%s,available=%s]", id, title, author, year, genre, available);
    }
}

// ---------- Member class demonstrating encapsulation ----------
class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String memberId;
    private String name;
    private String email;

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return String.format("Member[id=%s,name=%s,email=%s]", memberId, name, email);
    }
}

// ---------- Loan class: association between Member and LibraryItem ----------
class Loan implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String loanId;
    private final String itemId;
    private final String memberId;
    private final LocalDate checkoutDate;
    private final LocalDate dueDate;
    private LocalDate returnDate = null;

    public Loan(String loanId, String itemId, String memberId, LocalDate checkoutDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.itemId = itemId;
        this.memberId = memberId;
        this.checkoutDate = checkoutDate;
        this.dueDate = dueDate;
    }

    public String getLoanId() { return loanId; }
    public String getItemId() { return itemId; }
    public String getMemberId() { return memberId; }
    public LocalDate getCheckoutDate() { return checkoutDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }

    public void markReturned(LocalDate returnedOn) { this.returnDate = returnedOn; }

    public boolean isOverdue() {
        LocalDate check = (returnDate == null) ? LocalDate.now() : returnDate;
        return check.isAfter(dueDate);
    }

    public long daysOverdue() {
        LocalDate check = (returnDate == null) ? LocalDate.now() : returnDate;
        if (!check.isAfter(dueDate)) return 0;
        return Duration.between(dueDate.atStartOfDay(), check.atStartOfDay()).toDays();
    }

    @Override
    public String toString() {
        return String.format("Loan[id=%s,item=%s,member=%s,checkout=%s,due=%s,returned=%s]", loanId, itemId, memberId, checkoutDate, dueDate, (returnDate==null?"not yet":returnDate));
    }
}

// ---------- The core Library class: manages items, members, and loans ----------
class Library implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, LibraryItem> items = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final Map<String, Loan> loans = new HashMap<>();

    // basic configuration
    private final int loanDays = 14;
    private final double finePerDay = 0.50; // currency units per day

    // items
    public void addItem(LibraryItem item) {
        items.put(item.getId(), item);
    }

    public LibraryItem removeItem(String id) {
        return items.remove(id);
    }

    public LibraryItem findItemById(String id) {
        return items.get(id);
    }

    public List<LibraryItem> searchByTitle(String term) {
        List<LibraryItem> results = new ArrayList<>();
        for (LibraryItem it : items.values()) {
            if (it.getTitle().toLowerCase().contains(term.toLowerCase())) results.add(it);
        }
        return results;
    }

    public Collection<LibraryItem> allItems() { return items.values(); }

    // members
    public void addMember(Member m) { members.put(m.getMemberId(), m); }
    public Member findMember(String memberId) { return members.get(memberId); }
    public Collection<Member> allMembers() { return members.values(); }

    // loans
    public Loan checkoutItem(String itemId, String memberId) throws IllegalStateException {
        LibraryItem it = items.get(itemId);
        if (it == null) throw new IllegalStateException("Item not found");
        if (!it.isAvailable()) throw new IllegalStateException("Item is already checked out");
        if (!members.containsKey(memberId)) throw new IllegalStateException("Member not found");

        String loanId = UUID.randomUUID().toString();
        LocalDate checkout = LocalDate.now();
        LocalDate due = checkout.plusDays(loanDays);
        Loan loan = new Loan(loanId, itemId, memberId, checkout, due);
        loans.put(loanId, loan);
        it.setAvailable(false);
        return loan;
    }

    public Loan returnItemByLoanId(String loanId) throws IllegalStateException {
        Loan loan = loans.get(loanId);
        if (loan == null) throw new IllegalStateException("Loan not found");
        if (loan.getReturnDate() != null) throw new IllegalStateException("Item already returned");
        loan.markReturned(LocalDate.now());
        LibraryItem it = items.get(loan.getItemId());
        if (it != null) it.setAvailable(true);
        return loan;
    }

    public Loan findLoanByItemId(String itemId) {
        for (Loan l : loans.values()) if (l.getItemId().equals(itemId) && l.getReturnDate()==null) return l;
        return null;
    }

    public Collection<Loan> allLoans() { return loans.values(); }

    public double calculateFine(Loan loan) {
        long days = loan.daysOverdue();
        return days * finePerDay;
    }

    // persistence
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static Library loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Library) ois.readObject();
        }
    }
}

// ---------- Simple command-line interface demonstrating usage ----------
public class LibraryManagementSystem {
    private static final String SAVE_FILE = "library.dat";
    private final Library library;
    private final Scanner in = new Scanner(System.in);

    public LibraryManagementSystem(Library library) {
        this.library = library;
    }

    public void run() {
        seedSampleDataIfEmpty();
        while (true) {
            printMenu();
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1": cmdListItems(); break;
                    case "2": cmdSearch(); break;
                    case "3": cmdAddBook(); break;
                    case "4": cmdRegisterMember(); break;
                    case "5": cmdCheckout(); break;
                    case "6": cmdReturn(); break;
                    case "7": cmdListMembers(); break;
                    case "8": cmdListLoans(); break;
                    case "9": cmdSave(); break;
                    case "0": System.out.println("Goodbye"); cmdSave(); return;
                    default: System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=== Library Management ===");
        System.out.println("1) List all items");
        System.out.println("2) Search by title");
        System.out.println("3) Add a book");
        System.out.println("4) Register member");
        System.out.println("5) Checkout item");
        System.out.println("6) Return item");
        System.out.println("7) List members");
        System.out.println("8) List loans");
        System.out.println("9) Save library to disk");
        System.out.println("0) Exit (saves automatically)");
        System.out.print("Choose: ");
    }

    private void cmdListItems() {
        System.out.println("Items in library:");
        for (LibraryItem it : library.allItems()) System.out.println(" - " + it.getDetails());
    }

    private void cmdListMembers() {
        System.out.println("Members:");
        for (Member m : library.allMembers()) System.out.println(" - " + m);
    }

    private void cmdListLoans() {
        System.out.println("Loans:");
        for (Loan l : library.allLoans()) {
            System.out.println(" - " + l + " | fine=" + String.format("%.2f", library.calculateFine(l)));
        }
    }

    private void cmdSearch() {
        System.out.print("Search term: ");
        String term = in.nextLine();
        List<LibraryItem> results = library.searchByTitle(term);
        if (results.isEmpty()) System.out.println("No results");
        else for (LibraryItem it : results) System.out.println(" - " + it.getDetails());
    }

    private void cmdAddBook() {
        System.out.print("Book ID: "); String id = in.nextLine().trim();
        System.out.print("Title: "); String title = in.nextLine().trim();
        System.out.print("Author: "); String author = in.nextLine().trim();
        System.out.print("Year: "); int year = Integer.parseInt(in.nextLine().trim());
        System.out.print("Genre: "); String genre = in.nextLine().trim();
        Book b = new Book(id, title, author, year, genre);
        library.addItem(b);
        System.out.println("Book added.");
    }

    private void cmdRegisterMember() {
        System.out.print("Member ID: "); String id = in.nextLine().trim();
        System.out.print("Name: "); String name = in.nextLine().trim();
        System.out.print("Email: "); String email = in.nextLine().trim();
        Member m = new Member(id, name, email);
        library.addMember(m);
        System.out.println("Member registered.");
    }

    private void cmdCheckout() {
        System.out.print("Item ID to checkout: "); String itemId = in.nextLine().trim();
        System.out.print("Member ID: "); String memberId = in.nextLine().trim();
        Loan loan = library.checkoutItem(itemId, memberId);
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
        System.out.println("Checked out. Loan id=" + loan.getLoanId() + " due=" + loan.getDueDate().format(f));
    }

    private void cmdReturn() {
        System.out.print("Loan ID (or press Enter to return by item id): "); String loanId = in.nextLine().trim();
        if (!loanId.isEmpty()) {
            Loan loan = library.returnItemByLoanId(loanId);
            double fine = library.calculateFine(loan);
            System.out.println("Returned. Fine: " + String.format("%.2f", fine));
            return;
        }
        System.out.print("Item ID: "); String itemId = in.nextLine().trim();
        Loan loan = library.findLoanByItemId(itemId);
        if (loan==null) { System.out.println("No active loan for that item"); return; }
        library.returnItemByLoanId(loan.getLoanId());
        double fine = library.calculateFine(loan);
        System.out.println("Returned. Fine: " + String.format("%.2f", fine));
    }

    private void cmdSave() {
        try { library.saveToFile(SAVE_FILE); System.out.println("Library saved."); }
        catch (IOException e) { System.out.println("Save failed: " + e.getMessage()); }
    }

    private void seedSampleDataIfEmpty() {
        if (library.allItems().isEmpty() && library.allMembers().isEmpty()) {
            library.addItem(new Book("B001","The Java Handbook","Patrick Naughton",1997,"Programming"));
            library.addItem(new Book("B002","Clean Code","Robert C. Martin",2008,"Programming"));
            library.addItem(new Book("B003","To Kill a Mockingbird","Harper Lee",1960,"Fiction"));
            library.addMember(new Member("M001","Alice","alice@example.com"));
            library.addMember(new Member("M002","Bob","bob@example.com"));
            System.out.println("Sample data seeded.");
        }
    }

    // ---------- main ----------
    public static void main(String[] args) {
        Library lib = null;
        try {
            lib = Library.loadFromFile(SAVE_FILE);
            System.out.println("Loaded library from disk.");
        } catch (Exception e) {
            lib = new Library();
            System.out.println("Starting with a new library (no saved data found).");
        }
        LibraryManagementSystem app = new LibraryManagementSystem(lib);
        app.run();
    }
}
