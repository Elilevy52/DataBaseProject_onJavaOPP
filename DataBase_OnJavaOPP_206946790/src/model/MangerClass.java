package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.mysql.cj.x.protobuf.MysqlxCursor.Open;

import controller.MainMenuController;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import listeners.*;

public class MangerClass<T> {

	List<Question> manualExam = new ArrayList<Question>();
	List<Question> allQuestions = new ArrayList<Question>();
	List<Question> autoExamArray = new ArrayList<Question>();

	List<File> files = new ArrayList<File>();
	List<File> uniqueFiles = new ArrayList<File>();

	private Vector<MainEventListener> mainEventListener = new Vector<MainEventListener>();

	TreeSet<Question> treeSet = new TreeSet<Question>();
	List<Question> newArrayList = new ArrayList<Question>();
	private MyArrayList myList = new MyArrayList();

	@SuppressWarnings("unused")
	private SimpleStringProperty name;
	private int amountOfQuestions;
	private int size;
	private int testCounter;
	private MainMenuController mc;
	private DatabaseSQL db;
	private Iterator mainIterator;

	Scanner scan = new Scanner(System.in);

	public MangerClass() throws ClassNotFoundException, SQLException {
		amountOfQuestions = 0;
		size = 0;
		testCounter = 1;
		db = new DatabaseSQL();

	}

	public void registerMainMenuListener(MainEventListener listener) {
		mainEventListener.add(listener);
	}

	public void fireAddedOpenQuesion(OpenQuestion quest) {
		for (MainEventListener l : mainEventListener) {
			l.addedOpenQuestion(quest);
		}
	}

	public void fireImportedBinaryFile() {
		for (MainEventListener l : mainEventListener) {
			l.importedBinaryFile(allQuestions);
		}
	}

	public void fireAddedMultipleChoiceQuestion(MultilpeChoiceQuestion quest) {
		for (MainEventListener l : mainEventListener) {
			l.addedMultipleChoiceQuestion(quest);
		}
	}

	public void fireAddedMulipleChoiceAnswer(MultilpeChoiceQuestion question) {
		for (MainEventListener l : mainEventListener) {
			l.addedMultipleChoiceAnswer(question);
		}
	}

	public void fireAutoSaveToBinaryFileOnExit() {
		for (MainEventListener l : mainEventListener) {
			l.autoSavedToBinartFileOnExit(allQuestions);
		}
	}

	public void fireAutoExamCreated(Stage stage) throws IOException {
		for (MainEventListener l : mainEventListener) {
			l.openAutoExamWindow(autoExamArray, stage);
		}
	}

	public void fireCreatedManualExam() throws IOException {
		for (MainEventListener l : mainEventListener) {

		}
	}

	private void fireDeletedMultipleChoiceAnswer(MultipeChoiseAnswer mc2, MultilpeChoiceQuestion mq) {
		for (MainEventListener l : mainEventListener) {
			l.deletedMultipleChoiceAnswer(mc2, mq);
		}

	}

	public void setSize(int size) {
		this.size = size;
		manualExam = new ArrayList<Question>((size));
		for (int i = 0; i < size; i++) {
			manualExam.add(i, null);
		}
	}

	public boolean isAllQuestionEmpty() {
		if (allQuestions.isEmpty()) {
			System.out.println(
					"There is no Questions available, Please add Questions or Import Questions list. \nReturning to main menu safely.");
			return true;
		} else {
			return false;
		}
	}

	public boolean addOpenQuestion(String question, String answer) throws SQLException, ClassNotFoundException {
		for (int i = 0; i < amountOfQuestions; i++) {
			if (allQuestions.get(i).getQuestion().equals(question)) {
				System.out.println("Question already exists");
				return false;
			}
		}
		OpenQuestion oq = new OpenQuestion(question, answer);
		allQuestions.add(oq);
		fireAddedOpenQuesion(oq);
		if (db.isConnected()) {
			db.addOpenQuestionToDB(oq);
			System.out.println("Open question added to database.");
		}
		System.out.println("Created question #" + (amountOfQuestions + 1));
		amountOfQuestions++;
		return true;

	}

