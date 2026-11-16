package courselignedroite.menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page de saisie des pseudos.
 * Valide les champs et transmet les noms au contrôleur de jeu.
 */
public class ControlleurSaisieJoueurs implements Initializable {

    @FXML private TextField tfJoueur1;
    @FXML private TextField tfJoueur2;
    @FXML private Label     errJoueur1;
    @FXML private Label     errJoueur2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Efface les erreurs dès que l'utilisateur tape
        tfJoueur1.textProperty().addListener((obs, ancien, nouveau) -> cacherErreur(errJoueur1));
        tfJoueur2.textProperty().addListener((obs, ancien, nouveau) -> cacherErreur(errJoueur2));
    }

    // -------------------------------------------------------
    // Bouton JOUER
    // -------------------------------------------------------
    @FXML
    private void handleJouer(ActionEvent event) {
        boolean valide = true;
        String nom1 = tfJoueur1.getText().trim();
        String nom2 = tfJoueur2.getText().trim();

        // Validation joueur 1
        if (nom1.isEmpty()) {
            afficherErreur(errJoueur1, "Le pseudo du joueur 1 est obligatoire.");
            valide = false;
        } else if (nom1.length() > 20) {
            afficherErreur(errJoueur1, "20 caractères maximum.");
            valide = false;
        }

        // Validation joueur 2
        if (nom2.isEmpty()) {
            afficherErreur(errJoueur2, "Le pseudo du joueur 2 est obligatoire.");
            valide = false;
        } else if (nom2.length() > 20) {
            afficherErreur(errJoueur2, "20 caractères maximum.");
            valide = false;
        }

        // Pseudos identiques
        if (!nom1.isEmpty() && nom1.equalsIgnoreCase(nom2)) {
            afficherErreur(errJoueur2, "Les pseudos doivent être différents.");
            valide = false;
        }

        if (!valide) return;

        // Navigation vers le jeu avec les pseudos
        Main.activerJeu(nom1, nom2);
    }

    // -------------------------------------------------------
    // Bouton RETOUR
    // -------------------------------------------------------
    @FXML
    private void handleRetour(ActionEvent event) {
        Main.activerMenu();
    }

    // -------------------------------------------------------
    // Erreurs
    // -------------------------------------------------------
    private void afficherErreur(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void cacherErreur(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
