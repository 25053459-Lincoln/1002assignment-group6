package calenderapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CLIMenuApp {
    private EventManager eventManager;
    private CalenderViewCLI calendarView;
    private Scanner scanner;
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public CLIMenuApp() {
        this.eventManager = new EventManager();
        this.calendarView = new CalenderViewCLI(eventManager);
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Calendar Scheduler - CLI Interface   ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        boolean running = true;
        while (running) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1" -> quickViewMenu();
                case "2" -> listViewMenu();
                case "3" -> calendarViewMenu();
                case "4" -> addEventInteractive();
                case "5" -> searchMenu();
                case "6" -> statisticsView();
                case "7" -> launchGUI();
                case "0" -> {
                    System.out.println("\nGoodbye! Your events have been saved.");
                    running = false;
                }
                default -> System.out.println("\n❌ Invalid choice. Please try again.\n");
            }
        }
        
        scanner.close();
    }
    
    private void displayMainMenu() {
        System.out.println("┌────────────────────────────────────────┐");
        System.out.println("│             MAIN MENU                  │");
        System.out.println("├────────────────────────────────────────┤");
        System.out.println("│ 1. Quick View (Today/Week/Month)       │");
        System.out.println("│ 2. List Views                          │");
        System.out.println("│ 3. Calendar Views                      │");
        System.out.println("│ 4. Add Event                           │");
        System.out.println("│ 5. Search Events                       │");
        System.out.println("│ 6. Statistics                          │");
        System.out.println("│ 7. Launch GUI                          │");
        System.out.println("│ 0. Exit                                │");
        System.out.println("└────────────────────────────────────────┘");
        System.out.print("\nEnter your choice: ");
    }
    
    private void quickViewMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            QUICK VIEW                  ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("1. Today");
        System.out.println("2. This Week");
        System.out.println("3. This Month");
        System.out.println("4. Upcoming (Next 7 days)");
        System.out.println("0. Back");
        System.out.print("\nChoice: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1" -> calendarView.displayToday();
            case "2" -> calendarView.displayThisWeek();
            case "3" -> calendarView.displayThisMonth();
            case "4" -> calendarView.displayUpcoming();
            case "0" -> {}
            default -> System.out.println("\n❌ Invalid choice.\n");
        }
        
        if (!choice.equals("0")) {
            pressEnterToContinue();
        }
    }
    
    private void listViewMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            LIST VIEWS                  ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("1. Day List View");
        System.out.println("2. Week List View");
        System.out.println("3. Month List View");
        System.out.println("0. Back");
        System.out.print("\nChoice: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter date (yyyy-MM-dd) or press Enter for today: ");
                    String dateInput = scanner.nextLine().trim();
                    LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput, dateFormat);
                    calendarView.displayDayListView(date);
                }
                case "2" -> {
                    System.out.print("Enter week start date (yyyy-MM-dd) or press Enter for this week: ");
                    String dateInput = scanner.nextLine().trim();
                    LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput, dateFormat);
                    calendarView.displayWeekListView(date);
                }
                case "3" -> {
                    System.out.print("Enter month (yyyy-MM) or press Enter for this month: ");
                    String monthInput = scanner.nextLine().trim();
                    YearMonth yearMonth = monthInput.isEmpty() ? YearMonth.now() : YearMonth.parse(monthInput);
                    calendarView.displayMonthListView(yearMonth);
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("\n❌ Invalid choice.\n");
            }
        } catch (DateTimeParseException e) {
            System.out.println("\n❌ Invalid date format. Please use yyyy-MM-dd.\n");
        }
        
        if (!choice.equals("0")) {
            pressEnterToContinue();
        }
    }
    
    private void calendarViewMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          CALENDAR VIEWS                ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("1. Month Calendar View");
        System.out.println("2. Week Calendar View");
        System.out.println("0. Back");
        System.out.print("\nChoice: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter month (yyyy-MM) or press Enter for this month: ");
                    String monthInput = scanner.nextLine().trim();
                    YearMonth yearMonth = monthInput.isEmpty() ? YearMonth.now() : YearMonth.parse(monthInput);
                    calendarView.displayMonthCalendarView(yearMonth);
                }
                case "2" -> {
                    System.out.print("Enter week start date (yyyy-MM-dd) or press Enter for this week: ");
                    String dateInput = scanner.nextLine().trim();
                    LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput, dateFormat);
                    calendarView.displayWeekCalendarView(date);
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("\n❌ Invalid choice.\n");
            }
        } catch (DateTimeParseException e) {
            System.out.println("\n❌ Invalid date format.\n");
        }
        
        if (!choice.equals("0")) {
            pressEnterToContinue();
        }
    }
    
    private void addEventInteractive() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            ADD NEW EVENT               ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("\n❌ Title cannot be empty.\n");
                return;
            }
            
            System.out.print("Description (optional): ");
            String description = scanner.nextLine().trim();
            
            System.out.print("Date (yyyy-MM-dd): ");
            String dateStr = scanner.nextLine().trim();
            LocalDate date = LocalDate.parse(dateStr, dateFormat);
            
            System.out.print("Start time (HH:mm): ");
            String startTimeStr = scanner.nextLine().trim();
            String[] startParts = startTimeStr.split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            
            System.out.print("End time (HH:mm): ");
            String endTimeStr = scanner.nextLine().trim();
            String[] endParts = endTimeStr.split(":");
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            LocalDateTime start = LocalDateTime.of(date, java.time.LocalTime.of(startHour, startMinute));
            LocalDateTime end = LocalDateTime.of(date, java.time.LocalTime.of(endHour, endMinute));
            
            if (end.isBefore(start) || end.equals(start)) {
                System.out.println("\n❌ End time must be after start time.\n");
                return;
            }
            
            System.out.print("Location (optional): ");
            String location = scanner.nextLine().trim();
            
            System.out.print("Category (optional): ");
            String category = scanner.nextLine().trim();
            
            System.out.print("Reminder minutes before (0 for none): ");
            int reminderMinutes = Integer.parseInt(scanner.nextLine().trim());
            
            eventManager.createEvent(title, description, start, end, location, category, "", reminderMinutes);
            
            System.out.println("\n✓ Event created successfully!\n");
            
        } catch (Exception e) {
            System.out.println("\n❌ Error creating event: " + e.getMessage() + "\n");
        }
    }
    
    private void searchMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          SEARCH EVENTS                 ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("1. Search by keyword");
        System.out.println("2. Search by date range");
        System.out.println("0. Back");
        System.out.print("\nChoice: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter keyword: ");
                    String keyword = scanner.nextLine().trim();
                    var results = eventManager.searchByKeyword(keyword);
                    displaySearchResults(results);
                }
                case "2" -> {
                    System.out.print("Start date (yyyy-MM-dd): ");
                    LocalDate start = LocalDate.parse(scanner.nextLine().trim(), dateFormat);
                    System.out.print("End date (yyyy-MM-dd): ");
                    LocalDate end = LocalDate.parse(scanner.nextLine().trim(), dateFormat);
                    var results = eventManager.searchByDateRange(start, end);
                    displaySearchResults(results);
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("\n❌ Invalid choice.\n");
            }
        } catch (Exception e) {
            System.out.println("\n❌ Error: " + e.getMessage() + "\n");
        }
        
        if (!choice.equals("0")) {
            pressEnterToContinue();
        }
    }
    
    private void displaySearchResults(java.util.List<Event> results) {
        System.out.println("\n" + "=".repeat(50));
        if (results.isEmpty()) {
            System.out.println("No events found.");
        } else {
            System.out.println("Found " + results.size() + " event(s):\n");
            results.sort(java.util.Comparator.comparing(Event::getStart));
            for (Event event : results) {
                System.out.printf("[%d] %s%s\n", event.getEventId(), event.getTitle(), 
                                event.isRecurring() ? " [Recurring]" : "");
                System.out.printf("    %s to %s\n", event.getStart(), event.getEnd());
                if (!event.getDescription().isEmpty()) {
                    System.out.printf("    %s\n", event.getDescription());
                }
                System.out.println();
            }
        }
        System.out.println("=".repeat(50));
    }
    
    private void statisticsView() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           STATISTICS                   ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("Total Events: " + eventManager.getTotalEvents());
        System.out.println("Recurring Events: " + eventManager.getRecurringEventCount());
        System.out.println("Single Events: " + (eventManager.getTotalEvents() - eventManager.getRecurringEventCount()));
        System.out.println("Busiest Day: " + eventManager.getBusiestDay());
        
        var categoryStats = eventManager.getEventsByCategory();
        if (!categoryStats.isEmpty()) {
            System.out.println("\nEvents by Category:");
            categoryStats.forEach((cat, count) -> 
                System.out.println("  " + cat + ": " + count));
        }
        
        System.out.println();
        pressEnterToContinue();
    }
    
    private void launchGUI() {
        System.out.println("\n🚀 Launching GUI...\n");
        javax.swing.SwingUtilities.invokeLater(() -> new CalenderApp());
    }
    
    private void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
        System.out.println();
    }
    
    public static void main(String[] args) {
        new CLIMenuApp().start();
    }
}