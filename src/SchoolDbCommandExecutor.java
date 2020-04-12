import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchoolDbCommandExecutor
{
	private Logger log;
	
	public static void main(String[] args)
	{
		SchoolDbCommandExecutor schoolDbCommandExecutor = new SchoolDbCommandExecutor();
		schoolDbCommandExecutor.ExecuteCommands();
	}
	
	public SchoolDbCommandExecutor()
	{
		this.log = Logger.getLogger(SchoolDbCommandExecutor.class.getName());
		this.log.setLevel(Level.ALL);
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
			}
			
			Command result = PrepareCommand(line);
			
			if(result != null)
			{
				System.out.println("Valid command!");
				System.out.println(result);
			}
			else
			{
				System.out.println("Invalid command!!");
			}
		}
	}
	
	private Command PrepareCommand(String commandStr)
	{
		String[] splittedCommand = commandStr.split("\\s+");
		
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
		else
		{
			log.warning(Constants.invalidCommand);
			return null;
		}
	}

}