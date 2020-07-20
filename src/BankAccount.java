import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class BankAccount implements Serializable {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private int id;
    private long balance = 0;
    private String token = null;
    private LocalDateTime tokenTime = null;

    public BankAccount(String firstName, String lastName, String username, String password, int number) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.id = 1000 + number;
    }

    //getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }

    //modifiers
    public void setToken(String token) {
        tokenTime = LocalDateTime.now();
        this.token = token;
    }

    public boolean isTokenExpired() {
        System.out.println("is token expired?:");
        System.out.println(tokenTime.plusHours(1));
        return tokenTime.plusHours(1).isBefore(LocalDateTime.now());
    }

    public long getBalance() {
        return this.balance;
    }

    public void deposit(long money) {
        this.balance += money;
    }

    public void withdraw(long money) {
        this.balance -= money;
    }

    public ArrayList<Receipt> getTransactions(String type) {
        ArrayList<Receipt> allReceipts = Bank.getAllAccountReceipts(this.id);
        if (type.equals("*"))
            return allReceipts;
        ArrayList<Receipt> result = new ArrayList<>();
        for (Receipt receipt : allReceipts) {
            switch (type) {
                case "+":
                    if (receipt.getDestId() == id)
                        result.add(receipt);
                    break;
                case "-":
                    if (receipt.getSourceId() == id)
                        result.add(receipt);
                    break;
            }
        }
        return result;
    }
}