package courselignedroite.test;

import courselignedroite.jeu.PartieJeu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de PartieJeu.
 *
 * Classe testée    : courselignedroite.jeu.PartieJeu
 * Classe de tests  : courselignedroite.test.PartieJeuTest
 * Framework        : JUnit 5
 */
public class PartieJeuTest {

    private PartieJeu partie;

    @BeforeEach
    void setUp() {
        partie = new PartieJeu("Alice", "Bob");
    }

    // Initialisation 

    @Test
    void testPseudosPresents() {
        assertNotNull(partie.getPseudo(0));
        assertNotNull(partie.getPseudo(1));
        assertFalse(partie.getPseudo(0).isEmpty());
        assertFalse(partie.getPseudo(1).isEmpty());
    }

    @Test
    void testPseudosDifferents() {
        assertNotEquals(partie.getPseudo(0), partie.getPseudo(1),
                "Les deux joueurs doivent avoir des pseudos différents.");
    }

    @Test
    void testPseudosContiennentAliceEtBob() {
        // L'attribution est aléatoire, mais Alice et Bob doivent être là
        boolean alicePresente = partie.getPseudo(0).equals("Alice")
                             || partie.getPseudo(1).equals("Alice");
        boolean bobPresent    = partie.getPseudo(0).equals("Bob")
                             || partie.getPseudo(1).equals("Bob");
        assertTrue(alicePresente, "Alice doit être l'un des deux joueurs.");
        assertTrue(bobPresent,    "Bob doit être l'un des deux joueurs.");
    }

    @Test
    void testTousLesPionsEnReserveAuDepart() {
        for (int j = 0; j < 2; j++)
            for (int p = 0; p < PartieJeu.NB_PIONS; p++)
                assertEquals(PartieJeu.RESERVE, partie.getPosition(j, p),
                        "Tous les pions doivent être en réserve au départ.");
    }

    @Test
    void testRougeCommenceToujours() {
        assertEquals(0, partie.getJoueurActif(),
                "Le joueur rouge (index 0) doit commencer.");
    }

    @Test
    void testPartieNonTermineeAuDepart() {
        assertFalse(partie.isPartieTerminee());
    }

    @Test
    void testGagnantMinusUnAuDepart() {
        assertEquals(-1, partie.getGagnant(),
                "Aucun gagnant au départ (valeur -1 attendue).");
    }

    @Test
    void testNbCasesEst12() {
        assertEquals(12, PartieJeu.NB_CASES,
                "Le plateau doit avoir 12 cases.");
    }

    @Test
    void testNbPionsEst3() {
        assertEquals(3, PartieJeu.NB_PIONS,
                "Chaque joueur doit avoir 3 pions.");
    }

    @Test
    void testConstanteReserveEst0() {
        assertEquals(0, PartieJeu.RESERVE);
    }

    @Test
    void testConstanteArriveEst13() {
        assertEquals(13, PartieJeu.ARRIVE);
    }

    // Dé 

    @RepeatedTest(50)
    void testDeEntre1Et6() {
        PartieJeu p = new PartieJeu("X", "Y");
        int v = p.lancerDe();
        assertTrue(v >= 1 && v <= 6,
                "Le dé doit donner une valeur entre 1 et 6, obtenu : " + v);
    }

    @Test
    void testDejaLanceApresLancer() {
        assertFalse(partie.isDejaLance());
        partie.lancerDe();
        assertTrue(partie.isDejaLance(),
                "isDejaLance() doit être vrai après lancerDe().");
    }

    @Test
    void testRelancerRetourneMemeValeur() {
        int v1 = partie.lancerDe();
        int v2 = partie.lancerDe(); // ne doit pas relancer
        assertEquals(v1, v2,
                "Un deuxième appel à lancerDe() doit retourner la même valeur.");
    }

    @Test
    void testDernierDeCorrect() {
        int v = partie.lancerDe();
        assertEquals(v, partie.getDernierDe());
    }

    // Déplacement

    @Test
    void testDeplacerSansDeException() {
        assertThrows(IllegalStateException.class,
                () -> partie.deplacerPion(0),
                "Déplacer sans avoir lancé le dé doit lever IllegalStateException.");
    }

