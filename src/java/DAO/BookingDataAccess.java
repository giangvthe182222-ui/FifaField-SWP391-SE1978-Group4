package DAO;

public class BookingDataAccess {

    private String lastInsertError;

    public BookingDataAccess() {
        this.lastInsertError = null;
    }

    public String getLastInsertError() {
        return lastInsertError;
    }

    public void setLastInsertError(String lastInsertError) {
        this.lastInsertError = lastInsertError;
    }

    public void clearLastInsertError() {
        this.lastInsertError = null;
    }
}
