import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/***
 * 
 * @author Tahsin Imtiaz
 * This represents the Database connector which
 */
public class DatabaseConnection
{
	private Logger log;	//the logger
	private Connection connection = null;	//hold the database connection
	private String nRemotePort = "53488";	//my port for onyx
	private String strDbPassword = "Databasemm0";	//my password for onyx
	private Statement statement;	//the statement used for executing query
	private String mostRecentTerm = null;	//holds the most recent term
	private String currentlyActiveClassId = null;	//holds the id of the currently active class
	private String currentlyActiveClassCourseNumber = null;
	private String currentlyActiveClassTerm = null;
	private String currentlyActiveClassSectionNo = null;
	private String currentlyActiveClassDescription = null;
	
	/**
	 * Creates one instance of SchoolDbCommandExecutor
	 * @param log - the logger
	 */
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
	
	/**
	 * establishes the database connection
	 * @return a boolean value indicating whether the connection is established or not.
	 */
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
			
			//most recent term
			this.mostRecentTerm = this.RetrieveMostRecentTerm();
			if(this.mostRecentTerm == null)
			{
				System.out.println("Most recent term: " + "null");
			}
			else
			{
				System.out.println("Most recent term: " + this.mostRecentTerm);
			}
			
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
	
	/*public boolean CreateSchoolDB()
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
	}*/
	
