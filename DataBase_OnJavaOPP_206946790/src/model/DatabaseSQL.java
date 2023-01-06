package model;

import java.sql.*;
import java.util.Iterator;
import java.util.Scanner;

public class DatabaseSQL {

	private Scanner input;
	private int innerOP;

	private ResultSet resultSet;
	private Connection connect;
	private Statement state;
	private PreparedStatement preState;

	private int question_ID;
	private int answer_ID;

	public DatabaseSQL() throws ClassNotFoundException, SQLException {
		input = new Scanner(System.in);
		this.innerOP = 0;
		resultSet = null;
		connect = null;
		connectToSql();
	}

	public void connectToSql() throws SQLException, ClassNotFoundException {

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			String dbUrl = "jdbc:mysql://localhost:3306/test";

			connect = DriverManager.getConnection(dbUrl, "root", "1234");

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (SQLException ex) {
			while (ex != null) {
				System.out.println("SQL exception: " + ex.getMessage());
				ex = ex.getNextException();
			}
		}
		createStatement();
		dropOldTables();
		createNewTable();
	}

	public boolean isConnected() throws SQLException, ClassNotFoundException {
		if (connect.isValid(1) == true) {
			return true;

		} else {
			return false;
		}
	}

	public void createStatement() throws SQLException {
		try {
			state = connect.createStatement();
		} catch (SQLException e) {
			System.out.println("SQL exception: " + e.getMessage());
			e.printStackTrace();

		}
	}

	private void createNewTable() throws SQLException {
		preState = connect.prepareStatement("use test");
		preState.execute();

		preState = connect.prepareStatement("create table OpenQuestions("
				+ "	id INT NOT NULL primary key,"
				+ "	questionText VARCHAR(255) NOT NULL," 
				+ "	correct_answer VARCHAR(255) NOT NULL)"
				+ " engine = InnoDB;");
		preState.execute();

		preState = connect.prepareStatement("create table MultipleChoiceQuestion("
				+ "id INT NOT NULL primary key,"
				+ "questionText varchar(255) NOT NULL)"
				+ "engine = InnoDB;");
		preState.execute();

		preState = connect.prepareStatement("create table MultipleChoiceAnswers("
				+ "id INT NOT NULL primary key AUTO_INCREMENT,"
				+ "question_id INT NOT NULL,"
				+ "answerText VARCHAR(255) NOT NULL,"
				+ "isTrue boolean NOT NULL default true,"
				+ "foreign key (question_id) REFERENCES MultipleChoiceQuestion(id))"
				+ "engine = InnoDB;");
		preState.executeUpdate();

		preState = connect.prepareStatement("create table Exams("
				+ "	id INT NOT NULL primary key auto_increment,"
				+ "multipleChoiceQuestion_ID INT NOT NULL,"
				+ "openQuestion_ID INT NOT NULL,"
				+ "foreign key (multipleChoiceQuestion_ID) references MultipleChoiceQuestion(id),"
				+ "foreign key (openQuestion_ID) references OpenQuestions (id))"
				+ "engine = InnoDB;");
		preState.executeUpdate();
		
	}

	private void dropOldTables() throws SQLException {
		preState = connect.prepareStatement("SET FOREIGN_KEY_CHECKS = 0;");
		preState.execute();

		preState = connect.prepareStatement("drop table if exists OpenQuestions");
		preState.execute();

		preState = connect.prepareStatement("drop table if exists MultipleChoiceQuestion");
		preState.execute();

		preState = connect.prepareStatement("drop table if exists MultipleChoiceAnswers");
		preState.execute();

		preState = connect.prepareStatement("drop table if exists Exams");
		preState.execute();

		preState = connect.prepareStatement("SET FOREIGN_KEY_CHECKS = 1;");
		preState.execute();
	}

	public void closeConnectionToDB() throws SQLException, ClassNotFoundException {
		System.out
				.println("Are you sure you want to close connection with the database?\n Enter '1' to close connection."
						+ "\n Enter '2' to stay connected to the database.");
		innerOP = input.nextInt();
		switch (innerOP) {
		case 1:
			connect.close();
			System.out.println("Connection close successfully.");
			break;
		case 2:
			System.out.println("Connection to database is still available.");
			break;

		default:
			System.out.println("Please enter valid input");
		}
	}

	public void addOpenQuestionToDB(OpenQuestion oQuest) throws SQLException {
		question_ID = oQuest.getId();
		String question = oQuest.getOpenQuestion();
		String answer = oQuest.getOpenAnswer();
		String query = "INSERT INTO OpenQuestions (id, questionText, correct_answer)" + "VALUES(?, ?, ?)";
		preState = connect.prepareStatement(query);
		preState.setInt(1, question_ID);
		preState.setString(2, question);
		preState.setString(3, answer);
		preState.executeUpdate();

	}

	public void addMultipleChoiceQuestionToDB(MultilpeChoiceQuestion mcQuestion) throws SQLException {
		question_ID = mcQuestion.getId();
		String question = mcQuestion.getQuestion();
		String query = "INSERT INTO MultipleChoiceQuestion (id,questionText)" + "VALUES(?,?)";
		preState = connect.prepareStatement(query);
		preState.setInt(1, question_ID);
		preState.setString(2, question);
		preState.executeUpdate();
	}

	public void addMultipleChoiceAnswerToDB(MultilpeChoiceQuestion mcQuestion, String answer, boolean isTrue)
			throws SQLException {
		question_ID = mcQuestion.getId();
		String query = "INSERT INTO MultipleChoiceAnswers (question_ID, answerText, isTrue)" + "VALUES(?, ? ,?)";
		preState = connect.prepareStatement(query);
		preState.setInt(1, question_ID);
		preState.setString(2, answer);
		preState.setBoolean(3, isTrue);
		preState.executeUpdate();
	}

	public void removeMultipleChoiceQuestionFromDB(Question mcQuest ,int ID) throws SQLException {
			ID = mcQuest.getId();
			String query2 = "DELETE FROM test.MultipleChoiceQuestion WHERE id = ID";
			preState = connect.prepareStatement(query2);
			preState.executeUpdate();
	}

	public void removeOpenQuestionFromDB(Question openQ, int ID) throws SQLException {
		ID = openQ.getId();
		String query = "DELETE FROM test.openQuestions WHERE id = ID";
		preState = connect.prepareStatement(query);
		preState.executeUpdate();
	}
}
