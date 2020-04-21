import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseConnection
{
	private Logger log;
	private Connection connection = null;
	private String nRemotePort = "53488";
	private String strDbPassword = "Databasemm0";
	private Statement statement;
	private String mostRecentTerm = null;
	private String currentlyActiveClassId = null;
	private String currentlyActiveClassCourseNumber = null;
	private String currentlyActiveClassTerm = null;
	private String currentlyActiveClassSectionNo = null;
	private String currentlyActiveClassDescription = null;
	public DatabaseConnection(Logger log)
	{
		this.log = log;
		try
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
			log.info("Class found!");
		}
		catch (ClassNotFoundException e)
		{
			log.warning("Class not found exception occured during class lookup: " + e.getStackTrace());
		}
	}
	
	public boolean EstablistDatabaseConnection()
	{
		
		if(this.connection != null)
		{
			return true;
		}		
		
		try
		{
			this.connection = DriverManager.getConnection("jdbc:mysql://localhost:"+nRemotePort+"/SchoolDb?verifyServerCertificate=false&useSSL=true&serverTimezone=UTC", "msandbox", strDbPassword);
			log.info("Database connection established!");
			this.statement = connection.createStatement();
			log.info("Statement read for query execution");
			return true;
		}
		catch (SQLException e)
		{
			log.warning("SQLException occured during connection establishment: ");
			e.printStackTrace();
			return false;
		} 
	}
	
	public boolean CloseConnection()
	{
		try
		{
			System.out.println("Coming here");
			this.connection.close();
			log.info("Connection closed successfully!");
			return true;
		}
		catch (SQLException e)
		{
			log.warning("SQLException occured during connection closing: " + e.getStackTrace());
			return false;
		}
	}
	
	public boolean CreateSchoolDB()
	{
		try
		{
			statement.executeUpdate(Constants.createDb + Constants.schoolDb);
			return true;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException occured during database creation: " + e.getStackTrace());
			return false;
		}
	}
	
	public boolean ExecuteQuery(Command command)
	{
		//statement.executeQuery(sql);
		String query;
		
		//class management
		if(command.GetCommandType().equals(Constants.newClass))
		{
			query = Constants.insertInto + Constants.classString + Constants.space
				  + Constants.leftBrace + Constants.courseNumber + Constants.commaSpace + Constants.term + Constants.commaSpace + Constants.sectionNo + Constants.commaSpace + Constants.classDescription + Constants.rightBrace + Constants.space
				  + Constants.values + Constants.leftBrace + Constants.singleQuote + command.GetCourseNumber() + Constants.singleQuote + Constants.commaSpace + Constants.singleQuote + command.GetCourseTerm() + Constants.singleQuote + Constants.commaSpace + command.GetCourseSectionNo() + Constants.commaSpace + Constants.singleQuote + command.GetCourseDescription() + Constants.singleQuote + Constants.rightBrace + Constants.semiColon;
			System.out.println("Query string: " + query);
		}
		else if(command.GetCommandType().equals(Constants.listClasses))	// join needs to be checked.........................
			//..................................
			//
			//.....................................
		{
			//select c.course_number, c.term, c.section_no, count(en.student_id) as student_count
			//from class c join enrolled_in en
			//on c.course_id = en.course_id
			//group by c.course_number, c.term, c.section_no
			query = Constants.select + Constants.classCL + Constants.dot + Constants.courseNumber + Constants.commaSpace + Constants.classCL + Constants.dot + Constants.term
				  + Constants.commaSpace + Constants.classCL + Constants.dot + Constants.sectionNo + Constants.commaSpace + Constants.count + Constants.leftBrace
				  + Constants.enrolledInEN + Constants.dot + Constants.studentId + Constants.rightBrace + Constants.space + Constants.as + Constants.studentCount + Constants.commaSpace
				  + Constants.from + Constants.classString + Constants.space + Constants.classCL + Constants.space + Constants.join + Constants.enrolledIn + Constants.space + Constants.enrolledInEN + Constants.space
				  + Constants.on + Constants.classCL + Constants.dot + Constants.courseId + Constants.space + Constants.equals + Constants.enrolledInEN + Constants.dot + Constants.courseId + Constants.space
				  + Constants.groupBy + Constants.classCL + Constants.dot + Constants.courseNumber + Constants.commaSpace + Constants.classCL + Constants.dot + Constants.term + Constants.commaSpace + Constants.classCL + Constants.dot + Constants.sectionNo + Constants.semiColon;
			System.out.println("Query string: " + query);
			
		}
		else if(command.GetCommandType().equals(Constants.selectClass))
		{
			
		}
		else if(command.GetCommandType().equals(Constants.showClass))
		{
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			else
			{
				System.out.println("Currently Active Class: ");
				System.out.print("Id: " + this.currentlyActiveClassId + ", course_number: " + this.currentlyActiveClassCourseNumber + ", term: " + this.currentlyActiveClassTerm
								+ ",\n" + "section_no: " + this.currentlyActiveClassSectionNo + ", description: " + this.currentlyActiveClassDescription);
			}			
		}
		//category and assignment management
		else if(command.GetCommandType().equals(Constants.showCategories))
		{
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			//select con.category_id, cat.category_name, con.weight
			//from class cl join contains con on cl.course_id = con.course_id
			//join category cat on con.category_id = cat.category_id
			//where cl.course_id = this.currentlyActiveClassId
			query = "SELECT con.category_id, cat.category_name, con.weight FROM class cl JOIN contains con ON cl.course_id = con.course_id "
				  + "JOIN category cat ON con.category_id = cat.category_id WHERE cl.course_id = " + this.currentlyActiveClassId;
			
			System.out.println("Query string: " + query);
		}
		else if(command.GetCommandType().equals(Constants.addCategory))
		{
			//add-category Name weight
			//we have to add the name to category, get the id and then add the weight in contains
			String tempQuery = "insert into"
			query = "insert into contains (course_id, category_id, weight) values (" + this.currentlyActiveClassId + ", " + theCategoryId + ", " + command.GetCategoryWeightForCourse() + ");";
		}

		return true;
			
	}
	
	public void SetRecentTerm()
	{
		//select distinct term from class
		String query = Constants.select + Constants.distinct + Constants.term + Constants.space + Constants.from + Constants.classString + Constants.semiColon;
		
		
	}

}
