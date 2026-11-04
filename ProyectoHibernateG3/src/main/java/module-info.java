module org.example.proyectohibernateg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;


    opens org.example.proyectohibernateg3 to javafx.fxml;
    exports org.example.proyectohibernateg3;
}