	/**
	 * Executes the given command
	 * @param command - the command instance holding all the information
	 * @return a boolean value indicating whether the command is successfully executed
	 */
	public boolean ExecuteQuery(Command command)
	{
		String query;
		
		//class management
		if(command.GetCommandType().equals(Constants.newClass))
		{
			//to insert class
			//first we need to check whether this class already exists
			try
			{
				String existQuery = "SELECT * FROM class WHERE course_number='" + command.GetCourseNumber() + "' AND term='" + command.GetCourseTerm()
								  + "' AND section_no='" + command.GetCourseSectionNo() + "';";
				ResultSet checkExist = this.statement.executeQuery(existQuery);
				
				if(checkExist.next())
				{
					log.warning(Constants.classAlreadyExists);
					return false;
				}
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.newClassInsertion);
				e.printStackTrace();
				return false;
			}
			
			//since everything is okay, we can proceed to insert the new class.
			try
			{
				query = "insert into class (course_number, term, section_no, class_description) values ('" + command.GetCourseNumber()
					  + "', '" + command.GetCourseTerm() + "', " + command.GetCourseSectionNo() + ", '" + command.GetCourseDescription() + "');";
				statement.executeUpdate(query);
				log.info(Constants.newClassInserted);
				
				//modify the most recent term
				this.mostRecentTerm = this.RetrieveMostRecentTerm();
				return true;
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.newClassInsertion);
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.listClasses))
		{			
			try
			{
				query = "SELECT cl.course_number, cl.term, cl.section_no, COUNT(en.student_id) AS student_count FROM class cl LEFT JOIN enrolled_in en ON cl.course_id = en.course_id GROUP BY cl.course_number, cl.term, cl.section_no;";
				
				ResultSet result = statement.executeQuery(query);
				
				boolean isResultFound = false;
				String[][] resultTable = null;
				int tableCounter = 0;
				while(result.next())
				{
					isResultFound = true;
					
					if(resultTable == null)
					{
						resultTable = new String[1][4];
						resultTable[tableCounter] = new String[4];
					}
					else
					{
						resultTable = Arrays.copyOf(resultTable, tableCounter+1);
						resultTable[tableCounter] = new String[4];
					}
					
					resultTable[tableCounter][0] = new String(result.getString("course_number"));
					resultTable[tableCounter][1] = new String(result.getString("term"));
					resultTable[tableCounter][2] = new String(String.valueOf(result.getInt("section_no")));
					resultTable[tableCounter][3] = new String(String.valueOf(result.getInt("student_count")));
					
					tableCounter++;
					
				}
				
				if(!isResultFound)
				{
					log.warning(Constants.noDataFound);
					return false;
				}
				else
				{
					this.PrintData(Constants.listClasses, resultTable, null);//, tableCounter, 4);
				}
				return true;
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.retrieveClassList);
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.selectClass))
		{
			if(command.GetCourseTerm() == null && command.GetCourseSectionNo() == null)	//only course_number given
			{				
				try
				{
					if(this.mostRecentTerm == null)
					{
						log.warning(Constants.mostRecentTermUndefined);
						return false;
					}
					
					query = "SELECT * FROM class WHERE course_number='" + command.GetCourseNumber() + "' AND term='"
						  + this.mostRecentTerm + "';";
					ResultSet result = statement.executeQuery(query);
					
					String tempCurrentlyActiveClassId = null;
					String tempCurrentlyActiveClassCourseNumber = null;
					String tempCurrentlyActiveClassTerm = null;
					String tempCurrentlyActiveClassSectionNo = null;
					String tempCurrentlyActiveClassDescription = null;
					
					int counter = 0;
					String[][] resultTable = null;
					while(result.next())
					{
						if(resultTable == null)
						{
							resultTable = new String[1][4];
							resultTable[counter] = new String[4];
						}
						else
						{
							resultTable = Arrays.copyOf(resultTable, counter+1);
							resultTable[counter] = new String[4];
						}
						
						resultTable[counter][0] = new String(String.valueOf(result.getInt("course_id")));
						resultTable[counter][1] = new String(result.getString("course_number"));
						resultTable[counter][2] = new String(result.getString("term"));
						resultTable[counter][3] = new String(String.valueOf(result.getInt("section_no")));
						
						
						if(counter == 0)
						{
							tempCurrentlyActiveClassId = String.valueOf(result.getInt("course_id"));
							tempCurrentlyActiveClassCourseNumber = result.getString("course_number");
							tempCurrentlyActiveClassTerm = result.getString("term");
							tempCurrentlyActiveClassSectionNo = String.valueOf(result.getInt("section_no"));
							tempCurrentlyActiveClassDescription = result.getString("class_description");
						}
						counter++;
					}
					
					log.info(Constants.availableClasses);
					this.PrintData(Constants.selectClass, resultTable, null);
					
					if(counter == 0)
					{
						log.warning(Constants.noClassFound);
						return false;
					}
					if(counter > 1)
					{						
						log.warning(Constants.tooManyClassCannotActivateClass);
						return false;
					}
					
					log.info(Constants.activatingClass + tempCurrentlyActiveClassCourseNumber + ", " + tempCurrentlyActiveClassTerm + ", " + tempCurrentlyActiveClassSectionNo);
					this.currentlyActiveClassId = tempCurrentlyActiveClassId;
					this.currentlyActiveClassCourseNumber = tempCurrentlyActiveClassCourseNumber;
					this.currentlyActiveClassTerm = tempCurrentlyActiveClassTerm;
					this.currentlyActiveClassSectionNo = tempCurrentlyActiveClassSectionNo;
					this.currentlyActiveClassDescription = tempCurrentlyActiveClassDescription;
					
					return true;
				}
				catch(SQLException e)
				{
					log.warning(Constants.sqlExceptionOccured + Constants.selectingClass);
					e.printStackTrace();
					return false;
				}
			}
			else if(command.GetCourseSectionNo() == null)	//course_number, term given
			{				
				try
				{
					query = "SELECT * FROM class WHERE course_number='" + command.GetCourseNumber() + "' AND term='"
						  + command.GetCourseTerm() + "';";
					
					ResultSet result = statement.executeQuery(query);
					
					String tempCurrentlyActiveClassId = null;
					String tempCurrentlyActiveClassCourseNumber = null;
					String tempCurrentlyActiveClassTerm = null;
					String tempCurrentlyActiveClassSectionNo = null;
					String tempCurrentlyActiveClassDescription = null;
					
					int counter = 0;
					String[][] resultTable = null;
					while(result.next())
					{
						if(resultTable == null)
						{
							resultTable = new String[1][4];
							resultTable[counter] = new String[4];
						}
						else
						{
							resultTable = Arrays.copyOf(resultTable, counter+1);
							resultTable[counter] = new String[4];
						}
						
						resultTable[counter][0] = new String(String.valueOf(result.getInt("course_id")));
						resultTable[counter][1] = new String(result.getString("course_number"));
						resultTable[counter][2] = new String(result.getString("term"));
						resultTable[counter][3] = new String(String.valueOf(result.getInt("section_no")));
						/*System.out.println(result.getInt("course_id") + ", " + result.getString("course_number")
										 + ", " + result.getString("term") + ", " + result.getString("section_no"));*/
						
						if(counter == 0)
						{
							tempCurrentlyActiveClassId = String.valueOf(result.getInt("course_id"));
							tempCurrentlyActiveClassCourseNumber = result.getString("course_number");
							tempCurrentlyActiveClassTerm = result.getString("term");
							tempCurrentlyActiveClassSectionNo = String.valueOf(result.getInt("section_no"));
							tempCurrentlyActiveClassDescription = result.getString("class_description");
						}
						counter++;
					}
					
					log.info(Constants.availableClasses);
					this.PrintData(Constants.selectClass, resultTable, null);
					
					if(counter == 0)
					{
						log.warning(Constants.noClassFound);
						return false;
					}
					if(counter > 1)
					{
						log.warning(Constants.tooManyClassCannotActivateClass);
						return false;
					}
					
					log.info(Constants.activatingClass + tempCurrentlyActiveClassCourseNumber + ", " + tempCurrentlyActiveClassTerm + ", " + tempCurrentlyActiveClassSectionNo);
					this.currentlyActiveClassId = tempCurrentlyActiveClassId;
					this.currentlyActiveClassCourseNumber = tempCurrentlyActiveClassCourseNumber;
					this.currentlyActiveClassTerm = tempCurrentlyActiveClassTerm;
					this.currentlyActiveClassSectionNo = tempCurrentlyActiveClassSectionNo;
					this.currentlyActiveClassDescription = tempCurrentlyActiveClassDescription;
					
					return true;
				}
				catch (SQLException e)
				{
					log.warning(Constants.sqlExceptionOccured + Constants.activateClass);
					e.printStackTrace();
					return false;
				} 
			}
			else	//course_number, term and section given
			{					
				try
				{
					query = "SELECT * FROM class WHERE course_number='" + command.GetCourseNumber() + "' AND term='"
						  + command.GetCourseTerm() + "' AND section_no=" + command.GetCourseSectionNo() + ";";
					
					ResultSet result = statement.executeQuery(query);
					
					if(result.next())
					{
						this.currentlyActiveClassId = String.valueOf(result.getInt("course_id"));
						this.currentlyActiveClassCourseNumber = result.getString("course_number");
						this.currentlyActiveClassTerm = result.getString("term");
						this.currentlyActiveClassSectionNo = String.valueOf(result.getInt("section_no"));
						this.currentlyActiveClassDescription = result.getString("class_description");
						
						log.info(Constants.activatingClass + this.currentlyActiveClassCourseNumber + ", " + this.currentlyActiveClassTerm + ", " + this.currentlyActiveClassSectionNo);
						return true;
					}
					else
					{
						log.warning(Constants.noClassFound);
						return false;
					}
				}
				catch (SQLException e)
				{
					log.warning(Constants.sqlExceptionOccured + Constants.activateClass);
					e.printStackTrace();
					return false;
				}
			}
		}
		else if(command.GetCommandType().equals(Constants.showClass))
		{
			if(this.currentlyActiveClassId == null)
			{
				log.warning(Constants.noActiveClass);
				return false;
			}
			else
			{
				System.out.println("Currently Active Class: ");
				String[][] resultTable = new String[1][4];
				resultTable[0] = new String[4];
				resultTable[0][0] = new String(String.valueOf(this.currentlyActiveClassId));
				resultTable[0][1] = new String(this.currentlyActiveClassCourseNumber);
				resultTable[0][2] = new String(this.currentlyActiveClassTerm);
				resultTable[0][3] = new String(String.valueOf(currentlyActiveClassSectionNo));
				this.PrintData(Constants.showClass, resultTable, null);
				
				System.out.println(Constants.activatedClassDescription + this.currentlyActiveClassDescription);
				return true;
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
			
			try
			{
				query = "SELECT con.category_id, cat.category_name, con.weight FROM class cl JOIN contains con ON cl.course_id = con.course_id "
					  + "JOIN category cat ON con.category_id = cat.category_id WHERE cl.course_id = " + this.currentlyActiveClassId;
				
				ResultSet result = statement.executeQuery(query);
				
				log.info(Constants.showingCategoryForClass + this.currentlyActiveClassCourseNumber);
				int counter = 0;
				String[][] resultTable = null;
				while(result.next())
				{
					if(resultTable == null)
					{
						resultTable = new String[1][3];
						resultTable[counter] = new String[3];
					}
					else
					{
						resultTable = Arrays.copyOf(resultTable, counter+1);
						resultTable[counter] = new String[3];
					}
					
					resultTable[counter][0] = new String(String.valueOf(result.getInt("category_id")));
					resultTable[counter][1] = new String(result.getString("category_name"));
					resultTable[counter][2] = new String(String.valueOf(result.getInt("weight")));
					
					counter++;
				}
				
				this.PrintData(Constants.showCategories, resultTable, null);
				if(counter == 0)
				{
					log.warning("No data found..");
					return false;
				}
				else
				{
					return true;
				}
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during showing categories...");
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.addCategory))
		{
			//add-category CategoryName weight
			//we have to add the name to category first if not already exists,
			//get the id and then add the weight in contains
			
			//if there is not active class this query is meaningliess
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			//first checking whether this category exists
			try
			{
				String categoryCheckQuery = "SELECT * FROM category WHERE category_name='" + command.GetCategoryName() + "';";
				ResultSet categoryCheckResult = statement.executeQuery(categoryCheckQuery);
				
				String currentCategoryId = null;
				int currentCategoryWeight = -1;
				if(categoryCheckResult.next())	//category exists in category table, get the id
				{
					System.out.println(Constants.categoryExists);
					currentCategoryId = String.valueOf(categoryCheckResult.getInt("category_id"));
				}
				else	//category doesn't exist in the category table, so add there first
				{
					System.out.println(Constants.categoryDoesNotExist);
					System.out.println(Constants.addingTheCategoryFirst);
					String addCategoryQuery = "insert into category (category_name) values ('" + command.GetCategoryName() + "');";
					statement.executeUpdate(addCategoryQuery);
					
					//now get the recently inserted id
					categoryCheckResult = statement.executeQuery(categoryCheckQuery);
					boolean categoryCheckBoolResult = categoryCheckResult.next();
					currentCategoryId = String.valueOf(categoryCheckResult.getInt("category_id"));
				}
				
				//now the category exists no matter what, we can just add the weight in contains
				//now we have to check whether this category is already exists in contains for this class
				String categoryClassContainCheckQuery = "SELECT * FROM contains WHERE course_id=" + this.currentlyActiveClassId
												      + " AND category_id=" + currentCategoryId + ";";
				ResultSet categoryClassContainCheckResult = statement.executeQuery(categoryClassContainCheckQuery);
				
				if(categoryClassContainCheckResult.next())	//this category is already added for this class
				{
					//getting the previous weight
					currentCategoryWeight = categoryClassContainCheckResult.getInt("weight");
					//if categroy already exists for this class, we replace the weight
					log.warning(Constants.categoryAlreadyExists);
					//first we need to check if total weight will sum to equal to or less than 100
					String weightSumQuery = "SELECT SUM(weight) AS total_weight FROM contains WHERE course_id=" + this.currentlyActiveClassId + ";";
					ResultSet weightSumResult = statement.executeQuery(weightSumQuery);
					boolean weightSumBoolResult = weightSumResult.next();
					int sum = weightSumResult.getInt("total_weight") - currentCategoryWeight + Integer.parseInt(command.GetCategoryWeightForCourse());
					
					//check sum
					if(sum > 100)
					{
						log.warning(Constants.weightExceedLimit);
						return false;
					}
					//otherwise everything is okay, we need to update the weight
					String updateCategoryWeightQuery = "UPDATE contains SET weight=" + command.GetCategoryWeightForCourse() + " WHERE category_id=" + currentCategoryId + " AND course_id=" + this.currentlyActiveClassId + ";";
					statement.executeUpdate(updateCategoryWeightQuery);
					log.info(Constants.givenCategoryWeightUpdated);
					return true;
				}
				
				//we need to check weight constraints
				String weightSumQuery = "SELECT SUM(weight) AS total_weight FROM contains WHERE course_id=" + this.currentlyActiveClassId + ";";
				ResultSet weightSumResult = statement.executeQuery(weightSumQuery);
				boolean weightSumBoolResult = weightSumResult.next();
				int sum = weightSumResult.getInt("total_weight") + Integer.parseInt(command.GetCategoryWeightForCourse());
				//check sum
				if(sum > 100)
				{
					log.warning(Constants.weightExceedLimit);
					return false;
				}
				//otherwise now we can add this category to contains
				String insertCategoryWeightQuery = "insert into contains (course_id, category_id, weight) values (" + this.currentlyActiveClassId
											+ ", " + currentCategoryId + ", " + command.GetCategoryWeightForCourse() + ");";
				
				statement.executeUpdate(insertCategoryWeightQuery);
				log.info(Constants.givenCategoryAdded);
				return true;
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during adding category...");
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.showAssignment))
		{
			//if there is not active class this query is meaningless
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			try
			{
				String showAssignmentQuery = "SELECT cat.category_id, cat.category_name, asn.assignment_id, asn.assignment_name, asn.point_value "
										   + "FROM assignment asn JOIN category cat ON asn.category_id=cat.category_id "
										   + "WHERE asn.course_id=" + this.currentlyActiveClassId + ";";
				ResultSet result = statement.executeQuery(showAssignmentQuery);
				
				log.info(Constants.showingAssignmentForCurrentClass);
				
				
				String[][] resultTable = null;
				int counter = 0;
				while(result.next())
				{
					if(resultTable == null)
					{
						resultTable = new String[1][5];
						resultTable[counter] = new String[5];
					}
					else
					{
						resultTable = Arrays.copyOf(resultTable, counter+1);
						resultTable[counter] = new String[5];
					}
					
					resultTable[counter][0] = new String(String.valueOf(result.getInt("category_id")));
					resultTable[counter][1] = new String(result.getString("category_name"));
					resultTable[counter][2] = new String(String.valueOf(result.getInt("assignment_id")));
					resultTable[counter][3] = new String(result.getString("assignment_name"));
					resultTable[counter][4] = new String(String.valueOf(result.getInt("point_value")));
					
					counter++;
				}
				
				if(counter == 0)
				{
					log.warning(Constants.noDataFound);
					return false;
				}
				else
				{
					this.PrintData(Constants.showAssignment, resultTable, null);
					return true;
				}
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.showingCategory);
				e.printStackTrace();
				return false;
			}
			
		}
		else if(command.GetCommandType().equals(Constants.addAssignment))
		{
			//assignment-name category description points
			//if there is not active class this query is meaningless
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			try
			{
				String assignmentExistanceCheckQuery = "SELECT * FROM assignment WHERE course_id=" + this.currentlyActiveClassId
													 + " AND assignment_name='" + command.GetAssignmentName() + "';";
				
				ResultSet assignmentExistanceCheckResult = statement.executeQuery(assignmentExistanceCheckQuery);
				
				if(assignmentExistanceCheckResult.next())
				{
					int oldAssignmentId = assignmentExistanceCheckResult.getInt("assignment_id");
					int oldCategoryId = assignmentExistanceCheckResult.getInt("category_id");
					log.warning("Assignment with same name already exists in this course! replacing it");
					
					//check category
					String getCategoryQuery = "SELECT * FROM contains con JOIN category cat ON con.category_id=cat.category_id "
											+ "WHERE cat.category_name='" + command.GetAssignmentCategory() + "' AND con.course_id=" + this.currentlyActiveClassId + ";";
					
					ResultSet getCategoryResult = statement.executeQuery(getCategoryQuery);
					
					String newCategoryId = null;
					if(getCategoryResult.next())
					{
						newCategoryId = String.valueOf(getCategoryResult.getInt("cat.category_id"));
					}
					else
					{
						log.warning("current class does not have the given category..");
						return false;
					}
					//
					String updateAssignmentQuery = "UPDATE assignment SET point_value=" + command.GetAssignmentPointValue() + ", assignment_description='" + command.GetAssignmentDescription() + "', category_id=" + newCategoryId + " WHERE assignment_id=" + oldAssignmentId + " AND assignment_name='" + command.GetAssignmentName() + "' AND course_id=" + this.currentlyActiveClassId + " AND category_id=" + oldCategoryId + ";";
					statement.executeUpdate(updateAssignmentQuery);
					log.info(Constants.assignmentAdded);
					return true;
				}
				
				//if it doesn't already exists, we add it
				//first we need the category id
				System.out.println("Assignment doesnt already exist");
				String getCategoryQuery = "SELECT * FROM contains con JOIN category cat ON con.category_id=cat.category_id "
										+ "WHERE cat.category_name='" + command.GetAssignmentCategory() + "' AND con.course_id=" + this.currentlyActiveClassId + ";";
				System.out.println("getCategoryQuery: " + getCategoryQuery);
				
				ResultSet getCategoryResult = statement.executeQuery(getCategoryQuery);
				
				String currentCategoryId = null;
				if(getCategoryResult.next())
				{
					currentCategoryId = String.valueOf(getCategoryResult.getInt("cat.category_id"));
				}
				else
				{
					log.warning("current class does not have the given category..");
					return false;
				}
				
				//finally we add the assignment
				String addAssignmentQuery = "insert into assignment (assignment_name, assignment_description, point_value, course_id, category_id) values ('"
										  + command.GetAssignmentName() + "', '" + command.GetAssignmentDescription() + "', " + command.GetAssignmentPointValue()
										  + ", " + this.currentlyActiveClassId + ", " + currentCategoryId + ");";
				statement.executeUpdate(addAssignmentQuery);
				log.info(Constants.assignmentAdded);
				return true;
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.addingAssignment);
				e.printStackTrace();
				return false;
			}
		}
		//student management
		else if(command.GetCommandType().equals(Constants.addStudent))
		{
			//if there is not active class this query is meaningless
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			//username, studentid, last, first			
			try
			{
				//check if the student already exists
				String studentExistsCheckQuery = "SELECT * FROM student WHERE user_name='" + command.GetStudentUserName() + "';";
				ResultSet studentExistsCheckResult = statement.executeQuery(studentExistsCheckQuery);
				
				if(studentExistsCheckResult.next())	//user already exists
				{
					System.out.println(Constants.studentAlreadyExists);
					
					int studentId = studentExistsCheckResult.getInt("student_id");
					if(command.GetStudentId() == null)	//add-student username
					{
						//check if the student is already enrolled
						//String studentId = String.valueOf(studentExistsCheckResult.getInt("student_id"));
						String checkEnrollmentStatusQuery = "SELECT * FROM enrolled_in WHERE course_id=" + this.currentlyActiveClassId
														  + " AND student_id=" + studentId + ";";
						
						ResultSet checkEnrollmentStatusResult = statement.executeQuery(checkEnrollmentStatusQuery);
						if(checkEnrollmentStatusResult.next())	//student is already enrolled
						{
							log.info(Constants.studentAlreadyEnrolled);
							return true;
						}
						else	//enrol the student
						{
							String enrollStudentQuery = "insert into enrolled_in (course_id, student_id) values (" + this.currentlyActiveClassId + ", " + studentId + ");";
							statement.executeUpdate(enrollStudentQuery);
							log.info("student: " + command.GetStudentUserName() + " enrolled in class: " + this.currentlyActiveClassCourseNumber + "!");
							return true;
						}
					}
					else	//add-student username studentid Last First
					{
						//check if their first and last name match
						if(studentExistsCheckResult.getString("student_name").equals(command.GetStudentFullName()))	//names match, so just enroll
						{
							//check if the student is already enrolled
							//String studentId = String.valueOf(studentExistsCheckResult.getInt("student_id"));
							String checkEnrollmentStatusQuery = "SELECT * FROM enrolled_in WHERE course_id=" + this.currentlyActiveClassId
															  + " AND student_id=" + studentId + ";";
							
							ResultSet checkEnrollmentStatusResult = statement.executeQuery(checkEnrollmentStatusQuery);
							if(checkEnrollmentStatusResult.next())	//student is already enrolled
							{
								log.info(Constants.studentAlreadyEnrolled);
								return true;
							}
							else	//otherwise enroll the student
							{
								String enrollStudentQuery = "insert into enrolled_in (course_id, student_id) values (" + this.currentlyActiveClassId + ", " + studentId + ");";
								statement.executeUpdate(enrollStudentQuery);
								log.info("student: " + command.GetStudentUserName() + " enrolled in class: " + this.currentlyActiveClassCourseNumber + "!");
								return true;
							}
						}
						else	//we need to update the info then do the rest
						{
							log.warning(Constants.nameDontMatch);
							log.info(Constants.updatingStudentInfo);
							//update the data first
							String updateQuery = "UPDATE student SET student_name='" + command.GetStudentFullName() + "' WHERE student_id=" + String.valueOf(studentExistsCheckResult.getInt("student_id")) + ";";
							statement.executeUpdate(updateQuery);
							
							// then do the rest as before
							//check if the student is already enrolled
							//String studentId = String.valueOf(studentExistsCheckResult.getInt("student_id"));
							String checkEnrollmentStatusQuery = "SELECT * FROM enrolled_in WHERE course_id=" + this.currentlyActiveClassId
															  + " AND student_id=" + studentId + ";";
							
							ResultSet checkEnrollmentStatusResult = statement.executeQuery(checkEnrollmentStatusQuery);
							if(checkEnrollmentStatusResult.next())	//student is already enrolled
							{
								log.info(Constants.studentAlreadyEnrolled);
								return true;
							}
							else	//enrol the student
							{
								String enrollStudentQuery = "insert into enrolled_in (course_id, student_id) values (" + this.currentlyActiveClassId + ", " + studentId + ");";
								statement.executeUpdate(enrollStudentQuery);
								log.info("student: " + command.GetStudentUserName() + " enrolled in class: " + this.currentlyActiveClassCourseNumber + "!");
								return true;
							}
						}
					}
					
				}
				else	//now if the user does not exist initially
				{
					if(command.GetStudentId() == null)	//add-student username
					{
						log.warning(Constants.studentDoesNotExist);
						return false;
					}
					else	//add-student username studentid Last First
					{
						//we need to check whether the student_id is valid or open for new student
						String idCheckQuery = "SELECT * FROM student WHERE student_id=" + command.GetStudentId() + ";";
						ResultSet idCheckResult = statement.executeQuery(idCheckQuery);
						int idCheckCounter = 0;
						while(idCheckResult.next())
						{
							idCheckCounter++;
						}
						if(idCheckCounter > 0)
						{
							log.warning(Constants.studentWithSameIdAlreadyExists);
							return false;
						}
						//student does not exist, so straight-forward, first add the student and then enroll
						String addStudentQuery = "insert into student (student_id, user_name, student_name) values (" + command.GetStudentId() + ", '" + command.GetStudentUserName() + "', '" + command.GetStudentFullName() + "');";
						statement.executeUpdate(addStudentQuery);
						
						//enroll the student to the class
						String enrollStudentQuery = "insert into enrolled_in (course_id, student_id) values (" + this.currentlyActiveClassId + ", " + command.GetStudentId() + ");";
						statement.executeUpdate(enrollStudentQuery);
						log.info("student: " + command.GetStudentUserName() + " enrolled in class: " + this.currentlyActiveClassCourseNumber + "!");
						return true;
					}
				}
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during adding student...");
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.showStudents))
		{
			//if we dont have a current class, the query is invalid
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			

			try
			{
				if(command.GetUsernameSubstring() == null)	//show-students
				{
					String showStudentsQuery = "SELECT st.student_id, st.user_name, st.student_name FROM enrolled_in en JOIN student st ON st.student_id=en.student_id "
											 + "WHERE en.course_id=" + this.currentlyActiveClassId + ";";
					ResultSet showStudentsResult = statement.executeQuery(showStudentsQuery);
					
					log.info(Constants.studentsInCurrentClass);
					int counter = 0;
					String[][] resultTable = null;
					while(showStudentsResult.next())
					{
						if(resultTable == null)
						{
							resultTable = new String[1][3];
							resultTable[counter] = new String[3];
						}
						else
						{
							resultTable = Arrays.copyOf(resultTable, counter+1);
							resultTable[counter] = new String[3];
						}
						
						resultTable[counter][0] = new String(String.valueOf(showStudentsResult.getInt("student_id")));
						resultTable[counter][1] = new String(showStudentsResult.getString("user_name"));
						resultTable[counter][2] = new String(showStudentsResult.getString("student_name"));
						
						counter++;
					}
					if(counter == 0)
					{
						log.warning(Constants.noStudentsFound);
						return false;
					}
					else
					{
						this.PrintData(Constants.showStudents, resultTable, null);
						return true;
					}
				}
				else	//show-students string
				{
					String showStudentsQuery = "SELECT DISTINCT st.student_id, st.user_name, st.student_name FROM enrolled_in en JOIN student st "
											 + "WHERE en.course_id=" + this.currentlyActiveClassId + " AND (st.user_name LIKE '%" + command.GetUsernameSubstring() + "%' OR st.student_name LIKE '%" + command.GetUsernameSubstring() + "%');";
					ResultSet showStudentsResult = statement.executeQuery(showStudentsQuery);
					
					int counter = 0;
					String[][] resultTable = null;
					while(showStudentsResult.next())
					{
						if(resultTable == null)
						{
							resultTable = new String[1][3];
							resultTable[counter] = new String[3];
						}
						else
						{
							resultTable = Arrays.copyOf(resultTable, counter+1);
							resultTable[counter] = new String[3];
						}
						
						resultTable[counter][0] = new String(String.valueOf(showStudentsResult.getInt("student_id")));
						resultTable[counter][1] = new String(showStudentsResult.getString("user_name"));
						resultTable[counter][2] = new String(showStudentsResult.getString("student_name"));

						counter++;
					}
					if(counter == 0)
					{
						log.warning(Constants.noStudentsFound);
						return false;
					}
					else
					{
						this.PrintData(Constants.showStudents, resultTable, null);
						return true;
					}
				}
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during showing students...");
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.grade))
		{
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			try
			{
				//check username validity
				String usernameCheckQuery = "SELECT * FROM student WHERE user_name='" + command.GetStudentUserName() + "';";
				ResultSet usernameCheckResult = statement.executeQuery(usernameCheckQuery);
				
				int usernameCheckCounter = 0;
				int studentId = -1;
				while(usernameCheckResult.next())
				{
					usernameCheckCounter++;
					if(usernameCheckCounter == 1)
					{
						studentId = usernameCheckResult.getInt("student_id");
					}
				}
				if(usernameCheckCounter == 0)
				{
					log.warning(Constants.usernameDoesNotExist);
					return false;
				}
				
				//check if assignmentname exists
				String assignmentnameCheckQuery = "SELECT * FROM assignment WHERE assignment_name='" + command.GetAssignmentName() + "' AND course_id=" + this.currentlyActiveClassId + ";";
				ResultSet assignmentnameCheckResult = statement.executeQuery(assignmentnameCheckQuery);
				
				int counter = 0;
				int pointValue = 0;
				int assignmentId = 0;
				while(assignmentnameCheckResult.next())
				{
					counter++;
					if(counter == 1)
					{
						pointValue = assignmentnameCheckResult.getInt("point_value");
						assignmentId = assignmentnameCheckResult.getInt("assignment_id");
					}
				}
				
				if(counter == 0)
				{
					log.warning(Constants.assignmentNameDoesNotExist);
					return false;
				}
				if(Integer.parseInt(command.GetStudentReceivedGradeForCourse()) > pointValue)
				{
					log.warning(Constants.pointExceedMaxPossiblePoint);
				}
				
				//checking if the student is enrolled to the current class
				String checkClassEnrollmentQuery = "SELECT * FROM enrolled_in WHERE course_id=" + this.currentlyActiveClassId + " AND student_id=" + studentId;
				ResultSet checkClassEnrollmentResult = statement.executeQuery(checkClassEnrollmentQuery);
				counter = 0;
				while(checkClassEnrollmentResult.next())
				{
					counter++;
				}
				if(counter == 0)
				{
					log.warning(Constants.studentNotEnrolledInCurrentClass);
					return false;
				}
				//everything is okay, now we check if the student already has a grade for this assignment
				String checkAssignmentAlreadyGradedQuery = "SELECT * FROM receives_grade_for WHERE assignment_id=" + assignmentId
														 + " AND student_id=" + studentId + ";";
				ResultSet checkAssignmentAlreadyGradedResult = statement.executeQuery(checkAssignmentAlreadyGradedQuery);
				if(checkAssignmentAlreadyGradedResult.next())	//this mean grade already exist, we need to update
				{
					log.warning(Constants.replacingPreviouslyReceivedGrade + Constants.from + String.valueOf(checkAssignmentAlreadyGradedResult.getInt("grade")) + Constants.to +  command.GetStudentReceivedGradeForCourse());
					String resultQuery = "UPDATE receives_grade_for SET grade=" + command.GetStudentReceivedGradeForCourse() + " WHERE student_id=" + studentId + " AND assignment_id=" + assignmentId + ";";
					statement.executeUpdate(resultQuery);
					log.info(Constants.studentGradeUpdated);
					return true;
				}
				else	//otherwise we just add the grade
				{
					String resultQuery = "insert into receives_grade_for (student_id, assignment_id, grade) values (" + studentId
									   + ", " + assignmentId + ", " + command.GetStudentReceivedGradeForCourse() + ");";
					statement.executeUpdate(resultQuery);
					log.info(Constants.studentGradeAdded);
					return true;
				}
				
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.addingStudentGrade);
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.studentGrades))
		{
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			try
			{
				//check username validity
				String usernameCheckQuery = "SELECT * FROM student WHERE user_name='" + command.GetStudentUserName() + "';";
				ResultSet usernameCheckResult = statement.executeQuery(usernameCheckQuery);
				
				int usernameCheckCounter = 0;
				int studentId = -1;
				while(usernameCheckResult.next())
				{
					usernameCheckCounter++;
					if(usernameCheckCounter == 1)
					{
						studentId = usernameCheckResult.getInt("student_id");
					}
				}
				if(usernameCheckCounter == 0)
				{
					log.warning(Constants.usernameDoesNotExist);
					return false;
				}

				//checking if the student is enrolled to the current class
				String checkClassEnrollmentQuery = "SELECT * FROM enrolled_in WHERE course_id=" + this.currentlyActiveClassId + " AND student_id=" + studentId;
				ResultSet checkClassEnrollmentResult = statement.executeQuery(checkClassEnrollmentQuery);
				int counter = 0;
				while(checkClassEnrollmentResult.next())
				{
					counter++;
				}
				if(counter == 0)
				{
					log.warning(Constants.studentNotEnrolledInCurrentClass);
					return false;
				}
				//now first report grade for each assignment
				String eachAssignmentQuery = "SELECT s.student_id, s.student_name, cat.category_name, a.assignment_name, a.point_value, rgf.grade FROM student s JOIN receives_grade_for rgf ON s.student_id=rgf.student_id "
										   + "JOIN assignment a ON rgf.assignment_id=a.assignment_id JOIN category cat ON a.category_id=cat.category_id WHERE a.course_id=" + this.currentlyActiveClassId
										   + " AND s.student_id=" + studentId + ";";
				ResultSet eachAssignmentResult = statement.executeQuery(eachAssignmentQuery);
				
				counter = 0;
				String[][] resultTable = null;
				while(eachAssignmentResult.next())
				{
					if(resultTable == null)
					{
						resultTable = new String[1][5];
						resultTable[counter] = new String[5];
					}
					else
					{
						resultTable = Arrays.copyOf(resultTable, counter+1);
						resultTable[counter] = new String[5];
					}
					
					resultTable[counter][0] = new String(eachAssignmentResult.getString("student_name"));
					resultTable[counter][1] = new String(eachAssignmentResult.getString("category_name"));
					resultTable[counter][2] = new String(eachAssignmentResult.getString("assignment_name"));
					resultTable[counter][3] = new String(String.valueOf(eachAssignmentResult.getInt("point_value")));
					resultTable[counter][4] = new String(String.valueOf(eachAssignmentResult.getInt("grade")));
					
					counter++;
				}
				
				this.PrintData(Constants.studentGrades, resultTable, Constants.assignmentList);
				//subtotals by category
				String categoryWiseTotalQuery = "SELECT cat.category_name, con.weight, SUM(rgf.grade) AS totalGradeReceived, SUM(a.point_value) AS totalGradePossible FROM assignment a JOIN receives_grade_for rgf ON a.assignment_id=rgf.assignment_id JOIN category cat on a.category_id=cat.category_id "
											  + "JOIN contains con ON cat.category_id=con.category_id WHERE a.course_id=" + this.currentlyActiveClassId + " AND rgf.student_id=" + studentId + " GROUP BY cat.category_name, con.weight;";
				
				ResultSet categoryWiseTotalResult = statement.executeQuery(categoryWiseTotalQuery);
				
				counter = 0;
				resultTable = null;
				double totalGradeReceivedInClass = 0;
				double totalGradePossible = 0;
				while(categoryWiseTotalResult.next())
				{
					if(resultTable == null)
					{
						resultTable = new String[1][4];
						resultTable[counter] = new String[4];
					}
					else
					{
						resultTable = Arrays.copyOf(resultTable, counter+1);
						resultTable[counter] = new String[4];
					}
					
					resultTable[counter][0] = new String(categoryWiseTotalResult.getString("category_name"));
					resultTable[counter][1] = new String(String.valueOf(categoryWiseTotalResult.getInt("weight")));
					resultTable[counter][2] = new String(String.valueOf(categoryWiseTotalResult.getInt("totalGradeReceived")));
					resultTable[counter][3] = new String(String.valueOf(categoryWiseTotalResult.getInt("totalGradePossible")));
					
					totalGradeReceivedInClass = totalGradeReceivedInClass + (((double)categoryWiseTotalResult.getInt("weight")) * (((double)Integer.parseInt(resultTable[counter][2])) / ((double)Integer.parseInt(resultTable[counter][3]))));
					totalGradePossible = totalGradePossible + ((double)categoryWiseTotalResult.getInt("weight"));
					counter++;
				}
				this.PrintData(Constants.studentGrades, resultTable, Constants.byCategory);
				
				System.out.printf("Overall grade in class: %.2f/%f\n",totalGradeReceivedInClass, totalGradePossible);
				return true;
			}
			catch(SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.showingStudentGrade);
				e.printStackTrace();
				return false;
			}
		}
		else if(command.GetCommandType().equals(Constants.gradebook))
		{
			if(this.currentlyActiveClassId == null)
			{
				log.warning("No Active class...");
				return false;
			}
			
			try
			{
				String resultQuery = "SELECT student_id, student_name, SUM(total_grade_received/total_grade_possible*weight) AS total FROM (SELECT s.student_id, s.user_name, s.student_name, a.category_id, con.weight, SUM(rgf.grade) AS total_grade_received, SUM(a.point_value) AS total_grade_possible FROM student s JOIN receives_grade_for rgf ON s.student_id=rgf.student_id JOIN assignment a ON rgf.assignment_id=a.assignment_id JOIN contains con ON con.category_id=a.category_id "
						  		   + " WHERE a.course_id=" + this.currentlyActiveClassId + " GROUP BY s.student_id, s.user_name, s.student_name, a.category_id, con.weight) AS inner_table GROUP BY student_id, student_name;";
				ResultSet gradeResult = statement.executeQuery(resultQuery);
				
				int counter = 0;
				String[][] resultTable = null;
				while(gradeResult.next())
				{
					if(resultTable == null)
					{
						resultTable = new String[1][3];
						resultTable[counter] = new String[3];
					}
					else
					{
						resultTable = Arrays.copyOf(resultTable, counter+1);
						resultTable[counter] = new String[3];
					}
					
					resultTable[counter][0] = new String(String.valueOf(gradeResult.getInt("student_id")));
					resultTable[counter][1] = new String(gradeResult.getString("student_name"));
					resultTable[counter][2] = new String(String.valueOf(gradeResult.getDouble("total")));
					
					counter++;
				}
				
				if(counter == 0)
				{
					log.warning(Constants.noDataFound);
					return false;
				}
				
				this.PrintData(Constants.gradebook, resultTable, null);
				return true;
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.showingGradebook);
				e.printStackTrace();
				return false;
			} 			
		}
		//extra credit
		else if(command.GetCommandType().equals(Constants.importGrades))
		{
			//check activated class
			if(this.currentlyActiveClassId == null)
			{
				log.warning(Constants.noActiveClass);
				return false;
			}
			try
			{
				//chack if assignment exists
				String assignmentnameCheckQuery = "SELECT * FROM assignment WHERE assignment_name='" + command.GetAssignmentName() + "' AND course_id=" + this.currentlyActiveClassId + ";";
				ResultSet assignmentnameCheckResult = statement.executeQuery(assignmentnameCheckQuery);
				
				int assignmentCheckCounter = 0;
				int pointValue = 0;
				int assignmentId = 0;
				while(assignmentnameCheckResult.next())
				{
					assignmentCheckCounter++;
					if(assignmentCheckCounter == 1)
					{
						pointValue = assignmentnameCheckResult.getInt("point_value");
						assignmentId = assignmentnameCheckResult.getInt("assignment_id");
					}
				}
				
				if(assignmentCheckCounter == 0)
				{
					log.warning("assignment " +  command.GetAssignmentName() + "does not exist..");
					return false;
				}
				//everything is okay so now insert the file rows
				File file = new File(command.GetFileName());
		        FileReader fr = new FileReader(file);
		        BufferedReader br = new BufferedReader(fr);
		        
		        String delimiter = ",";
		        String line = "";
		        String[] tempArr;
		        String userName = null;
		        String grade = null;
		        while((line = br.readLine()) != null)
		        {
		           tempArr = line.split(delimiter);
		           if(tempArr.length != 2)
		           {
		        	   log.warning("Invalid dimention of file!");
		        	   return false;
		           }
		           int counter = 0;
		           for(String tempStr : tempArr)
		           {
		        	   if(counter == 0)
		        	   {
		        		   userName = tempStr;
		        	   }
		        	   else if(counter == 1)
		        	   {
		        		   grade = tempStr;
		        	   }
		        	   counter++;
		            }
		           	//check username validity
		           	String usernameCheckQuery = "SELECT * FROM student WHERE user_name='" + userName + "';";
		           	ResultSet usernameCheckResult = statement.executeQuery(usernameCheckQuery);
					
					int usernameCheckCounter = 0;
					int studentId = -1;
					while(usernameCheckResult.next())
					{
						usernameCheckCounter++;
						if(usernameCheckCounter == 1)
						{
							studentId = usernameCheckResult.getInt("student_id");
						}
					}
					if(usernameCheckCounter == 0)
					{
						log.warning("username " + userName + "does not exist..");
						continue;
					}
					//check whether grade is valid
					try
					{
						int gradeInt = Integer.parseInt(grade);
					}
					catch(Exception e)
					{
						log.warning(Constants.gradeMustBeInteger);
						continue;
					}
					//check grade limit
					if(Integer.parseInt(grade) > pointValue)
					{
						log.warning(Constants.pointExceedMaxPossiblePoint);
						//continue;
					}
					//check whether the student is really enrolled in the current class
					String checkClassEnrollmentQuery = "SELECT * FROM enrolled_in WHERE course_id=" + this.currentlyActiveClassId + " AND student_id=" + studentId;
					ResultSet checkClassEnrollmentResult = statement.executeQuery(checkClassEnrollmentQuery);
					int checkCounter = 0;
					while(checkClassEnrollmentResult.next())
					{
						checkCounter++;
					}
					if(checkCounter == 0)
					{
						log.warning(Constants.studentNotEnrolledInCurrentClass);
						continue;
					}
					//everything is okay, now we check if the student already has a grade for this assignment
					String checkAssignmentAlreadyGradedQuery = "SELECT * FROM receives_grade_for WHERE assignment_id=" + assignmentId
															 + " AND student_id=" + studentId + ";";
					ResultSet checkAssignmentAlreadyGradedResult = statement.executeQuery(checkAssignmentAlreadyGradedQuery);
					if(checkAssignmentAlreadyGradedResult.next())	//this means grade already exists, we need to update
					{
						String resultQuery = "UPDATE receives_grade_for SET grade=" + grade + " WHERE student_id=" + studentId + " AND assignment_id=" + assignmentId + ";";
						statement.executeUpdate(resultQuery);
						log.info(Constants.studentGradeUpdated);
					}
					else	//otherwise we just add the grade
					{
						String resultQuery = "insert into receives_grade_for (student_id, assignment_id, grade) values (" + studentId
										   + ", " + assignmentId + ", " + grade + ");";
						statement.executeUpdate(resultQuery);
						log.info(Constants.studentGradeAdded);
					}
		            TimeUnit.MILLISECONDS.sleep(350);
		         }
		         br.close();
			}
			catch (FileNotFoundException e)
			{
				log.warning(Constants.fileNotFoundExceptionOccured);
				e.printStackTrace();
				return false;
			}
			catch(IOException e)
			{
				log.warning(Constants.ioExceptionOccured);
				e.printStackTrace();
				return false;
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlExceptionOccured + Constants.readingCsvFile);
				e.printStackTrace();
				return false;
			}
			catch (InterruptedException e)
			{
				log.warning(Constants.interruptedExceptionOccured + Constants.readingCsvFile);
				e.printStackTrace();
				return false;
			}
		}
		return true;
			
	}
	
	/**
	 * prints out the retrieved data in a structured way
	 * @param commandType - what command type we are dealing with
	 * @param resultTable - holding the retrieved data
	 * @param subCommand - secondary command type for print clarification
	 */
	private void PrintData(String commandType, String[][] resultTable, String subCommand)
	{
		if(resultTable == null)
		{
			return;
		}
		if(commandType.equals(Constants.listClasses))
		{
			/*course_number, term, section_no, student_count*/
			final Object[] header = new String[] {"course_number", "term", "section_no", "student_count"};
			System.out.format("%15s%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s%15s\n", row);
			}
		}
		else if(commandType.equals(Constants.selectClass))
		{
			/*course_id, course_number, term, section_no*/
			final Object[] header = new String[] {"course_id", "course_number", "term", "section_no"};
			System.out.format("%15s%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s%15s\n", row);
			}
		}
		else if(commandType.equals(Constants.showClass))
		{
			/*course_id, course_number, term, section_no*/
			final Object[] header = new String[] {"course_id", "course_number", "term", "section_no"};
			System.out.format("%15s%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s%15s\n", row);
			}
		}
		else if(commandType.equals(Constants.showCategories))
		{
			/*category_id, category_name, weight*/
			final Object[] header = new String[] {"category_id", "category_name", "weight"};
			System.out.format("%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s\n", row);
			}
		}
		else if(commandType.equals(Constants.showAssignment))
		{
			/*category_id, category_name, assignment_id, assignment_name, point_value*/
			final Object[] header = new String[] {"category_id", "category_name", "assignment_id", "assignment_name", "point_value"};
			System.out.format("%15s%15s%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s%15s%15s\n", row);
			}
		}
		else if(commandType.equals(Constants.showStudents))
		{
			/*student_id, user_name, student_name*/
			final Object[] header = new String[] {"student_id", "user_name", "student_name"};
			System.out.format("%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s\n", row);
			}
		}
		else if(commandType.equals(Constants.studentGrades))
		{
			if(subCommand.equals(Constants.assignmentList))
			{
				/*"student_id, student_name, category_name, assignment_name, point_value, grade*/
				final Object[] header = new String[] {"name", "category", "assignment_name", "point_value", "grade"};
				System.out.format("%15s%15s%15s%18s%15s\n", header);
				for(Object[] row : resultTable)
				{
					System.out.format("%15s%15s%15s%18s%15s\n", row);
				}
			}
			else if(subCommand.equals(Constants.byCategory))
			{
				/*category_name, weight, totalGradeReceived, totalGradePossible*/
				final Object[] header = new String[] {"category_name", "weight", "totalGradeReceived", "totalGradePossible"};
				System.out.format("%13s%8s%25s%25s\n", header);
				for(Object[] row : resultTable)
				{
					System.out.format("%13s%8s%25s%25s\n", row);
				}
			}
		}
		else if(commandType.equals(Constants.gradebook))
		{
			/*student_id, student_name, total*/
			final Object[] header = new String[] {"student_id", "student_name", "total(%)"};
			System.out.format("%15s%15s%15s\n", header);
			for(Object[] row : resultTable)
			{
				System.out.format("%15s%15s%15s\n", row);
			}
		}
	}
	
	/**
	 * retrieves the most recent "term"
	 * @return the most recent "term" if found, otherwise null
	 */
	private String RetrieveMostRecentTerm()
	{
		try
		{
			String query = "SELECT DISTINCT term from class";
			
			ResultSet result = statement.executeQuery(query);
			
			String returnResult = null;
			int counter = 0;
			
			while(result.next())
			{
				if(counter == 0)
				{
					returnResult = new String(result.getString("term"));
				}
				else
				{
					String tempTerm = result.getString("term");
					
					String tempYear = tempTerm.substring(tempTerm.length()-2);
					int tempYearInt = Integer.parseInt(tempYear);
					String tempSemester = tempTerm.substring(0, tempTerm.length()-2);
					
					String currentYear = returnResult.substring(returnResult.length()-2);
					int currentYearInt = Integer.parseInt(currentYear);
					String currentSemester = returnResult.substring(0, returnResult.length()-2);
					
					
					if(tempYearInt > currentYearInt)
					{
						returnResult = new String(tempTerm);
					}
					else if(tempYearInt == currentYearInt)
					{
						if((tempSemester.equals(Constants.Spring) && currentSemester.equals(Constants.Summer))
						  || (tempSemester.equals(Constants.Spring) && currentSemester.equals(Constants.Fall))
						  || (tempSemester.equals(Constants.Summer) && currentSemester.equals(Constants.Fall)))
						{
							returnResult = new String(tempTerm);
						}
					}
				}
				counter++;
			}
			return returnResult;
		}
		catch(SQLException e)
		{
			log.warning(Constants.sqlExceptionOccured + Constants.getLatestTerm);
			return null;
		}
	}

}
