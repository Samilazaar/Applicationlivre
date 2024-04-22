import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class livres extends JFrame {

    private JTable booksTable;
    private JTextField searchField;
    private JButton searchButton;
    private JButton returnButton;
    private JButton viewCartButton;
    private List<Emprunt> panier = new ArrayList<>();

    public livres() {
        setTitle("Page de Présentation des Livres");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Bienvenue dans la Bibliothèque!");
        returnButton = new JButton("Retour");
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new biblio().setVisible(true);
            }
        });

        searchField = new JTextField(20);
        searchButton = new JButton("Rechercher");

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Rechercher: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Titre");
        tableModel.addColumn("Auteur");
        tableModel.addColumn("Emprunter");

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:8889/MaBibliotheque", "root", "root")) {
            String query = "SELECT * FROM livre";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int idLivre = resultSet.getInt("id_livre");
                    String titre = resultSet.getString("titre");
                    String auteur = resultSet.getString("auteur");
                    Object[] rowData = new Object[]{idLivre, titre, auteur, "Emprunter"};
                    tableModel.addRow(rowData);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }

        booksTable = new JTable(tableModel);
        booksTable.getColumn("Emprunter").setCellRenderer(new ButtonRenderer());
        booksTable.getColumn("Emprunter").setCellEditor(new ButtonEditor(new JCheckBox()));

        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        add(returnButton, BorderLayout.WEST);
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBooks();
            }
        });

        viewCartButton = new JButton("Voir le Panier");
        viewCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCart();
            }
        });
        add(viewCartButton, BorderLayout.SOUTH);

        // Charge les emprunts existants depuis la base de données au démarrage de l'application
        loadEmpruntsFromDatabase();
    }

    private void searchBooks() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) booksTable.getModel());
        booksTable.setRowSorter(sorter);
        if (searchTerm.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchTerm));
        }
    }

    private void addToCart(int selectedRow) {
        String selectedBookId = booksTable.getModel().getValueAt(selectedRow, 0).toString();
        String selectedTitle = booksTable.getModel().getValueAt(selectedRow, 1).toString();
        String selectedAuthor = booksTable.getModel().getValueAt(selectedRow, 2).toString();
        LocalDate dueDate = LocalDate.now().plusWeeks(2); // Date limite dans deux semaines
        
        // Insérer l'emprunt dans la base de données
        insertEmpruntIntoDatabase(selectedBookId, dueDate);
        
        panier.add(new Emprunt(selectedBookId, selectedTitle, selectedAuthor, dueDate));
        
        JOptionPane.showMessageDialog(this, "Livre ajouté au panier avec succès!");
    }

    private void insertEmpruntIntoDatabase(String bookId, LocalDate dueDate) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:8889/MaBibliotheque", "root", "root")) {
            String query = "INSERT INTO emprunts (id_utilisateur, id_livre, date_emprunt, date_retour_prevue) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                // Vous devrez remplacer 'id_utilisateur' par l'ID de l'utilisateur qui emprunte le livre
                // Pour l'exemple, je mets 1 comme ID utilisateur
                preparedStatement.setInt(1, 1);
                preparedStatement.setString(2, bookId);
                preparedStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now())); // Date d'emprunt
                preparedStatement.setDate(4, java.sql.Date.valueOf(dueDate)); // Date de retour prévue
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }
    }

    private void loadEmpruntsFromDatabase() {
        panier.clear(); // Effacer le panier actuel
        
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:8889/MaBibliotheque", "root", "root")) {
            String query = "SELECT * FROM emprunts WHERE id_utilisateur = ?"; // Remplacez id_utilisateur par l'ID de l'utilisateur actuel
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                // Vous devrez remplacer 'id_utilisateur' par l'ID de l'utilisateur actuel
                // Pour l'exemple, je mets 1 comme ID utilisateur
                preparedStatement.setInt(1, 1);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String bookId = resultSet.getString("id_livre");
                    LocalDate dueDate = resultSet.getDate("date_retour_prevue").toLocalDate();
                    // Ajoutez l'emprunt à la liste panier
                    panier.add(new Emprunt(bookId, "", "", dueDate));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
        }
    }

    private void showCart() {
        StringBuilder cartContent = new StringBuilder("Panier :\n");
        for (Emprunt emprunt : panier) {
            cartContent.append(emprunt.getDueDate()).append("\n");
        }
        JOptionPane.showMessageDialog(this, cartContent.toString());
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {

        private final JButton button;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    addToCart(row);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            button.setText((value == null) ? "" : value.toString());
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // L'utilisateur a cliqué sur le bouton
            }
            isPushed = false;
            return null;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new livres().setVisible(true);
            }
        });
    }
}

class Emprunt {
    private String id;
    private String title;
    private String author;
    private LocalDate dueDate;

    public Emprunt(String id, String title, String author, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