	public boolean addMultilpeChoiceQuestion(MultilpeChoiceQuestion question)
			throws ClassNotFoundException, SQLException {
		for (int i = 0; i < amountOfQuestions; i++) {
			if (allQuestions.get(i).getQuestion().equals(question.getQuestion())) {
				System.out.println("Question already exists");
				return false;
			}
		}
		allQuestions.add(question);
		fireAddedMultipleChoiceQuestion(question);
		if (db.isConnected()) {
			db.addMultipleChoiceQuestionToDB(question);
			System.out.println("Multiple choice question added to database.");
		}
		System.out.println("Created question #" + (amountOfQuestions + 1));
		amountOfQuestions++;
		return true;
	}

	public void addMultipleChoiceAnswer(MultilpeChoiceQuestion quest, String answer, boolean isTrue)
			throws ClassNotFoundException, SQLException {
		MultipeChoiseAnswer an = new MultipeChoiseAnswer(answer, isTrue);
		System.out.println(quest.addAnswer(an));
		fireAddedMulipleChoiceAnswer(quest);
		if (db.isConnected()) {
			db.addMultipleChoiceAnswerToDB(quest, answer, isTrue);
			System.out.println("Added multiple choice answer to database.");
		}
	}

	private Question getQuestionById(int questionNumber) {
		return allQuestions.get(questionNumber);
	}

	public String updateAnExistingQuestion(int questionNumber, String question) {
		Question quest = getQuestionById(questionNumber - 1);
		if (quest != null) {
			quest.setQuestion(question);
			return "Question updated successfully.";
		}
		return "Question does not exist";
	}

	public String deleteAnExistingAnswer(int questionNumber, int loction) {
		Question quest = getQuestionById(questionNumber - 1);
		if (quest == null) {
			return "Question does not exist.";
		}
		if (quest instanceof OpenQuestion) {
			return "Cannot delete an Open Question type question.";
		}
		MultilpeChoiceQuestion mQuestion = (MultilpeChoiceQuestion) quest;
		if (loction > mQuestion.getAnswerNumber() || loction < 1) {
			return "Answer does not exist.";
		}
		mQuestion.deleteAnswer(loction);
		return "Answer deleted successfully.";
	}

	public void deleteMultipleChoiceAnswer(MultipeChoiseAnswer mc, MultilpeChoiceQuestion mq) {
		if (mq.deleteAnswerObject(mc)) {
			fireDeletedMultipleChoiceAnswer(mc, mq);
		}
	}

	public String updateAnAnswer(int questionNumber, int loction, String newAnswer) {
		Question quest = getQuestionById(questionNumber - 1);
		if (quest instanceof OpenQuestion) {
			OpenQuestion open = (OpenQuestion) quest;
			open.setOpenAnswer(newAnswer);
		}
		if (quest instanceof MultilpeChoiceQuestion) {
			MultilpeChoiceQuestion mQuestion = (MultilpeChoiceQuestion) quest;
			if (loction > mQuestion.getAnswerNumber() || loction < 1) {
				return "Answer does not exist.";
			}
			mQuestion.updateAnswer(loction, newAnswer);
		}
		return "Answer updated successfully.";
	}

	public void printAllQuestionAndAnswers() {
		System.out.println("___Printing all Questions and all Answers___");
		if (allQuestions.isEmpty()) {
			System.out.println(
					"There is no Questions available, Please add Questions or Import Questions list. Type '-1' to go back to main menu.");
		}
		for (int i = 0; i < allQuestions.size(); i++) {
			if (allQuestions.get(i) != null) {
				System.out.println(allQuestions.get(i));
			}
		}
	}

	public void printAllQuestions() {
		System.out.println("\n___All Multiple Choice Questions___" + "\n");
		if (allQuestions.isEmpty()) {
			System.out.println(
					"There is no Multiple Questions available, Please add Questions or Import Questions list. Type '-1' to go back to main menu.");
		}
		for (int i = 0; i < allQuestions.size(); i++) {
			if (allQuestions.get(i) != null) {
				if (allQuestions.get(i) instanceof MultilpeChoiceQuestion) {
					System.out.println(allQuestions.get(i).getId() + ") " + allQuestions.get(i).getQuestion());
				}
			}
		}
		System.out.println("\n___All Open Questions___" + "\n");
		if (allQuestions.isEmpty()) {
			System.out.println(
					"There is no Open Questions available, Please add Questions or Import Questions list. Type '-1' to go back to main menu.");
		}
		for (int i = 0; i < allQuestions.size(); i++) {
			if (allQuestions.get(i) != null) {
				if (allQuestions.get(i) instanceof OpenQuestion) {
					System.out.println(allQuestions.get(i).getId() + ") " + allQuestions.get(i).getQuestion());
				}
			}
		}
	}

