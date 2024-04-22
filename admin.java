import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class admin extends JFrame {

    private JTextField titleField;
    private JTextField authorField;
    private JTextField prixField;
    private JTextField descField;
    private JButton addBookButton;
    private JButton removeBookButton;
    private JButton retourButton; // Nouveau bouton Retour

    public admin() {
        setTitle("Page Admin");
        setSize(400, 300); // Augmenté la hauteur pour inclure le bouton Retour
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel adminPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Titre:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        adminPanel.add(titleLabel, gbc);

        titleField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        adminPanel.add(titleField, gbc);

        JLabel authorLabel = new JLabel("Auteur:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        adminPanel.add(authorLabel, gbc);

        authorField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        adminPanel.add(authorField, gbc);

        JLabel prixlabel = new JLabel("Prix:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        adminPanel.add(prixlabel, gbc);

        prixField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        adminPanel.add(prixField, gbc);

        JLabel descLabel = new JLabel("Description:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        adminPanel.add(descLabel, gbc);

        descField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        adminPanel.add(descField, gbc);

        addBookButton = new JButton("Ajouter Livre");
        addBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBookToDatabase();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        adminPanel.add(addBookButton, gbc);

        removeBookButton = new JButton("Retirer Livre");
        removeBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeBookFromDatabase();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        adminPanel.add(removeBookButton, gbc);

        // Ajout du bouton Retour
        retourButton = new JButton("Retour");
        retourButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Code pour revenir à la page livres.java
                dispose(); // Fermer la fenêtre actuelle
                new livres().setVisible(true); // Ouvrir la page livres.java
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        adminPanel.add(retourButton, gbc);

        add(adminPanel);
    }



    private void addBookToDatabase() {
        String title = titleField.getText();
        String author = authorField.getText();
        String price = prixField.getText();
        String description = descField.getText();

        String url = "jdbc:mysql://localhost:8889/MaBibliotheque";
        String user = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String insertQuery = "INSERT INTO livre (titre, auteur, prix, description) VALUES (?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setString(3, price);
                preparedStatement.setString(4, description);

                preparedStatement.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Livre ajouté à la base de données avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du livre à la base de données", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void removeBookFromDatabase() {
        String title = titleField.getText();
        String author = authorField.getText();

        String url = "jdbc:mysql://localhost:8889/MaBibliotheque";
        String user = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String deleteQuery = "DELETE FROM livre WHERE titre = ? AND auteur = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Livre retiré de la base de données avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Aucun livre trouvé avec le titre et l'auteur spécifiés", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du retrait du livre de la base de données", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new admin().setVisible(true);
            }
        });
    }
}
