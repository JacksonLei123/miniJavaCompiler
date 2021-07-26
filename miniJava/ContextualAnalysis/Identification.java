package miniJava.ContextualAnalysis;

import java.util.HashMap;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassDeclList;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class Identification implements Visitor<Object, Object> {
	
	public IdentificationTable table;
	private ErrorReporter reporter;
	String nameUsedforDeclaration;
	MethodDecl currentMethod = null;
	ClassDecl currentClass = null;
	
	public Identification(Package ast, ErrorReporter reporter) {
		
		this.reporter = reporter;
		table = new IdentificationTable(reporter);
		
		table.classes.put("System", table.retrieve("System"));
        table.classes.put("_PrintStream", table.retrieve("_PrintStream"));
        table.classes.put("String", table.retrieve("String"));

        table.fields.put("System", new HashMap<String, Declaration>());
        table.fields.get("System").put("out", ((ClassDecl) table.retrieve("System")).fieldDeclList.get(0));

        table.fields.put("_PrintStream", new HashMap<String, Declaration>());
        table.fields.get("_PrintStream").put("println",
                ((ClassDecl) table.retrieve("_PrintStream")).methodDeclList.get(0));
		
	}
	
	public void startVisit(AST ast) {
			
		ast.visit(this, table);
	}

	@Override
	public Object visitPackage(Package prog, Object arg) {
	
		ClassDeclList cl = prog.classDeclList;
		
		for (ClassDecl cd: cl) {
			
			if (!table.classes.containsKey(cd.name)) {
				table.classes.put(cd.name, cd);
			
				table.fields.put(cd.name, new HashMap<String, Declaration>());
				table.methods.put(cd.name, new HashMap<String, Declaration>());
				
			} else {
				
				reporter.reportError("*** line " + cd.posn.linenumber + ": duplicate class");
			}
			
			
			for (FieldDecl fieldDecl: cd.fieldDeclList) {
				
				table.fields.get(cd.name).put(fieldDecl.name, fieldDecl);
		
				
				
			}
			
			for (MethodDecl methodDecl: cd.methodDeclList) {
				table.fields.get(cd.name).put(methodDecl.name, methodDecl);
				table.methods.get(cd.name).put(methodDecl.name, methodDecl);
			}
		}
		
        for (ClassDecl c: prog.classDeclList){
            c.visit(this, table);
        }
        return null;

	}

	@Override
	public Object visitClassDecl(ClassDecl cd, Object arg) {
		System.out.println("class");
	
		table.openScope();
		table.enter(cd.name, cd);
		
		table.openScope();
		
		for (FieldDecl f: cd.fieldDeclList)
        	f.visit(this, table);
        
        for (MethodDecl m: cd.methodDeclList)
        	m.visit(this, table);
        
        table.closeScope();
        table.closeScope();
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		
		System.out.println("field");
	
		table.enter(fd.name, fd);
		fd.type.visit(this, table);
		
		return null;
		
		
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) {
		System.out.println("method");
		currentMethod = md;
		
		if (md.type instanceof ClassType) {
			
			Declaration classDecl = null;
			
			if (table.classes.containsKey(((ClassType) md.type).className.spelling)) {
		            classDecl = table.classes.get(((ClassType) md.type).className.spelling);
		     }
			
			if (classDecl == null) {
				reporter.reportError("*** line " + md.posn.linenumber + ": method declaration is going to return type that doesn't exist");
				
			}
    	}
	
		table.enter(md.name, md);
		table.openScope(); 
		
		for (ParameterDecl pd: md.parameterDeclList) {
			
            pd.visit(this, table);
        }
		
		table.openScope(); 
		
        StatementList sl = md.statementList;
        for (Statement s: sl) {
            s.visit(this, table);
        }
        
        table.closeScope();
        table.closeScope();
		
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) {
		System.out.println("parameter");

		table.enter(pd.name, pd);
		
		pd.type.visit(this, table);
		
		if (pd.type instanceof ClassType) {
			ClassType classType = (ClassType) pd.type;
			String className = classType.className.spelling;
			
			Declaration exists = null;
			 if (table.classes.containsKey(className)) {
				 exists = table.classes.get(className);
	        } 
			
			if (exists == null) {
				reporter.reportError("*** line " + pd.posn.linenumber + ": the parameter references an undefined class");
			}
		}
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) {
		System.out.println("var decl");
        table.enter(decl.name, decl);
       
        decl.type.visit(this, table);
		return null;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) {
		
		type.className.visit(this, table);
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) {
		
		type.eltType.visit(this, table);
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		System.out.println("block stmt");
        table.openScope();

        for (Statement statement : stmt.sl)
            statement.visit(this, table);

        table.closeScope();
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		System.out.println("vardeclstmt");
		this.nameUsedforDeclaration = stmt.varDecl.name;
        stmt.initExp.visit(this, table);        
        this.nameUsedforDeclaration = null;
        stmt.varDecl.visit(this, table);
    
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		System.out.println("assign stmt");
		
		stmt.ref.visit(this, table);
        stmt.val.visit(this, table);
        
//        System.out.println(table.methods.get(table.currentClass().getClass().getName()).containsKey(stmt.ref.));
        
        if (stmt.ref instanceof QualRef) {
        	
        	QualRef arrayref = (QualRef) stmt.ref;
        	
        	if (arrayref.ref.decl.type.typeKind == TypeKind.ARRAY && arrayref.id.spelling.equals("length")) {
        		
        		reporter.reportError("*** line " + stmt.posn.linenumber + ": cannot assign array length to something else");
            	System.exit(4);
            }
        }
        
        
        
        if (stmt.ref.decl instanceof MethodDecl) {
        	
        	reporter.reportError("*** line " + stmt.posn.linenumber + ": cannot assign from a method");
        	
        }
  
        if (stmt.val instanceof RefExpr) {
        	
			if (((RefExpr) stmt.val).ref instanceof IdRef) {
				String idName = ((IdRef) ((RefExpr) stmt.val).ref).id.spelling;
				int whereitIs = 0;
				
				for (int level = table.idtable.size() - 1; level >= 0; level--) {
		            
		            if (table.idtable.get(level).containsKey(idName)) {
		            	
		                whereitIs = level;
		            }
		        }
		     
				if (whereitIs == 0) {
					
					for (String className : table.classes.keySet()) {
			            if (idName.equals(className))
			                whereitIs = 1;

			            if (table.fields.containsKey(className) && table.fields.get(className).containsKey(idName))
			                whereitIs = 2;
			        }	
					
				}
		       System.out.println(whereitIs);
		        
				if (whereitIs == 1) {
					reporter.reportError(
							"*** line " + stmt.posn.linenumber + ": cannot assign variable to a class name");
				}
			}
		}
        
		return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, Object arg) {
		
		stmt.ref.visit(this, table);
        stmt.ix.visit(this, table);
        stmt.exp.visit(this, table);
        
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) {
		
        stmt.methodRef.visit(this, table);
     
        for (Expression e: stmt.argList) {
            e.visit(this, table);
            
            if (!(stmt.methodRef.decl instanceof MethodDecl)) {
            	reporter.reportError("*** line " + stmt.posn.linenumber + ": static method called in illegal context.");
            }
            
           
        }
        
        if (stmt.methodRef.decl instanceof VarDecl) {
        	
        	reporter.reportError("*** line " + stmt.posn.linenumber + ": attempted method call with a variable");
        	
        	System.exit(4);
        }
        
        if (stmt.methodRef instanceof ThisRef) {
        	
        	reporter.reportError("*** line " + stmt.posn.linenumber + ": attempted method call with a This reference");
        	
        	System.exit(4);
        }
        
        MethodDecl calledMethod = (MethodDecl) stmt.methodRef.decl;
        
        if (this.currentMethod.isStatic && !(stmt.methodRef instanceof QualRef) && !calledMethod.isStatic) {
            reporter.reportError("*** line " + stmt.posn.linenumber + ": cannot call a nonstatic method in inside a static method");
        }
        
        if (stmt.methodRef instanceof QualRef) {
        	
            QualRef qualRef = (QualRef) stmt.methodRef;
            
            if (qualRef.ref instanceof IdRef) {
            	
                IdRef idRef = (IdRef) qualRef.ref; 
                Declaration idDecl = idRef.decl;
                
                if (idDecl instanceof VarDecl && idDecl.type instanceof ClassType) {
                	
                	String classDec = ((ClassType) idDecl.type).className.spelling;
                	Declaration idDecl2 = null;
                	
                	
                	if (table.classes.containsKey(classDec)) {
                		idDecl2 = table.classes.get(classDec);
                	}
                	
//                    idDecl = table.getClass(((ClassType) idDecl.type).className.spelling);
                	
                	Declaration outsideMethod = null;
                	
                	if (table.classes.containsKey(idDecl2.name)) {
                		
                        if (table.methods.get(idDecl2.name).containsKey(calledMethod.name)) {
                        	
                            outsideMethod = (MethodDecl) table.methods.get(idDecl2.name).get(calledMethod.name);
                            
                            if (((MethodDecl)outsideMethod).isPrivate) {
                                reporter.reportError(
                                        "*** line " + stmt.posn.linenumber + ": cannot call private method outside of class");
                            }
                        }
                    }

                 

                } else if (idDecl instanceof ClassDecl) {
                	
                	Declaration outsideMethod = null;
                	
                	if (table.classes.containsKey(idDecl.name)) {
                		
                		System.out.println(calledMethod == null);
                		
                        if (table.methods.get(idDecl.name).containsKey(calledMethod.name)) {
                        	
                        	
                        	
                            outsideMethod = (MethodDecl) table.methods.get(idDecl.name).get(calledMethod.name);
                        }
                    }
                	
                    if (outsideMethod instanceof MethodDecl) {
                    	
                    	Declaration outsideMethod2 = null;
                    	
                    	if (table.classes.containsKey(idDecl.name)) {
                    		
                            if (table.methods.get(idDecl.name).containsKey(calledMethod.name)) {
                            	
                                outsideMethod2 = (MethodDecl) table.methods.get(idDecl.name).get(calledMethod.name);
                                
                                if (!((MethodDecl)outsideMethod2).isStatic) {
                                    reporter.reportError(
                                            "*** line " + stmt.posn.linenumber + ": can't call static method like that.");
                                }

                                if (((MethodDecl)outsideMethod2).isPrivate) {
                                    reporter.reportError(
                                            "*** line " + stmt.posn.linenumber + ": method is private so can't call it outside of its class.");
                                }
                            }
                        }
                    	
                    
                    } else {
                        reporter.reportError(
                                "*** line " + stmt.posn.linenumber + ": called statement did not use a method");
                    }
                }
                
            }
        }
        
        
        
        return null;
    }
	

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
		
		if (stmt.returnExpr != null)
    		stmt.returnExpr.visit(this, table);
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) {
		
		System.out.println("ifstmt");
		stmt.cond.visit(this, table);
		
		if (stmt.thenStmt instanceof VarDeclStmt) {
			
            reporter.reportError("*** line " + stmt.thenStmt.posn.linenumber + ": the body of this if statement is just a variable declaration statement ");
        }
		
        stmt.thenStmt.visit(this, table);
        
        if (stmt.elseStmt != null) {
        	
            stmt.elseStmt.visit(this, table);
            
            if (stmt.elseStmt instanceof VarDeclStmt) {
                reporter.reportError(
                        "*** line " + stmt.elseStmt.posn.linenumber + ": else statement is just a variable declaration statement");
            }
        
        }
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
        
        if (stmt.body instanceof VarDeclStmt) {
            reporter.reportError("*** line " + stmt.body.posn.linenumber + ": while statement is just a variable declaration statement");
        }
        
        stmt.cond.visit(this, table);
        stmt.body.visit(this, table);
        
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		expr.operator.visit(this, table);
        expr.expr.visit(this, table);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		expr.operator.visit(this, table);
        expr.left.visit(this, table);
        expr.right.visit(this, table);
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) {
		expr.ref.visit(this, table);
		return null;
	}

	@Override
	public Object visitIxExpr(IxExpr expr, Object arg) {
		expr.ref.visit(this, table);
        expr.ixExpr.visit(this, table); 
		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) {
		expr.functionRef.visit(this, table);
        for (Expression expression : expr.argList) {
            expression.visit(this, table);
        }
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		expr.lit.visit(this, table);
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		expr.classtype.visit(this, table);
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		
		expr.eltType.visit(this, table);
        expr.sizeExpr.visit(this, table);

		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) {
		
		ref.decl = (Declaration) table.idtable.get(1).values().toArray()[0];
		
	//	ref.decl =  (ClassDecl) table.idtable.get(1).values().iterator().next();
		
        
        if (this.currentMethod.isStatic) {
        	
            reporter.reportError("*** line " + ref.posn.linenumber + ": this reference in a static method.");
        }
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, Object arg) {
		System.out.println("idref");
		
		if (ref.id.spelling.equals(this.nameUsedforDeclaration)) {
			reporter.reportError("*** line " + ref.posn.linenumber + ": variable initialization cannot use itself");
    	}
		
		ref.id.visit(this, table);
		
		ref.decl = ref.id.decl;
//		System.out.println(((ClassType)ref.id.decl.type).className.spelling);
		
		if (ref.decl instanceof MethodDecl) {
			
        	if (((MethodDecl) ref.decl).isStatic && !this.currentMethod.isStatic || !((MethodDecl) ref.decl).isStatic && this.currentMethod.isStatic) {
        		
				reporter.reportError("*** line " + "method inside and the method outside have unmatching staticness");
        	}
        } else if (ref.decl instanceof FieldDecl) {
        	
        	if (((FieldDecl) ref.decl).isStatic != this.currentMethod.isStatic) {
        		
				reporter.reportError("*** line " + ref.posn.linenumber+ " field does not match the staticness of the method");
        	}
        }
		
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, Object arg) {
		System.out.println("qref");

		ref.ref.visit(this, table);
	
		if (ref.ref instanceof IdRef) {
			
			String x = ((IdRef) ref.ref).id.spelling;
			
			
		
		}
		
		
		if (ref.ref.decl == null) {
			
			return null;
		}
		
		if (ref.ref.decl instanceof MethodDecl) {
		
			reporter.reportError("*** line " + ref.posn.linenumber + " can't have function in the middle of qualrefs");
		}
		
		
		if (ref.ref.decl.type instanceof ClassType) {
            String className = ((ClassType) ref.ref.decl.type).className.spelling;
            String className2 = ref.ref.decl.name;
            
           System.out.println("saljelskjfalfj");
           System.out.println(className);
            table.setreferencedClass(className);
        }
		
		
		if (ref.ref.decl.type.typeKind == TypeKind.ARRAY && ref.id.spelling.equals("length")) {
			
			
			ref.id.decl = new FieldDecl(false, false, new BaseType(TypeKind.INT, ref.ref.posn), "length", ref.ref.posn);
			
			
		} else {
			
			ref.id.visit(this,table);
			
		}
		
//		ref.id.visit(this,table);

		ref.decl = ref.id.decl;
		
		table.makeReferencedClassNull();
		
		if (ref.ref instanceof IdRef) {
			String idName = ((IdRef) ref.ref).id.spelling;
			
			int tableLevel = -1;
			
			for (int level = table.idtable.size() - 1; level >= 0; level--) {
	   
	            if (table.idtable.get(level).containsKey(idName)) {
	            	
	                tableLevel = level;
	            }
	        }
			
			if (tableLevel == -1) {
				
				for (String className : table.classes.keySet()) {
		            if (idName.equals(className))
		                tableLevel = 1;

		            if (table.fields.containsKey(className) && table.fields.get(className).containsKey(idName))
		                tableLevel = 2;
		        }
				
			}
	        
	        if (tableLevel == -1) {
	        	
	        	tableLevel = 0;
	        }

			if (tableLevel == 1) {
				
				ref.ref.decl.classStaticaccess = true;
				ref.decl.classStaticaccess = true;
				
		        if (ref.id.decl instanceof FieldDecl) {
		        	
		        	if (this.currentMethod.isStatic && !((FieldDecl)ref.id.decl).isStatic) {
		        		
						reporter.reportError("*** line " + ref.posn.linenumber + " current method and the field do not match staticness");
		        	}
		        }
			}
		}
		
		
		
    	
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) {

		id.decl = table.retrieve(id.spelling);

//		id.decl = table.getFieldFromClass(id.spelling, table.currentClass().getClass().getName());

		
		if (id.decl == null) {
			
            reporter.reportError("*** line " + id.posn.linenumber + ": Identifier has no known declaration.");
      
        }
		
		ClassDecl curr = (ClassDecl) table.idtable.get(1).values().toArray()[0];
		
		String currentClassName = curr.name;
		
		Declaration field = null;
//		if (table.classes.containsKey(currentClassName)) {
//            if (table.fields.get(currentClassName).containsKey(id)) {
//                field = table.fields.get(currentClassName).get(id.spelling);
//            }
//        }
		
		boolean fieldisWithinClass = false;
		
		if (table.classes.containsKey(currentClassName)) {
			
            if (table.fields.get(currentClassName).containsKey(id.spelling)) {
           
                fieldisWithinClass = true;
            }
        }
		
		
		if (id.decl instanceof FieldDecl && !fieldisWithinClass) {
			
		
			
			if (((FieldDecl)id.decl).isPrivate) {
				reporter.reportError("*** line " + id.posn.linenumber + " accessed private field " + id.spelling
						+ " in illegal context.");
			}
		}
		
	
//		if (id.decl instanceof FieldDecl && field == null) {
//			
//			if (((FieldDecl) id.decl).isPrivate) {
//				
//				reporter.reportError("*** line " + id.posn.linenumber + " accessed private field " + id.spelling + " in illegal context.");
//			}
//		}
		return null;
	}

	@Override
	public Object visitOperator(Operator op, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nul, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

}
