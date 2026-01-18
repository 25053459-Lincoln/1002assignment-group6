package calenderapp;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String FILE_PATH = "data/events.csv";
    private static final String DELIMITER = "||"; // Use unlikely delimiter to avoid conflicts

    // Read all events
    public static List<Event> readEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(FILE_PATH);
        
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Created new events file: " + FILE_PATH);
            } catch (IOException e) {
                System.err.println("Failed to create events file: " + e.getMessage());
            }
            return events;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;
                
                try {
    String[] parts = line.split("\\|\\|", -1); // -1 keeps trailing empty strings
    if (parts.length != 10) {
        System.err.println("Skipping malformed line " + lineNum + ": expected 10 fields, got " + parts.length);
        System.err.println("Line content: " + line); // Debug output
        continue;
    }

                    int id = Integer.parseInt(parts[0].trim());
                    String title = parts[1];
                    String desc = parts[2];
                    LocalDateTime start = LocalDateTime.parse(parts[3]);
                    LocalDateTime end = LocalDateTime.parse(parts[4]);
                    boolean recurring = Boolean.parseBoolean(parts[5]);
                    String recurrenceType = parts[6];
                    int recurrenceCount = Integer.parseInt(parts[7].trim());
                    int seriesId = Integer.parseInt(parts[8].trim());
                    int reminderMinutes = Integer.parseInt(parts[9].trim());

                    Event e = new Event(id, title, desc, start, end);
                    e.setRecurring(recurring);
                    e.setRecurrenceType(recurrenceType);
                    e.setRecurrenceCount(recurrenceCount);
                    e.setSeriesId(seriesId);
                    e.setReminderMinutes(reminderMinutes);
                    events.add(e);
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading events file: " + e.getMessage());
        }
        
        return events;
    }

    // Append one event
    public static void saveEvent(Event e) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(formatEvent(e));
            bw.newLine();
        } catch (IOException ex) {
            System.err.println("Error saving event: " + ex.getMessage());
        }
    }

    // Save all events (overwrite)
    public static void saveEvents(List<Event> events) {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Event e : events) {
                bw.write(formatEvent(e));
                bw.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Error saving events: " + ex.getMessage());
        }
    }

    // Format event with safe delimiter
    private static String formatEvent(Event e) {
        return e.getEventId() + DELIMITER +
               e.getTitle() + DELIMITER +
               e.getDescription() + DELIMITER +
               e.getStart() + DELIMITER +
               e.getEnd() + DELIMITER +
               e.isRecurring() + DELIMITER +
               e.getRecurrenceType() + DELIMITER +
               e.getRecurrenceCount() + DELIMITER +
               e.getSeriesId() + DELIMITER +
               e.getReminderMinutes();
    }
}