package vallegrande.edu.pe.view;

import vallegrande.edu.pe.controller.ContactController;
import vallegrande.edu.pe.model.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContactView extends JFrame {

    private final ContactController controller;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // 🎨 Colores lilas-rosados
    private final Color primaryColor = new Color(186, 104, 200); // Lila
    private final Color hoverColor = new Color(171, 71, 188);    // Lila oscuro
    private final Color accentColor = new Color(255, 182, 193);  // Rosa pastel

    public ContactView(ContactController controller) {
        this.controller = controller;
        initUI();
        loadContacts();
        showWelcomeMessage();
    }

    private void initUI() {
        setTitle("📒 Agenda de Contactos");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 🌈 Panel con degradado
        JPanel gradientPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(248, 194, 255),  // Rosa claro arriba
                        getWidth(), getHeight(), new Color(186, 104, 200) // Lila abajo
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(gradientPanel);

        // 🔍 Barra de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false); // Transparente para mostrar el degradado

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setToolTipText("🔍 Buscar contacto...");

        searchPanel.add(searchField, BorderLayout.CENTER);
        gradientPanel.add(searchPanel, BorderLayout.NORTH);

        // 📋 Tabla con estilo
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Teléfono", "Email"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionBackground(primaryColor);
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color evenColor = new Color(245, 230, 255); // Lila muy claro
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? evenColor : Color.WHITE);
                }
                return c;
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        gradientPanel.add(scrollPane, BorderLayout.CENTER);

        // 🔍 Búsqueda en vivo
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateFilter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
        });

        // 🎛 Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        JButton addButton = createStyledButton("Agregar", primaryColor, hoverColor);
        JButton deleteButton = createStyledButton("Eliminar", accentColor, new Color(255, 105, 97));

        addButton.addActionListener(e -> addContact());
        deleteButton.addActionListener(e -> deleteContact());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        gradientPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 🔄 Bordes redondeados
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // ✨ Hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });

        return button;
    }

    private void loadContacts() {
        tableModel.setRowCount(0);
        for (Contact c : controller.findAll()) {
            tableModel.addRow(new Object[]{c.id(), c.name(), c.phone(), c.email()});
        }
    }

    private void addContact() {
        String name = JOptionPane.showInputDialog(this, "Nombre:");
        if (name == null || name.isBlank()) return;

        String phone = JOptionPane.showInputDialog(this, "Teléfono:");
        if (phone == null || phone.isBlank()) return;

        String email = JOptionPane.showInputDialog(this, "Email:");
        if (email == null || email.isBlank()) return;

        controller.create(new Contact(null, name, phone, email));
        loadContacts();
        showToast("✅ Contacto agregado con éxito");
    }

    private void deleteContact() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showToast("⚠ Selecciona un contacto para eliminar");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        controller.delete(id);
        loadContacts();
        showToast("🗑 Contacto eliminado");
    }

    private void showToast(String message) {
        JDialog toast = new JDialog(this);
        toast.setUndecorated(true);
        toast.setLayout(new BorderLayout());

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(50, 50, 50, 220));
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setBorder(new EmptyBorder(10, 20, 10, 20));

        toast.add(label);
        toast.pack();
        toast.setLocationRelativeTo(this);

        new Timer(1500, e -> toast.dispose()).start();
        toast.setVisible(true);
    }

    private void showWelcomeMessage() {
        showToast("💜 Bienvenido a tu Agenda de Contactos");
    }
}
