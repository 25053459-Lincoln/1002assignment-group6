package calenderapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Calendar App");
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Title","Start","End"}, 0);
            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane);

            // Add a dummy event
            EventManager manager = new EventManager();
            manager.createEvent("Demo Event", "Just testing", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
            manager.getEvents().forEach(e -> model.addRow(new Object[]{e.getEventId(), e.getTitle(), e.getStart(), e.getEnd()}));

            frame.setVisible(true);
        });
    }
}
