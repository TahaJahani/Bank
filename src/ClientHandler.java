import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class ClientHandler extends Thread {
    private Socket client;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    public ClientHandler (Socket client) {
        this.client = client;
        try {
            inputStream = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            outputStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
        } catch (IOException e) {
            System.err.println("Couldn't connect to client");
        }
    }

    @Override
    public void run() {
        String msg = "message";
        while (!msg.equals("exit")) {
            try {
                msg = inputStream.readUTF();
                System.out.println(msg);
                checkReceivedMessage(msg);
            }catch (EOFException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
                sendReply(e.getMessage());
            }
        }
        try {
            Bank.removeClient(client);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendReply (String message) {
        try {
            outputStream.writeUTF(message);
            System.out.println("Reply: " + message);
            outputStream.flush();
            System.out.println("Sent\n");
        } catch (IOException ignored) {
        }
    }

    private void checkReceivedMessage (String message) throws Exception {
        System.out.println("Received: " + message + "\n");
        String[] parameters = message.split("\\s");
        int size = parameters.length;
        if (message.startsWith("create_account") && size == 6) {
            createAccount(parameters);
        }else if (message.startsWith("get_token") && size == 3)
            getToken(parameters[1], parameters[2]);
        else if (message.startsWith("create_receipt") && size == 7)
            createReceipt(parameters);
        else if (message.startsWith("get_transactions") && size == 3)
            getTransactions(parameters[1], parameters[2]);
        else if (message.startsWith("pay") && size == 2)
            pay(parameters[1]);
        else if (message.startsWith("get_balance") && size == 2)
            getBalance(parameters[1]);
        else if (message.equals("exit")) {
            return;
        } else
            throw new Exception("error: invalid input");
    }

    private void getBalance(String token) throws Exception {
        checkToken(token);
        sendReply(String.valueOf(Objects.requireNonNull(Bank.getAccountByToken(token)).getBalance()));
    }

    private void pay(String receiptId) throws Exception {
        Receipt receiptToPay = Bank.getReceiptById(Integer.parseInt(receiptId));
        checkReceiptBeforePay(receiptToPay);
        switch (receiptToPay.getType()) {
            case "deposit":
                receiptToPay.getDestAccount().deposit(receiptToPay.getMoney());
                break;
            case "withdraw":
                receiptToPay.getSourceAccount().withdraw(receiptToPay.getMoney());
                break;
            case "move":
                receiptToPay.getSourceAccount().withdraw(receiptToPay.getMoney());
                receiptToPay.getDestAccount().deposit(receiptToPay.getMoney());
                break;
        }
        receiptToPay.setPaid(true);
        sendReply("done successfully");
    }

    private void checkReceiptBeforePay(Receipt receiptToPay) throws Exception {
        if (receiptToPay == null)
            throw new Exception("error: invalid receipt id");
        if (receiptToPay.isPaid())
            throw new Exception("error: receipt is paid before");
        if (!receiptToPay.getType().equals("deposit")) {
            if (receiptToPay.getSourceAccount().getBalance() < receiptToPay.getMoney())
                throw new Exception("error: source account does not have enough money");
        }
    }

    private void getTransactions(String token, String type) throws Exception {
        checkToken(token);
        StringBuilder string = new StringBuilder();
        for (Receipt transaction : Bank.getAccountByToken(token).getTransactions(type)) {
            string.append(transaction).append("*");
        }
        sendReply(string.toString());
    }

    private void createReceipt(String[] split) throws Exception {
        checkReceiptInputs(split);
        int sourceId = Integer.parseInt(split[4]);
        int destId = Integer.parseInt(split[5]);
        Receipt receipt = new Receipt(split[2], Long.parseLong(split[3]), sourceId,
                destId, split[6]);
        Bank.addReceipt(receipt);
        sendReply(String.valueOf(receipt.getId()));
    }

    private void checkReceiptInputs(String[] split) throws Exception {
        BankAccount loggedOnAccount = Bank.getAccountByToken(split[1]);
        if (!checkToken(split[1]) || !Receipt.checkType(split[2]) || !checkNumericalInput(split[3]))
            return; //exception is thrown
        if (!split[2].equals("deposit")) {
            if (!split[4].equals(String.valueOf(loggedOnAccount.getId())))
                throw new Exception("error: token is invalid");
            if (!Bank.checkAccountId(split[4]))
                throw new Exception("error: source account id is invalid");
        }else{
            if (!split[4].equals("-1"))
                throw new Exception("error: invalid account id");
        }
        if (!split[2].equals("withdraw")) {
            if (!Bank.checkAccountId(split[5]))
                throw new Exception("error: dest account id is invalid");
        }
        if (split[4].equals(split[5]))
            throw new Exception("error: equal source and dest account");
        if (!split[6].matches("\\w+"))
            throw new Exception("error: your input contains invalid characters");
    }

    private void getToken(String username, String password) throws Exception {
        if (!Bank.checkAuthentication(username, password))
            throw new Exception("error: invalid username or password");
        BankAccount loggedOnAccount = Bank.getAccountByUsername(username);
        String token = generateToken();
        assert loggedOnAccount != null;
        loggedOnAccount.setToken(token);
        sendReply(token);
    }

    private boolean checkToken (String token) throws Exception {
        BankAccount account = Bank.getAccountByToken(token);
        if (account == null)
            throw new Exception("error: invalid token");
        if (account.isTokenExpired())
            throw new Exception("error: token expired");
        return true;
    }

    private String generateToken() {
        char[] token = new char[20];
        int min = 48;
        int max = 122;
        for (int i = 0 ; i < 20 ; i++) {
            int randomCharacter = (int) (Math.random() * (max - min + 1) + min);
            if ( (randomCharacter >= 58 && randomCharacter <= 64) || (randomCharacter <= 96 && randomCharacter >= 91) ) {
                i--;
            }else
                token[i] = (char) randomCharacter;
        }
        return String.valueOf(token);
    }

    private void createAccount(String[] split) throws Exception {
        if (!split[4].equals(split[5]))
            throw new Exception("error: Passwords do not match");
        if (Bank.isUsernameDuplicated(split[3]))
            throw new Exception("error: username is not available");
        BankAccount account = new BankAccount(split[1], split[2], split[3], split[4], Bank.getNumberOfAccounts());
        Bank.addAccount(account);
        sendReply(String.valueOf(account.getId()));
    }

    private boolean checkNumericalInput (String input) throws Exception {
        if ( input.matches("\\d+") && Integer.parseInt(input) != 0 )
            return true;
        throw new Exception("error: invalid money");
    }
}