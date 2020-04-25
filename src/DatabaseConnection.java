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
import java.util.concurrent.TimeUnit;
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
				log.info("New class info inserted!");
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
				while(result.next())
				{
					isResultFound = true;
					
					System.out.println(result.getString("course_number") + ", " + result.getString("term")
									   + ", " + result.getInt("section_no") + ", " + result.getInt("student_count"));
				}
				
				if(!isResultFound)
				{
					log.warning(Constants.noDataFound);
					return false;
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
				// TODO:: how do we get the recent term
				log.warning("Not yet implemented!");
				return false;
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
					
					log.info(Constants.availableClasses);
					int counter = 0;
					while(result.next())
					{
						counter++;
						System.out.println(result.getInt("course_id") + ", " + result.getString("course_number")
										 + ", " + result.getString("term") + ", " + result.getString("section_no"));
						
						if(counter == 1)
						{
							tempCurrentlyActiveClassId = String.valueOf(result.getInt("course_id"));
							tempCurrentlyActiveClassCourseNumber = result.getString("course_number");
							tempCurrentlyActiveClassTerm = result.getString("term");
							tempCurrentlyActiveClassSectionNo = String.valueOf(result.getInt("section_no"));
							tempCurrentlyActiveClassDescription = result.getString("class_description");
						}
					}
					
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
					
					log.info("activating class: " + tempCurrentlyActiveClassCourseNumber + ", " + tempCurrentlyActiveClassTerm + ", " + tempCurrentlyActiveClassSectionNo);
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
						
						log.info("activating class: " + this.currentlyActiveClassCourseNumber + ", " + this.currentlyActiveClassTerm + ", " + this.currentlyActiveClassSectionNo);
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
				System.out.print("Id: " + this.currentlyActiveClassId + ", course_number: " + this.currentlyActiveClassCourseNumber + ", term: " + this.currentlyActiveClassTerm
								+ ",\n" + "section_no: " + this.currentlyActiveClassSectionNo + ", description: " + this.currentlyActiveClassDescription);
				
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
			
			//select con.category_id, cat.category_name, con.weight
			//from class cl join contains con on cl.course_id = con.course_id
			//join category cat on con.category_id = cat.category_id
			//where cl.course_id = this.currentlyActiveClassId			
			try
			{
				query = "SELECT con.category_id, cat.category_name, con.weight FROM class cl JOIN contains con ON cl.course_id = con.course_id "
					  + "JOIN category cat ON con.category_id = cat.category_id WHERE cl.course_id = " + this.currentlyActiveClassId;
					
				System.out.println("Query string: " + query);
				
				ResultSet result = statement.executeQuery(query);
				
				log.info("Showing categories for the class: " + this.currentlyActiveClassCourseNumber);
				int counter = 0;
				while(result.next())
				{
					counter++;
					System.out.println(result.getInt("category_id") + ", " + result.getString("category_name") + ", " + result.getInt("weight"));
				}
				
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
				System.out.println("first checking if the category exists...");
				String categoryCheckQuery = "SELECT * FROM category WHERE category_name='" + command.GetCategoryName() + "';";
				ResultSet categoryCheckResult = statement.executeQuery(categoryCheckQuery);
				
				String currentCategoryId = null;
				if(categoryCheckResult.next())	//category exists in category table, get the id
				{
					System.out.println("category exist!");
					currentCategoryId = String.valueOf(categoryCheckResult.getInt("category_id"));
				}
				else	//category doesn't exist in the category table, so add there first
				{
					System.out.println("category does not exist!");
					System.out.println("adding the category first");
					String addCategoryQuery = "insert into category (category_name) values ('" + command.GetCategoryName() + "');";
					System.out.println("query = " + addCategoryQuery);
					statement.executeUpdate(addCategoryQuery);
					
					categoryCheckResult = statement.executeQuery(categoryCheckQuery);
					boolean categoryCheckBoolResult = categoryCheckResult.next();
					currentCategoryId = String.valueOf(categoryCheckResult.getInt("category_id"));
					System.out.println("id: " + currentCategoryId);
				}
				
				//now the category exists no matter what, we can just add the weight in contains
				//TODO: do we update or not?
				//now we have to check whether this category is already exists in contains for this class
				String categoryClassContainCheckQuery = "SELECT * FROM contains WHERE course_id=" + this.currentlyActiveClassId
												      + " AND category_id=" + currentCategoryId + ";";
				ResultSet categoryClassContainCheckResult = statement.executeQuery(categoryClassContainCheckQuery);
				
				if(categoryClassContainCheckResult.next())	//this category is already added for this class
				{
					//TODO:: should we replace?
					log.warning("This catehory already exists for this class");
					return false;
				}
				
				System.out.print("inserting into contains");
				
				//otherwise now we can add this category to contains
				String insertCategoryWeightQuery = "insert into contains (course_id, category_id, weight) values (" + this.currentlyActiveClassId
											+ ", " + currentCategoryId + ", " + command.GetCategoryWeightForCourse() + ");";
				
				statement.executeUpdate(insertCategoryWeightQuery);
				log.info("Given category added for given class");
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
				
				log.info("Showing assignments for the current class...");
				
				int counter = 0;
				while(result.next())
				{
					counter++;
					System.out.println(result.getInt("category_id") + ", " + result.getString("category_name") + ", "
									  + result.getInt("assignment_id") + ", " + result.getString("assignment_name") + ", "
									  + result.getInt("point_value"));
				}
				
				if(counter == 0)
				{
					log.warning("No data found..");
					return false;
				}
				else
				{
					return true;
				}
			} catch (SQLException e) {
				log.warning("SQLException occured during showing category...");
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
				//TODO:: should we check for already existance? now we are doing that
				String assignmentExistanceCheckQuery = "SELECT * FROM assignment WHERE course_id=" + this.currentlyActiveClassId
													 + " AND assignment_name='" + command.GetAssignmentName() + "';";
				
				System.out.println("assignmentExistanceCheckQuery = " + assignmentExistanceCheckQuery);
				ResultSet assignmentExistanceCheckResult = statement.executeQuery(assignmentExistanceCheckQuery);
				
				if(assignmentExistanceCheckResult.next())
				{
					int assignmentId = assignmentExistanceCheckResult.getInt("assignment_id");
					int categoryId = assignmentExistanceCheckResult.getInt("category_id");
					log.warning("Assignment with same name already exists in this course! replacing it");
					String updateAssignmentQuery = "UPDATE assignment SET point_value=" + command.GetAssignmentPointValue() + ", assignment_description='" + command.GetAssignmentDescription() + "' WHERE assignment_id=" + assignmentId + " AND course_id=" + this.currentlyActiveClassId + " AND category_id=" + categoryId + ";";
					System.out.println("updateAssignmentQuery: " + updateAssignmentQuery);
					statement.executeUpdate(updateAssignmentQuery);
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
				System.out.println("addAssignmentQuery: " + addAssignmentQuery);
				statement.executeUpdate(addAssignmentQuery);
				return true;
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during adding assignment...");
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
				System.out.println("studentExistsCheckQuery: " + studentExistsCheckQuery);
				ResultSet studentExistsCheckResult = statement.executeQuery(studentExistsCheckQuery);
				
				if(studentExistsCheckResult.next())	//user already exists
				{
					System.out.println("student already exists");
					System.out.println("just enrolling requuired");
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
							log.info("Student already enrolled in class..");
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
								log.info("Student already enrolled in class..");
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
						else	//we need to update the info then do the rest
						{
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
								log.info("Student already enrolled in class..");
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
						log.warning("Student does not exist...");
						return false;
					}
					else	//add-student username studentid Last First
					{
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
			//TODO:: show-students string
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
					String showStudentsQuery = "SELECT st.student_id, st.user_name, st.student_name FROM enrolled_in en JOIN student st "
											 + "WHERE en.course_id=" + this.currentlyActiveClassId + ";";
					ResultSet showStudentsResult = statement.executeQuery(showStudentsQuery);
					
					log.info("Students in current class:");
					int counter = 0;
					
					while(showStudentsResult.next())
					{
						counter++;
						
						System.out.println(showStudentsResult.getInt("student_id") + ", " + showStudentsResult.getString("user_name") + ", " + showStudentsResult.getString("student_name"));
					}
					if(counter == 0)
					{
						log.warning("No students found..");
						return false;
					}
					else
					{
						return true;
					}
				}
				else	//show-students string
				{
					//TODO:: this is not complete
					String showStudentsQuery = "SELECT st.student_id, st.user_name, st.student_name FROM enrolled_in en JOIN student st "
											 + "WHERE en.course_id=" + this.currentlyActiveClassId + " AND (st.user_name LIKE '%" + command.GetUsernameSubstring() + "%' OR st.student_name LIKE '%" + command.GetUsernameSubstring() + "%');";
					ResultSet showStudentsResult = statement.executeQuery(showStudentsQuery);
					
					int counter = 0;
					
					while(showStudentsResult.next())
					{
						counter++;
						
						System.out.println(showStudentsResult.getInt("student_id") + ", " + showStudentsResult.getString("user_name") + ", " + showStudentsResult.getShort("student_name"));
					}
					if(counter == 0)
					{
						log.warning("No students found..");
						return false;
					}
					else
					{
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
					log.warning("username does not exist..");
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
					log.warning("assignmentname does not exist..");
					return false;
				}
				if(Integer.parseInt(command.GetStudentReceivedGradeForCourse()) > pointValue)
				{
					log.warning("Point exceeds the maximum possible point!!");
					return false;
				}
				
				//everything is okay, now we check if the student already has a grade for this assignment
				String checkAssignmentAlreadyGradedQuery = "SELECT * FROM receives_grade_for WHERE assignment_id=" + assignmentId
														 + " AND student_id=" + studentId + ";";
				ResultSet checkAssignmentAlreadyGradedResult = statement.executeQuery(checkAssignmentAlreadyGradedQuery);
				if(checkAssignmentAlreadyGradedResult.next())	//this mean grade already exist, we need to update
				{
					String resultQuery = "UPDATE receives_grade_for SET grade=" + command.GetStudentReceivedGradeForCourse() + "' WHERE student_id=" + studentId + " AND assignment_id=" + assignmentId + ";";
					statement.executeUpdate(resultQuery);
					log.info("student grade updated!!");
					return true;
				}
				else	//otherwise we just add the grade
				{
					String resultQuery = "insert into receives_grade_for (student_id, assignment_id, grade) values (" + studentId
									   + ", " + assignmentId + ", " + command.GetStudentReceivedGradeForCourse() + ");";
					statement.executeUpdate(resultQuery);
					log.info("student grade added!!");
					return true;
				}
				
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during adding student grade...");
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
					log.warning("username does not exist..");
					return false;
				}
				
				//now first report grade for each assignment
				String eachAssignmentQuery = "SELECT s.student_id, s.student_name, cat.category_name, a.assignment_name, a.point_value, rgf.grade FROM student s JOIN receives_grade_for rgf ON s.student_id=rgf.student_id "
										   + "JOIN assignment a ON rgf.assignment_id=a.assignment_id JOIN category cat ON a.category_id=cat.category_id WHERE a.course_id=" + this.currentlyActiveClassId
										   + " AND s.student_id=" + studentId + ";";
				ResultSet eachAssignmentResult = statement.executeQuery(eachAssignmentQuery);
				
				while(eachAssignmentResult.next())
				{
					System.out.println(eachAssignmentResult.getInt("student_id") + ", " + eachAssignmentResult.getString("student_name") + ", " + eachAssignmentResult.getString("assignment_name") + ", " + eachAssignmentResult.getInt("point_value") + ", " + eachAssignmentResult.getInt("grade"));
				}
				
				
				String categoryWiseTotalQuery = "SELECT cat.category_name, SUM(rgf.grade) as total FROM assignment a JOIN receives_grade_for rgf ON a.assignment_id=rgf.assignment_id JOIN category cat on a.category_id=cat.category_id "
											  + "WHERE a.course_id=" + this.currentlyActiveClassId + " AND rgf.student_id=" + studentId + " GROUP BY cat.category_name;";
				
				ResultSet categoryWiseTotalResult = statement.executeQuery(categoryWiseTotalQuery);
				while(categoryWiseTotalResult.next())
				{
					System.out.println(categoryWiseTotalResult.getString("category_name") + ", " + categoryWiseTotalResult.getInt("total"));
				}
				
				return true;
			}
			catch(SQLException e)
			{
				log.warning("SQLException occured during showing student grades...");
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
				String resultQuery = "SELECT s.student_id, SUM(rgf.grade) as total FROM student s JOIN receives_grade_for rgf ON s.student_id=rgf.student_id JOIN assignment a ON rgf.assignment_id=a.assignment_id "
						  		   + " WHERE a.course_id=" + this.currentlyActiveClassId + " GROUP BY s.student_id;";
				ResultSet gradeResult = statement.executeQuery(resultQuery);
				
				while(gradeResult.next())
				{
					System.out.println(gradeResult.getInt("student_id") + ", " + gradeResult.getInt("total"));
				}
				return true;
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during showing gradebook...");
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
				log.warning("No Active class...");
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
					log.warning("assignmentname does not exist..");
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
		           System.out.println("total line: " + line);
		           int counter = 0;
		           for(String tempStr : tempArr)
		           {
		        	   System.out.println("counter: " + counter);
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
		           	System.out.println("username: " + userName + ", grade:" + grade);
		           	//check username validity
		           	String usernameCheckQuery = "SELECT * FROM student WHERE user_name='" + userName + "';";
		           	System.out.println("usernameCheckQuery: " + usernameCheckQuery);
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
						log.warning("username does not exist..");
						continue;
					}
					//check grade limit
					if(Integer.parseInt(grade) > pointValue)
					{
						log.warning("Point exceeds the maximum possible point!!");
						continue;
					}
					
					System.out.println("username: " + userName + ", and grade: " + grade);
					//everything is okay, now we check if the student already has a grade for this assignment
					String checkAssignmentAlreadyGradedQuery = "SELECT * FROM receives_grade_for WHERE assignment_id=" + assignmentId
															 + " AND student_id=" + studentId + ";";
					ResultSet checkAssignmentAlreadyGradedResult = statement.executeQuery(checkAssignmentAlreadyGradedQuery);
					if(checkAssignmentAlreadyGradedResult.next())	//this means grade already exists, we need to update
					{
						String resultQuery = "UPDATE receives_grade_for SET grade=" + grade + " WHERE student_id=" + studentId + " AND assignment_id=" + assignmentId + ";";
						statement.executeUpdate(resultQuery);
						log.info("student grade updated!!");
					}
					else	//otherwise we just add the grade
					{
						String resultQuery = "insert into receives_grade_for (student_id, assignment_id, grade) values (" + studentId
										   + ", " + assignmentId + ", " + grade + ");";
						statement.executeUpdate(resultQuery);
						log.info("student grade added!!");
					}
		            TimeUnit.MILLISECONDS.sleep(200);
		            System.out.println();
		         }
		         br.close();
			}
			catch (FileNotFoundException e)
			{
				log.warning("FileNotFoundException occured during reading CSV file...");
				e.printStackTrace();
				return false;
			}
			catch(IOException e)
			{
				log.warning("IOException occured during reading CSV file...");
				e.printStackTrace();
				return false;
			}
			catch (SQLException e)
			{
				log.warning("SQLException occured during reading CSV file...");
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				log.warning("InterruptedException occured during reading CSV file...");
				e.printStackTrace();
			}
		}
		return true;
			
	}
	
	public void SetRecentTerm()
	{
		//select distinct term from class
		String query = Constants.select + Constants.distinct + Constants.term + Constants.space + Constants.from + Constants.classString + Constants.semiColon;
		
		
	}

}
