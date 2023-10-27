import org.json.JSONArray;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Cluster cluster = new Cluster();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Connected: " + cluster.getStatus() + " - Active database: " + cluster.getActiveDatabase());
            switch (cluster.MainMenu()) {
                case 1 -> {
                    System.out.print("Enter document ID: ");
                    String documentId = scanner.nextLine();
                    System.out.println("\n" + cluster.readDocument(documentId));
                }
                case 2 -> {
                    JSONArray array = cluster.readAllDocuments();
                    System.out.println("\n[");
                    for (int i = 0; i < array.length(); i++) {
                        System.out.println(array.get(i));
                    }
                    System.out.println("]");
                }
                case 3 -> {
                    System.out.print("Enter a property: ");
                    String property = scanner.nextLine();
                    System.out.print("Enter property's value: ");
                    String value = scanner.nextLine();
                    JSONArray array = cluster.readDocumentsWhere(property, value);
                    System.out.println("\n[");
                    for (int i = 0; i < array.length(); i++) {
                        System.out.print("\n" + array.get(i));
                    }
                    System.out.println("]");
                }
                case 4 -> {
                    cluster.createDocument();
                }
                case 5 -> {
                    System.out.print("Enter document ID: ");
                    String documentId = scanner.nextLine();
                    cluster.deleteDocument(documentId);
                }
                case 6 -> {
                    System.out.print("Enter document ID: ");
                    String documentId = scanner.nextLine();
                    System.out.print("Enter a property: ");
                    String property = scanner.nextLine();
                    System.out.print("Enter a value: ");
                    String value = scanner.nextLine();
                    cluster.createProperty(documentId, property, value);
                }
                case 7 -> {
                    System.out.print("Enter document ID: ");
                    String documentId = scanner.nextLine();
                    System.out.print("Enter a property: ");
                    String property = scanner.nextLine();
                    System.out.print("Enter new value: ");
                    String value = scanner.nextLine();
                    cluster.updateProperty(documentId, property, value);
                }
                case 8 -> {
                    System.out.print("Enter document ID: ");
                    String documentId = scanner.nextLine();
                    System.out.print("Enter a property: ");
                    String property = scanner.nextLine();
                    cluster.deleteProperty(documentId, property);
                }
                case 9 -> {
                    System.out.print("Enter property to be indexed: ");
                    String property = scanner.nextLine();
                    cluster.createIndex(property);
                }
                case 10 -> {
                    System.out.print("Enter property: ");
                    String property = scanner.nextLine();
                    System.out.print("Are you sure ? (y/N)");
                    if (property.equals("y")) {
                        cluster.deleteIndex(property);
                    }
                }
                case 11 -> {
                    System.out.print("Enter database name: ");
                    String database = scanner.nextLine();
                    cluster.createDatabase(database);
                }
                case 12 -> {
                    System.out.print("Are you sure ? (y/N) ");
                    String confirm = scanner.nextLine();
                    if (confirm.equals("y")) {
                        cluster.deleteDatabase();
                    }
                }
                case 13 -> {
                    System.out.print("Enter database name: ");
                    String database = scanner.nextLine();
                    cluster.connectToDatabase(database);
                }
                case 14 -> {
                    System.out.println(cluster.connect() ? "\nWELCOME" : "\nUNAUTHORIZED");
                }
                case 15 -> {
                    cluster.register();
                }
                case 16 -> {
                    System.out.println("Thank you");
                    System.exit(0);
                }
                case 17 -> {
                    System.out.print("Enter document ID: ");
                    String documentId = scanner.nextLine();
                    System.out.print("Enter a property: ");
                    String property = scanner.nextLine();
                    cluster.readProperty(documentId, property);
                }
                default -> {
                    System.out.print("Invalid ");
                    System.out.println("input!");
                }
            }
            System.out.print("press any key to continue...");
            System.in.read();
        }
    }
}










/*System.out.println("hello");
        Cluster cluster = new Cluster();
        cluster.connect();
        cluster.connectToDatabase("first-db");
        cluster.deleteDocument("13");*/
        /*for(int i=0;i<10;i++) {
            Thread thread = new Thread(cluster);
            thread.start();
        }*/

/*cluster.connect();
        cluster.connectToDatabase("first-db");
        for (int i = 0; i < 10; i++) {
        Thread thread = new Thread(cluster);
        thread.start();
        //thread.join();
        }*/