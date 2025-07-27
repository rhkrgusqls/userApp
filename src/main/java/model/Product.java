package model;

public class Product {
    private int productID;
    private String productName;
    private int productStock;
    private int productPrice;

    public Product() {}

    public Product(int productID, String productName, int productStock, int productPrice) {
        this.productID = productID;
        this.productName = productName;
        this.productStock = productStock;
        this.productPrice = productPrice;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductStock() {
        return productStock;
    }

    public void setProductStock(int productStock) {
        this.productStock = productStock;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }
} 