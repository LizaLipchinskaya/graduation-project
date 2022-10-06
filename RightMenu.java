import de.vandermeer.asciitable.AsciiTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RightMenu {

    private Connection connection;

    public RightMenu(Connection connection) {
        this.connection = connection;
    }

    public void createNewMenu() {
        List<String[]> menu = compareMenu();

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

    private List<String[]> compareMenu() {
        List<String[]> rightMenu = new ArrayList<>();
        int i = 0;
        for (Day day : Day.values()) {
            String[] rightDayMenu = new String[4];
            rightDayMenu[0] = Day.dayRus(day);
            List<String[]> changedDishes = findCategoryAndNameDish(day);

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

    private List<String[]> findCategoryAndNameDish(Day day) {
        List<String[]> listDishNameAndCategory = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select category, name from replacement " +
                                                              " join dish d on d.id = replacement.id_dish " +
                                                              " where day_of_week = '"+ day +"' " +
                                                              " order by category");
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
            ResultSet resultSet = statement.executeQuery("select name from "+ day +" " +
                                                              " join dish d on d.id = "+ day +".id_dish " +
                                                              " where category = "+ category +" ");

            while (resultSet.next()) {
                name = resultSet.getString(1);
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
}
