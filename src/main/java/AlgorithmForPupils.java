import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmForPupils {

    private final Connection connection;

    public AlgorithmForPupils(Connection connection) {
        this.connection = connection;
    }

    public void changedMenu(List<Long> pupilIds) {
        if (pupilIds == null || pupilIds.isEmpty()) {
            throw new IllegalArgumentException("Необходимо ввести хотя бы одного ученика");
        }

        clearTableReplacement();

        for (Day day : Day.values()) {
            for (int i = 1; i < 4; i++) { //для конкретного блюда
                List<Long> pupilsNeedChangeDish = new ArrayList<>();
                for (Long pupilId : pupilIds) {
                    try (Statement statement = connection.createStatement()) {
                        ResultSet resultSet = statement.executeQuery(
                                "select pupil.id from pupils pupil"
                                        + " join pupils_products pp on pupil.id = pp.pupil_id"
                                        + " join products product on product.id = pp.product_id"
                                        + " join dishes_products dp on product.id = dp.product_id"
                                        + " join dishes dish on dish.id = dp.dish_id"
                                        + " join " + day + " d on dish.id = d.id_dish"
                                        + " where pupil.id = " + pupilId + " and dish.category = " + i);
                        while (resultSet.next()) {
                            pupilsNeedChangeDish.add(resultSet.getLong(1));
                        }

                        resultSet.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                String condition = "";
                String join = "";
                switch (day) {
                    case MONDAY -> {
                        join = " left join tuesday t on dishes.id = t.id_dish "
                                + " left join wednesday w on dishes.id = w.id_dish ";
                        condition = " and t.id_dish isnull and w.id_dish isnull ";
                    }
                    case TUESDAY -> {
                        join = " left join monday m on dishes.id = m.id_dish "
                                + " left join (select dish_id from replacement r "
                                + "            where r.day_of_week in ('MONDAY')) r on dishes.id = r.dish_id "
                                + " left join wednesday w on dishes.id = w.id_dish "
                                + " left join thursday t on dishes.id = t.id_dish ";
                        condition = " and m.id isnull and r.dish_id isnull and w.id_dish isnull and t.id_dish isnull";
                    }
                    case WEDNESDAY -> {
                        join = " left join monday m on dishes.id = m.id_dish "
                                + " left join tuesday t on dishes.id = t.id_dish "
                                + " left join (select dish_id from replacement r "
                                + "            where r.day_of_week in ('MONDAY', 'TUESDAY')) r on dishes.id = r.dish_id "
                                + " left join thursday th on dishes.id = th.id_dish "
                                + " left join friday f on dishes.id = f.id_dish ";
                        condition = " and m.id isnull and t.id isnull and r.dish_id isnull and th.id_dish isnull "
                                + " and f.id_dish isnull ";
                    }
                    case THURSDAY -> {
                        join = " left join tuesday t on dishes.id = t.id_dish "
                                + " left join wednesday w on dishes.id = w.id_dish "
                                + " left join (select dish_id from replacement r "
                                + "            where r.day_of_week in ('TUESDAY', 'WEDNESDAY')) r on dishes.id = r.dish_id "
                                + " left join friday f on dishes.id = f.id_dish ";
                        condition = " and t.id isnull and w.id isnull and r.dish_id isnull and f.id_dish isnull ";
                    }
                    case FRIDAY -> {
                        join = " left join wednesday w on dishes.id = w.id_dish "
                                + " left join thursday t on dishes.id = t.id_dish "
                                + " left join (select dish_id from replacement r "
                                + "            where r.day_of_week in ('WEDNESDAY', 'THURSDAY')) r on dishes.id = r.dish_id ";
                        condition = " and w.id isnull and t.id isnull and r.dish_id isnull ";
                    }
                }

                replacementAlgorithm(pupilsNeedChangeDish, day, i, join, condition);
            }
        }
    }

    private void replacementAlgorithm(List<Long> pupilIds, Day day, int category, String join, String condition) {
        Map<Long, List<Long>> suitableDishes = new HashMap<>();

        do {
            for (Long pupilId : pupilIds) {
                List<Long> dishIds = new ArrayList<>();
                String query = "select dishes.id from dishes "
                        + " left join (select distinct dish_id from pupils "
                        + "            inner join pupils_products p on pupils.id = p.pupil_id "
                        + "            inner join products p2 on p2.id = p.product_id "
                        + "            right join dishes_products d on p2.id = d.product_id "
                        + "            where pupils.id = " + pupilId + ") t1 on dishes.id = t1.dish_id "
                        + join
                        + " where category = " + category + " and t1.dish_id isnull "
                        + condition;

                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(query);

                    while (resultSet.next()) {
                        dishIds.add(resultSet.getLong(1));
                    }

                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (Long dishId : dishIds) {
                    if (suitableDishes.containsKey(dishId)) {
                        List<Long> pupils = new ArrayList<>(suitableDishes.get(dishId));
                        pupils.add(pupilId);
                        suitableDishes.put(dishId, pupils);
                    } else {
                        suitableDishes.put(dishId, Arrays.asList(pupilId));
                    }
                }
            }

            int maxRang = 0;
            Long dishId = 0L;
            List<Long> pupils = new ArrayList<>();
            for (Map.Entry<Long, List<Long>> entry : suitableDishes.entrySet()) {
                if (entry.getValue().size() > maxRang) {
                    maxRang = entry.getValue().size();
                    dishId = entry.getKey();
                    pupils = entry.getValue();
                }
            }

            for (Long pupilId : pupils) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("insert into replacement values ("+ maxIdReplacement() +","+ dishId +",'"+ day +"'," + pupilId + ")");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (Long id : pupilIds) {
                    if (id.equals(pupilId)) {
                        pupilIds.remove(id);
                        break;
                    }
                }
            }

            suitableDishes.clear();
        } while (!pupilIds.isEmpty());
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

    private void clearTableReplacement() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("truncate table replacement");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
