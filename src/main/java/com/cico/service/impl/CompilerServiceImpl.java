package com.cico.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cico.payload.CompilerRequest;
import com.cico.payload.CompilerResponse;
import com.cico.service.ICompilerService;

@Service
public class CompilerServiceImpl implements ICompilerService{

	@Override
	public CompilerResponse compileCode(CompilerRequest compilerRequest) {
		String language = compilerRequest.getLanguage();
        String code = compilerRequest.getCode();
        String result;
        CompilerResponse compilerResponse = new CompilerResponse();
        
        try {
            // Handle Java separately from other languages
            if ("java".equalsIgnoreCase(language)) {
                result = compileAndRunJava(code, compilerRequest.getFileName());
            } else if ("c".equalsIgnoreCase(language)) {
                result = compileAndRunC(code);
            } else if ("cpp".equalsIgnoreCase(language)) {
                result = compileAndRunCpp(code);
            } else if ("python".equalsIgnoreCase(language)) {
                result = runPython(code);
            } else {
                throw new IllegalArgumentException("Unsupported language: " + language);
            }
            compilerResponse.setResponse(result);
            compilerResponse.setType("Success");
        } catch (IOException | InterruptedException e) {
            result = "Error: " + e.getMessage();
        }
        
		return compilerResponse;
	}
	
	  // Java compilation and execution
    private  String compileAndRunJava(String code, String fileName) throws IOException, InterruptedException {
        if (fileName == null || fileName.isEmpty()) {
            fileName = "JavaProgram_" + UUID.randomUUID();
        }
        
        fileName = fileName.replace(".java", "");
        String dirName= fileName+ UUID.randomUUID();
        // Create a directory for the Java program
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();  // Create the directory
        }

        String javaFileName = dirName+ File.separator + fileName+".java";  // Path for the .java file
        String className = fileName;  // Class name (same as the directory name)

        try {
        	saveCodeToFile(javaFileName, code);
      
            // Compile Java file
            String compileCommand = String.format("cmd.exe /c javac %s", javaFileName);
            String compileOutput = executeCommand(compileCommand);

            if (!compileOutput.isEmpty()) {
            	String substring = compileOutput.substring(41);// Remove extra Character
                return substring;  // Return the compilation errors if any
            }
            
            // Run the compiled Java class
            String runCommand = String.format("cmd.exe /c java -cp %s %s", dir, className );
            return executeCommand(runCommand);  // Execute the Java program
        } finally {
            // Delete the folder and its contents
            deleteDirectory(dir);
        }
    }

    // C compilation and execution
    private String compileAndRunC(String code) throws IOException, InterruptedException {
        String cFileName = "CProgram_" + UUID.randomUUID() + ".c";
        String compileFileName  = cFileName.replace(".c", "");

        try {
            saveCodeToFile(cFileName, code);

            // Compile C file
            String compileCommand = String.format("cmd.exe /c gcc %s -o %s", cFileName,compileFileName);
            String compileOutput = executeCommand(compileCommand);

            if (!compileOutput.isEmpty()) {
                return compileOutput;
            }

            // Run the compiled C program
            String runCommand = "cmd.exe /c "+ compileFileName;
            return executeCommand(runCommand);
        } finally {
            deleteFile(cFileName);
            deleteFile(compileFileName + ".exe");
        }
    }

    // C++ compilation and execution
    private  String compileAndRunCpp(String code) throws IOException, InterruptedException {
        String cppFileName = "CppProgram_" + UUID.randomUUID() + ".cpp";
        String compileFileName  = cppFileName.replace(".cpp", "");

        try {
            saveCodeToFile(cppFileName, code);

            // Compile C++ file
            String compileCommand = String.format("cmd.exe /c g++ %s -o %s",cppFileName, compileFileName);
            String compileOutput = executeCommand(compileCommand);

            if (!compileOutput.isEmpty()) {
                return compileOutput;
            }

            // Run the compiled C++ program
            String runCommand = "cmd.exe /c "+ compileFileName;
            return executeCommand(runCommand);
        } finally {
            deleteFile(cppFileName);
            deleteFile(compileFileName + ".exe");
        }
    }

    // Python execution (no compilation needed)
    private String runPython(String code) throws IOException, InterruptedException {
        String pyFileName = "PythonProgram_" + UUID.randomUUID() + ".py";

        try {
            saveCodeToFile(pyFileName, code);

            // Run Python file
            String runCommand = String.format("cmd.exe /c python %s", pyFileName);
            return executeCommand(runCommand);
        } finally {
            deleteFile(pyFileName);
        }
    }

    // Save code to a file
    private void saveCodeToFile(String fileName, String code) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(code);
        }
    }


    // Method to execute a command on Windows and return the output
    private String executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder output = new StringBuilder();
        String line;

        // Read standard output
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Read error output
        while ((line = errorReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        process.waitFor();
        return output.toString();
    }
    
    private synchronized String executeCommandSync(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder output = new StringBuilder();
        String line;

        // Read standard output
        while ((line = outputReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Read error output
        while ((line = errorReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        process.waitFor();
        return output.toString();
    }

    // Helper method to delete a file
    private void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    private  void deleteDirectory(File dir) throws IOException {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);  // Recursively delete subdirectories
                    } else {
                        file.delete();  // Delete files
                    }
                }
            }
            dir.delete();  // Delete the directory itself
        }
    }
}
