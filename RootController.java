import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RootController implements Initializable {

    @FXML
    private BorderPane root;
    @FXML
    private BorderPane serverPane;
    @FXML
    private BorderPane clientPane;
    @FXML
    private Button serverChoice;
    @FXML
    private Button clientChoice;
    @FXML
    private Button sendButton;
    @FXML
    private ListView<String> serverList;
    @FXML
    private ListView<String> messageList;
    @FXML
    private ListView<String> userList;
    @FXML
    private TextField textField;



    private HashMap<String, Scene> sceneMap;
    private VBox clientBox;
    private Scene startScene;
    Server serverConnection;
    Client clientConnection;
    private TextField c1;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void ServerMethod(ActionEvent e) throws IOException {
        //get instance of the loader class
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/GuiServer.fxml"));
        Parent serverPane = loader.load(); //load view into parent

        serverPane.getStylesheets().add("CSS/GuiServer.css");//set style

        //create a new BorderPane to use as the root node
        BorderPane newRoot = new BorderPane();
        newRoot.setCenter(serverPane);

        //create a new scene with the new root node
        Scene scene = new Scene(newRoot, 500, 400);

        //set the scene of the stage to the new scene
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setScene(scene);

        //set the preferred size of the root node
        newRoot.setPrefSize(200, 200);


        serverList = (ListView<String>) loader.getNamespace().get("serverList");

        serverConnection = new Server(data -> {
            Platform.runLater(() -> {
                    serverList.getItems().add(data.toString());
            });
        });
    }

    public void ClientMethod(ActionEvent e) throws IOException {
        //get instance of the loader class
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/Client.fxml"));
        Parent serverPane = loader.load(); //load view into parent

        serverPane.getStylesheets().add("CSS/Client.css");//set style

        //create a new BorderPane to use as the root node
        BorderPane newRoot = new BorderPane();
        newRoot.setCenter(serverPane);

        //create a new scene with the new root node
        Scene scene = new Scene(newRoot, 400, 300);

        //set the scene of the stage to the new scene
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setScene(scene);

        //set the preferred size of the root node
        newRoot.setPrefSize(400, 300);

        this.messageList = (ListView<String>) loader.getNamespace().get("messageList");
        this.userList = (ListView<String>) loader.getNamespace().get("userList");
        this.sendButton = (Button) loader.getNamespace().get("sendButton");
        this.textField = (TextField) loader.getNamespace().get("textField");


        this.clientConnection = new Client(data -> {
            Platform.runLater(() -> {
                String message = data.toString();
                if(message.contains("Online")){
                    this.userList.getItems().clear();
                    this.userList.getItems().add(message);
                } else if(message.contains("#")|| message.contains("from")){
                    this.messageList.getItems().add(message);
                }
            });
        });
        this.clientConnection.start();
        sendButton.setOnAction(e1->{
            if (this.clientConnection != null) {
                this.clientConnection.send(this.textField.getText());
                this.textField.clear();
            }
        });
    }

}