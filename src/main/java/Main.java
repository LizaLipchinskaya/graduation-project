import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        JdbcProvider provider = new JdbcProvider();
//        provider.getAlgorithm().doChanges(1L);
//        provider.getRightMenu().createNewMenu(List.of(1L));

        provider.getAlgorithmForPupils().changedMenu(Arrays.asList(1L, 2L, 3L));
        provider.getRightMenu().createNewMenu(Arrays.asList(1L, 2L, 3L));
    }
}