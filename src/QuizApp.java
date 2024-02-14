import java.sql.*;
import java.util.Scanner;

public class QuizApp {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/quizdb", "your_username", "your_password")) {

            initializeDatabase(connection);

            while (true) {
                System.out.println("1. Создать квиз");
                System.out.println("2. Редактировать квиз");
                System.out.println("3. Удалить квиз");
                System.out.println("4. Вывести все квизы");
                System.out.println("5. Пройти квиз");
                System.out.println("6. Выйти");

                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        createQuiz(connection, scanner);
                        break;
                    case 2:
                        editQuiz(connection, scanner);
                        break;
                    case 3:
                        deleteQuiz(connection, scanner);
                        break;
                    case 4:
                        displayAllQuizzes(connection);
                        break;
                    case 5:
                        takeQuiz(connection, scanner);
                        break;
                    case 6:
                        System.exit(0);
                    default:
                        System.out.println("Некорректный выбор. Попробуйте еще раз.");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS quizzes (" +
                    "id SERIAL PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "questions VARCHAR[], " +
                    "answers VARCHAR[], " +
                    "correct_answers VARCHAR[])");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_results (" +
                    "id SERIAL PRIMARY KEY, " +
                    "quiz_id INTEGER REFERENCES quizzes(id), " +
                    "user_name VARCHAR(255) NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    private static void createQuiz(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Введите название квиза:");
        String title = scanner.nextLine();

        System.out.println("Введите вопросы (разделите их точкой с запятой):");
        String[] questions = scanner.nextLine().split(";");

        System.out.println("Введите варианты ответов (разделите их точкой с запятой):");
        String[] answers = scanner.nextLine().split(";");

        System.out.println("Введите правильные ответы (разделите их точкой с запятой):");
        String[] correctAnswers = scanner.nextLine().split(";");

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO quizzes (title, questions, answers, correct_answers) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, title);
            preparedStatement.setArray(2, connection.createArrayOf("VARCHAR", questions));
            preparedStatement.setArray(3, connection.createArrayOf("VARCHAR", answers));
            preparedStatement.setArray(4, connection.createArrayOf("VARCHAR", correctAnswers));

            preparedStatement.executeUpdate();

            System.out.println("Квиз успешно создан.");
        }
    }

    private static void editQuiz(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Введите ID квиза, который вы хотите отредактировать:");
        int quizId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM quizzes WHERE id = ?")) {
            preparedStatement.setInt(1, quizId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Введите новое название квиза:");
                String title = scanner.nextLine();

                System.out.println("Введите новые вопросы (разделите их точкой с запятой):");
                String[] questions = scanner.nextLine().split(";");

                System.out.println("Введите новые варианты ответов (разделите их точкой с запятой):");
                String[] answers = scanner.nextLine().split(";");

                System.out.println("Введите новые правильные ответы (разделите их точкой с запятой):");
                String[] correctAnswers = scanner.nextLine().split(";");

                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE quizzes SET title = ?, questions = ?, answers = ?, correct_answers = ? WHERE id = ?")) {
                    updateStatement.setString(1, title);
                    updateStatement.setArray(2, connection.createArrayOf("VARCHAR", questions));
                    updateStatement.setArray(3, connection.createArrayOf("VARCHAR", answers));
                    updateStatement.setArray(4, connection.createArrayOf("VARCHAR", correctAnswers));
                    updateStatement.setInt(5, quizId);

                    updateStatement.executeUpdate();

                    System.out.println("Квиз успешно отредактирован.");
                }
            } else {
                System.out.println("Квиз с указанным ID не найден.");
            }
        }
    }

    private static void deleteQuiz(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Введите ID квиза, который вы хотите удалить:");
        int quizId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM quizzes WHERE id = ?")) {
            preparedStatement.setInt(1, quizId);

            int rowCount = preparedStatement.executeUpdate();

            if (rowCount > 0) {
                System.out.println("Квиз успешно удален.");
            } else {
                System.out.println("Квиз с указанным ID не найден.");
            }
        }
    }

    private static void displayAllQuizzes(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM quizzes")) {

            while (resultSet.next()) {
                int quizId = resultSet.getInt("id");
                String title = resultSet.getString("title");
                Array questionsArray = resultSet.getArray("questions");
                Array answersArray = resultSet.getArray("answers");
                Array correctAnswersArray = resultSet.getArray("correct_answers");

                String[] questions = (String[]) questionsArray.getArray();
                String[] answers = (String[]) answersArray.getArray();
                String[] correctAnswers = (String[]) correctAnswersArray.getArray();

                System.out.println("ID: " + quizId);
                System.out.println("Title: " + title);
                System.out.println("Questions: " + String.join(", ", questions));
                System.out.println("Answers: " + String.join(", ", answers));
                System.out.println("Correct Answers: " + String.join(", ", correctAnswers));
                System.out.println();
            }
        }
    }

    private static void takeQuiz(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Введите ID квиза, который вы хотите пройти:");
        int quizId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM quizzes WHERE id = ?")) {
            preparedStatement.setInt(1, quizId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String title = resultSet.getString("title");
                Array questionsArray = resultSet.getArray("questions");
                Array answersArray = resultSet.getArray("answers");
                Array correctAnswersArray = resultSet.getArray("correct_answers");

                String[] questions = (String[]) questionsArray.getArray();
                String[] answers = (String[]) answersArray.getArray();
                String[] correctAnswers = (String[]) correctAnswersArray.getArray();

                int score = 0;

                for (int i = 0; i < questions.length; i++) {
                    System.out.println("Вопрос " + (i + 1) + ": " + questions[i]);
                    System.out.println("Варианты ответов: " + answers[i]);
                    System.out.println("Введите номер вашего ответа:");

                    int userAnswer = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    if (userAnswer > 0 && userAnswer <= answers.length) {
                        String[] userAnswers = answers[userAnswer - 1].split(",");
                        String[] correctUserAnswers = correctAnswers[i].split(",");

                        if (arrayEquals(userAnswers, correctUserAnswers)) {
                            System.out.println("Верно!");
                            score++;
                        } else {
                            System.out.println("Неверно. Правильный ответ: " + correctAnswers[i]);
                        }
                    } else {
                        System.out.println("Некорректный номер ответа.");
                        i--; // Повторить текущий вопрос
                    }
                }

                try (PreparedStatement insertResultStatement = connection.prepareStatement(
                        "INSERT INTO quiz_results (quiz_id, user_name, score) VALUES (?, ?, ?)")) {
                    insertResultStatement.setInt(1, quizId);
                    System.out.println("Введите ваше имя:");
                    insertResultStatement.setString(2, scanner.nextLine());
                    insertResultStatement.setInt(3, score);

                    insertResultStatement.executeUpdate();

                    System.out.println("Результаты успешно сохранены.");
                }

            } else {
                System.out.println("Квиз с указанным ID не найден.");
            }
        }
    }

    private static boolean arrayEquals(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        for (int i = 0; i < arr1.length; i++) {
            if (!arr1[i].equals(arr2[i])) {
                return false;
            }
        }
        return true;
    }
}
