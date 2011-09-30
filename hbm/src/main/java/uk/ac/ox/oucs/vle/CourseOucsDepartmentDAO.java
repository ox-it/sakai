package uk.ac.ox.oucs.vle;


public class CourseOucsDepartmentDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String oucsCode;
    private String nickName;
    private String fullName;
    private String f4Char;
    private String mailer;
    private String t2Char;
    private String cardCode;
    
    public CourseOucsDepartmentDAO() {
    }
    
    public CourseOucsDepartmentDAO(String code) {
        this.cardCode = code;
    }
    
    public String getCardCode() {
        return this.cardCode;
    }
    
    public void setCardCode(String code) {
        this.cardCode = code;
    }
    
    public String getNickName() {
        return this.nickName;
    }
    
    public void setNickName(String name) {
        this.nickName = name;
    }
    
    public String getFullName() {
        return this.fullName;
    }
    
    public void setFullName(String name) {
        this.fullName = name;
    }
    
    public String getOucsCode() {
        return this.oucsCode;
    }
    
    public void setOucsCode(String name) {
        this.oucsCode = name;
    }
    
    public String getF4Char() {
        return this.f4Char;
    }
    
    public void setF4Char(String name) {
        this.f4Char = name;
    }
    
    public String getT2Char() {
        return this.t2Char;
    }
    
    public void setT2Char(String name) {
        this.t2Char = name;
    }
    
    public String getMailer() {
        return this.mailer;
    }
    
    public void setMailer(String name) {
        this.mailer = name;
    }
}
