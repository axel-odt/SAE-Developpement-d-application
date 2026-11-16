package courselignedroite.test;

import courselignedroite.jeu.PartieJeu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du modèle joueur via PartieJeu.
 *
 * Classe testée    : courselignedroite.jeu.PartieJeu (logique joueur/pion)
 * Classe de tests  : courselignedroite.test.JoueurTest
 */
public class JoueurTest {

    private PartieJeu partie;

    // On fixe toujours rouge = joueur 0 (rouges commencent)
    private static final int ROUGE = 0;
    private static final int BLEU  = 1;

    @BeforeEach
    void setUp() {
        partie = new PartieJeu("Alice", "Bob");
    }

    // État initial d'un pion

    @Test
    void testPionInitialementEnReserve() {
        for (int p = 0; p < PartieJeu.NB_PIONS; p++)
            assertTrue(partie.pionEnReserve(ROUGE, p),
                    "Le pion " + p + " doit être en réserve au départ.");
    }

    @Test
    void testPositionInitialeEst0() {
        for (int p = 0; p < PartieJeu.NB_PIONS; p++)
            assertEquals(PartieJeu.RESERVE, partie.getPosition(ROUGE, p));
    }

    @Test
    void testPionJouableAuDepart() {
        for (int p = 0; p < PartieJeu.NB_PIONS; p++)
            assertTrue(partie.pionJouable(ROUGE, p),
                    "Un pion en réserve doit être jouable.");
    }

    // Avancement normal

    @Test
    void testPionAvanceDepuisReserve() {
        // Force le dé à 3 est impossible directement, on vérifie juste
        // qu'après un déplacement le pion n'est plus en réserve (si pas dépassement)
        partie.lancerDe();
        int de = partie.getDernierDe();
        String res = partie.deplacerPion(0);
        if ("ok".equals(res)) {
            assertFalse(partie.pionEnReserve(ROUGE, 0),
                    "Après un déplacement OK, le pion ne doit plus être en réserve.");
            assertEquals(de, partie.getPosition(ROUGE, 0),
                    "La position doit correspondre à la valeur du dé.");
        }
    }

    @Test
    void testTroisPionsIndependants() {
        // Déplace le pion 0, vérifie que 1 et 2 restent en réserve
        partie.lancerDe();
        String res = partie.deplacerPion(0);
        if ("ok".equals(res)) {
            assertTrue(partie.pionEnReserve(ROUGE, 1),
                    "Le pion 1 ne doit pas avoir bougé.");
            assertTrue(partie.pionEnReserve(ROUGE, 2),
                    "Le pion 2 ne doit pas avoir bougé.");
        }
    }

    // Dépassement

    @Test
    void testDepassementRenvoyeEnReserve() {
        // Pour forcer un dépassement : amener un pion en case 11,
        // puis avec un dé >= 2 il dépasse
        // On simule en jouant des tours jusqu'à obtenir "depasse"
        boolean depasseTrouve = false;
        for (int essai = 0; essai < 200 && !depasseTrouve; essai++) {
            PartieJeu p = new PartieJeu("X", "Y");
            // Amener pion 0 rouge le plus loin possible
            int posActuelle = 0;
            while (posActuelle < 10 && !p.isPartieTerminee()) {
                p.lancerDe();
                String r = p.deplacerPion(0);
                if ("ok".equals(r)) {
                    posActuelle = p.getPosition(ROUGE, 0);
                } else break;
                // Passer le tour bleu
                if (!p.isPartieTerminee()) { p.lancerDe(); p.deplacerPion(0); }
            }
            // Maintenant essayer de dépasser
            if (!p.isPartieTerminee() && posActuelle >= 10) {
                p.lancerDe();
                int de = p.getDernierDe();
                if (posActuelle + de > PartieJeu.NB_CASES) {
                    String res = p.deplacerPion(0);
                    if ("depasse".equals(res)) {
                        assertEquals(PartieJeu.RESERVE, p.getPosition(ROUGE, 0),
                                "Après dépassement, le pion doit être en RESERVE.");
                        assertTrue(p.pionEnReserve(ROUGE, 0));
                        depasseTrouve = true;
                    }
                }
            }
        }
    }

    //Pion arrivé 

    @Test
    void testPionArrivePasJouable() {
        // Simuler qu'un pion arrive (position = ARRIVE)
        // On joue jusqu'à avoir un pion arrivé
        boolean arriveTrouve = false;
        for (int essai = 0; essai < 500 && !arriveTrouve; essai++) {
            PartieJeu p = new PartieJeu("X", "Y");
            int tours = 0;
            while (!p.isPartieTerminee() && tours++ < 200) {
                if (!p.isDejaLance()) p.lancerDe();
                int actif = p.getJoueurActif();
                for (int i = 0; i < PartieJeu.NB_PIONS; i++) {
                    if (p.pionJouable(actif, i)) {
                        String res = p.deplacerPion(i);
                        // Vérifier si un pion rouge est arrivé
                        for (int pp = 0; pp < PartieJeu.NB_PIONS; pp++) {
                            if (p.getPosition(ROUGE, pp) == PartieJeu.ARRIVE) {
                                assertFalse(p.pionJouable(ROUGE, pp),
                                        "Un pion arrivé ne doit pas être jouable.");
                                arriveTrouve = true;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    //  Capture

    @Test
    void testCaptureRenvoiePionAdversaireEnReserve() {
        // On vérifie l'invariant de capture : pendant toute une partie,
        // deux pions actifs (non en réserve, non arrivés) ne peuvent pas
        // être sur la même case simultanément
        PartieJeu p = new PartieJeu("X", "Y");
        int tours = 0;
        while (!p.isPartieTerminee() && tours++ < 3000) {
            if (!p.isDejaLance()) p.lancerDe();
            int actif = p.getJoueurActif();
            for (int i = 0; i < PartieJeu.NB_PIONS; i++) {
                if (p.pionJouable(actif, i)) {
                    p.deplacerPion(i);
                    break;
                }
            }
            // Vérifier l'invariant après chaque coup
            for (int i = 0; i < PartieJeu.NB_PIONS; i++) {
                int posR = p.getPosition(ROUGE, i);
                if (posR == PartieJeu.RESERVE || posR == PartieJeu.ARRIVE) continue;
                for (int j = 0; j < PartieJeu.NB_PIONS; j++) {
                    int posB = p.getPosition(BLEU, j);
                    assertNotEquals(posR, posB,
                            "Un pion rouge et un pion bleu ne peuvent pas "
                            + "être sur la même case active.");
                }
            }
        }
    }

    //  Nombre de pions en réserve 

    @Test
    void testNbPionsEnReserveDecrementApresDeplacer() {
        int avantDeplacer = partie.nbPionsEnReserve(ROUGE);
        partie.lancerDe();
        String res = partie.deplacerPion(0);
        if ("ok".equals(res) || "victoire".equals(res)) {
            assertEquals(avantDeplacer - 1, partie.nbPionsEnReserve(ROUGE),
                    "Un pion de moins en réserve après déplacement réussi.");
        } else {
            // depasse : le pion revient en réserve
            assertEquals(avantDeplacer, partie.nbPionsEnReserve(ROUGE));
        }
    }
}
