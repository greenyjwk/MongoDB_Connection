//import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.*;
import org.bson.Document;
import java.awt.image.BufferedImage;
import java.io.*;

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

        import static com.mongodb.client.model.Projections.fields;

public class OlympicAccessMongo extends JFrame {


    MongoDatabase sampleDBImage = null;
    ArrayList<String> playersName = new ArrayList<>();
    String NOCGlobal = "";

    JPanel northPanel = new JPanel();
    JPanel southPanel = new JPanel();
    JTextField input, commentInput;
    JTextField inputDistance;
    JTextArea output;
    MongoDatabase sampleDB = null;
    MongoClient client = null;
    MongoCollection<Document> collection = null;
    MongoCursor<Document> cursor = null;
    MongoCursor<Document> cursor1 = null;

    WindowListener exitListener = null;
    MongoIterable<String> dbList = null;
    MongoIterable<String> collList = null;
    JButton detailInfo;
    JComboBox<String> cb;
    JComboBox<String> location;
    JLabel lbl, lbl2, lbl3, lblCommentInput, label1;
    JButton btn, addComment;
    JButton showImages;
    JButton showLocation;

    public OlympicAccessMongo() {

        exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are You Sure to Close Application?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (confirm == 0) {
                    // Close the Mongo Client
//                    client.close();
                    System.exit(0);
                }
            }
        };
        addWindowListener(exitListener);

        setSize(600, 200);
        setLocation(400, 500);
        setTitle("Olympic Player Data Retrieval");

        Container cont = getContentPane();
        cont.setLayout(new BorderLayout());

        JButton search = new JButton("General Search");
        showImages = new JButton("Country Flag");
        JButton generateNames = new JButton("Generate Names");
        JButton connect = new JButton("Connect");
        JButton connectImageDB = new JButton("Connect Image");
        JButton clear = new JButton("Clear");
        showLocation = new JButton("Show");

        lbl2 = new JLabel("Enter Distance");
        lbl3 = new JLabel("Select City");
        label1 = new JLabel("Enter Player Name: ");

        inputDistance = new JTextField(5);

        String[] cities = {"London", "Paris", "Antwerpen", "Albertville", "Lillehammer", "Berlin", "Seoul", "Beijing", "Tokyo", "Atlanta"};
        location = new JComboBox<String>(cities);
        location.setMaximumSize(location.getPreferredSize()); // added code
        location.setAlignmentX(Component.CENTER_ALIGNMENT);// added code
        location.setVisible(true);

        input = new JTextField(20);
        output = new JTextArea(10, 50);
        JScrollPane spOutput = new JScrollPane(output);
        spOutput.setBackground(Color.lightGray);
        output.setBackground(Color.lightGray);


        northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
        northPanel.setBackground(Color.PINK);
        //northPanel.add(connect);
        //northPanel.add(connectImageDB);

        southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout());
        southPanel.add(connect);
        southPanel.add(connectImageDB);
        southPanel.setVisible(true);
        southPanel.setBackground(Color.PINK);

        northPanel.add(label1);
        northPanel.add(input);
        northPanel.add(generateNames);
        northPanel.add(search);

        northPanel.add(showImages);
        southPanel.add(clear);


        //--

        lbl = new JLabel("Name List:");
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setVisible(false);
        showImages.setVisible(false);
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


        btn = new JButton("Submit");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); // added code
        btn.setVisible(false);
        northPanel.add(btn);
        northPanel.setVisible(true); // added code

        btn.addActionListener(new dropDown());

        // location
        northPanel.add(lbl2);
        northPanel.add(inputDistance);
        northPanel.add(lbl3);
        northPanel.add(location);
        northPanel.add(showLocation);
        inputDistance.setVisible(true);
        lbl2.setVisible(true);
        lbl3.setVisible(true);
        showLocation.setVisible(true);


        //Comment
        addComment = new JButton("Add Comment");
        lblCommentInput = new JLabel("Comment: ");
        commentInput = new JTextField(20);
        southPanel.add(lblCommentInput);
        southPanel.add(commentInput);
        southPanel.add(addComment);


//        southPanel.setLayout(new FlowLayout());
//        southPanel.setVisible(true);
        lblCommentInput.setVisible(false);
        commentInput.setVisible(false);
        addComment.setVisible(false);
        // Comment

        //--

        cont.add(northPanel, BorderLayout.NORTH);
        cont.add(spOutput, BorderLayout.CENTER);
        cont.add(southPanel, BorderLayout.SOUTH);

        generateNames.addActionListener(new GenerateNames());
        connect.addActionListener(new ConnectMongo());

        connectImageDB.addActionListener(new ConnectMongoForImage());
        showImages.addActionListener(new GetMongoImage());
        addComment.addActionListener(new AddComment());
        search.addActionListener(new GetMongo());
        clear.addActionListener(new ClearMongo());

        showLocation.addActionListener(new ShowLoc());

