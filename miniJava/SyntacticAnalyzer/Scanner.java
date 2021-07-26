/**
 *  Scan the a single line of input
 *
 *  Grammar:
 *   num ::= digit digit*
 *   digit ::= '0' | ... | '9'
 *   oper ::= '+' | '*'
 *   
 *   whitespace is the space character
 */
package miniJava.SyntacticAnalyzer;

import java.io.*;

import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.ErrorReporter;

public class Scanner {

	private InputStream inputStream;
	private ErrorReporter reporter;

	private char currentChar;
	private StringBuilder currentSpelling;
	
	public int linenumber;
	public int offset;
	public SourcePosition sourceposition;
	// true when end of line is found
	private boolean eot = false;

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;
		
		this.sourceposition = new SourcePosition(0, 0);

		// initialize scanner state
		readChar();
	}
	
	/**
	 * skip whitespace and scan next token
	 */
	public Token scan() {
		
		// skip whitespace
		while (!eot && (currentChar == ' ' || currentChar == '\n' || currentChar == '\t' || currentChar == '\r' || currentChar == '\uFEFF' || currentChar == '/')) {
			
			if (currentChar == '\n' || currentChar == '\r') {
				
				sourceposition.offset = 0;
				sourceposition.linenumber++;
				skipIt();
				continue;
				
				}

			if (currentChar == '/') {
				System.out.println(currentChar);
				
				sourceposition.offset++;
				nextChar();
				
				if (currentChar == '/') {
					sourceposition.offset++;
					nextChar();
					
					while (currentChar != '\n' && currentChar != '\r' && !eot) {
						System.out.println(currentChar);
						
						sourceposition.offset++;
						skipIt();
					}
					
					continue;
					
				} else if (currentChar == '*') {
					sourceposition.offset++;
					nextChar();
					
					while (!eot) {
						
						if (currentChar == '\n' || currentChar == '\r') {
						
						sourceposition.offset = 0;
						sourceposition.linenumber++;
						skipIt();
						if (eot) {
							return new Token(TokenKind.ERROR, "Terminated", sourceposition);
						}
						continue;
						
						}
						
						if (currentChar != '*') {
							sourceposition.offset++;
							nextChar();
							
						}
						if (currentChar == '*') {
						
							while (currentChar == '*') {
								if (currentChar == '\n' || currentChar == '\r') {
									sourceposition.offset = 0;
									sourceposition.linenumber++;
									skipIt();
									continue;
									
									}
								sourceposition.offset++;
								nextChar();
							}
							if (currentChar == '/') {
								sourceposition.offset++;
								nextChar();
								break;
							}
						}
						
						if (eot) {
							return new Token(TokenKind.ERROR, "Terminated", sourceposition);
							
						}
					}
					
					continue;
				} else {
					return new Token(TokenKind.DIVIDE, "/", sourceposition);
				}
			}
			sourceposition.offset++;
			skipIt();
		}
//		System.out.println(currentChar);
//		System.out.println("split here");
//		System.out.println(currentChar == '\n');
//		System.out.println(currentChar == '\r');
//		System.out.println(currentChar == '\uFEFF');
//		System.out.println(!eot);
//		System.out.println(currentChar == ' ');
//		System.out.println(currentChar == '\t');
		// start of a token: collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();
		String spelling = currentSpelling.toString();
		// return new token
		return new Token(kind, spelling, sourceposition);
	}

	/**
	 * determine token kind
	 */
