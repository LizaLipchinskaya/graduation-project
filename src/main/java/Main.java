public class Main {

    public static void main(String[] args) {
        JdbcProvider provider = new JdbcProvider();
        Algorithm algorithm = provider.getAlgorithm();
        algorithm.doChanges(1L);
    }
}
