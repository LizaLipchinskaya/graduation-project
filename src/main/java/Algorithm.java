import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Algorithm {

    Connection connection;

    public Algorithm(Connection connection) {
        this.connection = connection;
    }

    public List<Integer> findBannedProduct(Long idPupil) {
        List<Integer> bannedCategory = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select category from pupil\n" +
                    "inner join pupiltoproduct p on pupil.id = p.id_pupil\n" +
                    "inner join product p2 on p2.id = p.id_product\n" +
                    "inner join dishtoproduct d on p2.id = d.id_product\n" +
                    "inner join dish d2 on d2.id = d.id_dish\n" +
                    "inner join menu m on d2.id = m.id_dish\n" +
                    "where pupil.id = " + idPupil);

            while (resultSet.next()) {
                bannedCategory.add(resultSet.getInt(1));
            }

            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bannedCategory;
    }

    public List<String> findChange(Long idPupil) {
        List<String> newDish = new ArrayList<>();
        List<Integer> categoryList = findBannedProduct(idPupil);

        String query = switch (categoryList.size()) {
            case 1 -> "select distinct d2.name from pupil\n" +
                    "inner join pupiltoproduct p on pupil.id = p.id_pupil\n" +
                    "inner join product p2 on p2.id = p.id_product\n" +
                    "right join dishtoproduct d on p2.id = d.id_product\n" +
                    "inner join dish d2 on d2.id = d.id_dish\n" +
                    "where (pupil.id != " + idPupil + " or pupil.id isnull) and category = " + categoryList.get(0);
            case 2 -> "select distinct d2.name from pupil\n" +
                    "inner join pupiltoproduct p on pupil.id = p.id_pupil\n" +
                    "inner join product p2 on p2.id = p.id_product\n" +
                    "right join dishtoproduct d on p2.id = d.id_product\n" +
                    "inner join dish d2 on d2.id = d.id_dish\n" +
                    "where (pupil.id != " + idPupil + " or pupil.id isnull) and " +
                    "(category = " + categoryList.get(0) + " or category = " + categoryList.get(1) + ")";
            case 3 -> "select distinct d2.name from pupil\n" +
                    "inner join pupiltoproduct p on pupil.id = p.id_pupil\n" +
                    "inner join product p2 on p2.id = p.id_product\n" +
                    "right join dishtoproduct d on p2.id = d.id_product\n" +
                    "inner join dish d2 on d2.id = d.id_dish\n" +
                    "where (pupil.id != " + idPupil + " or pupil.id isnull) and " +
                    "(category = " + categoryList.get(0) + " or category = " + categoryList.get(1) +
                    " or category = " + categoryList.get(2) + ")";
            default -> "";
        };

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                newDish.add(resultSet.getString(1));
            }

            resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return newDish;
    }
}
