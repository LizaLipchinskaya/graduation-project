import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Algorithm {

    private Connection connection;

    public Algorithm(Connection connection) {
        this.connection = connection;
    }

    public void doChanges(Long idPupil) {
        clearTableReplacement();

        for (Day day : Day.values()) {
            List<Integer> categoryList = findBannedProduct(idPupil, day);
            findChange(idPupil, categoryList, day);
        }
    }

    /**
     * Нахождение категорий, которые нужно заменить
     *
     * @param idPupil id ученика
     * @param day день недели, в котором нужно найти запрещенные категории блюд
     * @return категории, которые нужно заменить
     */
    public List<Integer> findBannedProduct(Long idPupil, Day day) {
        List<Integer> bannedCategory = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select category from pupil\n" +
                    "inner join pupiltoproduct p on pupil.id = p.id_pupil\n" +
                    "inner join product p2 on p2.id = p.id_product\n" +
                    "inner join dishtoproduct d on p2.id = d.id_product\n" +
                    "inner join dish d2 on d2.id = d.id_dish\n" +
                    "inner join " + day + " m on d2.id = m.id_dish\n" +
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

    /**
     * Нахождение замен по категориям
     *
     * @param idPupil id ученика
     * @param categories номер категории
     */
    public void findChange(Long idPupil, List<Integer> categories, Day day) {
        List<String> firstCourse = new ArrayList<>();
        List<String> garnish = new ArrayList<>();
        List<String> meaty = new ArrayList<>();
        String dayRus = Day.dayChangeString(day);

        for (Integer category : categories) {
            switch (category) {
                case 1 -> firstCourse = findDishes(idPupil, 1, day);
                case 2 -> garnish = findDishes(idPupil, 2, day);
                case 3 -> meaty = findDishes(idPupil, 3, day);
            }
        }

        if (!firstCourse.isEmpty()) {
            StringBuilder message = new StringBuilder("Замените первое блюдо" + dayRus + "на : \n");
            for (int i = 0; i < firstCourse.size() - 1; i++) {
                message.append(" ").append(i + 1).append(" ").append(firstCourse.get(i)).append(",\n");
            }
            message.append(" ").append(firstCourse.size()).append(" ").append(firstCourse.get(firstCourse.size() - 1)).append(";");
            System.out.println(message);
            addData(firstCourse, day);
        }

        if (!garnish.isEmpty()) {
            StringBuilder message = new StringBuilder("Замените гарнир" + dayRus + "на : \n");
            for (int i = 0; i < garnish.size() - 1; i++) {
                message.append(" ").append(i + 1).append(" ").append(garnish.get(i)).append(",\n");
            }
            message.append(" ").append(garnish.size()).append(" ").append(garnish.get(garnish.size() - 1)).append(";");
            System.out.println(message);
            addData(garnish, day);
        }

        if (!meaty.isEmpty()) {
            StringBuilder message = new StringBuilder("Замените основное блюдо" + dayRus + "на : \n");
            for (int i = 0; i < meaty.size() - 1; i++) {
                message.append(" ").append(i + 1).append(" ").append(meaty.get(i)).append(",\n");
            }
            message.append(" ").append(meaty.size()).append(" ").append(meaty.get(meaty.size() - 1)).append(";");
            System.out.println(message);
            addData(meaty, day);
        }
    }

    /**
     * Нахождение блюд по определенной категории
     *
     * @param idPupil id ученика
     * @param category номер категории
     * @return список названий блюд
     */
    private List<String> findDishes(Long idPupil, int category, Day day) {
        List<String> newDish = new ArrayList<>();
        String sqlJoin = "";
        String condition = "";

        //запрещенное блюдо уберется первым join
        switch (day) {
            case MONDAY -> {
                sqlJoin = " left join tuesday t on dish.id = t.id_dish " +
                          " left join wednesday w on dish.id = w.id_dish ";
                condition = " and t.id_dish isnull and w.id_dish isnull ";
            }
            case TUESDAY -> {
                sqlJoin = " left join monday m on dish.id = m.id_dish " +
                          " left join (select id_dish from replacement r " +
                          "            where r.day_of_week in ('monday')) r on dish.id = r.id_dish " +
                          " left join wednesday w on dish.id = w.id_dish " +
                          " left join thursday t on dish.id = t.id_dish ";
                condition = " and m.id isnull and r.id_dish isnull and w.id_dish isnull and t.id_dish isnull";
            }
            case WEDNESDAY -> {
                sqlJoin = " left join monday m on dish.id = m.id_dish " +
                          " left join tuesday t on dish.id = t.id_dish " +
                          " left join (select id_dish from replacement r " +
                          "            where r.day_of_week in ('monday', 'tuesday')) r on dish.id = r.id_dish " +
                          " left join thursday th on dish.id = th.id_dish " +
                          " left join friday f on dish.id = f.id_dish ";
                condition = " and m.id isnull and t.id isnull and r.id_dish isnull and th.id_dish isnull " +
                            " and f.id_dish isnull ";
            }
            case THURSDAY -> {
                sqlJoin = " left join tuesday t on dish.id = t.id_dish " +
                          " left join wednesday w on dish.id = w.id_dish " +
                          " left join (select id_dish from replacement r " +
                          "            where r.day_of_week in ('tuesday', 'wednesday')) r on dish.id = r.id_dish " +
                          " left join friday f on dish.id = f.id_dish ";;
                condition = " and t.id isnull and w.id isnull and r.id_dish isnull and f.id_dish isnull ";
            }
            case FRIDAY -> {
                sqlJoin = " left join wednesday w on dish.id = w.id_dish " +
                          " left join thursday t on dish.id = t.id_dish " +
                          " left join (select id_dish from replacement r " +
                          "            where r.day_of_week in ('wednesday', 'thursday')) r on dish.id = r.id_dish ";
                condition = " and w.id isnull and t.id isnull and r.id_dish isnull ";
            }
        }

        String query = "select name from dish " +
                " left join (select distinct id_dish from pupil " +
                "            inner join pupiltoproduct p on pupil.id = p.id_pupil " +
                "            inner join product p2 on p2.id = p.id_product " +
                "            right join dishtoproduct d on p2.id = d.id_product " +
                "            where pupil.id = " + idPupil + ") t1 on id = t1.id_dish " +
                sqlJoin +
                " where category = " + category + " and t1.id_dish isnull " + condition;

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

    private void clearTableReplacement() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("truncate table replacement");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addData(List<String> dishes, Day day) {
        Scanner scanner = new Scanner(System.in);
        Long idDish = findDishIdByName(dishes.get(scanner.nextInt() - 1));

        try (Statement statement = connection.createStatement()) {
            statement.execute("insert into replacement values ("+ maxIdReplacement() +","+ idDish +",'"+ day +"')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Long maxIdReplacement() {
        Long id = null;
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select max(id) from replacement");

            while (resultSet.next()) {
                id = resultSet.getLong(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private Long findDishIdByName(String name) {
        Long id = null;
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select id from dish\n" +
                                                              "where name = '" + name + "'");
            while (resultSet.next()) {
                id = (long) resultSet.getInt(1);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
}