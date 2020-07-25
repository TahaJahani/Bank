import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

public class Receipt implements Serializable {
    private String type;
    private long money;
    private int sourceId;
    private int destId;
    private int receiptId;
    private String description;
    private boolean paid;

    public Receipt(String type, long money, int sourceId, int destId, String description) {
        this.type = type;
        this.money = money;
        this.sourceId = sourceId;
        this.destId = destId;
        this.description = description;
        this.paid = false;
        this.receiptId = Bank.getNumberOfReceipts() + 1000;
    }

    public static boolean checkType (String type) throws Exception {
        if ( type.equals("deposit") || type.equals("withdraw") || type.equals("move") )
            return true;
        throw new Exception("error: invalid receipt type");
    }

    public int getId() {
        return receiptId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getDestId() {
        return destId;
    }

    public boolean isPaid() {
        return paid;
    }

    public BankAccount getSourceAccount () {
        return Bank.getAccountById(sourceId);
    }

    public long getMoney() {
        return money;
    }

    public String getType() {
        return this.type;
    }

    public BankAccount getDestAccount() {
        return Bank.getAccountById(destId);
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        Gson gsonBuilder = new GsonBuilder().create();
        String jsonObject = gsonBuilder.toJson(this);
        return jsonObject.toString();
    }
}