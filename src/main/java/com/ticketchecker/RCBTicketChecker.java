package com.ticketchecker;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RCBTicketChecker {

    private static final String CONFIG_FILE = "config.properties";
    private static final String TARGET_URL = "https://shop.royalchallengers.com/ticket";
    private static final Logger LOGGER = Logger.getLogger(RCBTicketChecker.class.getName());
    private static boolean firstLoad = true;

    public static void main(String[] args) {
        Properties config = loadConfig();
        int intervalSeconds = 3; // default 3 seconds for aggressive polling

        if (config != null) {
            if (config.getProperty("check.interval.seconds") != null) {
                intervalSeconds = Integer.parseInt(config.getProperty("check.interval.seconds"));
            } else if (config.getProperty("check.interval.minutes") != null) {
                intervalSeconds = Integer.parseInt(config.getProperty("check.interval.minutes")) * 60;
            }
        }

        LOGGER.info("Starting RCB Ticket Checker. Will refresh browser every " + intervalSeconds + " seconds...");

        WebDriver driver = BrowserManager.getVisibleDriver();
        if (driver == null) {
            LOGGER.severe("Unable to create browser driver. Exiting.");
            return;
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                checkTicketsAndAlert(scheduler, config, driver);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An error occurred during checking loop:", e);
            }
        };

        // Schedule to run initially without delay, then continuously.
        scheduler.scheduleAtFixedRate(task, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    private static void checkTicketsAndAlert(ScheduledExecutorService scheduler, Properties config, WebDriver driver) {
        ZonedDateTime nowIst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        String formattedIst = nowIst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        LOGGER.info("Srivatsa is checking RCB website for tickets at: " + formattedIst);
        boolean buttonFound = false;
        try {
            if (firstLoad) {
                driver.get(TARGET_URL);
                firstLoad = false;
            } else {
                driver.navigate().refresh();
            }

            // Wait up to 15 seconds for the BUY NOW button to become visible.
            String xpath = "//button[contains(translate(normalize-space(.), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'BUY TICKETS') or contains(translate(normalize-space(.), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'SOLD OUT')]";
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            try {
                WebElement buyNowButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
                LOGGER.info("!!! 'BUY NOW' BUTTON FOUND !!! Handing over to you.");
                buttonFound = true;

                // Click the button to speed things up
                buyNowButton.click();

                // Play a beep sound
                java.awt.Toolkit.getDefaultToolkit().beep();

                // Send email alert, if configured
                String senderEmail = config.getProperty("sender.email");
                String appPassword = config.getProperty("smtp.app.password");
                String receiverEmail = config.getProperty("receiver.email");

                if (senderEmail == null || appPassword == null || receiverEmail == null) {
                    LOGGER.warning("Email not sent because sender.email, smtp.app.password, or receiver.email is not configured in config.properties.");
                } else {
                    EmailNotifier.sendEmailAlert(senderEmail, appPassword, receiverEmail);
                }

                // stop scheduling; we already found the button
                scheduler.shutdown();

                // close driver after shutdown so target path is clicked and user can proceed
                driver.quit();

            } catch (TimeoutException te) {
                LOGGER.info("No 'Buy Now' button visible yet (wait timed out). Will try again on next interval.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while checking website", e);
            try {
                driver.quit();
            } catch (Exception ignore) {
            }
            scheduler.shutdown();
        }
    }

    private static Properties loadConfig() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            prop.load(fis);
            return prop;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading " + CONFIG_FILE, e);
            return null;
        }
    }
}
