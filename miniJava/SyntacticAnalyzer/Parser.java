package miniJava.SyntacticAnalyzer;

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
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.SyntacticAnalyzer.Parser.SyntaxError;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.Terminal;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class Parser {
	
	/* 
	 * Remember to account for comments
	 * check for infinite loops
	 * see if certain things are still accepted because of the grammar ex: if (+4)
	 * ask about single AND operator
	 */
	
	private Scanner scanner;
	private ErrorReporter reporter;
	private Token token;
	private boolean trace = true;

	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.scanner = scanner;
		this.reporter = reporter;
	}
	
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;	
	}
	
	public Package parse() {
		token = scanner.scan();
		try {
			return parseProgram();
		}
		catch (SyntaxError e) { }
		return null;
	}
	
	public Package parseProgram() throws SyntaxError {
		
		
		
		ClassDeclList classes = new ClassDeclList();
		while (token.kind != TokenKind.EOT) {
		
			classes.add(parseClassDeclaration());

		}
		
		Package packageobject = new Package(classes, this.scanner.sourceposition.copy());
		
		accept(TokenKind.EOT);
		return packageobject;
	}
	
	private ClassDecl parseClassDeclaration() throws SyntaxError {
		
		MethodDeclList methoddecs = new MethodDeclList();
		FieldDeclList fielddecs = new FieldDeclList();
		
		accept(TokenKind.CLASS);
		
		String cn = token.spelling;
		
		Identifier id = new Identifier((token));
		
	

		accept(TokenKind.ID);
		
		accept(TokenKind.LCURBRACK);
		
		
		while (token.kind != TokenKind.RCURBRACK) {
			
			boolean isPrivate = false;
			boolean isStatic = false;

			if (token.kind == TokenKind.RCURBRACK) {
				break;
			}
			if (token.kind == TokenKind.PUBLIC || token.kind == TokenKind.PRIVATE) {
				
				if (token.kind == TokenKind.PRIVATE) {
					isPrivate = true;
				}
				
				
				acceptIt();
			}
			
			System.out.println(token.spelling);
			
			if (token.kind == TokenKind.STATIC) {
				
				isStatic = true;
				acceptIt();
			}

			if (token.kind == TokenKind.VOID) {
				
				TypeDenoter type = new BaseType(TypeKind.VOID, this.scanner.sourceposition.copy());
				StatementList statements = new StatementList();
				ParameterDeclList parameters = new ParameterDeclList();
				
				acceptIt();
				
				String method_name = token.spelling;
				
				FieldDecl field = new FieldDecl(isPrivate, isStatic, type, method_name, this.scanner.sourceposition.copy());
				
				accept(TokenKind.ID);
//				parseMethodDeclaration();
				
				accept(TokenKind.LPAREN);
				
				if (token.kind != TokenKind.RPAREN) {
					parameters = parseParameterList();
				} 
				accept(TokenKind.RPAREN);
				
				accept(TokenKind.LCURBRACK);
				
				while (token.kind != TokenKind.RCURBRACK) {
				//	statements = parseStatement();
					statements.add(parseStatement());
				}
				
				accept(TokenKind.RCURBRACK);
				
				MethodDecl methoddecl = new MethodDecl(field, parameters, statements, this.scanner.sourceposition.copy());
				methoddecs.add(methoddecl);
				
			} else {
				
				StatementList statements = new StatementList();
				ParameterDeclList parameters = new ParameterDeclList();

				TypeDenoter type = parseType();
				
				String name = token.spelling;
				accept(TokenKind.ID);
				
//				if (token.kind == TokenKind.ASSIGN) {
//					acceptIt();
//					parseExpression();
//					
//					accept(TokenKind.SEMICOLON);
//				} else 
				
				System.out.println("private " + isPrivate);
				System.out.println("static " + isStatic );
				
				FieldDecl field = new FieldDecl(isPrivate, isStatic, type, name, this.scanner.sourceposition.copy());
					
				if (token.kind == TokenKind.SEMICOLON) {
					acceptIt();
					fielddecs.add(field);
				} else {
//					parseMethodDeclaration();
			
					accept(TokenKind.LPAREN);
					
					if (token.kind != TokenKind.RPAREN) {
						parameters = parseParameterList();
					} 
					accept(TokenKind.RPAREN);
					
					accept(TokenKind.LCURBRACK);
					
					while (token.kind != TokenKind.RCURBRACK) {
						statements.add(parseStatement());
					}
					
					accept(TokenKind.RCURBRACK);
					
					methoddecs.add(new MethodDecl(field, parameters, statements, this.scanner.sourceposition.copy()));
				}
			}
			
			
			
			
			
			
		}
		
		accept(TokenKind.RCURBRACK);
		
		
		
		

		ClassDecl result = new ClassDecl(cn, fielddecs, methoddecs, this.scanner.sourceposition.copy());
		

		result.type = new ClassType(id, this.scanner.sourceposition.copy());
		return result;
		
	}
	
	private Statement parseStatement() throws SyntaxError {
 
		if (token.kind == TokenKind.LCURBRACK) {
			acceptIt();
			
			StatementList statementlist = new StatementList();
			while (token.kind != TokenKind.RCURBRACK) {
				statementlist.add(parseStatement());
			}

			accept(TokenKind.RCURBRACK);
			BlockStmt block = new BlockStmt(statementlist, this.scanner.sourceposition.copy());
			return block;
			
		} else if (token.kind == TokenKind.INT || token.kind == TokenKind.BOOLEAN) {
			
			TypeDenoter type = parseType();
			String identifier = token.spelling;
			accept(TokenKind.ID);
			VarDecl declaration = new VarDecl(type, identifier, this.scanner.sourceposition.copy());
			accept(TokenKind.ASSIGN);
			Expression expression = parseExp();
//			Expression expression = parseExpression();
			System.out.println("1");
			
			accept(TokenKind.SEMICOLON);
			
			VarDeclStmt statement = new VarDeclStmt(declaration, expression, this.scanner.sourceposition.copy());
			
			return statement;
			
			
		} else if (token.kind == TokenKind.ID) {
			System.out.println("HELLEO");
			Identifier id = new Identifier(token);
			Reference idref = new IdRef(id, this.scanner.sourceposition.copy());
			acceptIt();
			if (token.kind == TokenKind.LPAREN) {
				acceptIt();
				ExprList arguments = new ExprList();
				if (token.kind != TokenKind.RPAREN) {
					arguments = parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOLON);
				return new CallStmt(idref, arguments, this.scanner.sourceposition.copy());
				
			}
			
			if (token.kind == TokenKind.LSQUBRACK) {
				
				acceptIt();
				
				if (token.kind != TokenKind.RSQUBRACK) {
					Expression exp1 = parseExp();
//					Expression exp1 = parseExpression();
					accept(TokenKind.RSQUBRACK);
					accept(TokenKind.ASSIGN);
					Expression exp2 = parseExp();
//					Expression exp2 = parseExpression();
					accept(TokenKind.SEMICOLON);
					return new IxAssignStmt(idref, exp1, exp2, this.scanner.sourceposition.copy());
				} else {
					
					accept(TokenKind.RSQUBRACK);
					
					ClassType classtype = new ClassType(id, this.scanner.sourceposition.copy());
					ArrayType arraytype = new ArrayType(classtype, this.scanner.sourceposition.copy());
					String identifier = token.spelling;
					accept(TokenKind.ID);
					accept(TokenKind.ASSIGN);
					Expression exp = parseExp();
//					Expression exp = parseExpression();
					accept(TokenKind.SEMICOLON);
					VarDecl vardeclaration = new VarDecl(arraytype, identifier, this.scanner.sourceposition.copy());
					
					return new VarDeclStmt(vardeclaration, exp, this.scanner.sourceposition.copy());
				}
				
			}
			
			if (token.kind == TokenKind.ID) {
				
				String identifier = token.spelling;
				ClassType classtype = new ClassType(id, this.scanner.sourceposition.copy());
				acceptIt();
			
				accept(TokenKind.ASSIGN);
				Expression exp = parseExp();
//				Expression exp = parseExpression();
				
				VarDecl var = new VarDecl(classtype, identifier, this.scanner.sourceposition.copy());

				accept(TokenKind.SEMICOLON);
				
				return new VarDeclStmt(var, exp, this.scanner.sourceposition.copy());
				
			} else {
				
				if (token.kind == TokenKind.PERIOD) {
//					acceptIt();
//					Reference nextref = parseReference2();
//					Reference qualref = new QualRef(nextref, id, this.scanner.sourceposition.copy());
					
					while (token.kind == TokenKind.PERIOD) {
						acceptIt();
						idref = new QualRef(idref, new Identifier(token), this.scanner.sourceposition.copy());
						accept(TokenKind.ID);
						
					}
					
					if (token.kind == TokenKind.ASSIGN) {
						acceptIt();
						Expression exp = parseExp();
//						Expression exp = parseExpression();
						accept(TokenKind.SEMICOLON);
						
						Statement assignstmt = new AssignStmt(idref, exp, this.scanner.sourceposition.copy());
						
						return assignstmt;
						
					} else if (token.kind == TokenKind.LSQUBRACK) {
						acceptIt();
						Expression exp = parseExp();
//						Expression exp = parseExpression();
						
						

						accept(TokenKind.RSQUBRACK);
						
						accept(TokenKind.ASSIGN);
						Expression exp2 = parseExp();
//						Expression exp2 = parseExpression();
						
						accept(TokenKind.SEMICOLON);
						
						return new IxAssignStmt(idref, exp, exp2, this.scanner.sourceposition.copy());
							
					} else if (token.kind == TokenKind.LPAREN) {
						acceptIt();
						
						ExprList arguments = new ExprList();
						if (token.kind != TokenKind.RPAREN) {
							arguments = parseArgumentList();
						}
						accept(TokenKind.RPAREN);
						accept(TokenKind.SEMICOLON);
						
						return new CallStmt(idref, arguments, this.scanner.sourceposition.copy());
					}
				}

				if (token.kind == TokenKind.ASSIGN) {
					acceptIt();
					Expression exp = parseExp();
//					Expression exp = parseExpression();
					
					accept(TokenKind.SEMICOLON);
					
					return new AssignStmt(idref, exp, this.scanner.sourceposition.copy());
					
				} else if (token.kind == TokenKind.LSQUBRACK) {
					acceptIt();
					Expression exp = parseExp();
//					Expression exp = parseExpression();

					accept(TokenKind.RSQUBRACK);
					
					accept(TokenKind.ASSIGN);
					Expression exp2 = parseExp();
//					Expression exp2 = parseExpression();
					
					accept(TokenKind.SEMICOLON);
					
					return new IxAssignStmt(idref, exp, exp2, this.scanner.sourceposition.copy());
						
				} else if (token.kind == TokenKind.LPAREN) {
					acceptIt();
					
					ExprList arguments = new ExprList();
					if (token.kind != TokenKind.RPAREN) {
						arguments = parseArgumentList();
					}
					accept(TokenKind.RPAREN);
					System.out.println("5");
					accept(TokenKind.SEMICOLON);
					
					return new CallStmt(idref, arguments, this.scanner.sourceposition.copy());
				}
				
				
			}
			
		} else if (token.kind == TokenKind.THIS) {
			
	//		Reference thisref = new ThisRef(this.scanner.sourceposition.copy());
			
			Reference thisref = parseReference();
			
			if (token.kind == TokenKind.ASSIGN) {
				acceptIt();
				Expression exp = parseExp();
//				Expression exp = parseExpression();
				System.out.println(exp);
				System.out.println("6");
				accept(TokenKind.SEMICOLON);
				return new AssignStmt(thisref, exp, this.scanner.sourceposition.copy());
				
			} else if (token.kind == TokenKind.LSQUBRACK) {
				acceptIt();
				Expression exp = parseExp();
//				Expression exp = parseExpression();

				accept(TokenKind.RSQUBRACK);
				accept(TokenKind.ASSIGN);
				Expression exp2 = parseExp();
//				Expression exp2 = parseExpression();

				accept(TokenKind.SEMICOLON);
				
				return new IxAssignStmt(thisref, exp, exp2, this.scanner.sourceposition.copy());
					
			} else if (token.kind == TokenKind.LPAREN) {
				acceptIt();
				ExprList arguments = new ExprList();
				if (token.kind != TokenKind.RPAREN) {
					arguments = parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOLON);
				return new CallStmt(thisref, arguments, this.scanner.sourceposition.copy());
			}
			
			
		} else if (token.kind == TokenKind.RETURN) {
			
			SourcePosition sp = this.scanner.sourceposition.copy();
			acceptIt();
			
			Expression returnexp = null;
			if (token.kind != TokenKind.SEMICOLON) {
				returnexp = parseExp();
//				returnexp = parseExpression();
			}
			accept(TokenKind.SEMICOLON);
			return new ReturnStmt(returnexp, sp);
			
		} else if (token.kind == TokenKind.IF) {
			SourcePosition sp = this.scanner.sourceposition.copy();
			acceptIt();
			accept(TokenKind.LPAREN);
			Expression bool = parseExp();
//			Expression bool = parseExpression();

			accept(TokenKind.RPAREN);
			Statement ifstatement = parseStatement();
			Statement elsestatement = null;
			if (token.kind == TokenKind.ELSE) {
				acceptIt();
				elsestatement = parseStatement();
			}
			
			return new IfStmt(bool, ifstatement, elsestatement, sp);
			
		} else if (token.kind == TokenKind.WHILE) {
			acceptIt();
			accept(TokenKind.LPAREN);
			Expression bool = parseExp();
			SourcePosition sp = this.scanner.sourceposition.copy();
//			Expression bool = parseExpression();
//			while (token.kind != TokenKind.RPAREN) {
//				parseExpression();
//			}
			accept(TokenKind.RPAREN);
			Statement whilestmt = parseStatement();
			return new WhileStmt(bool, whilestmt, sp);
		} else {
			parseError("unknown token " + token.spelling);
			return null;
		}
		return null;
		
		
		


	}

	
	private Expression parseExp() throws SyntaxError {
		System.out.println(this.scanner.sourceposition.copy().linenumber);
		Expression disjunct = parseDisjunct();
		return disjunct;
		
	}
	
	private Expression parseDisjunct() throws SyntaxError {
		
		Expression exp = parseConjunct();
		
		while (token.kind == TokenKind.OR) {
			Operator op = new Operator(token);
			acceptIt();
			Expression exp2 = parseConjunct();
			
			exp = new BinaryExpr(op, exp, exp2, this.scanner.sourceposition.copy());
			
		}
		
		return exp;
		
	}
	
	private Expression parseConjunct() throws SyntaxError {
		Expression exp = parseEquality();
		
		while (token.kind == TokenKind.AND) {
			Operator op = new Operator(token);
			acceptIt();
			Expression exp2 = parseEquality();
			
			exp = new BinaryExpr(op, exp, exp2, this.scanner.sourceposition.copy());
		}
		return exp;
		
		
	}
	
	private Expression parseEquality() throws SyntaxError {
		
		Expression exp = parseRelational();
		
		while (token.kind == TokenKind.EQUALS || token.kind == TokenKind.NOTEQ) {
			Operator op = new Operator(token);
			acceptIt();
			Expression exp2 = parseRelational();
			
			exp = new BinaryExpr(op, exp, exp2, this.scanner.sourceposition.copy());
		}
		return exp;
	}
	
	private Expression parseRelational() throws SyntaxError {
		
		Expression exp = parseAdditive();
		
		while (token.kind == TokenKind.GREATER || token.kind == TokenKind.GREATOREQ || token.kind ==TokenKind.LESSOREQ || token.kind == TokenKind.LESS) {
			Operator op = new Operator(token);
			acceptIt();
			Expression exp2 = parseAdditive();
			
			exp = new BinaryExpr(op, exp, exp2, this.scanner.sourceposition.copy());
		}
		
		return exp;
		
		
		
	}
	
	private Expression parseAdditive() throws SyntaxError {
		
		Expression exp = parseMult();
		
		while (token.kind == TokenKind.MINUS || token.kind == TokenKind.PLUS) {
			
			Operator op = new Operator(token);
			acceptIt();
			Expression exp2 = parseMult();
			
			exp = new BinaryExpr(op, exp, exp2, this.scanner.sourceposition.copy());
		}
		
		return exp;
	}
	
	private Expression parseMult() throws SyntaxError {
		
		Expression exp = parseUnary();
		
		while (token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE) {
			
			Operator op = new Operator(token);
			acceptIt();
			Expression exp2 = parseUnary();
			
			exp = new BinaryExpr(op, exp, exp2, this.scanner.sourceposition.copy());
		}
		
		return exp;
		
	}
	
	private Expression parseUnary() throws SyntaxError {
		
		if (token.kind == TokenKind.MINUS || token.kind == TokenKind.NOT) {
			
			Operator op = new Operator(token);
			acceptIt();
			
			Expression exp;
			if (token.kind == TokenKind.MINUS || token.kind == TokenKind.NOT) {
				exp = parseUnary();
			} else {
				exp = parseNum();
			}
			
			exp = new UnaryExpr(op, exp, this.scanner.sourceposition.copy());
			return exp;
			
		}
		
		return parseNum();
		
		
		
		
	}
	
	private Expression parseNum() throws SyntaxError {
		
		if (token.kind == TokenKind.NULL) {
			
			acceptIt();
			return new LiteralExpr(new NullLiteral(token), this.scanner.sourceposition.copy());
		}
		
		if (token.kind == TokenKind.NEW) {
			acceptIt();
			
			if (token.kind == TokenKind.INT) {
				BaseType type = new BaseType(TypeKind.INT, this.scanner.sourceposition.copy());
				acceptIt();
				accept(TokenKind.LSQUBRACK);
				NewArrayExpr newarray;
				
				if (token.kind != TokenKind.RSQUBRACK) {
					Expression expression = parseExp();
					accept(TokenKind.RSQUBRACK);
					newarray =  new NewArrayExpr(type, expression, this.scanner.sourceposition.copy());
					
				} else {
					parseError("empty statement inside brackets");
					return null;
				
				}	
				
				return newarray;
				
				
				
			} else if (token.kind == TokenKind.ID) {
				Identifier id = new Identifier(token);
				acceptIt();
				if (token.kind == TokenKind.LPAREN) {
					acceptIt();
					ClassType cl = new ClassType(id, this.scanner.sourceposition.copy());
					accept(TokenKind.RPAREN);
					
					return new NewObjectExpr(cl, this.scanner.sourceposition.copy());
					
					
				} else if (token.kind == TokenKind.LSQUBRACK) {
					
					ClassType cl = new ClassType(id, this.scanner.sourceposition.copy());
					acceptIt();
					Expression exp = parseExp();
					accept(TokenKind.RSQUBRACK);
					return new NewArrayExpr(cl, exp, this.scanner.sourceposition.copy());
				} else {
					parseError("expected [ or { but got " + token.spelling);
					return null;
				}
				
			}
			
		} else if (token.kind == TokenKind.NUM || token.kind == TokenKind.TRUE || token.kind == TokenKind.FALSE) {
			System.out.println(token.spelling);
			Terminal num;
			if (token.kind == TokenKind.NUM) {
				num = new IntLiteral(token);
			} else {
				num = new BooleanLiteral(token);
			}
			LiteralExpr literal = new LiteralExpr(num, this.scanner.sourceposition.copy());
			acceptIt();
			
			return literal;
			
		} else if (token.kind == TokenKind.LPAREN) {
			acceptIt();
			if (token.kind != TokenKind.RPAREN) {
				Expression exp = parseExp();
				accept(TokenKind.RPAREN);
				return exp;
			} else {
				parseError("could not find expression inside parentheses");
				return null;
			}
		} else if (token.kind == TokenKind.MINUS || token.kind == TokenKind.NOT) {
			acceptIt();
			Expression exp = parseExp();
			return new UnaryExpr(new Operator(token), exp, this.scanner.sourceposition.copy());
		} else if (token.kind == TokenKind.ID || token.kind == TokenKind.THIS) {
			
			Reference reference = parseReference();
			Expression expr = new RefExpr(reference, this.scanner.sourceposition.copy());
			
			if (token.kind == TokenKind.LSQUBRACK) {
				acceptIt();
				
				if (token.kind != TokenKind.RSQUBRACK ) {
					Expression exp = parseExp();
					
					expr = new IxExpr(reference, exp, this.scanner.sourceposition.copy());
					accept(TokenKind.RSQUBRACK);
					
					return expr;
				} else {
					parseError("empty expression inside square brackets");
					return null;
				}
				
			} else if (token.kind == TokenKind.LPAREN) {
				
				acceptIt();
				ExprList arguments = new ExprList();
				if (token.kind != TokenKind.RPAREN) {
					arguments = parseArgumentList();
				}
				expr = new CallExpr(reference, arguments, this.scanner.sourceposition.copy());
				accept(TokenKind.RPAREN);
				
				
				return expr;
			
			} else {
				return expr;
			}

			
		} else {
			
			parseError("expected operator but got " + token.spelling);
			return null;
		}
		return null;
		
		
		
	}
	
	
	
	
	private ExprList parseArgumentList() throws SyntaxError {
		System.out.println(this.scanner.sourceposition.copy().linenumber);
		ExprList arguments = new ExprList();
		arguments.add(parseExp());
		while (token.kind != TokenKind.RPAREN) {
			accept(TokenKind.COMMA);
			arguments.add(parseExp());
		}
		
		return arguments;
		
		
	}
		
	
	
	private ParameterDeclList parseParameterList() throws SyntaxError {
		System.out.println(this.scanner.sourceposition.copy().linenumber);
		TypeDenoter type = parseType();
		ParameterDeclList parameterlist = new ParameterDeclList();
		String name = token.spelling;
		
		ParameterDecl parameter = new ParameterDecl(type, name, this.scanner.sourceposition.copy());
		
		parameterlist.add(parameter);
		accept(TokenKind.ID);
		
		while (token.kind != TokenKind.RPAREN) {
						
			accept(TokenKind.COMMA);
			TypeDenoter type2 = parseType();
			String name2 = token.spelling;
			ParameterDecl parameter2 = new ParameterDecl(type2, name2, this.scanner.sourceposition.copy());
			
			parameterlist.add(parameter2);
			accept(TokenKind.ID);	
		}
		
		return parameterlist;
		
	}
	
	private Reference parseReference() throws SyntaxError {
		System.out.println(this.scanner.sourceposition.copy().linenumber);
		if (token.kind == TokenKind.THIS) {
			Reference ref = new ThisRef(this.scanner.sourceposition.copy());
			acceptIt();
			while (token.kind == TokenKind.PERIOD) {
				acceptIt();
				
				ref = new QualRef(ref, new Identifier(token), this.scanner.sourceposition.copy());
				accept(TokenKind.ID);
			}
			return ref;
		} else if (token.kind == TokenKind.ID) {
			Reference ref = new IdRef(new Identifier(token), this.scanner.sourceposition.copy());
			acceptIt();
			while (token.kind == TokenKind.PERIOD) {
				acceptIt();
				
				ref = new QualRef(ref, new Identifier(token), this.scanner.sourceposition.copy());
				accept(TokenKind.ID);
			}
			return ref;
		
		} else {
		
			parseError("expected reference but got " + token.spelling);
			return null;
		}
		
	}

	
	private TypeDenoter parseType() throws SyntaxError {
		
		if (token.kind == TokenKind.BOOLEAN) {
			
			acceptIt();
			
			return new BaseType(TypeKind.BOOLEAN, this.scanner.sourceposition.copy());
			
		} else if (token.kind == TokenKind.INT || token.kind == TokenKind.ID) {
			
			TypeDenoter type;
			if (token.kind == TokenKind.INT) {
				type = new BaseType(TypeKind.INT, this.scanner.sourceposition.copy());
				
			} else {
				type = new ClassType(new Identifier(token), this.scanner.sourceposition.copy());
			}
			acceptIt();
			if (token.kind != TokenKind.LSQUBRACK) {
				return type;
			}
			
			if (token.kind == TokenKind.LSQUBRACK) {
				acceptIt();
				
				if (token.kind == TokenKind.RSQUBRACK) {
					acceptIt();
					return new ArrayType(type, this.scanner.sourceposition);
				} else {
					parseError("expected right square bracket but got " + token.spelling + " " + token.posn.linenumber);
					return null;
				}
			} 
			
		} else {
			parseError("expected Type but got " + token.spelling + " " + token.posn.linenumber);
			return null;
		}
		return null;
		

	}
	
	
	private void parseError(String e) throws SyntaxError {
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}
	
	private void acceptIt() throws SyntaxError {
		accept(token.kind);
	}
	
	/**
	 * verify that current token in input matches expected token and advance to next token
	 * @param expectedToken
	 * @throws SyntaxError  if match fails
	 */
	private void accept(TokenKind expectedTokenKind) throws SyntaxError {
		if (token.kind == expectedTokenKind) {
			if (trace)
				pTrace();
			token = scanner.scan();
		}
		else
			parseError("expecting '" + expectedTokenKind +
					"' but found '" + token.kind + "'");
	}
	
	private void pTrace() {
		StackTraceElement [] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
		System.out.println();
	}
	
	

}
