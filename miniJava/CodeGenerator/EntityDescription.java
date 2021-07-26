package miniJava.CodeGenerator;

import mJAM.Machine.Reg;

public class EntityDescription {
	
	public int offset;
	public Reg base;
	
	public EntityDescription(int offset, Reg base) {
		
		this.offset = offset;
		this.base = base;
	}

}
