public class Main {

    public static void main(String[] args) {
        JdbcProvider provider = new JdbcProvider();
        provider.getAlgorithm().doChanges(1L);
        provider.getRightMenu().createNewMenu();
    }
}