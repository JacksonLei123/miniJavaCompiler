 package miniJava.ContextualAnalysis;

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
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.ExprList;
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
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.TokenKind;

public class TypeChecking implements Visitor<Object, TypeDenoter>{
	
	Identification identifier;
	ErrorReporter errorreporter;
	
	public TypeChecking(Identification identifier, ErrorReporter reporter) {
        this.identifier = identifier;
        this.errorreporter = reporter;
    }
	
	public void typeCheck(AST ast) {
		
	    ast.visit(this, null);
	}
	 
	 private boolean typeKindEquals(TypeDenoter type1, TypeKind type2) {
		 if (type1 == null || type2 == null) {
			return false;
		 }
	    	
	     return type1.typeKind == type2;
	 }
	 
	 private boolean typesEq(TypeDenoter type1, TypeDenoter type2) {
		 
		 if (type1 == null || type2 == null) {
	            return false;
	        }
	               
	        if (type1.typeKind == TypeKind.ERROR || type2.typeKind == TypeKind.ERROR) {
	            return true;
	        } else if (type1.typeKind == TypeKind.UNSUPPORTED || type2.typeKind == TypeKind.UNSUPPORTED) {
	            return false;
	        } else if (type1 instanceof ArrayType || type2 instanceof ArrayType) {
	        	// arrays can be assigned and compared to null
	        	if (type1.typeKind == TypeKind.NULL || type2.typeKind == TypeKind.NULL) {
	        		return true;
	        	}
	        	
	            if (!(type1 instanceof ArrayType) || !(type2 instanceof ArrayType)) {
	                return false;
	            }

	            return typesEq(((ArrayType) type1).eltType, ((ArrayType) type2).eltType);
	        } else if (type1 instanceof ClassType || type2 instanceof ClassType) {            
	            // Class objects can be assigned to null
	            if (type1.typeKind == TypeKind.CLASS && type2.typeKind == TypeKind.NULL) {
	                return true;
	            } else if (type1.typeKind == TypeKind.NULL && type2.typeKind == TypeKind.CLASS) {
	                return true;
	            } else if (!(type1 instanceof ClassType) || !(type2 instanceof ClassType)) {
	                return false;
	            }

	            Identifier className1 = ((ClassType) type1).className;
	            Identifier className2 = ((ClassType) type2).className;
	     
	            
	            if (className1.decl != null && className2.decl != null) {
	    			if (typeKindEquals(className1.decl.type, TypeKind.UNSUPPORTED)
	    					|| typeKindEquals(className2.decl.type, TypeKind.UNSUPPORTED)) {
	    				return false;
	    			}
	            }
	            
	            return className1.spelling.equals(className2.spelling); // name equivalence
	        }

	        return type1.typeKind == type2.typeKind;
		 
//		 if (type1 == null || type2 == null) {
//	            return false;
//	     }
////		 
//		 if (type1.typeKind == TypeKind.ERROR || type2.typeKind == TypeKind.ERROR) {
//			 return true;
//		 }
//		 
//		 if (type1 instanceof ArrayType || type2 instanceof ArrayType) {
//			 
//		
//			 
//			 if (!(type1 instanceof ArrayType) || !(type2 instanceof ArrayType)) {
//	                return false;
//	         }
//			 
//			 return typesEq(((ArrayType) type1).eltType, ((ArrayType) type2).eltType);
//			 
//		 } else if (type1 instanceof ClassType || type2 instanceof ClassType) {            
//	         
//	            if (type1.typeKind == TypeKind.CLASS && type2.typeKind == TypeKind.NULL || type1.typeKind == TypeKind.NULL && type2.typeKind == TypeKind.CLASS) {
//	            	
//	                return true;
//	                
//	            } else if (!(type1 instanceof ClassType) || !(type2 instanceof ClassType)) {
//	            	
//	                return false;
//	                
//	            } 
//
//	            Identifier className1 = ((ClassType) type1).className;
//	            Identifier className2 = ((ClassType) type2).className;
//	            
//	            System.out.println("hello" + className1.spelling);
//	            System.out.println("hello" + className2.spelling);
//	            if (className1.spelling.equals(className2.spelling)) {
//	            	return true;
//	            }
//	            
//	            return false;
//	            
//		 }
		 
		
	//	return type1.typeKind == type2.typeKind;
		 
	 }

