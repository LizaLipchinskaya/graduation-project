import de.vandermeer.asciitable.AsciiTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RightMenu {

    private final Connection connection;

    public RightMenu(Connection connection) {
        this.connection = connection;
    }

    public void createNewMenu(List<Long> pupilIds) {
        for (Long pupilId : pupilIds) {
            printPupil(pupilId);

            List<String[]> menu = compareMenu(pupilId);

            AsciiTable table = new AsciiTable();
            table.addRule();
            String[] monday = menu.get(0);
            String[] tuesday = menu.get(1);
            String[] wednesday = menu.get(2);
            String[] thursday = menu.get(3);
            String[] friday = menu.get(4);
            for (int i = 0; i < monday.length; i++) {
                table.addRow(monday[i], tuesday[i], wednesday[i], thursday[i], friday[i]);
                table.addRule();
            }
            System.out.println(table.render());
        }
    }

    private List<String[]> compareMenu(Long pupilId) {
        List<String[]> rightMenu = new ArrayList<>();
        int i = 0;
        for (Day day : Day.values()) {
            String[] rightDayMenu = new String[4];
            rightDayMenu[0] = Day.dayRus(day);
            List<String[]> changedDishes = findCategoryAndNameDish(day, pupilId);

            int j = 1;
            while (j <= 3) {
                String[] categories = new String[2];
                if (!changedDishes.isEmpty()) {
                    categories = changedDishes.get(0);
                }

                if (!changedDishes.isEmpty() && Integer.parseInt(categories[0]) == j) {
                    String[] dish = changedDishes.get(0);
                    rightDayMenu[j] = dish[1];
                } else {
                    rightDayMenu[j] = getDishName(day, j);
                }
                j++;
            }

            rightMenu.add(i, rightDayMenu);
            i++;
        }
        return rightMenu;
    }

    private List<String[]> findCategoryAndNameDish(Day day, Long pupilId) {
        List<String[]> listDishNameAndCategory = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select category, name from replacement "
                                                           + " join dishes d on d.id = replacement.dish_id "
                                                           + " where day_of_week = '"+ day +"' and pupil_id = " + pupilId
                                                           + " order by category"
            );
            while (resultSet.next()) {
                String[] row = {
                        String.valueOf(resultSet.getInt(1)),
                        resultSet.getString(2)
                };
                listDishNameAndCategory.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listDishNameAndCategory;
    }

    private String getDishName(Day day, int category) {
        String name = "";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select name from "+ day +" "
                                                           + " join dishes d on d.id = "+ day +".id_dish "
                                                           + " where category = "+ category +" "
            );

            while (resultSet.next()) {
                name = resultSet.getString(1);
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    private void printPupil(Long pupilId) {
        String[] pupilCredential = new String[2];
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select first_name, last_name from pupils "
                                                                + "where id = " + pupilId);

            while (resultSet.next()) {
                pupilCredential[0] = resultSet.getString(1);
                pupilCredential[1] = resultSet.getString(2);
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(pupilCredential[1] + ' ' + pupilCredential[0]);
    }
}