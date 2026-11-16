module courselignedroite {
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.controls;
	requires org.junit.jupiter.api;
	
	opens courselignedroite.menu to javafx.fxml, javafx.graphics;
	opens courselignedroite.menu.style to javafx.fxml;
	opens courselignedroite.jeu to javafx.fxml;
	opens courselignedroite.test to org.junit.jupiter.api;
}