	public void printOnlyOpenQuestions() {
		System.out.println("\n___All Open Questions___" + "\n");
		if (allQuestions.isEmpty()) {
			System.out.println(
					"There is no Open Questions available, Please add Questions or Import Questions list. Type '-1' to go back to main menu.");
		}
		for (int i = 0; i < allQuestions.size(); i++) {
			if (allQuestions.get(i) != null) {
				if (allQuestions.get(i) instanceof OpenQuestion) {
					System.out.println(allQuestions.get(i).getQuestion());
				}
			}
		}
	}

	public void printOnlyMultilpeChoiceQuestions() {
		System.out.println("\n___All Multipe Choise Questions___" + "\n");
		if (allQuestions.isEmpty()) {
			System.out.println(
					"There is no Multiple Questions available, Please add Questions or Import Questions list. Type '-1' to go back to main menu.");
		}
		for (int i = 0; i < allQuestions.size(); i++) {
			if (allQuestions.get(i) != null) {
				if (allQuestions.get(i) instanceof MultilpeChoiceQuestion) {
					System.out.println(
							"[" + allQuestions.get(i).getId() + "] " + allQuestions.get(i).printQuestionNumber());
				}
			}
		}
	}

	public void printSelectedMultipeChoiseAnswers(int questionNumber) {
		System.out.println("Selected Answers for Question number ->\t" + "[" + questionNumber + "] ");
		Question quest = getQuestionById(questionNumber);
		if (quest instanceof MultilpeChoiceQuestion) {
			MultilpeChoiceQuestion mQuestion = (MultilpeChoiceQuestion) quest;
			for (int i = 0; i < mQuestion.getAnswerNumber(); i++) {
				if (mQuestion.getAnswers(i) != null) {
					System.out.println("[" + (i + 1) + "] " + mQuestion.getAnswers(i));
				}
			}
		}
	}

	public void sortAndPrintAutoExam(Stage stage) throws IOException {
		CompareQuestion cq = new CompareQuestion();
		autoExamArray.sort(cq);
		saveFile(getCurrentDate(), autoExamArray);
		fireAutoExamCreated(stage);
		for (int i = 0; i < autoExamArray.size(); i++) {
			System.out.println(autoExamArray.get(i));
		}
		JOptionPane.showMessageDialog(null,
				"Exam created successfully.\n" + "The exam contains: " + autoExamArray.size() + " questions.");
		System.out.println("Exam created successfully\n" + "The exam contains:" + autoExamArray.size() + " questions.");
		autoExamArray.clear();
	}

	public void sortAndPrintManualExam() throws IOException {
		if (manualExam.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No exam created. \nNo questions added to exam.");
		} else {
			for (int i = 0; i < manualExam.size(); i++) {
				if (manualExam.get(i) != null) {
					if (manualExam.get(i) instanceof OpenQuestion) {
						System.out.println(manualExam.get(i).toString());
					}
					if (manualExam.get(i) instanceof MultilpeChoiceQuestion) {
						MultilpeChoiceQuestion mQuestion = (MultilpeChoiceQuestion) manualExam.get(i);
						addBulitInAnswers(mQuestion);
						System.out.println(manualExam.get(i).toString());
					}
				}
			}
			int counter = 0;
			for (int i = 0; i < manualExam.size(); i++) {
				if (manualExam.get(i) != null) {
					counter++;
				}
			}
			JOptionPane.showMessageDialog(null,
					"Exam created successfully\n" + "The exam contains: " + counter + " questions.");
			System.out.println("Exam created successfully\n" + "The exam contains: " + counter + " questions.");
			try {
				saveFile(getCurrentDate(), manualExam);
			} catch (IOException e) {
				e.printStackTrace();
			}
			manualExam.clear();
		}

	}

