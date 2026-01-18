package calenderapp;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class CalenderApp extends JFrame {

    private EventManager manager;
    private AdditionalFieldManager additionalFields;
    private JLabel monthLabel;
    private JPanel calendarPanel;
    private YearMonth currentMonth;
    private java.util.Timer reminderTimer;

    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CalenderApp() {
        this.manager = new EventManager();
        this.additionalFields = manager.getAdditionalFieldManager();
        this.currentMonth = YearMonth.now();

        setTitle("Calendar Scheduler");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeComponents();
        startReminderSystem();
        updateCalendar();
        setVisible(true);
    }

    private void initializeComponents() {
        // Top panel with navigation and controls
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Navigation panel
        JPanel navPanel = new JPanel();
        JButton prevBtn = new JButton("◄ Previous");
        JButton todayBtn = new JButton("Today");
        JButton nextBtn = new JButton("Next ►");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 22));
        
        navPanel.add(prevBtn);
        navPanel.add(todayBtn);
        navPanel.add(monthLabel);
        navPanel.add(nextBtn);
        
        // Action buttons panel
        JPanel actionPanel = new JPanel();
        JButton searchBtn = new JButton("🔍 Search");
        JButton listViewBtn = new JButton("📋 List View");
        JButton weekViewBtn = new JButton("📅 Week View");  
JButton dayViewBtn = new JButton("📆 Day View");
        JButton statsBtn = new JButton("📊 Statistics");
        JButton backupBtn = new JButton("💾 Backup");
        JButton restoreBtn = new JButton("📂 Restore");
        
        
        actionPanel.add(searchBtn);
        actionPanel.add(listViewBtn);
        actionPanel.add(statsBtn);
        actionPanel.add(weekViewBtn);  
actionPanel.add(dayViewBtn);
        actionPanel.add(backupBtn);
        actionPanel.add(restoreBtn);
        
        
        topPanel.add(navPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Calendar grid
        calendarPanel = new JPanel();
        add(new JScrollPane(calendarPanel), BorderLayout.CENTER);

        // Button actions
        prevBtn.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });
        
        todayBtn.addActionListener(e -> {
            currentMonth = YearMonth.now();
            updateCalendar();
        });

        nextBtn.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        searchBtn.addActionListener(e -> searchDialog());
        listViewBtn.addActionListener(e -> listViewDialog());
        statsBtn.addActionListener(e -> showStatistics());
        weekViewBtn.addActionListener(e -> showWeekViewGUI());  
