package courselignedroite.jeu;

import java.io.*;
import java.util.Random;

/**
 *
 * Positions logiques :
 *   0        = réserve
 *   1..12    = sur le plateau (progression vers 12)
 *   13       = arrivé (hors jeu, victoire partielle)
 *
 * Colonne visuelle sur le plateau (col 1..12) :
 *   Rouge : colonne = position logique          (avance gauche→droite)
 *   Bleu  : colonne = 13 - position logique     (avance droite→gauche)
 *
 * CAPTURE :
 *   Un pion rouge en position R et un pion bleu en position B sont
 *   face à face quand ils occupent la MÊME COLONNE VISUELLE :
 *     col_rouge = R   et   col_bleu = 13 - B   → R == 13 - B → B == 13 - R
 *   Donc : capture quand positionBleu == 13 - positionRouge
 *
 * Sauvegarde :
 *   La partie est sérialisée dans un fichier (CSV).
 */
public class PartieJeu implements Serializable {

    public static final int NB_CASES = 12;
    public static final int NB_PIONS = 3;
    public static final int RESERVE  = 0;
    public static final int ARRIVE   = 13;

    private final String[] pseudos   = new String[2];
    private final int[][]  positions = new int[2][3];

    private int     joueurActif;
    private int     dernierDe;
    private boolean dejaLance;
    private boolean partieTerminee;
    private int     gagnant;

    private final Random random = new Random();

    // Constructeur
    public PartieJeu(String pseudo1, String pseudo2) {
        boolean p1Rouge  = random.nextBoolean();
        pseudos[0] = p1Rouge ? pseudo1 : pseudo2;
        pseudos[1] = p1Rouge ? pseudo2 : pseudo1;

        for (int j = 0; j < 2; j++)
            for (int p = 0; p < NB_PIONS; p++)
                positions[j][p] = RESERVE;

        joueurActif    = 0;
        dernierDe      = 0;
        dejaLance      = false;
        partieTerminee = false;
        gagnant        = -1;
    }

    // Lancer le dé 
    public int lancerDe() {
        if (dejaLance || partieTerminee) return dernierDe;
        dernierDe = random.nextInt(6) + 1;
        dejaLance = true;
        return dernierDe;
    }

    // Déplacer un pion
    /**
     * Déplace le pion du joueur actif.
     * @return "ok", "depasse", "capture", "victoire"
     */
    public String deplacerPion(int indexPion) {
        if (!dejaLance)     throw new IllegalStateException("Lancez le dé d'abord.");
        if (partieTerminee) return "ok";
        if (indexPion < 0 || indexPion >= NB_PIONS)
            throw new IllegalArgumentException("Index invalide : " + indexPion);
        if (positions[joueurActif][indexPion] == ARRIVE)
            throw new IllegalArgumentException("Ce pion est déjà arrivé.");

        int pos = positions[joueurActif][indexPion];
        if (pos == RESERVE) pos = 0;

        int nouvelle = pos + dernierDe;
        String resultat;

        if (nouvelle >= NB_CASES) {
            if (nouvelle == NB_CASES) {
                // Arrivée exacte
                positions[joueurActif][indexPion] = ARRIVE;
                // Capture possible même à l'arrivée
                boolean capture = appliquerCapture(nouvelle);
                resultat = aGagne(joueurActif) ? "victoire"
                         : capture ? "capture" : "ok";
            } else {
                // Dépassement → réserve
                positions[joueurActif][indexPion] = RESERVE;
                resultat = "depasse";
            }
        } else {
            positions[joueurActif][indexPion] = nouvelle;
            boolean capture = appliquerCapture(nouvelle);
            resultat = capture ? "capture" : "ok";
        }

        if ("victoire".equals(resultat)) {
            partieTerminee = true;
            gagnant        = joueurActif;
        } else {
            joueurActif = 1 - joueurActif;
            dejaLance   = false;
        }

        return resultat;
    }