	@Override
	public TypeDenoter visitPackage(Package prog, Object arg) {
		
		
		for (ClassDecl classDecl : prog.classDeclList) {
            classDecl.visit(this, null);
        }
        return new BaseType(TypeKind.UNSUPPORTED, prog.posn);
	}

	@Override
	public TypeDenoter visitClassDecl(ClassDecl cd, Object arg) {
		
		for (FieldDecl fieldDecl : cd.fieldDeclList) {
            fieldDecl.visit(this, null);
        }
		
		for (MethodDecl methodDecl : cd.methodDeclList) {
	        methodDecl.visit(this, null);
	    }
		
		return cd.type;
	}

	@Override
	public TypeDenoter visitFieldDecl(FieldDecl fd, Object arg) {

		return fd.type;
	}

	@Override
	public TypeDenoter visitMethodDecl(MethodDecl md, Object arg) {
		
		TypeDenoter returnType = md.type;
		
		for (ParameterDecl parameterDecl : md.parameterDeclList) {
            parameterDecl.visit(this, null);
        }
		
		for (Statement s: md.statementList) {
            TypeDenoter state = s.visit(this, null);
          
            if (s instanceof ReturnStmt && !typesEq(state, returnType)) {
            	
            	errorreporter.reportError("*** line " + s.posn.linenumber + "not matching types between " + state.typeKind+ " and " + returnType.typeKind);
            	
            
            
            }
            
        }
		return returnType;
	}

	@Override
	public TypeDenoter visitParameterDecl(ParameterDecl pd, Object arg) {
		return pd.type;
	
	}

	@Override
	public TypeDenoter visitVarDecl(VarDecl decl, Object arg) {
		 return decl.type;
	}

	@Override
	public TypeDenoter visitBaseType(BaseType type, Object arg) {
		return type;
	}

	@Override
	public TypeDenoter visitClassType(ClassType type, Object arg) {
		return type;
	}

	@Override
	public TypeDenoter visitArrayType(ArrayType type, Object arg) {
		return type;
	}

	@Override
	public TypeDenoter visitBlockStmt(BlockStmt stmt, Object arg) {
		
		for (Statement statement : stmt.sl) {
            statement.visit(this, null);
        }
		return new BaseType(TypeKind.UNSUPPORTED, stmt.posn);
	}

