package miniJava.SyntacticAnalyzer;

public class SourcePosition {
	
	public String filename;
	public int linenumber;
	public int offset;
	
	
	public SourcePosition(String filename, int linenumber) {
		
		this.filename = filename;
		this.linenumber = linenumber;
	}
	
	public SourcePosition(int offset, int linenumber) {
		this.offset = offset;
		this.linenumber = linenumber;
	}
	
	public int getlineNumber() {
		return this.linenumber;
	}
	
	public String getfilename() {
		return this.filename;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public SourcePosition copy() {
		return new SourcePosition(offset, linenumber);
	}
	
	

}
