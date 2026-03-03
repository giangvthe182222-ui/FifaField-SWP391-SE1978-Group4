package Service;

import DAO.BookingDAO;
import Models.BookingViewModel;

import java.util.List;
import java.util.UUID;

public class BookingService {

    private final BookingDAO bookingDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
    }

    public List<BookingViewModel> getCustomerBookingHistory(UUID userId, String date, String time, String status) {
        return bookingDAO.getByBookerFiltered(userId, date, time, status);
    }

    public List<BookingViewModel> getLocationBookingHistory(UUID locationId, String date, String status, String customerKeyword) {
        return bookingDAO.getByLocationFiltered(locationId, date, status, customerKeyword);
    }

    public boolean updateBookingStatus(UUID bookingId, String status) {
        return bookingDAO.updateStatus(bookingId, status);
    }
}
