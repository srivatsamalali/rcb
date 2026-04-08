# RCB Ticket Checker

A small Java-based automation utility that monitors the Royal Challengers Bangalore ticket booking page and alerts the user when the "Buy Now" button appears.

## Features

- Opens a visible Chrome browser using Selenium
- Refreshes the RCB ticket page at a configured interval
- Detects the "BUY TICKETS" button
- Plays a local beep and optionally sends an email alert

## Requirements

- Java 17
- Maven
- Chrome browser installed
- Internet access for Selenium Manager and Gmail SMTP (optional)

## Setup

1. Clone or download this repository.
2. Ensure Java 17 is installed and available on your PATH.
3. Ensure Maven is installed and available on your PATH.
4. Verify Chrome is installed.

## Configuration

The app reads settings from `config.properties` in the project root.

Example values:

```properties
# SMTP/Email configuration
sender.email=your-email@gmail.com
smtp.app.password=your-app-password
receiver.email=recipient@example.com

# Timing Configuration
check.interval.seconds=3
# check.interval.minutes=1
```

- `sender.email`: Gmail address used to send alerts
- `smtp.app.password`: app password for Gmail SMTP
- `receiver.email`: one or more comma-separated addresses to notify
- `check.interval.seconds`: polling interval in seconds
- `check.interval.minutes`: optional legacy fallback if seconds is not configured

## Build

From the repository root:

```bash
mvn package
```

This builds a shaded JAR with the main class `com.ticketchecker.RCBTicketChecker`.

## Run

From the repository root:

```bash
java -jar target/rcb-ticket-checker-1.0-SNAPSHOT.jar
```

If you want to run directly from Maven during development:

```bash
mvn exec:java -Dexec.mainClass="com.ticketchecker.RCBTicketChecker"
```

## Notes

- The app uses Selenium ChromeDriver via Selenium Manager, so no separate driver binary is required.
- The browser is launched in a visible window and kept open after the program ends.
- If email settings are not configured, the app still alerts locally by clicking the button and beeping.

## Disclaimer

Use responsibly. This tool is intended for personal monitoring of ticket availability and not for abusive or automated bulk booking.
