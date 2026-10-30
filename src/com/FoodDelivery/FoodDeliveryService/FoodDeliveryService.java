package com.fooddelivery.service;

// 1. IMPORTS
import com.fooddelivery.model.*;
import com.fooddelivery.repository.*;
import com.fooddelivery.payment.*;

import java.math.BigDecimal;
import java.util.*;

// 2. SERVICE CLASS
public class FoodDeliveryService {

    // 3. REPOSITORY OBJECTS
    private final RestaurantRepository restaurants = new RestaurantRepository();
    private final CustomerRepository customers = new CustomerRepository();
    private final OrderRepository orders = new OrderRepository();
    private final PaymentRepository payments = new PaymentRepository();
    private final DeliveryRepository partners = new DeliveryRepository();
    private final ReviewRepository reviews = new ReviewRepository();

    // 4. CART MANAGEMENT
    private final Map<Integer, List<CartItem>> carts = new HashMap<>();
    private final Map<Integer, Integer> cartRestaurant = new HashMap<>();

    // 5. ID GENERATORS
    private int nextRestaurant = 1;
    private int nextItem = 1;
    private int nextCustomer = 1;
    private int nextOrder = 1;

    // 6. CONSTRUCTOR - INITIAL DATA
    public FoodDeliveryService() {

        Restaurant r = new Restaurant(
                nextRestaurant++,
                "Annapoorna",
                "South Indian",
                "Chennai",
                new BigDecimal("100"),
                true
        );

        r.addMenuItem(new MenuItem(nextItem++, "Idli", "Veg", new BigDecimal("40"), true));
        r.addMenuItem(new MenuItem(nextItem++, "Dosa", "Veg", new BigDecimal("80"), true));
        r.addMenuItem(new MenuItem(nextItem++, "Chicken Biryani", "Non-Veg", new BigDecimal("220"), true));
        r.addMenuItem(new MenuItem(nextItem++, "Brownie", "Veg", new BigDecimal("90"), false));

        restaurants.save(r);

        Restaurant r2 = new Restaurant(
                nextRestaurant++,
                "Wok Express",
                "Chinese",
                "Chennai",
                new BigDecimal("150"),
                true
        );

        r2.addMenuItem(new MenuItem(nextItem++, "Veg Fried Rice", "Veg", new BigDecimal("160"), true));
        r2.addMenuItem(new MenuItem(nextItem++, "Noodles", "Veg", new BigDecimal("140"), true));

        restaurants.save(r2);

        partners.save(new DeliveryPartner(1, "Arun", "Chennai"));
        partners.save(new DeliveryPartner(2, "Priya", "Chennai"));
    }

    // 7. RESTAURANT MANAGEMENT

    public List<Restaurant> restaurants() {
        return restaurants.findAll();
    }

    public Restaurant restaurant(int id) {
        Restaurant r = restaurants.findById(id);

        if (r == null) {
            throw new IllegalArgumentException("Restaurant not found.");
        }

        return r;
    }

    // 8. MENU MANAGEMENT

    public MenuItem addMenuItem(
            int restaurantId,
            String name,
            String category,
            BigDecimal price,
            boolean available
    ) {
        MenuItem item = new MenuItem(nextItem++, name, category, price, available);

        restaurant(restaurantId).addMenuItem(item);

        return item;
    }

    // 9. CUSTOMER MANAGEMENT

    public Customer register(
            String name,
            String phone,
            String email,
            String address,
            String city
    ) {
        Customer c = new Customer(
                nextCustomer++,
                name,
                phone,
                email,
                address,
                city
        );

        customers.save(c);
        carts.put(c.getId(), new ArrayList<>());

        return c;
    }

    public List<Customer> customers() {
        return customers.findAll();
    }

    // 10. ADD ITEMS TO CART

    public void addToCart(
            int customerId,
            int restaurantId,
            int itemId,
            int quantity
    ) {

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        if (quantity > 10) {
            throw new IllegalArgumentException("Maximum 10 units of one item.");
        }

        Customer c = customer(customerId);
        Restaurant r = restaurant(restaurantId);

        if (!c.getCity().equalsIgnoreCase(r.getCity())) {
            throw new IllegalArgumentException(
                    "Delivery address must be in the restaurant's city."
            );
        }

        if (!r.isOpen()) {
            throw new IllegalArgumentException("Restaurant is currently closed.");
        }

        if (cartRestaurant.containsKey(customerId)
                && cartRestaurant.get(customerId) != restaurantId) {

            throw new IllegalArgumentException(
                    "Cart can contain items from one restaurant only. Clear cart first."
            );
        }

        MenuItem item = r.findItem(itemId);

        if (item == null) {
            throw new IllegalArgumentException("Menu item not found.");
        }

        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Item is unavailable.");
        }

        List<CartItem> cart = carts.get(customerId);

        for (CartItem ci : cart) {

            if (ci.getItem().getId() == itemId) {

                if (ci.getQuantity() + quantity > 10) {
                    throw new IllegalArgumentException(
                            "Maximum 10 units of one item."
                    );
                }

                ci.setQuantity(ci.getQuantity() + quantity);
                cartRestaurant.put(customerId, restaurantId);

                return;
            }
        }

