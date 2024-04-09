package Contexts.Product.Infrastructure.SQL;
import Contexts.Product.Domain.Product;
import Contexts.Product.Domain.ProductType;
import Contexts.Product.Domain.ProductsRepository;
import Contexts.Ticket.Domain.Ticket;

import java.util.List;
import Infrastructure.Connections.MySQLConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static Infrastructure.Connections.MySQLConnection.getMySQLDatabase;

public class ProductRepositorySQL implements ProductsRepository {
    private MySQLConnection mySQLConnection;

    public ProductRepositorySQL(MySQLConnection mySQLConnection) {
        this.mySQLConnection = mySQLConnection;
    }

    @Override
    public void initialize() {

    }

    @Override
    public Product getProduct(int id) {
        Product product = null;
        try{
            Connection conn = getMySQLDatabase();
            PreparedStatement statement = conn.prepareStatement(QueriesSQL.SQL_SELECT_PRODUCT);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                product = new Product(
                        rs.getInt("idproduct"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        ProductType.valueOf(rs.getString("type")),
                        rs.getString("attribute"));
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return product;
    }

    @Override
    public Product getLastProduct() {
        Product lastProduct = null;
        try {
            Connection conn = getMySQLDatabase();
            PreparedStatement statement = conn.prepareStatement(QueriesSQL.SQL_SELECT_LAST_PRODUCT);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                lastProduct = new Product(rs.getInt("idproduct"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        ProductType.valueOf(rs.getString("type")),
                        rs.getString("attribute"));
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        //TODO Añadir excepcion personalizada si el producto es null
        return lastProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        try {
            Connection conn = getMySQLDatabase();
            PreparedStatement stmt = conn.prepareStatement(QueriesSQL.SQL_SELECT);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("idproduct"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        ProductType.valueOf(rs.getString("type")),
                        rs.getString("attribute"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return products;
    }


    @Override
    public List<Product> getFlowers() {
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getType() == ProductType.FLOWER)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getTrees() {
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getType() == ProductType.TREE)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getDecorations() {
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getType() == ProductType.DECORATION)
                .collect(Collectors.toList());
    }

    @Override
    public void addPrimaryStock() {
        Object[][] allData = {
                {"Rosa", "Roja", ProductType.FLOWER},
                {"Girasol", "Blanca", ProductType.FLOWER},
                {"Amapola", "Roja", ProductType.FLOWER},
                {"Lirio", "Naranja", ProductType.FLOWER},
                {"Clavel", "Amarillo", ProductType.FLOWER},
                {"Manzano", 1.5, ProductType.TREE},
                {"Olivo", 2.0, ProductType.TREE},
                {"Pino", 3.0, ProductType.TREE},
                {"Rosal", 0.5, ProductType.TREE},
                {"Jarron", "Madera", ProductType.DECORATION},
                {"Tiesto", "Plastico", ProductType.DECORATION},
                {"Jarron", "Plastico", ProductType.DECORATION},
                {"Tiesto", "Madera", ProductType.DECORATION}
        };

        try {
            Connection connection = mySQLConnection.getMySQLDatabase();
            PreparedStatement productStatement = connection.prepareStatement(QueriesSQL.SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

            for (Object[] rowData : allData) {
                String name = (String) rowData[0];
                ProductType productType = (ProductType) rowData[2];

                productStatement.setString(1, name);
                productStatement.setInt(2, 0);
                productStatement.setDouble(3, 0.0);
                productStatement.setString(4, productType.name());
                productStatement.executeUpdate();

                ResultSet generatedKeys = productStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int productId = generatedKeys.getInt(1);
                    String specificAttribute = (productType == ProductType.TREE) ? "height" :
                            ((productType == ProductType.FLOWER) ? "color" : "material");

                    String formattedQuery = String.format(QueriesSQL.SQL_INSERT_ATTRIBUTE, productType.name().toLowerCase(), specificAttribute);
                    try (PreparedStatement specificDataStatement = connection.prepareStatement(formattedQuery)) {
                        specificDataStatement.setInt(1, productId);
                        if (rowData[1] instanceof Double) {
                            specificDataStatement.setDouble(2, (Double) rowData[1]);
                        } else {
                            specificDataStatement.setString(2, (String) rowData[1]);
                        }
                        specificDataStatement.executeUpdate();
                    }
                }
            }
            System.out.println("Primary Stock was added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProduct(Product product) {
        try {
            Connection conn = getMySQLDatabase();
            PreparedStatement stmt = conn.prepareStatement(QueriesSQL.SQL_UPDATE);
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getQuantity());
            stmt.setDouble(3, product.getPrice());
            stmt.setString(4, product.getType().toString());
            stmt.setInt(5, product.getProductId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public void deleteProduct(Product product) {
        try {
            Connection conn = getMySQLDatabase();
            PreparedStatement stmt = conn.prepareStatement(QueriesSQL.SQL_DELETE);
            stmt.setInt(1, product.getProductId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    public void addProduct(Product product) {
        try {
            Connection connection = mySQLConnection.getMySQLDatabase();
            PreparedStatement productStatement = connection.prepareStatement(QueriesSQL.SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

            productStatement.setString(1, product.getName());
            productStatement.setInt(2, product.getQuantity());
            productStatement.setDouble(3, product.getPrice());
            productStatement.setString(4, product.getType().name());
            productStatement.executeUpdate();

            ResultSet generatedKeys = productStatement.getGeneratedKeys();
            int productId = -1;
            if (generatedKeys.next()) {
                productId = generatedKeys.getInt(1);
            }

            String specificAttribute = (product.getType() == ProductType.TREE) ? "height" :
                    ((product.getType() == ProductType.FLOWER) ? "color" : "material");

                String formattedQuery = String.format(QueriesSQL.SQL_INSERT_ATTRIBUTE, product.getType().name().toLowerCase(), specificAttribute);
                try (PreparedStatement specificDataStatement = connection.prepareStatement(formattedQuery)) {
                    specificDataStatement.setInt(1, productId);
                    specificDataStatement.setString(2, product.getAttributes().toString());
                    specificDataStatement.executeUpdate();
                }

            System.out.println("Product was added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