	public boolean checkIfQuestionInArray(Question quest) {
		if (quest == null) {
			return false;
		}
		for (int i = 0; i < autoExamArray.size(); i++) {
			if (autoExamArray.get(i) != null) {
				if (autoExamArray.contains(quest)) {
					return true;
				}
			}
		}
		return false;
	}

	public Question generateNewQuestion(int amount) {
		Random rand = new Random();
		while (autoExamArray.size() != amount) {
			Question quest = getQuestionById(rand.nextInt(amount));
			while (checkIfQuestionInArray(quest)) {
				quest = getQuestionById(rand.nextInt(amount));
			}
			return quest;
		}
		return null;
	}

	public int checkAllQuestionLength() {
		return allQuestions.size();
	}

	public void addBulitInAnswers(MultilpeChoiceQuestion answers) {
		int trueCounter = 0;
		int falseCounter = 0;
		for (int i = 0; i < answers.getAnswerNumber(); i++) {
			if (answers.getAnswers(i).getIsTrue()) {
				trueCounter++;
			} else {
				falseCounter++;
			}

		}
		if (trueCounter == 1) {
			answers.addAnswer(new MultipeChoiseAnswer("Nothing is Correct", false));
			answers.addAnswer(new MultipeChoiseAnswer("More then one answer is correct", false));
		}
		if (trueCounter > 1) {
			answers.addAnswer(new MultipeChoiseAnswer("Nothing is Correct", false));
			answers.addAnswer(new MultipeChoiseAnswer("More then one answer is correct", true));
		}
		if (falseCounter > 0 && trueCounter == 0) {
			answers.addAnswer(new MultipeChoiseAnswer("Nothing is Correct", true));
			answers.addAnswer(new MultipeChoiseAnswer("More then one answer is correct", false));
		}
	}

