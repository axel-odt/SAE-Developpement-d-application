package courselignedroite.menu;

import courselignedroite.jeu.ControlleurJeu;
import courselignedroite.jeu.PartieJeu;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principale
 */
public class Main extends Application {

    private static Stage         fenetre;
    private static Scene         sceneMenu;
    private static Scene         sceneSaisie;
    private static Scene         sceneStyle;
    private static Scene         sceneJeu;
    private static ControlleurJeu ctrlJeu;

    // Navigation 
    public static void activerMenu()   { 
    	fenetre.setScene(sceneMenu);   
    }
    public static void activerSaisie() { 
    	fenetre.setScene(sceneSaisie); 
    }
    public static void activerStyle()  { 
    	fenetre.setScene(sceneStyle);  
    }

    /** Nouvelle partie avec les deux pseudos. */
    public static void activerJeu(String p1, String p2) {
        ctrlJeu.initialiserPartie(p1, p2);
        fenetre.setScene(sceneJeu);
    }

    /** Reprendre une partie sauvegardée. */
    public static void activerJeuSauvegarde(PartieJeu p) {
        ctrlJeu.chargerPartie(p);
        fenetre.setScene(sceneJeu);
    }

    // Démarrage 
    @Override
    public void start(Stage stage) throws Exception {
        fenetre = stage;

        sceneMenu   = charger("MenuPrincipal.fxml");
        sceneSaisie = charger("SaisieJoueurs.fxml");
        sceneStyle  = charger("style/Style.fxml");

        FXMLLoader loaderJeu = new FXMLLoader(
                getClass().getResource("/courselignedroite/jeu/Jeu.fxml"));
        sceneJeu = new Scene(loaderJeu.load());
        ctrlJeu  = loaderJeu.getController();

        stage.setTitle("La Course en Ligne Droite");
        stage.setResizable(false);
        stage.setScene(sceneMenu);
        stage.show();
    }

    private Scene charger(String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        return new Scene(root);
    }

    public static void main(String[] args) { launch(args); }
}
