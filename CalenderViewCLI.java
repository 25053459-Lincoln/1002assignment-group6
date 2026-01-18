package calenderapp;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class CalenderViewCLI {
    private EventManager eventManager;
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public CalenderViewCLI(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    // ==================== LIST VIEWS ====================
    
    /**
     * Display events for a specific day
     */
    public void displayDayListView(LocalDate date) {
        System.out.println("\n=== " + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) + " ===");
        
        List<Event> dayEvents = new ArrayList<>(eventManager.getEventsForDate(date));
        
        if (dayEvents.isEmpty()) {
            System.out.println("No events scheduled.");
        } else {
            dayEvents.sort(Comparator.comparing(Event::getStart));
            for (Event event : dayEvents) {
                String timeStr = event.getStart().format(timeFormat);
                String recurring = event.isRecurring() ? " [R]" : "";
                System.out.println("  " + timeStr + " - " + event.getTitle() + recurring);
                if (!event.getDescription().isEmpty()) {
                    System.out.println("       " + event.getDescription());
                }
            }
        }
        System.out.println();
    }
    
    /**
     * Display events for a week
     */
    public void displayWeekListView(LocalDate startOfWeek) {
        // Ensure we start on Sunday
        LocalDate sunday = startOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        
        System.out.println("\n=== Week of " + sunday.format(dateFormat) + " ===");
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String dayNum = String.format("%02d", date.getDayOfMonth());
            
            List<Event> dayEvents = new ArrayList<>(eventManager.getEventsForDate(date));
            
            if (dayEvents.isEmpty()) {
                System.out.println(dayName + " " + dayNum + ": No events");
            } else {
                dayEvents.sort(Comparator.comparing(Event::getStart));
                System.out.print(dayName + " " + dayNum + ": ");
                
                if (dayEvents.size() == 1) {
                    Event event = dayEvents.get(0);
                    System.out.println(event.getTitle() + " (" + event.getStart().format(timeFormat) + ")");
                } else {
                    System.out.println();
                    for (Event event : dayEvents) {
                        System.out.println("       " + event.getTitle() + " (" + event.getStart().format(timeFormat) + ")");
                    }
                }
            }
        }
        System.out.println();
    }
    
    /**
     * Display events for a month in list format
     */
    public void displayMonthListView(YearMonth yearMonth) {
        System.out.println("\n=== " + yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + 
                          " " + yearMonth.getYear() + " ===\n");
        
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        
        Map<LocalDate, List<Event>> eventsByDate = new TreeMap<>();
        
        // Group events by date
        for (Event event : eventManager.getEvents()) {
            LocalDate eventDate = event.getStart().toLocalDate();
            if (!eventDate.isBefore(firstDay) && !eventDate.isAfter(lastDay)) {
                eventsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(event);
            }
        }
        
        if (eventsByDate.isEmpty()) {
            System.out.println("No events this month.");
        } else {
            for (Map.Entry<LocalDate, List<Event>> entry : eventsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<Event> events = entry.getValue();
                events.sort(Comparator.comparing(Event::getStart));
                
                System.out.println(date.format(DateTimeFormatter.ofPattern("EEE dd:")));
                for (Event event : events) {
                    String timeStr = event.getStart().format(timeFormat);
                    String recurring = event.isRecurring() ? " [R]" : "";
                    System.out.println("  " + timeStr + " - " + event.getTitle() + recurring);
                }
                System.out.println();
            }
        }
    }
    
    // ==================== CALENDAR VIEWS ====================
    
    /**
     * Display calendar view for a month
     */
    public void displayMonthCalendarView(YearMonth yearMonth) {
        System.out.println("\n" + yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + 
                          " " + yearMonth.getYear());
        
        // Header with day names
        System.out.println("Su Mo Tu We Th Fr Sa");
        
        LocalDate firstDay = yearMonth.atDay(1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 0 = Sunday
        
        // Print leading spaces
        for (int i = 0; i < startDayOfWeek; i++) {
            System.out.print("   ");
        }
        
        // Map to store which days have events
        Map<Integer, List<Event>> eventsByDay = new HashMap<>();
        for (Event event : eventManager.getEvents()) {
            LocalDate eventDate = event.getStart().toLocalDate();
            if (eventDate.getYear() == yearMonth.getYear() && 
                eventDate.getMonth() == yearMonth.getMonth()) {
                eventsByDay.computeIfAbsent(eventDate.getDayOfMonth(), k -> new ArrayList<>()).add(event);
            }
        }
        
        // Print days
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            String dayStr;
            if (eventsByDay.containsKey(day)) {
                dayStr = String.format("%2d*", day);
            } else {
                dayStr = String.format("%3d", day);
            }
            System.out.print(dayStr);
            
            // New line after Saturday
            if ((startDayOfWeek + day) % 7 == 0) {
                System.out.println();
            } else {
                System.out.print(" ");
            }
        }
        
        System.out.println("\n");
        
        // Print legend for days with events
        if (!eventsByDay.isEmpty()) {
            List<Integer> daysWithEvents = new ArrayList<>(eventsByDay.keySet());
            Collections.sort(daysWithEvents);
            
            for (int day : daysWithEvents) {
                List<Event> events = eventsByDay.get(day);
                events.sort(Comparator.comparing(Event::getStart));
                
                System.out.println("* " + day + ":");
                for (Event event : events) {
                    System.out.println("    " + event.getTitle() + " (" + event.getStart().format(timeFormat) + ")");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * Display calendar view for a week
     */
    public void displayWeekCalendarView(LocalDate startOfWeek) {
        LocalDate sunday = startOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate saturday = sunday.plusDays(6);
        
        System.out.println("\n=== Week: " + sunday.format(dateFormat) + " to " + 
                          saturday.format(dateFormat) + " ===\n");
        
        // Print header
        System.out.println("Time  | Sun | Mon | Tue | Wed | Thu | Fri | Sat |");
        System.out.println("------|-----|-----|-----|-----|-----|-----|-----|");
        
        // Collect all events for the week
        Map<LocalDate, List<Event>> eventsByDate = new TreeMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            List<Event> dayEvents = new ArrayList<>(eventManager.getEventsForDate(date));
            if (!dayEvents.isEmpty()) {
                eventsByDate.put(date, dayEvents);
            }
        }
        
        // Find time range
        int earliestHour = 8;
        int latestHour = 18;
        
        for (List<Event> events : eventsByDate.values()) {
            for (Event event : events) {
                earliestHour = Math.min(earliestHour, event.getStart().getHour());
                latestHour = Math.max(latestHour, event.getEnd().getHour() + 1);
            }
        }
        
        // Print time slots
        for (int hour = earliestHour; hour <= latestHour; hour++) {
            System.out.printf("%02d:00 |", hour);
            
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate date = sunday.plusDays(dayOffset);
                List<Event> dayEvents = eventsByDate.getOrDefault(date, new ArrayList<>());
                
                boolean hasEventAtHour = false;
                for (Event event : dayEvents) {
                    int eventHour = event.getStart().getHour();
                    if (eventHour == hour) {
                        System.out.print("  *  |");
                        hasEventAtHour = true;
                        break;
                    }
                }
                
                if (!hasEventAtHour) {
                    System.out.print("     |");
                }
            }
            System.out.println();
        }
        
        System.out.println();
        
        // Print event details
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            List<Event> dayEvents = eventsByDate.get(date);
            
            if (dayEvents != null && !dayEvents.isEmpty()) {
                String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
                System.out.println(dayName + " " + date.format(dateFormat) + ":");
                
                dayEvents.sort(Comparator.comparing(Event::getStart));
                for (Event event : dayEvents) {
                    System.out.println("  " + event.getStart().format(timeFormat) + " - " + 
                                      event.getEnd().format(timeFormat) + ": " + event.getTitle());
                }
                System.out.println();
            }
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Display today's events
     */
    public void displayToday() {
        displayDayListView(LocalDate.now());
    }
    
    /**
     * Display this week's events
     */
    public void displayThisWeek() {
        displayWeekListView(LocalDate.now());
    }
    
    /**
     * Display this month's events
     */
    public void displayThisMonth() {
        displayMonthListView(YearMonth.now());
    }
    
    /**
     * Display upcoming events (next 7 days)
     */
    public void displayUpcoming() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        
        System.out.println("\n=== Upcoming Events (Next 7 Days) ===\n");
        
        List<Event> upcomingEvents = eventManager.searchByDateRange(today, weekFromNow);
        upcomingEvents.sort(Comparator.comparing(Event::getStart));
        
        if (upcomingEvents.isEmpty()) {
            System.out.println("No upcoming events.");
        } else {
            LocalDate currentDate = null;
            for (Event event : upcomingEvents) {
                LocalDate eventDate = event.getStart().toLocalDate();
                
                if (!eventDate.equals(currentDate)) {
                    currentDate = eventDate;
                    System.out.println("\n" + eventDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd:")));
                }
                
                System.out.println("  " + event.getStart().format(timeFormat) + " - " + event.getTitle());
            }
        }
        System.out.println();
    }
}