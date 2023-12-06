/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controleur;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import modele.GestionSql;
import modele.Session;

/**
 * FXML Controller class
 *
 * @author Philippe
 */
public class FenFXML_ConfirmationInscriptionControleur implements Initializable
{
    @FXML
    TextField txtNom, txtSession, txtDate, txtNbInscrits, txtNbPlaces;
    
    Session maSession;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        txtNom.setText(MainApp.getMonClientSelectionne().getNom());
        txtDate.setText(String.valueOf(MainApp.getMaSessionSelectionnee().getDate_debut()));
        txtSession.setText(MainApp.getMaSessionSelectionnee().getLibFormation());
        txtNbInscrits.setText(String.valueOf(MainApp.getMaSessionSelectionnee().getNb_inscrits()));
        txtNbPlaces.setText(String.valueOf(MainApp.getMaSessionSelectionnee().getNb_places()));
    }
    
    @FXML
    public void handleInscription()
    {
        boolean insertOK = GestionSql.insereInscription(MainApp.getMonClientSelectionne().getId(), MainApp.getMaSessionSelectionnee().getId());
        
        if(insertOK)
        {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("INSERTION REUSSIE");
            al.setHeaderText("Une insertion et deux maj effectuées");
            al.setContentText("Tables insertion, session_formation et plan_formation mises à jour");
            al.showAndWait();
        }
        else
        {
            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("INSERTION RATEE");
            al.setHeaderText("L'insertion ne s'est pas faite.");
            al.setContentText("Il est probable que le client soit déjà inscrit.");
            al.showAndWait();
        }
 
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    public void handleAnnulation()
    {
        // récupération du stage courant pour fermeture
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
    
}
