package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;

import java.net.URL;

public class Controller {
    private String username;
    private String password;
    private URL xmlRpcUrl;


    public Controller(){
        // To initialise get username, password and xmRpcUrl form the respective fields using proper getter.
        //BlogPost blogPost = new BlogPost(username,password,xmlRpcUrl);

    }

    @FXML
    private TextArea contentField;

    @FXML
    private TextField headerField;

    @FXML
    private TextField userNameField;


    @FXML
    private TextField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    public void getThePosts(ActionEvent actionEvent) {

    }

    @FXML
    public void login(ActionEvent actionEvent) {
    }

    @FXML
    public void submitPosts(ActionEvent actionEvent) {
    }
}

