import com.mongodb.Block;
//import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.*;
import com.mongodb.client.gridfs.model.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import static com.mongodb.client.model.Filters.eq;
//


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

//Mongo Imports
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.regex;

public class AccessMongo extends JFrame {


    MongoDatabase sampleDBImage = null;
    ArrayList<String> playersName = new ArrayList<>();

    JPanel northPanel = new JPanel();
    JTextField input;
    JTextArea output;
    MongoDatabase sampleDB = null;
    MongoClient client = null;
    MongoCollection<Document> collection = null;
    MongoCursor<Document> cursor = null;

    WindowListener exitListener = null;
    MongoIterable<String> dbList = null;
    MongoIterable<String> collList = null;
    JButton detailInfo;
    JComboBox<String> cb;
    JLabel lbl;
    JButton btn;

    public AccessMongo() {
        System.out.println("Hello Mongo");

        setSize(600, 200);
        setLocation(400, 500);
        setTitle("Access MongoDB");

        Container cont = getContentPane();
        cont.setLayout(new BorderLayout() );

        JButton search = new JButton("General Search");
        JButton showImage = new JButton("Show Images");
        JButton generateNames = new JButton("Generate Names");
        JButton connect = new JButton("Connect");
        JButton connectImageDB = new JButton("Connect Image");
        JButton clear = new JButton("Clear");

        input = new JTextField(20);
        output = new JTextArea(10, 30);
        JScrollPane spOutput = new JScrollPane(output);

        northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
        northPanel.add(connect);
        northPanel.add(connectImageDB);

        northPanel.add(input);
        northPanel.add(generateNames);
        northPanel.add(search);

        northPanel.add(showImage);
        northPanel.add(clear);


        //--

        lbl = new JLabel("Name List:");
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setVisible(false);
        //lbl.setVisible(true); // Not needed

        northPanel.add(lbl);

        //String[] choices = { "ID", "Name", "NOC", "Sex", "Sport" , "Event", "Games"};



//        String playerList[] = playersName.toArray(new String[playersName.size()]);
//        cb = new JComboBox<String>(playerList);
//
//
//
//        cb.setMaximumSize(cb.getPreferredSize()); // added code
//        cb.setAlignmentX(Component.CENTER_ALIGNMENT);// added code
//        //cb.setVisible(true); // Not needed
//        northPanel.add(cb);


        String playerList[] = playersName.toArray(new String[playersName.size()]);
        cb = new JComboBox<String>(playerList);
        cb.setMaximumSize(cb.getPreferredSize()); // added code
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);// added code
        northPanel.add(cb);
        cb.setVisible(false);