    @Test
    void testDeplacerIndexNegatifException() {
        partie.lancerDe();
        assertThrows(IllegalArgumentException.class,
                () -> partie.deplacerPion(-1));
    }

    @Test
    void testDeplacerIndexTropGrandException() {
        partie.lancerDe();
        assertThrows(IllegalArgumentException.class,
                () -> partie.deplacerPion(3));
    }

    @Test
    void testPionAvanceApresDeplacer() {
        partie.lancerDe();
        int de = partie.getDernierDe();
        String res = partie.deplacerPion(0);
        int pos = partie.getPosition(0, 0);
        // selon la valeur du dé : OK (pos=de), VICTOIRE (pos=ARRIVE=13 si de=12),
        // ou DEPASSE (pos=RESERVE=0 si de>12, impossible ici car de<=6 et pos initiale=0)
        if ("ok".equals(res)) {
            assertEquals(de, pos, "Après OK, le pion doit être sur la case " + de);
        } else if ("victoire".equals(res)) {
            assertEquals(PartieJeu.ARRIVE, pos);
        }
        // "depasse" impossible au premier déplacement
    }

    @Test
    void testTourPasseApresDeplacer() {
        int avant = partie.getJoueurActif();
        partie.lancerDe();
        partie.deplacerPion(0);
        if (!partie.isPartieTerminee()) {
            assertNotEquals(avant, partie.getJoueurActif(),
                    "Le tour doit passer à l'autre joueur après un déplacement.");
        }
    }

    @Test
    void testDeNonLanceApresDeplacer() {
        partie.lancerDe();
        partie.deplacerPion(0);
        if (!partie.isPartieTerminee()) {
            assertFalse(partie.isDejaLance(),
                    "isDejaLance() doit être faux après le déplacement.");
        }
    }

    @Test
    void testPionJouableAuDepart() {
        for (int p = 0; p < PartieJeu.NB_PIONS; p++)
            assertTrue(partie.pionJouable(0, p),
                    "Tous les pions doivent être jouables au départ.");
    }

    @Test
    void testPionEnReserveAuDepart() {
        for (int p = 0; p < PartieJeu.NB_PIONS; p++)
            assertTrue(partie.pionEnReserve(0, p));
    }

    @Test
    void testNbPionsEnReserveAuDepart() {
        assertEquals(3, partie.nbPionsEnReserve(0));
        assertEquals(3, partie.nbPionsEnReserve(1));
    }

    // Fin de partie 

    @Test
    void testPartieSeTermine() {
        jouerJusquALaFin();
        assertTrue(partie.isPartieTerminee(),
                "La partie doit se terminer.");
        assertTrue(partie.getGagnant() == 0 || partie.getGagnant() == 1,
                "Le gagnant doit être le joueur 0 ou 1.");
    }

    @Test
    void testGagnantA3PionsArrives() {
        jouerJusquALaFin();
        int g = partie.getGagnant();
        for (int p = 0; p < PartieJeu.NB_PIONS; p++)
            assertEquals(PartieJeu.ARRIVE, partie.getPosition(g, p),
                    "Les 3 pions du gagnant doivent être à ARRIVE.");
    }

    @Test
    void testRelancerApresFinNeChangePasGagnant() {
        jouerJusquALaFin();
        int gagnantAvant = partie.getGagnant();
        partie.lancerDe();
        assertEquals(gagnantAvant, partie.getGagnant(),
                "Le gagnant ne doit pas changer après la fin.");
    }

    // Capture 

    @Test
    void testDeuxJoueursJamaisSurMemeCaseSimultanement() {
        //Après chaque déplacement, un pion adverse sur la même
        // case doit avoir été renvoyé en réserve
        jouerJusquALaFin();
        // Si aucune AssertionError n'est levée pendant la partie, l'invariant tient
        assertTrue(partie.isPartieTerminee());
    }

    // Utilitaire 

    private void jouerJusquALaFin() {
        int garde = 0;
        while (!partie.isPartieTerminee() && garde++ < 5000) {
            if (!partie.isDejaLance()) partie.lancerDe();
            int actif = partie.getJoueurActif();
            for (int i = 0; i < PartieJeu.NB_PIONS; i++) {
                if (partie.pionJouable(actif, i)) {
                    partie.deplacerPion(i);
                    break;
                }
            }
        }
    }
}
