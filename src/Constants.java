
public class Constants
{
	//all attributes
	public static String courseId = "course_id";
	public static String courseNumber = "course_number";
	public static String term = "term";
	public static String sectionNo = "section_no";
	public static String description = "description";
	public static String classDescription = "class_description";
	public static String name = "name";
	public static String pointValue = "point_value";
	public static String userName = "user_name";
	public static String weight = "weight";
	public static String category = "category";
	public static String categoryId = "category_id";
	public static String categoryName = "category_name";
	public static String studentId = "student_id";
	public static String first = "First";
	public static String last = "Last";
	public static String usernamesubstring = "string";
	public static String assignmentName = "assignmentname";
	public static String fileName = "file-name";
	
	//runtime attributes
	public static String studentCount = "student_count";
	
	//class management
	public static String newClass = "new-class";
	public static String listClasses = "list-classes";
	public static String selectClass = "select-class";
	public static String showClass = "show-class";
	
	//Category and assignment management
	public static String showCategories = "show-categories";
	public static String addCategory = "add-category";
	public static String showAssignment = "show-assignment";
	public static String addAssignment = "add-assignment";
	
	//student management
	public static String addStudent = "add-student";
	public static String showStudents = "show-students";
	public static String grade = "grade";
	
	//grade reporting
	public static String studentGrades = "student-grades";
	public static String gradebook = "gradebook";
	
	//extra credit
	public static String importGrades = "import-grades";
	
	public static String nullStr = "NULL";
	public static String invalidUsage = "Invalid usage of the command: ";
	public static String invalidCommand = "Invalid command";
	public static String usageOfCommand = "Usage of command ";
	public static String space = " ";
	public static String dot = ".";
	public static String colon = ": ";
	public static String semiColon = ";";
	public static String leftSquareBrace = "[";
	public static String rightSquareBrace = "]";
	public static char quoteMark = '\"';
	public static String singleQuote = "'";
	public static String emptyString = "";
	public static String leftBrace = "(";
	public static String rightBrace = ")";
	public static String commaSpace = ", ";
	public static String equals = "= ";
	
	//sql queries
	public static String createDb = "CREATE DATABASE IF NOT EXISTS ";
	public static String use = "USE";
	public static String schoolDb = "SchoolDb";
	public static String createTable = "CREATE TABLE ";
	public static String classString = "class";
	public static String studentString = "student";
	public static String categoryString = "category";
	public static String assignmentString = "assignment";
	public static String enrolledIn = "enrolled_in";
	public static String containsString = "contains";
	public static String receivesGradeForString = "receives_grade_for";
	public static String integer = "INTEGER ";
	public static String primaryKey = "PRIMARY KEY ";
	public static String autoIncrement = "AUTO_INCREMENT";
	public static String varchar = "VARCHAR";
	public static String notNull = "NOT NULL";
	public static String uniqueKey = "UNIQUE KEY ";
	public static String foreignKey = "FOREIGN KEY ";
	public static String references = "REFERENCES ";
	public static String index = "INDEX ";
	public static String fifty = "50";
	public static String twoHundred = "200";
	public static String oneHundred = "100";
	public static String fiveHundred = "500";
	public static String insertInto = "insert into ";
	public static String values = "values ";
	public static String select = "SELECT ";
	public static String distinct = "DISTINCT ";
	public static String count = "COUNT";
	public static String as = "AS ";
	public static String join = "JOIN ";
	public static String left = "LEFT ";
	public static String on = "ON ";
	public static String groupBy = "GROUP BY ";
	//shorts forms
	public static String classCL = "cl";
	public static String enrolledInEN = "en";
	public static String containsCON = "con";
	public static String categoryCAT = "cat";
	
	//semesters
	public static String fall = "fall";
	public static String Fall = "Fall";
	public static String spring = "spring";
	public static String Spring = "Spring";
	public static String springShort = "sp";
	public static String SpringShort = "Sp";
	public static String summer = "summer";
	public static String Summer = "Summer";
	public static String summerShort = "sum";
	public static String SummerShort = "Sum";
	
	//log.info
	public static String availableClasses = "Available classes with given information:";
	public static String newClassInserted = "New class info inserted!";
	public static String activatingClass = "activating class: ";
	public static String activatedClassDescription = "Activated class description: ";
	public static String showingCategoryForClass = "Showing categories for the class: ";
	public static String categoryExists = "category exists!";
	public static String categoryDoesNotExist = "category does not exist!";
	public static String addingTheCategoryFirst = "adding the category first";
	public static String givenCategoryAdded = "Given category and weight added for given class";
	public static String givenCategoryWeightUpdated = "Weight updated for given category for current active class";
	public static String showingAssignmentForCurrentClass = "Showing assignments for the current class...";
	public static String assignmentAdded = "Assignment added";
	public static String studentAlreadyEnrolled = "Student already enrolled in class..";
	public static String updatingStudentInfo = "Updating student info";
	public static String studentsInCurrentClass = "Students in current class:";
	public static String studentGradeUpdated = "Student grade updated!";
	public static String studentGradeAdded = "student grade added!";
	
	//log.warning
	public static String classAlreadyExists = "Information about same class already exist!";
	public static String noDataFound = "No data found!";
	public static String noClassFound = "No class found..";
	public static String tooManyClassCannotActivateClass = "too many classes exist by the given information, cannot activate class...";
	public static String noActiveClass = "No Active class...";
	public static String sqlExceptionOccured = "SQLException occured during ";
	public static String newClassInsertion = "new class insertion...";
	public static String retrieveClassList = "retriving class list...";
	public static String activateClass = "activating class...";
	public static String getLatestTerm = "getting latest term";
	public static String showingCategory = "showing category..";
	public static String addingAssignment = "adding assignment..";
	public static String mostRecentTermUndefined = "Most recent term undefined, cannot select class";
	public static String categoryAlreadyExists = "This catehory already exists for this class, replacing weight";
	public static String weightExceedLimit = "Total weight exceeding 100 for this class, cannot insert weight";
	public static String studentAlreadyExists = "student already exists";
	public static String nameDontMatch = "Name does not match!";
	public static String studentDoesNotExist = "Student does not exist...";
	public static String noStudentsFound = "No students found..";
	public static String usernameDoesNotExist = "username does not exist..";
	public static String assignmentNameDoesNotExist = "assignmentname does not exist..";
	public static String pointExceedMaxPossiblePoint = "Point exceeds the maximum possible point!!";
	public static String studentNotEnrolledInCurrentClass = "Student is not enrolled in the current class";
	public static String replacingPreviouslyReceivedGrade = "Replacing previously received grade ";
	public static String addingStudentGrade = "adding student grade...";
	public static String showingStudentGrade = "showing student grades...";
	public static String showingGradebook= "showing gradebook...";
	public static String fileNotFoundExceptionOccured = "FileNotFoundException occured during reading CSV file...";
	public static String ioExceptionOccured = "IOException occured during reading CSV file...";
	public static String readingCsvFile = "reading CSV file...";
	public static String interruptedExceptionOccured = "InterruptedException occured during ";
	public static String sectionNoMustBeInteger = "Section No must be an integer";
	public static String weightMustBeInteger = "Weight must be an integer";
	public static String pointsMustBeInteger = "Points must be an integer";
	public static String studentIdMustBeInteger = "Student id must be an integer";
	public static String gradeMustBeInteger = "Grade must be an integer";
	public static String studentWithSameIdAlreadyExists = "Student with same student id already exists!";
	//
	public static String from = "from ";
	public static String to = " to ";
	public static String assignmentList = "assignment-list";
	public static String byCategory = "by-category";
}