public TokenKind scanToken() {
		
		if (eot)
		
			return(TokenKind.EOT); 

		// scan Token
		switch (currentChar) {	
	
		case '+':
			takeIt();
			return(TokenKind.PLUS);

		case '*':
			takeIt();
			return(TokenKind.TIMES);
			
		case '-':
			takeIt();
			return(TokenKind.MINUS);
			
		case '/':
			takeIt();
//			if (currentChar == '/') {
//				takeIt();
//				while (currentChar != '\n' && currentChar != '\r' && !eot) {
////					System.out.println(currentChar);
////					
//					
//					skipIt();
//				}
//				
//				return TokenKind.LINECOMMENT;
//			} else if (currentChar == '*') {
//				nextChar();
//				while (currentChar != '*' && !eot) {
//					nextChar();
//					
//					if (currentChar == '*') {
//						nextChar();
//						if (currentChar == '/') {
//							
//							nextChar();
//							System.out.println(currentChar);
//							return TokenKind.PARACOMMENT;
//							
//						}
//					}
//				}
//			}
			return(TokenKind.DIVIDE);
			
		case '=':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return(TokenKind.EQUALS);
			}
			return(TokenKind.ASSIGN);
			
		case '>':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return(TokenKind.GREATOREQ);
			}
			return(TokenKind.GREATER);
			
		case '<':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return(TokenKind.LESSOREQ);
			}
			return(TokenKind.LESS);
			
		case '!':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return(TokenKind.NOTEQ);
			}
			return(TokenKind.NOT);
			
		case '&':
			takeIt();
			if (currentChar == '&') {
				takeIt();
				return(TokenKind.AND);
			}
			return(TokenKind.ERROR);
			
		case '|':
			takeIt();
			if (currentChar == '|') {
				takeIt();
				return(TokenKind.OR);
			}
			return(TokenKind.ERROR);
			

		case '(': 
			takeIt();
			return(TokenKind.LPAREN);

		case ')':
			takeIt();
			return(TokenKind.RPAREN);
			
		case '}':
			takeIt();
			return(TokenKind.RCURBRACK);
			
		case '{':
			takeIt();
			return(TokenKind.LCURBRACK);
			
		case ']':
			takeIt();
			return(TokenKind.RSQUBRACK);
			
		case '[':
			takeIt();
			return(TokenKind.LSQUBRACK);
			
		case ',':
			takeIt();
			return(TokenKind.COMMA);
			
		case ';':
			takeIt();
			return(TokenKind.SEMICOLON);
			
		case '.':
			takeIt();
			return(TokenKind.PERIOD);

		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			while (isDigit(currentChar))
				takeIt();
			return(TokenKind.NUM);
		
		case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h':
		case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': case 'o': case 'p':
		case 'q': case 'r': case 's': case 't': case 'u': case 'v': case 'w': case 'x':
		case 'y': case 'z': case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
		case 'G': case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
		case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V':
		case 'W': case 'X': case 'Y': case 'Z':
			takeIt();
			while (Character.isDigit(currentChar) || Character.isLetter(currentChar) || currentChar == '_') {
				takeIt();
			}
			String curr = currentSpelling.toString();
			if (curr.equals("int")) {
				return(TokenKind.INT);
				
			} else if (curr.equals("boolean")) {
				return(TokenKind.BOOLEAN);
			
			} else if (curr.equals("false")) {
				return(TokenKind.FALSE);
				
			} else if (curr.equals("true")) {
				return(TokenKind.TRUE);
			
			} else if (curr.equals("return")) {
				return(TokenKind.RETURN);
				
			} else if (curr.equals("while")) {
				return(TokenKind.WHILE);
				
			} else if (curr.equals("if")) {
				return(TokenKind.IF);
				
			} else if (curr.equals("else")) {
				return(TokenKind.ELSE);
				
			} else if (curr.equals("this")) {
				return(TokenKind.THIS);
				
			} else if (curr.equals("public")) {
				return(TokenKind.PUBLIC);
				
			} else if (curr.equals("private")) {
				return(TokenKind.PRIVATE);
				
			} else if (curr.equals("void")) {
				return(TokenKind.VOID);
				
			} else if (curr.equals("static")) {
				return(TokenKind.STATIC);
				
			} else if (curr.equals("class")) {
				return(TokenKind.CLASS);
				
			} else if (curr.equals("new")) {
				return(TokenKind.NEW);
			} else if (curr.equals("null")) {
				return (TokenKind.NULL);
			} else {
				System.out.println(curr);
				return(TokenKind.ID);
			}
			
		default:
			scanError("Unrecognized character '" + currentChar + "' in input");
		//	System.out.println(currentChar);
			return(TokenKind.ERROR);
		}
	}

	private void takeIt() {
		currentSpelling.append(currentChar);
		
		nextChar();
	}

	private void skipIt() {
		nextChar();
	}

	private boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}

	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}


	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';

	/**
	 * advance to next char in inputstream
	 * detect end of file or end of line as end of input
	 */
	private void nextChar() {
		if (!eot)
			readChar();
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
//			currentChar = (char) inputStream.indexOf(this.index);
	//		this.index = this.index + 1;
			
			// || currentChar == eolUnix || currentChar == eolWindows
		
			if (c == -1) {
				eot = true;
			}
//			if (this.index >= inputStream.length() || currentChar == eolUnix || currentChar == eolWindows) {
//				eot = true;
//			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}
}


