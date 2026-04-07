package DAO;

import Models.Booking;
import Models.BookingViewModel;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;

public class BookingStateDAO {

    static final String STATUS_PENDING = "pending";
    static final String STATUS_PAID = "paid";
    static final String STATUS_CANCELLED = "cancelled";
    static final String STATUS_PENDING_REFUND = "pending refund";
    static final String STATUS_REFUNDED = "refunded";
    static final String STATUS_CHECKED_IN = "checked in";
    static final String STATUS_CHECKED_OUT = "checked out";
    static final String STATUS_PENDING_EXTRA = "pending extra";
    static final String STATUS_FINISHED = "finished";
    static final String STATUS_COMPLETED = "completed";
    static final String STATUS_DEPOSITED = "deposited";

    static final String PLAY_STATUS_BOOKED = "booked";
    static final String PLAY_STATUS_CANCELLED = "cancelled";
    static final String PAYMENT_STATUS_FAILED = "failed";
    static final String EXTRA_PAYMENT_STATUS_NONE = "none";
    static final String EXTRA_PAYMENT_STATUS_PAID = "paid extra";
    static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30");

    static final Set<String> SUPPORTED_PLAY_STATUSES = new HashSet<>(Arrays.asList(
            PLAY_STATUS_BOOKED,
            STATUS_CHECKED_IN,
            STATUS_CHECKED_OUT,
            STATUS_COMPLETED,
            PLAY_STATUS_CANCELLED
    ));

    static final Set<String> SUPPORTED_PAYMENT_STATUSES = new HashSet<>(Arrays.asList(
            STATUS_PENDING,
            STATUS_DEPOSITED,
            STATUS_PAID,
            STATUS_PENDING_REFUND,
            STATUS_REFUNDED,
            PAYMENT_STATUS_FAILED
    ));

    static final Set<String> SUPPORTED_EXTRA_PAYMENT_STATUSES = new HashSet<>(Arrays.asList(
            EXTRA_PAYMENT_STATUS_NONE,
            STATUS_PENDING_EXTRA,
            EXTRA_PAYMENT_STATUS_PAID
    ));

    private static final Set<String> SUPPORTED_STATUSES = new HashSet<>(Arrays.asList(
            STATUS_PENDING,
            STATUS_PAID,
            STATUS_CANCELLED,
            STATUS_PENDING_REFUND,
            STATUS_REFUNDED,
            STATUS_CHECKED_IN,
            STATUS_CHECKED_OUT,
            STATUS_PENDING_EXTRA,
            STATUS_FINISHED,
            STATUS_COMPLETED,
            STATUS_DEPOSITED
    ));

    private final BookingResourceDAO bookingResourceDAO;

    public BookingStateDAO() {
        this(new BookingResourceDAO());
    }

    public BookingStateDAO(BookingResourceDAO bookingResourceDAO) {
        this.bookingResourceDAO = bookingResourceDAO;
    }

    public boolean updateStatus(UUID bookingId, String newStatus) {
        if (newStatus == null) {
            return false;
        }

        synchronizeBookingStates();
        newStatus = normalizeStatus(newStatus);
        if (STATUS_FINISHED.equals(newStatus)) {
            newStatus = STATUS_CHECKED_OUT;
        }

        if (!SUPPORTED_STATUSES.contains(newStatus)) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (newStatus.equals(snapshot.status)) {
                conn.rollback();
                return true;
            }

            boolean success = false;

            switch (newStatus) {
                case STATUS_PAID:
                    if (STATUS_PENDING.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_PENDING);
                        if (!success && STATUS_DEPOSITED.equals(snapshot.status)) {
                            success = updateBookingStatus(conn, bookingId, STATUS_PAID, STATUS_DEPOSITED);
                        }
                    }
                    break;

                case STATUS_DEPOSITED:
                    if (STATUS_PENDING.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_DEPOSITED, STATUS_PENDING);
                    }
                    break;

