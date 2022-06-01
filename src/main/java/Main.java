import java.util.List;

public class Main {

    public static void main(String[] args) {
//        Controller controller = new Controller();
//        Algorithm algorithm = controller.getAlgorithm();
//        System.out.println(algorithm.findBannedProduct(1L));

        Controller controller = new Controller();
        Algorithm algorithm = controller.getAlgorithm();
        List<String> newDish = algorithm.findChange(1L);

        for (String dish : newDish) {
            System.out.println(dish);
        }
    }
}
