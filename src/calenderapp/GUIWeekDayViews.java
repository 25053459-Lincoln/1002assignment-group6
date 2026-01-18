package calenderapp;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

/**
 * GUI-based Week and Day views for the Calendar App
 */
public class GUIWeekDayViews {
    
    private EventManager eventManager;
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    public GUIWeekDayViews(EventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Display Day View in a new window
     */
    public void showDayView(LocalDate date, Component parent) {
        JFrame dayFrame = new JFrame("Day View - " + date.format(dateFormat));
        dayFrame.setSize(600, 700);
        dayFrame.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with date and navigation
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel dateLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), 
                                      SwingConstants.CENTER);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        JPanel navPanel = new JPanel();
        JButton prevBtn = new JButton("◄ Previous Day");
        JButton todayBtn = new JButton("Today");
        JButton nextBtn = new JButton("Next Day ►");
        
        navPanel.add(prevBtn);
        navPanel.add(todayBtn);
        navPanel.add(nextBtn);
        
        headerPanel.add(dateLabel, BorderLayout.NORTH);
        headerPanel.add(navPanel, BorderLayout.SOUTH);
        
        // Time slots panel
        JPanel timePanel = createDayTimePanel(date);
        JScrollPane scrollPane = new JScrollPane(timePanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Navigation button actions
        prevBtn.addActionListener(e -> {
            dayFrame.dispose();
            showDayView(date.minusDays(1), parent);
        });
        
        todayBtn.addActionListener(e -> {
            dayFrame.dispose();
            showDayView(LocalDate.now(), parent);
        });
        
        nextBtn.addActionListener(e -> {
            dayFrame.dispose();
            showDayView(date.plusDays(1), parent);
        });
        
        dayFrame.add(mainPanel);
        dayFrame.setVisible(true);
    }
    
    /**
     * Create the time slot panel for day view
     */
    private JPanel createDayTimePanel(LocalDate date) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        List<Event> dayEvents = new ArrayList<>(eventManager.getEventsForDate(date));
        dayEvents.sort(Comparator.comparing(Event::getStart));
        
        // Create time slots from 6 AM to 11 PM
        for (int hour = 6; hour < 23; hour++) {
            JPanel hourPanel = new JPanel(new BorderLayout());
            hourPanel.setPreferredSize(new Dimension(550, 60));
            hourPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            hourPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
            hourPanel.setBackground(Color.WHITE);
            
            // Time label
            JLabel timeLabel = new JLabel(String.format("%02d:00", hour));
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            timeLabel.setPreferredSize(new Dimension(50, 60));
            timeLabel.setVerticalAlignment(SwingConstants.TOP);
            timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
            
            // Events panel
            JPanel eventsPanel = new JPanel();
            eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
            eventsPanel.setBackground(Color.WHITE);
            
            // Find events in this hour
            for (Event event : dayEvents) {
                int eventHour = event.getStart().getHour();
                if (eventHour == hour) {
                    JPanel eventCard = createEventCard(event);
                    eventsPanel.add(eventCard);
                }
            }
            
            hourPanel.add(timeLabel, BorderLayout.WEST);
            hourPanel.add(eventsPanel, BorderLayout.CENTER);
            panel.add(hourPanel);
        }
        
        if (dayEvents.isEmpty()) {
            JLabel noEventsLabel = new JLabel("No events scheduled for this day", SwingConstants.CENTER);
            noEventsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noEventsLabel.setForeground(Color.GRAY);
            noEventsLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
            panel.add(noEventsLabel);
        }
        
