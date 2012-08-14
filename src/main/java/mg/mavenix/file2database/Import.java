package mg.mavenix.file2database;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Import {

    public static void main(String[] args) {
        
        int num = args.length;
        
        //default database name
        String database = "dbname";
        
        if (num > 0) {
            database = args[0];
            System.out.println(database);
        }
            
        DataInputStream in = null;

        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        PreparedStatement pst = null;
        PreparedStatement pst2 = null;

        String url = "jdbc:mysql://10.0.0.1/" + database;
        String user = "dbusername";
        String password = "dbpassword";

        DateFormat formatter = new SimpleDateFormat("dd-MMM-yy");

        try {
            con = DriverManager.getConnection(url, user, password);

            FileInputStream fstream = new FileInputStream("/tmp/virtcpt.txt");

            //to speed up an huge inserts
            pst = con.prepareStatement("ALTER TABLE comptes CHANGE id id INT( 11 ) UNSIGNED NOT NULL");
            pst.executeUpdate();

            pst = con.prepareStatement("ALTER TABLE comptes DROP PRIMARY KEY");
            pst.executeUpdate();

            pst = con.prepareStatement("ALTER TABLE comptes DROP INDEX IDX_Numero");
            pst.executeUpdate();

            pst = con.prepareStatement("TRUNCATE comptes");
            pst.executeUpdate();

            pst = con.prepareStatement("FLUSH TABLES");
            pst.executeUpdate();

            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String[] temp;
            String delimiter = "\\|";
            String strLine;
            final int batchSize = 5000;
            int count = 1;

            pst = con.prepareStatement("INSERT INTO comptes(id, Numero) VALUES(?, ?)");
            pst2 = con.prepareStatement("INSERT INTO comptes(id, Numero, DateFermeture) VALUES(?, ? , ?)");

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                temp = strLine.split(delimiter);
                if (temp.length > 6) {
                    if (temp.length > 7) {
                        java.sql.Date sqlDate = new Date(((java.util.Date) formatter.parse(temp[7])).getTime());
                        pst2.setDate(3, sqlDate);
                        pst2.setInt(1, count);
                        pst2.setString(2, temp[1]);
                        pst2.addBatch();
                    } else {
                        pst.setInt(1, count);
                        pst.setString(2, temp[1]);
                        pst.addBatch();
                    }

                    if (++count % batchSize == 0) {
                        pst.executeBatch();
                        pst2.executeBatch();
                        //System.out.print(".");
                    }
                }
            }
            pst.executeBatch();
            pst2.executeBatch();

            pst = con.prepareStatement("ALTER TABLE comptes ADD UNIQUE IDX_Numero (Numero)");
            pst.executeUpdate();

            pst = con.prepareStatement("ALTER TABLE comptes ADD PRIMARY KEY ( `id` )");
            pst.executeUpdate();

            pst = con.prepareStatement("ALTER TABLE comptes CHANGE id id INT( 11 ) UNSIGNED NOT NULL AUTO_INCREMENT");
            pst.executeUpdate();

            fstream.close();

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Import.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());

        } finally {
            //Close the input stream
            try {
                if (in != null) {
                    in.close();
                }

                if (pst2 != null) {
                    pst2.close();
                }
                
                if (pst != null) {
                    pst.close();
                }

                if (rs != null) {
                    rs.close();
                }

                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (Exception ex) {
                Logger lgr = Logger.getLogger(Import.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
}
