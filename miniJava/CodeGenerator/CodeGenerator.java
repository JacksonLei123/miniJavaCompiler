package miniJava.CodeGenerator;

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
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.SyntacticAnalyzer.TokenKind;

import java.util.ArrayList;

import mJAM.Disassembler;
import mJAM.Machine;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.Machine.Reg;

public class CodeGenerator implements Visitor<Object, Object> {
	
	public ErrorReporter reporter;
	public int LBoffset;
	public int SBoffset;
	public int staticFieldsCount;
	public int OBoffset;
	public int mainaddress;
	public int currentmethodparamnum;
	
	public int popcount;
	
	public ArrayList<Integer> patcharray1;
	public ArrayList<Declaration> patcharray2;
	
	public CodeGenerator(ErrorReporter reporter) {
		
		this.reporter = reporter;
		this.patcharray1 = new ArrayList<Integer>();
		this.patcharray2 = new ArrayList<Declaration>();
		Machine.initCodeGen();
		System.out.println("Generating code");
		
	}
	
	public Object generate(AST ast) {
		
		System.out.println("STARTING CODE GENERATION");
		
		Machine.emit(Op.LOADL,0);            // array length 0
		
		Machine.emit(Prim.newarr);           // empty String array argument
		
		int patchAddr_Call_main = Machine.nextInstrAddr();  // record instr addr where main is called     
		
		Machine.emit(Op.CALL,Reg.CB,-1);     // static call main (address to be patched)
		
//		Machine.emit(Op.LOADL, 1);
//		Machine.emit(Op.STORE, Reg.LB, 3);
		
		Machine.emit(Op.HALT,0,0,0);         // end execution
		
		ast.visit(this, null);
		
		
		
		Machine.patch(patchAddr_Call_main, mainaddress);
		
		
		
		
		return null;
	   
		
	}

