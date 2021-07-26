/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.EntityDescription;
import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Declaration extends AST {
	
	public Declaration(String name, TypeDenoter type, SourcePosition posn) {
		super(posn);
		this.name = name;
		this.type = type;
		this.description = null;
		this.classStaticaccess = false;
		this.printornot = false;
	}
	
	public String name;
	public TypeDenoter type;
	public EntityDescription description;
	public boolean classStaticaccess;
	public boolean printornot;
}