	public void autoCreateExam(int amount, Stage stage) throws IOException {
		try {
			for (int i = 0; i < amount; i++) {
				Question quest = generateNewQuestion(amount);
				if (quest instanceof OpenQuestion) {
					OpenQuestion open = (OpenQuestion) quest;
					autoExamArray.add(open);
				}
				if (quest instanceof MultilpeChoiceQuestion) {
					MultilpeChoiceQuestion mQuestion = (MultilpeChoiceQuestion) quest;
					autoExamArray.add(mQuestion);
					addBulitInAnswers(mQuestion);
				}

			}
			sortAndPrintAutoExam(stage);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "No questions in data");
		}
	}

	public boolean checkInstanceofQuestion(int questionNum) {
		Question quest = getQuestionById(questionNum);
		if (quest instanceof OpenQuestion) {
			return false;
		}
		if (quest instanceof MultilpeChoiceQuestion) {
			return true;
		}
		return false;
	}

	public void getMultipeChoiseAnswer(int questionNum) {
		Question quest = getQuestionById(questionNum);
		if (quest instanceof MultilpeChoiceQuestion) {
			MultilpeChoiceQuestion mQuestion = (MultilpeChoiceQuestion) quest;
			for (int i = 0; i < mQuestion.getAnswerNumber(); i++) {
				if (!mQuestion.getAnswers(i).getAnswer().isEmpty()) {
					System.out.println("[" + (i + 1) + "] " + mQuestion.getAnswers(i));
				} else {
					System.out.println("No answers available.");
				}
			}
		}
	}

	public boolean addMultipeChoiseQuestionToManualExam(MultilpeChoiceQuestion question) {
		if (!manualExam.contains(question)) {
			manualExam.add(question);
			JOptionPane.showMessageDialog(null, "Multiple choice question added successfully.");
			return true;
		} else {
			return false;
		}
	}

	public boolean addOpenQuestionToManualExam(OpenQuestion question) {
		if (!manualExam.contains(question)) {
			manualExam.add(question);
			JOptionPane.showMessageDialog(null, "Open question added successfully.");
			return true;
		} else {
			return false;
		}
	}

	public boolean addQuestionToManualExam(int questionNumber, List<Integer> answersArray) {
		Question question = getQuestionById(questionNumber);
		if (question instanceof OpenQuestion) {
			OpenQuestion open = (OpenQuestion) question;
			addOpenQuestionToManualExam(open);
			return true;
		}
		if (question instanceof MultilpeChoiceQuestion) {
			MultilpeChoiceQuestion quest = (MultilpeChoiceQuestion) question;
			MultilpeChoiceQuestion aQ = new MultilpeChoiceQuestion(quest.getQuestion());
			for (int i = 0; i < answersArray.size(); i++) {
				aQ.addAnswer(new MultipeChoiseAnswer(quest.getAnswers(answersArray.get(i) - 1)));
			}
			addMultipeChoiseQuestionToManualExam(aQ);
			return true;
		}
		return false;
	}

	public int intException(Scanner e) {
		int num = 0;
		boolean input = false;
		do {
			input = false;
			try {
				num = e.nextInt();
			} catch (InputMismatchException exception) {
				System.err.println("Invalid input ! Expected numercial value. ");
				input = true;
			}
			e.nextLine();
		} while (input);
		return num;
	}

	public boolean booleanException(Scanner e) {
		boolean input = false;
		boolean invalidInput = false;
		do {
			invalidInput = false;
			try {
				invalidInput = e.nextBoolean();
			} catch (InputMismatchException exception) {
				System.err.println("Invalid input ! Expected boolean value, (true/false). ");
				invalidInput = true;
			}
			e.nextLine();
		} while (invalidInput);
		return input;
	}

	public void saveFile(String date, List<Question> manualExam) throws IOException, FileAlreadyExistsException {
		BufferedWriter questionsFile = new BufferedWriter(
				new FileWriter("Exams/" + "Exam_" + date + "_Questions_" + "(" + testCounter + ") " + ".txt"));
		BufferedWriter answersFile = new BufferedWriter(
				new FileWriter("Exams/" + "Exam_" + date + "_Solutions_" + "(" + testCounter + ") " + ".txt"));
		testCounter++;
		for (int i = 0; i < manualExam.size(); i++) {
			if (manualExam.get(i) != null) {
				questionsFile.write(manualExam.get(i).getQuestion() + "\n");
			}
		}
		for (int i = 0; i < manualExam.size(); i++) {
			if (manualExam.get(i) instanceof MultilpeChoiceQuestion) {
				MultilpeChoiceQuestion MultipleQuest = (MultilpeChoiceQuestion) manualExam.get(i);
				for (int j = 0; j < MultipleQuest.getAnswerNumber(); j++) {
					answersFile.write(MultipleQuest.getAnswers(j).getAnswer() + " - "
							+ MultipleQuest.getAnswers(j).getIsTrue() + "\n");
				}
			}
			if (manualExam.get(i) instanceof OpenQuestion) {
				OpenQuestion oQuestion = (OpenQuestion) manualExam.get(i);
				answersFile.write(oQuestion.getOpenAnswer() + "\n");
			}
		}
		questionsFile.flush();
		questionsFile.close();
		answersFile.flush();
		answersFile.close();

	}

	public void saveOnExitBinaryFile()
			throws IOException, FileNotFoundException, ClassNotFoundException, FileAlreadyExistsException {
		try {
			Files.createDirectory(Paths.get("BinaryFile/"));
			fireAutoSaveToBinaryFileOnExit();
		} catch (FileAlreadyExistsException e) {
			e.getMessage();
		}
		ObjectOutputStream outFile = new ObjectOutputStream(new FileOutputStream("BinaryFile/Exam.dat"));
		for (int i = 0; i < allQuestions.size(); i++) {
			outFile.writeObject(allQuestions.get(i));
		}
		outFile.close();
		System.out.println("Binary file created.");
	}

	public void readBinaryFile() throws ClassNotFoundException, IOException {
		try (ObjectInputStream inFile = new ObjectInputStream(new FileInputStream("BinaryFile/Exam.dat"))) {
			while (true) {
				allQuestions.add((Question) inFile.readObject());
			}
		} catch (EOFException e) {
			System.out.println("Binary File -> \n");
		}
	}

	private final static String getCurrentDate() {
		String pattern = "MM-dd-yyyy";
		DateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(new Date());
	}

	public void printAndSaveManualExamToFile() throws IOException {
		CompareQuestion cq = new CompareQuestion();
		manualExam.sort(cq);
		fireCreatedManualExam();
		saveFile(getCurrentDate(), manualExam);
		for (int i = 0; i < manualExam.size(); i++) {
			if (manualExam.get(i) != null) {
				System.out.println(manualExam.get(i));
			}
		}
		manualExam.clear();
	}

	public void sortQuestionsByAnswersLength() {
		CompareQuestion cq = new CompareQuestion();
		allQuestions.sort(cq);
		System.out.println("___Printing all Questions and all Answers___");
		for (int i = 0; i < allQuestions.size(); i++) {
			if (allQuestions.get(i) != null) {
				System.out.println(allQuestions.get(i));
			}
		}
	}

	public void createCopyOfAnExistingExam() throws IOException, FileNotFoundException {
		int countFile = 0;
		File[] curDir = new File("Exams/").listFiles();
		for (File f : curDir) {
			if (f.isFile()) {
				countFile++;
				System.out.println(countFile + ") " + f.getName());
				files.add(f);
			}
		}
	}

	public List<File> showAllExistingFilesInDir() {
		File[] textFiles = new File("Exams/").listFiles();
		name = new SimpleStringProperty();
		for (File f : textFiles) {
			if (f.isFile()) {
				files.add(f);
				name.set(f.getName());
			}
		}
		removeDuplicates();
		return uniqueFiles;
	}

	public void removeDuplicates() {
		for (File l : files) {
			if (!uniqueFiles.contains(l)) {
				uniqueFiles.add(l);
			}
		}
	}

	public void makeCopyOfChosenFile(File f) throws IOException, FileNotFoundException {
		for (int i = 0; i < uniqueFiles.size(); i++) {
			if (uniqueFiles.get(i).getName().equals(f.getName())) {
				BufferedReader read = new BufferedReader(new FileReader(f));
				FileWriter copiedFile = new FileWriter("ExamCopy/" + "Copy of" + uniqueFiles.get(i).getName());
				String count;
				while ((count = read.readLine()) != null) {
					copiedFile.write(count + "\n");
				}
				JOptionPane.showMessageDialog(null, "Created copy of" + uniqueFiles.get(i).getName());
				System.out.println("Created copy of" + uniqueFiles.get(i).getName());
				read.close();
				copiedFile.flush();
				copiedFile.close();
			}

		}

	}

	public void addQuestionsFromBinaryFile(String fileName)
			throws FileNotFoundException, IOException, ClassNotFoundException {

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("BinaryFile/" + fileName))) {
			while (true) {
				allQuestions.add((Question) in.readObject());
			}

		} catch (EOFException e) {
			fireImportedBinaryFile();
			JOptionPane.showMessageDialog(null, "Questions added from binary file successfully");
			System.out.println("Questions added from binary file successfully");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "File Not Found.");
		}
		for (int i = 0; i < allQuestions.size(); i++) {
			Question quest = null;
			if (i == 0) {
				quest = getQuestionById(i + 1);
			} else {
				quest = getQuestionById(i);
			}
			if (quest != null) {
				quest.setQuestionsId(allQuestions);
				break;
			}
		}

	}

	public int getAllQuestionSize() {
		return allQuestions.size();
	}

	public Question getAllQuestion(int index) {
		return allQuestions.get(index);
	}

	public int getManualExamSize() {
		return manualExam.size();
	}
	public void questionList() throws SQLException, ClassNotFoundException {

		// General questions
		String quest1 = "First question";
		String quest2 = "Second question";
		String quest3 = "Third question";
		String quest4 = "Fourth question";
		String quest5 = "Fifth question";
		String quest6 = "Sixth question";
		String quest7 = "Seventh question";
		String quest8 = "Eighth question";

		// Just answers
		String ans1 = "First answer";
		String ans2 = "Second answer";
		String ans3 = "Another answer";
		String ans4 = "And another one";
		String ans5 = "Fifth answer";
		String ans6 = "Sixth answer";
		String ans7 = "Seventh answer";
		String ans8 = "Eighth answer";

		// False answers
		String ansF1 = "False answer #1";
		String ansF2 = "False answer #2";
		String ansF3 = "False answer #3";
		String ansF4 = "False answer #4";
		String ansF5 = "False answer #5";

		// Open answers
		String openAns = "First open answer";
		String openAns2 = "Second open answer";
		String openAns3 = "Third open answer";
		String openAns4 = "Fourth open answer";

		// Create new Multiple Choice Question question objects
		MultilpeChoiceQuestion ameriTest1 = new MultilpeChoiceQuestion(quest1);
		MultilpeChoiceQuestion ameriTest2 = new MultilpeChoiceQuestion(quest2);
		MultilpeChoiceQuestion ameriTest3 = new MultilpeChoiceQuestion(quest3);
		MultilpeChoiceQuestion ameriTest4 = new MultilpeChoiceQuestion(quest4);

		// Add add Multiple Choice Question questions
		addMultilpeChoiceQuestion(ameriTest1);
		addMultilpeChoiceQuestion(ameriTest2);
		addMultilpeChoiceQuestion(ameriTest3);
		addMultilpeChoiceQuestion(ameriTest4);

		// Multiple Choice answers
		MultipeChoiseAnswer ameriAns = new MultipeChoiseAnswer(ans1, true);
		MultipeChoiseAnswer ameriAns2 = new MultipeChoiseAnswer(ans2, true);
		MultipeChoiseAnswer ameriAns3 = new MultipeChoiseAnswer(ans3, true);
		MultipeChoiseAnswer ameriAns4 = new MultipeChoiseAnswer(ans4, true);
		MultipeChoiseAnswer ameriAns5 = new MultipeChoiseAnswer(ans5, true);
		MultipeChoiseAnswer ameriAns6 = new MultipeChoiseAnswer(ans6, true);
		MultipeChoiseAnswer ameriAns7 = new MultipeChoiseAnswer(ans7, true);
		MultipeChoiseAnswer ameriAns8 = new MultipeChoiseAnswer(ans8, true);
		MultipeChoiseAnswer ameriAnsF1 = new MultipeChoiseAnswer(ansF1, false);
		MultipeChoiseAnswer ameriAnsF2 = new MultipeChoiseAnswer(ansF2, false);
		MultipeChoiseAnswer ameriAnsF3 = new MultipeChoiseAnswer(ansF3, false);
		MultipeChoiseAnswer ameriAnsF4 = new MultipeChoiseAnswer(ansF4, false);
		MultipeChoiseAnswer ameriAnsF5 = new MultipeChoiseAnswer(ansF5, false);

		// More than one is correct:
		ameriTest1.addAnswer(ameriAns);
		ameriTest1.addAnswer(ameriAns2);
		ameriTest1.addAnswer(ameriAns3);
		ameriTest1.addAnswer(ameriAns4);
		ameriTest1.addAnswer(ameriAnsF1);
		ameriTest1.addAnswer(ameriAnsF4);
		addBulitInAnswers(ameriTest1);
		// All false
		ameriTest2.addAnswer(ameriAnsF1);
		ameriTest2.addAnswer(ameriAnsF2);
		ameriTest2.addAnswer(ameriAnsF3);
		ameriTest2.addAnswer(ameriAnsF4);
		ameriTest2.addAnswer(ameriAnsF5);
		addBulitInAnswers(ameriTest2);

		// Only one question is true
		ameriTest3.addAnswer(ameriAns5);
		ameriTest3.addAnswer(ameriAnsF1);
		ameriTest3.addAnswer(ameriAnsF2);
		ameriTest3.addAnswer(ameriAnsF3);
		ameriTest3.addAnswer(ameriAnsF4);
		addBulitInAnswers(ameriTest3);
		// more than one is correct #2
		ameriTest4.addAnswer(ameriAns6);
		ameriTest4.addAnswer(ameriAns7);
		ameriTest4.addAnswer(ameriAns8);
		ameriTest4.addAnswer(ameriAnsF3);
		ameriTest4.addAnswer(ameriAnsF4);
		addBulitInAnswers(ameriTest4);
		// Open questions + Answers
		addOpenQuestion(quest5, openAns);
		addOpenQuestion(quest6, openAns2);
		addOpenQuestion(quest7, openAns3);
		addOpenQuestion(quest8, openAns4);

	}
	/////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Design Patterns Homework /////////////////////
	///////////////////////////////////////////////////////////////////////////

	public void copyArratListToTreeSet() throws SQLException {
		treeSet = new TreeSet<Question>(Collections.reverseOrder(new Comparator<Question>() {
			@Override
			public int compare(Question o1, Question o2) {
				if (o1.getQuestion().length() > o2.getQuestion().length()) {
					return 1;
				}
				if (o1.getQuestion().length() < o2.getQuestion().length()) {
					return -1;
				} else {
					return 0;
				}
			}
		}));
		treeSet.addAll(allQuestions);
		System.out.println("Copied 'ArrayList' to treeSet successfully.");
	}

	public void copyTreeSetIntoNewArrayList() throws SQLException {
		Collections.sort(newArrayList, new StringComparator());
		if (treeSet.isEmpty()) {
			System.out.println("'TreeSet' is empty, cannot copy empty Collection.");
		} else {
			newArrayList.addAll(treeSet);
			System.out.println("Copied 'TreeSet' to 'HashSet' successfully.");
		}
	}

	public void addTheCollectionToMyArrayList() throws NullPointerException, SQLException {
		createNewLists();
		List<Question> list1 = new ArrayList<Question>(newArrayList);
		System.out.println(newArrayList.get(0).getQuestion());
		Collections.sort(list1, treeSet.comparator());
		mainIterator = list1.iterator();
		while (mainIterator.hasNext()) {
			myList.add((Question) mainIterator.next());
		}
		System.out.println("Copied 'newArrayList' to MyArrayList.");
	}

	public void printTreeSet() {
		Iterator<Question> entrySet = treeSet.iterator();
		while (entrySet.hasNext()) {
			System.out.println(entrySet.next());
		}
	}

	public void printMyArrayList() {
		Iterator<Question> newIterator = myList.iterator();
		while (newIterator.hasNext()) {
			System.out.println(newIterator.next());
		}
	}

	public void printNewArrayList() {
		if (newArrayList.isEmpty()) {
			System.out.println("'TreeSet' is empty, cannot print an empty Collection.\n");
		} else {
			mainIterator = newArrayList.iterator();
			while (mainIterator.hasNext()) {
				System.out.println(mainIterator.next());
			}
		}
	}

	public void printNewArrayListWithoutDup() {
		if (newArrayList.isEmpty()) {
			System.out.println("'TreeSet' is empty, cannot print an empty Collection.\n");
		} else {
			Collections.sort(newArrayList, treeSet.comparator());
			mainIterator = newArrayList.iterator();
			while (mainIterator.hasNext()) {
				System.out.println(mainIterator.next());
			}
		}
	}

	public void createNewLists() throws SQLException {
		copyArratListToTreeSet();
		copyTreeSetIntoNewArrayList();
		// printTreeSet();
		// printNewArrayList();
		// printNewArrayListWithoutDup();
	}
	///////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////
	////////////////////////////// mySQL////////////////////
	///////////////////////////////////////////////////////


	public void removeQuestionFromDatabaseUsingID(Question quest, int ID)
			throws SQLException, ClassNotFoundException {
		if (db.isConnected() && quest instanceof MultilpeChoiceQuestion) {
			db.removeMultipleChoiceQuestionFromDB(quest, ID);
			System.out.println(quest.getQuestion() + "\nRemoved successfully.");
		} if(db.isConnected() && quest instanceof OpenQuestion) {
			db.removeOpenQuestionFromDB(quest, ID);
			System.out.println(quest.getQuestion() + "\nRemoved successfully.");
		}
		else if (!db.isConnected()) {
			System.out.println("Please establish connection with the database first.");
		}
	}

	public void connectToDB() throws SQLException, ClassNotFoundException {
		db.connectToSql();
		if (db.isConnected()) {
			System.out.println("Connected to mySQL database successfully");
		} else if (!db.isConnected()) {
			System.out.println("Connection fail.");
		}
	}

	public void closeConnectionToDB() throws SQLException, ClassNotFoundException {
		if (!db.isConnected()) {
			System.out.println("No connection was established");
		} else {
			db.closeConnectionToDB();
		}

	}

	public boolean checkConnectionToDB() throws ClassNotFoundException, SQLException {
		if (db.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public void importPremadeQuestions() throws ClassNotFoundException, SQLException {
		questionList();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

}