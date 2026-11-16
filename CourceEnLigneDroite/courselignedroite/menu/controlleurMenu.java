package courselignedroite.menu;

import courselignedroite.jeu.PartieJeu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur du menu principal.
 */
public class controlleurMenu implements Initializable {

    private static final String SAUVEGARDE =
            System.getProperty("user.home") + File.separator + "sauvegarde_course.dat";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verifierSauvegarde();
    }

    private void verifierSauvegarde() {
        if (!new File(SAUVEGARDE).exists()) return;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Partie sauvegardée");
        a.setHeaderText("Une partie sauvegardée a été trouvée.");
        a.setContentText("Voulez-vous la reprendre ?");
        ButtonType reprendre = new ButtonType("Reprendre");
        ButtonType nouvelle  = new ButtonType("Nouvelle partie");
        ButtonType supprimer = new ButtonType("Supprimer la sauvegarde");
        a.getButtonTypes().setAll(reprendre, nouvelle, supprimer);

        a.showAndWait().ifPresent(r -> {
            if (r == reprendre) {
                lancerSauvegarde();
            } else if (r == supprimer) {
                new File(SAUVEGARDE).delete();
                info("Sauvegarde supprimée", "La partie sauvegardée a été supprimée.");
            }
            // nouvelle partie → on reste sur le menu
        });
    }

    @FXML 
    private void handleJouer(ActionEvent e)   { 
    	Main.activerSaisie(); 
    }
    @FXML 
    private void handleStyle(ActionEvent e)   {
    	Main.activerStyle();  
    }

    @FXML
    private void handleCharger(ActionEvent e) {
        if (!new File(SAUVEGARDE).exists()) {
            info("Aucune sauvegarde", "Aucune partie sauvegardée n'a été trouvée.");
            return;
        }
        lancerSauvegarde();
    }

    private void lancerSauvegarde() {
        try {
            PartieJeu p = PartieJeu.charger(SAUVEGARDE);
            Main.activerJeuSauvegarde(p);
        } catch (IOException ex) {
            erreur("Erreur de chargement",
                    "Impossible de charger la sauvegarde.\n" + ex.getMessage());
        }
    }

    @FXML
    private void handleRegles(ActionEvent e) {
        String contenu = lireFichier("regle.txt");
        if (contenu == null) { erreur("Erreur", "Impossible de lire les règles."); return; }
        TextArea ta = new TextArea(contenu);
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefSize(600, 400);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Règles du jeu");
        a.setHeaderText("La Course en Ligne Droite");
        a.getDialogPane().setContent(ta);
        a.getDialogPane().setPrefSize(640, 480);
        a.showAndWait();
    }

    @FXML
    private void handleQuitter(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Quitter"); a.setHeaderText(null);
        a.setContentText("Voulez-vous vraiment quitter le jeu ?");
        ButtonType oui = new ButtonType("Oui, quitter");
        ButtonType non = new ButtonType("Non, rester");
        a.getButtonTypes().setAll(oui, non);
        if (a.showAndWait().filter(r -> r == oui).isPresent())
            javafx.application.Platform.exit();
    }

    private void info(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void erreur(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private String lireFichier(String nom) {
        try (InputStream is = getClass().getResourceAsStream(nom)) {
            if (is == null) return null;
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String l; while ((l = br.readLine()) != null) sb.append(l).append("\n");
            return sb.toString();
        } catch (IOException ex) { return null; }
    }
}