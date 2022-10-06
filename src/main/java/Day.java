public enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY;

    public static String dayChangeString(Day day) {
        String dayChange = "";

        switch (day) {
            case MONDAY -> dayChange = " в понедельник ";
            case TUESDAY -> dayChange = " во вторник ";
            case WEDNESDAY -> dayChange = " в среду ";
            case THURSDAY -> dayChange = " в четверг ";
            case FRIDAY -> dayChange = " в пятницу ";
        }
        return dayChange;
    }

    public static String dayRus(Day day) {
        String dayRus = "";

        switch (day) {
            case MONDAY -> dayRus = "Понедельник";
            case TUESDAY -> dayRus = "Вторник";
            case WEDNESDAY -> dayRus = "Среда";
            case THURSDAY -> dayRus = "Четверг";
            case FRIDAY -> dayRus = "Пятница";
        }
        return dayRus;
    }
}
