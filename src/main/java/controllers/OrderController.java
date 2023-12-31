package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.Address;
import model.LineItem;
import model.Order;
import model.User;
import utils.Log;

import javax.xml.crypto.Data;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT *,\n" +
            "billing.street_address as billing, shipping.street_address as shipping\n" +
            "from orders\n" +
            "join user on orders.user_id = user.id\n" +
            "Join address as billing on orders.billing_address_id=billing.id\n" +
            "join address as shipping on orders.shipping_address_id=shipping.id where orders.id =" + id;

    return getOrdersCollect(sql);
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Malthe: got rid of nested queries
    String sql2 = "SELECT *,\n" +
            "billing.street_address as billing, shipping.street_address as shipping\n" +
            "from orders\n" +
            "join user on orders.user_id = user.id\n" +
            "Join address as billing on orders.billing_address_id=billing.id\n" +
            "join address as shipping on orders.shipping_address_id=shipping.id";

    ArrayList<Order> orders = new ArrayList<Order>();

    Order order = getOrdersCollect(sql2);

    orders.add(order);

    // return the orders
    return orders;
  }

  private static Order getOrdersCollect(String sql) {
    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);

    Order order = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));

        User user1 = new User(
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getLong("created_at"),
                rs.getString("username"),
                rs.getString("token"));
        user1.setToken(null);
        user1.setPassword(null);

        Address billing = new Address(
                rs.getInt("billing_address_id"),
                rs.getString("name"),
                rs.getString("billing"),
                rs.getString("city"),
                rs.getString("zipcode"));

        Address shipping = new Address(
                rs.getInt("shipping_address_id"),
                rs.getString("name"),
                rs.getString("shipping"),
                rs.getString("city"),
                rs.getString("zipcode"));

        // Create an object instance of order from the database dataa
        order =
                new Order(
                        rs.getInt("id"),
                        user1,
                        lineItems,
                        billing,
                        shipping,
                        rs.getFloat("order_total"),
                        rs.getLong("created_at"),
                        rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    // Returns null
    return order;
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Trying to create a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    try{
      DatabaseController.getConnection().setAutoCommit(false);

      // Save addresses to database and save them back to initial order instance
      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      // Save the user to the database and save them back to initial order instance
      order.setCustomer(UserController.createUser(order.getCustomer()));

      // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts : fixed

      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");

      if(orderID == 0){
        return null;
      }

      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for(LineItem item : order.getLineItems()){
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);

        //Malthe: Commit the order to the database
        DatabaseController.getConnection().commit();
      }

      order.setLineItems(items);

      Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    }catch (SQLException e){
      try{
        //Malthe: Stops the connection to the database
        DatabaseController.getConnection().rollback();
      }catch (SQLException ex){
        ex.printStackTrace();
      }
      Log.writeLog(OrderController.class.getName(), order, "Creation of order failed", 0);

    }
    finally {
      try{
        //Malthe: Set auto commit to true so other sql calls will not fail even though this order fails
        DatabaseController.getConnection().setAutoCommit(true);
      }catch (SQLException e){
        e.printStackTrace();
    }

    }

    // Return order
    return order;
    }



}