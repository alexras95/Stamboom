/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.IOException;
import static java.lang.reflect.Array.set;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
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

    private Connection conn;

    public DatabaseMediator(Properties props) {
        this.props = props;
        initConnection();
    }

    @Override
    public Administratie load() throws IOException {
        //todo opgave 4
        Administratie admin = new Administratie();
        for(Persoon p : readPersoon()){
            // ouderlijkgezin is hier nog null wordt pas toegewezen als de gezinnen bestaan
            admin.addPersoon(p.getGeslacht(), p.getVoornamen().split(" "), p.getAchternaam(), p.getTussenvoegsel(), p.getGebDat(), p.getGebPlaats(), p.getOuderlijkGezin());
        }
        for(Gezin g : readGezin()){
            for(Persoon p1: admin.getPersonen()){
                for(Persoon p2 : admin.getPersonen()){
                    if(g.getOuder1().getNr() == p1.getNr()){
                        if(g.getOuder2() != null){
                            if(g.getOuder2().getNr() == p2.getNr()){
                                admin.addOngehuwdGezin(p1, p2);
                            }
                        }
                        else{
                            admin.addOngehuwdGezin(p1, null);
                        }         
                    }
                }     
            }
        }
        for(Persoon p: admin.getPersonen()){
            if(getOuderlijkGezinNr(p.getNr()) != 0){
                for(Gezin g: admin.getGezinnen()){
                    if(g.getNr() == getOuderlijkGezinNr(p.getNr())){
                        p.setOuders(g);
                    }
                }
            }
        }
        for(Gezin g: admin.getGezinnen()){
            Calendar huwdat = getHuwDatumGezin(g.getNr());
            if(huwdat != null){
                admin.setHuwelijk(g, huwdat);
            }
            Calendar scheidat = getScheiDatumGezin(g.getNr());
            if(scheidat != null){
                admin.setScheiding(g, scheidat);
            }
        }
        return admin;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        //todo opgave 4
        deleteFKOuders();
        deleteGezinnen();
        deletePersonen();
        for(Persoon p : admin.getPersonen()){
            createPersoon(p);
        }
        for(Gezin g : admin.getGezinnen()){
            createGezin(g);
        }
        // FKOuders pas updaten als de gezinnen bestaan ivm FK constraint
        for(Persoon p : admin.getPersonen()){
            if(p.getOuderlijkGezin() != null){
                updateFKOuders(p);
            }           
        }
        
    }

    /**
     * @Author Frank Haver
     * Maakt een persoon aan in de database, FK_ouders kan pas toegewezen worden als de gezinnen bestaan
     * @param p de persoon die in de database gezet moet worden
     * @return retourneert de primary key van het gegenereerde object
     */
    public int createPersoon(Persoon p){
        initConnection();
        PreparedStatement pstmt = null;
        ResultSet keys = null;
        
        String insertSql = 
                "insert into persoon (PK_persoonsNummer, achternaam, voornamen, tussenvoegsel, geboortedatum, geboorteplaats, geslacht, FK_ouders)"
                + "values(?, ?, ?, ?, ?, ?, ?, null)";
        try{
            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, p.getNr());
            pstmt.setString(2, p.getAchternaam());
            pstmt.setString(3, p.getVoornamen());
            pstmt.setString(4, p.getTussenvoegsel());
            pstmt.setDate(5, calendarToDate(p.getGebDat()));
            pstmt.setString(6, p.getGebPlaats());
            pstmt.setString(7, p.getGeslacht().toString());
            pstmt.executeUpdate();
            keys = pstmt.getGeneratedKeys();
            int key = -1;
            if(keys.next()){
                key = keys.getInt(1);
            }
            keys.close();
            pstmt.close();
            System.out.println("Persoon Aangemaakt met id: " + key);                        
            return key;
        }
        catch(Exception ex){
            System.out.println(ex.toString());
            return -1;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }
    
    /**
     * @Author Frank Haver
     * Leest de tabel Persoon uit, maakt hier Persoon objecten van en stopt dit in een lijst van personen
     * ouderlijkgezin moet later uitgelezen worden
     * @return retourneert de lijst van personen
     */
    public ArrayList<Persoon> readPersoon(){
        initConnection();
        Statement stmt = null; 
        ResultSet rs = null;
        ArrayList<Persoon> tempPersonen = new ArrayList<>();
        
        String readSql = "SELECT * FROM PERSOON";
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(readSql);
            while(rs.next()){
                Geslacht geslacht;
                if(rs.getString("geslacht").equals("MAN")){
                    geslacht = Geslacht.MAN;
                }
                else{
                    geslacht = Geslacht.VROUW;
                }                   
                Persoon tempPers = new Persoon(rs.getInt("PK_persoonsNummer"), 
                        rs.getString("voornamen").split(" "), 
                        rs.getString("achternaam"), 
                        rs.getString("tussenvoegsel"), 
                        dateToCalendar(rs.getDate("geboortedatum")), 
                        rs.getString("geboorteplaats"), 
                        geslacht, 
                        null);
                tempPersonen.add(tempPers);
            }
            rs.close();
            stmt.close();
            return tempPersonen;
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            return null;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @Author Frank Haver
     * geeft het nr van het ouderlijk gezin terug van een bepaald persoon
     * @param PK_persoonsNummer nummer van een persoon komt binnen als parameter
     * @return retourneert het nummer van een ouderlijk gezin van een bepaald persoon
     */
    public int getOuderlijkGezinNr(int PK_persoonsNummer){
        initConnection();
        PreparedStatement pstmt = null; 
        ResultSet rs = null;
        int ouderlijkGezin = 0;
        
        String readSql = "SELECT FK_ouders FROM PERSOON WHERE PK_persoonsNummer = ?";
        try{
            pstmt = conn.prepareStatement(readSql);
            pstmt.setInt(1, PK_persoonsNummer);
            rs = pstmt.executeQuery();
            while(rs.next()){
                ouderlijkGezin = rs.getInt("FK_ouders");
            }
            rs.close();
            pstmt.close();
            return ouderlijkGezin;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @Author Frank Haver 
     * Maakt een gezin aan in de database FK_ouder2, huwelijksDatum en scheidingsdatum kunnen null zijn
     * @param g gezin dat in de database gezet moet worden
     * @return retourneert de primary key van het gegenereerde object 
     */
    public int createGezin(Gezin g){
        initConnection();
        PreparedStatement pstmt = null;
        ResultSet keys = null;
        
        String insertSql = "insert into gezin (PK_gezinsNummer, FK_ouder1, FK_ouder2, huwelijksDatum, scheidingsdatum) "
                + "values (?, ?, ?, ?, ?)";
        try{
            pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, g.getNr());
            pstmt.setInt(2, g.getOuder1().getNr());
            if(g.getOuder2() == null){
                pstmt.setNull(3, Types.INTEGER);
            }
            else{ 
                pstmt.setInt(3, g.getOuder2().getNr());
            }
            if(g.getHuwelijksdatum() == null){ 
                pstmt.setNull(4, Types.DATE);
            }
            else{ 
                pstmt.setDate(4, calendarToDate(g.getHuwelijksdatum()));
            }
            if(g.getScheidingsdatum() == null){ 
                pstmt.setNull(5, Types.DATE);
            }
            else{ 
                pstmt.setDate(5, calendarToDate(g.getScheidingsdatum()));
            }
            pstmt.executeUpdate();
            keys = pstmt.getGeneratedKeys();
            int key = -1;
            if(keys.next()){
                key = keys.getInt(1);
            }
            keys.close();
            pstmt.close();
            System.out.println("Gezin Aangemaakt met id: " + key);                        
            return key;
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            return -1;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    /**
     * @Author Frank Haver
     * Leest de tabel Gezin uit, maakt hier Gezin objecten van en stopt dit in een lijst van gezinnen
     * ouder1 en ouder2 moeten toegewezen worden dmv de methode readPersoon()
     * als de data niet null zijn worden deze aan het gezin worden toegewezen
     * @return 
     */
    public ArrayList<Gezin> readGezin(){
        initConnection();
        Statement stmt = null; 
        ResultSet rs = null;
        ArrayList<Gezin> tempGezinnen = new ArrayList<>();
        
        String readSql = "SELECT * FROM Gezin";
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(readSql);
            while(rs.next()){
                Integer FK_ouder2 = -1;
                Integer FK_ouder1 = rs.getInt("FK_ouder1");
                FK_ouder2 = rs.getInt("FK_ouder2");
                Date huwdat = rs.getDate("huwelijksDatum");
                Date scheidat = rs.getDate("scheidingsdatum");
                Calendar huwdatum = null;
                Calendar scheidatum = null;
                if(huwdat != null){
                    huwdatum = dateToCalendar(huwdat);
                }
                if(scheidat != null){
                    scheidatum = dateToCalendar(scheidat);
                }
                Persoon ouder1 = null;
                Persoon ouder2 = null;
                for(Persoon p : readPersoon()){
                    if(p.getNr() == FK_ouder1){
                        ouder1 = p;
                    }
                    else if(FK_ouder2 != 0){
                        if(p.getNr() == FK_ouder2){
                            ouder2 = p;
                        }
                    }                        
                }
                Gezin tempGezin = new Gezin(rs.getInt("PK_gezinsNummer"), 
                        ouder1, 
                        ouder2);
                tempGezinnen.add(tempGezin);
            }
            stmt.close();
            rs.close();
            return tempGezinnen;
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            return null;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Calendar getHuwDatumGezin(int PK_gezinsNummer){
        initConnection();
        PreparedStatement pstmt = null; 
        ResultSet rs = null;
        Date huwdat = null;
        
        String readSql = "SELECT huwelijksDatum FROM GEZIN WHERE PK_gezinsNummer = ?";
        try{
            pstmt = conn.prepareStatement(readSql);
            pstmt.setInt(1, PK_gezinsNummer);
            rs = pstmt.executeQuery();
            while(rs.next()){
                huwdat = rs.getDate("huwelijksDatum");
            }
            rs.close();
            pstmt.close();
            if(huwdat != null){
                return dateToCalendar(huwdat);
            }
            else{
                return null;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Calendar getScheiDatumGezin(int PK_gezinsNummer){
        initConnection();
        PreparedStatement pstmt = null; 
        ResultSet rs = null;
        Date scheidat = null;
        
        String readSql = "SELECT scheidingsdatum FROM GEZIN WHERE PK_gezinsNummer = ?";
        try{
            pstmt = conn.prepareStatement(readSql);
            pstmt.setInt(1, PK_gezinsNummer);
            rs = pstmt.executeQuery();
            while(rs.next()){
                scheidat = rs.getDate("scheidingsdatum");
            }
            rs.close();
            pstmt.close();
            if(scheidat != null){
               return dateToCalendar(scheidat);
            }
            else{
                return null;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @Author Frank Haver
     * verwijdert alle personen uit de personen tabel
     */
    public void deletePersonen(){       
        initConnection();
        Statement stmt = null;
        String deleteSql = "DELETE FROM PERSOON WHERE PK_persoonsNummer != -1";
        
        try{
            stmt = conn.createStatement();
            stmt.executeUpdate(deleteSql);
            stmt.close();
        }
        catch(SQLException ex){
            System.out.println(ex.toString());
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @Author Frank Haver
     * verwijdert alle gezinnen uit de gezinnen tabel
     */
    public void deleteGezinnen(){
        initConnection();
        Statement stmt = null;
        String deleteSql = "DELETE FROM GEZIN WHERE PK_gezinsNummer != -1";
        
        try{
            stmt = conn.createStatement();
            stmt.executeUpdate(deleteSql);
            stmt.close();
        }
        catch(SQLException ex){
            System.out.println(ex.toString());
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
        }
    }
    
    /**
     * @Author Frank Haver
     * update de tabel zet de foreign key van ouders
     * @param p de persoon die geupdate moet worden
     */
    public void updateFKOuders(Persoon p){
        initConnection();        
        PreparedStatement pstmt = null;
        String updateSql = "UPDATE PERSOON SET FK_ouders = ? WHERE PK_persoonsNummer = ?";
        
        try{
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setInt(1, p.getOuderlijkGezin().getNr());
            pstmt.setInt(2, p.getNr());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
         
    /**
     * @Author Frank Haver
     * zet de FK ouders in de tabel persoon weer allemaal op null zodat deze tabel geen 
     * koppeling meer heeft met de tabel gezin
     */
    public void deleteFKOuders(){
        initConnection();
        PreparedStatement pstmt = null;
        String updateSql = "UPDATE PERSOON SET FK_ouders = ? WHERE PK_persoonsNummer != ?";
        
        try{
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setNull(1, Types.INTEGER);
            pstmt.setInt(2, -1);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        }
        finally{
            try {
                conn.close();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
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
        initConnection();
        return true;
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

    private void initConnection(){
        //opgave 4
        final String url = "jdbc:mysql://localhost:3306/";
        final String dbName = "dbstamboom";
        final String userName = "FrankHaver";
        final String password = "H4verFr4nk";
        try {

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    //"jdbc:mysql://athena01.fhict.local:3306/dbi320127", "dbi320127", "Qmw4J6kVid"
                    url+dbName, userName, password
            );
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Failed to get connection");
            e.printStackTrace();
        }
        
    }
    
    public Date calendarToDate(Calendar cd){ 
        java.sql.Date d = new java.sql.Date(cd.getTimeInMillis());
        return d;       
    }
    
    public Calendar dateToCalendar(Date date){ 
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
    
}
