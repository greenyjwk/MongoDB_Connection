import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.regex.Pattern;

//Mongo Imports
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.regex;

public class AccessMongo extends JFrame {

    JTextField input;
    JTextArea output;
    MongoDatabase sampleDB = null;
    MongoClient client = null;
    MongoCollection<Document> collection = null;
    MongoCursor<Document> cursor = null;

    WindowListener exitListener = null;
    MongoIterable<String> dbList = null;
    MongoIterable<String> collList = null;

    public AccessMongo() {
        System.out.println("Hello Mongo");

        setSize(600, 200);
        setLocation(400, 500);
        setTitle("Access MongoDB");

        Container cont = getContentPane();
        cont.setLayout(new BorderLayout() );

        JButton search = new JButton("Search");
        JButton connect = new JButton("Connect");
        JButton clear = new JButton("Clear");

        input = new JTextField(20);
        output = new JTextArea(10, 30);
        JScrollPane spOutput = new JScrollPane(output);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
        northPanel.add(connect);
        northPanel.add(input);
        northPanel.add(search);
        northPanel.add(clear);

        cont.add(northPanel, BorderLayout.NORTH);
        cont.add(spOutput, BorderLayout.CENTER);

        connect.addActionListener(new ConnectMongo());
        search.addActionListener(new GetMongo());
        clear.addActionListener(new ClearMongo());

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are You Sure to Close Application?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (confirm == 0) {
                    // Close the Mongo Client
                    client.close();
                    System.exit(0);
                }
            }
        };

        addWindowListener(exitListener);
        setVisible(true);
    } //AccessMongo

    public static void main (String [] args) {
        // The following statements are used to eliminate MongoDB Logging
        //   information suche as INFO messages that the user should not see.
        // It requires the import of Logger and Level classes.
        //Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        //mongoLogger.setLevel(Level.INFO);

        AccessMongo runIt = new AccessMongo();
    }

    class ConnectMongo implements ActionListener {
        public void actionPerformed (ActionEvent event) {

            client = MongoClients.create("mongodb+srv://Ji:1q2w3e4r5tzxcvb@iste-610-team2.bafxi.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
            output.append("Connection to server completed\n");

            try {
                // Use try/catch so that you will always close the database (finally)
                //Get a List of databases on the server connection

                dbList = client.listDatabaseNames();
                //Does Database Exist?
                String dbName = "OlympicHistory";
                if (objectExists(dbList, dbName)) {  //returns true or false
                    System.out.println("\n* Database found\n");
                    sampleDB = client.getDatabase(dbName); //connect to the database
                    System.out.println("\n* Connection to database completed\n");
                } else {  //db not found
                    System.out.println("\nDATABASE NOT FOUND\n");
                    sampleDB = null; //DB not found
                    client.close();
                }

                collList = sampleDB.listCollectionNames();

                //get the collection
                String colName = "Olympic";
                if (objectExists(collList, colName)) {  //returns true or false
                    System.out.println("\n* Collection found\n");
                    collection = sampleDB.getCollection(colName);
                    System.out.println("\n* Collection connection completed\n");
                } else {  //collection not found
                    System.out.println("\n* COLLECTION NOT FOUND\n");
                    collection = null;
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            output.append("Collection obtained\n");
        }//actionPerformed


        private boolean objectExists(MongoIterable <String> objectList, String objectName) {
            for (String name : objectList) if (name.equals(objectName)) return true;
            return false;
        }
    }//class ConnectMongo

    class GetMongo implements ActionListener {

        public void actionPerformed (ActionEvent event) {
            // In this section you should retrieve the data from the collection
            // and use a cursor to list the data in the output JTextArea

            //Normal Find text
            String searchText = input.getText();
            System.out.println(searchText);
            Bson searchTextBson = regex("text", searchText, "i");

            Pattern p = Pattern.compile(searchText + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

//            623903bbadb4ce02b755122d
//            Document query = new Document(  "_id", new ObjectId(searchText) );
//            cursor = collection.find(query).iterator();

//            BasicDBObject QueryObj = new BasicDBObject();
//            QueryObj.put("Name", searchTextBson);
//            QueryObj.put("Name", searchText);
            cursor = collection.find(QueryObj).iterator();

            HashSet<String> playersId = new HashSet<>();
            while(cursor.hasNext()) {
                Document d = cursor.next();
                if(!playersId.contains(d.getString("ID"))){
                    playersId.add(d.getString("ID"));
                    output.append("\n\n");
                    output.append(d.getString("Name") +"  /  "+  d.getString("NOC") + "  /  " + d.getString("Team") +"\n");
                    output.append("----- Attendance Games -----" + '\n');
                }
                output.append(d.getString("City") + "  " + d.getString("Year") + "  " + d.getString("Games") +"\n");
            }

            output.append("Total Attendance Games " + playersId.size() + "\n");

            //Normal Find id numeric value
            // int searchText = Integer.parseInt(input.getText());

            // Normal Find regex
            // String searchText = input.getText();
            // String regexPattern = "^" + searchText + "\\b.*";
            // cursor = collection.find(regex("text", regexPattern, "i")).iterator();

        }//actionPerformed
    }


    class ClearMongo implements ActionListener {
        public void actionPerformed (ActionEvent event) {
            //in this section open the connection. Should be able to see if it is not null
            // to see if ti is already open
            output.setText("");
        }
    }
}