	@Override
	public Object visitPackage(Package prog, Object arg) {
		
		staticFieldsCount = 0;
    	for (ClassDecl classes: prog.classDeclList) {
    		
    		int x = 0;
    		for (FieldDecl fielddecl : classes.fieldDeclList) {
    			
    			if (fielddecl.isStatic) {
    				staticFieldsCount++;
    				fielddecl.description = new EntityDescription(SBoffset, Reg.SB);
    				SBoffset++;
    				
    			} else {
    				fielddecl.description = new EntityDescription(x, Reg.OB);
    				x++;
    				OBoffset++;
    			}
    		}
    		
    		classes.description = new EntityDescription(x, Reg.SB);
    	
    	}
    	
    	Machine.patch(0, staticFieldsCount);
	
        ClassDeclList cl = prog.classDeclList;
        
        boolean mainexists = false;
        int nummains = 0;
        for (ClassDecl c: prog.classDeclList) {
        	
        	for (MethodDecl m: c.methodDeclList) {
        		
        		if (m.name.equals("main")) {
        			
        			nummains++;
        			
        			if (!m.isPrivate) {
        				
        				if (m.isStatic) {
        					
        					if (m.type.typeKind == TypeKind.VOID) {
        						
        						if (m.parameterDeclList.size() == 1) {
        							
        							if (m.parameterDeclList.get(0).type instanceof ArrayType) {
        								
        								ArrayType a = (ArrayType) m.parameterDeclList.get(0).type;
        								
        								if (a.eltType instanceof ClassType) {
        									
        									ClassType classtype =(ClassType) a.eltType;
        									if (classtype.className.spelling.equals("String")) {
        										
        										mainexists = true;
        									}
        								}
        								
        							}
        						}
        					}
        					
        				}
        			}
        		}
        			
        		
        	}
        	
        	
        }
        
        for (ClassDecl classDecl: prog.classDeclList) {
    		
    		for (MethodDecl m: classDecl.methodDeclList) {
    			
    			boolean returnstmtexists = false;
    			int numreturns = 0;
    			
    			for (Statement stmt: m.statementList) {
    				
    				if (stmt instanceof ReturnStmt) {
    					
    					returnstmtexists = true;
    					numreturns++;
    				}
    			}
    			
    			if (numreturns > 1) {
    				
    				this.reporter.reportError("too many returns");
    				System.exit(4);
    			}
    			
    			if (!returnstmtexists) {
    				
    				if (m.type.typeKind != TypeKind.VOID) {
    					
    					reporter.reportError("non void method does not have a return statement");
    					System.exit(4);
    				} else {
    					
    					m.statementList.add(new ReturnStmt(null, m.posn));
    					
    				}
    				
    			}
    			
    		}
    		
    		
    	}
  
    	
    	if (!mainexists || nummains != 1) {
    		
    		reporter.reportError("too many main methods or the main isn't right");
    		System.exit(4);
    	}
    	
    	for (ClassDecl classdecls: prog.classDeclList){
        	
            classdecls.visit(this, null);
        }
    	
    	for (int i = 0; i < patcharray1.size(); i++) {
    		
    		Machine.patch(patcharray1.get(i), patcharray2.get(i).description.offset);
    	}
    	
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, Object arg) {
		
		for (FieldDecl f : cd.fieldDeclList) {
			
			
			f.visit(this, null);
			
			
		}
			
		for (MethodDecl m : cd.methodDeclList) {
			
			m.visit(this, null);
			
		}
		
		return null;
	
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		
		fd.type.visit(this, null);
		
//		if (fd.isStatic) {
//			Machine.emit(Op.PUSH, 1);
//			SBoffset++;
//			fd.description = new EntityDescription(SBoffset, Reg.SB);
//			
//		} else {
//			
//			Machine.emit(Op.PUSH, 1);
//			OBoffset++;
//			fd.description = new EntityDescription(OBoffset, Reg.OB);
//			
//		}
		
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) {
		
		LBoffset = 3;
		
		if (md.name.equals("main")) {
			
			mainaddress = Machine.nextInstrAddr();
		}
		
    	md.type.visit(this, null);
    
        ParameterDeclList pdl = md.parameterDeclList;
        currentmethodparamnum = md.parameterDeclList.size();
        int x = 0 - currentmethodparamnum;
  //      int x = - 1;
        for (ParameterDecl pd: pdl) {
            pd.visit(this, null);
           
            pd.description = new EntityDescription(x, Reg.LB);
            x++;
        }
      
        md.description = new EntityDescription(Machine.nextInstrAddr(), Reg.CB);
        
        StatementList sl = md.statementList;
 
        for (Statement s: sl) {
            s.visit(this,null);
        }
        return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) {
		
//		Machine.emit(Op.PUSH, 1);
//		LBoffset++;
		
		
		pd.type.visit(this, null);
	
		
//		if (pd.type instanceof ClassType) {
//			
//			Machine.emit(Op.PUSH, 1);
//			
//			LBoffset++;
//			pd.description = new EntityDescription(LBoffset, Reg.LB);
//			
//			Machine.emit(Op.LOADL,-1);		     // -1 on stack (= no class descriptor)
//			Machine.emit(Op.LOADL, 1);			 //  1 on stack (# of fields in class "Counter")
//			Machine.emit(Prim.newobj);
//			
//			
//		} else if (pd.type instanceof BaseType) {
//			
//			Machine.emit(Op.PUSH, 1);
//			
//			LBoffset++;
//			pd.description = new EntityDescription(LBoffset, Reg.LB);
//			
//			
//		} else if (pd.type instanceof ArrayType) {
//			
//			Machine.emit(Op.PUSH, 1);
//			
//			LBoffset++;
//			
//			Machine.emit(Prim.newarr); 
//			pd.description = new EntityDescription(LBoffset, Reg.LB);
//			
//		}
		
		return null;
		
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) {
		
		Machine.emit(Op.PUSH, 1);
	//	LBoffset++;
		
		decl.description = new EntityDescription(LBoffset, Reg.LB);
		LBoffset++;
		
		decl.type.visit(this, null);
		
//		if (decl.type instanceof ClassType) {
//			
//			Machine.emit(Op.PUSH, 1);
//			
//			LBoffset++;
//			decl.description = new EntityDescription(LBoffset, Reg.LB);
//			
//			Machine.emit(Op.LOADL,-1);		     // -1 on stack (= no class descriptor)
//			Machine.emit(Op.LOADL, 1);			 //  1 on stack (# of fields in class "Counter")
//			Machine.emit(Prim.newobj);
//			
//			
//		} else if (decl.type instanceof BaseType) {
//			
//			Machine.emit(Op.PUSH, 1);
//			
//			LBoffset++;
//			decl.description = new EntityDescription(LBoffset, Reg.LB);
//			
//			
//		} else if (decl.type instanceof ArrayType) {
//			
//			Machine.emit(Op.PUSH, 1);
//			
//			LBoffset++;
//			
//			Machine.emit(Prim.newarr); 
//			decl.description = new EntityDescription(LBoffset, Reg.LB);
//			
//		}
		
		return null;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		
		this.popcount = 0;
		
		StatementList sl = stmt.sl;
    
        for (Statement s: sl) {
        	s.visit(this, null);
        }
        
        if (this.popcount > 0) {
        	this.LBoffset = this.LBoffset - this.popcount;
        	Machine.emit(Op.POP, this.popcount);
        }
        
        return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		
		
		this.popcount ++;
		
		stmt.varDecl.visit(this, null);
		
		stmt.initExp.visit(this, null);
		
		Machine.emit(Op.STORE, Reg.LB, stmt.varDecl.description.offset);
		
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		
	//	stmt.ref.visit(this, null);
		
		if (stmt.ref.decl instanceof FieldDecl) {
			FieldDecl field = (FieldDecl) stmt.ref.decl;
			if (field.isStatic) {
				stmt.val.visit(this, null);
				Machine.emit(Op.STORE, Reg.SB, stmt.ref.decl.description.offset);
				return null;
			}
		}
        
		if (stmt.ref instanceof IdRef) {
			IdRef idRef = (IdRef) stmt.ref;
			
			if (idRef.decl instanceof FieldDecl) {
				Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
				Machine.emit(Op.LOADL, idRef.id.decl.description.offset);
				stmt.val.visit(this, null);
				Machine.emit(Prim.fieldupd);
			} else {
				
				stmt.val.visit(this, null);
				
				if (idRef.id.decl.classStaticaccess) {
					
					Machine.emit(Op.STORE, Reg.SB, stmt.ref.decl.description.offset);
				} else {
					Machine.emit(Op.STORE, Reg.LB, stmt.ref.decl.description.offset);
				}
				
				
	//			storeIdRef((IdRef) stmt.ref);
			}
        
		} else if (stmt.ref instanceof QualRef) {
			
			QualRef stmtref = (QualRef) stmt.ref;
//			stmt.ref.visit(this, null);
			
			if (stmtref.id.decl.description != null) {
				
				ArrayList<Integer> fields = new ArrayList<Integer>();
				
				fields.add(stmtref.id.decl.description.offset);
				
				while (stmtref.ref instanceof QualRef) {
					
					stmtref = (QualRef) stmtref.ref;
					fields.add(stmtref.decl.description.offset);
				}
				
				stmtref.ref.visit(this, null);
				
				for (int i = fields.size() - 1; i >= 0; i--) {
					
					Machine.emit(Op.LOADL, fields.get(i));
					
					if (i > 0) {
						Machine.emit(Prim.fieldref);
					}
				}
			
			stmt.val.visit(this, null);
	
			Machine.emit(Prim.fieldupd);
		} 
        
		}
   
        return null;
	}

	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, Object arg) {
		
		stmt.ref.visit(this, null);
		
        stmt.ix.visit(this, null);
        
        stmt.exp.visit(this, null);
        
        Machine.emit(Prim.arrayupd);
        
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) {
		
		for (Expression e: stmt.argList) {
            e.visit(this, null);
        }
		
		if (((MethodDecl)stmt.methodRef.decl).isStatic) {
			
			int calladdress = Machine.nextInstrAddr();
			
			Machine.emit(Op.CALL, Machine.Reg.CB, 0);
			
			patcharray1.add(calladdress);
			patcharray2.add(stmt.methodRef.decl);
			
		} else {
			
			if (stmt.methodRef instanceof QualRef) {
				
				QualRef x = (QualRef) stmt.methodRef;
				
				if (x.decl.printornot) {
					
					Machine.emit(Prim.putintnl);
					
					return null;
					
				} else {
					
					x.ref.visit(this, null);
					
				} 
				
			} else {
				
				Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
			}
			
			int calladdress = Machine.nextInstrAddr();
			
			Machine.emit(Op.CALLI, Machine.Reg.CB, -1);
			
			patcharray1.add(calladdress);
		
			patcharray2.add(stmt.methodRef.decl);
			
		}
		
		
		
			
		
		
		if (stmt.methodRef.decl.type.typeKind != TypeKind.VOID) {
			Machine.emit(Op.POP, 1);
		}
		
        return null;
		
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
		
		if (stmt.returnExpr != null) {
			stmt.returnExpr.visit(this, null);
			
		}
			
		
		if (stmt.returnExpr == null) {
			Machine.emit(Op.RETURN, 0, 0, currentmethodparamnum);
		} else {
			
			Machine.emit(Op.RETURN, 1, 0, currentmethodparamnum);
		}
		
		return null;
		
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) {
		
		stmt.cond.visit(this, null);

		int x = Machine.nextInstrAddr();
		
		Machine.emit(Op.JUMPIF, 0, Machine.Reg.CB, 0);
		
		stmt.thenStmt.visit(this, null);
		
		int y = Machine.nextInstrAddr();
		
		Machine.emit(Op.JUMP, Machine.Reg.CB, 0);
		
		Machine.patch(x, Machine.nextInstrAddr());
		

		if (stmt.elseStmt != null) {
			stmt.elseStmt.visit(this, null);
		}

		Machine.patch(y, Machine.nextInstrAddr());
		
		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
		
		int x = Machine.nextInstrAddr();
		
		stmt.cond.visit(this, null);
		
		int y = Machine.nextInstrAddr();
		
		Machine.emit(Op.JUMPIF, 0, Reg.CB, 0);
		
        stmt.body.visit(this, null);
        
        Machine.emit(Op.JUMP, Reg.CB, x);
        
        int z = Machine.nextInstrAddr();
        
        Machine.patch(y, z);
        
        return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		
		if (expr.operator.kind == TokenKind.MINUS) {
		
			Machine.emit(Op.LOADL, 0);
		}
	        
        expr.expr.visit(this, null);
        
        expr.operator.visit(this, null);
        
        return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		
		if (expr.operator.kind == TokenKind.OR) {
			
			expr.left.visit(this, null);
			
			Machine.emit(Op.LOAD, Machine.Reg.ST, -1);
			
			int skip = Machine.nextInstrAddr();
			
			Machine.emit(Op.JUMPIF, 1, Machine.Reg.CB, 0);
			
			expr.right.visit(this, null);
			
			expr.operator.visit(this, null);
			
			Machine.patch(skip, Machine.nextInstrAddr());
			
		} else if (expr.operator.kind == TokenKind.AND) {
			
			expr.left.visit(this, null);
			
			Machine.emit(Op.LOAD, Machine.Reg.ST, -1);
			
			int skip = Machine.nextInstrAddr();
			
			Machine.emit(Op.JUMPIF, 0, Machine.Reg.CB, 0);
			
			expr.right.visit(this, null);
			
			expr.operator.visit(this, null);
			
			Machine.patch(skip, Machine.nextInstrAddr());
		} else {
			
			expr.left.visit(this, null);
	        expr.right.visit(this, null);
	        expr.operator.visit(this, null);
			
		}
		
    
        
        
        
        return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) {
			
		if (expr.ref.decl.classStaticaccess) {
			
			Machine.emit(Op.LOAD, Machine.Reg.SB, expr.ref.decl.description.offset);
		} else if (expr.ref instanceof ThisRef) {
			
			Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
		} else {
			
			expr.ref.visit(this, null);
			
		}
		
		
	
		return null;
	}

	@Override
	public Object visitIxExpr(IxExpr expr, Object arg) {
		
		 expr.ref.visit(this, null);
	     expr.ixExpr.visit(this, null);
	     Machine.emit(Prim.arrayref);
	     
	     return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) {
		
		for (Expression e: expr.argList) {
            e.visit(this, null);
        }
		
		if (((MethodDecl)expr.functionRef.decl).isStatic) {
			
			int calladdress = Machine.nextInstrAddr();
			
			Machine.emit(Op.CALL, Machine.Reg.CB, 0);
			
			patcharray1.add(calladdress);
			patcharray2.add(expr.functionRef.decl);
		} else {
			
			if (expr.functionRef instanceof QualRef) {
				
				QualRef x = (QualRef) expr.functionRef;
				
				if (expr.functionRef.decl.printornot) {
					
					Machine.emit(Prim.putintnl);
					
					return null;
					
				} else {
					
					x.ref.visit(this, null);
					
				} 
				
			} else {
				
				Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
			}
			
			int calladdress = Machine.nextInstrAddr();
			
			Machine.emit(Op.CALLI, Machine.Reg.CB, 0);
			patcharray1.add(calladdress);
			patcharray2.add(expr.functionRef.decl);
			
		}
		
		
		
			
		
		
		
		return null;
		
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		
		expr.lit.visit(this, null);
        return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		
		Machine.emit(Op.LOADL,-1);		     // -1 on stack (= no class descriptor)
	
		Machine.emit(Op.LOADL, expr.classtype.className.decl.description.offset);			 //  1 on stack (# of fields in class "Counter")
		Machine.emit(Prim.newobj);
		
		expr.classtype.visit(this, null);
        return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		
		expr.sizeExpr.visit(this, null);
		Machine.emit(Prim.newarr); 
		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) {
		
		Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
		
		return null;
	}

	@Override
	public Object visitIdRef(IdRef ref, Object arg) {
		
		ref.id.visit(this, null);
	
		if (ref.decl instanceof FieldDecl) {
			
			if (((FieldDecl)ref.decl).isStatic) {
				
				Machine.emit(Op.LOAD, Machine.Reg.SB, ref.id.decl.description.offset);
				
			} else {
				
				Machine.emit(Op.LOAD, Machine.Reg.OB, ref.id.decl.description.offset);
		
			}
		} else {
			
			if (ref.decl.classStaticaccess) {
					
				Machine.emit(Op.LOAD, Machine.Reg.SB, ref.id.decl.description.offset);
			} else {
				Machine.emit(Op.LOAD, Machine.Reg.LB, ref.id.decl.description.offset);
				
			}
		
			
			
		}
		
		return null;
	}

	@Override
	public Object visitQRef(QualRef ref, Object arg) {
		
		if (ref.id.spelling.equals("length")) {
			
			IdRef refid = (IdRef) ref.ref;
			
			refid.id.visit(this, null);
			
			if (refid.decl instanceof FieldDecl) {
				
				if (((FieldDecl)refid.decl).isStatic) {
					
					Machine.emit(Op.LOAD, Machine.Reg.SB, refid.id.decl.description.offset);
					
				} else {
					
					Machine.emit(Op.LOAD, Machine.Reg.OB, refid.id.decl.description.offset);
			
				}
			} else {
				
				if (refid.decl.classStaticaccess) {
						
					Machine.emit(Op.LOAD, Machine.Reg.SB, refid.id.decl.description.offset);
				} else {
					Machine.emit(Op.LOAD, Machine.Reg.LB, refid.id.decl.description.offset);
					
				}
			
				
				
			}
			
//			Machine.emit(Op.LOAD, Machine.Reg.LB, refid.id.decl.description.offset);
			
			Machine.emit(Prim.arraylen);
			return null;
			
		}
			
		
//		Machine.emit(Op.LOAD, Machine.Reg.LB, ref.ref.decl.description.offset);
		
//		if (ref.id.decl.description != null) {
//			Machine.emit(Op.LOADL, ref.id.decl.description.offset);
//		}
		
		
		if (ref.id.decl.description != null) {
			
			ArrayList<Integer> fields = new ArrayList<Integer>();
			
			fields.add(ref.id.decl.description.offset);
			
			while (ref.ref instanceof QualRef) {
				
				ref = (QualRef) ref.ref;
				fields.add(ref.decl.description.offset);
			}
			
			ref.ref.visit(this, null);
			
			for (int i = fields.size() - 1; i >= 0; i--) {
				
				Machine.emit(Op.LOADL, fields.get(i));
				
				if (i > 0) {
					Machine.emit(Prim.fieldref);
				}
			}
			
			Machine.emit(Prim.fieldref);
			
		}
		
		
		
		
		
		
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitOperator(Operator op, Object arg) {
		
		if (op.kind == TokenKind.LESS) {
			Machine.emit(Prim.lt);
			
		} else if (op.kind == TokenKind.GREATER) {
			Machine.emit(Prim.gt);
			
		} else if (op.kind == TokenKind.EQUALS) {
			Machine.emit(Prim.eq);
			
		} else if (op.kind == TokenKind.LESSOREQ) {
			
			Machine.emit(Prim.le);
		} else if (op.kind == TokenKind.GREATOREQ) {
			
			Machine.emit(Prim.ge);
			
		} else if (op.kind == TokenKind.NOT) {
			
			Machine.emit(Prim.not);
			
		} else if (op.kind == TokenKind.NOTEQ) {
			
			Machine.emit(Prim.ne);
		} else if (op.kind == TokenKind.AND) {
			
			Machine.emit(Prim.and);
		} else if (op.kind == TokenKind.OR) {
			
			Machine.emit(Prim.or);
		} else if (op.kind == TokenKind.PLUS) {
			
			Machine.emit(Prim.add);
		} else if (op.kind == TokenKind.MINUS) {
			
			Machine.emit(Prim.sub);
		} else if (op.kind == TokenKind.TIMES) {
			
			Machine.emit(Prim.mult);
		} else if (op.kind == TokenKind.DIVIDE) {
			
			Machine.emit(Prim.div);
		} else {
			
			reporter.reportError("unknown operator: " + op.kind + " during code generation");
		}
		
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) {
		
		int intValue = Integer.parseInt(num.spelling);
		Machine.emit(Op.LOADL, intValue);
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		
		if (bool.spelling.equals("true")) {
			Machine.emit(Op.LOADL, Machine.trueRep);
		} else if (bool.spelling.equals("false")) {
			Machine.emit(Op.LOADL, Machine.falseRep);
		}
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nul, Object arg) {
		
		Machine.emit(Op.LOADL, Machine.nullRep);
		return null;
	}

}
