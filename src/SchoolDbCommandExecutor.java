import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchoolDbCommandExecutor
{
	private Logger log;
	private DatabaseConnection dbConnection;
	
	public static void main(String[] args)
	{
		SchoolDbCommandExecutor schoolDbCommandExecutor = new SchoolDbCommandExecutor();
		
		//Adding shut down hook
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
		schoolDbCommandExecutor.ExecuteCommands();
	}
	
	public SchoolDbCommandExecutor()
	{
		this.log = Logger.getLogger(SchoolDbCommandExecutor.class.getName());
		this.log.setLevel(Level.ALL);
		
		System.out.println("attempting to open a db instance");
		this.dbConnection = new DatabaseConnection(this.log);
		System.out.println("attempting to establish db connection");
		this.dbConnection.EstablistDatabaseConnection();
		/*System.out.println("attempting to close db connection");
		this.dbConnection.CloseConnection();*/
	}
	
	private void ExecuteCommands()
	{
		Scanner scan = new Scanner(System.in);
		
		boolean exit = false;
		
		while(!exit)
		{
			System.out.println("reading line...");
			String line = scan.nextLine();
			
			if(line.equals("exit"))
			{
				exit = true;
				log.info("closing database connection");
				this.CloseDatabase();
				continue;
			}
			
			//String[] result = SplitCommandLine(line);
			
			Command command = PrepareCommand(line);
			
			if(command != null)
			{
				System.out.println("Valid command!");
				System.out.println(command);
				
				boolean executionResult = this.dbConnection.ExecuteQuery(command);
			}
			else
			{
				System.out.println("Invalid command!!");
			}
			
			
		}
	}
	
	private int CountCharacterInString(String line, char character)
	{
		return (int)line.chars().filter(num -> num == character).count();
	}
	
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
	
	private String[] SplitCommandLine(String commandStr)
	{
		if(CountCharacterInString(commandStr, Constants.quoteMark) % 2 != 0)
		{
			System.out.println("Invalid");
			return null;
		}
		if(!IsStringQuoteValid(commandStr))
		{
			System.out.println("Invalid2");
			return null;
		}
		
		String[] result = null;
		
		int startIndex = 0;
		int endIndex = 0;
		boolean didQuoteStart = false;
		int resultCounter = 0;
		System.out.println("Total string length: " + commandStr.length());
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
		
		System.out.println("Splitting commandline: Found length: " + resultCounter + ", actual: " + result.length);
		
		System.out.println("arguments:");
		for(int i=0;i<result.length;i++)
		{
			System.out.println(result[i]);
		}
		
		return result;
		
	}
	
	private Command PrepareCommand(String commandStr)
	{
		String[] splittedCommand = SplitCommandLine(commandStr);
		
		if(splittedCommand == null)
		{
			return null;
		}
		
		Command command = new Command();
		if(splittedCommand[0].equals(Constants.newClass))
		{
			if(splittedCommand.length != 5)
			{
				log.warning(Constants.invalidUsage + Constants.newClass);
				log.warning(Constants.usageOfCommand + Constants.newClass + Constants.colon + Constants.newClass
						    + Constants.space + Constants.courseNumber + Constants.space + Constants.term
						    + Constants.space + Constants.sectionNo + Constants.space + Constants.description);
				return null;
			}
			
			command.SetCommandType(Constants.newClass);
			command.SetCourseNumber(splittedCommand[1]);
			command.SetCourseTerm(splittedCommand[2]);
			command.SetCourseSectionNo(splittedCommand[3]);
			command.SetCourseDescription(splittedCommand[4]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.listClasses))
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
		else if(splittedCommand[0].equals(Constants.selectClass))
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
				command.SetCourseTerm(splittedCommand[2]);
			}
			if(splittedCommand.length > 3)
			{
				command.SetCourseSectionNo(splittedCommand[3]);
			}
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showClass))
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
		else if(splittedCommand[0].equals(Constants.showCategories))
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
		else if(splittedCommand[0].equals(Constants.addCategory))
		{
			if(splittedCommand.length != 3)
			{
				log.warning(Constants.invalidUsage + Constants.addCategory);
				log.warning(Constants.usageOfCommand + Constants.addCategory + Constants.colon + Constants.addCategory
						    + Constants.space + Constants.name + Constants.space + Constants.weight);
				return null;
			}
			
			command.SetCommandType(Constants.addCategory);
			command.SetCategoryName(splittedCommand[1]);
			command.SetCategoryWeightForCourse(splittedCommand[2]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showAssignment))
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
		else if(splittedCommand[0].equals(Constants.addAssignment))
		{
			if(splittedCommand.length != 5)
			{
				log.warning(Constants.invalidUsage + Constants.addAssignment);
				log.warning(Constants.usageOfCommand + Constants.addAssignment + Constants.colon + Constants.addAssignment
						    + Constants.space + Constants.name + Constants.space + Constants.category + Constants.space
						    + Constants.description + Constants.space + Constants.pointValue);
				return null;
			}
			
			command.SetCommandType(Constants.addAssignment);
			command.SetAssignmentName(splittedCommand[1]);
			command.SetAssignmentCategory(splittedCommand[2]);
			command.SetAssignmentDescription(splittedCommand[3]);
			command.SetAssignmentPointValue(splittedCommand[4]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.addStudent))
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
			command.SetStudentUserName(splittedCommand[1]);
			
			if(splittedCommand.length == 5)
			{
				command.SetStudentId(splittedCommand[2]);
				command.SetStudentFullName(splittedCommand[4] + Constants.space + splittedCommand[3]);
			}
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.showStudents))
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
		else if(splittedCommand[0].equals(Constants.grade))
		{
			if(splittedCommand.length != 4)
			{
				log.warning(Constants.invalidUsage + Constants.grade);
				log.warning(Constants.usageOfCommand + Constants.grade + Constants.colon + Constants.grade
						    + Constants.space + Constants.assignmentName + Constants.space + Constants.userName
						    + Constants.space + Constants.grade);
				
				return null;
			}
			
			command.SetCommandType(Constants.grade);
			command.SetAssignmentName(splittedCommand[1]);
			command.SetStudentUserName(splittedCommand[2]);
			command.SetStudentReceivedGradeForCourse(splittedCommand[3]);
			
			return command;
		}
		else if(splittedCommand[0].equals(Constants.studentGrades))
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
		else if(splittedCommand[0].equals(Constants.gradebook))
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
	public void CloseDatabase()
	{
		this.dbConnection.CloseConnection();
	}

}