    /**
     * Applique la règle de capture.
     *
     * Un pion rouge en position R et un pion bleu en position B sont
     * face à face (même colonne visuelle) quand :
     *   colonne_rouge = R  et  colonne_bleu = 13 - B  → R = 13 - B → B = 13 - R
     *
     * Si le joueur actif est rouge et vient d'aller en position :
     *   on cherche les pions bleus en position (13 - pos).
     * Si le joueur actif est bleu et vient d'aller en position  :
     *   on cherche les pions rouges en position (13 - pos).
     *
     * Les pions en RESERVE (0) ou ARRIVE (13) ne peuvent pas être capturés.
     *
     * @param posActif position logique où vient d'arriver le pion actif
     * @return true si au moins un pion adverse a été capturé
     */
    private boolean appliquerCapture(int posActif) {
        // La case adverse "face à face"
        int posAdverse = NB_CASES + 1 - posActif;  // = 13 - posActif

        // Pas de capture si la position face à face sort des limites
        if (posAdverse <= RESERVE || posAdverse >= ARRIVE) return false;

        int adversaire = 1 - joueurActif;
        boolean capture = false;
        for (int p = 0; p < NB_PIONS; p++) {
            if (positions[adversaire][p] == posAdverse) {
                positions[adversaire][p] = RESERVE;
                capture = true;
            }
        }
        return capture;
    }

    private boolean aGagne(int j) {
        for (int p = 0; p < NB_PIONS; p++)
            if (positions[j][p] != ARRIVE) return false;
        return true;
    }

    /**
     * Sauvegarde la partie dans un fichier texte.
     * Format CSV : une valeur par ligne.
     */
    public void sauvegarder(String cheminFichier) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(cheminFichier))) {
            pw.println(pseudos[0]);
            pw.println(pseudos[1]);
            for (int j = 0; j < 2; j++)
                for (int p = 0; p < NB_PIONS; p++)
                    pw.println(positions[j][p]);
            pw.println(joueurActif);
            pw.println(dernierDe);
            pw.println(dejaLance);
            pw.println(partieTerminee);
            pw.println(gagnant);
        }
    }

    /**
     * Charge une partie depuis un fichier texte.
     * @return une PartieJeu restaurée, ou null si le fichier est invalide
     */
    public static PartieJeu charger(String cheminFichier) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            // Créer une instance sans attribution aléatoire
            PartieJeu p = new PartieJeu("_", "_");
            p.pseudos[0]      = br.readLine();
            p.pseudos[1]      = br.readLine();
            for (int j = 0; j < 2; j++)
                for (int k = 0; k < NB_PIONS; k++)
                    p.positions[j][k] = Integer.parseInt(br.readLine().trim());
            p.joueurActif    = Integer.parseInt(br.readLine().trim());
            p.dernierDe      = Integer.parseInt(br.readLine().trim());
            p.dejaLance      = Boolean.parseBoolean(br.readLine().trim());
            p.partieTerminee = Boolean.parseBoolean(br.readLine().trim());
            p.gagnant        = Integer.parseInt(br.readLine().trim());
            return p;
        }
    }

    // Getters
    public String  getPseudo(int j) { 
    	return pseudos[j];         
    }
    public int     getPosition(int j, int p) { 
    	return positions[j][p];    
    }
    public int     getJoueurActif() { 
    	return joueurActif;        
    }
    public int     getDernierDe() { 
    	return dernierDe;          
    }
    public boolean isDejaLance() { 
    	return dejaLance;          
    }
    public boolean isPartieTerminee() { 
    	return partieTerminee;     
    }
    public int     getGagnant() { 
    	return gagnant;            
    }

    public boolean pionJouable(int j, int p) {
        return positions[j][p] != ARRIVE;
    }
    public boolean pionEnReserve(int j, int p) {
        return positions[j][p] == RESERVE;
    }
    public int nbPionsEnReserve(int j) {
        int c = 0;
        for (int p = 0; p < NB_PIONS; p++) if (positions[j][p] == RESERVE) c++;
        return c;
    }

    // Utilisé uniquement par charger() les pseudos sont finaux donc on
    // déclare un constructeur privé sans attribution aléatoire
    private PartieJeu() {
        joueurActif = 0; dernierDe = 0; dejaLance = false;
        partieTerminee = false; gagnant = -1;
    }
}
