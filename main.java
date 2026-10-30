import com.fooddelivery.cli.FoodDeliveryCli;
import com.fooddelivery.service.FoodDeliveryService;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FoodDeliveryService service = new FoodDeliveryService();
        FoodDeliveryCli cli = new FoodDeliveryCli(service, scanner);
        cli.start();
        scanner.close();
    }
}
