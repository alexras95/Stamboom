/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.IOException;
import static java.lang.reflect.Array.set;
import org.sql2o.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sql2o.Sql2o;
import stamboom.domain.Administratie;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;

public class DatabaseMediator implements IStorageMediator {

    private Properties props;

    private Sql2o sql2o;

    public DatabaseMediator(Properties props) {
        this.props = props;
        try {
            initConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Administratie load() throws IOException {
        //todo opgave 4
        
        
        return null;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        //todo opgave 4
        // voordat alle personen toegevoegd worden, worden ze verwijderd
        ClearTableGezin();
        ClearTablePersoon();
        for(Persoon p : admin.getPersonen()){
            String geslacht = "";
            if(p.getGeslacht().equals(Geslacht.MAN)){
                geslacht = "MAN";
            }
            else{
                geslacht = "VROUW";
            }

            InsertPersoon(p.getNr(),p.getAchternaam(),p.getTussenvoegsel(),calendarToDate(p.getGebDat()),p.getGebPlaats(),geslacht);      
            
        }
        for(Gezin g : admin.getGezinnen()){
            Integer ouder2 = 0;
            if(g.getOuder2() == null){
                ouder2 = null;
            }
            else{
                ouder2 = g.getOuder2().getNr();
            }
            Date huwdatum;
            if(g.getHuwelijksdatum() == null){
                huwdatum = null;
            }
            else{
                huwdatum = calendarToDate(g.getHuwelijksdatum());
            }
            Date scheidatum;
            if(g.getScheidingsdatum() == null){
                scheidatum = null;
            }
            else{
                scheidatum = calendarToDate(g.getScheidingsdatum());
            }
            
            InsertGezin(g.getNr(), g.getOuder1().getNr(), ouder2, huwdatum, scheidatum);           
        }
        for(Persoon p : admin.getPersonen()){
            if(p.getOuderlijkGezin() != null){
                addFKOuders(p.getOuderlijkGezin().getNr(), p.getNr());
            }
        }
    }

    /**
     * Laadt de instellingen, in de vorm van een Properties bestand, en controleert
     * of deze in de correcte vorm is, en er verbinding gemaakt kan worden met
     * de database.
     * @param props
     * @return
     */
    @Override
    public final boolean configure(Properties props) {
        this.props = props;
        if (!isCorrectlyConfigured()) {
            System.err.println("props mist een of meer keys");
            return false;
        }

        try {
            initConnection();
            return true;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            this.props = null;
            return false;
        } finally {
            
        }
    }

    @Override
    public Properties config() {
        return props;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        if (!props.containsKey("driver")) {
            return false;
        }
        if (!props.containsKey("url")) {
            return false;
        }
        if (!props.containsKey("username")) {
            return false;
        }
        if (!props.containsKey("password")) {
            return false;
        }
        return true;
    }

    private void initConnection() throws SQLException {
        //opgave 4
        final String url = "jdbc:mysql://localhost:3306/";
        final String dbName = "dbstamboom";
        final String userName = "FrankHaver";
        final String password = "H4verFr4nk";
        sql2o  = new Sql2o(url+dbName, userName, password);
    }
    
    // toevoegen aan ouderlijkgezin gebeurt pas als het ouderlijkgezin bestaat
    public void InsertPersoon(int pnummer, String achternaam, String tussenvoegsel, Date gbdatum, String gbplaats, String geslacht){
        String insertSql = 
                "insert into persoon (PK_persoonsNummer, achternaam, tussenvoegsel, geboortedatum, geboorteplaats, geslacht, FK_ouders)"
                + "values(:pnummer, :achternaam, :tussenvoegsel, :gbdatum, :gbplaats, :geslacht, null)";
        try(Connection con = sql2o.open()){
            con.createQuery(insertSql)
                    .addParameter("pnummer", pnummer)
                    .addParameter("achternaam", achternaam)
                    .addParameter("tussenvoegsel", tussenvoegsel)
                    .addParameter("gbdatum", gbdatum)
                    .addParameter("gbplaats", gbplaats)
                    .addParameter("geslacht", geslacht)
                    .executeUpdate();
            System.out.println("Inserten Persoon Gelukt!");
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }        
    }
    
    public void InsertGezin(int geznr, int ouder1, Integer ouder2, Date huwelijksDatum, Date scheidingsdatum){
        String insertSql = 
                "insert into gezin (PK_gezinsNummer, FK_ouder1, FK_ouder2, huwelijksDatum, scheidingsdatum) "
                + "values (:geznr, :ouder1, :ouder2, :huwelijksDatum, :scheidingsdatum)";
        try(Connection con = sql2o.open()){
            con.createQuery(insertSql)
                    .addParameter("geznr", geznr)
                    .addParameter("ouder1", ouder1)
                    .addParameter("ouder2", ouder2)
                    .addParameter("huwelijksDatum", huwelijksDatum)
                    .addParameter("scheidingsdatum", scheidingsdatum)
                    .executeUpdate();
            System.out.println("Inserten Gezin Gelukt!");
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }      
    }
    
    public void addFKOuders(int FK_ouders, int PK_persoonsNummer){
        String updateSql = 
                "UPDATE PERSOON "
                + "SET FK_ouders = :FK_ouders "
                + "WHERE PK_persoonsNummer = :PK_persoonsNummer;";
        try(Connection con = sql2o.open()){
            con.createQuery(updateSql)
                    .addParameter("FK_ouders", FK_ouders)
                    .addParameter("PK_persoonsNummer", PK_persoonsNummer)
                    .executeUpdate();
            System.out.println("FK ouders gezet van persoon met nr: " + PK_persoonsNummer);
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }
    }
        
    public Date calendarToDate(Calendar cd){ 
        java.sql.Date d = new java.sql.Date(cd.getTimeInMillis());
        return d;
        
    }
    
    public void ClearTablePersoon(){
        String updateSql = "DELETE FROM PERSOON WHERE PK_persoonsNummer != -1";

        try (org.sql2o.Connection con = sql2o.open()) {
            con.createQuery(updateSql)
                .executeUpdate();
        }
    }
    
    public void ClearTableGezin(){
        String updateSql = "DELETE FROM GEZIN WHERE PK_gezinsNummer != -1;";

        try (org.sql2o.Connection con = sql2o.open()) {
            con.createQuery(updateSql)
                .executeUpdate();
        }
    }
}