//        addComment.addActionListener(new AddComment());


        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);



        setVisible(true);
    } //AccessMongo

    public static void main(String[] args) {
        // The following statements are used to eliminate MongoDB Logging
        //   information suche as INFO messages that the user should not see.
        // It requires the import of Logger and Level classes.
        //Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        //mongoLogger.setLevel(Level.INFO);

        OlympicAccessMongo runIt = new OlympicAccessMongo();

    }


    class GenerateNames implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            //Normal Find text
            String searchText = input.getText();

            Pattern p = Pattern.compile("^\\b" + searchText + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

            cursor = collection.find(QueryObj).iterator();

            playersName = new ArrayList<>();

            HashSet<String> duplicateCheck = new HashSet<>();
            cb.removeAllItems();
            while (cursor.hasNext()) {
                Document d = cursor.next();
                String player = d.getString("Name");
                playersName.add(player);

                if (!duplicateCheck.contains(player)) {
                    duplicateCheck.add(player);
                    cb.addItem(d.getString("Name"));
                }
            }

            cb.setVisible(true);
            lbl.setVisible(true);
            btn.setVisible(true);
            showImages.setVisible(true);


            lblCommentInput.setVisible(true);
            commentInput.setVisible(true);
            addComment.setVisible(true);

        }//actionPerformed
    }


    class dropDown implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            //in this section open the connection. Should be able to see if it is not null
            // to see if ti is already open
            String dropdown = cb.getSelectedItem().toString();

            Pattern p = Pattern.compile("^\\b" + dropdown + "\\b" + ".*", Pattern.CASE_INSENSITIVE);
            BasicDBObject QueryObj = new BasicDBObject("Name", p);

            cursor = collection.find(QueryObj).iterator();

            HashSet<Integer> playersId = new HashSet<>();
            while (cursor.hasNext()) {
                Document d = cursor.next();
                if (!playersId.contains(d.getInteger("ID"))) {
                    if (!playersId.contains(d.getInteger("ID"))) {
                        playersId.add(d.getInteger("ID"));

                        //need to check
                        NOCGlobal = d.getString("Team");

                        output.append(d.getString("Name") + "  /  "  + d.getString("Team") + "  /  "  + d.getString("Sport") + "\n");
                        output.append("The comment is: " + "\n" + d.getString("Comment") + "\n");

                        detailInfo = new JButton("Detail");
                        detailInfo.addActionListener(new detailInfoMongo());
                        detailInfo.setVisible(true);
                        output.add(detailInfo);

                        output.append(" ----- Attendance Games ----- " + '\n');
                    }
                    output.append(d.getString("City") + " " + d.getString("Games") + " / " + d.getString("Event") + "\n");
                }
            }
            output.append("\n\n\n\n");
        }
    }

    class ConnectMongoForImage implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            client = MongoClients.create("mongodb+srv://Ji:1q2w3e4r5tzxcvb@iste-610-team2.bafxi.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
            output.append("Connection to image server completed\n");

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
            output.append("Image collection obtained\n\n");
        }//actionPerformed


        private boolean objectExists(MongoIterable<String> objectList, String objectName) {
            for (String name : objectList) if (name.equals(objectName)) return true;
            return false;
        }
    }


    class ConnectMongo implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            client = MongoClients.create("mongodb+srv://Ji:1q2w3e4r5tzxcvb@iste-610-team2.bafxi.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
            output.append("Connection to mongoDB Atlas completed\n");

            try {
                // Use try/catch so that you will always close the database (finally)
                //Get a List of databases on the server connection

                dbList = client.listDatabaseNames();
                //Does Database Exist?
                String dbName = "OlympicHistory2";
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
            output.append("Olympic collection obtained\n\n");
        }//actionPerformed


        private boolean objectExists(MongoIterable<String> objectList, String objectName) {
            for (String name : objectList) if (name.equals(objectName)) return true;
            return false;
        }
    }//class ConnectMongo


    class AddComment implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            String comment = commentInput.getText();
            String dropdownUsername = cb.getSelectedItem().toString();


