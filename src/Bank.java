import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Bank {

    private final int PORT = 2222;
    private final String IP = "127.0.0.1";
    private static ArrayList<Socket> connectedClients = new ArrayList<>();
    private ServerSocket serverSocket;
    private static ArrayList<BankAccount> allAccounts = new ArrayList<>();
    private static ArrayList<Receipt> allReceipts = new ArrayList<>();

    public static void main(String[] args) {
        try {
            loadFromDatabase();
            System.out.println("Database loaded successfully");
            new Bank().waitForClient();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static BankAccount getAccountById(int sourceId) {
        for (BankAccount account : allAccounts) {
            if (account.getId() == sourceId)
                return account;
        }
        return null;
    }

    public static BankAccount getAccountByToken (String token) {
        for (BankAccount account : allAccounts) {
            if (token.equals(account.getToken()))
                return account;
        }
        return null;
    }

    public static Receipt getReceiptById(int receiptId) {
        for (Receipt receipt : allReceipts) {
            if (receipt.getId() == receiptId)
                return receipt;
        }
        return null;
    }

    public static ArrayList<Receipt> getAllAccountReceipts (int accountId) {
        ArrayList<Receipt> result = new ArrayList<>();
        for (Receipt receipt : allReceipts) {
            if (receipt.getSourceId() == accountId || receipt.getDestId() == accountId)
                result.add(receipt);
        }
        return result;
    }

    private void waitForClient () throws IOException {
        serverSocket = new ServerSocket(PORT);
        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("new client connected");
            connectedClients.add(client);
            new ClientHandler(client).start();
        }
    }

    public static void addAccount (BankAccount account) throws Exception {
        allAccounts.add(account);
        try {
            saveToDatabase();
        } catch (IOException e) {
            throw new Exception("database error");
        }
    }

    public static void removeClient (Socket client) {
        connectedClients.remove(client);
    }

    public static void addReceipt(Receipt receipt) throws Exception {
        allReceipts.add(receipt);
        try {
            saveToDatabase();
        } catch (IOException e) {
            throw new Exception("database error");
        }
    }

    public static void saveToDatabase() throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("Database.db"));
        outputStream.writeObject(allAccounts);
        outputStream.writeObject(allReceipts);
        outputStream.close();
    }

    private static void loadFromDatabase() throws IOException, ClassNotFoundException {
        File file = new File("Database.db");
        if (!file.exists())
            file.createNewFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(fileInputStream);
        }catch (EOFException e) {
            return;
        }
        allAccounts = (ArrayList<BankAccount>) inputStream.readObject();
        allReceipts = (ArrayList<Receipt>) inputStream.readObject();
        inputStream.close();
    }

    public static int getNumberOfAccounts () {
        return allAccounts.size();
    }

    public static int getNumberOfReceipts() {
        return allReceipts.size();
    }

    public static boolean isUsernameDuplicated (String username) {
        for (BankAccount account : allAccounts) {
            if (account.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public static boolean checkAuthentication (String username, String password) {
        for (BankAccount account : allAccounts) {
            if (account.getUsername().equals(username))
                if (account.getPassword().equals(password))
                    return true;
        }
        return false;
    }

    public static boolean checkAccountId (String id) {
        if (id.matches("\\d+")) {
            int numId = Integer.parseInt(id);
            return (numId - 1000 <= allAccounts.size()) && (numId - 1000) >= 0;
        }
        return false;
    }

    public static BankAccount getAccountByUsername (String username) {
        for (BankAccount account : allAccounts) {
            if (account.getUsername().equals(username))
                return account;
        }
        return null;
    }
}