        cart.add(new CartItem(item, quantity));
        cartRestaurant.put(customerId, restaurantId);
    }

    // 11. VIEW CART

    public List<CartItem> cart(int customerId) {

        customer(customerId);

        return new ArrayList<>(carts.get(customerId));
    }

    // 12. CLEAR CART

    public void clearCart(int customerId) {

        customer(customerId);

        carts.get(customerId).clear();
        cartRestaurant.remove(customerId);
    }

    // 13. PLACE ORDER

    public Order placeOrder(int customerId, String paymentName) {

        Customer c = customer(customerId);
        List<CartItem> cart = carts.get(customerId);

        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        Restaurant r = restaurant(cartRestaurant.get(customerId));

        if (!r.isOpen()) {
            throw new IllegalStateException("Restaurant is closed.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem ci : cart) {

            if (!ci.getItem().isAvailable()) {
                throw new IllegalStateException(
                        ci.getItem().getName() + " became unavailable."
                );
            }

            subtotal = subtotal.add(ci.getLineTotal());
        }

        if (subtotal.compareTo(r.getMinimumOrder()) < 0) {
            throw new IllegalStateException(
                    "Minimum order value is Rs. " + r.getMinimumOrder()
            );
        }

        long active = orders.findAll().stream()
                .filter(o -> o.getCustomerId() == customerId
                        && o.getStatus() != Order.Status.DELIVERED
                        && o.getStatus() != Order.Status.CANCELLED)
                .count();

        if (active >= 3) {
            throw new IllegalStateException(
                    "Customer cannot have more than 3 active orders."
            );
        }

        // DELIVERY CHARGE
        BigDecimal delivery = subtotal.compareTo(new BigDecimal("500")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("40");

        // GST CALCULATION
        BigDecimal gst = subtotal.multiply(new BigDecimal("0.05"));

        // TOTAL AMOUNT
        BigDecimal total = subtotal.add(delivery)
                .add(gst)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // COD VALIDATION
        if (paymentName.equals("Cash on Delivery")
                && total.compareTo(new BigDecimal("1500")) > 0) {

            throw new IllegalArgumentException(
                    "COD allowed only for totals up to Rs. 1500."
            );
        }

        // PAYMENT METHOD SELECTION
        PaymentMethod method = paymentName.equals("UPI")
                ? new UPIPayment()
                : paymentName.equals("Card")
                ? new CardPayment()
                : new CODPayment();

        if (!method.pay(total)) {
            throw new IllegalStateException(
                    "Payment failed; order was not placed."
            );
        }

        // CREATE ORDER
        Order order = new Order(
                nextOrder++,
                c.getId(),
                c.getName(),
                r.getId(),
                r.getName(),
                cart,
                subtotal,
                total
        );

        // SAVE PAYMENT
        if (!paymentName.equals("Cash on Delivery")) {
            payments.save(
                    new Payment(order.getId(), total, paymentName, "PAID")
            );
        }

        orders.save(order);

        clearCart(customerId);

        return order;
    }

    // 14. ORDER MANAGEMENT

    public List<Order> orders() {
        return orders.findAll();
    }

    public Order order(int id) {

        Order o = orders.findById(id);

        if (o == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        return o;
    }

    // 15. UPDATE ORDER STATUS

    public void updateStatus(int id, int action) {

        Order o = order(id);

        Order.Status[] flow = {
                Order.Status.PLACED,
                Order.Status.ACCEPTED,
                Order.Status.PREPARING,
                Order.Status.READY_FOR_PICKUP,
                Order.Status.OUT_FOR_DELIVERY,
                Order.Status.DELIVERED
        };

        // CANCEL ORDER
        if (action == 0) {

            if (o.getStatus() != Order.Status.PLACED
                    && o.getStatus() != Order.Status.ACCEPTED) {

                throw new IllegalStateException(
                        "Order can only be cancelled while Placed or Accepted."
                );
            }

            o.setStatus(Order.Status.CANCELLED);
            return;
        }

        // STATUS TRANSITION
        int idx = -1;

        for (int i = 0; i < flow.length; i++) {

            if (flow[i] == o.getStatus()) {
                idx = i;
            }
        }

        if (idx < 0 || idx + 1 >= flow.length || action != 1) {
            throw new IllegalStateException("Invalid status transition.");
        }

        o.setStatus(flow[idx + 1]);

        // DELIVERY PARTNER ALLOCATION
        if (o.getStatus() == Order.Status.READY_FOR_PICKUP) {

            for (DeliveryPartner p : partners.findAll()) {

                if (p.isAvailable()
                        && p.getCity().equalsIgnoreCase(
                                restaurant(o.getRestaurantId()).getCity()
                        )) {

                    p.setAvailable(false);
                    break;
                }
            }
        }

        // DELIVERY COMPLETION
        if (o.getStatus() == Order.Status.DELIVERED) {

            for (DeliveryPartner p : partners.findAll()) {

                if (!p.isAvailable()) {
                    p.setAvailable(true);
                    p.addEarning(30);
                    break;
                }
            }

            Payment p = payments.findByOrderId(id);

            if (p == null) {
                payments.save(
                        new Payment(id, o.getTotal(), "Cash on Delivery", "PAID")
                );
            }
        }
    }

    // 16. REVIEW MANAGEMENT

    public void addReview(int orderId, int rating, String comment) {

        Order o = order(orderId);

        if (o.getStatus() != Order.Status.DELIVERED) {
            throw new IllegalStateException(
                    "Only delivered orders can be reviewed."
            );
        }

        reviews.save(new Review(orderId, rating, comment));
    }

    // 17. PAYMENT MANAGEMENT

    public List<Payment> payments() {
        return payments.findAll();
    }

    // 18. REVIEW LIST

    public List<Review> reviews() {
        return reviews.findAll();
    }

    // 19. DELIVERY PARTNER LIST

    public List<DeliveryPartner> partners() {
        return partners.findAll();
    }

    // 20. PRIVATE CUSTOMER VALIDATION

    private Customer customer(int id) {

        Customer c = customers.findById(id);

        if (c == null) {
            throw new IllegalArgumentException("Customer not found.");
        }

        return c;
    }
}