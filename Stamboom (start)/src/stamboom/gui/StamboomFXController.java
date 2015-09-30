/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.gui;

import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import stamboom.controller.StamboomController;
import stamboom.domain.Administratie;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;
import stamboom.util.StringUtilities;

/**
 *
 * @author frankpeeters
 */
public class StamboomFXController extends StamboomController implements Initializable {

    //MENUs en TABs
    @FXML MenuBar menuBar;
    @FXML MenuItem miNew;
    @FXML MenuItem miOpen;
    @FXML MenuItem miSave;
    @FXML CheckMenuItem cmDatabase;
    @FXML MenuItem miClose;
    @FXML Tab tabPersoon;
    @FXML Tab tabGezin;
    @FXML Tab tabPersoonInvoer;
    @FXML Tab tabGezinInvoer;

    //PERSOON
    @FXML ComboBox cbPersonen;
    @FXML TextField tfPersoonNr;
    @FXML TextField tfVoornamen;
    @FXML TextField tfTussenvoegsel;
    @FXML TextField tfAchternaam;
    @FXML TextField tfGeslacht;
    @FXML TextField tfGebDatum;
    @FXML TextField tfGebPlaats;
    @FXML ComboBox cbOuderlijkGezin;
    @FXML ListView lvAlsOuderBetrokkenBij;
    @FXML Button btStamboom;

    //INVOER GEZIN
    @FXML ComboBox cbOuder1Invoer;
    @FXML ComboBox cbOuder2Invoer;
    @FXML TextField tfHuwelijkInvoer;
    @FXML TextField tfScheidingInvoer;
    @FXML Button btOKGezinInvoer;
    @FXML Button btCancelGezinInvoer;
    
    // NIEUW PERSOON
    @FXML TextField tfNieuwPersVoornamen;
    @FXML TextField tfNieuwPersTsv;
    @FXML TextField tfNieuwPersAnaam;
    @FXML TextField tfNieuwPersGebDat;
    @FXML TextField tfNieuwPersGebPl;
    @FXML ComboBox cbNieuwPersGeslacht;
    @FXML ComboBox cbNieuwPersOudGez;
    @FXML Button btnOkNieuwPersoon;
    @FXML Button btnCancelNieuwPersoon;
    
    // GEZIN
    @FXML ComboBox cbKiesGezin;
    @FXML TextField tfGezinNr;
    @FXML TextField tfSelOuder1;
    @FXML TextField tfSelOuder2;
    @FXML TextField tfGezHuwelijk;
    @FXML TextField tfGezScheding;
    @FXML ListView lvGezKinderen;
    
    //opgave 4
    private boolean withDatabase;
    
