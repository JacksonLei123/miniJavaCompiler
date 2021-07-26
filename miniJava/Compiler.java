package miniJava;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import mJAM.Disassembler;
import mJAM.Interpreter;
import mJAM.ObjectFile;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.CodeGenerator.CodeGenerator;
import miniJava.ContextualAnalysis.Identification;
import miniJava.ContextualAnalysis.IdentificationTable;
import miniJava.ContextualAnalysis.TypeChecking;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException; 

public class Compiler {
	
	public static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream in = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }
 
        return sb.toString();
    }
	
	public static void main(String[] args) {
		
		

		/*
		 * note that a compiler should read the file specified by args[0]
		 * instead of reading from the keyboard!
		 */
	//	System.out.print("Enter arithmetic expression: ");
	//	String filename = args[0];
	//	System.out.println(System.getProperty("user.dir"));
		
//		File file = new File("src/miniJava/Test1.txt");
		
//		InputStream inputStream = System.in;
//		args = new String[1];
//		File file = new File("src/miniJava/Test1.txt");
//		 
//        String content = null;
//        try {
//            content = readFile(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        args[0] = content;
	
		InputStream inputStream = null;
	    try {
	    	inputStream = new FileInputStream(args[0]);
	        } catch (FileNotFoundException e) {
	               System.out.println("Input file " + args[0] + " not found");
	               System.exit(1);
	             }
		
//		InputStream inputStream = new File("Test1.txt");
		
//		Charset charset = StandardCharsets.UTF_8;
//		String content = null;
//		
//		try (InputStream in = new FileInputStream(file)) {
//            byte[] bytes = new byte[(int) file.length()];
//            in.read(bytes);
// 
//            content = new String(bytes, charset);
//          //  System.out.println(content);
// 
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
		
		
		
//		try {
//            content = readFile(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//		
//		content.replace("\uFEFF", "");
//		System.out.println(content);
//		
		
		ErrorReporter reporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, reporter);
		Parser parser = new Parser(scanner, reporter);
		
		System.out.println("Syntactic analysis ... ");
		AST ast = parser.parse();
		System.out.print("Syntactic analysis complete:  ");
		
		if (reporter.hasErrors()) {
			System.out.println("INVALID miniJava program");
			// return code for invalid input
			System.exit(4);
		}
		else {
			System.out.println("valid miniJava program");
			// return code for valid input
			ASTDisplay td = new ASTDisplay();
			td.showTree(ast);
			
			Identification identification = new Identification((Package) ast, reporter);
			TypeChecking typechecking = new TypeChecking(identification, reporter);
			CodeGenerator codegenerator = new CodeGenerator(reporter);
			identification.startVisit(ast);
			
			if (reporter.hasErrors()) {
				System.out.println("error in identification");
				// return code for invalid input
				System.exit(4);
			} else {
				typechecking.typeCheck(ast);
				if (reporter.hasErrors()) {
					System.exit(4);
				}
				
			}
			
			codegenerator.generate(ast);
			
			/*
			 * write code to object code file (.mJAM)
			 */
//				String objectCodeFileName = args[0];
				String objectCodeFileName = "Counter.mJAM";
//				objectCodeFileName = objectCodeFileName.replace(".java", ".mJAM");
				ObjectFile objF = new ObjectFile(objectCodeFileName);
				System.out.print("Writing object code file " + objectCodeFileName + " ... ");
				if (objF.write()) {
					
					System.out.println("FAILED!");
					return;
				}
				else
					
					System.out.println("SUCCEEDED");	
				
			
		/****************************************************************************************
		 * The pa4 code generator should write an object file as shown above 
		 * at the end of code generation and exit(0) if there are no errors.
		 * 
		 * During development of the code generator you may want to run the generated code 
		 * directly after code generation using the mJAM debugger mode.   You can adapt the 
		 * following code for this (it assumes objectCodeFileName holds the name of the 
		 * objectFile)
		 * 
		 */
				 // create asm file corresponding to object code using disassembler 
		        String asmCodeFileName = objectCodeFileName.replace(".mJAM",".asm");
		        System.out.print("Writing assembly file " + asmCodeFileName + " ... ");
		        Disassembler d = new Disassembler(objectCodeFileName);
		        if (d.disassemble()) {
		                System.out.println("FAILED!");
		                return;
		        }
		        else
		                System.out.println("SUCCEEDED");

		/* 
		 * run code using debugger
		 * 
		 */
		        System.out.println("Running code in debugger ... ");
		        Interpreter.debug(objectCodeFileName, asmCodeFileName);

		        System.out.println("*** mJAM execution completed");
		
			
			System.exit(0);
		}
	}

}
