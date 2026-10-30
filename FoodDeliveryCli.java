package com.fooddelivery.cli;
import com.fooddelivery.model.*; import com.fooddelivery.service.FoodDeliveryService;
import java.math.BigDecimal; import java.util.*;
public class FoodDeliveryCli {
    private final FoodDeliveryService service; private final Scanner sc;
    public FoodDeliveryCli(FoodDeliveryService service,Scanner sc){this.service=service;this.sc=sc;}
    public void start(){
        boolean running=true;
        while(running){
            System.out.println("\n========== FOOD DELIVERY APP ==========");
            System.out.println("1. View restaurants and menu");
            System.out.println("2. Register customer");
            System.out.println("3. Add item to cart");
            System.out.println("4. View cart");
            System.out.println("5. Place order and pay");
            System.out.println("6. View orders / update status");
            System.out.println("7. View delivery partners");
            System.out.println("8. Add rating/review");
            System.out.println("9. View payments and reviews");
            System.out.println("0. Exit");
            int ch=readInt("Choose: ");
            try{switch(ch){
                case 1:viewRestaurants();break;
                case 2:register();break;
                case 3:addCart();break;
                case 4:viewCart();break;
                case 5:placeOrder();break;
                case 6:manageOrders();break;
                case 7:viewPartners();break;
                case 8:review();break;
                case 9:viewRecords();break;
                case 0:running=false;System.out.println("Thank you!");break;
                default:System.out.println("Choose a valid option.");
            }}catch(RuntimeException e){System.out.println("Error: "+e.getMessage());}
        }
    }
    private void viewRestaurants(){
        for(Restaurant r:service.restaurants()){
            System.out.println("\nRestaurant ID "+r.getId()+" | "+r.getName()+" | "+r.getCuisine()+" | "+r.getCity()+" | "+(r.isOpen()?"OPEN":"CLOSED")+" | Minimum Rs."+r.getMinimumOrder());
            System.out.printf("%-5s %-22s %-12s %-10s %-12s%n","ID","Item","Category","Price","Availability");
            for(MenuItem i:r.getMenu())System.out.printf("%-5d %-22s %-12s %-10s %-12s%n",i.getId(),i.getName(),i.getCategory(),i.getPrice(),i.isAvailable()?"Available":"Unavailable");
        }
    }
    private void register(){
        String name=read("Name: "),phone=read("Phone: "),email=read("Email: "),address=read("Delivery address: "),city=read("City (e.g. Chennai): ");
        Customer c=service.register(name,phone,email,address,city);System.out.println("Registered customer ID: "+c.getId());
    }
    private void addCart(){
        int cid=readInt("Customer ID: ");viewRestaurants();int rid=readInt("Restaurant ID: ");int iid=readInt("Menu item ID: ");int qty=readInt("Quantity (1-10): ");
        service.addToCart(cid,rid,iid,qty);System.out.println("Item added to cart.");
    }
    private void viewCart(){
        int cid=readInt("Customer ID: ");List<CartItem> items=service.cart(cid);BigDecimal total=BigDecimal.ZERO;
        if(items.isEmpty()){System.out.println("Cart is empty.");return;}
        for(CartItem i:items){System.out.println(i.getItem().getName()+" x "+i.getQuantity()+" = Rs. "+i.getLineTotal());total=total.add(i.getLineTotal());}
        System.out.println("Items total: Rs. "+total);
    }
    private void placeOrder(){
        int cid=readInt("Customer ID: ");System.out.println("Payment: 1.UPI  2.Card  3.Cash on Delivery");
        int p=readInt("Choose payment: ");String method=p==1?"UPI":p==2?"Card":p==3?"Cash on Delivery":"";
        if(method.isEmpty())throw new IllegalArgumentException("Invalid payment method.");
        Order o=service.placeOrder(cid,method);System.out.println("Order placed successfully! Order ID: "+o.getId()+" | Total: Rs. "+o.getTotal()+" | Status: "+o.getStatus());
    }
    private void manageOrders(){
        List<Order> list=service.orders();if(list.isEmpty()){System.out.println("No orders yet.");return;}
        for(Order o:list)System.out.println("Order "+o.getId()+" | "+o.getCustomerName()+" | "+o.getRestaurantName()+" | Rs."+o.getTotal()+" | "+o.getStatus());
        int id=readInt("Order ID to update (0 to go back): ");if(id==0)return;
        System.out.println("1. Advance to next status  0. Cancel (only Placed/Accepted)");
        int action=readInt("Action: ");service.updateStatus(id,action);System.out.println("Order updated: "+service.order(id).getStatus());
    }
    private void viewPartners(){for(DeliveryPartner p:service.partners())System.out.println(p.getId()+" | "+p.getName()+" | "+p.getCity()+" | "+(p.isAvailable()?"Available":"Busy")+" | Earnings Rs."+p.getEarnings());}
    private void review(){int id=readInt("Delivered order ID: ");int rating=readInt("Rating (1-5): ");String comment=read("Comment: ");service.addReview(id,rating,comment);System.out.println("Review saved.");}
    private void viewRecords(){
        System.out.println("--- Payments ---");for(Payment p:service.payments())System.out.println("Order "+p.getOrderId()+" | "+p.getMethod()+" | Rs."+p.getAmount()+" | "+p.getStatus());
        System.out.println("--- Reviews ---");for(Review r:service.reviews())System.out.println("Order "+r.getOrderId()+" | "+r.getRating()+"/5 | "+r.getComment());
    }
    private String read(String p){System.out.print(p);return sc.nextLine().trim();}
    private int readInt(String p){while(true){try{return Integer.parseInt(read(p));}catch(NumberFormatException e){System.out.println("Enter a valid number.");}}}
}