    private Administratie admin;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        admin = new Administratie();
        initComboboxes();
        withDatabase = false;
    }

    private void initComboboxes() {
        //todo opgave 3 
        cbPersonen.setItems(admin.getPersonen());
        cbKiesGezin.setItems(admin.getObGezinnen());
    }

    public void selectPersoon(Event evt) {
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        showPersoon(persoon);
    }

    private void showPersoon(Persoon persoon) {
        if (persoon == null) {
            clearTabPersoon();
        } else {
            tfPersoonNr.setText(persoon.getNr() + "");
            tfVoornamen.setText(persoon.getVoornamen());
            tfTussenvoegsel.setText(persoon.getTussenvoegsel());
            tfAchternaam.setText(persoon.getAchternaam());
            tfGeslacht.setText(persoon.getGeslacht().toString());
            tfGebDatum.setText(StringUtilities.datumString(persoon.getGebDat()));
            tfGebPlaats.setText(persoon.getGebPlaats());
            if (persoon.getOuderlijkGezin() != null) {
                cbOuderlijkGezin.getSelectionModel().select(persoon.getOuderlijkGezin());
            } else {
                cbOuderlijkGezin.getSelectionModel().clearSelection();
            }

            //todo opgave 3
            //
            lvAlsOuderBetrokkenBij.setItems(persoon.getObAlsOuderBetrokkenIn());
        }
    }

    public void setOuders(Event evt) {
        if (tfPersoonNr.getText().isEmpty()) {
            return;
        }
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        if (ouderlijkGezin == null) {
            return;
        }

        int nr = Integer.parseInt(tfPersoonNr.getText());
        Persoon p = getAdministratie().getPersoon(nr);
        if(getAdministratie().setOuders(p, ouderlijkGezin)){
            showDialog("Success", ouderlijkGezin.toString()
                + " is nu het ouderlijk gezin van " + p.getNaam());
        }
        
    }

    public void selectGezin(Event evt) {
        // todo opgave 3

    }

    private void showGezin(Gezin gezin) {
        // todo opgave 3

    }

    public void setHuwdatum(Event evt) {
        // todo opgave 3

    }

    public void setScheidingsdatum(Event evt) {
        // todo opgave 3

    }

    public void cancelPersoonInvoer(Event evt) {
        // todo opgave 3
        // frank
        clearTabPersoonInvoer();
    }

    public void okPersoonInvoer(Event evt) {
        // todo opgave 3
        // frank
        String tussenVoegsel = "";
        if(!tfNieuwPersVoornamen.getText().equals("")){
            String[] vnamen = tfNieuwPersVoornamen.getText().split(" ");           
            if(!tfNieuwPersTsv.getText().equals("")){
                tussenVoegsel = tfNieuwPersTsv.getText();        
            }
            if(!tfNieuwPersAnaam.getText().equals("")){
                if(!tfNieuwPersGebDat.getText().equals("")){
                    Calendar calendar = Calendar.getInstance();
                    String[] gebdat = tfNieuwPersGebDat.getText().split("-");
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(gebdat[0]));
                    calendar.set(Calendar.MONTH, Integer.parseInt(gebdat[1]));
                    calendar.set(Calendar.YEAR, Integer.parseInt(gebdat[2]));
                    if(!tfNieuwPersGebPl.getText().equals("")){
                        Geslacht geslacht = (Geslacht) Geslacht.valueOf(cbNieuwPersGeslacht.getSelectionModel().getSelectedItem().toString());
                        Gezin ouderlijkGezin = (Gezin) cbNieuwPersOudGez.getSelectionModel().getSelectedItem();
                        if(geslacht != null){          
                            Persoon p = admin.addPersoon(geslacht, vnamen, tfNieuwPersAnaam.getText(), tussenVoegsel, calendar, tfNieuwPersGebPl.getText(), ouderlijkGezin);
                            if(p != null){
                                System.out.println("Persoon succesvol aangemaakt");
                            }                           
                        }
                        else{
                            showDialog("Warning", "geen geslacht ingevoerd");
                        }                          
                    }
                    else{
                        showDialog("Warning", "geen geboorteplaats ingevoerd");
                    }

                }
                else{
                    showDialog("Warning", "geen geboortedatum ingevoerd");
                }
            }
            else{
                showDialog("Warning", "Geen Achternaam Ingevoerd");
            }            
        }
        else{
            showDialog("Warning", "Geen Voornamen Ingegeven");
        }
        
        clearTabPersoonInvoer();
        initComboboxes();
        
    }

    public void okGezinInvoer(Event evt) {
        Persoon ouder1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        if (ouder1 == null) {
            showDialog("Warning", "eerste ouder is niet ingevoerd");
            return;
        }
        Persoon ouder2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar huwdatum;
        try {
            huwdatum = StringUtilities.datum(tfHuwelijkInvoer.getText());
        } catch (IllegalArgumentException exc) {
            showDialog("Warning", "huwelijksdatum :" + exc.getMessage());
            return;
        }
        Gezin g;
        if (huwdatum != null) {
            g = getAdministratie().addHuwelijk(ouder1, ouder2, huwdatum);
            if (g == null) {
                showDialog("Warning", "Invoer huwelijk is niet geaccepteerd");
            } else {
                Calendar scheidingsdatum;
                try {
                    scheidingsdatum = StringUtilities.datum(tfScheidingInvoer.getText());
                    if(scheidingsdatum != null){
                        getAdministratie().setScheiding(g, scheidingsdatum);
                    }
                } catch (IllegalArgumentException exc) {
                    showDialog("Warning", "scheidingsdatum :" + exc.getMessage());
                }
            }
        } else {
            g = getAdministratie().addOngehuwdGezin(ouder1, ouder2);
            if (g == null) {
                showDialog("Warning", "Invoer ongehuwd gezin is niet geaccepteerd");
            }
        }

        clearTabGezinInvoer();
    }

    public void cancelGezinInvoer(Event evt) {
        clearTabGezinInvoer();
    }

    
    public void showStamboom(Event evt) {
        // todo opgave 3
    }

    public void createEmptyStamboom(Event evt) {
        this.clearAdministratie();
        clearTabs();
        initComboboxes();
    }

    
    public void openStamboom(Event evt) {
        // todo opgave 3
       
    }

    
    public void saveStamboom(Event evt) {
        // todo opgave 3
       
    }

    
    public void closeApplication(Event evt) {
        saveStamboom(evt);
        getStage().close();
    }

   
    public void configureStorage(Event evt) {
        withDatabase = cmDatabase.isSelected();
    }

 
    public void selectTab(Event evt) {
        Object source = evt.getSource();
        if (source == tabPersoon) {
            clearTabPersoon();
        } else if (source == tabGezin) {
            clearTabGezin();
        } else if (source == tabPersoonInvoer) {
            clearTabPersoonInvoer();
        } else if (source == tabGezinInvoer) {
            clearTabGezinInvoer();
        }
    }

    private void clearTabs() {
        clearTabPersoon();
        clearTabPersoonInvoer();
        clearTabGezin();
        clearTabGezinInvoer();
    }

    
    private void clearTabPersoonInvoer() {
        //todo opgave 3
        // frank
        tfNieuwPersVoornamen.clear();
        tfNieuwPersTsv.clear();
        tfNieuwPersAnaam.clear();
        tfNieuwPersGebDat.clear();
        tfNieuwPersGebPl.clear();
        cbNieuwPersGeslacht.getSelectionModel().clearSelection();
        cbNieuwPersOudGez.getSelectionModel().clearSelection();
    }

    
    private void clearTabGezinInvoer() {
        //todo opgave 3
        // frank
        cbOuder1Invoer.getSelectionModel().clearSelection();
        cbOuder2Invoer.getSelectionModel().clearSelection();
        tfHuwelijkInvoer.clear();
        tfScheidingInvoer.clear();
    }

    private void clearTabPersoon() {
        cbPersonen.getSelectionModel().clearSelection();
        tfPersoonNr.clear();
        tfVoornamen.clear();
        tfTussenvoegsel.clear();
        tfAchternaam.clear();
        tfGeslacht.clear();
        tfGebDatum.clear();
        tfGebPlaats.clear();
        cbOuderlijkGezin.getSelectionModel().clearSelection();
        lvAlsOuderBetrokkenBij.setItems(FXCollections.emptyObservableList());
    }

    
    private void clearTabGezin() {
        // todo opgave 3
       
    }

    private void showDialog(String type, String message) {
        Stage myDialog = new Dialog(getStage(), type, message);
        myDialog.show();
    }

    private Stage getStage() {
        return (Stage) menuBar.getScene().getWindow();
    }

}
