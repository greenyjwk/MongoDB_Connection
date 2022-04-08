//Mongo Imports
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.ArrayList;


public class Regex{
    // Attribute used by Mongo in several methods
    MongoDatabase sampleDB = null;
    MongoClient client = null;
    MongoCollection<Document> collection = null;
    MongoCursor<Document> cursor = null;
    MongoIterable<String> dbList = null;
    MongoIterable<String> collList = null;


    public static void main(String[] args) {

        Regex app = new Regex();

        // Make the connection
        boolean success = app.connections();

        if(success) {
            // find
            app.mongoFind();
            app.closeMongo();
        }
    }

    public boolean connections() { //boolean returns true if connections are successful

        //boolean to return is connections are success or not
        boolean connSuccess = false;

        // Connect to the Mongo Database
        client = MongoClients.create("mongodb+srv://Ji:1q2w3e4r5tzxcvb@iste-610-team2.bafxi.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
//        client = MongoClients.create("mongodb+srv://iste-610-team2.bafxi.mongodb.net/myFirstDatabase?authSource=%24external&authMechanism=MONGODB-X509&retryWrites=true&w=majority");

        System.out.println("\n* Connection to server completed\n");

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
                connSuccess = true;
            } else {  //db not found
                System.out.println("\nDATABASE NOT FOUND\n");
                sampleDB = null; //DB not found
                connSuccess = false;
                client.close();
                return connSuccess;
            }

            //Get a List of collections in the database
            collList = sampleDB.listCollectionNames();

            //get the collection
            String colName = "Olympic";
            if (objectExists(collList, colName)) {  //returns true or false
                System.out.println("\n* Collection found\n");
                collection = sampleDB.getCollection(colName);

                // This is sampele query
                Document query = new Document(  "_id", new ObjectId("623903bbadb4ce02b755122d") );
                Document result = collection.find(query).iterator().next();

                System.out.println("Query Result: " + result.getString("Name"));
                System.out.println("\n* Collection connection completed\n");

            } else {  //collection not found
                System.out.println("\n* COLLECTION NOT FOUND\n");
                collection = null;
                connSuccess = false;
                client.close();
                return connSuccess;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connSuccess;
    }//connections

    private ArrayList<Document> mongoFind() {
        String searchText1 = "\\bdish\\b";
        String searchText2 = "\\bgood\\b";
        Bson query1 = regex("text", searchText1, "i");
        Bson query2 = regex("text", searchText2, "i");
        Bson query3 = and(query1,query2);
        cursor = collection.find(query3).iterator();


        ArrayList<Document> list = new ArrayList<>();

        //traverse cursor
        int cnt = 0;
        while(cursor.hasNext()) {
            Document doc = cursor.next();
            System.out.println(doc.getString("Name"));
            list.add(doc);
            cnt = cnt+1;
        }

        System.out.println(list);
        System.out.println("\nThe count is " + cnt + "\n");
        return list;
    }



    private boolean objectExists(MongoIterable <String> objectList, String objectName) {
        for (String name : objectList) {
            if (name.equals(objectName)) return true;
        }
        return false;
    }

    private void closeMongo() {
        client.close();
    } //closeMongo
}