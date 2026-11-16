package courselignedroite.jeu;

import courselignedroite.menu.Main;
import courselignedroite.menu.style.ControlleurStyle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur de la scène de jeu.
 *
 * Plateau :
 *   Ligne 0 (haut)  → BLEU  (arrivée colonne 1,  réserve à droite)
 *   Ligne 1 (bas)   → ROUGE (arrivée colonne 12, réserve à gauche)
 *
 * Tour :
 *   1. Clic "Lancer le dé" → valeur numérique affichée, pions s'illuminent
 *   2. Clic sur un pion (Button) → déplacement, tour suivant
 *
 * Style :
 *   Les préférences visuelles sont lues depuis ControlleurStyle.
 *   Elles sont appliquées à chaque rafraîchissement du plateau :
 *     - fond de l'interface : classe CSS sur le StackPane fond
 *     - fond du plateau     : classe CSS sur le GridPane
 *     - couleur des pions   : classe CSS de palette sur le StackPane racine
 *     - forme des pions     : classe CSS sur chaque Button pion
 */
public class ControlleurJeu implements Initializable {

    // Constantes joueurs 
    private static final int ROUGE = 0;
    private static final int BLEU  = 1;

    // Taille d'une case 
    private static final double CELL = 52;

    // Chemin de sauvegarde
    private static final String CHEMIN_SAUVEGARDE =
            System.getProperty("user.home") + File.separator + "sauvegarde_course.dat";

    // FXML
    @FXML private Label    lblTour;
    @FXML private Label    lblDe;
    @FXML private Label    lblMessage;
    @FXML private Button   btnLancer;
    @FXML private GridPane gridPlateau;
    @FXML private HBox     reserveRouge;
    @FXML private HBox     reserveBleu;
    @FXML private Label    lblInfoRouge;
    @FXML private Label    lblInfoBleu;
    @FXML private MenuBar  menuBar;

    /**
     * Panneau de fond de l'interface
     * On l'injecte depuis le FXML via fx:id="fondPane"
     */
    @FXML private Pane fondPane;

    /**
     * Panneau racine de la scène de jeu.
     * Sert à porter la classe de palette (.palette-bleuRouge …).
     * On l'injecte via fx:id="racineJeu".
     */
    @FXML private VBox racineJeu;

    // Modèle 
    private PartieJeu partie;

    // Initialisation JavaFX 
    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    // API publique

    /** Nouvelle partie avec les deux pseudos. */
    public void initialiserPartie(String pseudo1, String pseudo2) {
        partie = new PartieJeu(pseudo1, pseudo2);
        resetUI();
    }

    /** Reprendre une partie sauvegardée. */
    public void chargerPartie(PartieJeu p) {
        partie = p;
        resetUI();
    }

    // Réinitialisation de l'interface 
    private void resetUI() {
        btnLancer.setDisable(false);
        lblDe.setText("—");
        lblMessage.setText("Lance le dé pour commencer !");
        rafraichir();
    }

    // Bouton LANCER 
    @FXML
    private void handleLancer(ActionEvent e) {
        if (partie.isDejaLance()) return;
        int v = partie.lancerDe();
        lblDe.setText(String.valueOf(v));
        lblMessage.setText("Dé : " + v + "  —  Choisis un pion à déplacer");
        btnLancer.setDisable(true);
        rafraichir();
    }

    // Clic sur un pion 
    private void clicPion(int indexPion) {
        if (!partie.isDejaLance()) { lblMessage.setText("Lance le dé d'abord !"); return; }
        if (partie.isPartieTerminee()) return;

        String res;
        try {
            res = partie.deplacerPion(indexPion);
        } catch (IllegalArgumentException ex) {
            lblMessage.setText(ex.getMessage()); return;
        }

        rafraichir();

        switch (res) {
            case "victoire" -> {
                lblMessage.setText(partie.getPseudo(partie.getGagnant()) + " gagne la partie !");
                btnLancer.setDisable(true);
                dialogueVictoire();
            }
            case "depasse" -> {
                lblMessage.setText("Dépassement ! Le pion repart en réserve.");
                btnLancer.setDisable(false);
            }
            case "capture" -> {
                lblMessage.setText("Capture ! Les pions adverses retournent en réserve.");
                btnLancer.setDisable(false);
            }
            default -> {
                lblMessage.setText("");
                btnLancer.setDisable(false);
            }
        }
    }