        return panel;
    }
    
    /**
     * Display Week View in a new window
     */
    public void showWeekView(LocalDate startDate, Component parent) {
        // Adjust to start on Sunday
        LocalDate sunday = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate saturday = sunday.plusDays(6);
        
        JFrame weekFrame = new JFrame("Week View - " + sunday.format(dateFormat) + 
                                     " to " + saturday.format(dateFormat));
        weekFrame.setSize(1200, 700);
        weekFrame.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header with week info and navigation
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel weekLabel = new JLabel("Week of " + sunday.format(dateFormat), SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JPanel navPanel = new JPanel();
        JButton prevBtn = new JButton("◄ Previous Week");
        JButton thisWeekBtn = new JButton("This Week");
        JButton nextBtn = new JButton("Next Week ►");
        
        navPanel.add(prevBtn);
        navPanel.add(thisWeekBtn);
        navPanel.add(nextBtn);
        
        headerPanel.add(weekLabel, BorderLayout.NORTH);
        headerPanel.add(navPanel, BorderLayout.SOUTH);
        
        // Week grid panel
        JPanel weekPanel = createWeekGridPanel(sunday);
        JScrollPane scrollPane = new JScrollPane(weekPanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Navigation actions
        prevBtn.addActionListener(e -> {
            weekFrame.dispose();
            showWeekView(sunday.minusWeeks(1), parent);
        });
        
        thisWeekBtn.addActionListener(e -> {
            weekFrame.dispose();
            showWeekView(LocalDate.now(), parent);
        });
        
        nextBtn.addActionListener(e -> {
            weekFrame.dispose();
            showWeekView(sunday.plusWeeks(1), parent);
        });
        
        weekFrame.add(mainPanel);
        weekFrame.setVisible(true);
    }
    
    /**
     * Create the week grid panel
     */
    private JPanel createWeekGridPanel(LocalDate sunday) {
        JPanel panel = new JPanel(new GridLayout(1, 7, 5, 5));
        panel.setBackground(Color.WHITE);
        
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            JPanel dayColumn = createWeekDayColumn(date, date.equals(today));
            panel.add(dayColumn);
        }
        
        return panel;
    }
    
    /**
     * Create a single day column for week view
     */
    private JPanel createWeekDayColumn(LocalDate date, boolean isToday) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        if (isToday) {
            column.setBackground(new Color(255, 255, 200));
        } else {
            column.setBackground(Color.WHITE);
        }
        
        // Day header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        
        JLabel dayNameLabel = new JLabel(
            date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()),
            SwingConstants.CENTER
        );
        dayNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dayNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel dayNumLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayNumLabel.setFont(new Font("Arial", Font.BOLD, 24));
        dayNumLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(dayNameLabel);
        headerPanel.add(dayNumLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        
        column.add(headerPanel);
        
        // Events for this day
        List<Event> dayEvents = new ArrayList<>(eventManager.getEventsForDate(date));
        dayEvents.sort(Comparator.comparing(Event::getStart));
        
        if (dayEvents.isEmpty()) {
            JLabel noEvents = new JLabel("No events", SwingConstants.CENTER);
            noEvents.setFont(new Font("Arial", Font.ITALIC, 12));
            noEvents.setForeground(Color.GRAY);
            noEvents.setAlignmentX(Component.CENTER_ALIGNMENT);
            column.add(noEvents);
        } else {
            for (Event event : dayEvents) {
                JPanel eventCard = createCompactEventCard(event);
                column.add(eventCard);
                column.add(Box.createVerticalStrut(5));
            }
        }
        
        return column;
    }
    
    /**
     * Create an event card with full details
     */
    private JPanel createEventCard(Event event) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 255), 2),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        card.setBackground(new Color(230, 240, 255));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        JLabel titleLabel = new JLabel(event.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        
        String timeStr = event.getStart().format(timeFormat) + " - " + event.getEnd().format(timeFormat);
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        card.add(titleLabel);
        card.add(timeLabel);
        
        if (!event.getDescription().isEmpty()) {
            JLabel descLabel = new JLabel("<html>" + event.getDescription().substring(
                0, Math.min(event.getDescription().length(), 50)) + "...</html>");
            descLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            descLabel.setForeground(Color.DARK_GRAY);
            card.add(descLabel);
        }
        
        return card;
    }
    
    /**
     * Create a compact event card for week view
     */
    private JPanel createCompactEventCard(Event event) {
        JPanel card = new JPanel(new BorderLayout(5, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 255), 1),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        card.setBackground(new Color(230, 240, 255));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel timeLabel = new JLabel(event.getStart().format(timeFormat));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 11));
        
        JLabel titleLabel = new JLabel(event.getTitle());
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        card.add(timeLabel, BorderLayout.WEST);
        card.add(titleLabel, BorderLayout.CENTER);
        
        if (event.isRecurring()) {
            JLabel recurLabel = new JLabel("↻");
            recurLabel.setFont(new Font("Arial", Font.BOLD, 12));
            recurLabel.setToolTipText("Recurring event");
            card.add(recurLabel, BorderLayout.EAST);
        }
        
        return card;
    }
}