                case STATUS_CHECKED_IN:
                    if (STATUS_PAID.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PAID);
                        if (!success && STATUS_DEPOSITED.equals(snapshot.status)) {
                            success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_DEPOSITED);
                        }
                    } else if (STATUS_PENDING_EXTRA.equals(snapshot.status) && isPaymentSuccessful(conn, bookingId)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_IN, STATUS_PENDING_EXTRA);
                    }
                    break;

                case STATUS_CHECKED_OUT:
                    if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_CHECKED_OUT, STATUS_CHECKED_IN);
                    }
                    break;

                case STATUS_PENDING_EXTRA:
                    if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_PENDING_EXTRA, STATUS_CHECKED_IN);
                    }
                    break;

                case STATUS_PENDING_REFUND:
                    if (STATUS_PAID.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_PENDING_REFUND, snapshot.status);
                        if (success) {
                            updatePaymentStatusByBooking(conn, bookingId, "REFUND_PENDING", false);
                        }
                    }
                    break;

                case STATUS_REFUNDED:
                    if (STATUS_PENDING_REFUND.equals(snapshot.status)) {
                        success = updateBookingStatus(conn, bookingId, STATUS_REFUNDED, snapshot.status);
                        if (success) {
                            updatePaymentStatusByBooking(conn, bookingId, "REFUNDED", true);
                            bookingResourceDAO.releaseBookingResources(conn, bookingId, snapshot.scheduleId);
                        }
                    }
                    break;

                case STATUS_COMPLETED:
                    if ((STATUS_CHECKED_IN.equals(snapshot.status)
                            || STATUS_PENDING_EXTRA.equals(snapshot.status)
                            || STATUS_CHECKED_OUT.equals(snapshot.status)
                            || STATUS_FINISHED.equals(snapshot.status))
                            && !bookingResourceDAO.hasOutstandingRemainingAmount(conn, bookingId)
                            && ((STATUS_CHECKED_IN.equals(snapshot.status) || STATUS_PENDING_EXTRA.equals(snapshot.status))
                            ? (snapshot.scheduleEnd != null && !LocalDateTime.now().isBefore(snapshot.scheduleEnd))
                            : true)) {

                        if (STATUS_PENDING_EXTRA.equals(snapshot.status)
                                && !isPaymentSuccessful(conn, bookingId)) {
                            break;
                        }

                        success = updateBookingStatus(conn, bookingId, STATUS_COMPLETED, snapshot.status);
                        if (success) {
                            bookingResourceDAO.releaseBookingResources(conn, bookingId, snapshot.scheduleId, false);
                        }
                    }
                    break;

                case STATUS_CANCELLED:
                    if (STATUS_PENDING.equals(snapshot.status)
                            || STATUS_PAID.equals(snapshot.status)
                            || STATUS_DEPOSITED.equals(snapshot.status)
                            || STATUS_CHECKED_OUT.equals(snapshot.status)
                            || STATUS_FINISHED.equals(snapshot.status)) {

                        success = updateBookingStatus(conn, bookingId, STATUS_CANCELLED, snapshot.status);

                        if (success) {
                            if (STATUS_PENDING.equals(snapshot.status)) {
                                updatePaymentStatusByBooking(conn, bookingId, "FAILED", true);
                            }
                            bookingResourceDAO.releaseBookingResources(conn, bookingId, snapshot.scheduleId);
                        }
                    }
                    break;

                default:
                    success = false;
            }

            if (!success) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markBookingPaid(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            if (STATUS_PAID.equals(snapshot.paymentStatus)) {
                conn.rollback();
                return true;
            }

            if (!STATUS_PENDING.equals(snapshot.paymentStatus) && !STATUS_DEPOSITED.equals(snapshot.paymentStatus)) {
                conn.rollback();
                return false;
            }

            boolean updated = updateSplitPaymentStatus(conn, bookingId, STATUS_PAID);
            if (!updated) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markBookingDeposited(UUID bookingId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            boolean updated = updateBookingStatus(conn, bookingId, STATUS_DEPOSITED, STATUS_PENDING);
            if (!updated) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean updateSplitPaymentStatus(Connection conn, UUID bookingId, String paymentStatus) throws SQLException {
        String sql = "UPDATE Booking SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeStatus(paymentStatus));
            ps.setString(2, bookingId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSplitStates(UUID bookingId, String playStatus, String paymentStatus, String extraPaymentStatus) {
        String normalizedPlayStatus = normalizeStatus(playStatus);
        String normalizedPaymentStatus = normalizeStatus(paymentStatus);
        String normalizedExtraPaymentStatus = normalizeStatus(extraPaymentStatus);

        if (!SUPPORTED_PLAY_STATUSES.contains(normalizedPlayStatus)
                || !SUPPORTED_PAYMENT_STATUSES.contains(normalizedPaymentStatus)
                || !SUPPORTED_EXTRA_PAYMENT_STATUSES.contains(normalizedExtraPaymentStatus)) {
            return false;
        }

        String sql = "UPDATE Booking "
                + "SET play_status = ?, payment_status = ?, extra_payment_status = ? "
                + "WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            BookingSnapshot snapshot = getBookingSnapshot(conn, bookingId);
            if (snapshot == null) {
                conn.rollback();
                return false;
            }

            String previousLifecycleStatus = snapshot.status;
            int updatedRows;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, normalizedPlayStatus);
                ps.setString(2, normalizedPaymentStatus);
                ps.setString(3, normalizedExtraPaymentStatus);
                ps.setString(4, bookingId.toString());
                updatedRows = ps.executeUpdate();
            }

            if (updatedRows <= 0) {
                conn.rollback();
                return false;
            }

            if (!bookingResourceDAO.applyCashSettlementForPaidExtra(
                    conn,
                    bookingId,
                    snapshot.paymentStatus,
                    snapshot.extraPaymentStatus,
                    normalizedPaymentStatus,
                    normalizedExtraPaymentStatus)) {
                conn.rollback();
                return false;
            }

            if (!bookingResourceDAO.applyCashSettlementForDepositedUpgrade(
                    conn,
                    bookingId,
                    snapshot.paymentStatus,
                    normalizedPaymentStatus,
                    normalizedExtraPaymentStatus)) {
                conn.rollback();
                return false;
            }

            String nextLifecycleStatus = resolveLifecycleStatus(
                    normalizedPlayStatus,
                    normalizedPaymentStatus,
                    normalizedExtraPaymentStatus);
            boolean releaseSchedule = bookingResourceDAO.shouldReleaseSchedule(nextLifecycleStatus)
                    && !bookingResourceDAO.shouldReleaseSchedule(previousLifecycleStatus);
            boolean releaseEquipment = bookingResourceDAO.shouldReleaseEquipment(nextLifecycleStatus)
                    && !bookingResourceDAO.shouldReleaseEquipment(previousLifecycleStatus);

            if (releaseEquipment) {
                bookingResourceDAO.releaseBookingResources(conn, bookingId, snapshot.scheduleId, releaseSchedule);
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isBookedByStaff(UUID bookingId) {
        if (bookingId == null) {
            return false;
        }

        String sql = "SELECT LOWER(ISNULL(r.role_name, '')) AS booker_role_name "
                + "FROM Booking b "
                + "LEFT JOIN Users u ON u.user_id = b.booker_id "
                + "LEFT JOIN Role r ON r.role_id = u.role_id "
                + "WHERE b.booking_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return "staff".equals(normalizeStatus(rs.getString("booker_role_name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void synchronizeBookingStates() {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            String sql = "SELECT b.booking_id, LOWER(" + lifecycleStatusExpression("b") + ") AS booking_status "
                    + "FROM Booking b "
                    + "INNER JOIN Schedule s ON s.schedule_id = b.schedule_id "
                    + "WHERE LOWER(" + lifecycleStatusExpression("b") + ") IN ('paid', 'deposited', 'checked in', 'checked out') "
                    + "AND (s.booking_date < CAST(SYSDATETIME() AS DATE) "
                    + "     OR (s.booking_date = CAST(SYSDATETIME() AS DATE) AND s.end_time <= CAST(SYSDATETIME() AS TIME)))";

            List<BookingSnapshot> expiredBookings = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingSnapshot snapshot = new BookingSnapshot();
                    snapshot.bookingId = UUID.fromString(rs.getString("booking_id"));
                    snapshot.status = normalizeStatus(rs.getString("booking_status"));
                    expiredBookings.add(snapshot);
                }
            }

            for (BookingSnapshot expiredBooking : expiredBookings) {
                BookingSnapshot snapshot = getBookingSnapshot(conn, expiredBooking.bookingId);
                if (snapshot == null) {
                    continue;
                }
                if (STATUS_PAID.equals(snapshot.status) || STATUS_DEPOSITED.equals(snapshot.status)) {
                    boolean updated = updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CANCELLED, snapshot.status);
                    if (updated) {
                        bookingResourceDAO.releaseBookingResources(conn, expiredBooking.bookingId, snapshot.scheduleId);
                    }
                } else if (STATUS_CHECKED_IN.equals(snapshot.status)) {
                    updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CHECKED_OUT, STATUS_CHECKED_IN);
                } else if (STATUS_CHECKED_OUT.equals(snapshot.status) || STATUS_FINISHED.equals(snapshot.status)) {
                    if (STATUS_FINISHED.equals(snapshot.status)) {
                        updateBookingStatus(conn, expiredBooking.bookingId, STATUS_CHECKED_OUT, STATUS_FINISHED);
                    }
                    if (!bookingResourceDAO.hasOutstandingRemainingAmount(conn, expiredBooking.bookingId)) {
                        boolean updated = updateBookingStatus(conn, expiredBooking.bookingId, STATUS_COMPLETED, STATUS_CHECKED_OUT);
                        if (!updated) {
                            updated = updateBookingStatus(conn, expiredBooking.bookingId, STATUS_COMPLETED, STATUS_FINISHED);
                        }
                        if (updated) {
                            bookingResourceDAO.releaseBookingResources(conn, expiredBooking.bookingId, snapshot.scheduleId, false);
                        }
                    }
                }
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BookingSnapshot getBookingSnapshot(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT b.schedule_id, b.field_id, b.play_status, b.payment_status, b.extra_payment_status, "
                + "s.booking_date, s.start_time, s.end_time "
                + "FROM Booking b "
                + "LEFT JOIN Schedule s ON s.schedule_id = b.schedule_id "
                + "WHERE b.booking_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                BookingSnapshot snapshot = new BookingSnapshot();
                String scheduleId = rs.getString("schedule_id");
                if (scheduleId != null) {
                    snapshot.scheduleId = UUID.fromString(scheduleId);
                }
                snapshot.playStatus = normalizeStatus(rs.getString("play_status"));
                snapshot.paymentStatus = normalizeStatus(rs.getString("payment_status"));
                snapshot.extraPaymentStatus = normalizeStatus(rs.getString("extra_payment_status"));
                snapshot.status = resolveLifecycleStatus(snapshot.playStatus, snapshot.paymentStatus, snapshot.extraPaymentStatus);

                Date bookingDate = rs.getDate("booking_date");
                Time startTime = rs.getTime("start_time");
                Time endTime = rs.getTime("end_time");
                if (bookingDate != null && startTime != null) {
                    snapshot.scheduleStart = LocalDateTime.of(bookingDate.toLocalDate(), startTime.toLocalTime());
                }
                if (bookingDate != null && endTime != null) {
                    snapshot.scheduleEnd = LocalDateTime.of(bookingDate.toLocalDate(), endTime.toLocalTime());
                }
                return snapshot;
            }
        }
    }

    BookingSplitState resolveNextSplitState(BookingSnapshot current, String newStatus) {
        String currentPlay = normalizeStatus(current.playStatus);
        String currentPayment = normalizeStatus(current.paymentStatus);
        String currentExtra = normalizeStatus(current.extraPaymentStatus);

        if (currentPlay.isEmpty()) {
            currentPlay = PLAY_STATUS_BOOKED;
        }
        if (currentPayment.isEmpty()) {
            currentPayment = STATUS_PENDING;
        }
        if (currentExtra.isEmpty()) {
            currentExtra = EXTRA_PAYMENT_STATUS_NONE;
        }

        switch (newStatus) {
            case STATUS_PENDING:
                return new BookingSplitState(PLAY_STATUS_BOOKED, STATUS_PENDING, EXTRA_PAYMENT_STATUS_NONE);
            case STATUS_DEPOSITED:
                return new BookingSplitState(PLAY_STATUS_BOOKED, STATUS_DEPOSITED, currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra);
            case STATUS_PAID:
                return new BookingSplitState(currentPlay.isEmpty() ? PLAY_STATUS_BOOKED : currentPlay, STATUS_PAID, currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra);
            case STATUS_CHECKED_IN:
                return new BookingSplitState(
                        STATUS_CHECKED_IN,
                        currentPayment.isEmpty() ? STATUS_PAID : currentPayment,
                        STATUS_PENDING_EXTRA.equals(currentExtra) ? EXTRA_PAYMENT_STATUS_PAID : (currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra)
                );
            case STATUS_PENDING_EXTRA:
                return new BookingSplitState(
                        STATUS_CHECKED_IN,
                        currentPayment.isEmpty() ? STATUS_PAID : currentPayment,
                        STATUS_PENDING_EXTRA
                );
            case STATUS_CHECKED_OUT:
                return new BookingSplitState(
                        STATUS_CHECKED_OUT,
                        currentPayment.isEmpty() ? STATUS_PAID : currentPayment,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            case STATUS_COMPLETED:
                return new BookingSplitState(
                        STATUS_COMPLETED,
                        STATUS_PAID,
                        STATUS_PENDING_EXTRA.equals(currentExtra) ? EXTRA_PAYMENT_STATUS_PAID : (currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra)
                );
            case STATUS_PENDING_REFUND:
                return new BookingSplitState(
                        PLAY_STATUS_CANCELLED,
                        STATUS_PENDING_REFUND,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            case STATUS_REFUNDED:
                return new BookingSplitState(
                        PLAY_STATUS_CANCELLED,
                        STATUS_REFUNDED,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            case STATUS_CANCELLED:
                return new BookingSplitState(
                        PLAY_STATUS_CANCELLED,
                        PAYMENT_STATUS_FAILED,
                        currentExtra.isEmpty() ? EXTRA_PAYMENT_STATUS_NONE : currentExtra
                );
            default:
                return new BookingSplitState(
                        resolvePlayStatus(newStatus),
                        resolvePaymentStatus(newStatus),
                        resolveExtraPaymentStatus(newStatus)
                );
        }
    }

    String resolveLifecycleStatus(String playStatus, String paymentStatus, String extraPaymentStatus) {
        String normalizedPlay = normalizeStatus(playStatus);
        String normalizedPayment = normalizeStatus(paymentStatus);
        String normalizedExtra = normalizeStatus(extraPaymentStatus);

        if (STATUS_PENDING_REFUND.equals(normalizedPayment)) {
            return STATUS_PENDING_REFUND;
        }
        if (STATUS_REFUNDED.equals(normalizedPayment)) {
            return STATUS_REFUNDED;
        }
        if (PAYMENT_STATUS_FAILED.equals(normalizedPayment) || STATUS_CANCELLED.equals(normalizedPlay)) {
            return STATUS_CANCELLED;
        }
        if (STATUS_COMPLETED.equals(normalizedPlay)) {
            return STATUS_COMPLETED;
        }
        if (STATUS_CHECKED_OUT.equals(normalizedPlay)) {
            return STATUS_CHECKED_OUT;
        }
        if (STATUS_CHECKED_IN.equals(normalizedPlay)) {
            return STATUS_PENDING_EXTRA.equals(normalizedExtra) ? STATUS_PENDING_EXTRA : STATUS_CHECKED_IN;
        }
        if (STATUS_DEPOSITED.equals(normalizedPayment)) {
            return STATUS_DEPOSITED;
        }
        if (STATUS_PAID.equals(normalizedPayment)) {
            return STATUS_PAID;
        }
        return STATUS_PENDING;
    }

    String resolvePlayStatus(String lifecycleStatus) {
        String normalized = normalizeStatus(lifecycleStatus);
        if (STATUS_CHECKED_IN.equals(normalized) || STATUS_PENDING_EXTRA.equals(normalized)) {
            return STATUS_CHECKED_IN;
        }
        if (STATUS_FINISHED.equals(normalized) || STATUS_CHECKED_OUT.equals(normalized)) {
            return STATUS_CHECKED_OUT;
        }
        if (STATUS_COMPLETED.equals(normalized)) {
            return STATUS_COMPLETED;
        }
        if (STATUS_CANCELLED.equals(normalized)
                || STATUS_REFUNDED.equals(normalized)
                || STATUS_PENDING_REFUND.equals(normalized)) {
            return PLAY_STATUS_CANCELLED;
        }
        return PLAY_STATUS_BOOKED;
    }

    String resolvePaymentStatus(String lifecycleStatus) {
        String normalized = normalizeStatus(lifecycleStatus);
        if (STATUS_PENDING.equals(normalized)) {
            return STATUS_PENDING;
        }
        if (STATUS_DEPOSITED.equals(normalized)) {
            return STATUS_DEPOSITED;
        }
        if (STATUS_PENDING_REFUND.equals(normalized)) {
            return STATUS_PENDING_REFUND;
        }
        if (STATUS_REFUNDED.equals(normalized)) {
            return STATUS_REFUNDED;
        }
        if (STATUS_CANCELLED.equals(normalized)) {
            return PAYMENT_STATUS_FAILED;
        }
        return STATUS_PAID;
    }

    String resolveExtraPaymentStatus(String lifecycleStatus) {
        return STATUS_PENDING_EXTRA.equals(normalizeStatus(lifecycleStatus)) ? STATUS_PENDING_EXTRA : EXTRA_PAYMENT_STATUS_NONE;
    }

    String lifecycleStatusExpression(String alias) {
        String p = alias + ".payment_status";
        String pl = alias + ".play_status";
        String ex = alias + ".extra_payment_status";
        return "CASE "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'pending refund' THEN 'pending refund' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'refunded' THEN 'refunded' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'failed' OR LOWER(ISNULL(" + pl + ", '')) = 'cancelled' THEN 'cancelled' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'completed' THEN 'completed' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'checked out' THEN 'checked out' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'checked in' AND LOWER(ISNULL(" + ex + ", 'none')) = 'pending extra' THEN 'pending extra' "
                + "WHEN LOWER(ISNULL(" + pl + ", '')) = 'checked in' THEN 'checked in' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'deposited' THEN 'deposited' "
                + "WHEN LOWER(ISNULL(" + p + ", '')) = 'paid' THEN 'paid' "
                + "ELSE 'pending' END";
    }

    void applyStateToBookingViewModel(ResultSet rs, BookingViewModel vm) throws SQLException {
        String playStatus = normalizeStatus(rs.getString("play_status"));
        String paymentStatus = normalizeStatus(rs.getString("payment_status"));
        String extraPaymentStatus = normalizeStatus(rs.getString("extra_payment_status"));
        String lifecycleStatus = resolveLifecycleStatus(playStatus, paymentStatus, extraPaymentStatus);

        vm.setStatus(lifecycleStatus);
        vm.setPlayStatus(playStatus);
        vm.setPaymentStatus(paymentStatus);
        vm.setExtraPaymentStatus(extraPaymentStatus);
    }

    void applyStateToBookingEntity(ResultSet rs, Booking booking) throws SQLException {
        String playStatus = normalizeStatus(rs.getString("play_status"));
        String paymentStatus = normalizeStatus(rs.getString("payment_status"));
        String extraPaymentStatus = normalizeStatus(rs.getString("extra_payment_status"));
        String lifecycleStatus = resolveLifecycleStatus(playStatus, paymentStatus, extraPaymentStatus);

        booking.setStatus(lifecycleStatus);
        booking.setPlayStatus(playStatus);
        booking.setPaymentStatus(paymentStatus);
        booking.setExtraPaymentStatus(extraPaymentStatus);
    }

    String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase();
    }

    boolean updateBookingStatus(Connection conn, UUID bookingId, String newStatus, String expectedStatus) throws SQLException {
        String normalizedExpectedStatus = normalizeStatus(expectedStatus);
        BookingSnapshot current = getBookingSnapshot(conn, bookingId);
        if (current == null || !normalizedExpectedStatus.equals(normalizeStatus(current.status))) {
            return false;
        }

        BookingSplitState nextState = resolveNextSplitState(current, normalizeStatus(newStatus));
        String sql = "UPDATE Booking "
                + "SET play_status = ?, payment_status = ?, extra_payment_status = ? "
                + "WHERE booking_id = ? "
                + "AND LOWER(ISNULL(play_status, '')) = ? "
                + "AND LOWER(ISNULL(payment_status, '')) = ? "
                + "AND LOWER(ISNULL(extra_payment_status, '')) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nextState.playStatus);
            ps.setString(2, nextState.paymentStatus);
            ps.setString(3, nextState.extraPaymentStatus);
            ps.setString(4, bookingId.toString());
            ps.setString(5, current.playStatus);
            ps.setString(6, current.paymentStatus);
            ps.setString(7, current.extraPaymentStatus);
            return ps.executeUpdate() > 0;
        }
    }

    private void updatePaymentStatusByBooking(Connection conn, UUID bookingId, String paymentStatus, boolean touchPaymentTime) throws SQLException {
        String sql = touchPaymentTime
                ? "UPDATE Payment SET payment_status = ?, payment_time = SYSDATETIME() WHERE booking_id = ?"
                : "UPDATE Payment SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, bookingId.toString());
            ps.executeUpdate();
        }
    }

    private boolean isPaymentSuccessful(Connection conn, UUID bookingId) throws SQLException {
        String sql = "SELECT LOWER(ISNULL(payment_status, '')) AS payment_status FROM Payment WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String paymentStatus = normalizeStatus(rs.getString("payment_status"));
                return "success".equals(paymentStatus) || "paid".equals(paymentStatus);
            }
        }
    }

    static class BookingSnapshot {
        UUID bookingId;
        UUID scheduleId;
        String status;
        String playStatus;
        String paymentStatus;
        String extraPaymentStatus;
        LocalDateTime scheduleStart;
        LocalDateTime scheduleEnd;
    }

    static class BookingSplitState {
        final String playStatus;
        final String paymentStatus;
        final String extraPaymentStatus;

        BookingSplitState(String playStatus, String paymentStatus, String extraPaymentStatus) {
            this.playStatus = playStatus;
            this.paymentStatus = paymentStatus;
            this.extraPaymentStatus = extraPaymentStatus;
        }
    }

    static class LatestPaymentInfo {
        final BigDecimal amount;
        final String paymentMethod;

        LatestPaymentInfo(BigDecimal amount, String paymentMethod) {
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }
    }
}