dayViewBtn.addActionListener(e -> showDayViewGUI());
        backupBtn.addActionListener(e -> backupEvents());
        restoreBtn.addActionListener(e -> restoreEvents());
         
    }

    // Calendar grid
    private void updateCalendar() {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(0, 7, 2, 2));

        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

        // Day headers
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            lbl.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(200, 200, 200));
            calendarPanel.add(lbl);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startDay = firstOfMonth.getDayOfWeek().getValue() % 7;

        // Empty cells before first day
        for (int i = 0; i < startDay; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            calendarPanel.add(emptyPanel);
        }

        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JPanel dayPanel = createDayPanel(date, today);
            calendarPanel.add(dayPanel);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createDayPanel(LocalDate date, LocalDate today) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Highlight today
        if (date.equals(today)) {
            panel.setBackground(new Color(255, 255, 200));
        } else {
            panel.setBackground(Color.WHITE);
        }

        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(dayLabel, BorderLayout.NORTH);

        // Events for this day
        List<Event> dayEvents = manager.getEventsForDate(date);
        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
        eventsPanel.setOpaque(false);
        
        for (Event e : dayEvents) {
            JLabel eventLabel = new JLabel("• " + e.getTitle() + " " + e.getStart().format(timeFormat));
            eventLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            eventsPanel.add(eventLabel);
        }
        panel.add(eventsPanel, BorderLayout.CENTER);

        // Click handler
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showDayOptions(date);
            }
        });

        return panel;
    }

    private void showDayOptions(LocalDate date) {
        Object[] options = {"Add Event", "Manage Events", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Select an option for " + date.format(dateFormat),
            "Day Options", 
            JOptionPane.YES_NO_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        if (choice == 0) addEventDialog(date);
        else if (choice == 1) manageEventsDialog(date);
    }

    // Add Event Dialog with all features
    private void addEventDialog(LocalDate date) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        JTextField titleField = new JTextField();
        JTextArea descField = new JTextArea(3, 20);
        JTextField startField = new JTextField("09:00");
        JTextField endField = new JTextField("10:00");
        JTextField locationField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField attendeesField = new JTextField();
        JTextField reminderField = new JTextField("0");
        
        JCheckBox recurringBox = new JCheckBox("Recurring Event");
        String[] types = {"DAILY", "WEEKLY", "MONTHLY"};
        JComboBox<String> recurrenceTypeBox = new JComboBox<>(types);
        JTextField repeatCountField = new JTextField("7");
        
        recurrenceTypeBox.setEnabled(false);
        repeatCountField.setEnabled(false);
        
        recurringBox.addActionListener(e -> {
            boolean enabled = recurringBox.isSelected();
            recurrenceTypeBox.setEnabled(enabled);
            repeatCountField.setEnabled(enabled);
        });

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descField));
        panel.add(new JLabel("Start Time (HH:mm):"));
        panel.add(startField);
        panel.add(new JLabel("End Time (HH:mm):"));
        panel.add(endField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Attendees:"));
        panel.add(attendeesField);
        panel.add(new JLabel("Reminder (minutes before):"));
        panel.add(reminderField);
        panel.add(recurringBox);
        panel.add(new JPanel());
        panel.add(new JLabel("Recurrence Type:"));
        panel.add(recurrenceTypeBox);
        panel.add(new JLabel("Number of Occurrences:"));
        panel.add(repeatCountField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Add Event", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (option == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title cannot be empty!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                LocalDateTime start = parseDateTime(date, startField.getText());
                LocalDateTime end = parseDateTime(date, endField.getText());
                
                if (end.isBefore(start) || end.equals(start)) {
                    JOptionPane.showMessageDialog(this, "End time must be after start time!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (manager.hasConflict(start, end)) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "This event conflicts with another event! Continue anyway?",
                        "Conflict Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                String desc = descField.getText().trim();
                String location = locationField.getText().trim();
                String category = categoryField.getText().trim();
                String attendees = attendeesField.getText().trim();
                int reminder = Integer.parseInt(reminderField.getText().trim());
                
                if (reminder < 0) {
                    JOptionPane.showMessageDialog(this, "Reminder minutes cannot be negative!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (recurringBox.isSelected()) {
                    int count = Integer.parseInt(repeatCountField.getText().trim());
                    if (count <= 0 || count > 365) {
                        JOptionPane.showMessageDialog(this, 
                            "Occurrence count must be between 1 and 365!", 
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    Event e = new Event(manager.getNextEventId(), title, desc, start, end);
                    e.setRecurring(true);
                    e.setRecurrenceType((String) recurrenceTypeBox.getSelectedItem());
                    e.setRecurrenceCount(count);
                    e.setReminderMinutes(reminder);
                    manager.addRecurringEvent(e);
                    
                    // Save additional fields for first event in series
                    if (!location.isEmpty() || !category.isEmpty() || !attendees.isEmpty()) {
                        additionalFields.saveFields(e.getEventId(), location, category, attendees);
                    }
                } else {
                    manager.createEvent(title, desc, start, end, location, category, attendees, reminder);
                }

                updateCalendar();
                JOptionPane.showMessageDialog(this, "Event created successfully!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(), 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void manageEventsDialog(LocalDate date) {
        List<Event> dayEvents = manager.getEventsForDate(date);
        if (dayEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events on this day.", 
                "No Events", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = new String[dayEvents.size()];
        for (int i = 0; i < dayEvents.size(); i++) {
            Event e = dayEvents.get(i);
            String recurring = e.isRecurring() ? " [Recurring]" : "";
            options[i] = String.format("%d: %s (%s)%s", 
                e.getEventId(), e.getTitle(), e.getStart().format(timeFormat), recurring);
        }

        String selected = (String) JOptionPane.showInputDialog(this, 
            "Select an event:", "Manage Events",
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            
        if (selected == null) return;

        int id = Integer.parseInt(selected.split(":")[0]);
        Event event = manager.getEventById(id);
        if (event == null) return;

        showEventOptions(event);
    }

    private void showEventOptions(Event event) {
        Object[] options = {"View Details", "Update", "Delete", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Event: " + event.getTitle(),
            "Event Options",
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

        switch (choice) {
            case 0 -> viewEventDetails(event);
            case 1 -> updateEventDialog(event);
            case 2 -> deleteEventWithConfirmation(event);
        }
    }

    private void viewEventDetails(Event event) {
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(event.getTitle()).append("\n");
        details.append("Description: ").append(event.getDescription()).append("\n");
        details.append("Start: ").append(event.getStart()).append("\n");
        details.append("End: ").append(event.getEnd()).append("\n");
        details.append("Duration: ").append(event.getDurationMinutes()).append(" minutes\n");
        
        if (event.getReminderMinutes() > 0) {
            details.append("Reminder: ").append(event.getReminderMinutes()).append(" minutes before\n");
        }
        
        if (event.isRecurring()) {
            details.append("Recurring: ").append(event.getRecurrenceType()).append("\n");
            details.append("Occurrences: ").append(event.getRecurrenceCount()).append("\n");
        }
        
        AdditionalFieldManager.AdditionalFields fields = additionalFields.getFields(event.getEventId());
        if (fields != null) {
            if (!fields.location.isEmpty()) {
                details.append("Location: ").append(fields.location).append("\n");
            }
            if (!fields.category.isEmpty()) {
                details.append("Category: ").append(fields.category).append("\n");
            }
            if (!fields.attendees.isEmpty()) {
                details.append("Attendees: ").append(fields.attendees).append("\n");
            }
        }
        
        JOptionPane.showMessageDialog(this, details.toString(), 
            "Event Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateEventDialog(Event event) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        JTextField titleField = new JTextField(event.getTitle());
        JTextArea descField = new JTextArea(event.getDescription(), 3, 20);
        JTextField startField = new JTextField(String.format("%02d:%02d", 
            event.getStart().getHour(), event.getStart().getMinute()));
        JTextField endField = new JTextField(String.format("%02d:%02d", 
            event.getEnd().getHour(), event.getEnd().getMinute()));
        
        AdditionalFieldManager.AdditionalFields fields = additionalFields.getFields(event.getEventId());
        JTextField locationField = new JTextField(fields != null ? fields.location : "");
        JTextField categoryField = new JTextField(fields != null ? fields.category : "");
        JTextField attendeesField = new JTextField(fields != null ? fields.attendees : "");
        JTextField reminderField = new JTextField(String.valueOf(event.getReminderMinutes()));

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descField));
        panel.add(new JLabel("Start Time (HH:mm):"));
        panel.add(startField);
        panel.add(new JLabel("End Time (HH:mm):"));
        panel.add(endField);
        panel.add(new JLabel("Location:"));
        panel.add(locationField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Attendees:"));
        panel.add(attendeesField);
        panel.add(new JLabel("Reminder (minutes):"));
        panel.add(reminderField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Update Event", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (option == JOptionPane.OK_OPTION) {
            try {
                LocalDate date = event.getStart().toLocalDate();
                LocalDateTime start = parseDateTime(date, startField.getText());
                LocalDateTime end = parseDateTime(date, endField.getText());
                
                if (manager.hasConflictExcludingEvent(start, end, event.getEventId())) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "This update creates a conflict! Continue?",
                        "Conflict Warning", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                String title = titleField.getText().trim();
                String desc = descField.getText().trim();
                String location = locationField.getText().trim();
                String category = categoryField.getText().trim();
                String attendees = attendeesField.getText().trim();
                int reminder = Integer.parseInt(reminderField.getText().trim());

                if (event.isRecurring()) {
                    Object[] opts = {"This occurrence only", "Entire series", "Cancel"};
                    int updateChoice = JOptionPane.showOptionDialog(this, 
                        "Update this occurrence or entire series?", "Update Recurring Event",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, 
                        null, opts, opts[0]);
                    
                    if (updateChoice == 0) {
                        manager.updateEvent(event.getEventId(), title, desc, start, end, 
                            location, category, attendees, reminder);
                    } else if (updateChoice == 1) {
                        manager.updateRecurringSeries(event.getSeriesId(), title, desc, reminder);
                    } else {
                        return;
                    }
                } else {
                    manager.updateEvent(event.getEventId(), title, desc, start, end, 
                        location, category, attendees, reminder);
                }

                updateCalendar();
                JOptionPane.showMessageDialog(this, "Event updated successfully!");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                    "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteEventWithConfirmation(Event event) {
        if (event.isRecurring()) {
            Object[] options = {"This occurrence", "Entire series", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this, 
                "Delete this occurrence or entire series?", "Delete Recurring Event",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, 
                null, options, options[2]);

            if (choice == 0) {
                manager.deleteSingleOccurrence(event);
                updateCalendar();
                JOptionPane.showMessageDialog(this, "Occurrence deleted!");
            } else if (choice == 1) {
                manager.deleteRecurringEvent(event);
                updateCalendar();
                JOptionPane.showMessageDialog(this, "Series deleted!");
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this event?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                manager.deleteEvent(event.getEventId());
                updateCalendar();
                JOptionPane.showMessageDialog(this, "Event deleted!");
            }
        }
    }

    // Search functionality
    private void searchDialog() {
        Object[] options = {"Date Range", "Keyword", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Search by:", "Search Events",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, 
            null, options, options[0]);

        if (choice == 0) searchByDateRange();
        else if (choice == 1) searchByKeyword();
    }

    private void searchByDateRange() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField startField = new JTextField(LocalDate.now().format(dateFormat));
        JTextField endField = new JTextField(LocalDate.now().plusDays(7).format(dateFormat));
        
        panel.add(new JLabel("Start Date (yyyy-MM-dd):"));
        panel.add(startField);
        panel.add(new JLabel("End Date (yyyy-MM-dd):"));
        panel.add(endField);
        
        int opt = JOptionPane.showConfirmDialog(this, panel, "Search by Date Range", 
            JOptionPane.OK_CANCEL_OPTION);
            
        if (opt == JOptionPane.OK_OPTION) {
            try {
                LocalDate start = LocalDate.parse(startField.getText());
                LocalDate end = LocalDate.parse(endField.getText());
                List<Event> results = manager.searchByDateRange(start, end);
                displaySearchResults(results);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format: " + ex.getMessage(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchByKeyword() {
        String keyword = JOptionPane.showInputDialog(this, "Enter search keyword:", 
            "Search", JOptionPane.PLAIN_MESSAGE);
            
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Event> results = manager.searchByKeyword(keyword);
            displaySearchResults(results);
        }
    }

    private void displaySearchResults(List<Event> results) {
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events found.", 
                "Search Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(results.size()).append(" event(s):\n\n");
        
        for (Event e : results) {
            sb.append(String.format("%d: %s\n   %s to %s\n", 
                e.getEventId(), e.getTitle(), e.getStart(), e.getEnd()));
        }
        
        JTextArea textArea = new JTextArea(sb.toString(), 15, 40);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), 
            "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    // List view
    private void listViewDialog() {
        List<Event> events = manager.getEvents();
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No events.", 
                "List View", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Sort by start time
        events.sort(Comparator.comparing(Event::getStart));

        StringBuilder sb = new StringBuilder();
        sb.append("Total Events: ").append(events.size()).append("\n\n");
        
        for (Event e : events) {
            sb.append(String.format("%d: %s%s\n   %s to %s\n", 
                e.getEventId(), e.getTitle(), 
                e.isRecurring() ? " [Recurring]" : "",
                e.getStart(), e.getEnd()));
        }
        
        JTextArea textArea = new JTextArea(sb.toString(), 20, 50);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), 
            "All Events", JOptionPane.INFORMATION_MESSAGE);
    }

    // Statistics
    private void showStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("Total Events: ").append(manager.getTotalEvents()).append("\n");
        stats.append("Recurring Events: ").append(manager.getRecurringEventCount()).append("\n");
        stats.append("Single Events: ").append(manager.getTotalEvents() - manager.getRecurringEventCount()).append("\n");
        stats.append("Busiest Day of Week: ").append(manager.getBusiestDay()).append("\n\n");
        
        Map<String, Integer> byCategory = manager.getEventsByCategory();
        if (!byCategory.isEmpty()) {
            stats.append("Events by Category:\n");
            byCategory.forEach((cat, count) -> 
                stats.append("  ").append(cat).append(": ").append(count).append("\n"));
        }
        
        JOptionPane.showMessageDialog(this, stats.toString(), 
            "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    // Reminder system - VERIFIED WORKING
    private void startReminderSystem() {
        reminderTimer = new java.util.Timer(true);
        final Set<Integer> shownReminders = new HashSet<>();
        
        reminderTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    List<Event> currentEvents = manager.getEvents();
                    
                    System.out.println("[Reminder Check] " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                                     " - Checking " + currentEvents.size() + " events");
                    
                    for (Event e : currentEvents) {
                        if (e.getReminderMinutes() > 0 && !shownReminders.contains(e.getEventId())) {
                            LocalDateTime eventStart = e.getStart();
                            LocalDateTime reminderTime = eventStart.minusMinutes(e.getReminderMinutes());
                            
                            // Debug: Print reminder info for events with reminders
                            System.out.println("  Event: " + e.getTitle() + 
                                             " | Start: " + eventStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                                             " | Reminder at: " + reminderTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                            
                            // Check if it's time to show the reminder
                            // Show if current time is within 1 minute AFTER the reminder time
                            if (now.isAfter(reminderTime.minusSeconds(30)) && 
                                now.isBefore(eventStart) &&
                                !shownReminders.contains(e.getEventId())) {
                                
                                shownReminders.add(e.getEventId());
                                Event finalEvent = e;
                                
                                System.out.println("  ⏰ SHOWING REMINDER for: " + e.getTitle());
                                
                                SwingUtilities.invokeLater(() -> {
                                    String message = String.format(
                                        "⏰ Reminder\n\n" +
                                        "Event: %s\n" +
                                        "Starts at: %s\n" +
                                        "Time until event: %d minutes\n\n" +
                                        "%s",
                                        finalEvent.getTitle(),
                                        finalEvent.getStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                                        finalEvent.getReminderMinutes(),
                                        finalEvent.getDescription().isEmpty() ? "" : "Description: " + finalEvent.getDescription()
                                    );
                                    
                                    JOptionPane.showMessageDialog(
                                        CalenderApp.this,
                                        message,
                                        "Event Reminder - " + finalEvent.getTitle(),
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                });
                            }
                        }
                    }
                    
                    // Clean up reminders for past events
                    shownReminders.removeIf(id -> {
                        Event e = manager.getEventById(id);
                        return e == null || e.getStart().isBefore(now.minusMinutes(5));
                    });
                    
                } catch (Exception ex) {
                    System.err.println("Error in reminder system: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, 0, 20 * 1000); // Check every 20 seconds for more responsive reminders
        
        System.out.println("✓ Reminder system started - checking every 20 seconds");
    }

    // Backup/Restore
    private void backupEvents() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Backup");
        chooser.setSelectedFile(new java.io.File("calendar_backup_" + 
            LocalDate.now().format(dateFormat) + ".csv"));
            
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            manager.backupEvents(chooser.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Backup completed successfully!", 
                "Backup", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void restoreEvents() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Warning: This will replace all current events! Continue?",
            "Restore Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm != JOptionPane.YES_OPTION) return;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Backup File");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            manager.restoreEvents(chooser.getSelectedFile().getAbsolutePath());
            updateCalendar();
            JOptionPane.showMessageDialog(this, "Restore completed successfully!", 
                "Restore", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Helper methods
    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }

    @Override
    public void dispose() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(CalenderApp::new);
    }
    private void showCLIViewMenu() {
    CalenderViewCLI cliView = new CalenderViewCLI(manager);
    
    Object[] options = {"Today", "This Week", "This Month", "Month Calendar", "Cancel"};
    int choice = JOptionPane.showOptionDialog(this,
        "Select CLI View to Display in Console:",
        "CLI Views",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null, options, options[0]);
    
    switch (choice) {
        case 0 -> cliView.displayToday();
        case 1 -> cliView.displayThisWeek();
        case 2 -> cliView.displayThisMonth();
        case 3 -> cliView.displayMonthCalendarView(currentMonth);
    }
    
    if (choice >= 0 && choice <= 3) {
        JOptionPane.showMessageDialog(this,
            "CLI view displayed in console/terminal!",
            "View Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
    

private void showWeekViewGUI() {
    GUIWeekDayViews views = new GUIWeekDayViews(manager);
    views.showWeekView(LocalDate.now(), this);
}

private void showDayViewGUI() {
    GUIWeekDayViews views = new GUIWeekDayViews(manager);
    views.showDayView(LocalDate.now(), this);
}
}