        System.out.println("This is iiii " + cb);
        btn = new JButton("Submit");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); // added code
        btn.setVisible(false);
        northPanel.add(btn);
        northPanel.setVisible(true); // added code

        btn.addActionListener(new dropDown());


        //--

        cont.add(northPanel, BorderLayout.NORTH);
        cont.add(spOutput, BorderLayout.CENTER);

        generateNames.addActionListener(new GenerateNames());
        connect.addActionListener(new ConnectMongo());

        connectImageDB.addActionListener(new ConnectMongoForImage());
        showImage.addActionListener(new GetMongoImage());

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


    class GenerateNames implements ActionListener {

        public void actionPerformed (ActionEvent event) {

            //Normal Find text
            String searchText = input.getText();

            Pattern p = Pattern.compile("^\\b" + searchText + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

            cursor = collection.find(QueryObj).iterator();

            playersName = new ArrayList<>();

            HashSet<String> duplicateCheck = new HashSet<>();
            cb.removeAllItems();
            while(cursor.hasNext()) {
                Document d = cursor.next();
                String player = d.getString("Name");
                playersName.add( player );

                if(!duplicateCheck.contains(player)){
                    duplicateCheck.add(player);
                    cb.addItem(d.getString("Name"));
                }

            }

            cb.setVisible(true);
            lbl.setVisible(true);
            btn.setVisible(true);

        }//actionPerformed
    }



    class dropDown implements ActionListener {
        public void actionPerformed (ActionEvent event) {
            //in this section open the connection. Should be able to see if it is not null
            // to see if ti is already open
            String dropdown = cb.getSelectedItem().toString();

            Pattern p = Pattern.compile("^\\b" + dropdown + "\\b" + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

            cursor = collection.find(QueryObj).iterator();

            HashSet<String> playersId = new HashSet<>();
            while(cursor.hasNext()) {
                Document d = cursor.next();
                if(!playersId.contains(d.getString("ID"))){
                    playersId.add(d.getString("ID"));
                    output.append("\n\n");

                    output.append(d.getString("Name") +"  /  "+   d.getString("Team") +"\n" + d.getString("NOC")   );

                    detailInfo = new JButton("Detail");
                    detailInfo.addActionListener(new detailInfoMongo());
                    detailInfo.setVisible(true);
                    output.add(detailInfo);

                    output.append(" ----- Attendance Games ----- " + '\n');
                }
                output.append(d.getString("City") + "  " + d.getString("Year") + "  " + d.getString("Games") +"\n");
            }

            output.append("Total Attendance Games " + playersId.size() + "\n");
        }
    }

    class ConnectMongoForImage implements ActionListener {
        public void actionPerformed (ActionEvent event) {

            client = MongoClients.create("mongodb+srv://Ji:1q2w3e4r5tzxcvb@iste-610-team2.bafxi.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
            output.append("Connection to server completed\n");

            try {
                // Use try/catch so that you will always close the database (finally)
                //Get a List of databases on the server connection

                dbList = client.listDatabaseNames();
                //Does Database Exist?
                String dbName = "Image";
                if (objectExists(dbList, dbName)) {  //returns true or false
                    System.out.println("\n* Database found\n");
                    sampleDBImage = client.getDatabase(dbName); //connect to the database
                    System.out.println("\n* Connection to database completed\n");
                } else {  //db not found
                    System.out.println("\nDATABASE NOT FOUND\n");
                    sampleDBImage = null; //DB not found
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
                String colImage = "countryFLag";
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


    class GetMongoImage implements ActionListener {

        // This is for image
        private BufferedImage image = null;
        private JLabel label = new JLabel();

        public void actionPerformed (ActionEvent event) {
            // In this section you should retrieve the data from the collection
            // and use a cursor to list the data in the output JTextArea

            //Create instance of GridFS implementation
//            GridFSBucket gridFs = GridFSBuckets.create(sampleDBImage, "photos");
            GridFSBucket gridFs = GridFSBuckets.create(sampleDBImage);
            //Find the image with the name image1 using GridFS API

            try (GridFSDownloadStream downloadStream = gridFs.openDownloadStream("Crotia.png")) {
                image = ImageIO.read(downloadStream);

                JFrame frame = new JFrame();
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(image)));
                frame.pack();
                frame.setVisible(true);



            } catch (IOException e) {
                e.printStackTrace();
            }

            Image displayImg = image.getScaledInstance(750, 500, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(displayImg);
            label = new JLabel(icon);
            add(label);
            label.setVisible(true);
            //image

        }//actionPerformed
    }



    class GetMongo implements ActionListener {

        public void actionPerformed (ActionEvent event) {
            // In this section you should retrieve the data from the collection
            // and use a cursor to list the data in the output JTextArea

            //Normal Find text
            String searchText = input.getText();
            System.out.println(searchText);

            Pattern p = Pattern.compile("^\\b" + searchText + "\\b" + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

//            detailInfo = new JButton("Detail");
//            output.add(detailInfo);
//            detailInfo.addActionListener(new detailInfoMongo());
//            detailInfo.setVisible(true);

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

                    output.append(d.getString("Name") +"  /  "+   d.getString("Team") +"\n" + d.getString("NOC")   );

                    detailInfo = new JButton("Detail");
                    detailInfo.addActionListener(new detailInfoMongo());
                    detailInfo.setVisible(true);
                    output.add(detailInfo);

                    output.append(" ----- Attendance Games ----- " + '\n');
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







    class detailInfoMongo implements ActionListener {

        public void actionPerformed (ActionEvent event) {
            // In this section you should retrieve the data from the collection
            // and use a cursor to list the data in the output JTextArea

            //Normal Find text
            String searchText = input.getText();
            System.out.println(searchText);

            Pattern p = Pattern.compile("^\\b" + searchText + "\\b" + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

//            623903bbadb4ce02b755122d
//            Document query = new Document(  "_id", new ObjectId(searchText) );
//            cursor = collection.find(query).iterator();

//            BasicDBObject QueryObj = new BasicDBObject();
//            QueryObj.put("Name", searchTextBson);
//            QueryObj.put("Name", searchText);
            cursor = collection.find(QueryObj).iterator();

            int playersId = 0 ;
            while(cursor.hasNext()) {
                Document d = cursor.next();
                output.append(d.getString("City") + "  " + d.getString("Year") + "  " + d.getString("Games") +"\n");
                playersId++;
            }

            output.append("Total Attendance Games " + playersId + "\n");

            //Normal Find id numeric value
            // int searchText = Integer.parseInt(input.getText());

            // Normal Find regex
            // String searchText = input.getText();
            // String regexPattern = "^" + searchText + "\\b.*";
            // cursor = collection.find(regex("text", regexPattern, "i")).iterator();

        }//actionPerformed
    }
}