package DAO;

import Models.Booking;
import Models.BookingEquipment;
import Models.BookingEquipmentViewModel;
import Models.BookingViewModel;
import Models.Field;
import Models.Location;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingDAO {

    private final BookingResourceDAO bookingResourceDAO;
    private final BookingQueryDAO bookingQueryDAO;
    private final BookingWriteDAO bookingWriteDAO;
    private final BookingStateDAO bookingStateDAO;
    private final WeeklyBookingDAO weeklyBookingDAO;
    private final BookingExtraEquipmentDAO bookingExtraEquipmentDAO;

    public BookingDAO() {
        this(new BookingDataAccess());
    }

    BookingDAO(BookingDataAccess bookingDataAccess) {
        this.bookingResourceDAO = new BookingResourceDAO();
        this.bookingStateDAO = new BookingStateDAO(bookingResourceDAO);
        this.bookingWriteDAO = new BookingWriteDAO(bookingDataAccess, bookingStateDAO, bookingResourceDAO);
        this.bookingQueryDAO = new BookingQueryDAO(bookingStateDAO);
        this.bookingExtraEquipmentDAO = new BookingExtraEquipmentDAO(bookingDataAccess, bookingStateDAO);
        this.weeklyBookingDAO = new WeeklyBookingDAO(bookingWriteDAO, bookingStateDAO);
    }

    public boolean insert(Booking booking, List<BookingEquipment> equipmentList) {
        return bookingWriteDAO.insert(booking, equipmentList);
    }

    public String getLastInsertError() {
        return bookingWriteDAO.getLastInsertError();
    }

    public List<BookingViewModel> getByBooker(UUID bookerId) {
        return bookingQueryDAO.getByBooker(bookerId);
    }

    public List<BookingViewModel> getByBookerFiltered(UUID bookerId, String bookingDateStr, String startTimeStr, String status) {
        return bookingQueryDAO.getByBookerFiltered(bookerId, bookingDateStr, startTimeStr, status);
    }

    public List<BookingViewModel> getByBookerFiltered(UUID bookerId,
                                                      String bookingDateStr,
                                                      String startTimeStr,
                                                      String playStatus,
                                                      String paymentStatus,
                                                      String extraPaymentStatus,
                                                      String status) {
        return bookingQueryDAO.getByBookerFiltered(bookerId, bookingDateStr, startTimeStr, playStatus, paymentStatus, extraPaymentStatus, status);
    }

    public List<BookingViewModel> getByWeeklyGroupId(UUID weeklyGroupId) {
        return weeklyBookingDAO.getByWeeklyGroupId(weeklyGroupId);
    }

    public List<BookingViewModel> getCustomerCalendarBookings(UUID bookerId,
                                                              LocalDate fromDate,
                                                              LocalDate toDate,
                                                              LocalDate selectedDate,
                                                              UUID locationId,
                                                              UUID fieldId) {
        return bookingQueryDAO.getCustomerCalendarBookings(bookerId, fromDate, toDate, selectedDate, locationId, fieldId);
    }

    public List<Field> getCustomerCalendarFields(UUID bookerId) {
        return bookingQueryDAO.getCustomerCalendarFields(bookerId);
    }

    public List<Field> getCustomerCalendarFields(UUID bookerId, UUID locationId) {
        return bookingQueryDAO.getCustomerCalendarFields(bookerId, locationId);
    }

    public List<Location> getCustomerCalendarLocations(UUID bookerId) {
        return bookingQueryDAO.getCustomerCalendarLocations(bookerId);
    }

    public BookingViewModel getById(UUID bookingId) {
        return bookingQueryDAO.getById(bookingId);
    }

    public BookingViewModel getByScheduleId(UUID scheduleId) {
        return bookingQueryDAO.getByScheduleId(scheduleId);
    }

    public BookingViewModel getByScheduleIdForCalendar(UUID scheduleId) {
        return bookingQueryDAO.getByScheduleIdForCalendar(scheduleId);
    }

    public List<BookingEquipmentViewModel> getBookingEquipments(UUID bookingId) {
        return bookingQueryDAO.getBookingEquipments(bookingId);
    }

    public boolean cancelBooking(UUID bookingId) {
        return bookingWriteDAO.cancelBooking(bookingId);
    }

    public boolean cancelBookingForPayment(UUID bookingId) {
        return bookingWriteDAO.cancelBookingForPayment(bookingId);
    }

    public boolean markBookingPaid(UUID bookingId) {
        return bookingStateDAO.markBookingPaid(bookingId);
    }

    public boolean markBookingDeposited(UUID bookingId) {
        return bookingStateDAO.markBookingDeposited(bookingId);
    }

    public boolean markWeeklyGroupPaid(UUID weeklyGroupId) {
        return weeklyBookingDAO.markWeeklyGroupPaid(weeklyGroupId);
    }

    public boolean markBookingPendingExtra(UUID bookingId) {
        return bookingExtraEquipmentDAO.markBookingPendingExtra(bookingId);
    }

    public boolean settlePendingExtraStatus(UUID bookingId) {
        return bookingExtraEquipmentDAO.settlePendingExtraStatus(bookingId);
    }

    public boolean cancelWeeklyGroupForPayment(UUID weeklyGroupId) {
        return weeklyBookingDAO.cancelWeeklyGroupForPayment(weeklyGroupId);
    }

    public boolean updateStatus(UUID bookingId, String newStatus) {
        return bookingStateDAO.updateStatus(bookingId, newStatus);
    }

    public boolean addEquipmentToBooking(UUID bookingId, List<BookingEquipment> equipmentList, BigDecimal additionalAmount) {
        return bookingExtraEquipmentDAO.addEquipmentToBooking(bookingId, equipmentList, additionalAmount);
    }

    public boolean finalizeSupplementaryEquipment(UUID bookingId, List<BookingEquipment> equipmentList, BigDecimal additionalAmount) {
        return bookingExtraEquipmentDAO.finalizeSupplementaryEquipment(bookingId, equipmentList, additionalAmount);
    }

    public BigDecimal getSupplementaryAmountByBookingId(UUID bookingId) {
        return bookingExtraEquipmentDAO.getSupplementaryAmountByBookingId(bookingId);
    }

    public List<BookingViewModel> getByLocation(UUID locationId) {
        return bookingQueryDAO.getByLocation(locationId);
    }

    public List<BookingViewModel> getByLocationFiltered(UUID locationId, String bookingDateStr, String status, String customerKeyword) {
        return bookingQueryDAO.getByLocationFiltered(locationId, bookingDateStr, status, customerKeyword);
    }

    public List<BookingViewModel> getByLocationFilteredByState(UUID locationId,
                                                               String bookingDateStr,
                                                               String playStatus,
                                                               String paymentStatus,
                                                               String extraPaymentStatus,
                                                               String customerKeyword) {
        return bookingQueryDAO.getByLocationFilteredByState(locationId, bookingDateStr, playStatus, paymentStatus, extraPaymentStatus, customerKeyword);
    }

    public Booking getBookingById(UUID bookingId) {
        return bookingWriteDAO.getBookingById(bookingId);
    }

    public boolean resetPaymentDeadline(UUID bookingId, LocalDateTime newDeadline) {
        return bookingWriteDAO.resetPaymentDeadline(bookingId, newDeadline);
    }

    public BigDecimal getOutstandingAmount(UUID bookingId) {
        return bookingQueryDAO.getOutstandingAmount(bookingId);
    }

    public boolean updateSplitStates(UUID bookingId, String playStatus, String paymentStatus, String extraPaymentStatus) {
        return bookingStateDAO.updateSplitStates(bookingId, playStatus, paymentStatus, extraPaymentStatus);
    }

    public boolean isBookedByStaff(UUID bookingId) {
        return bookingStateDAO.isBookedByStaff(bookingId);
    }

    public List<Booking> insertWeekly(UUID bookerId,
                                      UUID fieldId,
                                      List<UUID> scheduleIds,
                                      List<BookingEquipment> equipmentList,
                                      UUID voucherId,
                                      BigDecimal discountPercent,
                                      LocalDateTime paymentDeadline,
                                      UUID weeklyGroupId,
                                      String bookingPhone) throws Exception {
        return weeklyBookingDAO.insertWeekly(
                bookerId,
                fieldId,
                scheduleIds,
                equipmentList,
                voucherId,
                discountPercent,
                paymentDeadline,
                weeklyGroupId,
                bookingPhone
        );
    }
}