    //   MENU EN JEU 

    /** Menu > Sauvegarder */
    @FXML
    private void handleSauvegarder(ActionEvent e) {
        File f = new File(CHEMIN_SAUVEGARDE);
        if (f.exists()) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Sauvegarde existante");
            a.setHeaderText("Une partie sauvegardée existe déjà.");
            a.setContentText("Voulez-vous la remplacer ?");
            ButtonType remplacer = new ButtonType("Remplacer");
            ButtonType annuler   = new ButtonType("Annuler");
            a.getButtonTypes().setAll(remplacer, annuler);
            Optional<ButtonType> rep = a.showAndWait();
            if (!rep.isPresent() || rep.get() != remplacer) return;
        }
        try {
            partie.sauvegarder(CHEMIN_SAUVEGARDE);
            afficherInfo("Sauvegarde", "La partie a été sauvegardée.");
        } catch (IOException ex) {
            afficherErreur("Erreur", "Impossible de sauvegarder : " + ex.getMessage());
        }
    }

    /** Menu > Règles */
    @FXML
    private void handleRegles(ActionEvent e) {
        String contenu = lireFichier("regle.txt");
        if (contenu == null) { afficherErreur("Erreur", "Impossible de lire les règles."); return; }
        TextArea ta = new TextArea(contenu);
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefSize(600, 400);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Règles du jeu");
        a.setHeaderText("La Course en Ligne Droite");
        a.getDialogPane().setContent(ta);
        a.getDialogPane().setPrefSize(640, 480);
        a.showAndWait();
    }

    /** Menu > Quitter */
    @FXML
    private void handleQuitter(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Quitter"); a.setHeaderText(null);
        a.setContentText("Abandonner et retourner au menu ?");
        ButtonType oui = new ButtonType("Oui"), non = new ButtonType("Non");
        a.getButtonTypes().setAll(oui, non);
        if (a.showAndWait().filter(r -> r == oui).isPresent())
            Main.activerMenu();
    }

    //  RAFRAÎCHISSEMENT
    private void rafraichir() {
        // ── Labels d'info ──
        int actif  = partie.getJoueurActif();
        String coul = actif == ROUGE ? "ROUGE" : "BLEU";
        lblTour.setText("Tour de : " + partie.getPseudo(actif) + " (" + coul + ")");
        lblInfoRouge.setText(partie.getPseudo(ROUGE)
                + "  (" + partie.nbPionsEnReserve(ROUGE) + " en réserve)");
        lblInfoBleu.setText(partie.getPseudo(BLEU)
                + "  (" + partie.nbPionsEnReserve(BLEU) + " en réserve)");

        //  Applique les styles de fond 
        appliquerStyleFond();

        // Construit le plateau 
        construirePlateau();
    }

    //   APPLICATION DES STYLES

    /**
     * Applique les classes CSS de fond sur :
     *   - fondPane  : fond de l'interface (.fond-bois / .fond-nuit / .fond-clair)
     *   - gridPlateau : fond du plateau   (.plateau-classique / .plateau-pierre / .plateau-vert)
     *   - racineJeu : palette des pions   (.palette-bleuRouge / …)
     */
    private void appliquerStyleFond() {

        // Fond de l'interface
        if (fondPane != null) {
            fondPane.getStyleClass().removeIf(c -> c.startsWith("fond-"));
            fondPane.getStyleClass().add("fond-" + ControlleurStyle.getFondActuel());
        }

        // Fond du plateau
        gridPlateau.getStyleClass().removeIf(c -> c.startsWith("plateau-"));
        gridPlateau.getStyleClass().add("plateau-" + ControlleurStyle.getPlateauActuel());

        // Palette de couleurs (portée par la racine pour que les règles CSS enfant s'appliquent)
        if (racineJeu != null) {
            racineJeu.getStyleClass().removeIf(c -> c.startsWith("palette-"));
            racineJeu.getStyleClass().add("palette-" + ControlleurStyle.getCouleurActuelle());
        }
    }

    //  CONSTRUCTION DU PLATEAU 
    /**
     * Plateau : 14 colonnes × 3 lignes
     *   col 0      = étiquette réserve rouge
     *   col 1..12  = cases de jeu
     *   col 13     = étiquette réserve bleu
     *   ligne 0    = bleu  (haut)
     *   ligne 1    = rouge (bas)
     *   ligne 2    = numéros
     */
    private void construirePlateau() {
        gridPlateau.getChildren().clear();
        gridPlateau.getColumnConstraints().clear();
        gridPlateau.getRowConstraints().clear();

        for (int c = 0; c < 14; c++) {
            ColumnConstraints cc = new ColumnConstraints(CELL);
            gridPlateau.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 3; r++) {
            RowConstraints rc = new RowConstraints(r == 2 ? 20 : CELL);
            gridPlateau.getRowConstraints().add(rc);
        }

        boolean actifEstRouge = (partie.getJoueurActif() == ROUGE);
        boolean actifEstBleu  = !actifEstRouge;
        boolean lance         = partie.isDejaLance();

        // Numéros de case (ligne 2)
        for (int c = 1; c <= 12; c++) {
            Label lbl = new Label(String.valueOf(c));
            lbl.setPrefWidth(CELL);
            lbl.setAlignment(Pos.CENTER);
            lbl.setStyle("-fx-text-fill:#C8B89A;-fx-font-size:10px;");
            add(lbl, c, 2);
        }

        // Étiquettes réserves
        ajouterLabel("Rés.\nRouge", 0, 1, "#EF9A9A");
        ajouterLabel("Rés.\nBleu",  13, 0, "#90CAF9");

        //  Ligne BLEU (haut) 
        // Position logique bleu B → colonne visuelle 13-B
        for (int col = 1; col <= 12; col++) {
            int posBleu = 13 - col;
            boolean arrivee = (col == 1);
            String bg = couleurCase(true, arrivee, col);
            StackPane sp = creerCase(bg);
            add(sp, col, 0);
            placerPions(sp, BLEU, posBleu, lance && actifEstBleu);
        }

        // Ligne ROUGE (bas) 
        for (int col = 1; col <= 12; col++) {
            boolean arrivee = (col == 12);
            String bg = couleurCase(false, arrivee, col);
            StackPane sp = creerCase(bg);
            add(sp, col, 1);
            placerPions(sp, ROUGE, col, lance && actifEstRouge);
        }

        // Réserves latérales 
        reserveRouge.getChildren().clear();
        reserveBleu.getChildren().clear();
        for (int i = 0; i < PartieJeu.NB_PIONS; i++) {
            if (partie.pionEnReserve(ROUGE, i))
                reserveRouge.getChildren().add(creerPionButton(ROUGE, i, lance && actifEstRouge));
            if (partie.pionEnReserve(BLEU, i))
                reserveBleu.getChildren().add(creerPionButton(BLEU, i, lance && actifEstBleu));
        }
    }

    /**
     * Retourne la couleur de fond d'une case en fonction du plateau sélectionné.
     *
     * @param estBleu   true si ligne bleue
     * @param arrivee   true si c'est la case d'arrivée
     * @param col       numéro de colonne (1..12) pour l'alternance
     */
    private String couleurCase(boolean estBleu, boolean arrivee, int col) {
        String plateau = ControlleurStyle.getPlateauActuel();
        switch (plateau) {
            case "pierre":
                if (arrivee)       return "#555566";
                return col % 2 == 0 ? "#3A3A4A" : "#2E2E3E";
            case "vert":
                if (arrivee)       return "#1B5E20";
                return col % 2 == 0 ? "#2E7D32" : "#1B5E20";
            default: // classique
                if (arrivee)       return "#6B3A20";
                if (estBleu)       return col % 2 == 0 ? "#2B4A7A" : "#1E3560";
                return col % 2 == 0 ? "#4A2E14" : "#3D2208";
        }
    }

    private StackPane creerCase(String bg) {
        StackPane sp = new StackPane();
        sp.setPrefSize(CELL, CELL);
        String bordure = ControlleurStyle.getPlateauActuel().equals("pierre")
                ? "#9E9E9E" : ControlleurStyle.getPlateauActuel().equals("vert")
                ? "#66BB6A" : "#D4A017";
        sp.setStyle("-fx-background-color:" + bg
                + ";-fx-border-color:" + bordure + ";-fx-border-width:0.6;");
        return sp;
    }

    /** Place les pions du joueur qui sont sur la case */
    private void placerPions(StackPane sp, int j, int caseNum, boolean cliquable) {
        if (caseNum <= PartieJeu.RESERVE || caseNum >= PartieJeu.ARRIVE) return;

        // Compter combien de pions sont sur cette case
        int total = 0;
        for (int i = 0; i < PartieJeu.NB_PIONS; i++)
            if (partie.getPosition(j, i) == caseNum) total++;

        // Espacement
        double largeurDispo = CELL - 6;
        double rayon        = total == 1 ? 13 : total == 2 ? 11 : 9;
        double espacement   = total > 1
                ? Math.min((largeurDispo - 2 * rayon) / (total - 1), 2 * rayon) : 0;
        double largeurGroupe = 2 * rayon + (total - 1) * espacement;
        double offsetDebut   = -largeurGroupe / 2 + rayon;

        int idx = 0;
        for (int i = 0; i < PartieJeu.NB_PIONS; i++) {
            if (partie.getPosition(j, i) == caseNum) {
                Button btn = creerPionButton(j, i, cliquable && partie.pionJouable(j, i));
                // Adapter la taille au nombre de pions
                btn.setMinSize(2 * rayon, 2 * rayon);
                btn.setMaxSize(2 * rayon, 2 * rayon);
                btn.setTranslateX(offsetDebut + idx * espacement);
                sp.getChildren().add(btn);
                idx++;
            }
        }
    }

    /**
     * Crée un pion sous forme de Button avec les classes CSS appropriées.
     *
     * Classes appliquées :
     *   pion-j0 ou pion-j1   → indique le joueur (0 = premier joueur, 1 = second)
     *   pion-rond/carre/coeur → forme choisie dans les paramètres de style
     *   pion-actif            → si le pion est cliquable ce tour
     */
    private Button creerPionButton(int joueur, int indexPion, boolean cliquable) {
        Button btn = new Button();

        // ── Classe joueur (j0 ou j1) ──
        // ROUGE = joueur 0 dans PartieJeu, BLEU = joueur 1
        // La palette CSS définit .pion-j0 et .pion-j1 selon les couleurs
        btn.getStyleClass().add("pion-j" + joueur);

        // ── Forme ──
        String forme = ControlleurStyle.getFormeActuelle();
        btn.getStyleClass().add("pion-" + forme);  // pion-rond / pion-carre / pion-coeur

        // ── Actif (cliquable) ──
        if (cliquable) {
            btn.getStyleClass().add("pion-actif");
            btn.setOnAction(e -> clicPion(indexPion));
        }

        // Taille par défaut (peut être écrasée dans placerPions)
        btn.setMinSize(30, 30);
        btn.setMaxSize(30, 30);

        return btn;
    }

    private void ajouterLabel(String texte, int col, int row, String couleur) {
        Label lbl = new Label(texte);
        lbl.setWrapText(true);
        lbl.setPrefWidth(CELL);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-text-fill:" + couleur + ";-fx-font-size:9px;-fx-font-weight:bold;");
        add(lbl, col, row);
    }

    private void add(javafx.scene.Node n, int col, int row) {
        GridPane.setColumnIndex(n, col);
        GridPane.setRowIndex(n, row);
        gridPlateau.getChildren().add(n);
    }

    // Dialogue victoire
    private void dialogueVictoire() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Victoire !");
        a.setHeaderText(partie.getPseudo(partie.getGagnant()) + " a gagné !");
        ButtonType rejouer = new ButtonType("Rejouer");
        ButtonType menu    = new ButtonType("Menu");
        a.getButtonTypes().setAll(rejouer, menu);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == rejouer)
            initialiserPartie(partie.getPseudo(ROUGE), partie.getPseudo(BLEU));
        else
            Main.activerMenu();
    }

    // Utilitaires
    private void afficherInfo(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void afficherErreur(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private String lireFichier(String nom) {
        try (InputStream is = getClass().getResourceAsStream(
                "/courselignedroite/menu/" + nom)) {
            if (is == null) return null;
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String ligne;
            while ((ligne = br.readLine()) != null) sb.append(ligne).append("\n");
            return sb.toString();
        } catch (IOException e) { return null; }
    }
}