	@Override
	public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		
		TypeDenoter refType = stmt.varDecl.visit(this, null);
        TypeDenoter exprType = stmt.initExp.visit(this, null);
      
    
		if (stmt.initExp instanceof RefExpr) {
		
			if (((RefExpr)stmt.initExp).ref.decl instanceof ClassDecl) {
				errorreporter.reportError("*** line " + stmt.varDecl.posn.linenumber + ": cannot assign something to a class like that");
			}
			
			if (((RefExpr)stmt.initExp).ref.decl instanceof MethodDecl) {
				errorreporter.reportError("*** line " + stmt.varDecl.posn.linenumber + ": cannot assign something to a method like that");
			}
		}
		
		
		if (!typesEq(refType,exprType)) {
			
			if (refType instanceof ClassType && exprType instanceof ClassType) {
				
				if (((ClassType) refType).className.decl == null || ((ClassType) exprType).className.decl == null) {
					errorreporter.reportError("*** line " + stmt.varDecl.posn.linenumber
							+ ": class hasn't been initialized yet");
				} else {
					
					errorreporter.reportError("*** line " + stmt.varDecl.posn.linenumber
							+ " : attempted assignment between two different class types");
				}
				
			} else {
				errorreporter.reportError("*** line " + stmt.varDecl.posn.linenumber + " : attempted assignment between different types");
				
			}
			return new BaseType(TypeKind.ERROR, stmt.posn);
		}
		return refType;
	}

	@Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, Object arg) {
		
		TypeDenoter refType = stmt.ref.visit(this, null);
        TypeDenoter exprType = stmt.val.visit(this, null);
        
        if (stmt.ref instanceof QualRef) {
        	
        	QualRef qref = (QualRef) stmt.ref;
        
//        	if ((typeKindEquals(refType, TypeKind.ARRAY) && qref.id.spelling.equals("length")) ) {
//        		
//        		errorreporter.reportError("*** line" + stmt.posn.linenumber + ": cannot assign array length to something else");
//        		System.exit(4);
//        	}
        	
        	
        }
        
        if (!typesEq(refType, exprType)) {
        	
        	errorreporter.reportError("*** line " + stmt.posn.linenumber + ": Assignment on column "
					+ stmt.posn.offset + " attempts assignment on different types");
        }
        
        
        if (stmt.val instanceof RefExpr) {
			RefExpr refExpr = (RefExpr) stmt.val;			
			if (refExpr.ref.decl instanceof MethodDecl) {
				errorreporter.reportError("*** line " + stmt.posn.linenumber + ": Assignment on column "
						+ stmt.posn.offset + " attempts invalid assignment of method.");
			}
		}
        
		return refType;
	}

	@Override
	public TypeDenoter visitIxAssignStmt(IxAssignStmt stmt, Object arg) {

		TypeDenoter refr = stmt.ref.visit(this, null);
		TypeDenoter exp = stmt.exp.visit(this, null);
		TypeDenoter index = stmt.ix.visit(this, null);
		
		if (!(refr instanceof ArrayType)) {
			
			errorreporter.reportError("*** line " + stmt.posn.linenumber + " should be an array but is not");
			return new BaseType(TypeKind.ERROR, stmt.posn);
			
		} else {
			
			if (exp instanceof ArrayType) {
				
				if (((ArrayType) refr).eltType.typeKind != ((ArrayType)exp).eltType.typeKind) {
					
					errorreporter.reportError("*** line " + stmt.posn.linenumber + " different types between the arrays");
					return new BaseType(TypeKind.ERROR, stmt.posn);
				}
			} else {
				
				if (((ArrayType) refr).eltType.typeKind != exp.typeKind) {
					
					errorreporter.reportError("*** line " + stmt.posn.linenumber + " different types between an array and another reference");
					return new BaseType(TypeKind.ERROR, stmt.posn);
				}
			}
			
		
		}
		
		if (index.typeKind != TypeKind.INT) {
			errorreporter.reportError("*** line " + stmt.posn.linenumber + " cannot index an array using something that is not an INT");
			
			return new BaseType(TypeKind.ERROR, stmt.posn);
		}
		

		
		
		return refr;
	}

	@Override
	public TypeDenoter visitCallStmt(CallStmt stmt, Object arg) {
		
		if (!(stmt.methodRef.decl instanceof MethodDecl)) {
			
            errorreporter.reportError("*** line " + stmt.posn.linenumber + ": that function doesn't exist");
            
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
		
		ParameterDeclList declaredparameters = ((MethodDecl) stmt.methodRef.decl).parameterDeclList;
		
		TypeDenoter declaredreturn = stmt.methodRef.visit(this,  null);
		
		if (declaredparameters.size() != stmt.argList.size()) {
			
	            errorreporter.reportError(
	                    "*** line " + stmt.posn.linenumber + ": function call has different amount of parameters compared to the declared");
	            
	            return new BaseType(TypeKind.ERROR, stmt.posn);
	    }
		 
		 for (int i = 0; i < stmt.argList.size(); i++) {
			 
	            TypeDenoter trueType = declaredparameters.get(i).type;
	            
	            TypeDenoter observedType = stmt.argList.get(i).visit(this, null);
	            
	            if (!typesEq(trueType, observedType)) {
	            	
	                errorreporter.reportError("*** line " + stmt.posn.linenumber + ": the parameter types are different");
	                return new BaseType(TypeKind.ERROR, stmt.posn);
	            }
		 }
		 
		
		return declaredreturn;
	}

	@Override
	public TypeDenoter visitReturnStmt(ReturnStmt stmt, Object arg) {
	
		if (stmt.returnExpr == null) {
			
			return new BaseType(TypeKind.VOID, stmt.posn);
		}
		
		return stmt.returnExpr.visit(this, null);
	}

	@Override
	public TypeDenoter visitIfStmt(IfStmt stmt, Object arg) {
		
		if (!typeKindEquals(stmt.cond.visit(this, null), TypeKind.BOOLEAN)) {
			
            errorreporter.reportError("*** line " + stmt.posn.linenumber + ": if statement condition isn't boolean");
            
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
		
		stmt.thenStmt.visit(this, null);
		
        if (stmt.elseStmt != null) {
        	
            stmt.elseStmt.visit(this, null);
        }
        
        return new BaseType(TypeKind.UNSUPPORTED, stmt.posn);
	}

	@Override
	public TypeDenoter visitWhileStmt(WhileStmt stmt, Object arg) {
		
		TypeDenoter condition = stmt.cond.visit(this, null);
		
		if (condition.typeKind != TypeKind.BOOLEAN) {
			
            errorreporter.reportError("*** line " + stmt.posn.linenumber + ": while statement condition isn't boolean");
            
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
        stmt.body.visit(this, null);
        
        return new BaseType(TypeKind.UNSUPPORTED, stmt.posn);
	}

	@Override
	public TypeDenoter visitUnaryExpr(UnaryExpr expr, Object arg) {
		
		TypeDenoter overallType = null;
		
		if (expr.operator.kind == TokenKind.MINUS) {
			
			TypeDenoter expression = expr.expr.visit(this, null);
			
			if (expression.typeKind != TypeKind.INT) {
				
                errorreporter.reportError("*** line " + expr.posn.linenumber + ": unary expressions using the minus sign should have type INT");
                
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
			
			overallType = new BaseType(TypeKind.INT, expr.posn);
			
		} else if (expr.operator.kind == TokenKind.NOT) {
			
			TypeDenoter expression = expr.expr.visit(this, null);
			
			if (expression.typeKind != TypeKind.BOOLEAN) {
				
                errorreporter.reportError("*** line " + expr.posn.linenumber + ": unary expressions using the NOT operator should have type BOOLEAN");
                
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            overallType = new BaseType(TypeKind.BOOLEAN, expr.posn);
		} 
		
		
		
		return overallType;
	}

	@Override
	public TypeDenoter visitBinaryExpr(BinaryExpr expr, Object arg) {
		
		TypeDenoter left = expr.left.visit(this, null);
		TypeDenoter right = expr.right.visit(this, null);
	
		if (expr.operator.kind == TokenKind.AND || expr.operator.kind == TokenKind.OR) {
			
			if (!typeKindEquals(left, TypeKind.BOOLEAN) || !typeKindEquals(right, TypeKind.BOOLEAN)) {
                errorreporter.reportError(
                        "*** line " + expr.left.posn.linenumber + ": with the operator AND or OR, the two sides should be boolean");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
			
		} else if (expr.operator.kind == TokenKind.NOTEQ || expr.operator.kind == TokenKind.EQUALS ) {
			
			if (!typesEq(left, right)) {
				
				errorreporter.reportError("*** line " + expr.posn.linenumber + ": types between operator != or == have to match");
				
				return new BaseType(TypeKind.ERROR, expr.posn);
				
			}
			
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
			
		} else if (expr.operator.kind == TokenKind.GREATER || expr.operator.kind == TokenKind.LESS || expr.operator.kind == TokenKind.GREATOREQ || expr.operator.kind == TokenKind.LESS || expr.operator.kind == TokenKind.LESSOREQ) {
			
			if (!typeKindEquals(left, TypeKind.INT) || !typeKindEquals(right, TypeKind.INT) ) {
                errorreporter.reportError("*** line " + expr.left.posn.linenumber + ": types between inequality operators have to be INT");
                
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
			
			return new BaseType(TypeKind.BOOLEAN, expr.posn);
		} else if (expr.operator.kind == TokenKind.PLUS || expr.operator.kind == TokenKind.MINUS || expr.operator.kind == TokenKind.TIMES || expr.operator.kind == TokenKind.DIVIDE) {
			
			if (!typeKindEquals(left, TypeKind.INT) || !typeKindEquals(right, TypeKind.INT) ) {
                errorreporter.reportError("*** line " + expr.left.posn.linenumber + ": types between arithmetic operators have to be INT");
                
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
			
			return new BaseType(TypeKind.INT, expr.posn);
			
		}
		return null;
		
	}

	@Override
	public TypeDenoter visitRefExpr(RefExpr expr, Object arg) {
		
		return expr.ref.visit(this, null);
	}

	@Override
	public TypeDenoter visitIxExpr(IxExpr expr, Object arg) {
		
		if (!(expr.ref.decl.type instanceof ArrayType)) {
			
			
			errorreporter.reportError("*** line " + expr.ixExpr.posn.linenumber + ": Operand on column " + "attempted reference on something that isn't an array);");
			return new BaseType(TypeKind.ERROR, expr.posn);
		} 
		
		TypeDenoter exprtype = ((ArrayType) expr.ref.decl.visit(this, null)).eltType;
		TypeDenoter ixexpr = expr.ixExpr.visit(this, null);
		if (!typeKindEquals(ixexpr, TypeKind.INT)) {
			
			errorreporter.reportError("*** line " + expr.ixExpr.posn.linenumber + ": Operand on column " + expr.ixExpr.posn.offset
                +  " is not of type INT.");
			
			return new BaseType(TypeKind.ERROR, expr.posn);
			
		}
		return exprtype;
	}

	@Override
	public TypeDenoter visitCallExpr(CallExpr expr, Object arg) {
		
		if (!(expr.functionRef.decl instanceof MethodDecl)) {
            errorreporter.reportError("*** line " + expr.posn.linenumber + ": function call does not exist");
            return new BaseType(TypeKind.ERROR, expr.posn);
        }

        ParameterDeclList trueParams = ((MethodDecl) expr.functionRef.decl).parameterDeclList;
        
		if (!(expr.functionRef.decl instanceof MethodDecl)) {
			
            errorreporter.reportError("*** line " + expr.posn.linenumber + ": function has not been declared");
            
            return new BaseType(TypeKind.ERROR, expr.posn);
        }
		
		if (trueParams.size() != expr.argList.size()) {
            errorreporter.reportError("*** line " + expr.posn.linenumber + ": declared number of parameters is not equal to number of parameters used in the call");
            
            return new BaseType(TypeKind.ERROR, expr.posn);
        }

        for (int i = 0; i < expr.argList.size(); i++) {
        	
            TypeDenoter trueType = trueParams.get(i).type;
            
            TypeDenoter observedType = expr.argList.get(i).visit(this, null);
            
            if (!typesEq(trueType, observedType)) {
            	
                errorreporter.reportError("*** line " + expr.posn.linenumber + ": some of those parameter types don't match the argument types");
                
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
        }
        
        TypeDenoter trueReturn = expr.functionRef.visit(this, null);
        return trueReturn;
	}

	@Override
	public TypeDenoter visitLiteralExpr(LiteralExpr expr, Object arg) {
		return expr.lit.visit(this, null);
	}

	@Override
	public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		return expr.classtype;
	}

	@Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		if (!typeKindEquals(expr.sizeExpr.visit(this, null), TypeKind.INT)) {
			
            errorreporter.reportError("*** line " + expr.sizeExpr.posn.linenumber + ": when initializing an array, you need an INT to determine the size");
            
            return new BaseType(TypeKind.ERROR, expr.sizeExpr.posn);
        }

        return new ArrayType(expr.eltType, expr.posn);
	}

	@Override
	public TypeDenoter visitThisRef(ThisRef ref, Object arg) {
		
	
		
		
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitIdRef(IdRef ref, Object arg) {
		
		
		
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitQRef(QualRef ref, Object arg) {
		TypeDenoter refType = ref.ref.visit(this, null);
		
		if (ref.ref instanceof ThisRef) {
			return ref.id.visit(this, null);
		}
		
		if (!(refType instanceof ClassType)) {
			
			if (!(typeKindEquals(refType, TypeKind.ARRAY) && ref.id.spelling.equals("length")) ) {
				
				errorreporter.reportError("*** line " + ref.posn.linenumber + ": reference for that class is not an object");
				return new BaseType(TypeKind.ERROR, ref.posn);
				
				
			}
			
			
			
		}
		
		
//		System.out.println("hello there" + refType.typeKind);
//		System.out.println(((ClassType )refType).className.spelling);
//		System.out.println(((IdRef)ref.ref).id.spelling);
//		System.out.println(refType);
		
//		String x = ((ClassType )refType).className.spelling;
//		System.out.println("Wtf" + identifier.table.methods.get(x).get(ref.id.spelling).type);
		
//		if (ref.id instanceof Identifier) {
//			
//			return identifier.table.methods.get(x).get(ref.id.spelling).type;
//			
//		}
		
        return ref.id.visit(this, null);
	}

	@Override
	public TypeDenoter visitIdentifier(Identifier id, Object arg) {
		return id.decl.type;
	}

	@Override
	public TypeDenoter visitOperator(Operator op, Object arg) {
		return new BaseType(TypeKind.UNSUPPORTED, op.posn);
	}

	@Override
	public TypeDenoter visitIntLiteral(IntLiteral num, Object arg) {
		return new BaseType(TypeKind.INT, num.posn);
	}

	@Override
	public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		return new BaseType(TypeKind.BOOLEAN, bool.posn);
	}

	@Override
	public TypeDenoter visitNullLiteral(NullLiteral nul, Object arg) {
		return new BaseType(TypeKind.NULL, nul.posn);
	}
	
	

}