// THIS IS EXAMPLE 3 - update multiple fields
            Document search = new Document("Name", dropdownUsername);
            Document updt = new Document(); //new empty Document instance

            updt = updt.append("Comment", comment);

            System.out.println(updt);

            Document doc = new Document("$set", updt);

            try {
                collection.updateMany(search, doc);
            } catch (Exception e) {
                System.out.println("Error on insert");
                System.out.println(e.getMessage());
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
    }




class GetMongoImage implements ActionListener {

    // This is for image
    private BufferedImage image = null;
    private JLabel label = new JLabel();

    public void actionPerformed(ActionEvent event) {
        // In this section you should retrieve the data from the collection
        // and use a cursor to list the data in the JTextArea

        //Create instance of GridFS implementation
//            GridFSBucket gridFs = GridFSBuckets.create(sampleDBImage, "photos");
        GridFSBucket gridFs = GridFSBuckets.create(sampleDBImage);
        //Find the image with the name image1 using GridFS API

        try (GridFSDownloadStream downloadStream = gridFs.openDownloadStream(NOCGlobal + ".png")) {
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


class ShowLoc implements ActionListener {

    private BufferedImage image = null;
    private JLabel label = new JLabel();

    public void actionPerformed(ActionEvent event) {
        // In this section you should retrieve the data from the collection
        // and use a cursor to list the data in the output JTextArea


        //Normal Find text
        int searchDistance = Integer.parseInt(inputDistance.getText());
        System.out.println(searchDistance);

        String cityName = location.getSelectedItem().toString();
        output.append(cityName + "\n");

        Pattern p = Pattern.compile(cityName);
        BasicDBObject QueryObj = new BasicDBObject("City", cityName);
        cursor = collection.find(QueryObj).iterator();

        double cityLatitude = 0;
        double citylongitude = 0;


        while (cursor.hasNext()) {
            Document d = cursor.next();
            cityLatitude = d.getDouble("latitude");
            citylongitude = d.getDouble("longitude");
            String playerName = d.getString("Name");
            output.append(playerName + "\n");
        }
        output.append(String.valueOf(cityLatitude) + "\n");
        output.append(String.valueOf(citylongitude));


        BasicDBObject criteria = new BasicDBObject("$near", new double[]{citylongitude, cityLatitude});
        criteria.put("$maxDistance", searchDistance);
        BasicDBObject query2 = new BasicDBObject("loc", criteria);
        ;
        cursor1 = collection.find(query2).iterator();

        while (cursor1.hasNext()) {
            Document d = cursor1.next();
            String playername = d.getString("Name");
            output.append(playername + "\n");
        }


    }//actionPerformed
}


class GetMongo implements ActionListener {

    private BufferedImage image = null;
    private JLabel label = new JLabel();

    public void actionPerformed(ActionEvent event) {
        // In this section you should retrieve the data from the collection
        // and use a cursor to list the data in the output JTextArea

        //Normal Find text
        String searchText = input.getText();
        System.out.println(searchText);

        Pattern p = Pattern.compile("^\\b" + searchText + "\\b" + ".*", Pattern.CASE_INSENSITIVE);
        BasicDBObject QueryObj = new BasicDBObject("Name", p);

        cursor = collection.find(QueryObj).iterator();

        HashSet<Integer> playersId = new HashSet<>();
        while (cursor.hasNext()) {
            Document d = cursor.next();
            if (!playersId.contains(d.getInteger("ID"))) {
                playersId.add(d.getInteger("ID"));
                output.append("\n\n");

                output.append(d.getString("Name") + "  /  " + d.getString("Team") + "\n" + d.getString("NOC") + "\n");
                detailInfo = new JButton("Detail");
                detailInfo.addActionListener(new detailInfoMongo());
                detailInfo.setVisible(true);
                output.add(detailInfo);

                output.append(" ----- Attendance Games ----- " + '\n');
            }
            output.append(d.getString("City") + "  " + d.getInteger("Year") + "  " + d.getString("Games") + "\n");
        }
        output.append("Total Attendance Games " + playersId.size() + "\n");
    }//actionPerformed
}


class ClearMongo implements ActionListener {
    public void actionPerformed(ActionEvent event) {
        //in this section open the connection. Should be able to see if it is not null
        // to see if ti is already open
        output.setText("");
    }
}


class detailInfoMongo implements ActionListener {

    public void actionPerformed(ActionEvent event) {
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

        int playersId = 0;
        while (cursor.hasNext()) {
            Document d = cursor.next();
            output.append(d.getString("City") + "  " + d.getInteger("Year") + "  " + d.getString("Games") + "\n");
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
