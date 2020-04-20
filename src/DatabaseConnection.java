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
		else if(command.GetCommandType().equals(Constants.listClasses))
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
			
		}

		return true;
			
	}
	
	public void SetRecentTerm()
	{
		//select distinct term from class
		String query = Constants.select + Constants.distinct + Constants.term + Constants.space + Constants.from + Constants.classString + Constants.semiColon;
		
		
	}

}
