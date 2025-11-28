module ru.rsreu.thirdeye {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires opencv;
    requires javafx.swing;
    requires java.net.http;

    opens ru.rsreu.thirdeye to javafx.fxml;
    exports ru.rsreu.thirdeye;
}