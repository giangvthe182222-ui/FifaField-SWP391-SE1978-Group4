package Listener;

import DAO.PaymentDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Payment Scheduler Listener
 * Automatically checks and cancels overdue bookings every minute
 * Runs in background when application starts
 */
@WebListener
public class PaymentScheduler implements ServletContextListener {
    
    private ScheduledExecutorService scheduler;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[PaymentScheduler] Starting payment scheduler...");
        
        // Create scheduler with 1 thread
        scheduler = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "PaymentScheduler-Thread");
            thread.setDaemon(true);
            return thread;
        });
        
        // Schedule task to check and cancel overdue bookings every minute
        scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean result = PaymentDAO.checkAndCancelOverdueBookings();
                System.out.println("[PaymentScheduler] Auto-cancel check completed. Bookings cancelled: " + result);
            } catch (Exception e) {
                System.err.println("[PaymentScheduler] Error checking overdue bookings:");
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.MINUTES); // Initial delay 1 min, then every 1 minute
        
        System.out.println("[PaymentScheduler] Started. Will check overdue bookings every 1 minute.");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[PaymentScheduler] Shutting down payment scheduler...");
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            
            try {
                // Wait for ongoing tasks to complete
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    System.out.println("[PaymentScheduler] Force shutdown completed.");
                } else {
                    System.out.println("[PaymentScheduler] Graceful shutdown completed.");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
