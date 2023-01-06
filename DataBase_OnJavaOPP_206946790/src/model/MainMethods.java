package model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.InputMismatchException;

public interface MainMethods {

	void mangerMethods() throws ClassNotFoundException, SQLException;

	void mainMenu() throws FileNotFoundException, IOException, ClassNotFoundException, InputMismatchException, SQLException;

	boolean importQuestions() throws ClassNotFoundException, IOException;
}
