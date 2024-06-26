import Contexts.Product.Domain.ProductsRepository;
import Contexts.Product.Infrastructure.MongoDB.ProductRepositoryMongoDB;
import Contexts.Product.Infrastructure.SQL.ProductRepositorySQL;
import Contexts.Ticket.Domain.TicketRepository;
import Contexts.Ticket.Infrastructure.MongoDB.TicketRepositoryMongoDB;
import Contexts.Ticket.Infrastructure.SQL.TicketRepositorySQL;
import FlowerStore.FlowerStore;
import Infrastructure.Connections.MongoDBConnection;
import Infrastructure.Connections.MySQLConnection;
import Utils.InputControl.InputControl;
import org.apache.commons.lang3.tuple.Pair;

public class Demo implements Runnable {

    private static FlowerStore flowerStore;
    private MongoDBConnection mongoDBConnection;
    private MySQLConnection mySQLConnection;
    private static ProductsRepository productsRepository;
    private static TicketRepository ticketRepository;


    public Demo() {
        String flowerName = nameStore();
        Pair<ProductsRepository, TicketRepository> repositories = configureRepository(flowerName);
        productsRepository = repositories.getLeft();
        ticketRepository = repositories.getRight();
        flowerStore = FlowerStore.getInstance(productsRepository, ticketRepository, flowerName);
    }

    @Override
    public void run() {
        productsRepository.initialize();
        menu();
    }

    public String nameStore() {
        return InputControl.readString("Name FlowerShop: ");
    }

    public Pair<ProductsRepository, TicketRepository> configureRepository(String nameStore) {
        String userDatabase = InputControl.readString("Select the database you would like to work with (MySQL or MongoDB)");
        Pair<ProductsRepository, TicketRepository> repositories;
        if (userDatabase.equalsIgnoreCase("MongoDB")) {
            MongoDBConnection mongoDBConnection = new MongoDBConnection(nameStore);
            productsRepository = new ProductRepositoryMongoDB(mongoDBConnection);
            ticketRepository = new TicketRepositoryMongoDB(mongoDBConnection, flowerStore);
            repositories = Pair.of(productsRepository, ticketRepository);
        } else if (userDatabase.equalsIgnoreCase("MySQL")) {
            MySQLConnection mySQLConnection = new MySQLConnection(nameStore);
            productsRepository = new ProductRepositorySQL(mySQLConnection, nameStore);
            ticketRepository = new TicketRepositorySQL(mySQLConnection);
            repositories = Pair.of(productsRepository, ticketRepository);
        } else {
            System.err.println("This database type is not valid");
            repositories = configureRepository(nameStore);
        }
        return repositories;
    }


    private int showMenu() {
        final int MAX_OPTIONS = 10;

        int selectAction = InputControl.readIntInRange(
                "\nType of action \n" +
                        "1. Create a product. \n" +
                        "2. Show all products.\n" +
                        "3. Update stock. \n" +
                        "4. Remove a product.\n" +
                        "5. Show per product stock. \n" +
                        "6. Show flower shop value. \n" +
                        "7. Create ticket. \n" +
                        "8. Show all tickets. \n" +
                        "9. Show flower shop benefits. \n" +
                        "10. Exit flower shop.", MAX_OPTIONS);

        return selectAction;
    }
    public void menu() {

        System.out.println("What action would you like to perform?");
        do {

            int selectAction = showMenu();

            switch (selectAction) {
                case 1:
                    flowerStore.addProduct();
                    break;
                case 2:
                    flowerStore.showAllProducts();
                    break;
                case 3:
                    flowerStore.updateStock();
                    break;
                case 4:
                    flowerStore.deleteProduct();
                    break;
                case 5:
                    flowerStore.showTypeProducts();
                    break;
                case 6:
                    flowerStore.totalValue();
                    break;
                case 7:
                    flowerStore.addProductsToTicket();
                    break;
                case 8:
                    flowerStore.showAllTickets();
                    break;
                case 9:
                    flowerStore.shopBenefits();
                    break;
                case 10:
                    exit();
                    break;
            }
        } while (true);
    }
    private void exit() {
        System.out.println("\n** GoodBye! **\n");
        System.exit(0);
        mongoDBConnection.disconnectMongo();
        mySQLConnection.disconnectMySQL();
    }
}

