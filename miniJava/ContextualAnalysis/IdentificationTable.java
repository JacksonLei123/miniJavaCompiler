package miniJava.ContextualAnalysis;

import java.util.HashMap;
import java.util.Stack;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;

public class IdentificationTable {
	
	public Stack<HashMap<String, Declaration>> idtable = new Stack<HashMap<String, Declaration>>();
	public HashMap<String, Declaration> classes;
	public HashMap<String, HashMap<String, Declaration>> fields;
	public HashMap<String, HashMap<String, Declaration>> methods;
	public HashMap<String, HashMap<String, Declaration>> params;
	
	ErrorReporter errorreporter;
	
	MethodDecl currentMethod;
	
	String referencedClass;
	
	boolean initialized = false;
	
	public IdentificationTable(ErrorReporter errorreporter) {
		
		classes = new HashMap<String, Declaration>();
		fields = new HashMap<String, HashMap<String, Declaration>>();
		methods = new HashMap<String,HashMap<String, Declaration>>();
		params = new HashMap<String, HashMap<String, Declaration>>();
		this.errorreporter = errorreporter;
		openScope();
		
		MethodDeclList printmethods = new MethodDeclList();
		ParameterDeclList printparams = new ParameterDeclList();
        MemberDecl printfield = new FieldDecl(false, false, new BaseType(TypeKind.VOID, null), "println", null);
        ParameterDecl parameter = new ParameterDecl(new BaseType(TypeKind.INT, null), "n", null);
        printparams.add(parameter);
        MethodDecl printlnMethod = new MethodDecl(printfield, printparams, new StatementList(), null);
        printmethods.add(printlnMethod);
        ClassDecl printdecl = new ClassDecl("_PrintStream", new FieldDeclList(), printmethods, null);
        Identifier printidentifier = new Identifier(new Token(TokenKind.ID, "_PrintStream"));
        printdecl.type = new ClassType(printidentifier, new SourcePosition(0, 0));
        enter("_PrintStream", printdecl);
        printlnMethod.printornot = true;

        // add class System { public static _Printstream out; }
        
        FieldDeclList systemfields = new FieldDeclList();
        
        FieldDecl out = new FieldDecl(false, true, new ClassType(printidentifier, null), "out", null);
        
        systemfields.add(out);
        ClassDecl systemDecl = new ClassDecl("System", systemfields, new MethodDeclList(), null);
        
        Identifier systemid = new Identifier(new Token(TokenKind.ID, "System"));
        
        systemDecl.type = new ClassType(systemid, new SourcePosition(0, 0));
        enter("System", systemDecl);
        
        // add class String { }
        ClassDecl StringDecl = new ClassDecl("String", new FieldDeclList(), new MethodDeclList(), null);
        StringDecl.type = new BaseType(TypeKind.UNSUPPORTED, null);
        enter("String", StringDecl);
		
		initialized= true;
		
		
	}
	
	public void openScope() {
		
		idtable.push(new HashMap<String, Declaration>());
	}
	
	public void closeScope() {
		
		idtable.pop();

	}
	
	public int currentLevel() {
		
		return idtable.size() - 1;
	}
	
	
	public void enter(String id, Declaration decl) {
		
	//	 !initialized || currentLevel() > 0;
		
		assert !initialized || idtable.size()- 1 > 0;
		System.out.println("aldsfkjakle");
		System.out.println(id);
		System.out.println(idtable.size());
		if (alreadyDeclaredInCurrentScope(id)) {
            errorreporter.reportError("*** line " + decl.posn.linenumber + ": name has been declared twice");
        } else if (alreadyDeclaredInNestedScope(id)) {
            errorreporter.reportError("*** line " + decl.posn.linenumber + ": declaration is hidden from too high of a scope");
        } else {
            idtable.peek().put(id, decl);
            
        }
		
		
		
	}
	
	public int getLevel(String id) {
	
        for (int level = idtable.size() - 1; level >= 0; level--) {
            HashMap<String, Declaration> scope = idtable.get(level);
            if (scope.containsKey(id)) {
            	
                return level;
            }
        }
        
        for (String className : classes.keySet()) {
            if (id.equals(className))
                return 1; // class

            if (fields.containsKey(className) && fields.get(className).containsKey(id))
                return 2; // class members
        }

        return 0;
    }
	
    public Declaration retrieve(String id) {
    	
    	System.out.println(this.referencedClass);
    	System.out.println(idtable.size());
    	System.out.println(idtable.get(idtable.size() - 1).containsKey("System"));
    	System.out.println(id);
    	if (this.referencedClass != null) {
            // we are only retrieving from this class
    		
            String className = this.referencedClass;
          
            if (methods.get(this.referencedClass) != null && methods.get(this.referencedClass).containsKey(id))
                return methods.get(this.referencedClass).get(id);

            if (fields.get(this.referencedClass) != null && fields.get(this.referencedClass).containsKey(id))
                return fields.get(this.referencedClass).get(id);
            
            System.out.println("Retrieval of " + id + " failed in classNameContext " + this.referencedClass);
            return null;
        }
    	
    	int stage = -1;
    	
    	for (int level = idtable.size() - 1; level >= 0; level--) {
    		System.out.println(level);
    		System.out.println(idtable.size() - 1);
    		System.out.println(idtable.get(idtable.size() - 1).containsKey("System"));
    		System.out.println(idtable.get(level).containsKey(id));
            HashMap<String, Declaration> scope = idtable.get(level);
            if (idtable.get(level).containsKey(id)) {
            	
                stage = level;
                break;
            }
        }
    	System.out.println(stage);
    	if (stage == -1) {
    		
    		for (String className : classes.keySet()) {
            	
                if (id.equals(className))
                	stage = 1;
                    
                if (fields.containsKey(className) && fields.get(className).containsKey(id))
                    stage = 2;
            }	
    		
    	}
        
    	if (stage == -1) {
    		
    		stage = 0;
    	}
    	
    	
    	if (stage == 0 && classes.containsKey(id)) {
            return classes.get(id);
        }
    	
    	if (idtable.get(stage).containsKey(id)) {
    		
    		return idtable.get(stage).get(id);
    	}
    	
    	for (String className : classes.keySet()) {
    		
    		if (id.equals(className)) {
    			
    			return classes.get(className);
    			
    		}
               
            if (methods.get(className) != null && methods.get(className).containsKey(id)) {
            	return methods.get(className).get(id);
            	
            }
                

            if (fields.get(className) != null && fields.get(className).containsKey(id)) {
            	
            	return fields.get(className).get(id);
            }
                
    		
    		
    	}
    	
    	return null;
    	
    }
    
    public void setreferencedClass(String className) {
        // restricts table retrieval to class with a given name
        this.referencedClass = className;
    }
    
    public void makeReferencedClassNull() {
        this.referencedClass = null;
    }

    
    public boolean alreadyDeclaredInCurrentScope(String id) {
    	
    	if (idtable.size() == 0) {
    		return false;
    	}
        return idtable.peek().containsKey(id);
    }

    public boolean alreadyDeclaredInNestedScope(String id) {
        for (int level = 3; level < idtable.size(); level++) {
            if (idtable.get(level).containsKey(id)) {
                return true;
            }
        }

        return false;
    }
	
	
	

}
