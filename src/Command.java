
public class Command
{
	
	private String commandType;		//new-class
	private String courseNumber;	//e.g. CS410
	private String courseTerm;	//e.g. sp20
	private String courseSectionNo;	//e.g. 1
	private String courseDescription;	//e.g. Database
	
	private String assignmentName;	//e.g. Assignment1
	private String assignmentCategory;
	private String assignmentDescription;	//e.g. Implement some databse
	private String assignmentPointValue;	//e.g. 40
	
	private String studentId;	//e.g. 123
	private String studentFullName;		//e.g. Ashley Lynn
	private String studentUserName;		//e.g. ashley123
	
	private String categoryName;	//e.g. project
	
	private String categoryWeightForCourse;	//e.g. 20
	private String studentReceivedGradeForCourse;	//e.g. 30
	
	private String usernameSubstring;
	
	public Command()
	{
		this.commandType = null;
		this.courseNumber = null;
		this.courseTerm = null;
		this.courseSectionNo = null;
		this.courseDescription = null;
		this.assignmentName = null;
		this.assignmentCategory = null;
		this.assignmentDescription = null;
		this.assignmentPointValue = null;
		this.studentId = null;
		this.studentFullName = null;
		this.studentUserName = null;
		this.categoryName = null;
		this.categoryWeightForCourse = null;
		this.studentReceivedGradeForCourse = null;
	}
	
	public Command(String commandType)
	{
		this.commandType = commandType;
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
	
	//get, set of assignmentCategory
	public String GetAssignmentCategory()
	{
		return this.assignmentCategory;
	}
	public void SetAssignmentCategory(String assignmentCategory)
	{
		this.assignmentCategory = assignmentCategory;
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
	
	//get, set of studentId
	public String GetStudentId()
	{
		return this.studentId;
	}
	public void SetStudentId(String studentId)
	{
		this.studentId = studentId;
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
	
	//get, set of usernameSubstring
	public String GetUsernameSubstring()
	{
		return this.usernameSubstring;
	}
	public void SetUsernameSubstring(String usernameSubstring)
	{
		this.usernameSubstring = usernameSubstring;
	}
	
	public String toString()
	{
		return "commandType: " + this.commandType + ", courseNumber: " + this.courseNumber + ", courseTerm: " + this.courseTerm
				+ ",\n courseSectionNo: " + this.courseSectionNo + ", courseDescription: " + this.courseDescription
				+ ",\n assignmentName: " + this.assignmentName + ", assignmentCategory: " + this.assignmentCategory
				+ ",\n assignmentDescription: " + this.assignmentDescription + ", assignmentPointValue: " + this.assignmentPointValue
				+ ",\n studentId: " + this.studentId + ", studentFullName: " + this.studentFullName + ", studentUserName: " + this.studentUserName
				+ ",\n categoryName: " + this.categoryName + ", categoryWeightForCourse: " + this.categoryWeightForCourse
				+ ",\n studentReceivedGradeForCourse: " + this.studentReceivedGradeForCourse + ", usernameSubstring: " + this.usernameSubstring;
	}
}
