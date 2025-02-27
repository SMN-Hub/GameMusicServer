module net.smappz.concurrent {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
                    requires org.kordamp.ikonli.javafx;
                
    opens net.smappz.concurrent to javafx.fxml;
    exports net.smappz.concurrent;
}