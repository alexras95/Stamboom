/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sql2o.Sql2o;
import stamboom.domain.Administratie;

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
    
    public void InsertPersoon(String achternaam, String tussenvoegsel, Date gbdatum, String gbplaats, String geslacht){
        String insertSql = 
                "insert into persoon (achternaam, tussenvoegsel, geboortedatum, geboorteplaats, geslacht, FK_ouders)"
                + "values(:achternaam, :tussenvoegsel, :gbdatum, :gbplaats, :geslacht, null)";
        try(org.sql2o.Connection con = sql2o.open()){
            con.createQuery(insertSql)
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
}
