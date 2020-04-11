
public class Command
{
	
	private String commandType;
	private String courseNumber;	//e.g. CS410
	private String courseTerm;	//e.g. sp20
	private String courseSectionNo;	//e.g. 1
	private String courseDescription;	//e.g. Database
	
	private String assignmentName;	//e.g. Assignment1
	private String assignmentDescription;	//e.g. Implement some databse
	private String assignmentPointValue;	//e.g. 40
	
	private String studentFullName;		//e.g. Ashley Lynn
	private String studentUserName;		//e.g. ashley123
	
	private String categoryName;	//e.g. project
	
	private String categoryWeightForCourse;	//e.g. 20
	private String studentReceivedGradeForCourse;	//e.g. 30
	
	public Command()
	{
		
	}
	
	//get, set of commandType
	public String GetCommandType()
	{
		return this.commandType;
	}
	public void SetCommandType(String commandType)
	{
		this.commandType = commandType;
	}
	
	//get, set of courseNumber
	public String GetCourseNumber()
	{
		return this.courseNumber;
	}
	public void SetCourseNumber(String courseNumber)
	{
		this.courseNumber = courseNumber;
	}
	
	//get, set of courseTerm
	public String GetCourseTerm()
	{
		return this.courseTerm;
	}
	public void SetCourseTerm(String courseTerm)
	{
		this.courseTerm = courseTerm;
	}
	
	//get, set of courseSectionNo
	public String GetCourseSectionNo()
	{
		return this.courseSectionNo;
	}
	public void SetCourseSectionNo(String courseSectionNo)
	{
		this.courseSectionNo = courseSectionNo;
	}
	
	//get, set of courseDescription
	public String GetCourseDescription()
	{
		return this.courseDescription;
	}
	public void SetCourseDescription(String courseDescription)
	{
		this.courseDescription = courseDescription;
	}
	
	//get, set of assignmentName
	public String GetAssignmentName()
	{
		return this.assignmentName;
	}
	public void SetAssignmentName(String assignmentName)
	{
		this.assignmentName = assignmentName;
	}
	
	//get, set of assignmentDescription
	public String GetAssignmentDescription()
	{
		return this.assignmentDescription;
	}
	public void SetAssignmentDescription(String assignmentDescription)
	{
		this.assignmentDescription = assignmentDescription;
	}
	
	//get, set of assignmentPointValue
	public String GetAssignmentPointValue()
	{
		return this.assignmentPointValue;
	}
	public void SetAssignmentPointValue(String assignmentPointValue)
	{
		this.assignmentPointValue = assignmentPointValue;
	}
	
	//get, set of studentFullName
	public String GetStudentFullName()
	{
		return this.studentFullName;
	}
	public void SetStudentFullName(String studentFullName)
	{
		this.studentFullName = studentFullName;
	}
	
	//get, set of studentUserName
	public String GetStudentUserName()
	{
		return this.studentUserName;
	}
	public void SetStudentUserName(String studentUserName)
	{
		this.studentUserName = studentUserName;
	}
	
	//get, set of categoryName
	public String GetCategoryName()
	{
		return this.categoryName;
	}
	public void SetCategoryName(String categoryName)
	{
		this.categoryName = categoryName;
	}
	
	//get, set of categoryWeightForCourse
	public String GetCategoryWeightForCourse()
	{
		return this.categoryWeightForCourse;
	}
	public void SetCategoryWeightForCourse(String categoryWeightForCourse)
	{
		this.categoryWeightForCourse = categoryWeightForCourse;
	}
	
	//get, set of studentReceivedGradeForCourse
	public String GetStudentReceivedGradeForCourse()
	{
		return this.studentReceivedGradeForCourse;
	}
	public void SetStudentReceivedGradeForCourse(String studentReceivedGradeForCourse)
	{
		this.studentReceivedGradeForCourse = studentReceivedGradeForCourse;
	}
}
