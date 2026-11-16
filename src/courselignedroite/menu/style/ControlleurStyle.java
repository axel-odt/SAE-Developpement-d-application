package courselignedroite.menu.style;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;

import courselignedroite.menu.Main;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page Style.
 * Gère la sélection des préférences visuelles (pions, couleurs, fonds).
 *
 * Les valeurs sont stockées en attributs STATIQUES pour être lues par ControlleurJeu
 *
 * 4 catégories de style :
 *   - forme des pions     : "rond" | "carre" | "coeur"
 *   - couleur des pions   : "bleuRouge" | "orangeVert" | "jauneViolet"
 *   - fond de l'interface : "bois" | "nuit" | "clair"
 *   - fond du plateau     : "classique" | "pierre" | "vert"
 */
public class ControlleurStyle implements Initializable {

    // Forme des pions
    @FXML private ToggleButton tbPionRond;
    @FXML private ToggleButton tbPionCarre;
    @FXML private ToggleButton tbPionCoeur;

    // Couleur des pions
    @FXML private ToggleButton tbBleuRouge;
    @FXML private ToggleButton tbOrangeVert;
    @FXML private ToggleButton tbJauneViollet; 

    // Fond de l'application
    @FXML private ToggleButton tbFondBois;
    @FXML private ToggleButton tbFondNuit;
    @FXML private ToggleButton tbFondClair;

    // Fond du plateau
    @FXML private ToggleButton tbPlateauClassique;
    @FXML private ToggleButton tbPlateauPierre;
    @FXML private ToggleButton tbPlateauVert;

    // Valeurs statique
    private static String formeActuelle   = "rond";
    private static String couleurActuelle = "bleuRouge";
    private static String fondActuel      = "bois";
    private static String plateauActuel   = "classique";

    // Initialisation 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remet les boutons dans l'état mémorisé (si l'utilisateur revient sur la page)
        appliquerSelection();
    }

    // Handlers forme des pions 
    @FXML 
    private void handlePionRond(ActionEvent e)  { 
    	selectionnerForme("rond");  
    }
    @FXML 
    private void handlePionCarre(ActionEvent e) { 
    	selectionnerForme("carre"); 
    }
    @FXML 
    private void handlePionCoeur(ActionEvent e) { 
    	selectionnerForme("coeur"); 
    }

    // Handlers couleur des pions 
    @FXML 
    private void handleBleuRouge(ActionEvent e)   { 
    	selectionnerCouleur("bleuRouge");   
    }
    @FXML 
    private void handleOrangeVert(ActionEvent e)  { 
    	selectionnerCouleur("orangeVert");  
    }
    @FXML 
    private void handleJauneViolet(ActionEvent e) { 
    	selectionnerCouleur("jauneViolet"); 
    }

    // Handlers fond de l'application 
    @FXML 
    private void handleFondBois(ActionEvent e)  { 
    	selectionnerFond("bois");  
    }
    @FXML 
    private void handleFondNuit(ActionEvent e)  { 
    	selectionnerFond("nuit");  
    }
    @FXML 
    private void handleFondClair(ActionEvent e) { 
    	selectionnerFond("clair"); 
    }

    // Handlers fond du plateau 
    @FXML 
    private void handlePlateauClassique(ActionEvent e) { 
    	selectionnerPlateau("classique"); 
    }
    @FXML 
    private void handlePlateauPierre(ActionEvent e)    { 
    	selectionnerPlateau("pierre");    
    }
    @FXML 
    private void handlePlateauVert(ActionEvent e)      { 
    	selectionnerPlateau("vert");      
    }

    // Bouton RETOUR 
    @FXML
    private void handleRetour(ActionEvent event) {
        Main.activerMenu();
    }

    // Getters pour les valeurs sélectionnées
    public static String getFormeActuelle()   { 
    	return formeActuelle;   
    }
    
    public static String getCouleurActuelle() { 
    	return couleurActuelle; 
    }
    
    public static String getFondActuel()      { 
    	return fondActuel;      
    }
    
    public static String getPlateauActuel()   { 
    	return plateauActuel;   
    }

    // Méthodes privées 

    private void selectionnerForme(String valeur) {
        formeActuelle = valeur;
        tbPionRond.setSelected(valeur.equals("rond"));
        tbPionCarre.setSelected(valeur.equals("carre"));
        tbPionCoeur.setSelected(valeur.equals("coeur"));
    }

    private void selectionnerCouleur(String valeur) {
        couleurActuelle = valeur;
        tbBleuRouge.setSelected(valeur.equals("bleuRouge"));
        tbOrangeVert.setSelected(valeur.equals("orangeVert"));
        tbJauneViollet.setSelected(valeur.equals("jauneViolet"));
    }

    private void selectionnerFond(String valeur) {
        fondActuel = valeur;
        tbFondBois.setSelected(valeur.equals("bois"));
        tbFondNuit.setSelected(valeur.equals("nuit"));
        tbFondClair.setSelected(valeur.equals("clair"));
    }

    private void selectionnerPlateau(String valeur) {
        plateauActuel = valeur;
        tbPlateauClassique.setSelected(valeur.equals("classique"));
        tbPlateauPierre.setSelected(valeur.equals("pierre"));
        tbPlateauVert.setSelected(valeur.equals("vert"));
    }

    /** Remet les ToggleButtons dans l'état mémorisé. */
    private void appliquerSelection() {
        selectionnerForme(formeActuelle);
        selectionnerCouleur(couleurActuelle);
        selectionnerFond(fondActuel);
        selectionnerPlateau(plateauActuel);
    }
}