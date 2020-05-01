import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * 
 * @author Tahsin Imtiaz
 * This represents the School database Command Executor which
 * takes commands mentioned in the project description and does appropriate operation.
 */
public class SchoolDbCommandExecutor
{
	private Logger log;		//This logger is used to print INFO and WARNING messages
	private DatabaseConnection dbConnection;	//holds the connection to he database
	private static boolean connectionEstablishment;		//keeps track whether a connection is established or not
	
	public static void main(String[] args)
	{
		SchoolDbCommandExecutor schoolDbCommandExecutor = new SchoolDbCommandExecutor();	//create a SchoolDbCommandExecutor instance
		
		if(!connectionEstablishment)	//check whether a connection is established
		{
			return;
		}
		
		//Adding shut down hook, if we press ctrl+c to close the program,
		//the database connection will be handled(closed) before the program shuts down.
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				System.out.println("\nclosing db connection before shutting down program....");
				schoolDbCommandExecutor.CloseDatabase();
				System.out.println("db connection closed...");
				System.out.println("shutting down...");
			}
		});
		schoolDbCommandExecutor.ExecuteCommands();	//finally, start reading and executing commands
	}
	
	/**
	 * Creates one instance of SchoolDbCommandExecutor
	 */
	public SchoolDbCommandExecutor()
	{
		//initializing the logger
		this.log = Logger.getLogger(SchoolDbCommandExecutor.class.getName());
		this.log.setLevel(Level.ALL);
		
		//connection to db
		System.out.println("attempting to open a db instance");
		this.dbConnection = new DatabaseConnection(this.log);
		System.out.println("attempting to establish db connection");
		connectionEstablishment = this.dbConnection.EstablistDatabaseConnection();
	}
	
	/**
	 * reads and executes commands given by the user.
	 */
	private void ExecuteCommands()
	{
		Scanner scan = new Scanner(System.in);	//scanner to read user commands
		
		boolean exit = false;	// command will be read until user wants to exit
		
		while(!exit)
		{
			System.out.println("reading command...");
			String line = scan.nextLine();
			
			if(line.equals("exit"))		//user wants to exit
			{
				exit = true;
				log.info("closing database connection");
				this.CloseDatabase();
				continue;
			}
			
			Command command = PrepareCommand(line);
			
			if(command != null)
			{
				System.out.println(Constants.validCommand);
				
				boolean executionResult = this.dbConnection.ExecuteQuery(command);
				if(executionResult)
				{
					log.info("command executed successfully");
				}
				else
				{
					log.info("command execution failed");
				}
			}
			else
			{
				System.out.println(Constants.invalidCommand);
			}
		}
	}
	
	/**
	 * Counts how many times a character appeared in a string.
	 * @param line - the string where the character is checked.
	 * @param character - the character that is check.
	 * @return An integer indicating how many time the character occured in the line string.
	 */
	private int CountCharacterInString(String line, char character)
	{
		return (int)line.chars().filter(num -> num == character).count();
	}
	
	/**
	 * checks whether the quotes are valid in a command line from the user
	 * @param line - the entire string given by the user
	 * @return whether line is Quote valid or not
	 */
	private boolean IsStringQuoteValid(String line)
	{
		boolean didQuoteStart = false;
		
		for(int i=0;i<line.length();i++)
		{
			if(line.charAt(i) == Constants.quoteMark)
			{
				if(didQuoteStart)
				{
					if(i == line.length()-1)
					{
						continue;
					}
					else if(line.charAt(i+1) != Constants.space.charAt(0))
					{
						return false;
					}
					
					didQuoteStart = false;
				}
				else
				{
					didQuoteStart = true;
				}
			}
		}
		return true;
	}
	
	/**
	 * parses the command string and separates the arguments
	 * @param commandStr
	 * @return the argument values separately
	 */
	private String[] SplitCommandLine(String commandStr)
	{
		if(CountCharacterInString(commandStr, Constants.quoteMark) % 2 != 0)
		{
			return null;
		}
		if(!IsStringQuoteValid(commandStr))
		{
			return null;
		}
		
		String[] result = null;
		
		int startIndex = 0;
		int endIndex = 0;
		boolean didQuoteStart = false;
		int resultCounter = 0;
		while(endIndex <= commandStr.length())
		{
			if(endIndex == commandStr.length())
			{
				if(!(startIndex > endIndex))
				{
					if(result == null)
					{
						result = new String[1];
						result[resultCounter] = commandStr.substring(startIndex, endIndex);
						startIndex = endIndex + 2;
						resultCounter++;
					}
					else
					{
						result = Arrays.copyOf(result, resultCounter+1);
						result[resultCounter] = commandStr.substring(startIndex, endIndex);
						startIndex = endIndex + 2;
						resultCounter++;
					}
				}
			}
			else if(commandStr.charAt(endIndex) == Constants.quoteMark)
			{
				if(didQuoteStart)
				{
					if(!(startIndex > endIndex))
					{
						if(result == null)
						{
							result = new String[1];
							result[resultCounter] = commandStr.substring(startIndex+1, endIndex);
							startIndex = endIndex + 2;
							resultCounter++;
						}
						else
						{
							result = Arrays.copyOf(result, resultCounter+1);
							result[resultCounter] = commandStr.substring(startIndex+1, endIndex);
							startIndex = endIndex + 2;
							resultCounter++;
						}
					}
					didQuoteStart = false;
				}
				else
				{
					startIndex = endIndex;
					didQuoteStart = true;
				}
			}
			else if(commandStr.charAt(endIndex) == Constants.space.charAt(0))
			{
				if(!didQuoteStart)
				{
					if(!(startIndex > endIndex))
					{
						if(result == null)
						{
							result = new String[1];
							result[resultCounter] = commandStr.substring(startIndex, endIndex);
							startIndex = endIndex + 1;
							resultCounter++;
						}
						else
						{
							result = Arrays.copyOf(result, resultCounter+1);
							result[resultCounter] = commandStr.substring(startIndex, endIndex);
							startIndex = endIndex + 1;
							resultCounter++;
						}
					}
				}
			}
			
			if(result != null)
			{
				if(result[resultCounter-1].equals(Constants.space) || result[resultCounter-1].equals(Constants.emptyString))	//empty string
				{
					result = Arrays.copyOf(result, resultCounter-1);
					resultCounter--;
					
					if(resultCounter == 0)
					{
						result = null;
					}
				}
			}

			endIndex++;
		}
		
		return result;
		
	}
	
	/**
	 * checks whether "term" has valid structure since "term" has specific structure.
	 * @param potentialTerm - the term string
	 * @return A standardized string for term is term is valid, otherwise null
	 */
	private String isTermStructureSatisfied(String potentialTerm)
	{
		String year = potentialTerm.substring(potentialTerm.length()-2);
		String semester = potentialTerm.substring(0, potentialTerm.length()-2);
		
		System.out.println("semester: " + semester + ", year: " + year);
		
		if(!(semester.equals(Constants.fall) || semester.equals(Constants.Fall) || semester.equals(Constants.spring) || semester.equals(Constants.Spring) || semester.equals(Constants.springShort)
		   || semester.equals(Constants.SpringShort) || semester.equals(Constants.summer) || semester.equals(Constants.Summer) || semester.equals(Constants.summerShort) || semester.equals(Constants.SummerShort)))
		{
			log.warning("Invalid semester in \"term\"!!! \nValid semesters are: " + Constants.fall + ", " + Constants.Fall + ", " + Constants.spring + ", " + Constants.Spring + ", " + Constants.springShort
		               + ", " + Constants.SpringShort + ", " + Constants.summer + ", " + Constants.Summer + ", " + Constants.summerShort + ", " + Constants.SummerShort);
			return null;
		}
		
		try
		{
			int yearInt = Integer.parseInt(year);
		}
		catch(Exception e)
		{
			log.warning("Invalid year in \"term\"");
			return null;
		}
		
		String result = null;
		if(semester.equals(Constants.Spring) || semester.equals(Constants.spring) || semester.equals(Constants.springShort) || semester.equals(Constants.SpringShort))
		{
			result = Constants.Spring + year;
		}
		else if(semester.equals(Constants.Summer) || semester.equals(Constants.summer) || semester.equals(Constants.summerShort) || semester.equals(Constants.SummerShort))
		{
			result = Constants.Summer + year;
		}
		else if(semester.equals(Constants.Fall) || semester.equals(Constants.fall))
		{
			result = Constants.Fall + year;
		}
		return result;
	}
	
	/**
	 * prepares the command from the string supplied by the user to be sent to the database instance
	 * @param commandStr - The command string read from user.
	 * @return a command instance having the proper arguments for the database.
	 */
	private Command PrepareCommand(String commandStr)
	{
		String[] splittedCommand = SplitCommandLine(commandStr);
		
		if(splittedCommand == null)
		{
			return null;
		}
		
		Command command = new Command();
		if(splittedCommand[0].equals(Constants.newClass))	//for creating a new class
		{
			if(splittedCommand.length != 5)
			{
				log.warning(Constants.invalidUsage + Constants.newClass);
				log.warning(Constants.usageOfCommand + Constants.newClass + Constants.colon + Constants.newClass
						    + Constants.space + Constants.courseNumber + Constants.space + Constants.term
						    + Constants.space + Constants.sectionNo + Constants.space + Constants.description);
				return null;
			}
			
			String modifiedTerm = isTermStructureSatisfied(splittedCommand[2]);
			if(modifiedTerm == null)	//checks whether term is valid
			{
				return null;
			}
			
			try
			{
				int sectionNo = Integer.parseInt(splittedCommand[3]);	//checks whether sectionNo is a valid integer
			}
			catch(Exception e)
			{
				log.warning("Section No must be an integer");
				return null;
			}
			
			command.SetCommandType(Constants.newClass);
			command.SetCourseNumber(splittedCommand[1]);
			command.SetCourseTerm(modifiedTerm);
			command.SetCourseSectionNo(splittedCommand[3]);
			command.SetCourseDescription(splittedCommand[4]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.listClasses))	//show all classes along with # of students
		{
			
			if(splittedCommand.length != 1)
			{
				log.warning(Constants.invalidUsage + Constants.listClasses);
				log.warning(Constants.usageOfCommand + Constants.listClasses + Constants.colon + Constants.listClasses);
				return null;
			}
			
			command.SetCommandType(Constants.listClasses);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.selectClass))	//activates a class
		{
			if(splittedCommand.length < 2 || splittedCommand.length > 4)
			{
				log.warning(Constants.invalidUsage + Constants.selectClass);
				log.warning(Constants.usageOfCommand + Constants.selectClass + Constants.colon + Constants.selectClass
						    + Constants.space + Constants.courseNumber + Constants.space + Constants.leftSquareBrace
						    + Constants.term + Constants.rightSquareBrace + Constants.space + Constants.leftSquareBrace
						    + Constants.sectionNo + Constants.rightSquareBrace);
				return null;
			}
			
			command.SetCommandType(Constants.selectClass);
			command.SetCourseNumber(splittedCommand[1]);
			if(splittedCommand.length > 2)
			{
				String modifiedTerm = isTermStructureSatisfied(splittedCommand[2]);	//checks whether term is valid
				if(modifiedTerm == null)
				{
					return null;
				}
				command.SetCourseTerm(modifiedTerm);
			}
			if(splittedCommand.length > 3)
			{
				try
				{
					int sectionNo = Integer.parseInt(splittedCommand[3]);	//checks whether section no is a valid integer
				}
				catch(Exception e)
				{
					log.warning(Constants.sectionNoMustBeInteger);
					return null;
				}
				
				command.SetCourseSectionNo(splittedCommand[3]);
			}
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showClass))		//shows the currently active class
		{
			if(splittedCommand.length != 1)
			{
				log.warning(Constants.invalidUsage + Constants.showClass);
				log.warning(Constants.usageOfCommand + Constants.showClass + Constants.colon + Constants.showClass);
				return null;
			}
			
			command.SetCommandType(Constants.showClass);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showCategories))	//shows all the categories in the active class
		{
			if(splittedCommand.length != 1)
			{
				log.warning(Constants.invalidUsage + Constants.showCategories);
				log.warning(Constants.usageOfCommand + Constants.showCategories + Constants.colon + Constants.showCategories);
				return null;
			}
			
			command.SetCommandType(Constants.showCategories);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.addCategory))	//adds a category
		{
			if(splittedCommand.length != 3)
			{
				log.warning(Constants.invalidUsage + Constants.addCategory);
				log.warning(Constants.usageOfCommand + Constants.addCategory + Constants.colon + Constants.addCategory
						    + Constants.space + Constants.name + Constants.space + Constants.weight);
				return null;
			}
			
			try
			{
				int weight = Integer.parseInt(splittedCommand[2]);		//checks whether weight is a valid integer
			}
			catch(Exception e)
			{
				log.warning(Constants.weightMustBeInteger);
				return null;
			}
			
			command.SetCommandType(Constants.addCategory);
			command.SetCategoryName(splittedCommand[1]);
			command.SetCategoryWeightForCourse(splittedCommand[2]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showAssignment))	//shows all the assignments for the active class
		{
			if(splittedCommand.length != 1)
			{
				log.warning(Constants.invalidUsage + Constants.showAssignment);
				log.warning(Constants.usageOfCommand + Constants.showAssignment + Constants.colon + Constants.showAssignment);
				return null;
			}
			
			command.SetCommandType(Constants.showAssignment);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.addAssignment))	//adds an assignment to the active class
		{
			if(splittedCommand.length != 5)
			{
				log.warning(Constants.invalidUsage + Constants.addAssignment);
				log.warning(Constants.usageOfCommand + Constants.addAssignment + Constants.colon + Constants.addAssignment
						    + Constants.space + Constants.name + Constants.space + Constants.category + Constants.space
						    + Constants.description + Constants.space + Constants.pointValue);
				return null;
			}
			
			try
			{
				int point = Integer.parseInt(splittedCommand[4]);	//checks whether point value is a valid integer
			}
			catch(Exception e)
			{
				log.warning(Constants.pointsMustBeInteger);
				return null;
			}
			
			command.SetCommandType(Constants.addAssignment);
			command.SetAssignmentName(splittedCommand[1]);
			command.SetAssignmentCategory(splittedCommand[2]);
			command.SetAssignmentDescription(splittedCommand[3]);
			command.SetAssignmentPointValue(splittedCommand[4]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.addStudent))	//adds a students and enrolls in the active class
		{
			if(splittedCommand.length != 5 && splittedCommand.length != 2)
			{
				log.warning(Constants.invalidUsage + Constants.addStudent);
				log.warning(Constants.usageOfCommand + Constants.addStudent + Constants.colon + Constants.addStudent
						    + Constants.space + Constants.userName + Constants.space + Constants.leftSquareBrace
						    + Constants.studentId + Constants.rightSquareBrace + Constants.space + Constants.leftSquareBrace
						    + Constants.last + Constants.rightSquareBrace + Constants.space + Constants.leftSquareBrace
						    + Constants.first + Constants.rightSquareBrace);
				return null;
			}
			
			command.SetCommandType(Constants.addStudent);
			command.SetStudentUserName(splittedCommand[1].toLowerCase());
			
			if(splittedCommand.length == 5)
			{
				try
				{
					int studentId = Integer.parseInt(splittedCommand[2]);	//checks whether student id is a valid integer
				}
				catch(Exception e)
				{
					log.warning(Constants.studentIdMustBeInteger);
					return null;
				}
				
				command.SetStudentId(splittedCommand[2]);
				command.SetStudentFullName(splittedCommand[4].toLowerCase() + Constants.space + splittedCommand[3].toLowerCase());
			}
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showStudents))	//shows students in the active class
		{
			if(splittedCommand.length > 2)
			{
				log.warning(Constants.invalidUsage + Constants.showStudents);
				log.warning(Constants.usageOfCommand + Constants.showStudents + Constants.colon + Constants.showStudents
						    + Constants.space + Constants.leftSquareBrace + Constants.usernamesubstring + Constants.rightSquareBrace);
				return null;
			}
			
			command.SetCommandType(Constants.showStudents);
			if(splittedCommand.length == 2)
			{
				command.SetUsernameSubstring(splittedCommand[1]);
			}
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.grade))	//assigns grade to student for particular assignment
		{
			if(splittedCommand.length != 4)
			{
				log.warning(Constants.invalidUsage + Constants.grade);
				log.warning(Constants.usageOfCommand + Constants.grade + Constants.colon + Constants.grade
						    + Constants.space + Constants.assignmentName + Constants.space + Constants.userName
						    + Constants.space + Constants.grade);
				
				return null;
			}
			
			try
			{
				int grade = Integer.parseInt(splittedCommand[3]);	//check whether grade is a valid integer
			}
			catch(Exception e)
			{
				log.warning(Constants.gradeMustBeInteger);
				return null;
			}
			
			command.SetCommandType(Constants.grade);
			command.SetAssignmentName(splittedCommand[1]);
			command.SetStudentUserName(splittedCommand[2]);
			command.SetStudentReceivedGradeForCourse(splittedCommand[3]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.studentGrades))		//reports particular students grades in the active class
		{
			if(splittedCommand.length != 2)
			{
				log.warning(Constants.invalidUsage + Constants.studentGrades);
				log.warning(Constants.usageOfCommand + Constants.studentGrades + Constants.colon + Constants.studentGrades
						    + Constants.space + Constants.userName);
				
				return null;
			}
			
			command.SetCommandType(Constants.studentGrades);
			command.SetStudentUserName(splittedCommand[1]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.gradebook))	//reports grades of all students in the active class
		{
			if(splittedCommand.length != 1)
			{
				log.warning(Constants.invalidUsage + Constants.gradebook);
				log.warning(Constants.usageOfCommand + Constants.gradebook + Constants.colon + Constants.gradebook);
				
				return null;
			}
			
			command.SetCommandType(Constants.gradebook);
			return command;
		}
		else if(splittedCommand[0].equals(Constants.importGrades))
		{
			if(splittedCommand.length != 3)
			{
				log.warning(Constants.invalidUsage + Constants.importGrades);
				log.warning(Constants.usageOfCommand + Constants.importGrades + Constants.colon + Constants.importGrades
						   +Constants.space + Constants.assignmentName + Constants.space + Constants.fileName);
				return null;
			}
			
			if(!splittedCommand[2].endsWith(".csv"))
			{
				log.warning("Invalid file name!");
				return null;
			}
			
			command.SetCommandType(Constants.importGrades);
			command.SetAssignmentName(splittedCommand[1]);
			command.SetFileName(splittedCommand[2]);
			return command;
		}
		else
		{
			log.warning(Constants.invalidCommand);
			return null;
		}
	}
	
	/**
	 * closes the database connection
	 */
	public void CloseDatabase()
	{
		this.dbConnection.CloseConnection();
	}

}