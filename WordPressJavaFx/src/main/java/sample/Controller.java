package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;

public class Controller {

    public Controller(){
        BlogPost blogPost=new BlogPost();
    }

    @FXML
    private TextField contentField;

    @FXML
    private TextField headerField;

    @FXML
    private TextField userNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    public void handleSubmitButtonAction(ActionEvent actionEvent) {

    }


}

